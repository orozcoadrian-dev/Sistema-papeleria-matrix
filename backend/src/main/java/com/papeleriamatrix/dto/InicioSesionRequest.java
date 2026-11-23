package com.papeleriamatrix.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InicioSesionRequest(
	@NotBlank(message = "Escribe tu nombre de usuario.")
	@Size(max = 20, message = "El nombre de usuario es demasiado largo.")
	String nombreUsuario,

	@NotBlank(message = "Escribe tu contraseña.")
	@Size(max = 255, message = "La contraseña es demasiado larga.")
	String contrasenia)
{
}
