import java.util.Date;

public class Clave_de_permiso extends Object 
{
	
	private int Id_clave = 0;
	private int Usuario_id = 0;
	private String Clave = "";
	private Date Fecha_creacion;
	private Date Fecha_actualizacion;
	private Utils utilidades = new Utils();

	//Constructor con id autoincremental
	public Clave_de_permiso(int idClave, int usuarioId, String clave, Date fechaCreacion, Date fechaActualizacion)
	{
		this.Id_clave = idClave;
		setUsuarioId(usuarioId);
		setClave(clave);
		setFechaCreacion(fechaCreacion);
		setFechaActualizacion(fechaActualizacion);
	}

	//Constructor sin id autoincremental
	public Clave_de_permiso(int usuarioId, String clave, Date fechaCreacion, Date fechaActualizacion)
	{
		setUsuarioId(usuarioId);
		setClave(clave);
		setFechaCreacion(fechaCreacion);
		setFechaActualizacion(fechaActualizacion);
	}

	public int getIdClave()
	{
		return Id_clave;
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

	public void setClave(String clave)
	{
		if(clave.isBlank() || clave.isEmpty())
		{
			throw new IllegalArgumentException("La clave no puede ir vacía.");
		} else if(clave.length() < 10)
		{
			throw new IllegalArgumentException("La longitud de la clave es demasiado corta.");
		} else if(clave.length() > 30)
		{
			throw new IllegalArgumentException("La longitud de la clave es demasiado larga.");
		} 
			this.Clave = clave;
	}

	public String getClave()
	{
		return Clave;
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
		return  Fecha_actualizacion;
	}
}