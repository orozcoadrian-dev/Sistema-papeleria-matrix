package com.papeleriamatrix.model;

import com.papeleriamatrix.common.Utils;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "ingreso_del_negocio")
public class Ingreso_del_negocio 
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Id_ingreso")
	private Integer idIngreso;

	@Column(name = "Codigo_ingreso", nullable = false, unique = true, length = 15)
	private String codigoIngreso = "";

	@Column(name = "Total_salario_operadores")
	private Double totalSalarioOperadores;

	@Column(name = "Total_vendido_ropa")
	private Double totalVendidoRopa;

	@Column(name = "Inversion_total_papeleria")
	private Double inversionTotalPapeleria;

	@Column(name = "Total_vendido_servicios")
	private Double totalVendidoServicios;

	@Column(name = "Total_vendido_productos")
	private Double totalVendidoProductos;

	@Column(name = "Total_recaudo_esperado_papeleria")
	private Double totalRecaudoEsperadoPapeleria;

	@Column(name = "Tipo_ingreso", nullable = false, length = 15)
	private String tipoIngreso = "";

	@Transient
	private Utils utilidades = new Utils();

	public Ingreso_del_negocio()
	{
	}

	public Ingreso_del_negocio(int idIngreso, String codigoIngreso, double totalSalarioOperadores, double totalVendidoRopa, double inversionTotalPapeleria, double totalVendidoServicios, double totalVendidoProductos, double totalRecaudoEsperadoPapeleria, String tipoIngreso)
	{
		this.idIngreso = idIngreso;
		setCodigoIngreso(codigoIngreso);
		setTotalSalarioOperadores(totalSalarioOperadores);
		setTotalVendidoRopa(totalVendidoRopa);
		setInversionTotalPapeleria(inversionTotalPapeleria);
		setTotalVendidoServicios(totalVendidoServicios);
		setTotalVendidoProductos(totalVendidoProductos);
		setTotalRecaudoEsperadoPapeleria(totalRecaudoEsperadoPapeleria);
		setTipoIngreso(tipoIngreso);
	}

	public Ingreso_del_negocio(String codigoIngreso, double totalSalarioOperadores, double totalVendidoRopa, double inversionTotalPapeleria, double totalVendidoServicios, double totalVendidoProductos, double totalRecaudoEsperadoPapeleria, String tipoIngreso)
	{
		setCodigoIngreso(codigoIngreso);
		setTotalSalarioOperadores(totalSalarioOperadores);
		setTotalVendidoRopa(totalVendidoRopa);
		setInversionTotalPapeleria(inversionTotalPapeleria);
		setTotalVendidoServicios(totalVendidoServicios);
		setTotalVendidoProductos(totalVendidoProductos);
		setTotalRecaudoEsperadoPapeleria(totalRecaudoEsperadoPapeleria);
		setTipoIngreso(tipoIngreso);
	}

	public int getIdIngreso()
	{
		return idIngreso != null ? idIngreso : 0;
	}

	public void setIdIngreso(int idIngreso)
	{
		this.idIngreso = idIngreso;
	}

	public void setCodigoIngreso(String codigoIngreso) throws IllegalArgumentException
	{
		if (codigoIngreso == null || codigoIngreso.isEmpty() || codigoIngreso.isBlank())
		{
			throw new IllegalArgumentException("El código de ingreso no puede estar vacío.");
		} else if (codigoIngreso.length() < 10)
		{
			throw new IllegalArgumentException("La longitud del código de ingreso es demasiado corta.");
		} else if (codigoIngreso.length() > 15)
		{
			throw new IllegalArgumentException("El código de ingreso no puede superar una longitud de 15 caracteres.");
		} else if (utilidades.tieneSoloNumeros(codigoIngreso))
		{
			this.codigoIngreso = codigoIngreso;
		}
	}

	public String getCodigoIngreso()
	{
		return codigoIngreso;
	}

	public void setTotalSalarioOperadores(double totalSalarioOperadores) throws IllegalArgumentException
	{
		if (totalSalarioOperadores <= -1)
		{
			throw new IllegalArgumentException("El total de salario de operadores no puede ser negativo.");
		}
		this.totalSalarioOperadores = totalSalarioOperadores;
	}

	public double getTotalSalarioOperadores()
	{
		return totalSalarioOperadores != null ? totalSalarioOperadores : 0;
	}

	public void setTotalVendidoRopa(double totalVendidoRopa) throws IllegalArgumentException
	{
		if (totalVendidoRopa <= -1)
		{
			throw new IllegalArgumentException("El total vendido de ropa no puede ser negativo.");
		}
		this.totalVendidoRopa = totalVendidoRopa;
	}

	public double getTotalVendidoRopa()
	{
		return totalVendidoRopa != null ? totalVendidoRopa : 0;
	}

	public void setInversionTotalPapeleria(double inversionTotalPapeleria) throws IllegalArgumentException
	{
		if (inversionTotalPapeleria <= -1)
		{
			throw new IllegalArgumentException("La inversión total de papelería no puede ser negativo.");
		}
		this.inversionTotalPapeleria = inversionTotalPapeleria;
	}

	public double getInversionTotalPapeleria()
	{
		return inversionTotalPapeleria != null ? inversionTotalPapeleria : 0;
	}

	public void setTotalVendidoServicios(double totalVendidoServicios) throws IllegalArgumentException
	{
		if (totalVendidoServicios <= -1)
		{
			throw new IllegalArgumentException("El total vendido de servicios no puede ser negativo.");
		}
		this.totalVendidoServicios = totalVendidoServicios;
	}

	public double getTotalVendidoServicios()
	{
		return totalVendidoServicios != null ? totalVendidoServicios : 0;
	}

	public void setTotalVendidoProductos(double totalVendidoProductos) throws IllegalArgumentException
	{
		if (totalVendidoProductos <= -1)
		{
			throw new IllegalArgumentException("El total vendido de productos no puede ser negativo.");
		}
		this.totalVendidoProductos = totalVendidoProductos;
	}

	public double getTotalVendidoProductos()
	{
		return totalVendidoProductos != null ? totalVendidoProductos : 0;
	}

	public void setTotalRecaudoEsperadoPapeleria(double totalRecaudoEsperadoPapeleria) throws IllegalArgumentException
	{
		if (totalRecaudoEsperadoPapeleria <= -1)
		{
			throw new IllegalArgumentException("El total de recaudo esperado de papelería no puede ser negativo.");
		}
		this.totalRecaudoEsperadoPapeleria = totalRecaudoEsperadoPapeleria;
	}

	public double getTotalRecaudoEsperadoPapeleria()
	{
		return totalRecaudoEsperadoPapeleria != null ? totalRecaudoEsperadoPapeleria : 0;
	}

	public void setTipoIngreso(String tipoIngreso) throws IllegalArgumentException
	{
		if (tipoIngreso == null || tipoIngreso.isEmpty() || tipoIngreso.isBlank())
		{
			throw new IllegalArgumentException("El tipo de ingreso no puede estar vacío.");
		} else if (tipoIngreso.length() < 10)
		{
			throw new IllegalArgumentException("La longitud del tipo de ingreso es demasiado corta.");
		} else if (tipoIngreso.length() > 15)
		{
			throw new IllegalArgumentException("El tipo de ingreso no puede superar una longitud de 15 caracteres.");
		} else if (utilidades.tieneSoloTexto(tipoIngreso))
		{
			this.tipoIngreso = tipoIngreso;
		}
	}

	public String getTipoIngreso()
	{
		return tipoIngreso;
	}
}