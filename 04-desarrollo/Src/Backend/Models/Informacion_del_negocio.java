import java.util.Date;

public class Informacion_del_negocio extends Object 
{
	private int Id_informacion = 0;
	private int Usuario_id = 0;
	private int ValorInformacion = 0;
	private String Tipo_informacion = "";
	private String Informacion = "";
	private Date Fecha_creacion;
	private Date Fecha_actualizacion;

	// Constructor con id incremental
	public Informacion_del_negocio(int idInformacion, int usuarioId, int valorInformacion, String tipoInformacion, String informacion, Date fechaCreacion, Date fechaActualizacion)
	{
		this.Id_informacion = idInformacion;
		setUsuarioId(usuarioId);
		setValorInformacion(valorInformacion);
		setTipoInformacion(tipoInformacion);
		setInformacion(informacion);
		setFechaCreacion(fechaCreacion);
		setFechaActualizacion(fechaActualizacion);
	}

	// Constructor sin id incremental
	public Informacion_del_negocio(int usuarioId, int valorInformacion, String tipoInformacion, String informacion, Date fechaCreacion, Date fechaActualizacion)
	{
		setUsuarioId(usuarioId);
		setValorInformacion(valorInformacion);
		setTipoInformacion(tipoInformacion);
		setInformacion(informacion);
		setFechaCreacion(fechaCreacion);
		setFechaActualizacion(fechaActualizacion);
	}

	public int getIdInformacion()
	{
		return Id_informacion;
	}

	public void setUsuarioId(int usuarioId)
	{
		if(usuarioId == 0)
		{
			throw new IllegalArgumentException("El ID del usuario no puede ser 0.");
		} else if(usuarioId <= -1)
		{
			throw new IllegalArgumentException("El ID del usuario no puede ser negativo.");
		}
		this.Usuario_id = usuarioId;
	}

	public int getUsuarioId()
	{
		return Usuario_id;
	}

	public void setValorInformacion(int valorInformacion)
	{
		if(valorInformacion == 0)
		{
			throw new IllegalArgumentException("El valor de la información no puede ser 0.");
		} else if(valorInformacion <= -1)
		{
			throw new IllegalArgumentException("El valor de la información no puede ser negativo.");
		}
		this.ValorInformacion = valorInformacion;
	}

	public int getValorInformacion()
	{
		return ValorInformacion;
	}

	public void setTipoInformacion(String tipoInformacion)
	{
		if(tipoInformacion.isEmpty() || tipoInformacion.isBlank())
		{
			throw new IllegalArgumentException("El tipo de información no puede estar vacío.");
		} else if(tipoInformacion.length() < 10)
		{
			throw new IllegalArgumentException("La longitud del tipo de información es demasiado corta.");
		} else if(tipoInformacion.length() > 15)
		{
			throw new IllegalArgumentException("El tipo de información no puede superar una longitud de 15 caracteres.");
		}
		this.Tipo_informacion = tipoInformacion;
	}

	public String getTipoInformacion()
	{
		return Tipo_informacion;
	}

	public void setInformacion(String informacion)
	{
		if(informacion.isEmpty() || informacion.isBlank())
		{
			throw new IllegalArgumentException("La información no puede estar vacía.");
		} else if(informacion.length() < 10)
		{
			throw new IllegalArgumentException("La longitud de la información es demasiado corta.");
		} else if(informacion.length() > 255)
		{
			throw new IllegalArgumentException("La información no puede superar una longitud de 255 caracteres.");
		}
		this.Informacion = informacion;
	}

	public String getInformacion()
	{
		return Informacion;
	}

	public void setFechaCreacion(Date fechaCreacion)
	{
		this.Fecha_creacion = fechaCreacion;
	}

	public Date getFechaCreacion()
	{
		return Fecha_creacion;
	}

	public void setFechaActualizacion(Date fechaActualizacion)
	{
		this.Fecha_actualizacion = fechaActualizacion;
	}

	public Date getFechaActualizacion()
	{
		return Fecha_actualizacion;
	}
}