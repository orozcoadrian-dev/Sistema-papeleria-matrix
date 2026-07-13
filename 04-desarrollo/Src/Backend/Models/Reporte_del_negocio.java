import java.util.Date;

public class Reporte_del_negocio extends Object
{
	private int Id_reporte = 0;
	private String Codigo_reporte = "";
	private int Historial_estadistica_del_negocio_id = 0;
	private int Historial_ingreso_del_negocio_id = 0;
	private int Usuario_id = 0;
	private String Ruta_archivo = "";
	private Date Fecha_generacion;

	// Constructor con id autoincremental
	public Reporte_del_negocio(int idReporte, String codigoReporte, int historialEstadisticaDelNegocioId, int historialIngresoDelNegocioId, int usuarioId, String rutaArchivo, Date fechaGeneracion)
	{
		this.Id_reporte = idReporte;
		setCodigoReporte(codigoReporte);
		setHistorialEstadisticaDelNegocioId(historialEstadisticaDelNegocioId);
		setHistorialIngresoDelNegocioId(historialIngresoDelNegocioId);
		setUsuarioId(usuarioId);
		setRutaArchivo(rutaArchivo);
		setFechaGeneracion(fechaGeneracion);
	}

	// Constructor sin id incremental
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
		return Id_reporte;
	}

	public void setCodigoReporte(String codigoReporte)
	{
		if (codigoReporte.isEmpty() || codigoReporte.isBlank())
		{
			throw new IllegalArgumentException("El código de reporte no puede estar vacío.");
		} else if (codigoReporte.length() < 20)
		{
			throw new IllegalArgumentException("La longitud del código de reporte es demasiado corta.");
		} else if (codigoReporte.length() > 20)
		{
			throw new IllegalArgumentException("El código de reporte no puede superar una longitud de 20 caracteres.");
		}
		this.Codigo_reporte = codigoReporte;
	}

	public String getCodigoReporte()
	{
		return Codigo_reporte;
	}

	public void setHistorialEstadisticaDelNegocioId(int historialEstadisticaDelNegocioId)
	{
		if (historialEstadisticaDelNegocioId == 0)
		{
			throw new IllegalArgumentException("El ID del historial de estadística del negocio no puede ser 0.");
		} else if (historialEstadisticaDelNegocioId <= -1)
		{
			throw new IllegalArgumentException("El ID del historial de estadística del negocio no puede ser negativo.");
		}
		this.Historial_estadistica_del_negocio_id = historialEstadisticaDelNegocioId;
	}

	public int getHistorialEstadisticaDelNegocioId()
	{
		return Historial_estadistica_del_negocio_id;
	}

	public void setHistorialIngresoDelNegocioId(int historialIngresoDelNegocioId)
	{
		if (historialIngresoDelNegocioId == 0)
		{
			throw new IllegalArgumentException("El ID del historial de ingreso del negocio no puede ser 0.");
		} else if (historialIngresoDelNegocioId <= -1)
		{
			throw new IllegalArgumentException("El ID del historial de ingreso del negocio no puede ser negativo.");
		}
		this.Historial_ingreso_del_negocio_id = historialIngresoDelNegocioId;
	}

	public int getHistorialIngresoDelNegocioId()
	{
		return Historial_ingreso_del_negocio_id;
	}

	public void setUsuarioId(int usuarioId)
	{
		if (usuarioId == 0)
		{
			throw new IllegalArgumentException("El ID del usuario no puede ser 0.");
		} else if (usuarioId <= -1)
		{
			throw new IllegalArgumentException("El ID del usuario no puede ser negativo.");
		}
		this.Usuario_id = usuarioId;
	}

	public int getUsuarioId()
	{
		return Usuario_id;
	}

	public void setRutaArchivo(String rutaArchivo)
	{
		if (rutaArchivo.isEmpty() || rutaArchivo.isBlank())
		{
			throw new IllegalArgumentException("La ruta del archivo no puede estar vacía.");
		} else if (rutaArchivo.length() < 20)
		{
			throw new IllegalArgumentException("La longitud de la ruta del archivo es demasiado corta.");
		} else if (rutaArchivo.length() > 255)
		{
			throw new IllegalArgumentException("La ruta del archivo no puede superar una longitud de 255 caracteres.");
		}
		this.Ruta_archivo = rutaArchivo;
	}

	public String getRutaArchivo()
	{
		return Ruta_archivo;
	}

	public void setFechaGeneracion(Date fechaGeneracion)
	{
		this.Fecha_generacion = fechaGeneracion;
	}

	public Date getFechaGeneracion()
	{
		return Fecha_generacion;
	}
}