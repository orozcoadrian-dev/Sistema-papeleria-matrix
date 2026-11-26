package com.papeleriamatrix.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.papeleriamatrix.security.ClaveApiFilter;
import java.util.List;
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
 * Prueba las ventas: el total, el descuento de existencias, los permisos y la anulación.
 * Cada prueba crea su propio producto para no cambiar las existencias del catálogo de ejemplo.
 */
@SpringBootTest
@ActiveProfiles("demo")
class VentaControllerTest
{
	private static final String CLAVE = "clave-de-prueba";

	@Autowired
	private WebApplicationContext contexto;

	@Autowired
	private ClaveApiFilter filtro;

	private MockMvc mvc;
	private String operador;
	private String administrador;

	@BeforeEach
	void preparar() throws Exception
	{
		mvc = MockMvcBuilders.webAppContextSetup(contexto).addFilters(filtro).build();
		operador = SesionControllerTest.entrar(mvc, SesionControllerTest.OPERADOR, SesionControllerTest.CONTRASENIA_OPERADOR);
		administrador = SesionControllerTest.entrar(mvc, SesionControllerTest.ADMINISTRADOR, SesionControllerTest.CONTRASENIA_ADMINISTRADOR);
	}

	private int productoNuevo(int existencias, double precio) throws Exception
	{
		String codigo = "V" + (System.nanoTime() % 1_000_000_000L);
		String cuerpo = """
			{"codigo":"%s","nombre":"Producto de prueba","cantidad":%d,"precioVenta":%s,"precioCompra":1,"descripcion":"Para probar ventas","lugarCompra":"Pruebas"}"""
			.formatted(codigo, existencias, precio);
		String respuesta = mvc.perform(post("/api/productos").header("X-API-Key", CLAVE).contentType(MediaType.APPLICATION_JSON).content(cuerpo))
			.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
		return JsonPath.read(respuesta, "$.id");
	}

	private int existencias(int idProducto) throws Exception
	{
		String respuesta = mvc.perform(get("/api/productos/interno").header("Authorization", administrador))
			.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		List<Integer> cantidades = JsonPath.read(respuesta, "$[?(@.id == " + idProducto + ")].cantidad");
		return cantidades.getFirst();
	}

	private static String venta(String articulos)
	{
		return "{\"articulos\":[" + articulos + "]}";
	}

	@Test
	void elOperadorVendeYSeDescuentanLasExistencias() throws Exception
	{
		int producto = productoNuevo(10, 1500);

		String respuesta = mvc.perform(post("/api/ventas").header("Authorization", operador).contentType(MediaType.APPLICATION_JSON)
				.content(venta("{\"tipo\":\"producto\",\"id\":%d,\"cantidad\":3},{\"tipo\":\"servicio\",\"id\":1,\"cantidad\":2}".formatted(producto))))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.total").value(3 * 1500 + 2 * 300))
			.andExpect(jsonPath("$.operador").value("Daniel Pérez"))
			.andExpect(jsonPath("$.articulos.length()").value(2))
			.andExpect(jsonPath("$.codigo").isString())
			.andReturn().getResponse().getContentAsString();

		assertEquals(7, existencias(producto));
		String codigo = JsonPath.read(respuesta, "$.codigo");
		assertEquals(15, codigo.length());
	}

	@Test
	void siNoAlcanzanLasExistenciasNoSeVendeNada() throws Exception
	{
		int producto = productoNuevo(2, 1000);

		mvc.perform(post("/api/ventas").header("Authorization", operador).contentType(MediaType.APPLICATION_JSON)
				.content(venta("{\"tipo\":\"servicio\",\"id\":1,\"cantidad\":1},{\"tipo\":\"producto\",\"id\":%d,\"cantidad\":5}".formatted(producto))))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.detail").value("Solo quedan 2 unidades de «Producto de prueba»."));

		assertEquals(2, existencias(producto));
	}

	@Test
	void elMismoArticuloRepetidoSeSumaEnUnaLinea() throws Exception
	{
		int producto = productoNuevo(10, 500);

		mvc.perform(post("/api/ventas").header("Authorization", operador).contentType(MediaType.APPLICATION_JSON)
				.content(venta("{\"tipo\":\"producto\",\"id\":%1$d,\"cantidad\":1},{\"tipo\":\"producto\",\"id\":%1$d,\"cantidad\":2}".formatted(producto))))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.articulos.length()").value(1))
			.andExpect(jsonPath("$.articulos[0].cantidad").value(3));
	}

