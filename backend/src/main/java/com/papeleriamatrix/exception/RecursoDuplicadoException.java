package com.papeleriamatrix.exception;

/**
 * Se lanza cuando se intenta guardar un código que ya existe.
 */
public class RecursoDuplicadoException extends RuntimeException
{
	public RecursoDuplicadoException(String mensaje)
	{
		super(mensaje);
	}
}
