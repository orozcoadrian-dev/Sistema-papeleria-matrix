package com.papeleriamatrix.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record FacturaDelNegocioRequest(
	@NotBlank(message = "El código de la factura es obligatorio.")
	@Size(min = 15, max = 15, message = "El código de la factura debe tener exactamente 15 caracteres.")
	String codigoFactura,

	@NotBlank(message = "El tipo de factura es obligatorio.")
	@Size(min = 8, max = 15, message = "El tipo de factura debe tener entre 8 y 15 caracteres.")
	String tipoFactura,

	@NotNull(message = "El ID del usuario es obligatorio.")
	@Positive(message = "El ID del usuario debe ser mayor que cero.")
	Integer usuarioId,

	@NotNull(message = "El monto es obligatorio.")
	@Positive(message = "El monto debe ser mayor que cero.")
	Double monto)
{
}

