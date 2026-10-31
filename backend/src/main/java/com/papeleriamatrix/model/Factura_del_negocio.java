package com.papeleriamatrix.model;

import com.papeleriamatrix.common.Utils;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Entity
@Table(name = "factura_del_negocio")
public class Factura_del_negocio 
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Id_factura")
	private Integer idFactura;

	@Column(name = "Codigo_factura", nullable = false, unique = true, length = 15)
	private String codigoFactura = "";

	@Column(name = "Tipo_factura", nullable = false, length = 15)
	private String tipoFactura = "";

	@Column(name = "Usuario_id", nullable = false)
	private Integer usuarioId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Usuario_id", insertable = false, updatable = false)
	private Operador_de_cibercafe operador;

	@Column(name = "Monto", nullable = false)
	private double monto = 0;

	@Column(name = "Fecha_generacion", nullable = false)
	private LocalDate fechaGeneracion;

	@Column(name = "Fecha_actualizacion")
	private LocalDate fechaActualizacion;

	@Transient
	private Utils utilidades = new Utils();

	public Factura_del_negocio()
	{
	}

	public Factura_del_negocio(int idFactura, String codigoFactura, String tipoFactura, int usuarioId, double monto, LocalDate fechaGeneracion, LocalDate fechaActualizacion)
	{
		this.idFactura = idFactura;
		setCodigoFactura(codigoFactura);
		setTipoFactura(tipoFactura);
		setUsuarioId(usuarioId);
		setMonto(monto);
		setFechaGeneracion(fechaGeneracion);
		setFechaActualizacion(fechaActualizacion);
	}

	public Factura_del_negocio(String codigoFactura, String tipoFactura, int usuarioId, double monto, LocalDate fechaGeneracion, LocalDate fechaActualizacion)
	{
		setCodigoFactura(codigoFactura);
		setTipoFactura(tipoFactura);
		setUsuarioId(usuarioId);
		setMonto(monto);
		setFechaGeneracion(fechaGeneracion);
		setFechaActualizacion(fechaActualizacion);
	}

	public Factura_del_negocio(int idFactura, String codigoFactura, String tipoFactura, int usuarioId, double monto, Date fechaGeneracion, Date fechaActualizacion)
	{
		this.idFactura = idFactura;
		setCodigoFactura(codigoFactura);
		setTipoFactura(tipoFactura);
		setUsuarioId(usuarioId);
		setMonto(monto);
		setFechaGeneracion(fechaGeneracion);
		setFechaActualizacion(fechaActualizacion);
	}

	public Factura_del_negocio(String codigoFactura, String tipoFactura, int usuarioId, double monto, Date fechaGeneracion, Date fechaActualizacion)
	{
		setCodigoFactura(codigoFactura);
		setTipoFactura(tipoFactura);
		setUsuarioId(usuarioId);
		setMonto(monto);
		setFechaGeneracion(fechaGeneracion);
		setFechaActualizacion(fechaActualizacion);
	}

	public int getIdFactura()
	{
		return idFactura != null ? idFactura : 0;
	}

	public void setIdFactura(int idFactura)
	{
		this.idFactura = idFactura;
	}

	public void setCodigoFactura(String codigoFactura) throws IllegalArgumentException
	{
		if (codigoFactura == null || codigoFactura.isBlank() || codigoFactura.isEmpty())
		{
			throw new IllegalArgumentException("El código de la factura no puede ir vacío.");
		} else if (codigoFactura.length() != 15)
		{
			throw new IllegalArgumentException("El código de la factura debe tener una longitud de 15 caracteres.");
		} else if (utilidades.tieneSoloNumeros(codigoFactura))
		{
			this.codigoFactura = codigoFactura;
		}
	}

	public String getCodigoFactura()
	{
		return codigoFactura;
	}

	public void setTipoFactura(String tipoFactura) throws IllegalArgumentException
	{
		if (tipoFactura == null || tipoFactura.isBlank() || tipoFactura.isEmpty())
		{
			throw new IllegalArgumentException("El tipo de la factura no puede ir vacío.");
		} else if (tipoFactura.length() < 8)
		{
			throw new IllegalArgumentException("El tipo de la factura tiene una longitud muy corta.");
		} else if (tipoFactura.length() > 15)
		{
			throw new IllegalArgumentException("El tipo de la factura no puede tener una longitud superior a 15 caracteres.");
		} else if (utilidades.tieneSoloTexto(tipoFactura))
		{
			this.tipoFactura = tipoFactura;
		}
	}

	public String getTipoFactura()
	{
		return tipoFactura;
	}

	public void setUsuarioId(int usuarioId) throws IllegalArgumentException
	{
		if (usuarioId == 0)
		{
			throw new IllegalArgumentException("El ID del usuario no puede ser 0.");
		} else if (usuarioId <= -1)
		{
			throw new IllegalArgumentException("El ID del usuario no puede ser negativo.");
		}
		this.usuarioId = usuarioId;
	}

	public int getUsuarioId()
	{
		return usuarioId != null ? usuarioId : 0;
	}

	public Operador_de_cibercafe getOperador()
	{
		return operador;
	}

	public void setOperador(Operador_de_cibercafe operador)
	{
		this.operador = operador;
		if (operador != null)
		{
			this.usuarioId = operador.getIdUsuario();
		}
	}

	public void setMonto(double monto) throws IllegalArgumentException
	{
		if (monto == 0)
		{
			throw new IllegalArgumentException("El monto no puede ser igual a 0.");
		} else if (monto <= -1)
		{
			throw new IllegalArgumentException("El monto no puede ser negativo.");
		}
		this.monto = monto;
	}

	public double getMonto()
	{
		return monto;
	}

	public void setFechaGeneracion(LocalDate fechaGeneracion) throws IllegalArgumentException
	{
		if (fechaGeneracion == null)
		{
			throw new IllegalArgumentException("La fecha de generación no puede ir vacía.");
		}
		this.fechaGeneracion = fechaGeneracion;
	}

	public void setFechaGeneracion(Date fechaGeneracion) throws IllegalArgumentException
	{
		if (fechaGeneracion == null)
		{
			throw new IllegalArgumentException("La fecha de generación no puede ir vacía.");
		}
		this.fechaGeneracion = fechaGeneracion.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}

	public LocalDate getFechaGeneracion()
	{
		return fechaGeneracion;
	}

	public void setFechaActualizacion(LocalDate fechaActualizacion)
	{
		this.fechaActualizacion = fechaActualizacion;
	}

	public void setFechaActualizacion(Date fechaActualizacion)
	{
		if (fechaActualizacion != null)
		{
			this.fechaActualizacion = fechaActualizacion.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		} else
		{
			this.fechaActualizacion = null;
		}
	}

	public LocalDate getFechaActualizacion()
	{
		return fechaActualizacion;
	}
}