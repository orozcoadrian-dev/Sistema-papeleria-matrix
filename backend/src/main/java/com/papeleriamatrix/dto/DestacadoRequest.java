package com.papeleriamatrix.dto;

import jakarta.validation.constraints.NotNull;

/** Marcar o desmarcar un artículo como destacado en la página. */
public record DestacadoRequest(
	@NotNull(message = "Indica si el artículo queda destacado.")
	Boolean destacado)
{
}
