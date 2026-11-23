package com.papeleriamatrix.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IngresoResponse(
	Integer id,
	String codigoIngreso,
	LocalDate desde,
	LocalDate hasta,
	BigDecimal totalVendidoServicios,
	BigDecimal totalVendidoRopa,
	BigDecimal totalVendidoProductos,
	BigDecimal totalGeneral,
	String tipoIngreso)
{
}
