package com.papeleriamatrix.controller;

import com.papeleriamatrix.dto.EstadoRespuesta;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Estado", description = "Comprobación de que la API está en línea")
public class EstadoController
{
	private final Environment entorno;

	public EstadoController(Environment entorno)
	{
		this.entorno = entorno;
	}

	/**
	 * Informa que la API está en línea y en qué modo corre.
	 * @return nombre de la aplicación, estado y modo.
	 */
	@GetMapping("/estado")
	@Operation(summary = "Consultar el estado de la API", description = "Devuelve el nombre de la aplicación, su estado y el modo en que corre: local o demo.")
	public EstadoRespuesta consultarEstado()
	{
		return new EstadoRespuesta("Papelería Matrix", "en línea", obtenerModo());
	}

	private String obtenerModo()
	{
		String[] perfiles = entorno.getActiveProfiles();

		if(perfiles.length > 0)
		{
			return perfiles[0];
		}

		// Sin perfil activo corre el perfil por defecto, que es local.
		return "local";
	}
}
