package com.papeleriamatrix.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record InformacionDelNegocioRequest(
	@NotNull(message = "El ID del usuario es obligatorio.")
	@Positive(message = "El ID del usuario debe ser mayor que cero.")
	Integer usuarioId,

	@NotNull(message = "El valor de la información es obligatorio.")
	@Positive(message = "El valor de la información debe ser mayor que cero.")
	Integer valorInformacion,

	@NotBlank(message = "El tipo de información es obligatorio.")
	@Size(min = 10, max = 15, message = "El tipo de información debe tener entre 10 y 15 caracteres.")
	String tipoInformacion,

	@NotBlank(message = "La información es obligatoria.")
	@Size(min = 10, max = 255, message = "La información debe tener entre 10 y 255 caracteres.")
	String informacion)
{
}

