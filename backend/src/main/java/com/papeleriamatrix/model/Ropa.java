package com.papeleriamatrix.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "ropa")
public class Ropa
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Id_ropa")
	private Integer id;

	@Column(name = "Codigo_ropa", nullable = false, unique = true, length = 20)
	private String codigoRopa;

	@Column(name = "Tipo_ropa", nullable = false, length = 20)
	private String tipoRopa;

	@Column(name = "Material", nullable = false, length = 30)
	private String material;

	@Column(name = "Talla", nullable = false, length = 5)
	private String talla;

	@Column(name = "Descripcion", length = 150)
	private String descripcion;

	@Column(name = "Nombre", length = 30)
	private String nombre;

	@Column(name = "Precio", nullable = false)
	private Double precio;

	@Column(name = "Color", length = 40)
	private String color;

	@Column(name = "Fecha_registro", nullable = false)
	private LocalDate fechaRegistro;

	@Column(name = "Fecha_actualizacion")
	private LocalDate fechaActualizacion;

	protected Ropa()
	{
	}

	public Integer getId()
	{
		return id;
	}

	public String getCodigoRopa()
	{
		return codigoRopa;
	}

	public String getTipoRopa()
	{
		return tipoRopa;
	}

	public String getMaterial()
	{
		return material;
	}

	public String getTalla()
	{
		return talla;
	}

	public String getDescripcion()
	{
		return descripcion;
	}

	public String getNombre()
	{
		return nombre;
	}

	public Double getPrecio()
	{
		return precio;
	}

	public String getColor()
	{
		return color;
	}

	public LocalDate getFechaRegistro()
	{
		return fechaRegistro;
	}

	public LocalDate getFechaActualizacion()
	{
		return fechaActualizacion;
	}
}