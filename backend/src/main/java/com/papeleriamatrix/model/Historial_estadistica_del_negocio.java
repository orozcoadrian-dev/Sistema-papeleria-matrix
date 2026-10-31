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
@Table(name = "historial_estadistica_del_negocio")
public class Historial_estadistica_del_negocio 
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Id_historial_estadistica")
	private Integer idHistorialEstadistica;

	@Column(name = "Estadistica_del_negocio_id", nullable = false)
	private Integer estadisticaDelNegocioId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Estadistica_del_negocio_id", insertable = false, updatable = false)
	private Estadistica_del_negocio estadistica;

	@Column(name = "Motivo_estadistica", nullable = false, length = 100)
	private String motivoEstadistica = "";

	@Column(name = "Fecha_generacion", nullable = false)
	private LocalDateTime fechaGeneracion;

	@Transient
	private Utils utilidades = new Utils();

	public Historial_estadistica_del_negocio()
	{
	}

	public Historial_estadistica_del_negocio(int idHistorialEstadistica, int estadisticaDelNegocioId, String motivoEstadistica, LocalDateTime fechaGeneracion)
	{
		this.idHistorialEstadistica = idHistorialEstadistica;
		setEstadisticaDelNegocioId(estadisticaDelNegocioId);
		setMotivoEstadistica(motivoEstadistica);
		setFechaGeneracion(fechaGeneracion);
	}

	public Historial_estadistica_del_negocio(int estadisticaDelNegocioId, String motivoEstadistica, LocalDateTime fechaGeneracion)
	{
		setEstadisticaDelNegocioId(estadisticaDelNegocioId);
		setMotivoEstadistica(motivoEstadistica);
		setFechaGeneracion(fechaGeneracion);
	}

	public int getIdHistorialEstadistica()
	{
		return idHistorialEstadistica != null ? idHistorialEstadistica : 0;
	}

	public void setIdHistorialEstadistica(int idHistorialEstadistica)
	{
		this.idHistorialEstadistica = idHistorialEstadistica;
	}

	public void setEstadisticaDelNegocioId(int estadisticaDelNegocioId) throws IllegalArgumentException
	{
		if (estadisticaDelNegocioId == 0)
		{
			throw new IllegalArgumentException("El ID de la estadística del negocio no puede ser 0.");
		} else if (estadisticaDelNegocioId <= -1)
		{
			throw new IllegalArgumentException("El ID de la estadística del negocio no puede ser negativa.");
		}
		this.estadisticaDelNegocioId = estadisticaDelNegocioId;
	}

	public int getEstadisticaDelNegocioId()
	{
		return estadisticaDelNegocioId != null ? estadisticaDelNegocioId : 0;
	}

	public Estadistica_del_negocio getEstadistica()
	{
		return estadistica;
	}

	public void setEstadistica(Estadistica_del_negocio estadistica)
	{
		this.estadistica = estadistica;
		if (estadistica != null)
		{
			this.estadisticaDelNegocioId = estadistica.getIdEstadistica();
		}
	}

	public void setMotivoEstadistica(String motivoEstadistica) throws IllegalArgumentException
	{
		if (motivoEstadistica == null || motivoEstadistica.isBlank() || motivoEstadistica.isEmpty())
		{
			throw new IllegalArgumentException("El motivo de la estadística no puede ir vacío.");
		} else if (motivoEstadistica.length() < 20)
		{
			throw new IllegalArgumentException("La longitud del motivo de la estadística es muy corta.");
		} else if (motivoEstadistica.length() > 100)
		{
			throw new IllegalArgumentException("La longitud del motivo de la estadística es muy larga.");
		} else if (utilidades.tieneSoloTexto(motivoEstadistica))
		{
			this.motivoEstadistica = motivoEstadistica;
		}
	}

	public String getMotivoEstadistica()
	{
		return motivoEstadistica;
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