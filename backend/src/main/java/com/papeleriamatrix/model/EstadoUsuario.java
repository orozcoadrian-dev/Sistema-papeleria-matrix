package com.papeleriamatrix.model;

/** Cómo está una persona que entra al sistema. Solo quien está activo puede entrar. */
public enum EstadoUsuario
{
	ACTIVO("Activo"),
	SUSPENDIDO("Suspendido"),
	DESPEDIDO("Despedido");

	private final String texto;

	EstadoUsuario(String texto)
	{
		this.texto = texto;
	}

	public String getTexto()
	{
		return texto;
	}

	public static EstadoUsuario desde(String texto)
	{
		for (EstadoUsuario estado : values())
		{
			if (estado.texto.equalsIgnoreCase(texto == null ? "" : texto.trim()))
			{
				return estado;
			}
		}
		throw new IllegalArgumentException("El estado debe ser Activo, Suspendido o Despedido.");
	}
}
