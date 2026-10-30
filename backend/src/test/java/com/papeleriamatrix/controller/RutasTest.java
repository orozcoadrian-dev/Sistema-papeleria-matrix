package com.papeleriamatrix.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
 * Prueba lo que pasa al abrir la raíz o una ruta que no existe, como hace un navegador.
 */
@SpringBootTest
@ActiveProfiles("demo")
class RutasTest
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
	void laRaizLlevaALaDocumentacion() throws Exception
	{
		mvc.perform(get("/"))
			.andExpect(status().is3xxRedirection())
			.andExpect(header().string("Location", "/swagger-ui.html"));
	}

	@Test
	void unaRutaQueNoExisteResponde404EnVezDeError() throws Exception
	{
		mvc.perform(get("/ruta-que-no-existe"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.title").value("Ruta no encontrada"));
	}

	@Test
	void elIconoDelNavegadorNoGeneraUnErrorDelServidor() throws Exception
	{
		mvc.perform(get("/favicon.ico")).andExpect(status().isNotFound());
	}

	@Test
	void unMetodoQueLaRutaNoAdmiteResponde405() throws Exception
	{
		mvc.perform(post("/api/estado"))
			.andExpect(status().isMethodNotAllowed())
			.andExpect(jsonPath("$.title").value("Método no permitido"));
	}
}
