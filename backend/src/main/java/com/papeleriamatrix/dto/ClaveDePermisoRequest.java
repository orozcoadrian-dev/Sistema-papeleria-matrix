package com.papeleriamatrix.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ClaveDePermisoRequest(
	@NotNull(message = "El ID del usuario es obligatorio.")
	@Positive(message = "El ID del usuario debe ser mayor que cero.")
	Integer usuarioId,

	@Size(min = 10, max = 30, message = "La clave debe tener entre 10 y 30 caracteres.")
	String clave)
{
}

