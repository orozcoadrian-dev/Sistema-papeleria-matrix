package com.papeleriamatrix.controller;

import com.papeleriamatrix.dto.InicioSesionRequest;
import com.papeleriamatrix.dto.SesionResponse;
import com.papeleriamatrix.exception.OperacionRechazadaException;
import com.papeleriamatrix.security.AlmacenDeSesiones;
import com.papeleriamatrix.security.InicioDeSesionService;
import com.papeleriamatrix.security.Sesion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sesion")
@Tag(name = "Sesión", description = "Entrar y salir del sistema. La sesión se cierra sola tras 15 minutos sin uso.")
public class SesionController
{
	private final InicioDeSesionService inicioDeSesion;
	private final AlmacenDeSesiones sesiones;

	public SesionController(InicioDeSesionService inicioDeSesion, AlmacenDeSesiones sesiones)
	{
		this.inicioDeSesion = inicioDeSesion;
		this.sesiones = sesiones;
	}

	@PostMapping
	@Operation(summary = "Iniciar sesión", description = "Devuelve el código de la sesión. Se manda en la cabecera Authorization: Bearer <código>.")
	public SesionResponse entrar(@Valid @RequestBody InicioSesionRequest peticion)
	{
		return inicioDeSesion.entrar(peticion);
	}

	@GetMapping
	@Operation(summary = "Consultar la sesión actual", security = @SecurityRequirement(name = "sesion"))
	public SesionResponse actual(@Parameter(hidden = true) @RequestAttribute(name = Sesion.ATRIBUTO, required = false) Sesion sesion)
	{
		if (sesion == null || !sesion.tieneUsuario())
		{
			throw new OperacionRechazadaException(HttpStatus.UNAUTHORIZED, "No autorizado", "No hay una sesión abierta o ya se cerró por inactividad.");
		}
		return SesionResponse.desde(null, sesion, sesiones.minutosDeInactividad());
	}

	@DeleteMapping
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Cerrar sesión", security = @SecurityRequirement(name = "sesion"))
	public void salir(@Parameter(hidden = true) @RequestHeader(name = "Authorization", required = false) String cabecera)
	{
		if (cabecera != null && cabecera.regionMatches(true, 0, "Bearer ", 0, 7))
		{
			inicioDeSesion.salir(cabecera.substring(7).trim());
		}
	}
}
