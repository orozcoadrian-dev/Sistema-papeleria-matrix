package com.papeleriamatrix.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Información que aparece en la página de documentación de la API (Swagger UI).
 */
@Configuration
public class OpenApiConfig
{
	@Bean
	public OpenAPI informacionDeLaApi()
	{
		return new OpenAPI()
			.info(new Info()
				.title("API de Papelería Matrix")
				.description("Servicios REST del sistema de gestión de Papelería Matrix. Consultar es público. Crear, modificar y borrar exige la clave de la API en la cabecera X-API-Key: en Swagger UI se ingresa con el botón Authorize.")
				.version("0.2.0"))
			.components(new Components().addSecuritySchemes("claveApi", new SecurityScheme()
				.type(SecurityScheme.Type.APIKEY)
				.in(SecurityScheme.In.HEADER)
				.name("X-API-Key")
				.description("Clave para crear, modificar y borrar.")));
	}
}
