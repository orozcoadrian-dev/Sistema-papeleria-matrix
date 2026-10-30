package com.papeleriamatrix.dto;

import com.papeleriamatrix.model.ProductoPapeleria;

/**
 * Producto de papelería tal como lo ve el público. No incluye el precio de compra
 * ni el lugar de compra, que son información interna del negocio.
 */
public record ProductoResponse(
	Integer id,
	String codigo,
	String nombre,
	String descripcion,
	Double precio,
	boolean disponible)
{
	public static ProductoResponse desde(ProductoPapeleria producto)
	{
		return new ProductoResponse(
			producto.getId(),
			producto.getCodigoProducto(),
			producto.getNombre(),
			producto.getDescripcion(),
			producto.getPrecioVenta(),
			producto.getCantidad() > 0);
	}
}
