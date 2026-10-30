package com.papeleriamatrix.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Prueba los catálogos públicos de papelería y de ropa, en modo demo.
 */
@SpringBootTest
@ActiveProfiles("demo")
class CatalogoControllerTest
{
	@Autowired
	private WebApplicationContext contexto;

	private MockMvc mvc;

	@BeforeEach
	void preparar()
	{
		mvc = MockMvcBuilders.webAppContextSetup(contexto).build();
	}

	@Test
	void losProductosNoMuestranLoQueEsInternoDelNegocio() throws Exception
	{
		mvc.perform(get("/api/productos"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].precio").exists())
			.andExpect(jsonPath("$[0].disponible").value(true))
			.andExpect(jsonPath("$[0].precioCompra").doesNotExist())
			.andExpect(jsonPath("$[0].lugarCompra").doesNotExist())
			.andExpect(jsonPath("$[0].cantidad").doesNotExist());
	}

	@Test
	void consultaUnProductoPorSuId() throws Exception
	{
		mvc.perform(get("/api/productos/1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.codigo").value("PAP-001"))
			.andExpect(jsonPath("$.precio").value(2000.0));
	}

	@Test
	void listaLaRopaDeEjemplo() throws Exception
	{
		mvc.perform(get("/api/ropa"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(4))
			.andExpect(jsonPath("$[0].tipo").value("Blusa"))
			.andExpect(jsonPath("$[0].talla").value("M"));
	}

	@Test
	void respondeNoEncontradoParaProductosYRopaQueNoExisten() throws Exception
	{
		mvc.perform(get("/api/productos/999999")).andExpect(status().isNotFound());
		mvc.perform(get("/api/ropa/999999")).andExpect(status().isNotFound());
	}

	@Test
	void respondeErrorClaroParaUnIdQueNoEsNumero() throws Exception
	{
		mvc.perform(get("/api/productos/abc"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.title").value("Parámetro inválido"));
	}
}
