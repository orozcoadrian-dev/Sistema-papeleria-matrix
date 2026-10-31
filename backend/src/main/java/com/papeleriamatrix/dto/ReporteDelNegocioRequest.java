package com.papeleriamatrix.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record ReporteDelNegocioRequest(
	@NotBlank(message = "El código del reporte es obligatorio.")
	@Size(min = 20, max = 20, message = "El código del reporte debe tener exactamente 20 caracteres.")
	String codigoReporte,

	@Positive(message = "El ID del historial de estadística debe ser mayor que cero.")
	Integer historialEstadisticaDelNegocioId,

	@Positive(message = "El ID del historial de ingreso debe ser mayor que cero.")
	Integer historialIngresoDelNegocioId,

	@Positive(message = "El ID del usuario debe ser mayor que cero.")
	Integer usuarioId,

	@NotBlank(message = "La ruta del archivo es obligatoria.")
	@Size(max = 255, message = "La ruta del archivo no puede superar los 255 caracteres.")
	String rutaArchivo,

	@NotNull(message = "La fecha de generación es obligatoria.")
	LocalDate fechaGeneracion)
{
}

