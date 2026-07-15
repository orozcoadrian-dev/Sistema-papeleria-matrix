import java.time.LocalDateTime;
import java.util.Date;

public class Administrador extends Usuario 
{
	
		private Date Fecha_posicionamiento;
		private String Area_acceso = "";
		private Utils utilidades = new Utils();
	
	// Constructor con id incremental
	public Administrador(int idUsuario, String primerNombre, String segundoNombre, String primerApellido, String segundoApellido, String numeroDocumento, String tipoDocumento, String nombreUsuario, String contrasenia, RolUsuario rol, String email, String telefono, LocalDateTime ultimaSesion, Date fechaPosicionamiento, String areaAcceso)
	{
		super(idUsuario, primerNombre, segundoNombre, primerApellido, segundoApellido, numeroDocumento, tipoDocumento, nombreUsuario, contrasenia, rol, email, telefono, ultimaSesion);
		setFechaPosicionamiento(fechaPosicionamiento);
		setAreaAcceso(areaAcceso);
	}

	// Constructor sin id incremental
	public Administrador(String primerNombre, String segundoNombre, String primerApellido, String segundoApellido, String numeroDocumento, String tipoDocumento, String nombreUsuario, String contrasenia, RolUsuario rol, String email, String telefono, LocalDateTime ultimaSesion, Date fechaPosicionamiento, String areaAcceso)
	{
		super(primerNombre, segundoNombre, primerApellido, segundoApellido, numeroDocumento, tipoDocumento, nombreUsuario, contrasenia, rol, email, telefono, ultimaSesion);
		setFechaPosicionamiento(fechaPosicionamiento);
		setAreaAcceso(areaAcceso);
	}

	public void setFechaPosicionamiento(Date fechaPosicionamiento)
	{
		if(fechaPosicionamiento == null)
		{
			throw new IllegalArgumentException("La fecha no puede quedar vacía.");
		}
		this.Fecha_posicionamiento = fechaPosicionamiento;
	}

	public Date getFechaPosicionamiento()
	{
		return Fecha_posicionamiento;
	}

	public void setAreaAcceso(String areaAcceso)
	{
		if(areaAcceso.isBlank() || areaAcceso.isEmpty())
		{
			throw new IllegalArgumentException("El área de acceso no puede ir vacía.");
		} else if(areaAcceso.length() < 3)
		{
			throw new IllegalArgumentException("La longitud del área de acceso es demasiado corta.");
		} else if(areaAcceso.length() > 15)
		{
			throw new IllegalArgumentException("La longitud del área de acceso es demasiado larga.");
		} else if(utilidades.tieneSoloTexto(areaAcceso) == true)
		{
			this.Area_acceso = areaAcceso;
		}
	}

	public String getAreaAcceso()
	{
		return Area_acceso;
	}
}