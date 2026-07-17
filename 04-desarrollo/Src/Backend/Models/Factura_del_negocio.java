package Backend.Models;
import Backend.Common.Utils;
import java.util.Date;

public class Factura_del_negocio extends Object 
{
	private int Id_factura = 0;
	private String Codigo_factura = "";
	private String Tipo_factura = "";
	private int Usuario_id = 0;
	private double Monto = 0;
	private Date Fecha_generacion;
	private Date Fecha_actualizacion;
	private Utils utilidades = new Utils();

	// Constructor con ID autoincremental
	public Factura_del_negocio(int idFactura, String codigoFactura, String tipoFactura, int usuarioId, double monto, Date fechaGeneracion, Date fechaActualizacion)
	{
		this.Id_factura = idFactura;
		setCodigoFactura(codigoFactura);
		setTipoFactura(tipoFactura);
		setUsuarioId(usuarioId);
		setMonto(monto);
		setFechaGeneracion(fechaGeneracion);
		setFechaActualizacion(fechaActualizacion);
	}

	// Constructor sin ID autoincremental
	public Factura_del_negocio(String codigoFactura, String tipoFactura, int usuarioId, double monto, Date fechaGeneracion, Date fechaActualizacion)
	{
		setCodigoFactura(codigoFactura);
		setTipoFactura(tipoFactura);
		setUsuarioId(usuarioId);
		setMonto(monto);
		setFechaGeneracion(fechaGeneracion);
		setFechaActualizacion(fechaActualizacion);
	}

	public int getIdFactura()
	{
		return Id_factura;
	}

	public void setCodigoFactura(String codigoFactura) throws IllegalArgumentException
	{
		if(codigoFactura.isBlank() || codigoFactura.isEmpty())
		{
			throw new IllegalArgumentException("El código de la factura no puede ir vacío.");
		} else if(codigoFactura.length() < 15)
		{
			throw new IllegalArgumentException("El código de la factura no puede tener una longitud inferior a 15 caracteres.");
		} else if(codigoFactura.length() > 15)
		{
			throw new IllegalArgumentException("El código de la factura no puede tener una longitud superior a 15 caracteres.");
		} else if(utilidades.tieneSoloNumeros(codigoFactura) == true)
		{
			this.Codigo_factura = codigoFactura;
		}
	}

	public String getCodigoFactura()
	{
		return Codigo_factura;
	}

	public void setTipoFactura(String tipoFactura) throws IllegalArgumentException
	{
		if(tipoFactura.isBlank() || tipoFactura.isEmpty())
		{
			throw new IllegalArgumentException("El tipo de la factura no puede ir vacío.");
		} else if(tipoFactura.length() < 8)
		{
			throw new IllegalArgumentException("El tipo de la factura tiene una longitud muy corta.");
		} else if(tipoFactura.length() > 15)
		{
			throw new IllegalArgumentException("El tipo de la factura no puede tener una longitud superior a 15 caracteres.");
		} else if(utilidades.tieneSoloTexto(tipoFactura) == true)
		{
			this.Tipo_factura = tipoFactura;
		}
	}

	public String getTipoFactura()
	{
		return Tipo_factura;
	}

	public void setUsuarioId(int usuarioId) throws IllegalArgumentException
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

	public void setMonto(double monto) throws IllegalArgumentException
	{
		if(monto == 0)
		{
			throw new IllegalArgumentException("El monto no puede ser igual a 0.");
		} else if(monto <= -1)
		{
			throw new IllegalArgumentException("El monto no puede ser negativo.");
		}
		this.Monto = monto;
	}

	public double getMonto()
	{
		return Monto;
	}

	public void setFechaGeneracion(Date fechaGeneracion) throws IllegalArgumentException
	{
		if(fechaGeneracion == null)
		{
			throw new IllegalArgumentException("La fecha de generación no puede ir vacía.");
		}
		this.Fecha_generacion = fechaGeneracion;
	}

	public Date getFechaGeneracion()
	{
		return Fecha_generacion;
	}

	public void setFechaActualizacion(Date fechaActualizacion) throws IllegalArgumentException
	{
		if (fechaActualizacion == null) {
			throw new IllegalArgumentException("La fecha de actualización no puede ir vacía.");
		}
		this.Fecha_actualizacion = fechaActualizacion;
	}

	public Date getFechaActualizacion()
	{
		return Fecha_actualizacion;
	}
}