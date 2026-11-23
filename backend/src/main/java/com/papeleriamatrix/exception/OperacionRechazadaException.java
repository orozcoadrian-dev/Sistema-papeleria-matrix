package com.papeleriamatrix.exception;

import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando la petición está bien escrita pero no se puede hacer: datos de entrada incorrectos,
 * falta de permiso, demasiados intentos o no hay suficientes existencias. El mensaje se muestra tal cual.
 */
public class OperacionRechazadaException extends RuntimeException
{
	private final HttpStatus estado;
	private final String titulo;

	public OperacionRechazadaException(HttpStatus estado, String titulo, String mensaje)
	{
		super(mensaje);
		this.estado = estado;
		this.titulo = titulo;
	}

	public HttpStatus getEstado()
	{
		return estado;
	}

	public String getTitulo()
	{
		return titulo;
	}
}
