import java.time.LocalDateTime;
import java.util.Date;

public class Operador_de_cibercafe extends Usuario
{
	
	private double Salario = 0;
	private String Horario = "";
	private String Direccion = "";
	private Date Fecha_contrato;

	// Constructor con id incremental
	public Operador_de_cibercafe(int idUsuario, String primerNombre, String segundoNombre, String primerApellido, String segundoApellido, String numeroDocumento, String tipoDocumento, String nombreUsuario, String contrasenia, RolUsuario rol, String email, String telefono, LocalDateTime ultimaSesion, double salario, String horario, String direccion, Date fechaContrato)
	{
		super(idUsuario, primerNombre, segundoNombre, primerApellido, segundoApellido, numeroDocumento, tipoDocumento, nombreUsuario, contrasenia, rol, email, telefono, ultimaSesion);
		setSalario(salario);
		setHorario(horario);
		setDireccion(direccion);
		setFechaContrato(fechaContrato);
	}

	// Constructor sin id incremental
	public Operador_de_cibercafe(String primerNombre, String segundoNombre, String primerApellido, String segundoApellido, String numeroDocumento, String tipoDocumento, String nombreUsuario, String contrasenia, RolUsuario rol, String email, String telefono, LocalDateTime ultimaSesion, double salario, String horario, String direccion, Date fechaContrato)
	{
		super(primerNombre, segundoNombre, primerApellido, segundoApellido, numeroDocumento, tipoDocumento, nombreUsuario, contrasenia, rol, email, telefono, ultimaSesion);
		setSalario(salario);
		setHorario(horario);
		setDireccion(direccion);
		setFechaContrato(fechaContrato);
	}

	public void setSalario(double salario)
	{
		if(salario == 0)
		{
			throw new IllegalArgumentException("El salario no puede ser igual a 0.");
		} else if (salario <= -1)
		{
			throw new IllegalArgumentException("El salario no puede ser negativo.");
		}
		this.Salario = salario; 
	}

	public double getSalario()
	{
		return Salario;
	}

	public void setHorario(String horario)
	{
		if(horario.isBlank() || horario.isEmpty())
		{
			throw new IllegalArgumentException("El horario no puede ir vacío.");
		} else if(horario.length() < 10)
		{
			throw new IllegalArgumentException("La longitud del horario es demasiado corta.");
		} else if(horario.length() > 80)
		{
			throw new IllegalArgumentException("La longitud del horario es demasiado larga.");
		}
		this.Horario= horario; 
	}

	public String getHorario()
	{
		return Horario;
	}

	public void setDireccion(String direccion)
	{
		if(direccion.isBlank() || direccion.isEmpty())
		{
			throw new IllegalArgumentException("La dirección no puede ir vacía.");
		} else if(direccion.length() < 10)
		{
			throw new IllegalArgumentException("La longitud de la dirección es demasiado corta.");
		} else if(direccion.length() > 80)
		{
			throw new IllegalArgumentException("La longitud de la dirección es demasiado larga.");
		}
		this.Direccion= direccion; 
	}

	public String getDireccion()
	{
		return Direccion;
	}

	public void setFechaContrato(Date fechaContrato)
	{
		this.Fecha_contrato = fechaContrato; 
	}

	public Date getFechaContrato()
	{
		return Fecha_contrato;
	}
}