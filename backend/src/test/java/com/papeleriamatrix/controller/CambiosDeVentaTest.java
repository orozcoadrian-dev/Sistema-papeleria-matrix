package com.papeleriamatrix.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.papeleriamatrix.security.ClaveApiFilter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Cambiar y anular ventas: la clave de permiso del operador, las existencias y el total.
 * Cada prueba crea sus productos y borra la clave que haya puesto.
 */
@SpringBootTest
@ActiveProfiles("demo")
class CambiosDeVentaTest
{
	private static final String CLAVE_API = "clave-de-prueba";
	private static final String PERMISO = "Permiso-Cañón-2026";

	@Autowired
	private WebApplicationContext contexto;

	@Autowired
	private ClaveApiFilter filtro;

	private MockMvc mvc;
	private String operador;
	private String administrador;
	private int idOperador;

	@BeforeEach
	void preparar() throws Exception
	{
		mvc = MockMvcBuilders.webAppContextSetup(contexto).addFilters(filtro).build();
		operador = SesionControllerTest.entrar(mvc, SesionControllerTest.OPERADOR, SesionControllerTest.CONTRASENIA_OPERADOR);
		administrador = SesionControllerTest.entrar(mvc, SesionControllerTest.ADMINISTRADOR, SesionControllerTest.CONTRASENIA_ADMINISTRADOR);
		idOperador = JsonPath.read(mvc.perform(get("/api/sesion").header("Authorization", operador))
			.andReturn().getResponse().getContentAsString(), "$.idUsuario");
		borrarClave();
	}

	@AfterEach
	void borrarClave() throws Exception
	{
		String claves = mvc.perform(get("/api/claves-permiso").header("X-API-Key", CLAVE_API)).andReturn().getResponse().getContentAsString();
		List<Integer> ids = JsonPath.read(claves, "$[?(@.usuarioId == " + idOperador + ")].idClave");
		for (Integer id : ids)
		{
			mvc.perform(delete("/api/claves-permiso/" + id).header("X-API-Key", CLAVE_API)).andExpect(status().isNoContent());
		}
	}

	private void darClave() throws Exception
	{
		mvc.perform(post("/api/claves-permiso").header("Authorization", administrador).contentType(MediaType.APPLICATION_JSON)
				.content("{\"usuarioId\":%d,\"clave\":\"%s\"}".formatted(idOperador, PERMISO)))
			.andExpect(status().isCreated());
	}

	private static String codificada(String clave)
	{
		return URLEncoder.encode(clave, StandardCharsets.UTF_8);
	}

	private int productoNuevo(int existencias, double precio) throws Exception
	{
		String codigo = "C" + (System.nanoTime() % 1_000_000_000L);
		String cuerpo = """
			{"codigo":"%s","nombre":"Producto de cambios","cantidad":%d,"precioVenta":%s,"precioCompra":1,"descripcion":"Para probar cambios","lugarCompra":"Pruebas"}"""
			.formatted(codigo, existencias, precio);
		String respuesta = mvc.perform(post("/api/productos").header("X-API-Key", CLAVE_API).contentType(MediaType.APPLICATION_JSON).content(cuerpo))
			.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
		return JsonPath.read(respuesta, "$.id");
	}

	private int prendaNueva(double precio) throws Exception
	{
		String codigo = "P" + (System.nanoTime() % 1_000_000_000L);
		String cuerpo = """
			{"codigo":"%s","tipoRopa":"Blusa","material":"Algodón","talla":"M","nombre":"Prenda de cambios","precio":%s}""".formatted(codigo, precio);
		String respuesta = mvc.perform(post("/api/ropa").header("X-API-Key", CLAVE_API).contentType(MediaType.APPLICATION_JSON).content(cuerpo))
			.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
		return JsonPath.read(respuesta, "$.id");
	}

	private int existencias(int idProducto) throws Exception
	{
		String respuesta = mvc.perform(get("/api/productos/interno").header("Authorization", administrador))
			.andReturn().getResponse().getContentAsString();
		List<Integer> cantidades = JsonPath.read(respuesta, "$[?(@.id == " + idProducto + ")].cantidad");
		return cantidades.getFirst();
	}

	private static String venta(String articulos)
	{
		return "{\"articulos\":[" + articulos + "]}";
	}

	private static String linea(String tipo, int id, int cantidad)
	{
		return "{\"tipo\":\"%s\",\"id\":%d,\"cantidad\":%d}".formatted(tipo, id, cantidad);
	}

	private int vender(String articulos) throws Exception
	{
		String respuesta = mvc.perform(post("/api/ventas").header("Authorization", operador).contentType(MediaType.APPLICATION_JSON).content(venta(articulos)))
			.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
		return JsonPath.read(respuesta, "$.id");
	}

	@Test
	void sinClaveAsignadaElOperadorNoCambiaNiAnula() throws Exception
	{
		int producto = productoNuevo(10, 1000);
		int venta = vender(linea("producto", producto, 2));

		mvc.perform(put("/api/ventas/" + venta).header("Authorization", operador).header("X-Clave-Permiso", codificada(PERMISO))
				.contentType(MediaType.APPLICATION_JSON).content(venta(linea("producto", producto, 1))))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.title").value("Sin clave de permiso"));
		mvc.perform(delete("/api/ventas/" + venta).header("Authorization", operador)).andExpect(status().isForbidden());
		assertEquals(8, existencias(producto));
	}

