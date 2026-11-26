package com.papeleriamatrix.model;

import org.hibernate.annotations.ColumnDefault;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

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

	// Retirar no borra: deja de mostrarse y lo anterior lo sigue nombrando.
	@Column(name = "Activo", nullable = false)
	@ColumnDefault("true")
	private boolean activo = true;

	@Column(name = "Motivo_retiro", length = 150)
	private String motivoRetiro;

	// La administradora lo marca para que salga entre los destacados de la página.
	@Column(name = "Destacado", nullable = false)
	@ColumnDefault("false")
	private boolean destacado = false;

	protected ProductoPapeleria()
	{
	}

	public ProductoPapeleria(String codigoProducto, String nombre, Integer cantidad, Double precioVenta, Double precioCompra, String descripcion, String lugarCompra)
	{
		this.codigoProducto = codigoProducto;
		this.nombre = nombre;
		this.cantidad = cantidad;
		this.precioVenta = precioVenta;
		this.precioCompra = precioCompra;
		this.descripcion = descripcion;
		this.lugarCompra = lugarCompra;
		this.fechaPublicacion = LocalDate.now();
	}

	public void actualizar(String codigoProducto, String nombre, Integer cantidad, Double precioVenta, Double precioCompra, String descripcion, String lugarCompra)
	{
		this.codigoProducto = codigoProducto;
		this.nombre = nombre;
		this.cantidad = cantidad;
		this.precioVenta = precioVenta;
		this.precioCompra = precioCompra;
		this.descripcion = descripcion;
		this.lugarCompra = lugarCompra;
		this.fechaActualizacion = LocalDate.now();
	}

	/** Resta lo vendido de las existencias. Quien llama ya revisó que alcanza. */
	public void descontar(int unidades)
	{
		if (unidades <= 0 || unidades > cantidad)
		{
			throw new IllegalArgumentException("No hay suficientes existencias de " + nombre + ".");
		}
		this.cantidad = cantidad - unidades;
	}

	/** Anota cuántas unidades hay de verdad en la tienda. */
	public void ajustarExistencias(int unidades)
	{
		if (unidades < 0)
		{
			throw new IllegalArgumentException("La cantidad no puede ser negativa.");
		}
		this.cantidad = unidades;
		this.fechaActualizacion = LocalDate.now();
	}

	/** Devuelve a las existencias lo de una venta anulada. */
	public void reponer(int unidades)
	{
		this.cantidad = cantidad + unidades;
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

	public boolean isActivo()
	{
		return activo;
	}

	public String getMotivoRetiro()
	{
		return motivoRetiro;
	}

	public void retirar(String motivo)
	{
		this.motivoRetiro = Retiro.motivo(motivo);
		this.activo = false;
		this.destacado = false;
	}

	public boolean isDestacado()
	{
		return destacado;
	}

	public void setDestacado(boolean destacado)
	{
		this.destacado = destacado;
	}
}
