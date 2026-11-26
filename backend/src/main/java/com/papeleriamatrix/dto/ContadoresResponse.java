package com.papeleriamatrix.dto;

/** Cuántos registros hay de cada cosa, incluidos los retirados y anulados. */
public record ContadoresResponse(
	long serviciosOfrecidos,
	long serviciosRetirados,
	long productosEnCatalogo,
	long productosRetirados,
	long ropaDisponible,
	long ropaVendida,
	long ropaRetirada,
	long datosPublicados,
	long datosRetirados,
	long operadoresActivos,
	long operadoresSuspendidos,
	long operadoresDespedidos,
	long clavesDePermiso,
	long ventas,
	long ventasAnuladas,
	long reportes)
{
}
