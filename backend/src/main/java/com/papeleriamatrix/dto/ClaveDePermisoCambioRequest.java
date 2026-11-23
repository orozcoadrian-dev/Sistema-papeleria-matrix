package com.papeleriamatrix.dto;

import jakarta.validation.constraints.NotBlank;

public record ClaveDePermisoCambioRequest(
	@NotBlank(message = "La nueva clave es obligatoria")
	String nuevaClave
)
{
}