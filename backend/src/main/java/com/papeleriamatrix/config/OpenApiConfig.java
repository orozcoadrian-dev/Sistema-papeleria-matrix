package com.papeleriamatrix.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
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
		return new OpenAPI().info(new Info()
			.title("API de Papelería Matrix")
			.description("Servicios REST del sistema de gestión de Papelería Matrix.")
			.version("0.1.0"));
	}
}
