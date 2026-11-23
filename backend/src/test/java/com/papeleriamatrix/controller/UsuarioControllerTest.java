package com.papeleriamatrix.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.papeleriamatrix.security.ClaveApiFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Prueba que el email y el teléfono son opcionales: varios usuarios pueden quedar sin ellos.
 */
@SpringBootTest
@ActiveProfiles("demo")
class UsuarioControllerTest
{
	@Autowired
	private WebApplicationContext contexto;

	@Autowired
	private ClaveApiFilter filtro;

	private MockMvc mvc;

	@BeforeEach
	void preparar()
	{
		mvc = MockMvcBuilders.webAppContextSetup(contexto).addFilters(filtro).build();
	}

	private String usuario(String documento, String nombreUsuario)
	{
		return """
			{"primerNombre":"Prueba","primerApellido":"Usuario","segundoApellido":"Demo","numeroDocumento":"%s","tipoDocumento":"CC",
			"nombreUsuario":"%s","contrasenia":"Contrasenia-de-prueba","rol":"Administrador"}""".formatted(documento, nombreUsuario);
	}

	@Test
	void dosUsuariosSinEmailNiTelefonoNoChocan() throws Exception
	{
		long sufijo = System.nanoTime() % 100000;
		mvc.perform(post("/api/usuarios").header("X-API-Key", "clave-de-prueba").contentType(MediaType.APPLICATION_JSON)
				.content(usuario("2000000000" + String.format("%05d", sufijo), "sin.datos" + (sufijo % 1000))))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.email").doesNotExist())
			.andExpect(jsonPath("$.contrasenia").doesNotExist());
		mvc.perform(post("/api/usuarios").header("X-API-Key", "clave-de-prueba").contentType(MediaType.APPLICATION_JSON)
				.content(usuario("3000000000" + String.format("%05d", sufijo), "sin.otros" + (sufijo % 1000))))
			.andExpect(status().isCreated());
	}
}
