package com.papeleriamatrix.dto;

import com.papeleriamatrix.model.Ingreso_del_negocio;

public record IngresoDelNegocioResponse(
	int idIngreso,
	String codigoIngreso,
	double totalSalarioOperadores,
	double totalVendidoRopa,
	double inversionTotalPapeleria,
	double totalVendidoServicios,
	double totalVendidoProductos,
	double totalRecaudoEsperadoPapeleria,
	String tipoIngreso)
{
	public static IngresoDelNegocioResponse desde(Ingreso_del_negocio ingreso)
	{
		return new IngresoDelNegocioResponse(
			ingreso.getIdIngreso(),
			ingreso.getCodigoIngreso(),
			ingreso.getTotalSalarioOperadores(),
			ingreso.getTotalVendidoRopa(),
			ingreso.getInversionTotalPapeleria(),
			ingreso.getTotalVendidoServicios(),
			ingreso.getTotalVendidoProductos(),
			ingreso.getTotalRecaudoEsperadoPapeleria(),
			ingreso.getTipoIngreso()
		);
	}
}
