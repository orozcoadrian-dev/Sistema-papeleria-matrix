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
@Table(name = "reporte_del_negocio")
public class Reporte_del_negocio 
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Id_reporte")
	private Integer idReporte;

	@Column(name = "Codigo_reporte", nullable = false, unique = true, length = 20)
	private String codigoReporte = "";

	@Column(name = "Historial_estadistica_del_negocio_id")
	private Integer historialEstadisticaDelNegocioId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Historial_estadistica_del_negocio_id", insertable = false, updatable = false)
	private Historial_estadistica_del_negocio historialEstadistica;

	@Column(name = "Historial_ingreso_del_negocio_id")
	private Integer historialIngresoDelNegocioId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Historial_ingreso_del_negocio_id", insertable = false, updatable = false)
	private Historial_ingreso_del_negocio historialIngreso;

	@Column(name = "Usuario_id")
	private Integer usuarioId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Usuario_id", insertable = false, updatable = false)
	private Administrador administrador;

	@Column(name = "Ruta_archivo", nullable = false, length = 255)
	private String rutaArchivo = "";

	@Column(name = "Fecha_generacion", nullable = false)
	private LocalDate fechaGeneracion;

	@Transient
	private Utils utilidades = new Utils();

	public Reporte_del_negocio()
	{
	}

	public Reporte_del_negocio(int idReporte, String codigoReporte, int historialEstadisticaDelNegocioId, int historialIngresoDelNegocioId, int usuarioId, String rutaArchivo, LocalDate fechaGeneracion)
	{
		this.idReporte = idReporte;
		setCodigoReporte(codigoReporte);
		setHistorialEstadisticaDelNegocioId(historialEstadisticaDelNegocioId);
		setHistorialIngresoDelNegocioId(historialIngresoDelNegocioId);
		setUsuarioId(usuarioId);
		setRutaArchivo(rutaArchivo);
		setFechaGeneracion(fechaGeneracion);
	}

	public Reporte_del_negocio(String codigoReporte, int historialEstadisticaDelNegocioId, int historialIngresoDelNegocioId, int usuarioId, String rutaArchivo, LocalDate fechaGeneracion)
	{
		setCodigoReporte(codigoReporte);
		setHistorialEstadisticaDelNegocioId(historialEstadisticaDelNegocioId);
		setHistorialIngresoDelNegocioId(historialIngresoDelNegocioId);
		setUsuarioId(usuarioId);
		setRutaArchivo(rutaArchivo);
		setFechaGeneracion(fechaGeneracion);
	}

	public Reporte_del_negocio(int idReporte, String codigoReporte, int historialEstadisticaDelNegocioId, int historialIngresoDelNegocioId, int usuarioId, String rutaArchivo, Date fechaGeneracion)
	{
		this.idReporte = idReporte;
		setCodigoReporte(codigoReporte);
		setHistorialEstadisticaDelNegocioId(historialEstadisticaDelNegocioId);
		setHistorialIngresoDelNegocioId(historialIngresoDelNegocioId);
		setUsuarioId(usuarioId);
		setRutaArchivo(rutaArchivo);
		setFechaGeneracion(fechaGeneracion);
	}

	public Reporte_del_negocio(String codigoReporte, int historialEstadisticaDelNegocioId, int historialIngresoDelNegocioId, int usuarioId, String rutaArchivo, Date fechaGeneracion)
	{
		setCodigoReporte(codigoReporte);
		setHistorialEstadisticaDelNegocioId(historialEstadisticaDelNegocioId);
		setHistorialIngresoDelNegocioId(historialIngresoDelNegocioId);
		setUsuarioId(usuarioId);
		setRutaArchivo(rutaArchivo);
		setFechaGeneracion(fechaGeneracion);
	}

	public int getIdReporte()
	{
		return idReporte != null ? idReporte : 0;
	}

	public void setIdReporte(int idReporte)
	{
		this.idReporte = idReporte;
	}

	public void setCodigoReporte(String codigoReporte) throws IllegalArgumentException
	{
		if (codigoReporte == null || codigoReporte.isEmpty() || codigoReporte.isBlank())
		{
			throw new IllegalArgumentException("El código de reporte no puede estar vacío.");
		} else if (codigoReporte.length() != 20)
		{
			throw new IllegalArgumentException("El código de reporte debe tener una longitud de 20 caracteres.");
		} else if (utilidades.tieneSoloNumeros(codigoReporte))
		{
			this.codigoReporte = codigoReporte;
		}
	}

	public String getCodigoReporte()
	{
		return codigoReporte;
	}

	public void setHistorialEstadisticaDelNegocioId(int historialEstadisticaDelNegocioId) throws IllegalArgumentException
	{
		if (historialEstadisticaDelNegocioId <= -1)
		{
			throw new IllegalArgumentException("El ID del historial de estadística del negocio no puede ser negativo.");
		}
		this.historialEstadisticaDelNegocioId = historialEstadisticaDelNegocioId != 0 ? historialEstadisticaDelNegocioId : null;
	}

	public int getHistorialEstadisticaDelNegocioId()
	{
		return historialEstadisticaDelNegocioId != null ? historialEstadisticaDelNegocioId : 0;
	}

	public Historial_estadistica_del_negocio getHistorialEstadistica()
	{
		return historialEstadistica;
	}

	public void setHistorialEstadistica(Historial_estadistica_del_negocio historialEstadistica)
	{
		this.historialEstadistica = historialEstadistica;
		if (historialEstadistica != null)
		{
			this.historialEstadisticaDelNegocioId = historialEstadistica.getIdHistorialEstadistica();
		}
	}

	public void setHistorialIngresoDelNegocioId(int historialIngresoDelNegocioId) throws IllegalArgumentException
	{
		if (historialIngresoDelNegocioId <= -1)
		{
			throw new IllegalArgumentException("El ID del historial de ingreso del negocio no puede ser negativo.");
		}
		this.historialIngresoDelNegocioId = historialIngresoDelNegocioId != 0 ? historialIngresoDelNegocioId : null;
	}

	public int getHistorialIngresoDelNegocioId()
	{
		return historialIngresoDelNegocioId != null ? historialIngresoDelNegocioId : 0;
	}

	public Historial_ingreso_del_negocio getHistorialIngreso()
	{
		return historialIngreso;
	}

	public void setHistorialIngreso(Historial_ingreso_del_negocio historialIngreso)
	{
		this.historialIngreso = historialIngreso;
		if (historialIngreso != null)
		{
			this.historialIngresoDelNegocioId = historialIngreso.getIdHistorialIngreso();
		}
	}

	public void setUsuarioId(int usuarioId) throws IllegalArgumentException
	{
		if (usuarioId <= -1)
		{
			throw new IllegalArgumentException("El ID del usuario no puede ser negativo.");
		}
		this.usuarioId = usuarioId != 0 ? usuarioId : null;
	}

	public int getUsuarioId()
	{
		return usuarioId != null ? usuarioId : 0;
	}

	public Administrador getAdministrador()
	{
		return administrador;
	}

	public void setAdministrador(Administrador administrador)
	{
		this.administrador = administrador;
		if (administrador != null)
		{
			this.usuarioId = administrador.getIdUsuario();
		}
	}

	public void setRutaArchivo(String rutaArchivo) throws IllegalArgumentException
	{
		if (rutaArchivo == null || rutaArchivo.isEmpty() || rutaArchivo.isBlank())
		{
			throw new IllegalArgumentException("La ruta del archivo no puede estar vacía.");
		} else if (rutaArchivo.length() > 255)
		{
			throw new IllegalArgumentException("La ruta del archivo no puede superar una longitud de 255 caracteres.");
		}
		this.rutaArchivo = rutaArchivo;
	}

	public String getRutaArchivo()
	{
		return rutaArchivo;
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
}