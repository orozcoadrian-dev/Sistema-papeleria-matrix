package com.papeleriamatrix.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Decide quién puede usar cada ruta de la API.
 *
 * <ul>
 * <li>Público, sin entrar: consultar servicios, productos, ropa, la información del negocio y el estado, e iniciar sesión.</li>
 * <li>Operador de cibercafé: lo público, sus ventas (para cambiarlas o anularlas se pide además la clave de permiso)
 * y las existencias de los productos.</li>
 * <li>Administrador: todo.</li>
 * </ul>
 *
 * Se entra de dos formas: con la sesión que devuelve POST /api/sesion (cabecera {@code Authorization: Bearer ...})
 * o con la clave de la API en la cabecera X-API-Key, que da acceso de administrador y sirve para Postman y las pruebas.
 */
@Component
public class ClaveApiFilter extends OncePerRequestFilter
{
	private static final Set<String> METODOS_DE_CONSULTA = Set.of("GET", "HEAD");

	/** Rutas que cualquiera puede consultar, también la página pública. */
	private static final List<String> CONSULTA_PUBLICA = List.of(
		"/api/servicios", "/api/productos", "/api/ropa", "/api/informacion-negocio", "/api/estado", "/api/imagenes");

	/** Rutas de consulta que necesitan sesión aunque empiecen como una pública. */
	private static final List<String> CONSULTA_INTERNA = List.of("/api/productos/interno");

	private static final String RUTA_SESION = "/api/sesion";
	private static final String RUTA_VENTAS = "/api/ventas";

	private final byte[] claveEsperada;
	private final AlmacenDeSesiones sesiones;

	public ClaveApiFilter(@Value("${app.api-key}") String claveEsperada, AlmacenDeSesiones sesiones)
	{
		this.claveEsperada = claveEsperada.getBytes(StandardCharsets.UTF_8);
		this.sesiones = sesiones;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest peticion)
	{
		return !peticion.getRequestURI().startsWith("/api/") || "OPTIONS".equals(peticion.getMethod());
	}

	@Override
	protected void doFilterInternal(HttpServletRequest peticion, HttpServletResponse respuesta, FilterChain cadena) throws ServletException, IOException
	{
		String metodo = peticion.getMethod();
		String ruta = peticion.getRequestURI();
		String claveRecibida = peticion.getHeader("X-API-Key");

		Optional<Sesion> sesion = claveRecibida != null
			? (claveEsValida(claveRecibida) ? Optional.of(Sesion.conClaveDeLaApi()) : Optional.empty())
			: sesiones.usar(codigoDeSesion(peticion));
		sesion.ifPresent(actual -> peticion.setAttribute(Sesion.ATRIBUTO, actual));

		if (esLibre(metodo, ruta))
		{
			cadena.doFilter(peticion, respuesta);
			return;
		}

		if (sesion.isEmpty())
		{
			String detalle = claveRecibida != null
				? "La clave de la API de la cabecera X-API-Key es incorrecta."
				: "Inicia sesión para continuar. Si ya habías entrado, la sesión se cerró por inactividad.";
			responder(respuesta, HttpServletResponse.SC_UNAUTHORIZED, "No autorizado", detalle);
			return;
		}

		if (!puede(sesion.get(), metodo, ruta))
		{
			responder(respuesta, HttpServletResponse.SC_FORBIDDEN, "Sin permiso", "Tu usuario no tiene permiso para hacer esto.");
			return;
		}

		cadena.doFilter(peticion, respuesta);
	}

	private boolean esLibre(String metodo, String ruta)
	{
		if (esRuta(ruta, RUTA_SESION))
		{
			// El controlador de la sesión revisa por su cuenta si hace falta haber entrado.
			return true;
		}
		if (!METODOS_DE_CONSULTA.contains(metodo))
		{
			return false;
		}
		boolean esInterna = CONSULTA_INTERNA.stream().anyMatch(base -> esRuta(ruta, base));
		return !esInterna && CONSULTA_PUBLICA.stream().anyMatch(base -> esRuta(ruta, base));
	}

	private boolean puede(Sesion sesion, String metodo, String ruta)
	{
		if (sesion.esAdministrador())
		{
			return true;
		}
		if (sesion.esOperador())
		{
			// Qué ventas puede tocar y si tiene la clave de permiso lo revisa el servicio de ventas.
			boolean trabajaConVentas = esRuta(ruta, RUTA_VENTAS);
			boolean consultaExistencias = METODOS_DE_CONSULTA.contains(metodo) && CONSULTA_INTERNA.stream().anyMatch(base -> esRuta(ruta, base));
			return trabajaConVentas || consultaExistencias;
		}
		return false;
	}

	private static boolean esRuta(String ruta, String base)
	{
		return ruta.equals(base) || ruta.startsWith(base + "/");
	}

	private boolean claveEsValida(String clave)
	{
		// La comparación toma el mismo tiempo sin importar en qué letra falla.
		return MessageDigest.isEqual(clave.getBytes(StandardCharsets.UTF_8), claveEsperada);
	}

	static String codigoDeSesion(HttpServletRequest peticion)
	{
		String cabecera = peticion.getHeader("Authorization");
		if (cabecera == null || !cabecera.regionMatches(true, 0, "Bearer ", 0, 7))
		{
			return null;
		}
		return cabecera.substring(7).trim();
	}

	private static void responder(HttpServletResponse respuesta, int estado, String titulo, String detalle) throws IOException
	{
		respuesta.setStatus(estado);
		respuesta.setContentType("application/problem+json");
		respuesta.setCharacterEncoding("UTF-8");
		respuesta.getWriter().write("{\"title\":\"" + titulo + "\",\"status\":" + estado + ",\"detail\":\"" + detalle + "\"}");
	}
}
