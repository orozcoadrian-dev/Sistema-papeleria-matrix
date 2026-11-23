package com.papeleriamatrix.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Una venta con sus artículos. total es lo que se cobró. El precio de cada artículo es el precio actual del catálogo,
 * porque la base de datos guarda el total de la factura pero no el precio de cada línea.
 */
public record VentaResponse(
	int id,
	String codigo,
	String tipo,
	LocalDate fecha,
	double total,
	int operadorId,
	String operador,
	List<Linea> articulos)
{
	public record Linea(
		String tipo,
		Integer id,
		String nombre,
		int cantidad,
		Double precioUnitario)
	{
	}
}
