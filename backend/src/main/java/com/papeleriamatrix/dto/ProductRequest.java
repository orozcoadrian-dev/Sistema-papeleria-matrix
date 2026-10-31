package com.papeleriamatrix.dto;
import jakarta.validation.constraints.*;
import java.util.Date;

public record ProductRequest (
	@NotBlank (message = "El código tiene un formato inválido.") @Size (max = 20)
	String codigo,

	@NotBlank (message = "El nombre del producto tiene un formato inválido.") @Size (max = 30)
	String nombre,

	@NotBlank (message = "La cantidad no puede ser nula.")
	int cantidad,

	@NotBlank (message = "El precio de venta no puede ser nulo.")
	double precio_de_venta,

	@NotBlank (message = "El precio de compra no puede ser nulo.")
	double precio_de_compra,

	@Size (max = 150)
	String descripcion,

	@Size (max = 40) 
	String lugar_de_compra,

	@NotBlank (message = "La fecha de publicación no puede ser nula.")
	Date Fecha_publicacion





) {}
