package com.papeleriamatrix.model;

public enum RolUsuario
{
	ADMINISTRADOR("Administrador"),
	OPERADOR_DE_CIBERCAFE("Operador de cibercafé");

	private final String rol;

	RolUsuario(String rol)
	{
		this.rol = rol;
	}

	public String getRol()
	{
		return rol;
	}

	public static RolUsuario fromString(String valor)
	{
		if (valor == null)
		{
			return null;
		}
		for (RolUsuario rolUsuario : values())
		{
			if (rolUsuario.rol.equalsIgnoreCase(valor) || rolUsuario.name().equalsIgnoreCase(valor))
			{
				return rolUsuario;
			}
		}
		throw new IllegalArgumentException("Rol desconocido: " + valor);
	}
}
