package com.papeleriamatrix.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.papeleriamatrix.security.ClaveApiFilter;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Fotos de productos, ropa y servicios: quién puede subirlas, qué formatos se aceptan y que se borren con el producto.
 */
@SpringBootTest
@ActiveProfiles("demo")
class ImagenControllerTest
{
	private static final String CLAVE = "clave-de-prueba";

	/** Los primeros bytes de un PNG de verdad; para el servidor basta con eso. */
	private static final byte[] PNG = { (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 13, 'I', 'H', 'D', 'R' };

	@TempDir
	static Path carpeta;

	@DynamicPropertySource
	static void carpetaDeFotos(DynamicPropertyRegistry propiedades)
	{
		propiedades.add("app.imagenes.carpeta", () -> carpeta.toString());
	}

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

	private int productoNuevo() throws Exception
	{
		String codigo = "F" + (System.nanoTime() % 1_000_000_000L);
		String respuesta = mvc.perform(post("/api/productos").header("X-API-Key", CLAVE).contentType(MediaType.APPLICATION_JSON).content("""
				{"codigo":"%s","nombre":"Con foto","cantidad":3,"precioVenta":1000,"precioCompra":500}""".formatted(codigo)))
			.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
		return JsonPath.read(respuesta, "$.id");
	}

	@Test
	void laAdministradoraSubeUnaFotoYCualquieraLaVe() throws Exception
	{
		int id = productoNuevo();

		mvc.perform(put("/api/imagenes/productos/" + id).header("X-API-Key", CLAVE).contentType(MediaType.IMAGE_PNG).content(PNG))
			.andExpect(status().isNoContent());

		mvc.perform(get("/api/imagenes"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.productos['" + id + "']").isNumber());
		mvc.perform(get("/api/imagenes/productos/" + id))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.IMAGE_PNG))
			.andExpect(header().string("X-Content-Type-Options", "nosniff"))
			.andExpect(content().bytes(PNG));
	}

	@Test
	void sinSesionOComoOperadorNoSePuedeSubir() throws Exception
	{
		int id = productoNuevo();
		mvc.perform(put("/api/imagenes/productos/" + id).contentType(MediaType.IMAGE_PNG).content(PNG))
			.andExpect(status().isUnauthorized());
		String operador = SesionControllerTest.entrar(mvc, SesionControllerTest.OPERADOR, SesionControllerTest.CONTRASENIA_OPERADOR);
		mvc.perform(put("/api/imagenes/productos/" + id).header("Authorization", operador).contentType(MediaType.IMAGE_PNG).content(PNG))
			.andExpect(status().isForbidden());
	}

	@Test
	void soloAceptaFotosDeVerdad() throws Exception
	{
		int id = productoNuevo();
		// Dice ser PNG pero es texto: se revisa el contenido, no lo que dice.
		mvc.perform(put("/api/imagenes/productos/" + id).header("X-API-Key", CLAVE).contentType(MediaType.IMAGE_PNG).content("<script>".getBytes()))
			.andExpect(status().isUnsupportedMediaType())
			.andExpect(jsonPath("$.title").value("Formato no válido"));
		mvc.perform(put("/api/imagenes/productos/" + id).header("X-API-Key", CLAVE).contentType(MediaType.TEXT_HTML).content(PNG))
			.andExpect(status().isUnsupportedMediaType());

		byte[] pesada = new byte[2 * 1024 * 1024 + 1];
		System.arraycopy(PNG, 0, pesada, 0, PNG.length);
		mvc.perform(put("/api/imagenes/productos/" + id).header("X-API-Key", CLAVE).contentType(MediaType.IMAGE_PNG).content(pesada))
			.andExpect(status().isPayloadTooLarge());
	}

	@Test
	void tiposYElementosQueNoExistenDan404() throws Exception
	{
		mvc.perform(put("/api/imagenes/usuarios/1").header("X-API-Key", CLAVE).contentType(MediaType.IMAGE_PNG).content(PNG))
			.andExpect(status().isNotFound());
		mvc.perform(put("/api/imagenes/productos/999999").header("X-API-Key", CLAVE).contentType(MediaType.IMAGE_PNG).content(PNG))
			.andExpect(status().isNotFound());
		mvc.perform(get("/api/imagenes/ropa/999999")).andExpect(status().isNotFound());
	}

	@Test
	void quitarLaFotoYBorrarElProductoLaEliminan() throws Exception
	{
		int id = productoNuevo();
		mvc.perform(put("/api/imagenes/productos/" + id).header("X-API-Key", CLAVE).contentType(MediaType.IMAGE_PNG).content(PNG));
		mvc.perform(delete("/api/imagenes/productos/" + id).header("X-API-Key", CLAVE)).andExpect(status().isNoContent());
		mvc.perform(get("/api/imagenes/productos/" + id)).andExpect(status().isNotFound());

		mvc.perform(put("/api/imagenes/productos/" + id).header("X-API-Key", CLAVE).contentType(MediaType.IMAGE_PNG).content(PNG));
		mvc.perform(delete("/api/productos/" + id).header("X-API-Key", CLAVE)).andExpect(status().isNoContent());
		mvc.perform(get("/api/imagenes/productos/" + id)).andExpect(status().isNotFound());
	}
}
