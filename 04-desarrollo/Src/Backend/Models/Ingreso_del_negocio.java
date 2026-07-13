public class Ingreso_del_negocio extends Object 
{
	private int Id_ingreso = 0;
	private String Codigo_ingreso = "";
	private double Total_salario_operadores = 0;
	private double Total_vendido_ropa = 0;
	private double Inversion_total_papeleria = 0;
	private double Total_vendido_servicios = 0;
	private double Total_vendido_productos = 0;
	private double Total_recaudo_esperado_papeleria = 0;
	private String Tipo_ingreso = "";

	// Constructor con id autoincremental
	public Ingreso_del_negocio(int idIngreso, String codigoIngreso, double totalSalarioOperadores, double totalVendidoRopa, double inversionTotalPapeleria, double totalVendidoServicios, double totalVendidoProductos, double totalRecaudoEsperadoPapeleria, String tipoIngreso)
	{
		this.Id_ingreso = idIngreso;
		setCodigoIngreso(codigoIngreso);
		setTotalSalarioOperadores(totalSalarioOperadores);
		setTotalVendidoRopa(totalVendidoRopa);
		setInversionTotalPapeleria(inversionTotalPapeleria);
		setTotalVendidoServicios(totalVendidoServicios);
		setTotalVendidoProductos(totalVendidoProductos);
		setTotalRecaudoEsperadoPapeleria(totalRecaudoEsperadoPapeleria);
		setTipoIngreso(tipoIngreso);
	}

	// Constructor sin id incremental
	public Ingreso_del_negocio(String codigoIngreso, double totalSalarioOperadores, double totalVendidoRopa, double inversionTotalPapeleria, double totalVendidoServicios, double totalVendidoProductos, double totalRecaudoEsperadoPapeleria, String tipoIngreso)
	{
		setCodigoIngreso(codigoIngreso);
		setTotalSalarioOperadores(totalSalarioOperadores);
		setTotalVendidoRopa(totalVendidoRopa);
		setInversionTotalPapeleria(inversionTotalPapeleria);
		setTotalVendidoServicios(totalVendidoServicios);
		setTotalVendidoProductos(totalVendidoProductos);
		setTotalRecaudoEsperadoPapeleria(totalRecaudoEsperadoPapeleria);
		setTipoIngreso(tipoIngreso);
	}

	public int getIdIngreso()
	{
		return Id_ingreso;
	}

	public void setCodigoIngreso(String codigoIngreso)
	{
		if (codigoIngreso.isEmpty() || codigoIngreso.isBlank())
		{
			throw new IllegalArgumentException("El código de ingreso no puede estar vacío.");
		} else if (codigoIngreso.length() < 10)
		{
			throw new IllegalArgumentException("La longitud del código de ingreso es demasiado corta.");
		} else if (codigoIngreso.length() > 15)
		{
			throw new IllegalArgumentException("El código de ingreso no puede superar una longitud de 15 caracteres.");
		}
		this.Codigo_ingreso = codigoIngreso;
	}

	public String getCodigoIngreso()
	{
		return Codigo_ingreso;
	}

	public void setTotalSalarioOperadores(double totalSalarioOperadores)
	{
		if (totalSalarioOperadores == 0)
		{
			throw new IllegalArgumentException("El total de salario de operadores no puede ser 0.");
		} else if (totalSalarioOperadores <= -1)
		{
			throw new IllegalArgumentException("El total de salario de operadores no puede ser negativo.");
		}
		this.Total_salario_operadores = totalSalarioOperadores;
	}

	public double getTotalSalarioOperadores()
	{
		return Total_salario_operadores;
	}

	public void setTotalVendidoRopa(double totalVendidoRopa)
	{
		if (totalVendidoRopa == 0)
		{
			throw new IllegalArgumentException("El total vendido de ropa no puede ser 0.");
		} else if (totalVendidoRopa <= -1)
		{
			throw new IllegalArgumentException("El total vendido de ropa no puede ser negativo.");
		}
		this.Total_vendido_ropa = totalVendidoRopa;
	}

	public double getTotalVendidoRopa()
	{
		return Total_vendido_ropa;
	}

	public void setInversionTotalPapeleria(double inversionTotalPapeleria)
	{
		if (inversionTotalPapeleria == 0)
		{
			throw new IllegalArgumentException("La inversión total de papelería no puede ser 0.");
		} else if (inversionTotalPapeleria <= -1)
		{
			throw new IllegalArgumentException("La inversión total de papelería no puede ser negativo.");
		}
		this.Inversion_total_papeleria = inversionTotalPapeleria;
	}

	public double getInversionTotalPapeleria()
	{
		return Inversion_total_papeleria;
	}

	public void setTotalVendidoServicios(double totalVendidoServicios)
	{
		if (totalVendidoServicios == 0)
		{
			throw new IllegalArgumentException("El total vendido de servicios no puede ser 0.");
		} else if (totalVendidoServicios <= -1)
		{
			throw new IllegalArgumentException("El total vendido de servicios no puede ser negativo.");
		}
		this.Total_vendido_servicios = totalVendidoServicios;
	}

	public double getTotalVendidoServicios()
	{
		return Total_vendido_servicios;
	}

	public void setTotalVendidoProductos(double totalVendidoProductos)
	{
		if (totalVendidoProductos == 0)
		{
			throw new IllegalArgumentException("El total vendido de productos no puede ser 0.");
		} else if (totalVendidoProductos <= -1)
		{
			throw new IllegalArgumentException("El total vendido de productos no puede ser negativo.");
		}
		this.Total_vendido_productos = totalVendidoProductos;
	}

	public double getTotalVendidoProductos()
	{
		return Total_vendido_productos;
	}

	public void setTotalRecaudoEsperadoPapeleria(double totalRecaudoEsperadoPapeleria)
	{
		if (totalRecaudoEsperadoPapeleria == 0)
		{
			throw new IllegalArgumentException("El total de recaudo esperado de papelería no puede ser 0.");
		} else if (totalRecaudoEsperadoPapeleria <= -1)
		{
			throw new IllegalArgumentException("El total de recaudo esperado de papelería no puede ser negativo.");
		}
		this.Total_recaudo_esperado_papeleria = totalRecaudoEsperadoPapeleria;
	}

	public double getTotalRecaudoEsperadoPapeleria()
	{
		return Total_recaudo_esperado_papeleria;
	}

	public void setTipoIngreso(String tipoIngreso)
	{
		if (tipoIngreso.isEmpty() || tipoIngreso.isBlank())
		{
			throw new IllegalArgumentException("El tipo de ingreso no puede estar vacío.");
		} else if (tipoIngreso.length() < 10)
		{
			throw new IllegalArgumentException("La longitud del tipo de ingreso es demasiado corta.");
		} else if (tipoIngreso.length() > 15)
		{
			throw new IllegalArgumentException("El tipo de ingreso no puede superar una longitud de 15 caracteres.");
		}
		this.Tipo_ingreso = tipoIngreso;
	}

	public String getTipoIngreso()
	{
		return Tipo_ingreso;
	}
}