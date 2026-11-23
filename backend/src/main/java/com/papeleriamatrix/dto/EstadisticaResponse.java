package com.papeleriamatrix.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EstadisticaResponse(
	Integer id,
	String codigoEstadistica,
	LocalDate desde,
	LocalDate hasta,
	int numeroFacturasGeneradas,
	int numeroProductosVendidos,
	int numeroRopaVendida,
	int numeroServicios,
	BigDecimal ticketPromedio,
	String tipoEstadistica)
{
}