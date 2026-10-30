package com.papeleriamatrix.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.papeleriamatrix.security.ClaveApiFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Prueba la API de servicios completa, con el filtro de la clave incluido y en modo demo.
 */
@SpringBootTest
@ActiveProfiles("demo")
class ServicioControllerTest
{
	private static final String CLAVE = "clave-de-prueba";

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

	private String codigoUnico()
	{
		return "T" + (System.nanoTime() % 1_000_000_000L);
	}

	private String cuerpo(String codigo, String nombre, double precio)
	{
		return """
			{"codigo":"%s","nombre":"%s","categoria":"Prueba","precio":%s,"descripcion":"Servicio de prueba"}""".formatted(codigo, nombre, precio);
	}

	private Integer crear(String codigo) throws Exception
	{
		MvcResult creado = mvc.perform(post("/api/servicios").header("X-API-Key", CLAVE).contentType(MediaType.APPLICATION_JSON).content(cuerpo(codigo, "Servicio nuevo", 1500)))
			.andExpect(status().isCreated())
			.andReturn();
		return JsonPath.read(creado.getResponse().getContentAsString(), "$.id");
	}

	@Test
	void listaLosServiciosDeEjemplo() throws Exception
	{
		mvc.perform(get("/api/servicios"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].codigo").value("SRV-001"))
			.andExpect(jsonPath("$[0].nombre").exists());
	}

	@Test
	void noPermiteCrearSinLaClave() throws Exception
	{
		mvc.perform(post("/api/servicios").contentType(MediaType.APPLICATION_JSON).content(cuerpo(codigoUnico(), "Sin clave", 100)))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.title").value("No autorizado"));
	}

	@Test
	void noPermiteCrearConUnaClaveIncorrecta() throws Exception
	{
		mvc.perform(post("/api/servicios").header("X-API-Key", "otra-clave").contentType(MediaType.APPLICATION_JSON).content(cuerpo(codigoUnico(), "Clave mala", 100)))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void creaConsultaActualizaYEliminaUnServicio() throws Exception
	{
		String codigo = codigoUnico();

		MvcResult creado = mvc.perform(post("/api/servicios").header("X-API-Key", CLAVE).contentType(MediaType.APPLICATION_JSON).content(cuerpo(codigo, "Servicio nuevo", 1500)))
			.andExpect(status().isCreated())
			.andExpect(header().exists("Location"))
			.andExpect(jsonPath("$.codigo").value(codigo))
			.andExpect(jsonPath("$.fechaVinculacion").exists())
			.andReturn();
		Integer id = JsonPath.read(creado.getResponse().getContentAsString(), "$.id");

		mvc.perform(get("/api/servicios/" + id))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.nombre").value("Servicio nuevo"));

		mvc.perform(put("/api/servicios/" + id).header("X-API-Key", CLAVE).contentType(MediaType.APPLICATION_JSON).content(cuerpo(codigo, "Servicio editado", 2000)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.nombre").value("Servicio editado"))
			.andExpect(jsonPath("$.precio").value(2000.0))
			.andExpect(jsonPath("$.fechaActualizacion").exists());

		mvc.perform(delete("/api/servicios/" + id).header("X-API-Key", CLAVE))
			.andExpect(status().isNoContent());

		mvc.perform(get("/api/servicios/" + id))
			.andExpect(status().isNotFound());
	}

	@Test
	void rechazaUnCodigoRepetido() throws Exception
	{
		String codigo = codigoUnico();
		Integer id = crear(codigo);

		mvc.perform(post("/api/servicios").header("X-API-Key", CLAVE).contentType(MediaType.APPLICATION_JSON).content(cuerpo(codigo, "Repetido", 500)))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.title").value("Código repetido"));

		mvc.perform(delete("/api/servicios/" + id).header("X-API-Key", CLAVE)).andExpect(status().isNoContent());
	}

	@Test
	void rechazaDatosInvalidosConMensajesClaros() throws Exception
	{
		mvc.perform(post("/api/servicios").header("X-API-Key", CLAVE).contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"codigo":"","nombre":"","categoria":"Prueba","precio":-5}"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errores.codigo").exists())
			.andExpect(jsonPath("$.errores.nombre").exists())
			.andExpect(jsonPath("$.errores.precio").value("El precio debe ser mayor que cero."));
	}

	@Test
	void rechazaUnCuerpoQueNoEsJson() throws Exception
	{
		mvc.perform(post("/api/servicios").header("X-API-Key", CLAVE).contentType(MediaType.APPLICATION_JSON).content("{"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.detail").value("El cuerpo de la petición no es un JSON válido."));
	}

	@Test
	void respondeNoEncontradoParaUnIdQueNoExiste() throws Exception
	{
		mvc.perform(get("/api/servicios/999999"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.detail").value("No existe un servicio con el id 999999."));
	}
}
