package com.papeleriamatrix.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Prueba el inicio de sesión y los permisos de cada rol con los usuarios de prueba del modo demo.
 */
@SpringBootTest
@ActiveProfiles("demo")
class SesionControllerTest
{
	static final String ADMINISTRADOR = "admin.matrix";
	static final String CONTRASENIA_ADMINISTRADOR = "Matrix-Admin-2026";
	static final String OPERADOR = "operador.matrix";
	static final String CONTRASENIA_OPERADOR = "Matrix-Operador-2026";

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

	static String entrar(MockMvc mvc, String usuario, String contrasenia) throws Exception
	{
		String respuesta = mvc.perform(post("/api/sesion").contentType(MediaType.APPLICATION_JSON)
				.content("{\"nombreUsuario\":\"%s\",\"contrasenia\":\"%s\"}".formatted(usuario, contrasenia)))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();
		return "Bearer " + JsonPath.read(respuesta, "$.codigo");
	}

	@Test
	void elAdministradorEntraYVeSuSesion() throws Exception
	{
		String sesion = entrar(mvc, ADMINISTRADOR, CONTRASENIA_ADMINISTRADOR);

		mvc.perform(get("/api/sesion").header("Authorization", sesion))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.rol").value("ADMINISTRADOR"))
			.andExpect(jsonPath("$.nombreUsuario").value(ADMINISTRADOR))
			.andExpect(jsonPath("$.minutosInactividad").value(15));
	}

	@Test
	void unaContraseniaIncorrectaNoEntra() throws Exception
	{
		mvc.perform(post("/api/sesion").contentType(MediaType.APPLICATION_JSON)
				.content("{\"nombreUsuario\":\"admin.matrix\",\"contrasenia\":\"no-es-esta-1234\"}"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.title").value("No se pudo entrar"));
	}

	@Test
	void despuesDeCincoFallosElUsuarioQuedaBloqueado() throws Exception
	{
		String usuario = "nadie" + (System.nanoTime() % 100000);
		String cuerpo = "{\"nombreUsuario\":\"%s\",\"contrasenia\":\"incorrecta-123\"}".formatted(usuario);
		for (int intento = 0; intento < 5; intento++)
		{
			mvc.perform(post("/api/sesion").contentType(MediaType.APPLICATION_JSON).content(cuerpo)).andExpect(status().isUnauthorized());
		}
		mvc.perform(post("/api/sesion").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
			.andExpect(status().isTooManyRequests());
	}

	@Test
	void losUsuariosSoloLosVeElAdministradorYSinContrasenia() throws Exception
	{
		mvc.perform(get("/api/usuarios")).andExpect(status().isUnauthorized());

		mvc.perform(get("/api/usuarios").header("Authorization", entrar(mvc, OPERADOR, CONTRASENIA_OPERADOR)))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.title").value("Sin permiso"));

		mvc.perform(get("/api/usuarios").header("Authorization", entrar(mvc, ADMINISTRADOR, CONTRASENIA_ADMINISTRADOR)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].nombreUsuario").exists())
			.andExpect(jsonPath("$[0].contrasenia").doesNotExist());
	}

	@Test
	void alSalirLaSesionDejaDeServir() throws Exception
	{
		String sesion = entrar(mvc, OPERADOR, CONTRASENIA_OPERADOR);
		mvc.perform(delete("/api/sesion").header("Authorization", sesion)).andExpect(status().isNoContent());
		mvc.perform(get("/api/sesion").header("Authorization", sesion)).andExpect(status().isUnauthorized());
		mvc.perform(get("/api/ventas").header("Authorization", sesion)).andExpect(status().isUnauthorized());
	}

	@Test
	void elCatalogoSigueSiendoPublicoPeroLasExistenciasNo() throws Exception
	{
		mvc.perform(get("/api/productos")).andExpect(status().isOk());
		mvc.perform(get("/api/informacion-negocio")).andExpect(status().isOk());
		mvc.perform(get("/api/productos/interno")).andExpect(status().isUnauthorized());

		mvc.perform(get("/api/productos/interno").header("Authorization", entrar(mvc, OPERADOR, CONTRASENIA_OPERADOR)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].cantidad").isNumber())
			.andExpect(jsonPath("$[0].precioCompra").isNumber());
	}

	@Test
	void consultarLasFacturasYaNoEsPublico() throws Exception
	{
		mvc.perform(get("/api/facturas-negocio")).andExpect(status().isUnauthorized());
		mvc.perform(get("/api/facturas-negocio").header("X-API-Key", "clave-de-prueba"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].codigoFactura").exists());
	}
}
