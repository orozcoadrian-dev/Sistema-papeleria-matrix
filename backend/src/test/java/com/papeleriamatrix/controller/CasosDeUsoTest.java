package com.papeleriamatrix.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.papeleriamatrix.security.ClaveApiFilter;
import com.papeleriamatrix.service.EnviadorDeCorreo;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Retirar sin borrar, estados de los operadores, destacados, existencias, ventas anuladas, reportes y correo.
 * Cada prueba crea lo que necesita para no cambiar el catálogo de ejemplo.
 */
@SpringBootTest
@ActiveProfiles("demo")
class CasosDeUsoTest
{
	private static final String CLAVE = "clave-de-prueba";

	@Autowired
	private WebApplicationContext contexto;

	@Autowired
	private ClaveApiFilter filtro;

	@Autowired
	private JdbcTemplate base;

	@MockitoBean
	private EnviadorDeCorreo correo;

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

	@Test
	void retirarUnProductoYaVendidoLoOcultaPeroNoLoBorra() throws Exception
	{
		int producto = productoNuevo(5);
		int venta = vender("producto", producto, 2);

		mvc.perform(delete("/api/productos/" + producto).param("motivo", "Ya no se consigue").header("Authorization", administrador))
			.andExpect(status().isNoContent());

		mvc.perform(get("/api/productos/" + producto)).andExpect(status().isNotFound());
		assertTrue(ids("/api/productos").stream().noneMatch(id -> id == producto));
		assertEquals("Ya no se consigue", base.queryForObject(
			"SELECT Motivo_retiro FROM producto_de_papeleria WHERE Id_producto = ? AND Activo = FALSE", String.class, producto));
		// La venta anterior sigue nombrándolo.
		mvc.perform(get("/api/ventas/" + venta).header("Authorization", administrador))
			.andExpect(jsonPath("$.articulos[0].nombre").value("Producto de prueba"));
		// Y ya no se puede vender.
		mvc.perform(conCuerpo(post("/api/ventas"), venta("producto", producto, 1)).header("Authorization", operador))
			.andExpect(status().isConflict()).andExpect(jsonPath("$.title").value("Artículo retirado"));
	}

	@Test
	void unCodigoRetiradoSeExplica() throws Exception
	{
		String codigo = codigoNuevo("S");
		String cuerpo = "{\"codigo\":\"%s\",\"nombre\":\"Plastificado\",\"categoria\":\"Copias\",\"precio\":2000}".formatted(codigo);
		int servicio = crear("/api/servicios", cuerpo);
		mvc.perform(delete("/api/servicios/" + servicio).header("Authorization", administrador)).andExpect(status().isNoContent());

		mvc.perform(conCuerpo(post("/api/servicios"), cuerpo).header("Authorization", administrador))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.detail").value("Hay un servicio retirado con el código " + codigo + ". Usa otro código."));
	}

	@Test
	void laInformacionRetiradaDejaDeMostrarseYQuedaGuardada() throws Exception
	{
		int administradora = base.queryForObject("SELECT MIN(Id_usuario) FROM administrador", Integer.class);
		int dato = crear("/api/informacion-negocio",
			"{\"usuarioId\":%d,\"valorInformacion\":9,\"tipoInformacion\":\"Medios de pago\",\"informacion\":\"Efectivo y transferencia\"}"
				.formatted(administradora));

		mvc.perform(delete("/api/informacion-negocio/" + dato).header("Authorization", administrador)).andExpect(status().isNoContent());

		assertTrue(ids("/api/informacion-negocio", "idInformacion").stream().noneMatch(id -> id == dato));
		mvc.perform(get("/api/informacion-negocio/" + dato)).andExpect(status().isNotFound());
		assertEquals(1, base.queryForObject("SELECT COUNT(*) FROM informacion_del_negocio WHERE Id_informacion = ?", Integer.class, dato));
	}

	@Test
	void destacarLoMuestraElCatalogoPublico() throws Exception
	{
		int producto = productoNuevo(3);

		mvc.perform(conCuerpo(put("/api/productos/" + producto + "/destacado"), "{\"destacado\":true}").header("Authorization", operador))
			.andExpect(status().isForbidden());
		mvc.perform(conCuerpo(put("/api/productos/" + producto + "/destacado"), "{\"destacado\":true}").header("Authorization", administrador))
			.andExpect(status().isOk()).andExpect(jsonPath("$.destacado").value(true));

		mvc.perform(get("/api/productos/" + producto)).andExpect(jsonPath("$.destacado").value(true));
	}

	@Test
	void elOperadorAnotaLasExistenciasPeroNoCambiaElProducto() throws Exception
	{
		int producto = productoNuevo(7);
		String ruta = "/api/productos/" + producto + "/existencias";

		mvc.perform(conCuerpo(put(ruta), "{\"cantidad\":12,\"cantidadAnterior\":7}").header("Authorization", operador))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.cantidad").value(12))
			.andExpect(jsonPath("$.precioCompra").doesNotExist());
		// Con unas unidades viejas en pantalla no se guarda.
		mvc.perform(conCuerpo(put(ruta), "{\"cantidad\":3,\"cantidadAnterior\":7}").header("Authorization", operador))
			.andExpect(status().isConflict()).andExpect(jsonPath("$.title").value("Las unidades cambiaron"));
		mvc.perform(delete("/api/productos/" + producto).header("Authorization", operador)).andExpect(status().isForbidden());
	}

