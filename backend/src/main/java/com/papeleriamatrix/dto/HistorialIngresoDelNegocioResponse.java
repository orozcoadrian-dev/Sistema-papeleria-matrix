package com.papeleriamatrix.dto;

import com.papeleriamatrix.model.Historial_ingreso_del_negocio;
import java.time.LocalDateTime;

public record HistorialIngresoDelNegocioResponse(
	int idHistorialIngreso,
	int ingresoId,
	String motivoIngreso,
	LocalDateTime fechaGeneracion)
{
	public static HistorialIngresoDelNegocioResponse desde(Historial_ingreso_del_negocio historial)
	{
		return new HistorialIngresoDelNegocioResponse(
			historial.getIdHistorialIngreso(),
			historial.getIngresoId(),
			historial.getMotivoIngreso(),
			historial.getFechaGeneracion()
		);
	}
}
