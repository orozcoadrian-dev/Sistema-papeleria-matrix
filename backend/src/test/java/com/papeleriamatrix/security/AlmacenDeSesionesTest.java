package com.papeleriamatrix.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.papeleriamatrix.model.RolUsuario;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

/**
 * Prueba que la sesión se cierra sola después de 15 minutos sin uso y que usarla le renueva el tiempo.
 */
class AlmacenDeSesionesTest
{
	/** Reloj que avanza solo cuando la prueba lo pide. */
	private static final class RelojDePrueba extends Clock
	{
		private Instant ahora = Instant.parse("2026-11-23T08:00:00Z");

		void avanzar(Duration tiempo)
		{
			ahora = ahora.plus(tiempo);
		}

		@Override
		public Instant instant()
		{
			return ahora;
		}

		@Override
		public ZoneId getZone()
		{
			return ZoneOffset.UTC;
		}

		@Override
		public Clock withZone(ZoneId zona)
		{
			return this;
		}
	}

	private final RelojDePrueba reloj = new RelojDePrueba();
	private final AlmacenDeSesiones almacen = new AlmacenDeSesiones(Duration.ofMinutes(15), reloj);
	private final Sesion sesion = new Sesion(1, "admin.matrix", "Marta Gómez", RolUsuario.ADMINISTRADOR);

	@Test
	void seCierraDespuesDeQuinceMinutosSinUso()
	{
		String codigo = almacen.abrir(sesion);
		reloj.avanzar(Duration.ofMinutes(15).plusSeconds(1));
		assertFalse(almacen.usar(codigo).isPresent());
	}

	@Test
	void usarlaLeRenuevaElTiempo()
	{
		String codigo = almacen.abrir(sesion);
		reloj.avanzar(Duration.ofMinutes(10));
		assertTrue(almacen.usar(codigo).isPresent());
		reloj.avanzar(Duration.ofMinutes(10));
		assertTrue(almacen.usar(codigo).isPresent());
	}

	@Test
	void cerrarDelUsuarioCierraTodasSusSesiones()
	{
		String primera = almacen.abrir(sesion);
		String segunda = almacen.abrir(sesion);
		almacen.cerrarDelUsuario(1);
		assertFalse(almacen.usar(primera).isPresent());
		assertFalse(almacen.usar(segunda).isPresent());
	}

	@Test
	void unCodigoInventadoNoSirve()
	{
		almacen.abrir(sesion);
		assertFalse(almacen.usar("codigo-inventado").isPresent());
		assertFalse(almacen.usar(null).isPresent());
	}
}
