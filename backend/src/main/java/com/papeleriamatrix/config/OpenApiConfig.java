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
				.description("Servicios REST del sistema de gestión de Papelería Matrix. Consultar servicios, productos, ropa y la información del negocio es público. "
					+ "Lo demás exige entrar: con la sesión de POST /api/sesion (cabecera Authorization: Bearer) o con la clave de la API en la cabecera X-API-Key, que da acceso de administrador. "
					+ "El operador de cibercafé solo puede registrar y consultar ventas y ver las existencias. En Swagger UI se ingresa con el botón Authorize.")
				.version("0.3.0"))
			.components(new Components()
				.addSecuritySchemes("claveApi", new SecurityScheme()
					.type(SecurityScheme.Type.APIKEY)
					.in(SecurityScheme.In.HEADER)
					.name("X-API-Key")
					.description("Clave de la API. Da acceso de administrador."))
				.addSecuritySchemes("sesion", new SecurityScheme()
					.type(SecurityScheme.Type.HTTP)
					.scheme("bearer")
					.description("Código que devuelve POST /api/sesion.")));
	}
}
