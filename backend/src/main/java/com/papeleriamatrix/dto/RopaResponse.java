package com.papeleriamatrix.dto;

import com.papeleriamatrix.model.Ropa;

/**
 * Prenda del catálogo tal como la devuelve la API.
 */
public record RopaResponse(
	Integer id,
	String codigo,
	String nombre,
	String tipo,
	String talla,
	String color,
	String material,
	String descripcion,
	Double precio)
{
	public static RopaResponse desde(Ropa ropa)
	{
		return new RopaResponse(
			ropa.getId(),
			ropa.getCodigoRopa(),
			ropa.getNombre(),
			ropa.getTipoRopa(),
			ropa.getTalla(),
			ropa.getColor(),
			ropa.getMaterial(),
			ropa.getDescripcion(),
			ropa.getPrecio());
	}
}
