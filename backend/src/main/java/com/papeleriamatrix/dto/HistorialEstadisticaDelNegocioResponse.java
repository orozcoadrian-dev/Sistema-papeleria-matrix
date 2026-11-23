package com.papeleriamatrix.dto;

import com.papeleriamatrix.model.Historial_estadistica_del_negocio;
import java.time.LocalDateTime;

public record HistorialEstadisticaDelNegocioResponse(
	int idHistorialEstadistica,
	int estadisticaDelNegocioId,
	String motivoEstadistica,
	LocalDateTime fechaGeneracion)
{
	public static HistorialEstadisticaDelNegocioResponse desde(Historial_estadistica_del_negocio historial)
	{
		return new HistorialEstadisticaDelNegocioResponse(
			historial.getIdHistorialEstadistica(),
			historial.getEstadisticaDelNegocioId(),
			historial.getMotivoEstadistica(),
			historial.getFechaGeneracion()
		);
	}
}
