package com.papeleriamatrix.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

@SpringBootTest
@ActiveProfiles("demo")
class ProductoControllerTest
{
	private static final String CLAVE = "clave-de-prueba";

	private static final String JSON_VALIDO = """
		{
		  "codigo": "TEST-001",
		  "nombre": "Cuaderno de prueba",
		  "cantidad": 10,
		  "precioVenta": 5000,
		  "precioCompra": 3000,
		  "descripcion": "Producto de prueba",
		  "lugarCompra": "Proveedor X"
		}
		""";

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

	@Test
	void listaProductos() throws Exception
	{
		mvc.perform(get("/api/productos"))
			.andExpect(status().isOk());
	}

	@Test
	void creaConsultaActualizaYEliminaUnProducto() throws Exception
	{
		MvcResult creado = mvc.perform(post("/api/productos")
				.header("X-API-Key", CLAVE)
				.contentType(MediaType.APPLICATION_JSON)
				.content(JSON_VALIDO))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.codigo").value("TEST-001"))
			.andReturn();

		Integer id = JsonPath.read(creado.getResponse().getContentAsString(), "$.id");

		mvc.perform(get("/api/productos/" + id))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.nombre").value("Cuaderno de prueba"));

		String actualizado = JSON_VALIDO.replace("Cuaderno de prueba", "Cuaderno editado");
		mvc.perform(put("/api/productos/" + id)
				.header("X-API-Key", CLAVE)
				.contentType(MediaType.APPLICATION_JSON)
				.content(actualizado))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.nombre").value("Cuaderno editado"));

		mvc.perform(delete("/api/productos/" + id).header("X-API-Key", CLAVE))
			.andExpect(status().isNoContent());

		mvc.perform(get("/api/productos/" + id))
			.andExpect(status().isNotFound());
	}

	@Test
	void rechazaCrearSinClave() throws Exception
	{
		mvc.perform(post("/api/productos")
				.contentType(MediaType.APPLICATION_JSON)
				.content(JSON_VALIDO))
			.andExpect(status().isUnauthorized());
	}

		private Integer crearProducto(String json) throws Exception
	{
		MvcResult creado = mvc.perform(post("/api/productos")
				.header("X-API-Key", CLAVE)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isCreated())
			.andReturn();

		return JsonPath.read(creado.getResponse().getContentAsString(), "$.id");
	}

	@Test
	void rechazaCodigoRepetido() throws Exception
	{
		String json = JSON_VALIDO.replace("TEST-001", "TEST-002");
		Integer id = crearProducto(json);

		mvc.perform(post("/api/productos")
				.header("X-API-Key", CLAVE)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isConflict());

		mvc.perform(delete("/api/productos/" + id).header("X-API-Key", CLAVE))
			.andExpect(status().isNoContent());
	}

	@Test
	void rechazaDatosInvalidos() throws Exception
	{
		String sinNombre = JSON_VALIDO.replace("\"nombre\": \"Cuaderno de prueba\"", "\"nombre\": \"\"");
		mvc.perform(post("/api/productos")
				.header("X-API-Key", CLAVE)
				.contentType(MediaType.APPLICATION_JSON)
				.content(sinNombre))
			.andExpect(status().isBadRequest());

		String precioNegativo = JSON_VALIDO.replace("\"precioVenta\": 5000", "\"precioVenta\": -5");
		mvc.perform(post("/api/productos")
				.header("X-API-Key", CLAVE)
				.contentType(MediaType.APPLICATION_JSON)
				.content(precioNegativo))
			.andExpect(status().isBadRequest());
	}

	@Test
	void actualizarYEliminarInexistenteDan404() throws Exception
	{
		mvc.perform(put("/api/productos/999999")
				.header("X-API-Key", CLAVE)
				.contentType(MediaType.APPLICATION_JSON)
				.content(JSON_VALIDO))
			.andExpect(status().isNotFound());

		mvc.perform(delete("/api/productos/999999").header("X-API-Key", CLAVE))
			.andExpect(status().isNotFound());
	}

	@Test
	void rechazaEditarYEliminarSinClave() throws Exception
	{
		mvc.perform(put("/api/productos/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(JSON_VALIDO))
			.andExpect(status().isUnauthorized());

		mvc.perform(delete("/api/productos/1"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void respuestaPublicaNoExponeDatosPrivados() throws Exception
	{
		Integer id = crearProducto(JSON_VALIDO.replace("TEST-001", "TEST-003"));

		mvc.perform(get("/api/productos/" + id))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.nombre").value("Cuaderno de prueba"))
			.andExpect(jsonPath("$.precioCompra").doesNotExist())
			.andExpect(jsonPath("$.lugarCompra").doesNotExist());

		mvc.perform(delete("/api/productos/" + id).header("X-API-Key", CLAVE))
			.andExpect(status().isNoContent());
	}
}