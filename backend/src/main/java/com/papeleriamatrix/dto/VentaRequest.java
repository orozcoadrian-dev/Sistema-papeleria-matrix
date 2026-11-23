package com.papeleriamatrix.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Una venta: lo que se lleva el cliente. Cada artículo es un producto de papelería, una prenda de ropa o un servicio.
 * operadorId solo se usa cuando la venta la registra un administrador o se entra con la clave de la API;
 * si entra un operador, la venta queda a su nombre.
 */
public record VentaRequest(
	@NotEmpty(message = "Agrega al menos un artículo a la venta.")
	@Size(max = 50, message = "Una venta no puede tener más de 50 artículos distintos.")
	List<@Valid @NotNull(message = "Hay un artículo vacío en la venta.") Articulo> articulos,

	@Positive(message = "El operador debe ser un id mayor que cero.")
	Integer operadorId)
{
	public record Articulo(
		@NotNull(message = "Falta el tipo del artículo.")
		@Pattern(regexp = "producto|ropa|servicio", message = "El tipo debe ser producto, ropa o servicio.")
		String tipo,

		@NotNull(message = "Falta el id del artículo.")
		@Positive(message = "El id del artículo debe ser mayor que cero.")
		Integer id,

		@NotNull(message = "Falta la cantidad.")
		@Positive(message = "La cantidad debe ser mayor que cero.")
		@Max(value = 150, message = "La cantidad no puede pasar de 150.")
		Integer cantidad)
	{
	}
}
