package com.papeleriamatrix.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record DetalleDeFacturaRequest(
	@NotNull(message = "El ID de la factura es obligatorio.")
	@Positive(message = "El ID de la factura debe ser mayor que cero.")
	Integer facturaDelNegocioId,

	@NotBlank(message = "La descripción es obligatoria.")
	@Size(min = 12, max = 120, message = "La descripción debe tener entre 12 y 120 caracteres.")
	String descripcion,

	Integer servicioId,

	@Min(value = 0, message = "La cantidad de servicio no puede ser negativa.")
	Integer cantidadServicio,

	Integer productoDePapeleriaId,

	@Min(value = 0, message = "La cantidad de producto no puede ser negativa.")
	Integer cantidadProducto,

	Integer ropaId,

	@Min(value = 0, message = "La cantidad de ropa no puede ser negativa.")
	Integer cantidadRopa)
{
}

