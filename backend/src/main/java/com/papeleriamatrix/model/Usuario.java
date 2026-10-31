package com.papeleriamatrix.model;

import com.papeleriamatrix.common.Utils;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Converter;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;

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

	public static RolUsuario fromString(String valor)
	{
		if (valor == null) return null;
		for (RolUsuario r : values())
		{
			if (r.rol.equalsIgnoreCase(valor) || r.name().equalsIgnoreCase(valor))
			{
				return r;
			}
		}
		throw new IllegalArgumentException("Rol desconocido: " + valor);
	}
}

@Converter(autoApply = true)
class RolUsuarioConverter implements AttributeConverter<RolUsuario, String>
{
	@Override
	public String convertToDatabaseColumn(RolUsuario attribute)
	{
		return attribute != null ? attribute.getRol() : null;
	}

	@Override
	public RolUsuario convertToEntityAttribute(String dbData)
	{
		return dbData != null ? RolUsuario.fromString(dbData) : null;
	}
}

@Entity 
@Table(name = "usuario")
@Inheritance(strategy = InheritanceType.JOINED)
public class Usuario 
{
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Id_usuario", nullable = false, unique = true)
	private int idUsuario = 0;

	@Column(name = "Primer_nombre", nullable = false, length = 30)
	private String primerNombre = "";

	@Column(name = "Segundo_nombre", length = 30)
	private String segundoNombre = "";

	@Column(name = "Primer_apellido", nullable = false, length = 30)
	private String primerApellido = "";

	@Column(name = "Segundo_apellido", nullable = false, length = 30)
	private String segundoApellido = "";

	@Column(name = "Numero_documento", nullable = false, unique = true, length = 20)
	private String numeroDocumento = "";

	@Column(name = "Tipo_documento", nullable = false, length = 5)
	private String tipoDocumento = "";

	@Column(name = "Nombre_usuario", nullable = false, unique = true, length = 15)
	private String nombreUsuario = "";

	@Column(name = "Contrasenia", nullable = false, length = 255)
	private String contrasenia = "";

	@Convert(converter = RolUsuarioConverter.class)
	@Column(name = "Rol", nullable = false)
	private RolUsuario rol;

	@Column(name = "Email", unique = true, length = 60)
	private String email = "";

	@Column(name = "Telefono", unique = true, length = 15)
	private String telefono = "";

	@Column(name = "Ultima_sesion", nullable = false)
	private LocalDateTime ultimaSesion;

	@Transient
	private Utils utilidades = new Utils();

	public Usuario() 
	{
	}

	public Usuario(int idUsuario, String primerNombre, String segundoNombre, String primerApellido, String segundoApellido, String numeroDocumento, String tipoDocumento, String nombreUsuario, String contrasenia, RolUsuario rol, String email, String telefono, LocalDateTime ultimaSesion) 
	{
		this.idUsuario = idUsuario;
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
		return idUsuario;
	}

	public void setIdUsuario(int idUsuario)
	{
		this.idUsuario = idUsuario;
	}

	public void setPrimerNombre(String primerNombre) throws IllegalArgumentException
	{
		if (primerNombre == null || primerNombre.isBlank() || primerNombre.isEmpty())
		{
			throw new IllegalArgumentException("El primer nombre no puede ingresarse vacío.");
		} else if (primerNombre.length() < 3)
		{
			throw new IllegalArgumentException("La longitud del primer nombre es demasiado corta.");
		} else if (primerNombre.length() > 30)
		{
			throw new IllegalArgumentException("La longitud del primer nombre es demasiado larga.");
		} else if (utilidades.tieneSoloTexto(primerNombre))
		{
			this.primerNombre = primerNombre;
		}
	}

	public String getPrimerNombre()
	{
		return primerNombre;
	}

	public void setSegundoNombre(String segundoNombre) throws IllegalArgumentException
	{
		if (segundoNombre == null || segundoNombre.isEmpty())
		{
			this.segundoNombre = segundoNombre;
			return;
		}
		if (segundoNombre.length() < 3)
		{
			throw new IllegalArgumentException("La longitud del segundo nombre es demasiado corta.");
		} else if (segundoNombre.length() > 30)
		{
			throw new IllegalArgumentException("La longitud del segundo nombre es demasiado larga.");
		} else if (utilidades.tieneSoloTexto(segundoNombre))
		{
			this.segundoNombre = segundoNombre;
		}
	}

	public String getSegundoNombre()
	{
		return segundoNombre;
	}

	public void setPrimerApellido(String primerApellido) throws IllegalArgumentException
	{
		if (primerApellido == null || primerApellido.isBlank() || primerApellido.isEmpty())
		{
			System.out.println("El primer apellido no puede ir vacío.");
		} else if (primerApellido.length() < 3)
		{
			throw new IllegalArgumentException("La longitud del primer apellido es demasiado corta.");
		} else if (primerApellido.length() > 30)
		{
			throw new IllegalArgumentException("La longitud del primer apellido es demasiado larga.");
		} else if (utilidades.tieneSoloTexto(primerApellido))
		{
			this.primerApellido = primerApellido;
		}
	}

	public String getPrimerApellido()
	{
		return primerApellido;
	}

