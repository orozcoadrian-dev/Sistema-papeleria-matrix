package com.papeleriamatrix.security;

import com.papeleriamatrix.model.RolUsuario;

/**
 * Quién hizo la petición. Se guarda en la petición después de revisar la sesión o la clave de la API.
 * Cuando se entra con la clave de la API no hay un usuario detrás: idUsuario es 0 y el rol es de administrador.
 */
public record Sesion(int idUsuario, String nombreUsuario, String nombre, RolUsuario rol)
{
	/** Nombre del atributo de la petición donde el filtro deja la sesión. */
	public static final String ATRIBUTO = "papeleriamatrix.sesion";

	public static Sesion conClaveDeLaApi()
	{
		return new Sesion(0, "clave-api", "Acceso con la clave de la API", RolUsuario.ADMINISTRADOR);
	}

	public boolean esAdministrador()
	{
		return rol == RolUsuario.ADMINISTRADOR;
	}

	public boolean esOperador()
	{
		return rol == RolUsuario.OPERADOR_DE_CIBERCAFE;
	}

	public boolean tieneUsuario()
	{
		return idUsuario > 0;
	}
}
