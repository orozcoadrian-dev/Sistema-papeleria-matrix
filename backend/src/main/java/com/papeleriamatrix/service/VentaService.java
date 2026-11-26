package com.papeleriamatrix.service;

import com.papeleriamatrix.model.Operador_de_cibercafe;
import java.util.HashMap;
import com.papeleriamatrix.dto.VentaRequest;
import com.papeleriamatrix.dto.VentaResponse;
import com.papeleriamatrix.exception.OperacionRechazadaException;
import com.papeleriamatrix.exception.RecursoNoEncontradoException;
import com.papeleriamatrix.model.Detalle_de_factura;
import com.papeleriamatrix.model.Factura_del_negocio;
import com.papeleriamatrix.model.ProductoPapeleria;
import com.papeleriamatrix.model.Ropa;
import com.papeleriamatrix.model.Servicio;
import com.papeleriamatrix.model.Usuario;
import com.papeleriamatrix.repository.DetalleDeFacturaRepository;
import com.papeleriamatrix.repository.FacturaDelNegocioRepository;
import com.papeleriamatrix.repository.OperadorDeCibercafeRepository;
import com.papeleriamatrix.repository.ProductoPapeleriaRepository;
import com.papeleriamatrix.repository.RopaRepository;
import com.papeleriamatrix.repository.ServicioRepository;
import com.papeleriamatrix.repository.UsuarioRepository;
import com.papeleriamatrix.security.PermisoDeVentas;
import com.papeleriamatrix.security.Sesion;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registra las ventas. Una venta es una factura del negocio con un detalle por cada artículo.
 * Al vender un producto de papelería se descuentan sus existencias; si no alcanzan, la venta no se hace.
 * La ropa y los servicios no llevan existencias en la base de datos.
 * Una venta se puede cambiar o anular; el operador necesita para eso su clave de permiso.
 * Anular no la borra: queda marcada como anulada y deja de contar.
 */
@Service
public class VentaService
{
	public static final String TIPO_FACTURA = "Venta en tienda";
	private static final DateTimeFormatter FORMATO_CODIGO = DateTimeFormatter.ofPattern("yyMMddHHmmss");

	private final FacturaDelNegocioRepository facturas;
	private final DetalleDeFacturaRepository detalles;
	private final ProductoPapeleriaRepository productos;
	private final RopaRepository ropa;
	private final ServicioRepository servicios;
	private final UsuarioRepository usuarios;
	private final OperadorDeCibercafeRepository operadores;
	private final PermisoDeVentas permisos;
	private final SecureRandom azar = new SecureRandom();

	public VentaService(FacturaDelNegocioRepository facturas, DetalleDeFacturaRepository detalles, ProductoPapeleriaRepository productos,
		RopaRepository ropa, ServicioRepository servicios, UsuarioRepository usuarios, OperadorDeCibercafeRepository operadores,
		PermisoDeVentas permisos)
	{
		this.facturas = facturas;
		this.detalles = detalles;
		this.productos = productos;
		this.ropa = ropa;
		this.servicios = servicios;
		this.usuarios = usuarios;
		this.operadores = operadores;
		this.permisos = permisos;
	}

	@Transactional
	public VentaResponse registrar(VentaRequest peticion, Sesion sesion)
	{
		int operadorId = operadorDeLaVenta(peticion, sesion);
		// Siempre en el mismo orden (tipo e id) para que dos ventas que bloquean las mismas filas no se esperen en cruz.
		Map<String, Integer> cantidades = new TreeMap<>(juntarRepetidos(peticion.articulos()));

		LocalDateTime ahora = LocalDateTime.now();
		Armado armado = prepararLineas(cantidades, ahora, Map.of());
		revisarTotal(armado.total(), peticion.totalEsperado());
		List<Detalle_de_factura> lineas = armado.lineas();

		LocalDate hoy = ahora.toLocalDate();
		Factura_del_negocio factura = facturas.save(new Factura_del_negocio(codigoNuevo(), TIPO_FACTURA, operadorId, armado.total(), hoy, null));
		for (Detalle_de_factura linea : lineas)
		{
			linea.setFacturaDelNegocioId(factura.getIdFactura());
		}
		detalles.saveAll(lineas);
		return armar(List.of(factura)).getFirst();
	}

