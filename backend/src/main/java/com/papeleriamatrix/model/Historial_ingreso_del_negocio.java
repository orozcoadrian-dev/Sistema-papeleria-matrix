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
import java.time.LocalDateTime;

@Entity
@Table(name = "historial_ingreso_del_negocio")
public class Historial_ingreso_del_negocio 
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Id_historial_ingreso")
	private Integer idHistorialIngreso;

	@Column(name = "Ingreso_id", nullable = false)
	private Integer ingresoId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Ingreso_id", insertable = false, updatable = false)
	private Ingreso_del_negocio ingreso;

	@Column(name = "Motivo_ingreso", nullable = false, length = 100)
	private String motivoIngreso = "";

	@Column(name = "Fecha_generacion", nullable = false)
	private LocalDateTime fechaGeneracion;

	@Transient
	private Utils utilidades = new Utils();

	public Historial_ingreso_del_negocio()
	{
	}

	public Historial_ingreso_del_negocio(int idHistorialIngreso, int ingresoId, String motivoIngreso, LocalDateTime fechaGeneracion)
	{
		this.idHistorialIngreso = idHistorialIngreso;
		setIngresoId(ingresoId);
		setMotivoIngreso(motivoIngreso);
		setFechaGeneracion(fechaGeneracion);
	}

	public Historial_ingreso_del_negocio(int ingresoId, String motivoIngreso, LocalDateTime fechaGeneracion)
	{
		setIngresoId(ingresoId);
		setMotivoIngreso(motivoIngreso);
		setFechaGeneracion(fechaGeneracion);
	}

	public int getIdHistorialIngreso()
	{
		return idHistorialIngreso != null ? idHistorialIngreso : 0;
	}

	public void setIdHistorialIngreso(int idHistorialIngreso)
	{
		this.idHistorialIngreso = idHistorialIngreso;
	}

	public int getIdHistorialEstadistica()
	{
		return getIdHistorialIngreso();
	}

	public void setIngresoId(int ingresoId) throws IllegalArgumentException
	{
		if (ingresoId == 0)
		{
			throw new IllegalArgumentException("El ID del ingreso del negocio no puede ser 0.");
		} else if (ingresoId <= -1)
		{
			throw new IllegalArgumentException("El ID del ingreso del negocio no puede ser negativo.");
		}
		this.ingresoId = ingresoId;
	}

	public int getIngresoId()
	{
		return ingresoId != null ? ingresoId : 0;
	}

	public int getEstadisticaDelNegocioId()
	{
		return getIngresoId();
	}

	public Ingreso_del_negocio getIngreso()
	{
		return ingreso;
	}

	public void setIngreso(Ingreso_del_negocio ingreso)
	{
		this.ingreso = ingreso;
		if (ingreso != null)
		{
			this.ingresoId = ingreso.getIdIngreso();
		}
	}

	public void setMotivoIngreso(String motivoIngreso) throws IllegalArgumentException
	{
		if (motivoIngreso == null || motivoIngreso.isBlank() || motivoIngreso.isEmpty())
		{
			throw new IllegalArgumentException("El motivo del ingreso no puede ir vacío.");
		} else if (motivoIngreso.length() < 20)
		{
			throw new IllegalArgumentException("La longitud del motivo del ingreso es muy corta.");
		} else if (motivoIngreso.length() > 100)
		{
			throw new IllegalArgumentException("La longitud del motivo del ingreso es muy larga.");
		} else if (utilidades.tieneSoloTexto(motivoIngreso))
		{
			this.motivoIngreso = motivoIngreso;
		}
	}

	public String getMotivoIngreso()
	{
		return motivoIngreso;
	}

	public void setFechaGeneracion(LocalDateTime fechaGeneracion) throws IllegalArgumentException
	{
		if (fechaGeneracion == null)
		{
			throw new IllegalArgumentException("La fecha de generación no puede ir vacía.");
		}
		this.fechaGeneracion = fechaGeneracion;
	}

	public LocalDateTime getFechaGeneracion()
	{
		return fechaGeneracion;
	}
}