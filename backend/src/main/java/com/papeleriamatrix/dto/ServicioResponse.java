package com.papeleriamatrix.dto;

import com.papeleriamatrix.model.Servicio;
import java.time.LocalDate;

/**
 * Servicio tal como lo devuelve la API.
 */
public record ServicioResponse(
	Integer id,
	String codigo,
	String nombre,
	String categoria,
	Double precio,
	String descripcion,
	LocalDate fechaVinculacion,
	LocalDate fechaActualizacion)
{
	public static ServicioResponse desde(Servicio servicio)
	{
		return new ServicioResponse(
			servicio.getId(),
			servicio.getCodigoServicio(),
			servicio.getNombre(),
			servicio.getCategoria(),
			servicio.getPrecio(),
			servicio.getDescripcion(),
			servicio.getFechaVinculacion(),
			servicio.getFechaActualizacion());
	}
}
