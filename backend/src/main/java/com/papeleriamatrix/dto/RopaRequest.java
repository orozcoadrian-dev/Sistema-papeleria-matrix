package com.papeleriamatrix.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record RopaRequest(
	@NotBlank(message = "El código es obligatorio.")
	@Size(max = 20, message = "El código no puede superar los 20 caracteres.")
	String codigo,

	@NotBlank(message = "El tipo de ropa es obligatorio.")
	@Size(max = 20, message = "El tipo de ropa no puede superar los 20 caracteres.")
	String tipoRopa,

	@NotBlank(message = "El material es obligatorio.")
	@Size(max = 30, message = "El material no puede superar los 30 caracteres.")
	String material,

	@NotBlank(message = "La talla es obligatoria.")
	@Size(max = 5, message = "La talla no puede superar los 5 caracteres.")
	String talla,

	@Size(max = 150, message = "La descripción no puede superar los 150 caracteres.")
	String descripcion,

	@Size(max = 30, message = "El nombre no puede superar los 30 caracteres.")
	String nombre,

	@NotNull(message = "El precio es obligatorio.")
	@Positive(message = "El precio debe ser mayor que cero.")
	Double precio,

	@Size(max = 40, message = "El color no puede superar los 40 caracteres.")
	String color)
{
}