	@Transactional(readOnly = true)
	public List<VentaResponse> listar(Sesion sesion, LocalDate desde, LocalDate hasta, boolean incluirAnuladas)
	{
		LocalDate fin = hasta != null ? hasta : LocalDate.now();
		LocalDate inicio = desde != null ? desde : fin.minusDays(30);
		if (inicio.isAfter(fin))
		{
			throw new OperacionRechazadaException(HttpStatus.BAD_REQUEST, "Fechas no válidas", "La fecha de inicio no puede ser posterior a la fecha final.");
		}
		List<Factura_del_negocio> encontradas = sesion.esOperador()
			? facturas.findByUsuarioIdAndFechaGeneracionBetweenOrderByIdFacturaDesc(sesion.idUsuario(), inicio, fin)
			: facturas.findByFechaGeneracionBetweenOrderByIdFacturaDesc(inicio, fin);
		return armar(encontradas.stream().filter(factura -> incluirAnuladas || !factura.isAnulada()).toList());
	}

	@Transactional(readOnly = true)
	public VentaResponse obtener(Integer id, Sesion sesion)
	{
		Factura_del_negocio factura = buscar(id);
		if (sesion.esOperador() && factura.getUsuarioId() != sesion.idUsuario())
		{
			// Para el operador, las ventas de otros no existen.
			throw new RecursoNoEncontradoException("No existe una venta con el id " + id + ".");
		}
		return armar(List.of(factura)).getFirst();
	}

	/**
	 * Cambia lo que se llevó el cliente: devuelve las existencias de antes, descuenta las nuevas y vuelve a
	 * calcular el total con los precios de hoy. La factura conserva su número, su fecha y su operador.
	 * Si algo falla, la venta queda como estaba.
	 */
	@Transactional
	public VentaResponse modificar(Integer id, VentaRequest peticion, Sesion sesion, String clavePermiso)
	{
		Factura_del_negocio factura = paraCambiar(id, sesion, clavePermiso);
		if (!TIPO_FACTURA.equalsIgnoreCase(factura.getTipoFactura()))
		{
			throw new OperacionRechazadaException(HttpStatus.CONFLICT, "No es una venta de la caja",
				"Solo se pueden cambiar las ventas hechas en la caja.");
		}
		List<Detalle_de_factura> anteriores = detalles.findByFacturaDelNegocioId(factura.getIdFactura());
		Map<String, Integer> cantidades = new TreeMap<>(juntarRepetidos(peticion.articulos()));

		Map<String, Integer> previas = cantidadesDe(anteriores);
		bloquear(anteriores, cantidades.keySet());
		devolverExistencias(anteriores);
		detalles.deleteAll(anteriores);
		// Las prendas que tenía esta venta quedan libres antes de revisar si se pueden vender.
		detalles.flush();

		LocalDateTime ahora = LocalDateTime.now();
		LocalDateTime fechaDeLaVenta = anteriores.isEmpty() ? ahora : anteriores.getFirst().getFechaGeneracion();
		Armado armado = prepararLineas(cantidades, fechaDeLaVenta, previas);
		revisarTotal(armado.total(), peticion.totalEsperado());

		factura.setMonto(armado.total());
		factura.setFechaActualizacion(ahora.toLocalDate());
		for (Detalle_de_factura linea : armado.lineas())
		{
			linea.setFacturaDelNegocioId(factura.getIdFactura());
			linea.setFechaActualizacion(ahora);
		}
		detalles.saveAll(armado.lineas());
		return armar(List.of(factura)).getFirst();
	}

	/** Anula la venta: devuelve las existencias de los productos y la deja marcada como anulada. */
	@Transactional
	public void anular(Integer id, Sesion sesion, String clavePermiso)
	{
		Factura_del_negocio factura = paraCambiar(id, sesion, clavePermiso);
		List<Detalle_de_factura> suyas = detalles.findByFacturaDelNegocioId(factura.getIdFactura());
		devolverExistencias(suyas);
		factura.anular(LocalDate.now());
	}

	/** Busca la venta que se va a cambiar y comprueba que esta persona pueda hacerlo. */
	private Factura_del_negocio paraCambiar(Integer id, Sesion sesion, String clavePermiso)
	{
		// Bloqueada: si otra persona la está cambiando a la vez, esta espera.
		Factura_del_negocio factura = facturas.bloquear(id)
			.orElseThrow(() -> new RecursoNoEncontradoException("No existe una venta con el id " + id + "."));
		if (sesion.esOperador() && factura.getUsuarioId() != sesion.idUsuario())
		{
			throw new RecursoNoEncontradoException("No existe una venta con el id " + id + ".");
		}
		permisos.exigir(sesion, clavePermiso);
		if (factura.isAnulada())
		{
			throw new OperacionRechazadaException(HttpStatus.CONFLICT, "Venta anulada", "Esta venta ya se anuló.");
		}
		return factura;
	}