	@Test
	void unaVentaVaciaOConDatosMalosSeRechaza() throws Exception
	{
		mvc.perform(post("/api/ventas").header("Authorization", operador).contentType(MediaType.APPLICATION_JSON).content(venta("")))
			.andExpect(status().isBadRequest());
		mvc.perform(post("/api/ventas").header("Authorization", operador).contentType(MediaType.APPLICATION_JSON)
				.content(venta("{\"tipo\":\"comida\",\"id\":1,\"cantidad\":1}")))
			.andExpect(status().isBadRequest());
		mvc.perform(post("/api/ventas").header("Authorization", operador).contentType(MediaType.APPLICATION_JSON)
				.content(venta("{\"tipo\":\"producto\",\"id\":999999,\"cantidad\":1}")))
			.andExpect(status().isNotFound());
	}

	@Test
	void elAdministradorVendeANombreDeUnOperador() throws Exception
	{
		mvc.perform(post("/api/ventas").header("Authorization", administrador).contentType(MediaType.APPLICATION_JSON)
				.content(venta("{\"tipo\":\"ropa\",\"id\":1,\"cantidad\":1}")))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.title").value("Falta el operador"));

		mvc.perform(post("/api/ventas").header("Authorization", administrador).contentType(MediaType.APPLICATION_JSON)
				.content("{\"articulos\":[{\"tipo\":\"ropa\",\"id\":1,\"cantidad\":1}],\"operadorId\":2}"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.operadorId").value(2))
			.andExpect(jsonPath("$.total").value(15000));
	}

	@Test
	void sinClaveElOperadorNoAnulaYElAdministradorSi() throws Exception
	{
		int producto = productoNuevo(5, 2000);
		String respuesta = mvc.perform(post("/api/ventas").header("Authorization", operador).contentType(MediaType.APPLICATION_JSON)
				.content(venta("{\"tipo\":\"producto\",\"id\":%d,\"cantidad\":4}".formatted(producto))))
			.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
		int venta = JsonPath.read(respuesta, "$.id");
		assertEquals(1, existencias(producto));

		mvc.perform(delete("/api/ventas/" + venta).header("Authorization", operador)).andExpect(status().isForbidden());
		mvc.perform(delete("/api/ventas/" + venta).header("Authorization", administrador)).andExpect(status().isNoContent());

		assertEquals(5, existencias(producto));
		mvc.perform(get("/api/ventas/" + venta).header("Authorization", administrador))
			.andExpect(status().isOk()).andExpect(jsonPath("$.anulada").value(true));
	}

	private double ingresoDeHoy(String campo) throws Exception
	{
		String hoy = java.time.LocalDate.now().toString();
		String respuesta = mvc.perform(get("/api/ingresos/resumen").param("desde", hoy).param("hasta", hoy).header("Authorization", administrador))
			.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		return ((Number) JsonPath.read(respuesta, "$." + campo)).doubleValue();
	}

	@Test
	void lasVentasDeLaCajaEntranEnElResumenDeIngresos() throws Exception
	{
		int producto = productoNuevo(10, 700);
		double serviciosAntes = ingresoDeHoy("totalVendidoServicios");
		double productosAntes = ingresoDeHoy("totalVendidoProductos");

		mvc.perform(post("/api/ventas").header("Authorization", operador).contentType(MediaType.APPLICATION_JSON)
				.content(venta("{\"tipo\":\"producto\",\"id\":%d,\"cantidad\":2},{\"tipo\":\"servicio\",\"id\":1,\"cantidad\":3}".formatted(producto))))
			.andExpect(status().isCreated());

		assertEquals(serviciosAntes + 3 * 300, ingresoDeHoy("totalVendidoServicios"), 0.001);
		assertEquals(productosAntes + 2 * 700, ingresoDeHoy("totalVendidoProductos"), 0.001);
	}

	@Test
	void elOperadorSoloVeSusVentasYElAdministradorTodas() throws Exception
	{
		mvc.perform(get("/api/ventas").header("Authorization", operador))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[*].operadorId", org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is(2))));

		mvc.perform(get("/api/ventas").header("Authorization", administrador))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()", org.hamcrest.Matchers.greaterThanOrEqualTo(2)));

		mvc.perform(get("/api/ventas")).andExpect(status().isUnauthorized());
	}
}
