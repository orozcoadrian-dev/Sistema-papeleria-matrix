package com.papeleriamatrix.model;

import com.papeleriamatrix.common.Utils;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "estadistica_del_negocio")
public class Estadistica_del_negocio 
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Id_estadistica")
	private Integer idEstadistica;

	@Column(name = "Codigo_estadistica", nullable = false, unique = true, length = 15)
	private String codigoEstadistica = "";

	@Column(name = "Tipo_estadistica", nullable = false, length = 15)
	private String tipoEstadistica = "";

	@Column(name = "Numero_informacion_eliminada")
	private Integer numeroInformacionEliminada = 0;

	@Column(name = "Servicios_eliminados")
	private Integer serviciosEliminados = 0;

	@Column(name = "Numero_informacion_agregada")
	private Integer numeroInformacionAgregada = 0;

	@Column(name = "Numero_servicios_ofrecidos")
	private Integer numeroServiciosOfrecidos = 0;

	@Column(name = "Numero_productos_vendidos")
	private Integer numeroProductosVendidos = 0;

	@Column(name = "Operadores_cibercafe_despedidos")
	private Integer operadoresCibercafeDespedidos = 0;

	@Column(name = "Ropa_actual_catalogo")
	private Integer ropaActualCatalogo = 0;

	@Column(name = "Numero_productos_papeleria")
	private Integer numeroProductosPapeleria = 0;

	@Column(name = "Ropa_removida_catalogo")
	private Integer ropaRemovidaCatalogo = 0;

	@Column(name = "Productos_removidos")
	private Integer productosRemovidos = 0;

	@Column(name = "Numero_ropa_vendida")
	private Integer numeroRopaVendida = 0;

	@Column(name = "Numero_servicios")
	private Integer numeroServicios = 0;

	@Column(name = "Numero_facturas_generadas")
	private Integer numeroFacturasGeneradas = 0;

	@Transient
	private Utils utilidades = new Utils();

	public Estadistica_del_negocio()
	{
	}

	public Estadistica_del_negocio(int idEstadistica, String codigoEstadistica, String tipoEstadistica)
	{
		this.idEstadistica = idEstadistica;
		setCodigoEstadistica(codigoEstadistica);
		setTipoEstadistica(tipoEstadistica);
	}

	public Estadistica_del_negocio(String codigoEstadistica, String tipoEstadistica)
	{
		setCodigoEstadistica(codigoEstadistica);
		setTipoEstadistica(tipoEstadistica);
	}

	public int getIdEstadistica()
	{
		return idEstadistica != null ? idEstadistica : 0;
	}

	public void setIdEstadistica(int idEstadistica)
	{
		this.idEstadistica = idEstadistica;
	}

	public void setCodigoEstadistica(String codigoEstadistica) throws IllegalArgumentException
	{
		if (codigoEstadistica == null || codigoEstadistica.isBlank() || codigoEstadistica.isEmpty())
		{
			throw new IllegalArgumentException("El código de la estadística no puede ir vacío.");
		} else if (codigoEstadistica.length() != 15)
		{
			throw new IllegalArgumentException("El código de la estadística debe tener una longitud exacta de 15 caracteres.");
		} else if (utilidades.tieneSoloNumeros(codigoEstadistica))
		{
			this.codigoEstadistica = codigoEstadistica;
		}
	}

	public String getCodigoEstadistica()
	{
		return codigoEstadistica;
	}

	public void setTipoEstadistica(String tipoEstadistica) throws IllegalArgumentException
	{
		if (tipoEstadistica == null || tipoEstadistica.isBlank() || tipoEstadistica.isEmpty())
		{
			throw new IllegalArgumentException("El tipo de la estadística no puede ir vacío.");
		} else if (tipoEstadistica.length() > 15)
		{
			throw new IllegalArgumentException("El tipo de la estadística no puede superar los 15 caracteres.");
		}
		this.tipoEstadistica = tipoEstadistica;
	}

	public String getTipoEstadistica()
	{
		return tipoEstadistica;
	}

	public void contarInformacionEliminada()
	{
		if (this.numeroInformacionEliminada == null) this.numeroInformacionEliminada = 0;
		this.numeroInformacionEliminada++;
	}

	public int getNumeroInformacionEliminada()
	{
		return numeroInformacionEliminada != null ? numeroInformacionEliminada : 0;
	}

	public void setNumeroInformacionEliminada(int numeroInformacionEliminada)
	{
		this.numeroInformacionEliminada = numeroInformacionEliminada;
	}

	public void contarServicioEliminado()
	{
		if (this.serviciosEliminados == null) this.serviciosEliminados = 0;
		this.serviciosEliminados++;
	}

	public int getServiciosEliminados()
	{
		return serviciosEliminados != null ? serviciosEliminados : 0;
	}

	public void setServiciosEliminados(int serviciosEliminados)
	{
		this.serviciosEliminados = serviciosEliminados;
	}

	public void contarInformacionAgregada()
	{
		if (this.numeroInformacionAgregada == null) this.numeroInformacionAgregada = 0;
		this.numeroInformacionAgregada++;
	}

	public int getNumeroInformacionAgregada()
	{
		return numeroInformacionAgregada != null ? numeroInformacionAgregada : 0;
	}

	public void setNumeroInformacionAgregada(int numeroInformacionAgregada)
	{
		this.numeroInformacionAgregada = numeroInformacionAgregada;
	}

	public void contarServicioOfrecido()
	{
		if (this.numeroServiciosOfrecidos == null) this.numeroServiciosOfrecidos = 0;
		this.numeroServiciosOfrecidos++;
	}

	public int getNumeroServiciosOfrecidos()
	{
		return numeroServiciosOfrecidos != null ? numeroServiciosOfrecidos : 0;
	}

	public void setNumeroServiciosOfrecidos(int numeroServiciosOfrecidos)
	{
		this.numeroServiciosOfrecidos = numeroServiciosOfrecidos;
	}

	public void contarProductoVendido()
	{
		if (this.numeroProductosVendidos == null) this.numeroProductosVendidos = 0;
		this.numeroProductosVendidos++;
	}

	public int getNumeroProductosVendidos()
	{
		return numeroProductosVendidos != null ? numeroProductosVendidos : 0;
	}

	public void setNumeroProductosVendidos(int numeroProductosVendidos)
	{
		this.numeroProductosVendidos = numeroProductosVendidos;
	}

	public void contarOperadorCibercafeDespedido()
	{
		if (this.operadoresCibercafeDespedidos == null) this.operadoresCibercafeDespedidos = 0;
		this.operadoresCibercafeDespedidos++;
	}

	public int getOperadoresCibercafeDespedidos()
	{
		return operadoresCibercafeDespedidos != null ? operadoresCibercafeDespedidos : 0;
	}

	public void setOperadoresCibercafeDespedidos(int operadoresCibercafeDespedidos)
	{
		this.operadoresCibercafeDespedidos = operadoresCibercafeDespedidos;
	}

	public void contarRopaActualCatalogo()
	{
		if (this.ropaActualCatalogo == null) this.ropaActualCatalogo = 0;
		this.ropaActualCatalogo++;
	}

	public int getRopaActualCatalogo()
	{
		return ropaActualCatalogo != null ? ropaActualCatalogo : 0;
	}

	public void setRopaActualCatalogo(int ropaActualCatalogo)
	{
		this.ropaActualCatalogo = ropaActualCatalogo;
	}

	public void contarNumeroProductoPapeleria()
	{
		if (this.numeroProductosPapeleria == null) this.numeroProductosPapeleria = 0;
		this.numeroProductosPapeleria++;
	}

	public int getNumeroProductosPapeleria()
	{
		return numeroProductosPapeleria != null ? numeroProductosPapeleria : 0;
	}

	public void setNumeroProductosPapeleria(int numeroProductosPapeleria)
	{
		this.numeroProductosPapeleria = numeroProductosPapeleria;
	}

	public void contarRopaRemovidaCatalogo()
	{
		if (this.ropaRemovidaCatalogo == null) this.ropaRemovidaCatalogo = 0;
		this.ropaRemovidaCatalogo++;
	}

	public int getRopaRemovidaCatalogo()
	{
		return ropaRemovidaCatalogo != null ? ropaRemovidaCatalogo : 0;
	}

	public void setRopaRemovidaCatalogo(int ropaRemovidaCatalogo)
	{
		this.ropaRemovidaCatalogo = ropaRemovidaCatalogo;
	}

	public void contarProductoRemovido()
	{
		if (this.productosRemovidos == null) this.productosRemovidos = 0;
		this.productosRemovidos++;
	}

	public int getProductosRemovidos()
	{
		return productosRemovidos != null ? productosRemovidos : 0;
	}

	public void setProductosRemovidos(int productosRemovidos)
	{
		this.productosRemovidos = productosRemovidos;
	}

	public void contarRopaVendida()
	{
		if (this.numeroRopaVendida == null) this.numeroRopaVendida = 0;
		this.numeroRopaVendida++;
	}

	public int getNumeroRopaVendida()
	{
		return numeroRopaVendida != null ? numeroRopaVendida : 0;
	}

	public void setNumeroRopaVendida(int numeroRopaVendida)
	{
		this.numeroRopaVendida = numeroRopaVendida;
	}

	public void contarNumeroServicio()
	{
		if (this.numeroServicios == null) this.numeroServicios = 0;
		this.numeroServicios++;
	}

	public int getNumeroServicios()
	{
		return numeroServicios != null ? numeroServicios : 0;
	}

	public void setNumeroServicios(int numeroServicios)
	{
		this.numeroServicios = numeroServicios;
	}

	public void contarFacturaGenerada()
	{
		if (this.numeroFacturasGeneradas == null) this.numeroFacturasGeneradas = 0;
		this.numeroFacturasGeneradas++;
	}

	public int getNumeroFacturasGeneradas()
	{
		return numeroFacturasGeneradas != null ? numeroFacturasGeneradas : 0;
	}

	public void setNumeroFacturasGeneradas(int numeroFacturasGeneradas)
	{
		this.numeroFacturasGeneradas = numeroFacturasGeneradas;
	}
}