package Backend.Models;
import Backend.Common.Utils;
import java.time.LocalDateTime;

//Enum para el rol del usuario
enum RolUsuario 
{
	ADMINISTRADOR("Administrador"),
	OPERADOR_DE_CIBERCAFE("Operador de cibercafé");

	private final String rol;

	RolUsuario(String rol) 
	{
		this.rol = rol;
	}

	public String getRol() 
	{
		return rol;
	}
}

public class Usuario extends Object 
{

	private int Id_usuario = 0;
	private String Primer_nombre = "";
	private String Segundo_nombre = "";
	private String Primer_apellido = "";
	private String Segundo_apellido = "";
	private String Numero_documento = "";
	private String Tipo_documento = "";
	private String Nombre_usuario = "";
	private String Contrasenia = "";
	private RolUsuario Rol;
	private String Email = "";
	private String Telefono = "";
	private LocalDateTime Ultima_sesion;
	private Utils utilidades = new Utils();

	// Constructor con id incremental
	public Usuario(int idUsuario, String primerNombre, String segundoNombre, String primerApellido, String segundoApellido, String numeroDocumento, String tipoDocumento, String nombreUsuario, String contrasenia, RolUsuario rol, String email, String telefono, LocalDateTime ultimaSesion) 
	{
		this.Id_usuario = idUsuario;
		setPrimerNombre(primerNombre);
		setSegundoNombre(segundoNombre);
		setPrimerApellido(primerApellido);
		setSegundoApellido(segundoApellido);
		setNumeroDocumento(numeroDocumento);
		setTipoDocumento(tipoDocumento);
		setNombreUsuario(nombreUsuario);
		setContrasenia(contrasenia);
		setRol(rol);
		setEmail(email);
		setTelefono(telefono);
		setUltima_sesion(ultimaSesion);
	}


	// Constructor sin id incremental
	public Usuario(String primerNombre, String segundoNombre, String primerApellido, String segundoApellido, String numeroDocumento, String tipoDocumento, String nombreUsuario, String contrasenia, RolUsuario rol, String email, String telefono, LocalDateTime ultimaSesion) 
	{
		setPrimerNombre(primerNombre);
		setSegundoNombre(segundoNombre);
		setPrimerApellido(primerApellido);
		setSegundoApellido(segundoApellido);
		setNumeroDocumento(numeroDocumento);
		setTipoDocumento(tipoDocumento);
		setNombreUsuario(nombreUsuario);
		setContrasenia(contrasenia);
		setRol(rol);
		setEmail(email);
		setTelefono(telefono);
		setUltima_sesion(ultimaSesion);
	}

	public int getIdUsuario()
	{
		return Id_usuario;
	}

	public void setPrimerNombre(String primerNombre) throws IllegalArgumentException
	{
		if(primerNombre.isBlank() || primerNombre.isEmpty())
		{
			throw new IllegalArgumentException("El primer nombre no puede ingresarse vacío.");
		} else if(primerNombre.length() < 3)
		{
			throw new IllegalArgumentException("La longitud del primer nombre es demasiado corta.");
		} else if(primerNombre.length() > 30)
		{
			throw new IllegalArgumentException("La longitud del primer nombre es demasiado larga.");
		} else if(utilidades.tieneSoloTexto(primerNombre) == true)
		{
			this.Primer_nombre = primerNombre;
		}
	}

	public String getPrimerNombre()
	{
		return Primer_nombre;
	}

	public void setSegundoNombre(String segundoNombre) throws IllegalArgumentException
	{
		if(segundoNombre.length() < 3)
		{
			throw new IllegalArgumentException("La longitud del segundo nombre es demasiado corta.");
		} else if(segundoNombre.length() > 30)
		{
			throw new IllegalArgumentException("La longitud del segundo nombre es demasiado larga.");
		} else if(utilidades.tieneSoloTexto(segundoNombre) == true)
		{
			this.Segundo_nombre = segundoNombre;
		}
		
	}

	public String getSegundoNombre()
	{
		return Segundo_nombre;
	}

	public void setPrimerApellido(String primerApellido) throws IllegalArgumentException
	{
		if(primerApellido.isBlank() || primerApellido.isEmpty())
		{
			System.out.println("El primer apellido no puede ir vacío.");
		} else if(primerApellido.length() < 3)
		{
			throw new IllegalArgumentException("La longitud del primer apellido es demasiado corta.");
		} else if(primerApellido.length() > 30)
		{
			throw new IllegalArgumentException("La longitud del primer apellido es demasiado larga.");
		} else if(utilidades.tieneSoloTexto(primerApellido) == true)
		{
			this.Primer_apellido = primerApellido;
		}
	}

	public String getPrimerApellido()
	{
		return Primer_apellido;
	}

