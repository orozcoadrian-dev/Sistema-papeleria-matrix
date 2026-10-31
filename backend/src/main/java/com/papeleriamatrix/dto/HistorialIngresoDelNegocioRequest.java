package com.papeleriamatrix.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record HistorialIngresoDelNegocioRequest(
	@NotNull(message = "El ID del ingreso es obligatorio.")
	@Positive(message = "El ID del ingreso debe ser mayor que cero.")
	Integer ingresoId,

	@NotBlank(message = "El motivo del ingreso es obligatorio.")
	@Size(min = 20, max = 100, message = "El motivo del ingreso debe tener entre 20 y 100 caracteres.")
	String motivoIngreso,

	@NotNull(message = "La fecha de generación es obligatoria.")
	LocalDateTime fechaGeneracion)
{
}

