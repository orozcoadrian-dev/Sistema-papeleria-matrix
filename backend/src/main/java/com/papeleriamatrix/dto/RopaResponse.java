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
	Double precio,
	boolean disponible,
	boolean destacado)
{
	/** disponible es falso cuando la prenda ya se vendió: cada prenda es única. */
	public static RopaResponse desde(Ropa ropa, boolean vendida)
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
			ropa.getPrecio(),
			!vendida,
			ropa.isDestacado());
	}

	public static RopaResponse desde(Ropa ropa)
	{
		return desde(ropa, false);
	}
}
