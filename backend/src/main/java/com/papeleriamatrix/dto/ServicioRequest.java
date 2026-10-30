package com.papeleriamatrix.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Datos que se envían para crear o actualizar un servicio.
 */
public record ServicioRequest(
	@NotBlank(message = "El código es obligatorio.")
	@Size(max = 20, message = "El código no puede superar los 20 caracteres.")
	String codigo,

	@NotBlank(message = "El nombre es obligatorio.")
	@Size(max = 30, message = "El nombre no puede superar los 30 caracteres.")
	String nombre,

	@NotBlank(message = "La categoría es obligatoria.")
	@Size(max = 30, message = "La categoría no puede superar los 30 caracteres.")
	String categoria,

	@NotNull(message = "El precio es obligatorio.")
	@Positive(message = "El precio debe ser mayor que cero.")
	Double precio,

	@Size(max = 150, message = "La descripción no puede superar los 150 caracteres.")
	String descripcion)
{
}
