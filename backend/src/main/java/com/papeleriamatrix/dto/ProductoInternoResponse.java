package com.papeleriamatrix.dto;

import com.papeleriamatrix.model.ProductoPapeleria;
import java.time.LocalDate;

/**
 * Producto de papelería con los datos internos del negocio: existencias, precio y lugar de compra.
 * El administrador lo ve todo; el operador de cibercafé solo las existencias (sinCostos).
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
	/** Para el operador: sin el precio ni el lugar de compra, que son datos de la administradora. */
	public ProductoInternoResponse sinCostos()
	{
		return new ProductoInternoResponse(id, codigo, nombre, descripcion, precio, cantidad, null, null, fechaPublicacion, fechaActualizacion);
	}

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
