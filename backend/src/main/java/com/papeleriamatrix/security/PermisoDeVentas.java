package com.papeleriamatrix.security;

import com.papeleriamatrix.exception.OperacionRechazadaException;
import com.papeleriamatrix.model.Clave_de_permiso;
import com.papeleriamatrix.repository.ClaveDePermisoRepository;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Pide la clave de permiso para cambiar o anular una venta. La administradora no la necesita:
 * es quien la entrega. El operador usa la suya, y tras varios intentos fallidos debe esperar.
 */
@Component
public class PermisoDeVentas
{
	/** La clave viaja codificada como en una dirección web (UTF-8), para que admita tildes y eñes. */
	public static final String CABECERA = "X-Clave-Permiso";

	private static final int INTENTOS_PERMITIDOS = 5;
	private static final Duration BLOQUEO = Duration.ofMinutes(5);

	private record Intentos(int fallidos, Instant ultimo)
	{
	}

	private final ClaveDePermisoRepository claves;
	private final PasswordEncoder codificador;
	private final Map<Integer, Intentos> intentos = new ConcurrentHashMap<>();

	public PermisoDeVentas(ClaveDePermisoRepository claves, PasswordEncoder codificador)
	{
		this.claves = claves;
		this.codificador = codificador;
	}

	public void exigir(Sesion sesion, String cabecera)
	{
		if (sesion.esAdministrador())
		{
			return;
		}
		int operador = sesion.idUsuario();
		revisarBloqueo(operador);

		Optional<Clave_de_permiso> suya = claves.findAll().stream().filter(clave -> clave.getUsuarioId() == operador).findFirst();
		if (suya.isEmpty())
		{
			throw rechazo("Sin clave de permiso", "Todavía no tienes clave de permiso. Pídele a la administradora que te cree una.");
		}
		if (cabecera == null || cabecera.isBlank())
		{
			throw rechazo("Falta la clave de permiso", "Escribe tu clave de permiso.");
		}
		if (!codificador.matches(URLDecoder.decode(cabecera, StandardCharsets.UTF_8), suya.get().getClave()))
		{
			intentos.merge(operador, new Intentos(1, Instant.now()),
				(anterior, nuevo) -> new Intentos(anterior.fallidos() + 1, nuevo.ultimo()));
			throw rechazo("Clave incorrecta", "La clave de permiso no es correcta.");
		}
		intentos.remove(operador);
	}

	private void revisarBloqueo(int operador)
	{
		Intentos actuales = intentos.get(operador);
		if (actuales == null)
		{
			return;
		}
		if (actuales.ultimo().plus(BLOQUEO).isBefore(Instant.now()))
		{
			intentos.remove(operador);
			return;
		}
		if (actuales.fallidos() >= INTENTOS_PERMITIDOS)
		{
			throw new OperacionRechazadaException(HttpStatus.TOO_MANY_REQUESTS, "Demasiados intentos",
				"Fallaste la clave de permiso varias veces. Espera 5 minutos y vuelve a intentarlo.");
		}
	}

	/** 403 y no 401: la sesión sigue siendo válida, solo falta el permiso para este cambio. */
	private static OperacionRechazadaException rechazo(String titulo, String detalle)
	{
		return new OperacionRechazadaException(HttpStatus.FORBIDDEN, titulo, detalle);
	}
}
