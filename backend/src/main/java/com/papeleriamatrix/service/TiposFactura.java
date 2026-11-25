package com.papeleriamatrix.service;

public final class TiposFactura
{
	public static final String SERVICIO = "servicio";
	public static final String ROPA = "ropa";
	public static final String PAPELERIA = "papelería";
	public static final String PAPELERIA_SIN_TILDE = "papeleria";
	public static final String VARIEDAD = "variedad";
	/** Las ventas de la caja (VentaService). Mezclan tipos, así que se suman por línea. */
	public static final String VENTA_EN_TIENDA = VentaService.TIPO_FACTURA;

	private TiposFactura()
	{
	}
}
