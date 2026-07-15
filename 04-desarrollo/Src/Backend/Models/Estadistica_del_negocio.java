public class Estadistica_del_negocio extends Object 
{
	
	//Los atributos int (Aparte del ID) cumplen con la función de un contador.
	private int Id_estadistica = 0;
	private String Codigo_estadistica = "";
	private int Numero_informacion_eliminada = 0;
	private int Servicios_eliminados = 0;
	private int Numero_informacion_agregada = 0;
	private int Numero_servicios_ofrecidos = 0;
	private int Numero_productos_vendidos = 0;
	private int Operadores_cibercafe_despedidos = 0;
	private int Ropa_actual_catalogo = 0;
	private int Numero_productos_papeleria = 0;
	private int Ropa_removida_catalogo = 0;
	private int Productos_removidos = 0;
	private int Numero_ropa_vendida = 0;
	private int Numero_servicios = 0;
	private int Numero_facturas_generadas = 0;
	private Utils utilidades = new Utils();

	// Constructor con ID incremental
	public Estadistica_del_negocio(int idEstadistica, String codigoEstadistica)
	{
		this.Id_estadistica = idEstadistica;
		setCodigoEstadistica(codigoEstadistica);
	}

	// Constructor sin ID incremental
	public Estadistica_del_negocio(String codigoEstadistica)
	{
		setCodigoEstadistica(codigoEstadistica);
	}

	public int getIdEstadistica()
	{
		return Id_estadistica;
	}

	public void setCodigoEstadistica(String codigoEstadistica)
	{
		if(codigoEstadistica.isBlank() || codigoEstadistica.isEmpty())
		{
			throw new IllegalArgumentException("El código de la estadística no puede ir vacío.");
		} else if(codigoEstadistica.length() < 15)
		{
			throw new IllegalArgumentException("El código de la estadística no puede tener una longitud inferior a 15 caracteres.");
		} else if(codigoEstadistica.length() > 15)
		{
			throw new IllegalArgumentException("El código de la estadística no puede tener una longitud superior a 15 caracteres.");
		} else if(utilidades.tieneSoloNumeros(codigoEstadistica) == true)
		{
			this.Codigo_estadistica = codigoEstadistica;
		}
	}

	public String getCodigoEstadistica()
	{
		return Codigo_estadistica;
	}

	public void contarInformacionEliminada()
	{
		this.Numero_informacion_eliminada++;
	}

	public int getNumeroInformacionEliminada()
	{
		return Numero_informacion_eliminada;
	}

	public void contarServicioEliminado()
	{
		this.Servicios_eliminados++;
	}

	public int getServiciosEliminados()
	{
		return Servicios_eliminados;
	}

	public void contarInformacionAgregada()
	{
		this.Numero_informacion_agregada++;
	}

	public int getNumeroInformacionAgregada()
	{
		return Numero_informacion_agregada;
	}

	public void contarServicioOfrecido()
	{
		this.Numero_servicios_ofrecidos++;
	}

	public int getNumeroServiciosOfrecidos()
	{
		return Numero_servicios_ofrecidos;
	}

	public void contarProductoVendido()
	{
		this.Numero_productos_vendidos++;
	}

	public int getNumeroProductosVendidos()
	{
		return Numero_productos_vendidos;
	}

	public void contarOperadorCibercafeDespedido()
	{
		this.Operadores_cibercafe_despedidos++;
	}

	public int getOperadoresCibercafeDespedidos()
	{
		return Operadores_cibercafe_despedidos;
	}

	public void contarRopaActualCatalogo()
	{
		this.Ropa_actual_catalogo++;
	}

	public int getRopaActualCatalogo()
	{
		return Ropa_actual_catalogo;
	}

	public void contarNumeroProductoPapeleria()
	{
		this.Numero_productos_papeleria++;
	}

	public int getNumeroProductosPapeleria()
	{
		return Numero_productos_papeleria;
	}

	public void contarRopaRemovidaCatalogo()
	{
		this.Ropa_removida_catalogo++;
	}

	public int getRopaRemovidaCatalogo()
	{
		return Ropa_removida_catalogo;
	}

	public void contarProductoRemovido()
	{
		this.Productos_removidos++;
	}

	public int getProductosRemovidos()
	{
		return Productos_removidos;
	}

	public void contarRopaVendida()
	{
		this.Numero_ropa_vendida++;
	}

	public int getNumeroRopaVendida()
	{
		return Numero_ropa_vendida;
	}

	public void contarNumeroServicio()
	{
		this.Numero_servicios++;
	}

	public int getNumeroServicios()
	{
		return Numero_servicios;
	}

	public void contarFacturaGenerada()
	{
		this.Numero_facturas_generadas++;
	}

	public int getNumeroFacturasGeneradas()
	{
		return Numero_facturas_generadas;
	}
}