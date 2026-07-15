import java.util.Date; 

public class Servicio extends Object
{
	private int Id_servicio = 0;
	private String Codigo_servicio = "";
	private String Nombre = "";
	private String Categoria = "";
	private double Precio = 0;
	private String Descripcion = "";
	private Date Fecha_vinculacion;
	private Date Fecha_actualizacion;
	private Utils utilidades = new Utils();

	// Constructor con id autoincremental
	public Servicio(int idServicio, String codigoServicio, String nombre, String categoria, double precio, String descripcion, Date fechaVinculacion, Date fechaActualizacion)
	{
		this.Id_servicio = idServicio;
		setCodigoServicio(codigoServicio);
		setNombre(nombre);
		setCategoria(categoria);
		setPrecio(precio);
		setDescripcion(descripcion);
		setFechaVinculacion(fechaVinculacion);
		setFechaActualizacion(fechaActualizacion);
	}

	// Constructor sin id incremental
	public Servicio(String codigoServicio, String nombre, String categoria, double precio, String descripcion, Date fechaVinculacion, Date fechaActualizacion)
	{
		setCodigoServicio(codigoServicio);
		setNombre(nombre);
		setCategoria(categoria);
		setPrecio(precio);
		setDescripcion(descripcion);
		setFechaVinculacion(fechaVinculacion);
		setFechaActualizacion(fechaActualizacion);
	}

	public int getIdServicio()
	{
		return Id_servicio;
	}

	public void setCodigoServicio(String codigoServicio)
	{
		if (codigoServicio.isEmpty() || codigoServicio.isBlank())
		{
			throw new IllegalArgumentException("El código de servicio no puede estar vacío.");
		} else if (codigoServicio.length() < 10)
		{
			throw new IllegalArgumentException("La longitud del código de servicio es demasiado corta.");
		} else if (codigoServicio.length() > 20)
		{
			throw new IllegalArgumentException("El código de servicio no puede superar una longitud de 20 caracteres.");
		} else if (utilidades.tieneSoloNumeros(codigoServicio) == true)
		{
			this.Codigo_servicio = codigoServicio;
		}
	}

	public String getCodigoServicio()
	{
		return Codigo_servicio;
	}

	public void setNombre(String nombre)
	{
		if(nombre.isBlank() || nombre.isEmpty())
		{
			throw new IllegalArgumentException("El nombre del servicio no puede ir vacío.");
		} else if(nombre.length() < 4)
		{
			throw new IllegalArgumentException("La longitud del nombre no puede ser menor de 4 caracteres.");
		} else if(nombre.length() > 30)
		{
			throw new IllegalArgumentException("La longitud del nombre no puede ser mayor de 30 caracteres.");
		} else if (utilidades.tieneSoloTexto(nombre) == true)
		{
			this.Nombre = nombre;
		}
	}

	public String getNombre()
	{
		return Nombre;
	}

	public void setCategoria(String categoria)
	{
		if (categoria.isEmpty() || categoria.isBlank())
		{
			throw new IllegalArgumentException("La categoría no puede estar vacía.");
		} else if (categoria.length() < 8)
		{
			throw new IllegalArgumentException("La longitud de la categoría es demasiado corta.");
		} else if (categoria.length() > 30)
		{
			throw new IllegalArgumentException("La categoría no puede superar una longitud de 30 caracteres.");
		} else if (utilidades.tieneSoloTexto(categoria) == true)
		{
			this.Categoria = categoria;
		}
	}

	public String getCategoria()
	{
		return Categoria;
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

	public void setFechaVinculacion(Date fechaVinculacion)
	{
		this.Fecha_vinculacion = fechaVinculacion;
	}

	public Date getFechaVinculacion()
	{
		return Fecha_vinculacion;
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