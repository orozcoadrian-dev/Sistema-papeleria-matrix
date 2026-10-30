package com.papeleriamatrix.model;
import com.papeleriamatrix.common.Utils;
import java.time.LocalDateTime;

public class Historial_ingreso_del_negocio extends Object 
{
	private int Id_historial_ingreso = 0;
	private int Ingreso_id = 0;
	private String Motivo_ingreso = "";
	private LocalDateTime Fecha_generacion;
	private Utils utilidades = new Utils();

	// Constructor con ID incremental
	public Historial_ingreso_del_negocio(int idHistorialIngreso, int ingresoId, String motivoIngreso, LocalDateTime fechaGeneracion)
	{
		this.Id_historial_ingreso = idHistorialIngreso;
		setIngresoId(ingresoId);
		setMotivoIngreso(motivoIngreso);
	}

	// Constructor sin ID incremental
	public Historial_ingreso_del_negocio(int ingresoId, String motivoIngreso, LocalDateTime fechaGeneracion)
	{
		setIngresoId(ingresoId);
		setMotivoIngreso(motivoIngreso);
	}

	public int getIdHistorialEstadistica()
	{
		return Id_historial_ingreso;
	}

	public void setIngresoId(int ingresoId) throws IllegalArgumentException
	{
		if(ingresoId == 0)
		{
			throw new IllegalArgumentException("El ID del ingreso del negocio no puede ser 0.");
		} else if(ingresoId <= -1)
		{
			throw new IllegalArgumentException("El ID del ingreso del negocio no puede ser negativa.");
		}
		this.Ingreso_id = ingresoId;
	}

	public int getEstadisticaDelNegocioId()
	{
		return Ingreso_id;
	}

	public void setMotivoIngreso(String motivoIngreso) throws IllegalArgumentException
	{
		if(motivoIngreso.isBlank() || motivoIngreso.isEmpty())
		{
			throw new IllegalArgumentException("El motivo del ingreso no puede ir vacío.");
		} else if(motivoIngreso.length() < 20)
		{
			throw new IllegalArgumentException("El la longitud del motivo del ingreso es muy corto.");
		} else if(motivoIngreso.length() > 100)
		{
			throw new IllegalArgumentException("El la longitud del motivo del ingreso es muy largo.");
		} else if(utilidades.tieneSoloTexto(motivoIngreso) == true)
		{
			this.Motivo_ingreso = motivoIngreso;
		}
	}

	public String getMotivoIngreso()
	{
		return Motivo_ingreso;
	}

	public void setFechaGeneracion(LocalDateTime fechaGeneracion) throws IllegalArgumentException
	{
		if(fechaGeneracion == null)
		{
			throw new IllegalArgumentException("La fecha de generación no puede ir vacía.");
		}
		this.Fecha_generacion = fechaGeneracion;
	}

	public LocalDateTime getFechaGeneracion()
	{
		return Fecha_generacion;
	}
}