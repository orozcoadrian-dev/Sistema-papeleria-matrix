package com.papeleriamatrix.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProductoPaperiaRequest(
	@NotBlank(message = "El código es obligatorio.")
	@Size(max = 20, message = "El código no puede superar los 20 caracteres.")
	String codigo,

	@NotBlank(message = "El nombre es obligatorio.")
	@Size(max = 30, message = "El nombre no puede superar los 30 caracteres.")
	String nombre,

	@NotNull(message = "La cantidad es obligatoria.")
	@PositiveOrZero(message = "La cantidad no puede ser negativa.")
	Integer cantidad,

	@NotNull(message = "El precio de venta es obligatorio.")
	@Positive(message = "El precio de venta debe ser mayor que cero.")
	Double precioVenta,

	@NotNull(message = "El precio de compra es obligatorio.")
	@Positive(message = "El precio de compra debe ser mayor que cero.")
	Double precioCompra,

	@Size(max = 150, message = "La descripción no puede superar los 150 caracteres.")
	String descripcion,

	@Size(max = 40, message = "El lugar de compra no puede superar los 40 caracteres.")
	String lugarCompra)
{
}

