package com.papeleriamatrix.dto;

import com.papeleriamatrix.model.ReporteDelNegocio;
import java.time.LocalDate;

public record ReporteDelNegocioResponse(
	int idReporte,
	String codigoReporte,
	Integer historialEstadisticaDelNegocioId,
	Integer historialIngresoDelNegocioId,
	Integer usuarioId,
	String rutaArchivo,
	LocalDate fechaGeneracion
)
{
	public static ReporteDelNegocioResponse desde(ReporteDelNegocio reporte)
	{
		return new ReporteDelNegocioResponse(
			reporte.getIdReporte(),
			reporte.getCodigoReporte(),
			reporte.getHistorialEstadisticaDelNegocioId() == 0 ? null : reporte.getHistorialEstadisticaDelNegocioId(),
			reporte.getHistorialIngresoDelNegocioId() == 0 ? null : reporte.getHistorialIngresoDelNegocioId(),
			reporte.getUsuarioId() == 0 ? null : reporte.getUsuarioId(),
			reporte.getRutaArchivo(),
			reporte.getFechaGeneracion()
		);
	}
}