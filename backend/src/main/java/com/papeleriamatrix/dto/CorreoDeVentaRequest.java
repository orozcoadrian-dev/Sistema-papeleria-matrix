package com.papeleriamatrix.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** A quién se le envía el registro de una venta. El nombre es opcional y no se guarda. */
public record CorreoDeVentaRequest(
	@NotBlank(message = "Escribe el correo.")
	@Email(message = "El correo no tiene un formato válido.")
	@Size(max = 100, message = "El correo no puede superar los 100 caracteres.")
	String correo,
	@Size(max = 60, message = "El nombre no puede superar los 60 caracteres.")
	String nombre)
{
}