	/** Bloquea los artículos de antes y los nuevos en el mismo orden que usa una venta nueva. */
	private void bloquear(List<Detalle_de_factura> anteriores, Set<String> nuevos)
	{
		Set<String> todos = new TreeSet<>(nuevos);
		for (Detalle_de_factura linea : anteriores)
		{
			if (linea.getProductoPapeleriaId() > 0)
			{
				todos.add("producto:" + linea.getProductoPapeleriaId());
			}
			if (linea.getRopaId() > 0)
			{
				todos.add("ropa:" + linea.getRopaId());
			}
		}
		for (String clave : todos)
		{
			String[] partes = clave.split(":");
			if (partes[0].equals("producto"))
			{
				productos.bloquear(Integer.parseInt(partes[1]));
			}
			else if (partes[0].equals("ropa"))
			{
				ropa.bloquear(Integer.parseInt(partes[1]));
			}
		}
	}

	private void devolverExistencias(List<Detalle_de_factura> lineas)
	{
		for (Detalle_de_factura linea : lineas)
		{
			if (linea.getProductoPapeleriaId() > 0 && linea.getCantidadProducto() > 0)
			{
				productos.bloquear(linea.getProductoPapeleriaId()).ifPresent(producto -> producto.reponer(linea.getCantidadProducto()));
			}
		}
	}

	private record Armado(List<Detalle_de_factura> lineas, double total)
	{
	}

	/**
	 * Revisa cada artículo, descuenta las existencias y arma las líneas de la factura.
	 * previas son las cantidades que ya tenía la venta que se está cambiando.
	 */
	private Armado prepararLineas(Map<String, Integer> cantidades, LocalDateTime fecha, Map<String, Integer> previas)
	{
		double total = 0;
		List<Detalle_de_factura> lineas = new ArrayList<>();

		for (Map.Entry<String, Integer> entrada : cantidades.entrySet())
		{
			String[] partes = entrada.getKey().split(":");
			String tipo = partes[0];
			int id = Integer.parseInt(partes[1]);
			int cantidad = entrada.getValue();

			Detalle_de_factura linea = new Detalle_de_factura();
			linea.setFechaGeneracion(fecha);
			switch (tipo)
			{
				case "producto" ->
				{
					ProductoPapeleria producto = productos.bloquear(id)
						.orElseThrow(() -> new RecursoNoEncontradoException("No existe un producto con el id " + id + "."));
					revisarEnCatalogo(producto.isActivo(), producto.getNombre(), cantidad, previas.get(entrada.getKey()));
					if (producto.getCantidad() < cantidad)
					{
						throw new OperacionRechazadaException(HttpStatus.CONFLICT, "No hay suficientes existencias",
							producto.getCantidad() == 0
								? "Ya no quedan unidades de «" + producto.getNombre() + "»."
								: "Solo quedan " + producto.getCantidad() + " unidades de «" + producto.getNombre() + "».");
					}
					producto.descontar(cantidad);
					linea.setDescripcion(descripcion(producto.getNombre()));
					linea.setProductoPapeleriaId(id);
					linea.setCantidadProducto(cantidad);
					total += producto.getPrecioVenta() * cantidad;
				}
				case "ropa" ->
				{
					Ropa prenda = ropa.bloquear(id)
						.orElseThrow(() -> new RecursoNoEncontradoException("No existe una prenda de ropa con el id " + id + "."));
					String nombrePrenda = prenda.getNombre() != null ? prenda.getNombre() : prenda.getTipoRopa();
					revisarEnCatalogo(prenda.isActivo(), nombrePrenda, cantidad, previas.get(entrada.getKey()));
					if (cantidad > 1)
					{
						throw new OperacionRechazadaException(HttpStatus.BAD_REQUEST, "Cantidad no válida",
							"Cada prenda es única: «" + nombrePrenda + "» se vende de a una.");
					}
					if (detalles.existsByRopaId(id))
					{
						throw new OperacionRechazadaException(HttpStatus.CONFLICT, "Prenda ya vendida", "«" + nombrePrenda + "» ya se vendió.");
					}
					linea.setDescripcion(descripcion(prenda.getNombre() != null ? prenda.getNombre() : prenda.getTipoRopa()));
					linea.setRopaId(id);
					linea.setCantidadRopa(cantidad);
					total += prenda.getPrecio() * cantidad;
				}
				default ->
				{
					Servicio servicio = servicios.findById(id)
						.orElseThrow(() -> new RecursoNoEncontradoException("No existe un servicio con el id " + id + "."));
					revisarEnCatalogo(servicio.isActivo(), servicio.getNombre(), cantidad, previas.get(entrada.getKey()));
					if (cantidad > 150)
					{
						throw new OperacionRechazadaException(HttpStatus.BAD_REQUEST, "Cantidad no válida", "Un servicio no puede pasar de 150 unidades por venta.");
					}
					linea.setDescripcion(descripcion(servicio.getNombre()));
					linea.setServicioId(id);
					linea.setCantidadServicio(cantidad);
					total += servicio.getPrecio() * cantidad;
				}
			}
			lineas.add(linea);
		}

		return new Armado(lineas, total);
	}

