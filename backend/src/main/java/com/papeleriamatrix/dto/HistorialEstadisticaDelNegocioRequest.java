package com.papeleriamatrix.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record HistorialEstadisticaDelNegocioRequest(
	@NotNull(message = "El ID de la estadística del negocio es obligatorio.")
	@Positive(message = "El ID de la estadística debe ser mayor que cero.")
	Integer estadisticaDelNegocioId,

	@NotBlank(message = "El motivo de la estadística es obligatorio.")
	@Size(min = 20, max = 100, message = "El motivo de la estadística debe tener entre 20 y 100 caracteres.")
	String motivoEstadistica,

	@NotNull(message = "La fecha de generación es obligatoria.")
	LocalDateTime fechaGeneracion)
{
}