	@Test
	void anularDejaElRegistroYNoCuentaEnLasCuentas() throws Exception
	{
		int producto = productoNuevo(4);
		double antes = totalDeHoy();
		int venta = vender("producto", producto, 4);
		assertEquals(antes + 4000, totalDeHoy(), 0.001);

		mvc.perform(delete("/api/ventas/" + venta).header("Authorization", administrador)).andExpect(status().isNoContent());

		assertEquals(antes, totalDeHoy(), 0.001);
		assertTrue(ids("/api/ventas").stream().noneMatch(id -> id == venta));
		assertTrue(ids("/api/ventas?incluirAnuladas=true").stream().anyMatch(id -> id == venta));
		mvc.perform(delete("/api/ventas/" + venta).header("Authorization", administrador)).andExpect(status().isConflict());
		mvc.perform(conCuerpo(put("/api/ventas/" + venta), venta("producto", producto, 1)).header("Authorization", administrador))
			.andExpect(status().isConflict());
	}

	@Test
	void unOperadorSuspendidoNoEntraNiRecibeVentasYAlReactivarloVuelve() throws Exception
	{
		String usuario = "op" + (System.nanoTime() % 1_000_000_000L);
		int nuevo = crear("/api/operadores-cibercafe", """
			{"primerNombre":"Carlos","primerApellido":"Prueba","segundoApellido":"Lopez","numeroDocumento":"%015d","tipoDocumento":"CC",
			"nombreUsuario":"%s","contrasenia":"Contrasenia-Larga-1","rol":"Operador de cibercafé","salario":1000000,
			"direccion":"Calle de prueba 123","fechaContrato":"2026-01-15"}""".formatted(System.nanoTime() % 1_000_000_000_000L, usuario));
		String suSesion = SesionControllerTest.entrar(mvc, usuario, "Contrasenia-Larga-1");

		mvc.perform(conCuerpo(put("/api/operadores-cibercafe/" + nuevo + "/estado"), "{\"estado\":\"Suspendido\"}").header("Authorization", administrador))
			.andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("Suspendido"));

		mvc.perform(get("/api/ventas").header("Authorization", suSesion)).andExpect(status().isUnauthorized());
		mvc.perform(conCuerpo(post("/api/sesion"), "{\"nombreUsuario\":\"%s\",\"contrasenia\":\"Contrasenia-Larga-1\"}".formatted(usuario)))
			.andExpect(status().isForbidden()).andExpect(jsonPath("$.title").value("Usuario no disponible"));
		mvc.perform(conCuerpo(post("/api/ventas"), "{\"articulos\":[{\"tipo\":\"servicio\",\"id\":1,\"cantidad\":1}],\"operadorId\":%d}".formatted(nuevo))
			.header("Authorization", administrador)).andExpect(status().isBadRequest());

		// Retirarlo lo deja como despedido, sin borrarlo.
		mvc.perform(delete("/api/operadores-cibercafe/" + nuevo).header("Authorization", administrador)).andExpect(status().isNoContent());
		mvc.perform(get("/api/operadores-cibercafe/" + nuevo).header("Authorization", administrador))
			.andExpect(jsonPath("$.estado").value("Despedido"));

