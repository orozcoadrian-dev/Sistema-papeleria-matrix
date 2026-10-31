package com.papeleriamatrix.model;

import com.papeleriamatrix.common.Utils;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;

@Entity
@Table(name = "detalle_de_factura")
public class Detalle_de_factura 
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Id_detalle")
	private Integer idDetalle;

	@Column(name = "Factura_del_negocio_id", nullable = false)
	private Integer facturaDelNegocioId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Factura_del_negocio_id", insertable = false, updatable = false)
	private Factura_del_negocio factura;

	@Column(name = "Descripcion", nullable = false, length = 120)
	private String descripcion = "";

	@Column(name = "Servicio_id")
	private Integer servicioId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Servicio_id", insertable = false, updatable = false)
	private Servicio servicio;

	@Column(name = "Cantidad_servicio")
	private Integer cantidadServicio;

	@Column(name = "Producto_de_papeleria_id")
	private Integer productoPapeleriaId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Producto_de_papeleria_id", insertable = false, updatable = false)
	private ProductoPapeleria productoPapeleria;

	@Column(name = "Cantidad_producto")
	private Integer cantidadProducto;

	@Column(name = "Ropa_id")
	private Integer ropaId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Ropa_id", insertable = false, updatable = false)
	private Ropa ropa;

	@Column(name = "Cantidad_ropa")
	private Integer cantidadRopa;

	@Column(name = "Fecha_generacion", nullable = false)
	private LocalDateTime fechaGeneracion;

	@Column(name = "Fecha_actualizacion")
	private LocalDateTime fechaActualizacion;

	@Transient
	private Utils utilidades = new Utils();

	public Detalle_de_factura()
	{
	}

	public Detalle_de_factura(int idDetalle, int facturaDelNegocioId, String descripcion, int servicioId, int cantidadServicio, int productoPapeleriaId, int cantidadProducto, int ropaId, int cantidadRopa, LocalDateTime fechaGeneracion, LocalDateTime fechaActualizacion)
	{
		this.idDetalle = idDetalle;
		setFacturaDelNegocioId(facturaDelNegocioId);
		setDescripcion(descripcion);
		setServicioId(servicioId);
		setCantidadServicio(cantidadServicio);
		setProductoPapeleriaId(productoPapeleriaId);
		setCantidadProducto(cantidadProducto);
		setRopaId(ropaId);
		setCantidadRopa(cantidadRopa);
		setFechaGeneracion(fechaGeneracion);
		setFechaActualizacion(fechaActualizacion);
	}

	public Detalle_de_factura(int facturaDelNegocioId, String descripcion, int servicioId, int cantidadServicio, int productoPapeleriaId, int cantidadProducto, int ropaId, int cantidadRopa, LocalDateTime fechaGeneracion, LocalDateTime fechaActualizacion)
	{
		setFacturaDelNegocioId(facturaDelNegocioId);
		setDescripcion(descripcion);
		setServicioId(servicioId);
		setCantidadServicio(cantidadServicio);
		setProductoPapeleriaId(productoPapeleriaId);
		setCantidadProducto(cantidadProducto);
		setRopaId(ropaId);
		setCantidadRopa(cantidadRopa);
		setFechaGeneracion(fechaGeneracion);
		setFechaActualizacion(fechaActualizacion);
	}

	public int getIdDetalle()
	{
		return idDetalle != null ? idDetalle : 0;
	}

	public void setIdDetalle(int idDetalle)
	{
		this.idDetalle = idDetalle;
	}

	public void setFacturaDelNegocioId(int facturaDelNegocioId) throws IllegalArgumentException
	{
		if (facturaDelNegocioId == 0)
		{
			throw new IllegalArgumentException("El ID de la factura del negocio no puede ser 0.");
		} else if (facturaDelNegocioId <= -1)
		{
			throw new IllegalArgumentException("El ID de la factura del negocio no puede ser negativo.");
		}
		this.facturaDelNegocioId = facturaDelNegocioId;
	}

	public int getFacturaDelNegocioId() 
	{
		return facturaDelNegocioId != null ? facturaDelNegocioId : 0;
	}

	public Factura_del_negocio getFactura()
	{
		return factura;
	}

	public void setFactura(Factura_del_negocio factura)
	{
		this.factura = factura;
		if (factura != null)
		{
			this.facturaDelNegocioId = factura.getIdFactura();
		}
	}

	public void setDescripcion(String descripcion) throws IllegalArgumentException
	{
		if (descripcion == null || descripcion.isBlank() || descripcion.isEmpty())
		{
			throw new IllegalArgumentException("La descripción no puede estar vacía.");
		} else if (descripcion.length() < 12)
		{
			throw new IllegalArgumentException("La longitud de la descripción es demasiado corta.");
		} else if (descripcion.length() > 120)
		{
			throw new IllegalArgumentException("La longitud de la descripción es demasiado larga.");
		} else if (utilidades.tieneSoloTexto(descripcion))
		{
			this.descripcion = descripcion;
		}
	}

	public String getDescripcion() 
	{
		return descripcion;
	}

	public void setServicioId(int servicioId) throws IllegalArgumentException
	{
		if (servicioId <= -1)
		{
			throw new IllegalArgumentException("El ID del servicio no puede ser negativo.");
		}
		this.servicioId = servicioId != 0 ? servicioId : null;
	}

	public int getServicioId() 
	{
		return servicioId != null ? servicioId : 0;
	}

	public Servicio getServicio()
	{
		return servicio;
	}

	public void setServicio(Servicio servicio)
	{
		this.servicio = servicio;
		if (servicio != null)
		{
			this.servicioId = servicio.getId();
		}
	}

	public void setCantidadServicio(int cantidadServicio) throws IllegalArgumentException
	{
		if (cantidadServicio < 0)
		{
			throw new IllegalArgumentException("La cantidad del servicio ofrecido no puede ser negativo.");
		} else if (cantidadServicio > 150)
		{
			throw new IllegalArgumentException("La cantidad del servicio no puede superar las 150 unidades.");
		}
		this.cantidadServicio = cantidadServicio;
	}

	public int getCantidadServicio() 
	{
		return cantidadServicio != null ? cantidadServicio : 0;
	}

	public void setProductoPapeleriaId(int productoPapeleriaId) throws IllegalArgumentException
	{
		if (productoPapeleriaId <= -1)
		{
			throw new IllegalArgumentException("El ID del producto de la papelería no puede ser negativo.");
		}
		this.productoPapeleriaId = productoPapeleriaId != 0 ? productoPapeleriaId : null;
	}

	public int getProductoPapeleriaId() 
	{
		return productoPapeleriaId != null ? productoPapeleriaId : 0;
	}

	public ProductoPapeleria getProductoPapeleria()
	{
		return productoPapeleria;
	}

	public void setProductoPapeleria(ProductoPapeleria productoPapeleria)
	{
		this.productoPapeleria = productoPapeleria;
		if (productoPapeleria != null)
		{
			this.productoPapeleriaId = productoPapeleria.getId();
		}
	}

	public void setCantidadProducto(int cantidadProducto) throws IllegalArgumentException
	{
		if (cantidadProducto < 0)
		{
			throw new IllegalArgumentException("La cantidad de productos no puede ser negativa.");
		}
		this.cantidadProducto = cantidadProducto;
	}

	public int getCantidadProducto() 
	{
		return cantidadProducto != null ? cantidadProducto : 0;
	}

	public void setRopaId(int ropaId) throws IllegalArgumentException
	{
		if (ropaId <= -1)
		{
			throw new IllegalArgumentException("El ID de la ropa no puede ser negativo.");
		}
		this.ropaId = ropaId != 0 ? ropaId : null;
	}

	public int getRopaId() 
	{
		return ropaId != null ? ropaId : 0;
	}

	public Ropa getRopa()
	{
		return ropa;
	}

	public void setRopa(Ropa ropa)
	{
		this.ropa = ropa;
		if (ropa != null)
		{
			this.ropaId = ropa.getId();
		}
	}

	public void setCantidadRopa(int cantidadRopa) throws IllegalArgumentException
	{
		if (cantidadRopa < 0)
		{
			throw new IllegalArgumentException("La cantidad de ropa no puede ser negativa.");
		}
		this.cantidadRopa = cantidadRopa;
	}

	public int getCantidadRopa() 
	{
		return cantidadRopa != null ? cantidadRopa : 0;
	}

	public void setFechaGeneracion(LocalDateTime fechaGeneracion) throws IllegalArgumentException
	{
		if (fechaGeneracion == null)
		{
			throw new IllegalArgumentException("La fecha de generación no puede estar vacía.");
		}
		this.fechaGeneracion = fechaGeneracion;
	}

	public LocalDateTime getFechaGeneracion() 
	{
		return fechaGeneracion;
	}

	public void setFechaActualizacion(LocalDateTime fechaActualizacion)
	{
		this.fechaActualizacion = fechaActualizacion;
	}
	
	public LocalDateTime getFechaActualizacion() 
	{
		return fechaActualizacion;
	}
}