	public void setSegundoApellido(String segundoApellido) throws IllegalArgumentException
	{
		if(segundoApellido.length() < 3)
		{
			throw new IllegalArgumentException("La longitud del segundo apellido es demasiado corta.");
		} else if(segundoApellido.length() > 30)
		{
			throw new IllegalArgumentException("La longitud del segundo apellido es demasiado larga.");
		} else if(utilidades.tieneSoloTexto(segundoApellido) == true)
		{
			this.Segundo_apellido = segundoApellido;
		}
	}

	public String getSegundoApellido()
	{
		return Segundo_apellido;
	}

	public void setNumeroDocumento(String numeroDocumento) throws IllegalArgumentException
	{
		if(numeroDocumento.isBlank() || numeroDocumento.isEmpty())
		{
			throw new IllegalArgumentException("El número de documento no puede estar vacío.");
		} else if(numeroDocumento.length() < 15)
		{
			throw new IllegalArgumentException("El número de documento tiene una longitud muy corta.");
		} else if(numeroDocumento.length() > 20)
		{
			throw new IllegalArgumentException("El número de documento tiene una longitud muy larga.");
		} else if(utilidades.tieneSoloNumeros(numeroDocumento) == true)
		{
			this.Numero_documento = numeroDocumento;
		}
	}

	public String getNumeroDocumento()
	{
		return Numero_documento;
	}

	public void setTipoDocumento(String tipoDocumento) throws IllegalArgumentException
	{
		if(tipoDocumento.isBlank() || tipoDocumento.isEmpty())
		{
			System.out.println("El tipo de documento no puede estar vacío.");
		} else if(tipoDocumento.length() > 5)
		{
			System.out.println("El tipo de documento superó el máximo de carácteres.");
		} else if(utilidades.tieneSoloTexto(tipoDocumento) == true)
		{
			this.Tipo_documento = tipoDocumento;
		}
	}

	public String getTipoDocumento()
	{
		return Tipo_documento;
	}

	public void setNombreUsuario(String nombreUsuario) throws IllegalArgumentException
	{
		if(nombreUsuario.isBlank() || nombreUsuario.isEmpty())
		{
			System.out.println("El nombre de usuario no puede estar vacío.");
		} else if(nombreUsuario.length() < 8)
		{
			throw new IllegalArgumentException("La longitud del nombre de usuario es demasiado corto.");
		} else if(nombreUsuario.length() > 20)
		{
			throw new IllegalArgumentException("La longitud del nombre de usuario es demasiado largo.");
		}
		this.Nombre_usuario = nombreUsuario;
	}

	public String getNombreUsuario()
	{
		return Nombre_usuario;
	}

	public void setContrasenia(String contrasenia) throws IllegalArgumentException
	{
		if(contrasenia.isBlank() || contrasenia.isEmpty())
		{
			System.out.println("La contraseña no puede estar vacía.");
		} else if(Contrasenia.length() < 10)
		{
			throw new IllegalArgumentException("La longitud de la contraseña es demasiado corta.");
		} else if(contrasenia.length() > 255)
		{
			throw new IllegalArgumentException("La longitud de la contraseña es demasiado larga.");
		}
		Contrasenia = contrasenia;
	}

	public String getContrasenia() {
		return Contrasenia;
	}

	public void setRol(RolUsuario rol) {
		Rol = rol;
	}

	public RolUsuario getRol() {
		return Rol;
	}

	public void setEmail(String email) throws IllegalArgumentException
	{
		if(email.isBlank() || email.isEmpty())
		{
			System.out.println("El email no puede estar vacío.");
		} else if(email.length() < 20)
		{
			throw new IllegalArgumentException("La longitud del email es demasiado corta.");
		} else if(email.length() > 60)
		{
			throw new IllegalArgumentException("La longitud del email es demasiado larga.");
		} else if(utilidades.esEmailValido(email) == true)
		{
			this.Email = email;
		}
	}

	public String getEmail() {
		return Email;
	}

	public void setTelefono(String telefono) throws IllegalArgumentException
	{
		if(telefono.isBlank() || telefono.isEmpty())
		{
			System.out.println("El email no puede estar vacío.");
		} else if(telefono.length() < 8)
		{
			throw new IllegalArgumentException("La longitud del teléfono es demasiado corta.");
		} else if(telefono.length() > 15)
		{
			throw new IllegalArgumentException("La longitud del teléfono  es demasiado larga.");
		} else if(utilidades.tieneSoloNumeros(telefono) == true)
		{
			this.Telefono = telefono;
		}
	}

	public String getTelefono() {
		return Telefono;
	}

	public void setUltima_sesion(LocalDateTime ultimaSesion) {
		this.Ultima_sesion = ultimaSesion;
	}

	public LocalDateTime getUltima_sesion() {
		return Ultima_sesion;
	}	
}