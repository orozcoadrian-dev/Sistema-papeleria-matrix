package com.papeleriamatrix.dto;

import com.papeleriamatrix.model.Estadistica_del_negocio;

public record EstadisticaDelNegocioResponse(
	int idEstadistica,
	String codigoEstadistica,
	String tipoEstadistica,
	int numeroInformacionEliminada,
	int serviciosEliminados,
	int numeroInformacionAgregada,
	int numeroServiciosOfrecidos,
	int numeroProductosVendidos,
	int operadoresCibercafeDespedidos,
	int ropaActualCatalogo,
	int numeroProductosPapeleria,
	int ropaRemovidaCatalogo,
	int productosRemovidos,
	int numeroRopaVendida,
	int numeroServicios,
	int numeroFacturasGeneradas)
{
	public static EstadisticaDelNegocioResponse desde(Estadistica_del_negocio estadistica)
	{
		return new EstadisticaDelNegocioResponse(
			estadistica.getIdEstadistica(),
			estadistica.getCodigoEstadistica(),
			estadistica.getTipoEstadistica(),
			estadistica.getNumeroInformacionEliminada(),
			estadistica.getServiciosEliminados(),
			estadistica.getNumeroInformacionAgregada(),
			estadistica.getNumeroServiciosOfrecidos(),
			estadistica.getNumeroProductosVendidos(),
			estadistica.getOperadoresCibercafeDespedidos(),
			estadistica.getRopaActualCatalogo(),
			estadistica.getNumeroProductosPapeleria(),
			estadistica.getRopaRemovidaCatalogo(),
			estadistica.getProductosRemovidos(),
			estadistica.getNumeroRopaVendida(),
			estadistica.getNumeroServicios(),
			estadistica.getNumeroFacturasGeneradas()
		);
	}
}
