package com.papeleriamatrix.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.papeleriamatrix.security.ClaveApiFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CorsFilter;

/**
 * Prueba que el frontend local pueda consultar la API desde el navegador y que otros sitios no.
 */
@SpringBootTest
@ActiveProfiles("demo")
class CorsConfigTest
{
	private static final String FRONTEND = "http://localhost:5500";

	@Autowired
	private WebApplicationContext contexto;

	@Autowired
	private FilterRegistrationBean<CorsFilter> filtroCors;

	@Autowired
	private ClaveApiFilter filtroClave;

	private MockMvc mvc;

	@BeforeEach
	void preparar()
	{
		mvc = MockMvcBuilders.webAppContextSetup(contexto).addFilters(filtroCors.getFilter(), filtroClave).build();
	}

	@Test
	void permiteConsultarDesdeElFrontendLocal() throws Exception
	{
		mvc.perform(get("/api/servicios").header("Origin", FRONTEND))
			.andExpect(status().isOk())
			.andExpect(header().string("Access-Control-Allow-Origin", FRONTEND));
	}

	@Test
	void respondeLaConsultaPreviaDelNavegador() throws Exception
	{
		mvc.perform(options("/api/servicios")
				.header("Origin", FRONTEND)
				.header("Access-Control-Request-Method", "POST")
				.header("Access-Control-Request-Headers", "content-type,x-api-key"))
			.andExpect(status().isOk())
			.andExpect(header().string("Access-Control-Allow-Origin", FRONTEND))
			.andExpect(header().string("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS"));
	}

	@Test
	void rechazaOtrosSitios() throws Exception
	{
		mvc.perform(get("/api/servicios").header("Origin", "http://sitio-ajeno.example"))
			.andExpect(status().isForbidden());
	}

	@Test
	void elErrorDeLaClaveTambienLlegaAlNavegador() throws Exception
	{
		mvc.perform(post("/api/servicios").header("Origin", FRONTEND).contentType(MediaType.APPLICATION_JSON).content("{}"))
			.andExpect(status().isUnauthorized())
			.andExpect(header().string("Access-Control-Allow-Origin", FRONTEND));
	}
}
