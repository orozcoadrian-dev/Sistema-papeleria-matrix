package com.papeleriamatrix.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

/**
 * Servicio que ofrece el negocio, como impresiones, copias o escáner.
 * Corresponde a la tabla servicio.
 */
@Entity
@Table(name = "servicio")
public class Servicio
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Id_servicio")
	private Integer id;

	@Column(name = "Codigo_servicio", nullable = false, unique = true, length = 20)
	private String codigoServicio;

	@Column(name = "Nombre", nullable = false, length = 30)
	private String nombre;

	@Column(name = "Categoria", nullable = false, length = 30)
	private String categoria;

	@Column(name = "Precio", nullable = false)
	private Double precio;

	@Column(name = "Descripcion", length = 150)
	private String descripcion;

	@Column(name = "Fecha_vinculacion", nullable = false)
	private LocalDate fechaVinculacion;

	@Column(name = "Fecha_actualizacion")
	private LocalDate fechaActualizacion;

	// JPA necesita un constructor sin parámetros.
	protected Servicio()
	{
	}

	public Servicio(String codigoServicio, String nombre, String categoria, Double precio, String descripcion)
	{
		this.codigoServicio = codigoServicio;
		this.nombre = nombre;
		this.categoria = categoria;
		this.precio = precio;
		this.descripcion = descripcion;
		this.fechaVinculacion = LocalDate.now();
	}

	/**
	 * Cambia los datos del servicio y registra la fecha de actualización.
	 */
	public void actualizar(String codigoServicio, String nombre, String categoria, Double precio, String descripcion)
	{
		this.codigoServicio = codigoServicio;
		this.nombre = nombre;
		this.categoria = categoria;
		this.precio = precio;
		this.descripcion = descripcion;
		this.fechaActualizacion = LocalDate.now();
	}

	public Integer getId()
	{
		return id;
	}

	public String getCodigoServicio()
	{
		return codigoServicio;
	}

	public String getNombre()
	{
		return nombre;
	}

	public String getCategoria()
	{
		return categoria;
	}

	public Double getPrecio()
	{
		return precio;
	}

	public String getDescripcion()
	{
		return descripcion;
	}

	public LocalDate getFechaVinculacion()
	{
		return fechaVinculacion;
	}

	public LocalDate getFechaActualizacion()
	{
		return fechaActualizacion;
	}
}
