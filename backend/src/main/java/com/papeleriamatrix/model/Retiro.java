package com.papeleriamatrix.model;

/** Reglas comunes al retirar algo del catálogo o de la información del negocio. */
final class Retiro
{
	private static final int LARGO_MAXIMO = 150;

	private Retiro()
	{
	}

	/** El motivo es opcional. Vacío se guarda como «sin motivo». */
	static String motivo(String texto)
	{
		if (texto == null || texto.isBlank())
		{
			return null;
		}
		String limpio = texto.trim();
		if (limpio.length() > LARGO_MAXIMO)
		{
			throw new IllegalArgumentException("El motivo no puede superar los " + LARGO_MAXIMO + " caracteres.");
		}
		return limpio;
	}
}
