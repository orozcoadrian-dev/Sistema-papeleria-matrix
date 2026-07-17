package Backend.Models;
import Backend.Common.Utils;
import java.util.Date;

public class Producto_de_papeleria extends Object
{
	private int Id_producto = 0;
	private String Codigo_producto = "";
	private String Nombre = "";
	private int Cantidad = 0;
	private double Precio_venta = 0;
	private double Precio_compra = 0;
	private String Descripcion = "";
	private String Lugar_compra = "";
	private Date Fecha_publicacion;
	private Date Fecha_actualizacion;
	private Utils utilidades = new Utils();

	// Constructor con id autoincremental
	public Producto_de_papeleria(int idProducto, String codigoProducto, String nombre, int cantidad, double precioVenta, double precioCompra, String descripcion, String lugarCompra, Date fechaPublicacion, Date fechaActualizacion)
	{
		this.Id_producto = idProducto;
		setCodigoProducto(codigoProducto);
		setNombre(nombre);
		setCantidad(cantidad);
		setPrecioVenta(precioVenta);
		setPrecioCompra(precioCompra);
		setDescripcion(descripcion);
		setLugarCompra(lugarCompra);
		setFechaPublicacion(fechaPublicacion);
		setFechaActualizacion(fechaActualizacion);
	}

	// Constructor sin id incremental
	public Producto_de_papeleria(String codigoProducto, String nombre, int cantidad, double precioVenta, double precioCompra, String descripcion, String lugarCompra, Date fechaPublicacion, Date fechaActualizacion)
	{
		setCodigoProducto(codigoProducto);
		setNombre(nombre);
		setCantidad(cantidad);
		setPrecioVenta(precioVenta);
		setPrecioCompra(precioCompra);
		setDescripcion(descripcion);
		setLugarCompra(lugarCompra);
		setFechaPublicacion(fechaPublicacion);
		setFechaActualizacion(fechaActualizacion);
	}

	public int getIdProducto()
	{
		return Id_producto;
	}

	public void setCodigoProducto(String codigoProducto) throws IllegalArgumentException
	{
		if (codigoProducto.isEmpty() || codigoProducto.isBlank())
		{
			throw new IllegalArgumentException("El código de producto no puede estar vacío.");
		} else if (codigoProducto.length() < 10)
		{
			throw new IllegalArgumentException("La longitud del código de producto es demasiado corta.");
		} else if (codigoProducto.length() > 20)
		{
			throw new IllegalArgumentException("El código de producto no puede superar una longitud de 20 caracteres.");
		} else if(utilidades.tieneSoloNumeros(codigoProducto) == true)
		{
			this.Codigo_producto = codigoProducto;
		}
	}

	public String getCodigoProducto()
	{
		return Codigo_producto;
	}

	public void setNombre(String nombre) throws IllegalArgumentException
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
		} else if(utilidades.tieneSoloTexto(nombre) == true)
		{
			this.Nombre = nombre;
		}
	}

	public String getNombre()
	{
		return Nombre;
	}

	public void setCantidad(int cantidad) throws IllegalArgumentException
	{
		if (cantidad == 0)
		{
			throw new IllegalArgumentException("La cantidad no puede ser 0.");
		} else if (cantidad <= -1)
		{
			throw new IllegalArgumentException("La cantidad no puede ser negativo.");
		}
		this.Cantidad = cantidad;
	}

	public int getCantidad()
	{
		return Cantidad;
	}

	public void setPrecioVenta(double precioVenta) throws IllegalArgumentException
	{
		if (precioVenta == 0)
		{
			throw new IllegalArgumentException("El precio de venta no puede ser 0.");
		} else if (precioVenta <= -1)
		{
			throw new IllegalArgumentException("El precio de venta no puede ser negativo.");
		}
		this.Precio_venta = precioVenta;
	}

	public double getPrecioVenta()
	{
		return Precio_venta;
	}

	public void setPrecioCompra(double precioCompra) throws IllegalArgumentException
	{
		if (precioCompra == 0)
		{
			throw new IllegalArgumentException("El precio de compra no puede ser 0.");
		} else if (precioCompra <= -1)
		{
			throw new IllegalArgumentException("El precio de compra no puede ser negativo.");
		}
		this.Precio_compra = precioCompra;
	}

	public double getPrecioCompra()
	{
		return Precio_compra;
	}

	public void setDescripcion(String descripcion) throws IllegalArgumentException
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
		} else if(utilidades.tieneSoloTexto(descripcion) == true)
		{
		this.Descripcion = descripcion;
		}
	}

	public String getDescripcion()
	{
		return Descripcion;
	}

	public void setLugarCompra(String lugarCompra) throws IllegalArgumentException
	{
		if (lugarCompra.isEmpty() || lugarCompra.isBlank())
		{
			throw new IllegalArgumentException("El lugar de compra no puede estar vacío.");
		} else if (lugarCompra.length() < 10)
		{
			throw new IllegalArgumentException("La longitud del lugar de compra es demasiado corta.");
		} else if (lugarCompra.length() > 40)
		{
			throw new IllegalArgumentException("El lugar de compra no puede superar una longitud de 40 caracteres.");
		} else if(utilidades.tieneSoloTexto(lugarCompra) == true)
		{
		this.Lugar_compra = lugarCompra;
		}
	}

	public String getLugarCompra()
	{
		return Lugar_compra;
	}

	public void setFechaPublicacion(Date fechaPublicacion) throws IllegalArgumentException
	{
		if(fechaPublicacion == null)
		{
			throw new IllegalArgumentException("La fecha de publicación no puede estar vacía.");
		}
		this.Fecha_publicacion = fechaPublicacion;
	}

	public Date getFechaPublicacion()
	{
		return Fecha_publicacion;
	}

	public void setFechaActualizacion(Date fechaActualizacion) throws IllegalArgumentException
	{
		if(fechaActualizacion == null)
		{
			throw new IllegalArgumentException("La fecha de actualización no puede ir vacía.");
		}
		this.Fecha_actualizacion = fechaActualizacion;
	}

	public Date getFechaActualizacion()
	{
		return Fecha_actualizacion;
	}
}