	/** Lo retirado del catálogo ya no se vende, salvo lo que esta venta ya tenía. */
	private static void revisarEnCatalogo(boolean activo, String nombre, int cantidad, Integer previa)
	{
		if (!activo && (previa == null || cantidad > previa))
		{
			throw new OperacionRechazadaException(HttpStatus.CONFLICT, "Artículo retirado", "«" + nombre + "» ya no está en el catálogo.");
		}
	}

	/** Cuántas unidades de cada artículo tenía la venta, con la misma llave que usa una venta nueva. */
	private static Map<String, Integer> cantidadesDe(List<Detalle_de_factura> lineas)
	{
		Map<String, Integer> cantidades = new HashMap<>();
		for (Detalle_de_factura linea : lineas)
		{
			if (linea.getProductoPapeleriaId() > 0)
			{
				cantidades.merge("producto:" + linea.getProductoPapeleriaId(), linea.getCantidadProducto(), Integer::sum);
			}
			else if (linea.getRopaId() > 0)
			{
				cantidades.merge("ropa:" + linea.getRopaId(), linea.getCantidadRopa(), Integer::sum);
			}
			else if (linea.getServicioId() > 0)
			{
				cantidades.merge("servicio:" + linea.getServicioId(), linea.getCantidadServicio(), Integer::sum);
			}
		}
		return cantidades;
	}

	private static void revisarTotal(double total, Double totalEsperado)
	{
		if (total <= 0)
		{
			throw new OperacionRechazadaException(HttpStatus.BAD_REQUEST, "Venta sin valor", "El total de la venta debe ser mayor que cero.");
		}
		if (totalEsperado != null && Math.abs(total - totalEsperado) >= 0.5)
		{
			// Lo que se cobró en la caja no es lo que vale ahora: alguien cambió un precio mientras se atendía.
			throw new OperacionRechazadaException(HttpStatus.CONFLICT, "Los precios cambiaron",
				String.format(java.util.Locale.ROOT, "Mientras atendías cambió el precio de algo: el total ahora es $ %,.0f. Revisa la venta y vuelve a cobrar.", total)
					.replace(',', '.'));
		}
	}

