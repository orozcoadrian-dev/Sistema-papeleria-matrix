package com.papeleriamatrix.security;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Guarda en memoria las sesiones abiertas. Cada sesión se identifica con un código al azar
 * que el navegador manda en la cabecera Authorization. La sesión se cierra sola cuando pasan
 * los minutos de app.sesion.minutos-inactividad sin usarla (15 por defecto).
 * Si el servidor se reinicia, todos tienen que volver a entrar.
 */
@Component
public class AlmacenDeSesiones
{
	private record Registro(Sesion sesion, Instant ultimoUso)
	{
	}

	private final Map<String, Registro> sesiones = new ConcurrentHashMap<>();
	private final SecureRandom azar = new SecureRandom();
	private final Duration inactividadMaxima;
	private final Clock reloj;

	@Autowired
	public AlmacenDeSesiones(@Value("${app.sesion.minutos-inactividad:15}") long minutos)
	{
		this(Duration.ofMinutes(minutos), Clock.systemUTC());
	}

	AlmacenDeSesiones(Duration inactividadMaxima, Clock reloj)
	{
		this.inactividadMaxima = inactividadMaxima;
		this.reloj = reloj;
	}

	public String abrir(Sesion sesion)
	{
		limpiarVencidas();
		byte[] bytes = new byte[32];
		azar.nextBytes(bytes);
		String codigo = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
		sesiones.put(codigo, new Registro(sesion, reloj.instant()));
		return codigo;
	}

	/** Devuelve la sesión si sigue vigente y le renueva el tiempo. */
	public Optional<Sesion> usar(String codigo)
	{
		if (codigo == null || codigo.isBlank())
		{
			return Optional.empty();
		}
		Instant ahora = reloj.instant();
		Registro registro = sesiones.computeIfPresent(codigo, (clave, actual) ->
			estaVencido(actual, ahora) ? null : new Registro(actual.sesion(), ahora));
		return Optional.ofNullable(registro).map(Registro::sesion);
	}

	public void cerrar(String codigo)
	{
		if (codigo != null)
		{
			sesiones.remove(codigo);
		}
	}

	/** Cierra todas las sesiones de un usuario, por ejemplo cuando se borra o cambia su contraseña. */
	public void cerrarDelUsuario(int idUsuario)
	{
		sesiones.values().removeIf(registro -> registro.sesion().idUsuario() == idUsuario);
	}

	public long minutosDeInactividad()
	{
		return inactividadMaxima.toMinutes();
	}

	private void limpiarVencidas()
	{
		Instant ahora = reloj.instant();
		sesiones.values().removeIf(registro -> estaVencido(registro, ahora));
	}

	private boolean estaVencido(Registro registro, Instant ahora)
	{
		return registro.ultimoUso().plus(inactividadMaxima).isBefore(ahora);
	}
}
