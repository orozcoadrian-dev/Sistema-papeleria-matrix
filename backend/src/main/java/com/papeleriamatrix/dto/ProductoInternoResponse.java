package com.papeleriamatrix.dto;

import com.papeleriamatrix.model.ProductoPapeleria;
import java.time.LocalDate;

/**
 * Producto de papelería con los datos internos del negocio: existencias, precio y lugar de compra.
 * Solo lo ven el administrador y el operador de cibercafé.
 */
public record ProductoInternoResponse(
	Integer id,
	String codigo,
	String nombre,
	String descripcion,
	Double precio,
	Integer cantidad,
	Double precioCompra,
	String lugarCompra,
	LocalDate fechaPublicacion,
	LocalDate fechaActualizacion)
{
	public static ProductoInternoResponse desde(ProductoPapeleria producto)
	{
		return new ProductoInternoResponse(
			producto.getId(),
			producto.getCodigoProducto(),
			producto.getNombre(),
			producto.getDescripcion(),
			producto.getPrecioVenta(),
			producto.getCantidad(),
			producto.getPrecioCompra(),
			producto.getLugarCompra(),
			producto.getFechaPublicacion(),
			producto.getFechaActualizacion());
	}
}
