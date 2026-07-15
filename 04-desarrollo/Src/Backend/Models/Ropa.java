import java.util.Date;

public class Ropa extends Object
{
	private int Id_ropa = 0;
	private String Codigo_ropa = "";
	private String Tipo_ropa = "";
	private String Material = "";
	private String Talla = "";
	private String Descripcion = "";
	private String Nombre = "";
	private double Precio = 0;
	private String Color = "";
	private Date Fecha_registro;
	private Date Fecha_actualizacion;
	private Utils utilidades = new Utils();

	// Constructor con id autoincremental
	public Ropa(int idRopa, String codigoRopa, String tipoRopa, String material, String talla, String descripcion, String nombre, double precio, String color, Date fechaRegistro, Date fechaActualizacion)
	{
		this.Id_ropa = idRopa;
		setCodigoRopa(codigoRopa);
		setTipoRopa(tipoRopa);
		setMaterial(material);
		setTalla(talla);
		setDescripcion(descripcion);
		setNombre(nombre);
		setPrecio(precio);
		setColor(color);
		setFechaRegistro(fechaRegistro);
		setFechaActualizacion(fechaActualizacion);
	}

	// Constructor sin id incremental
	public Ropa(String codigoRopa, String tipoRopa, String material, String talla, String descripcion, String nombre, double precio, String color, Date fechaRegistro, Date fechaActualizacion)
	{
		setCodigoRopa(codigoRopa);
		setTipoRopa(tipoRopa);
		setMaterial(material);
		setTalla(talla);
		setDescripcion(descripcion);
		setNombre(nombre);
		setPrecio(precio);
		setColor(color);
		setFechaRegistro(fechaRegistro);
		setFechaActualizacion(fechaActualizacion);
	}

	public int getIdRopa()
	{
		return Id_ropa;
	}

	public void setCodigoRopa(String codigoRopa)
	{
		if (codigoRopa.isEmpty() || codigoRopa.isBlank())
		{
			throw new IllegalArgumentException("El código de ropa no puede estar vacío.");
		} else if (codigoRopa.length() < 10)
		{
			throw new IllegalArgumentException("La longitud del código de ropa es demasiado corta.");
		} else if (codigoRopa.length() > 20)
		{
			throw new IllegalArgumentException("El código de ropa no puede superar una longitud de 20 caracteres.");
		} else if (utilidades.tieneSoloNumeros(codigoRopa) == true)
		{
			this.Codigo_ropa = codigoRopa;
		}
	}

	public String getCodigoRopa()
	{
		return Codigo_ropa;
	}

	public void setTipoRopa(String tipoRopa)
	{
		if (tipoRopa.isEmpty() || tipoRopa.isBlank())
		{
			throw new IllegalArgumentException("El tipo de ropa no puede estar vacío.");
		} else if (tipoRopa.length() < 10)
		{
			throw new IllegalArgumentException("La longitud del tipo de ropa es demasiado corta.");
		} else if (tipoRopa.length() > 20)
		{
			throw new IllegalArgumentException("El tipo de ropa no puede superar una longitud de 20 caracteres.");
		} else if (utilidades.tieneSoloTexto(tipoRopa) == true)
		{
			this.Tipo_ropa = tipoRopa;
		}
	}

	public String getTipoRopa()
	{
		return Tipo_ropa;
	}

	public void setMaterial(String material)
	{
		if (material.isEmpty() || material.isBlank())
		{
			throw new IllegalArgumentException("El material no puede estar vacío.");
		} else if (material.length() < 10)
		{
			throw new IllegalArgumentException("La longitud del material es demasiado corta.");
		} else if (material.length() > 30)
		{
			throw new IllegalArgumentException("El material no puede superar una longitud de 30 caracteres.");
		} else if (utilidades.tieneSoloTexto(material) == true)
		{
			this.Material = material;
		}
	}

	public String getMaterial()
	{
		return Material;
	}

	public void setTalla(String talla)
	{
		if (talla.isEmpty() || talla.isBlank())
		{
			throw new IllegalArgumentException("La talla no puede estar vacía.");
		} else if (talla.length() < 1)
		{
			throw new IllegalArgumentException("La longitud de la talla es demasiado corta.");
		} else if (talla.length() > 5)
		{
			throw new IllegalArgumentException("La talla no puede superar una longitud de 5 caracteres.");
		} 
			this.Talla = talla;
	}

	public String getTalla()
	{
		return Talla;
	}

	public void setDescripcion(String descripcion)
	{
		if (descripcion.isEmpty() || descripcion.isBlank())
		{
			throw new IllegalArgumentException("La descripción no puede estar vacía.");
		} else if (descripcion.length() < 10)
		{
			throw new IllegalArgumentException("La longitud de la descripción es demasiado corta.");
		} else if (descripcion.length() > 150)
		{
			throw new IllegalArgumentException("La descripción no puede superar una longitud de 150 caracteres.");
		} else if (utilidades.tieneSoloTexto(descripcion) == true)
		{
			this.Descripcion = descripcion;
		}
	}

	public String getDescripcion()
	{
		return Descripcion;
	}

	public void setNombre(String nombre)
	{
		if (nombre.isEmpty() || nombre.isBlank())
		{
			throw new IllegalArgumentException("El nombre no puede estar vacío.");
		} else if (nombre.length() < 10)
		{
			throw new IllegalArgumentException("La longitud del nombre es demasiado corta.");
		} else if (nombre.length() > 30)
		{
			throw new IllegalArgumentException("El nombre no puede superar una longitud de 30 caracteres.");
		} else if (utilidades.tieneSoloTexto(nombre) == true)
		{
			this.Nombre = nombre;
		}
	}

	public String getNombre()
	{
		return Nombre;
	}

	public void setPrecio(double precio)
	{
		if (precio == 0)
		{
			throw new IllegalArgumentException("El precio no puede ser 0.");
		} else if (precio <= -1)
		{
			throw new IllegalArgumentException("El precio no puede ser negativo.");
		}
		this.Precio = precio;
	}

	public double getPrecio()
	{
		return Precio;
	}

	public void setColor(String color)
	{
		if (color.isEmpty() || color.isBlank())
		{
			throw new IllegalArgumentException("El color no puede estar vacío.");
		} else if (color.length() < 3)
		{
			throw new IllegalArgumentException("La longitud del color es demasiado corta.");
		} else if (color.length() > 20)
		{
			throw new IllegalArgumentException("El color no puede superar una longitud de 20 caracteres.");
		} else if (utilidades.tieneSoloTexto(color) == true)
		{
			this.Color = color;
		}
	}

	public String getColor()
	{
		return Color;
	}

	public void setFechaRegistro(Date fechaRegistro)
	{
		this.Fecha_registro = fechaRegistro;
	}

	public Date getFechaRegistro()
	{
		return Fecha_registro;
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