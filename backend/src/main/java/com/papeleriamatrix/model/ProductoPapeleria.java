package com.papeleriamatrix.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

/**
 * Producto de papelería que vende el negocio.
 * Corresponde a la tabla producto_de_papeleria.
 */
@Entity
@Table(name = "producto_de_papeleria")
public class ProductoPapeleria
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Id_producto")
	private Integer id;

	@Column(name = "Codigo_producto", nullable = false, unique = true, length = 20)
	private String codigoProducto;

	@Column(name = "Nombre", nullable = false, length = 30)
	private String nombre;

	@Column(name = "Cantidad", nullable = false)
	private Integer cantidad;

	@Column(name = "Precio_venta", nullable = false)
	private Double precioVenta;

	@Column(name = "Precio_compra", nullable = false)
	private Double precioCompra;

	@Column(name = "Descripcion", length = 150)
	private String descripcion;

	@Column(name = "Lugar_compra", length = 40)
	private String lugarCompra;

	@Column(name = "Fecha_publicacion", nullable = false)
	private LocalDate fechaPublicacion;

	@Column(name = "Fecha_actualizacion")
	private LocalDate fechaActualizacion;

	// JPA necesita un constructor sin parámetros.
	protected ProductoPapeleria()
	{
	}

	public Integer getId()
	{
		return id;
	}

	public String getCodigoProducto()
	{
		return codigoProducto;
	}

	public String getNombre()
	{
		return nombre;
	}

	public Integer getCantidad()
	{
		return cantidad;
	}

	public Double getPrecioVenta()
	{
		return precioVenta;
	}

	public Double getPrecioCompra()
	{
		return precioCompra;
	}

	public String getDescripcion()
	{
		return descripcion;
	}

	public String getLugarCompra()
	{
		return lugarCompra;
	}

	public LocalDate getFechaPublicacion()
	{
		return fechaPublicacion;
	}

	public LocalDate getFechaActualizacion()
	{
		return fechaActualizacion;
	}
}
