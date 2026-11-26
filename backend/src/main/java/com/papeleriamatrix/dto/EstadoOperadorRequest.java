package com.papeleriamatrix.dto;

import jakarta.validation.constraints.NotBlank;

/** El nuevo estado de un operador: Activo, Suspendido o Despedido. */
public record EstadoOperadorRequest(
	@NotBlank(message = "El estado es obligatorio.")
	String estado)
{
}