	public void setSegundoApellido(String segundoApellido) throws IllegalArgumentException
	{
		if (segundoApellido == null || segundoApellido.isBlank() || segundoApellido.isEmpty())
		{
			System.out.println("El segundo apellido no puede ir vacío.");
			return;
		}
		if (segundoApellido.length() < 3)
		{
			throw new IllegalArgumentException("La longitud del segundo apellido es demasiado corta.");
		} else if (segundoApellido.length() > 30)
		{
			throw new IllegalArgumentException("La longitud del segundo apellido es demasiado larga.");
		} else if (utilidades.tieneSoloTexto(segundoApellido))
		{
			this.segundoApellido = segundoApellido;
		}
	}

	public String getSegundoApellido()
	{
		return segundoApellido;
	}

	public void setNumeroDocumento(String numeroDocumento) throws IllegalArgumentException
	{
		if (numeroDocumento == null || numeroDocumento.isBlank() || numeroDocumento.isEmpty())
		{
			throw new IllegalArgumentException("El número de documento no puede estar vacío.");
		} else if (numeroDocumento.length() < 15)
		{
			throw new IllegalArgumentException("El número de documento tiene una longitud muy corta.");
		} else if (numeroDocumento.length() > 20)
		{
			throw new IllegalArgumentException("El número de documento tiene una longitud muy larga.");
		} else if (utilidades.tieneSoloNumeros(numeroDocumento))
		{
			this.numeroDocumento = numeroDocumento;
		}
	}

	public String getNumeroDocumento()
	{
		return numeroDocumento;
	}

	public void setTipoDocumento(String tipoDocumento) throws IllegalArgumentException
	{
		if (tipoDocumento == null || tipoDocumento.isBlank() || tipoDocumento.isEmpty())
		{
			System.out.println("El tipo de documento no puede estar vacío.");
		} else if (tipoDocumento.length() > 5)
		{
			System.out.println("El tipo de documento superó el máximo de carácteres.");
		} else if (utilidades.tieneSoloTexto(tipoDocumento))
		{
			this.tipoDocumento = tipoDocumento;
		}
	}

	public String getTipoDocumento()
	{
		return tipoDocumento;
	}

	public void setNombreUsuario(String nombreUsuario) throws IllegalArgumentException
	{
		if (nombreUsuario == null || nombreUsuario.isBlank() || nombreUsuario.isEmpty())
		{
			System.out.println("El nombre de usuario no puede estar vacío.");
		} else if (nombreUsuario.length() < 8)
		{
			throw new IllegalArgumentException("La longitud del nombre de usuario es demasiado corto.");
		} else if (nombreUsuario.length() > 20)
		{
			throw new IllegalArgumentException("La longitud del nombre de usuario es demasiado largo.");
		}
		this.nombreUsuario = nombreUsuario;
	}

	public String getNombreUsuario()
	{
		return nombreUsuario;
	}

	public void setContrasenia(String contrasenia) throws IllegalArgumentException
	{
		if (contrasenia == null || contrasenia.isBlank() || contrasenia.isEmpty())
		{
			System.out.println("La contraseña no puede estar vacía.");
		} else if (contrasenia.length() < 10)
		{
			throw new IllegalArgumentException("La longitud de la contraseña es demasiado corta.");
		} else if (contrasenia.length() > 255)
		{
			throw new IllegalArgumentException("La longitud de la contraseña es demasiado larga.");
		}
		this.contrasenia = contrasenia;
	}

	public String getContrasenia() 
	{
		return contrasenia;
	}

	public void setRol(RolUsuario rol) 
	{
		this.rol = rol;
	}

	public RolUsuario getRol() 
	{
		return rol;
	}

	public void setEmail(String email) throws IllegalArgumentException
	{
		if (email == null || email.isBlank() || email.isEmpty())
		{
			System.out.println("El email no puede estar vacío.");
		} else if (email.length() < 20)
		{
			throw new IllegalArgumentException("La longitud del email es demasiado corta.");
		} else if (email.length() > 60)
		{
			throw new IllegalArgumentException("La longitud del email es demasiado larga.");
		} else if (utilidades.esEmailValido(email))
		{
			this.email = email;
		}
	}

	public String getEmail() 
	{
		return email;
	}

	public void setTelefono(String telefono) throws IllegalArgumentException
	{
		if (telefono == null || telefono.isBlank() || telefono.isEmpty())
		{
			System.out.println("El teléfono no puede estar vacío.");
		} else if (telefono.length() < 8)
		{
			throw new IllegalArgumentException("La longitud del teléfono es demasiado corta.");
		} else if (telefono.length() > 15)
		{
			throw new IllegalArgumentException("La longitud del teléfono es demasiado larga.");
		} else if (utilidades.tieneSoloNumeros(telefono))
		{
			this.telefono = telefono;
		}
	}

	public String getTelefono() 
	{
		return telefono;
	}

	public void setUltima_sesion(LocalDateTime ultimaSesion) 
	{
		this.ultimaSesion = ultimaSesion;
	}

	public LocalDateTime getUltima_sesion() 
	{
		return ultimaSesion;
	}

	public void setUltimaSesion(LocalDateTime ultimaSesion) 
	{
		this.ultimaSesion = ultimaSesion;
	}

	public LocalDateTime getUltimaSesion() 
	{
		return ultimaSesion;
	}
}