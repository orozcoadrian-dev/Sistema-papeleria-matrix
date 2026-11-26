package com.papeleriamatrix.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jayway.jsonpath.JsonPath;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

/**
 * Casos de la auditoría de inconsistencias: varias personas usando el sistema a la vez, formularios con datos
 * viejos, prendas únicas y precios que cambian mientras se atiende. Usa el servidor de verdad (puerto al azar)
 * y peticiones en paralelo, porque MockMvc no reproduce la concurrencia.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("demo")
class ConsistenciaTest
{
	private static final String CLAVE = "clave-de-prueba";

	@LocalServerPort
	private int puerto;

	private final HttpClient cliente = HttpClient.newHttpClient();

	private record Respuesta(int estado, String cuerpo)
	{
	}

	private Respuesta pedir(String metodo, String ruta, String cuerpo, String... cabeceras) throws Exception
	{
		HttpRequest.Builder peticion = HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + "/api" + ruta))
			.header("Content-Type", "application/json")
			.method(metodo, cuerpo == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(cuerpo));
		for (int i = 0; i < cabeceras.length; i += 2)
		{
			peticion.header(cabeceras[i], cabeceras[i + 1]);
		}
		HttpResponse<String> respuesta = cliente.send(peticion.build(), HttpResponse.BodyHandlers.ofString());
		return new Respuesta(respuesta.statusCode(), respuesta.body());
	}

	private String entrar(String usuario, String contrasenia) throws Exception
	{
		Respuesta r = pedir("POST", "/sesion", "{\"nombreUsuario\":\"%s\",\"contrasenia\":\"%s\"}".formatted(usuario, contrasenia));
		assertEquals(200, r.estado(), r.cuerpo());
		return "Bearer " + JsonPath.read(r.cuerpo(), "$.codigo");
	}

	private String operador() throws Exception
	{
		return entrar(SesionControllerTest.OPERADOR, SesionControllerTest.CONTRASENIA_OPERADOR);
	}

	private String administrador() throws Exception
	{
		return entrar(SesionControllerTest.ADMINISTRADOR, SesionControllerTest.CONTRASENIA_ADMINISTRADOR);
	}

	private int productoNuevo(int existencias) throws Exception
	{
		String codigo = "C" + (System.nanoTime() % 1_000_000_000L);
		Respuesta r = pedir("POST", "/productos", """
			{"codigo":"%s","nombre":"Prueba concurrencia","cantidad":%d,"precioVenta":1000,"precioCompra":500,"descripcion":"x","lugarCompra":"x"}"""
			.formatted(codigo, existencias), "X-API-Key", CLAVE);
		assertEquals(201, r.estado(), r.cuerpo());
		return JsonPath.read(r.cuerpo(), "$.id");
	}

	private int existencias(int producto) throws Exception
	{
		Respuesta r = pedir("GET", "/productos/interno", null, "X-API-Key", CLAVE);
		List<Integer> cantidades = JsonPath.read(r.cuerpo(), "$[?(@.id == " + producto + ")].cantidad");
		return cantidades.getFirst();
	}

	/** Corre todas las tareas al mismo tiempo (una barrera las suelta juntas) y devuelve los estados HTTP. */
	private List<Integer> aLaVez(List<Callable<Respuesta>> tareas) throws Exception
	{
		ExecutorService hilos = Executors.newFixedThreadPool(tareas.size());
		CyclicBarrier salida = new CyclicBarrier(tareas.size());
		try
		{
			List<Future<Respuesta>> futuros = new ArrayList<>();
			for (Callable<Respuesta> tarea : tareas)
			{
				futuros.add(hilos.submit(() -> {
					salida.await();
					return tarea.call();
				}));
			}
			List<Integer> estados = new ArrayList<>();
			for (Future<Respuesta> futuro : futuros)
			{
				estados.add(futuro.get().estado());
			}
			return estados;
		} finally
		{
			hilos.shutdown();
		}
	}

	@Test
	void diezVentasALaVezNoVendenMasDeLoQueHay() throws Exception
	{
		int producto = productoNuevo(5);
		String venta = "{\"articulos\":[{\"tipo\":\"producto\",\"id\":%d,\"cantidad\":1}]}".formatted(producto);
		List<Callable<Respuesta>> tareas = new ArrayList<>();
		for (int i = 0; i < 10; i++)
		{
			String sesion = operador();
			tareas.add(() -> pedir("POST", "/ventas", venta, "Authorization", sesion));
		}

		List<Integer> estados = aLaVez(tareas);

		assertEquals(5, estados.stream().filter(e -> e == 201).count(), "Vendidas: " + estados);
		assertEquals(5, estados.stream().filter(e -> e == 409).count(), "Rechazadas: " + estados);
		assertEquals(0, existencias(producto));
	}

	@Test
	void anularLaMismaVentaDosVecesALaVezDevuelveLasExistenciasUnaSolaVez() throws Exception
	{
		int producto = productoNuevo(10);
		Respuesta venta = pedir("POST", "/ventas", "{\"articulos\":[{\"tipo\":\"producto\",\"id\":%d,\"cantidad\":4}]}".formatted(producto),
			"Authorization", operador());
		int id = JsonPath.read(venta.cuerpo(), "$.id");
		String primera = administrador();
		String segunda = administrador();

		List<Integer> estados = aLaVez(List.of(
			() -> pedir("DELETE", "/ventas/" + id, null, "Authorization", primera),
			() -> pedir("DELETE", "/ventas/" + id, null, "Authorization", segunda)));

		assertTrue(estados.contains(204) && estados.contains(409), "Respuestas: " + estados);
		assertEquals(10, existencias(producto));
	}

	@Test
	void unaPrendaSeVendeUnaSolaVezYVuelveSiSeAnula() throws Exception
	{
		String codigo = "R" + (System.nanoTime() % 1_000_000_000L);
		Respuesta creada = pedir("POST", "/ropa", """
			{"codigo":"%s","nombre":"Prenda unica","tipoRopa":"Blusa","material":"Algodon","talla":"M","precio":15000,"color":"Azul","descripcion":"x"}"""
			.formatted(codigo), "X-API-Key", CLAVE);
		assertEquals(201, creada.estado(), creada.cuerpo());
		int prenda = JsonPath.read(creada.cuerpo(), "$.id");
		String venta = "{\"articulos\":[{\"tipo\":\"ropa\",\"id\":%d,\"cantidad\":1}]}".formatted(prenda);

		assertEquals(400, pedir("POST", "/ventas", "{\"articulos\":[{\"tipo\":\"ropa\",\"id\":%d,\"cantidad\":2}]}".formatted(prenda),
			"Authorization", operador()).estado());

		String a = operador();
		String b = operador();
		List<Integer> estados = aLaVez(List.of(
			() -> pedir("POST", "/ventas", venta, "Authorization", a),
			() -> pedir("POST", "/ventas", venta, "Authorization", b)));
		assertTrue(estados.contains(201) && estados.contains(409), "Respuestas: " + estados);

		Respuesta catalogo = pedir("GET", "/ropa/" + prenda, null);
		assertEquals(false, JsonPath.read(catalogo.cuerpo(), "$.disponible"));

		Respuesta vendidas = pedir("GET", "/ventas", null, "Authorization", administrador());
		List<Integer> ids = JsonPath.read(vendidas.cuerpo(), "$[?(@.articulos[0].tipo == 'ropa' && @.articulos[0].id == " + prenda + ")].id");
		assertEquals(204, pedir("DELETE", "/ventas/" + ids.getFirst(), null, "Authorization", administrador()).estado());
		assertEquals(true, JsonPath.read(pedir("GET", "/ropa/" + prenda, null).cuerpo(), "$.disponible"));
	}

	@Test
	void guardarUnProductoConUnidadesViejasNoBorraLasVentas() throws Exception
	{
		int producto = productoNuevo(20);
		pedir("POST", "/ventas", "{\"articulos\":[{\"tipo\":\"producto\",\"id\":%d,\"cantidad\":3}]}".formatted(producto), "Authorization", operador());
		String cambio = """
			{"codigo":"C-VIEJO-%d","nombre":"Prueba concurrencia","cantidad":20,"precioVenta":1200,"precioCompra":500,"descripcion":"x","lugarCompra":"x","cantidadAnterior":20}"""
			.formatted(producto);

		Respuesta r = pedir("PUT", "/productos/" + producto, cambio, "Authorization", administrador());

		assertEquals(409, r.estado(), r.cuerpo());
		assertTrue(r.cuerpo().contains("ahora quedan 17"), r.cuerpo());
		assertEquals(17, existencias(producto));
	}

	@Test
	void siCambioUnPrecioMientrasSeAtendiaNoSeCobraOtroTotal() throws Exception
	{
		int producto = productoNuevo(5);
		Respuesta r = pedir("POST", "/ventas", "{\"articulos\":[{\"tipo\":\"producto\",\"id\":%d,\"cantidad\":2}],\"totalEsperado\":1500}".formatted(producto),
			"Authorization", operador());

		assertEquals(409, r.estado(), r.cuerpo());
		assertTrue(r.cuerpo().contains("2.000"), r.cuerpo());
		assertEquals(5, existencias(producto));
	}
}
