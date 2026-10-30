package com.papeleriamatrix.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Exige la clave de la API para crear, modificar y borrar. Consultar sigue siendo público.
 * Es una protección sencilla para la demostración. El inicio de sesión con roles
 * la reemplazará cuando se construya.
 */
@Component
public class ClaveApiFilter extends OncePerRequestFilter
{
	private static final Set<String> METODOS_DE_ESCRITURA = Set.of("POST", "PUT", "PATCH", "DELETE");

	private final byte[] claveEsperada;

	public ClaveApiFilter(@Value("${app.api-key}") String claveEsperada)
	{
		this.claveEsperada = claveEsperada.getBytes(StandardCharsets.UTF_8);
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest peticion)
	{
		boolean esEscritura = METODOS_DE_ESCRITURA.contains(peticion.getMethod());
		boolean esApi = peticion.getRequestURI().startsWith("/api/");
		return !(esEscritura && esApi);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest peticion, HttpServletResponse respuesta, FilterChain cadena) throws ServletException, IOException
	{
		String clave = peticion.getHeader("X-API-Key");

		// La comparación toma el mismo tiempo sin importar en qué letra falla.
		boolean esValida = clave != null && MessageDigest.isEqual(clave.getBytes(StandardCharsets.UTF_8), claveEsperada);

		if(!esValida)
		{
			respuesta.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			respuesta.setContentType("application/problem+json");
			respuesta.setCharacterEncoding("UTF-8");
			respuesta.getWriter().write("""
				{"title":"No autorizado","status":401,"detail":"Falta la clave de la API en la cabecera X-API-Key o es incorrecta."}""");
			return;
		}

		cadena.doFilter(peticion, respuesta);
	}
}
