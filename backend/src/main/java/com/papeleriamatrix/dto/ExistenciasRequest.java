package com.papeleriamatrix.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Cuántas unidades hay de un producto. cantidadAnterior son las que mostraba la pantalla: si mientras tanto
 * se vendió algo, el cambio no se guarda para no borrar esa venta de las existencias.
 */
public record ExistenciasRequest(
	@NotNull(message = "Escribe cuántas unidades hay.")
	@PositiveOrZero(message = "La cantidad no puede ser negativa.")
	@Max(value = 100000, message = "La cantidad no puede pasar de 100000.")
	Integer cantidad,
	@NotNull(message = "Falta la cantidad anterior.")
	@PositiveOrZero(message = "La cantidad anterior no puede ser negativa.")
	Integer cantidadAnterior)
{
}
