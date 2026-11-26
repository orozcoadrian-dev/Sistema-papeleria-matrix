package com.papeleriamatrix.service;

/** El servicio que de verdad entrega los correos. */
public interface EnviadorDeCorreo
{
	/** Falso mientras no se haya configurado la cuenta que envía. */
	boolean disponible();

	void enviar(String para, String asunto, String texto);
}
