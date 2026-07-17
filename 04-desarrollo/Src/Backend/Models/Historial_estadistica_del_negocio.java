package Backend.Models;
import Backend.Common.Utils;
import java.time.LocalDateTime;

public class Historial_estadistica_del_negocio extends Object 
{
	private int Id_historial_estadistica = 0;
	private int Estadistica_del_negocio_id = 0;
	private String Motivo_estadistica = "";
	private LocalDateTime Fecha_generacion;
	private Utils utilidades = new Utils();

	// Constructor con ID incremental
	public Historial_estadistica_del_negocio(int idHistorialEstadista, int estadisticaDelNegocioId, String motivo_estadistica, LocalDateTime fechaGeneracion)
	{
		this.Id_historial_estadistica = idHistorialEstadista;
		setEstadisticaDelNegocioId(estadisticaDelNegocioId);
		setMotivoEstadistica(motivo_estadistica);
	}

	// Constructor sin ID incremental
	public Historial_estadistica_del_negocio(int estadisticaDelNegocioId, String motivo_estadistica, LocalDateTime fechaGeneracion)
	{
		setEstadisticaDelNegocioId(estadisticaDelNegocioId);
		setMotivoEstadistica(motivo_estadistica);
	}

	public int getIdHistorialEstadistica()
	{
		return Id_historial_estadistica;
	}

	public void setEstadisticaDelNegocioId(int estadisticaDelNegocioId) throws IllegalArgumentException
	{
		if(estadisticaDelNegocioId == 0)
		{
			throw new IllegalArgumentException("El ID de la estadística del negocio no puede ser 0.");
		} else if(estadisticaDelNegocioId <= -1)
		{
			throw new IllegalArgumentException("El ID de la estadística del negocio no puede ser negativa.");
		}
		this.Estadistica_del_negocio_id = estadisticaDelNegocioId;
	}

	public int getEstadisticaDelNegocioId()
	{
		return Estadistica_del_negocio_id;
	}

	public void setMotivoEstadistica(String motivoEstadistica) throws IllegalArgumentException
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
		} else if(utilidades.tieneSoloTexto(motivoEstadistica) == true)
		{
			this.Motivo_estadistica = motivoEstadistica;
		}
	}

	public String getMotivoEstadistica()
	{
		return Motivo_estadistica;
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