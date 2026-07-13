import java.time.LocalDateTime;

public class Detalle_de_factura extends Object 
{
	
	private int Id_detalle = 0;
	private int Factura_de_negocio_id = 0;
	private String Descripcion = "";
	private int Servicio_id = 0;
	private int Cantidad_servicio = 0;
	private int Producto_papeleria_id = 0;
	private int Cantidad_producto = 0;
	private int Ropa_id = 0;
	private LocalDateTime Fecha_generacion;
	private LocalDateTime Fecha_actualizacion;

	// Constructor con id autoincremental
	public Detalle_de_factura(int idDetalle, int facturaDeNegocioId, String descripcion, int servicioId, int cantidadServicio, int productoPapeleriaId, int cantidadProducto, int Ropa_id, LocalDateTime fechaGeneracion, LocalDateTime fechaActualizacion)
	{
		this.Id_detalle = idDetalle;
		setFacturaDeNegocioId(facturaDeNegocioId);
		setDescripcion(descripcion);
		setServicioId(servicioId);
		setCantidadServicio(cantidadServicio);
		setProductoPapeleriaId(productoPapeleriaId);
		setCantidadProducto(cantidadProducto);
		setRopaId(Ropa_id);
		setFechaGeneracion(fechaGeneracion);
		setFechaActualizacion(fechaActualizacion);
	}

	//Constructor sin id incremental
	public Detalle_de_factura(int facturaDeNegocioId, String descripcion, int servicioId, int cantidadServicio, int productoPapeleriaId, int cantidadProducto, int ropaId, LocalDateTime fechaGeneracion, LocalDateTime fechaActualizacion)
	{
		setFacturaDeNegocioId(facturaDeNegocioId);
		setDescripcion(descripcion);
		setServicioId(servicioId);
		setCantidadServicio(cantidadServicio);
		setProductoPapeleriaId(productoPapeleriaId);
		setCantidadProducto(cantidadProducto);
		setRopaId(ropaId);
		setFechaGeneracion(fechaGeneracion);
		setFechaActualizacion(fechaActualizacion);
	}


	public int getIdDetalle()
	{
		return Id_detalle;
	}

	public void setFacturaDeNegocioId(int facturaDeNegocioId) {

		if(facturaDeNegocioId == 0)
		{
			throw new IllegalArgumentException("El ID de la factura del negocio no puede ser 0.");
		} else if (facturaDeNegocioId <= -1)
		{
			throw new IllegalArgumentException("El ID de la factura del negocio no puede ser negativo.");
		}
		Factura_de_negocio_id = facturaDeNegocioId;
	}

	public int getFacturaDeNegocioId() {
		return Factura_de_negocio_id;
	}

	public void setDescripcion(String descripcion) {

		if(descripcion.isBlank() || descripcion.isEmpty())
		{
			throw new IllegalArgumentException("La descripción no puede estar vacía.");
		} else if(descripcion.length() < 12)
		{
			throw new IllegalArgumentException("La longitud de la descripción es demasiado corta.");
		} else if(descripcion.length() > 120)
		{
			throw new IllegalArgumentException("La longitud de la descripción es demasiado larga.");
		}
		this.Descripcion = descripcion;
	}

	public String getDescripcion() {
		return Descripcion;
	}

	public void setServicioId(int servicioId) {
		if(servicioId == 0)
		{
			throw new IllegalArgumentException("El ID del servicio no puede ser 0.");
		} else if (servicioId <= -1)
		{
			throw new IllegalArgumentException("El ID del servicio no puede ser negativo.");
		}
		this.Servicio_id = servicioId;
	}

	public int getServicioId() {
		return Servicio_id;
	}

	public void setCantidadServicio(int cantidadServicio) {

		if(cantidadServicio == 0)
		{
			throw new IllegalArgumentException("La cantidad del servicio ofrecido no puede ser 0.");
		} else if (cantidadServicio <= -1)
		{
			throw new IllegalArgumentException("La cantidad del servicio ofrecido no puede ser negativo.");
		} else if(cantidadServicio > 150)
		{
			throw new IllegalArgumentException("La cantidad del servicio no puede superar las 150 unidades.");
		}
		Cantidad_servicio = cantidadServicio;
	}

	public int getCantidadServicio() {
		return Cantidad_servicio;
	}

	public void setProductoPapeleriaId(int productoPapeleriaId) {
		if(productoPapeleriaId == 0)
		{
			throw new IllegalArgumentException("El ID del producto de la papelería no puede ser 0.");
		} else if (productoPapeleriaId <= -1)
		{
			throw new IllegalArgumentException("El ID del producto de la papelería no puede ser negativo.");
		}
		Producto_papeleria_id = productoPapeleriaId;
	}

	public int getProductoPapeleriaId() {
		return Producto_papeleria_id;
	}

	public void setCantidadProducto(int cantidadProducto) {
		Cantidad_producto = cantidadProducto;
	}

	public int getCantidadProducto() {
		return Cantidad_producto;
	}

	public void setRopaId(int ropaId) {
		if(ropaId == 0)
		{
			throw new IllegalArgumentException("El ID de la ropa no puede ser 0.");
		} else if (ropaId <= -1)
		{
			throw new IllegalArgumentException("El ID de la ropa no puede ser negativo.");
		}
		Ropa_id = ropaId;
	}

	public int getRopaId() {
		return Ropa_id;
	}

	public void setFechaGeneracion(LocalDateTime fechaGeneracion) {
		Fecha_generacion = fechaGeneracion;
	}

	public LocalDateTime getFechaGeneracion() {
		return Fecha_generacion;
	}

	public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
		Fecha_actualizacion = fechaActualizacion;
	}
	
	public LocalDateTime getFechaActualizacion() {
		return Fecha_actualizacion;
	}
}