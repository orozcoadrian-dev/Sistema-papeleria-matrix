package com.papeleriamatrix.exception;

/**
 * Se lanza cuando se pide un servicio, producto o prenda que no existe.
 */
public class RecursoNoEncontradoException extends RuntimeException
{
	public RecursoNoEncontradoException(String mensaje)
	{
		super(mensaje);
	}
}