	@Test
	void conSuClaveElOperadorCambiaLaVentaYLasExistencias() throws Exception
	{
		darClave();
		int primero = productoNuevo(10, 1000);
		int segundo = productoNuevo(5, 700);
		int venta = vender(linea("producto", primero, 4));

		mvc.perform(put("/api/ventas/" + venta).header("Authorization", operador).header("X-Clave-Permiso", codificada(PERMISO))
				.contentType(MediaType.APPLICATION_JSON).content(venta(linea("producto", primero, 1) + "," + linea("producto", segundo, 2))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(venta))
			.andExpect(jsonPath("$.total").value(1000 + 2 * 700))
			.andExpect(jsonPath("$.articulos.length()").value(2));

		assertEquals(9, existencias(primero));
		assertEquals(3, existencias(segundo));
	}

	@Test
	void unaClaveEquivocadaNoCambiaNadaNiCierraLaSesion() throws Exception
	{
		darClave();
		int producto = productoNuevo(10, 1000);
		int venta = vender(linea("producto", producto, 3));

		mvc.perform(delete("/api/ventas/" + venta).header("Authorization", operador).header("X-Clave-Permiso", "otra-clave-cualquiera"))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.title").value("Clave incorrecta"));

		assertEquals(7, existencias(producto));
		mvc.perform(get("/api/ventas/" + venta).header("Authorization", operador)).andExpect(status().isOk());
	}

	@Test
	void conSuClaveElOperadorAnula() throws Exception
	{
		darClave();
		int producto = productoNuevo(6, 500);
		int venta = vender(linea("producto", producto, 6));

		mvc.perform(delete("/api/ventas/" + venta).header("Authorization", operador).header("X-Clave-Permiso", codificada(PERMISO)))
			.andExpect(status().isNoContent());

		assertEquals(6, existencias(producto));
		mvc.perform(get("/api/ventas/" + venta).header("Authorization", administrador)).andExpect(status().isNotFound());
	}

	@Test
	void siElCambioNoSePuedeHacerLaVentaQuedaComoEstaba() throws Exception
	{
		int producto = productoNuevo(5, 1000);
		int venta = vender(linea("producto", producto, 2));

		mvc.perform(put("/api/ventas/" + venta).header("Authorization", administrador).contentType(MediaType.APPLICATION_JSON)
				.content(venta(linea("producto", producto, 6))))
			.andExpect(status().isConflict());

		assertEquals(3, existencias(producto));
		mvc.perform(get("/api/ventas/" + venta).header("Authorization", administrador))
			.andExpect(jsonPath("$.total").value(2000))
			.andExpect(jsonPath("$.articulos[0].cantidad").value(2));
	}

	@Test
	void alCambiarSePuedenUsarLasUnidadesQueYaTeniaLaVenta() throws Exception
	{
		int producto = productoNuevo(5, 1000);
		int venta = vender(linea("producto", producto, 4));

		mvc.perform(put("/api/ventas/" + venta).header("Authorization", administrador).contentType(MediaType.APPLICATION_JSON)
				.content(venta(linea("producto", producto, 5))))
			.andExpect(status().isOk());

		assertEquals(0, existencias(producto));
	}

	@Test
	void laRopaSigueEnLaVentaOVuelveAEstarDisponible() throws Exception
	{
		int prenda = prendaNueva(9000);
		int venta = vender(linea("ropa", prenda, 1));

		// Se puede conservar la misma prenda y sumar un servicio.
		mvc.perform(put("/api/ventas/" + venta).header("Authorization", administrador).contentType(MediaType.APPLICATION_JSON)
				.content(venta(linea("ropa", prenda, 1) + "," + linea("servicio", 1, 2))))
			.andExpect(status().isOk());
		mvc.perform(get("/api/ropa/" + prenda)).andExpect(jsonPath("$.disponible").value(false));

		// Si se quita de la venta, vuelve a la vitrina.
		mvc.perform(put("/api/ventas/" + venta).header("Authorization", administrador).contentType(MediaType.APPLICATION_JSON)
				.content(venta(linea("servicio", 1, 1))))
			.andExpect(status().isOk());
		mvc.perform(get("/api/ropa/" + prenda)).andExpect(jsonPath("$.disponible").value(true));
	}

	@Test
	void elTotalEsperadoDebeCoincidir() throws Exception
	{
		int producto = productoNuevo(5, 1000);
		int venta = vender(linea("producto", producto, 1));

		mvc.perform(put("/api/ventas/" + venta).header("Authorization", administrador).contentType(MediaType.APPLICATION_JSON)
				.content("{\"articulos\":[" + linea("producto", producto, 2) + "],\"totalEsperado\":1}"))
			.andExpect(status().isConflict());

		assertEquals(4, existencias(producto));
	}

	@Test
	@DirtiesContext
	void despuesDeCincoClavesMalasHayQueEsperar() throws Exception
	{
		darClave();
		int producto = productoNuevo(5, 1000);
		int venta = vender(linea("producto", producto, 1));

		for (int intento = 0; intento < 5; intento++)
		{
			mvc.perform(delete("/api/ventas/" + venta).header("Authorization", operador).header("X-Clave-Permiso", "clave-mala-" + intento))
				.andExpect(status().isForbidden());
		}
		// Ya ni la correcta se acepta hasta que pase el tiempo.
		mvc.perform(delete("/api/ventas/" + venta).header("Authorization", operador).header("X-Clave-Permiso", codificada(PERMISO)))
			.andExpect(status().isTooManyRequests());
	}
}
