package com.papeleriamatrix.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EstadisticaDelNegocioRequest(
	@NotBlank(message = "El código de la estadística es obligatorio.")
	@Size(min = 15, max = 15, message = "El código de la estadística debe tener exactamente 15 caracteres.")
	String codigoEstadistica,

	@NotBlank(message = "El tipo de estadística es obligatorio.")
	@Size(max = 15, message = "El tipo de estadística no puede superar los 15 caracteres.")
	String tipoEstadistica,

	@Min(value = 0, message = "El número de información eliminada no puede ser negativo.")
	Integer numeroInformacionEliminada,

	@Min(value = 0, message = "El número de servicios eliminados no puede ser negativo.")
	Integer serviciosEliminados,

	@Min(value = 0, message = "El número de información agregada no puede ser negativo.")
	Integer numeroInformacionAgregada,

	@Min(value = 0, message = "El número de servicios ofrecidos no puede ser negativo.")
	Integer numeroServiciosOfrecidos,

	@Min(value = 0, message = "El número de productos vendidos no puede ser negativo.")
	Integer numeroProductosVendidos,

	@Min(value = 0, message = "El número de operadores despedidos no puede ser negativo.")
	Integer operadoresCibercafeDespedidos,

	@Min(value = 0, message = "La cantidad de ropa en catálogo no puede ser negativa.")
	Integer ropaActualCatalogo,

	@Min(value = 0, message = "El número de productos de papelería no puede ser negativo.")
	Integer numeroProductosPapeleria,

	@Min(value = 0, message = "La cantidad de ropa removida no puede ser negativa.")
	Integer ropaRemovidaCatalogo,

	@Min(value = 0, message = "El número de productos removidos no puede ser negativo.")
	Integer productosRemovidos,

	@Min(value = 0, message = "El número de ropa vendida no puede ser negativo.")
	Integer numeroRopaVendida,

	@Min(value = 0, message = "El número de servicios no puede ser negativo.")
	Integer numeroServicios,

	@Min(value = 0, message = "El número de facturas generadas no puede ser negativo.")
	Integer numeroFacturasGeneradas)
{
}

