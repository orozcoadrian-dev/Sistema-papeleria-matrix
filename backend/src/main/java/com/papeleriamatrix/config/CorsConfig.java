package com.papeleriamatrix.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Permite que el frontend, que corre en otra dirección, consulte la API desde el navegador.
 * Solo se aceptan los orígenes de la lista app.cors.origenes. El filtro va primero para que
 * hasta las respuestas de error, como el 401 de la clave, lleguen al navegador con sus permisos.
 */
@Configuration
public class CorsConfig
{
	@Bean
	public FilterRegistrationBean<CorsFilter> filtroCors(@Value("${app.cors.origenes}") List<String> origenes)
	{
		CorsConfiguration configuracion = new CorsConfiguration();
		configuracion.setAllowedOrigins(origenes);
		configuracion.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuracion.setAllowedHeaders(List.of("Content-Type", "X-API-Key"));
		configuracion.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
		fuente.registerCorsConfiguration("/api/**", configuracion);

		FilterRegistrationBean<CorsFilter> registro = new FilterRegistrationBean<>(new CorsFilter(fuente));
		registro.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return registro;
	}
}
