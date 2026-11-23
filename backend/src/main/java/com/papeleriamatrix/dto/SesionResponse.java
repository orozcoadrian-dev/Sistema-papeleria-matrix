package com.papeleriamatrix.dto;

import com.papeleriamatrix.security.Sesion;

/**
 * Lo que recibe el navegador al entrar. El código va en la cabecera Authorization: Bearer de las siguientes peticiones.
 * rol es el código (ADMINISTRADOR u OPERADOR_DE_CIBERCAFE) y rolNombre el texto para mostrar.
 */
public record SesionResponse(
	String codigo,
	int idUsuario,
	String nombreUsuario,
	String nombre,
	String rol,
	String rolNombre,
	long minutosInactividad)
{
	public static SesionResponse desde(String codigo, Sesion sesion, long minutosInactividad)
	{
		return new SesionResponse(codigo, sesion.idUsuario(), sesion.nombreUsuario(), sesion.nombre(),
			sesion.rol().name(), sesion.rol().getRol(), minutosInactividad);
	}
}
