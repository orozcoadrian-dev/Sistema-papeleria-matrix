package com.papeleriamatrix.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record AdministradorRequest(
	@NotBlank(message = "El primer nombre es obligatorio.")
	@Size(min = 3, max = 30, message = "El primer nombre debe tener entre 3 y 30 caracteres.")
	String primerNombre,

	@Size(min = 3, max = 30, message = "El segundo nombre debe tener entre 3 y 30 caracteres.")
	String segundoNombre,

	@NotBlank(message = "El primer apellido es obligatorio.")
	@Size(min = 3, max = 30, message = "El primer apellido debe tener entre 3 y 30 caracteres.")
	String primerApellido,

	@NotBlank(message = "El segundo apellido es obligatorio.")
	@Size(min = 3, max = 30, message = "El segundo apellido debe tener entre 3 y 30 caracteres.")
	String segundoApellido,

	@NotBlank(message = "El número de documento es obligatorio.")
	@Size(min = 15, max = 20, message = "El número de documento debe tener entre 15 y 20 caracteres.")
	String numeroDocumento,

	@NotBlank(message = "El tipo de documento es obligatorio.")
	@Size(max = 5, message = "El tipo de documento no puede superar los 5 caracteres.")
	String tipoDocumento,

	@NotBlank(message = "El nombre de usuario es obligatorio.")
	@Size(min = 8, max = 15, message = "El nombre de usuario debe tener entre 8 y 15 caracteres.")
	String nombreUsuario,

	@NotBlank(message = "La contraseña es obligatoria.")
	@Size(min = 10, max = 255, message = "La contraseña debe tener entre 10 y 255 caracteres.")
	String contrasenia,

	@NotBlank(message = "El rol es obligatorio.")
	String rol,

	@Email(message = "El email no tiene un formato válido.")
	@Size(min = 20, max = 60, message = "El email debe tener entre 20 y 60 caracteres.")
	String email,

	@Size(min = 8, max = 15, message = "El teléfono debe tener entre 8 y 15 caracteres.")
	String telefono,

	@NotNull(message = "La fecha de posicionamiento es obligatoria.")
	LocalDate fechaPosicionamiento,

	@NotBlank(message = "El área de acceso es obligatoria.")
	@Size(min = 3, max = 15, message = "El área de acceso debe tener entre 3 y 15 caracteres.")
	String areaAcceso)
{
}

