import java.time.LocalDateTime;

public class Historial_ingreso_del_negocio extends Object 
{
	private int Id_historial_ingreso = 0;
	private int Ingreso_id = 0;
	private String Motivo_estadistica = "";
	private LocalDateTime Fecha_generacion;

	// Constructor con ID incremental
	public Historial_ingreso_del_negocio(int idHistorialIngreso, int ingresoId, String motivo_estadistica, LocalDateTime fechaGeneracion)
	{
		this.Id_historial_ingreso = idHistorialIngreso;
		setIngresoId(ingresoId);
		setMotivoEstadistica(motivo_estadistica);
	}

	// Constructor sin ID incremental
	public Historial_ingreso_del_negocio(int ingresoId, String motivo_estadistica, LocalDateTime fechaGeneracion)
	{
		setIngresoId(ingresoId);
		setMotivoEstadistica(motivo_estadistica);
	}

	public int getIdHistorialEstadistica()
	{
		return Id_historial_ingreso;
	}

	public void setIngresoId(int ingresoId)
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

	public void setMotivoEstadistica(String motivoEstadistica)
	{
		if(motivoEstadistica.isBlank() || motivoEstadistica.isEmpty())
		{
			throw new IllegalArgumentException("El motivo de la estadística no puede ir vacío.");
		} else if(motivoEstadistica.length() < 20)
		{
			throw new IllegalArgumentException("El la longitud del motivo de la estadística es muy corto.");
		} else if(motivoEstadistica.length() > 100)
		{
			throw new IllegalArgumentException("El la longitud del motivo de la estadística es muy largo.");
		}
		this.Motivo_estadistica = motivoEstadistica;
	}

	public String getMotivoEstadistica()
	{
		return Motivo_estadistica;
	}

	public void setFechaGeneracion(LocalDateTime fechaGeneracion)
	{
		this.Fecha_generacion = fechaGeneracion;
	}

	public LocalDateTime getFechaGeneracion()
	{
		return Fecha_generacion;
	}
}
