package com.papeleriamatrix.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record IngresoDelNegocioRequest(
	@NotBlank(message = "El código de ingreso es obligatorio.")
	@Size(min = 10, max = 15, message = "El código de ingreso debe tener entre 10 y 15 caracteres.")
	String codigoIngreso,

	@PositiveOrZero(message = "El total de salario de operadores no puede ser negativo.")
	Double totalSalarioOperadores,

	@PositiveOrZero(message = "El total vendido de ropa no puede ser negativo.")
	Double totalVendidoRopa,

	@PositiveOrZero(message = "La inversión total de papelería no puede ser negativa.")
	Double inversionTotalPapeleria,

	@PositiveOrZero(message = "El total vendido de servicios no puede ser negativo.")
	Double totalVendidoServicios,

	@PositiveOrZero(message = "El total vendido de productos no puede ser negativo.")
	Double totalVendidoProductos,

	@PositiveOrZero(message = "El total de recaudo esperado no puede ser negativo.")
	Double totalRecaudoEsperadoPapeleria,

	@NotBlank(message = "El tipo de ingreso es obligatorio.")
	@Size(min = 10, max = 15, message = "El tipo de ingreso debe tener entre 10 y 15 caracteres.")
	String tipoIngreso)
{
}