		mvc.perform(conCuerpo(put("/api/operadores-cibercafe/" + nuevo + "/estado"), "{\"estado\":\"Activo\"}").header("Authorization", administrador))
			.andExpect(status().isOk());
		SesionControllerTest.entrar(mvc, usuario, "Contrasenia-Larga-1");
		mvc.perform(conCuerpo(put("/api/operadores-cibercafe/" + nuevo + "/estado"), "{\"estado\":\"Dormido\"}").header("Authorization", administrador))
			.andExpect(status().isBadRequest());
	}

	@Test
	void losContadoresSonDeLaAdministradora() throws Exception
	{
		mvc.perform(get("/api/contadores").header("Authorization", operador)).andExpect(status().isForbidden());
		mvc.perform(get("/api/contadores")).andExpect(status().isUnauthorized());
		mvc.perform(get("/api/contadores").header("Authorization", administrador))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.serviciosOfrecidos").isNumber())
			.andExpect(jsonPath("$.operadoresActivos").isNumber());
	}

	@Test
	void unReporteSeGuardaYSeEliminaAntesQueSuHistorial() throws Exception
	{
		String rango = "{\"desde\":\"2026-01-01\",\"hasta\":\"2026-01-31\"}";
		mvc.perform(conCuerpo(post("/api/ingresos/calcular"), rango).header("Authorization", administrador)).andExpect(status().is2xxSuccessful());
		int historial = ids("/api/historiales-ingresos-negocio", "idHistorialIngreso").stream().max(Integer::compare).orElseThrow();
		int administradora = base.queryForObject("SELECT MIN(Id_usuario) FROM administrador", Integer.class);
		int reporte = crear("/api/reportes", """
			{"codigoReporte":"%020d","historialIngresoDelNegocioId":%d,"usuarioId":%d,"rutaArchivo":"reporte-de-prueba.pdf","fechaGeneracion":"2026-02-01"}"""
			.formatted(System.nanoTime(), historial, administradora), "idReporte");

		mvc.perform(delete("/api/historiales-ingresos-negocio/" + historial).header("Authorization", administrador)).andExpect(status().isConflict());
		mvc.perform(delete("/api/reportes/" + reporte).header("Authorization", operador)).andExpect(status().isForbidden());
		mvc.perform(delete("/api/reportes/" + reporte).header("Authorization", administrador)).andExpect(status().isNoContent());
		mvc.perform(delete("/api/historiales-ingresos-negocio/" + historial).header("Authorization", administrador)).andExpect(status().is2xxSuccessful());
	}

	@Test
	void elCorreoSinConfigurarSeAvisa() throws Exception
	{
		when(correo.disponible()).thenReturn(false);
		int venta = vender("servicio", 1, 1);

		mvc.perform(conCuerpo(post("/api/ventas/" + venta + "/correo"), "{\"correo\":\"cliente@ejemplo.com\"}").header("Authorization", operador))
			.andExpect(status().isServiceUnavailable()).andExpect(jsonPath("$.title").value("Correo sin configurar"));
		mvc.perform(conCuerpo(post("/api/ventas/" + venta + "/correo"), "{\"correo\":\"sin-arroba\"}").header("Authorization", operador))
			.andExpect(status().isBadRequest());
	}

	@Test
	void elCorreoLlevaElDetalleDeLaVenta() throws Exception
	{
		when(correo.disponible()).thenReturn(true);
		int producto = productoNuevo(5);
		int venta = vender("producto", producto, 3);

		mvc.perform(conCuerpo(post("/api/ventas/" + venta + "/correo"), "{\"correo\":\"cliente@ejemplo.com\",\"nombre\":\"Lucía\"}")
			.header("Authorization", operador)).andExpect(status().isNoContent());

		ArgumentCaptor<String> texto = ArgumentCaptor.forClass(String.class);
		verify(correo).enviar(org.mockito.ArgumentMatchers.eq("cliente@ejemplo.com"), anyString(), texto.capture());
		assertTrue(texto.getValue().contains("Hola, Lucía:"));
		assertTrue(texto.getValue().contains("3 x Producto de prueba"));
		assertTrue(texto.getValue().contains("Total pagado: $ 3.000"));
		assertFalse(texto.getValue().contains("null"));
	}

	private static MockHttpServletRequestBuilder conCuerpo(MockHttpServletRequestBuilder peticion, String cuerpo)
	{
		return peticion.contentType(MediaType.APPLICATION_JSON).content(cuerpo);
	}

	private static String codigoNuevo(String letra)
	{
		return letra + (System.nanoTime() % 1_000_000_000L);
	}

	private int crear(String ruta, String cuerpo) throws Exception
	{
		return crear(ruta, cuerpo, null);
	}

	private int crear(String ruta, String cuerpo, String campo) throws Exception
	{
		String respuesta = mvc.perform(conCuerpo(post(ruta), cuerpo).header("X-API-Key", CLAVE))
			.andExpect(status().is2xxSuccessful()).andReturn().getResponse().getContentAsString();
		String nombre = campo != null ? campo : ruta.contains("operadores") ? "idUsuario" : ruta.contains("informacion") ? "idInformacion" : "id";
		return JsonPath.read(respuesta, "$." + nombre);
	}

	private int productoNuevo(int existencias) throws Exception
	{
		return crear("/api/productos", """
			{"codigo":"%s","nombre":"Producto de prueba","cantidad":%d,"precioVenta":1000,"precioCompra":1}"""
			.formatted(codigoNuevo("C"), existencias));
	}

	private static String venta(String tipo, int id, int cantidad)
	{
		return "{\"articulos\":[{\"tipo\":\"%s\",\"id\":%d,\"cantidad\":%d}]}".formatted(tipo, id, cantidad);
	}

	private int vender(String tipo, int id, int cantidad) throws Exception
	{
		String respuesta = mvc.perform(conCuerpo(post("/api/ventas"), venta(tipo, id, cantidad)).header("Authorization", operador))
			.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
		return JsonPath.read(respuesta, "$.id");
	}

	private List<Integer> ids(String ruta) throws Exception
	{
		return ids(ruta, "id");
	}

	private List<Integer> ids(String ruta, String campo) throws Exception
	{
		String respuesta = mvc.perform(get(ruta).header("Authorization", administrador))
			.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		return JsonPath.read(respuesta, "$[*]." + campo);
	}

	private double totalDeHoy() throws Exception
	{
		String hoy = java.time.LocalDate.now().toString();
		String respuesta = mvc.perform(get("/api/ingresos/resumen").param("desde", hoy).param("hasta", hoy).header("Authorization", administrador))
			.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		Number total = JsonPath.read(respuesta, "$.totalGeneral");
		return total.doubleValue();
	}
}