	private Factura_del_negocio buscar(Integer id)
	{
		return facturas.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("No existe una venta con el id " + id + "."));
	}

	private int operadorDeLaVenta(VentaRequest peticion, Sesion sesion)
	{
		if (sesion.esOperador())
		{
			return sesion.idUsuario();
		}
		if (peticion.operadorId() == null)
		{
			throw new OperacionRechazadaException(HttpStatus.BAD_REQUEST, "Falta el operador",
				"Las ventas quedan a nombre de un operador de cibercafé. Indica el operador en operadorId.");
		}
		Operador_de_cibercafe operador = operadores.findById(peticion.operadorId())
			.orElseThrow(() -> new OperacionRechazadaException(HttpStatus.BAD_REQUEST, "Operador no válido",
				"No existe un operador de cibercafé con el id " + peticion.operadorId() + "."));
		if (!operador.isActivo())
		{
			throw new OperacionRechazadaException(HttpStatus.BAD_REQUEST, "Operador no válido",
				operador.getPrimerNombre() + " no está activo. Elige otro operador.");
		}
		return peticion.operadorId();
	}

	/** Si el mismo artículo viene dos veces, se suman las cantidades en una sola línea. */
	private static Map<String, Integer> juntarRepetidos(List<VentaRequest.Articulo> articulos)
	{
		Map<String, Integer> cantidades = new LinkedHashMap<>();
		for (VentaRequest.Articulo articulo : articulos)
		{
			cantidades.merge(articulo.tipo() + ":" + articulo.id(), articulo.cantidad(), Integer::sum);
		}
		return cantidades;
	}

	private static String descripcion(String nombre)
	{
		String texto = "Venta de " + (nombre == null ? "artículo" : nombre.trim());
		return texto.length() > 120 ? texto.substring(0, 120) : texto;
	}

	/** Código de 15 cifras: la fecha y hora (12) más 3 cifras al azar. Se repite si por casualidad ya existe. */
	private String codigoNuevo()
	{
		String codigo;
		do
		{
			codigo = LocalDateTime.now().format(FORMATO_CODIGO) + String.format("%03d", azar.nextInt(1000));
		} while (facturas.existsByCodigoFactura(codigo));
		return codigo;
	}

	private List<VentaResponse> armar(List<Factura_del_negocio> lista)
	{
		if (lista.isEmpty())
		{
			return List.of();
		}
		Set<Integer> ids = lista.stream().map(Factura_del_negocio::getIdFactura).collect(Collectors.toSet());
		Map<Integer, List<Detalle_de_factura>> porFactura = detalles.findByFacturaDelNegocioIdIn(ids).stream()
			.collect(Collectors.groupingBy(Detalle_de_factura::getFacturaDelNegocioId));

		List<Detalle_de_factura> todas = porFactura.values().stream().flatMap(List::stream).toList();
		Map<Integer, ProductoPapeleria> productosUsados = porId(productos.findAllById(
			todas.stream().map(Detalle_de_factura::getProductoPapeleriaId).filter(id -> id > 0).collect(Collectors.toSet())), ProductoPapeleria::getId);
		Map<Integer, Ropa> ropaUsada = porId(ropa.findAllById(
			todas.stream().map(Detalle_de_factura::getRopaId).filter(id -> id > 0).collect(Collectors.toSet())), Ropa::getId);
		Map<Integer, Servicio> serviciosUsados = porId(servicios.findAllById(
			todas.stream().map(Detalle_de_factura::getServicioId).filter(id -> id > 0).collect(Collectors.toSet())), Servicio::getId);
		Map<Integer, Usuario> vendedores = porId(usuarios.findAllById(
			lista.stream().map(Factura_del_negocio::getUsuarioId).collect(Collectors.toSet())), Usuario::getIdUsuario);

		return lista.stream().map(factura ->
		{
			List<VentaResponse.Linea> lineas = porFactura.getOrDefault(factura.getIdFactura(), List.of()).stream()
				.map(linea -> linea(linea, productosUsados, ropaUsada, serviciosUsados))
				.toList();
			Usuario vendedor = vendedores.get(factura.getUsuarioId());
			String nombre = vendedor != null ? (vendedor.getPrimerNombre() + " " + vendedor.getPrimerApellido()).trim() : "Operador " + factura.getUsuarioId();
			return new VentaResponse(factura.getIdFactura(), factura.getCodigoFactura(), factura.getTipoFactura(), factura.getFechaGeneracion(),
				factura.getMonto(), factura.getUsuarioId(), nombre, lineas, factura.isAnulada());
		}).toList();
	}

	private static VentaResponse.Linea linea(Detalle_de_factura linea, Map<Integer, ProductoPapeleria> productos, Map<Integer, Ropa> ropa, Map<Integer, Servicio> servicios)
	{
		if (linea.getProductoPapeleriaId() > 0)
		{
			ProductoPapeleria producto = productos.get(linea.getProductoPapeleriaId());
			return new VentaResponse.Linea("producto", linea.getProductoPapeleriaId(), producto != null ? producto.getNombre() : linea.getDescripcion(),
				linea.getCantidadProducto(), producto != null ? producto.getPrecioVenta() : null);
		}
		if (linea.getRopaId() > 0)
		{
			Ropa prenda = ropa.get(linea.getRopaId());
			return new VentaResponse.Linea("ropa", linea.getRopaId(), prenda != null ? prenda.getNombre() : linea.getDescripcion(),
				linea.getCantidadRopa(), prenda != null ? prenda.getPrecio() : null);
		}
		Servicio servicio = servicios.get(linea.getServicioId());
		return new VentaResponse.Linea("servicio", linea.getServicioId() > 0 ? linea.getServicioId() : null,
			servicio != null ? servicio.getNombre() : linea.getDescripcion(), linea.getCantidadServicio(), servicio != null ? servicio.getPrecio() : null);
	}

	private static <T> Map<Integer, T> porId(List<T> lista, Function<T, Integer> id)
	{
		return lista.stream().collect(Collectors.toMap(id, elemento -> elemento, (a, b) -> a));
	}
}
