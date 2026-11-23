package com.papeleriamatrix.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record ReporteDelNegocioRequest(
	@NotBlank(message = "El código del reporte es obligatorio.")
	@Pattern(regexp = "\\d{20}", message = "El código del reporte debe tener exactamente 20 dígitos.")
	String codigoReporte,

	@PositiveOrZero(message = "El ID del historial de estadísticas no puede ser negativo.")
	Integer historialEstadisticaDelNegocioId,

	@PositiveOrZero(message = "El ID del historial de ingresos no puede ser negativo.")
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
