package com.papeleriamatrix.service;

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
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registra las ventas. Una venta es una factura del negocio con un detalle por cada artículo.
 * Al vender un producto de papelería se descuentan sus existencias; si no alcanzan, la venta no se hace.
 * La ropa y los servicios no llevan existencias en la base de datos.
 */
@Service
public class VentaService
{
	private static final String TIPO_FACTURA = "Venta en tienda";
	private static final DateTimeFormatter FORMATO_CODIGO = DateTimeFormatter.ofPattern("yyMMddHHmmss");

	private final FacturaDelNegocioRepository facturas;
	private final DetalleDeFacturaRepository detalles;
	private final ProductoPapeleriaRepository productos;
	private final RopaRepository ropa;
	private final ServicioRepository servicios;
	private final UsuarioRepository usuarios;
	private final OperadorDeCibercafeRepository operadores;
	private final SecureRandom azar = new SecureRandom();

	public VentaService(FacturaDelNegocioRepository facturas, DetalleDeFacturaRepository detalles, ProductoPapeleriaRepository productos,
		RopaRepository ropa, ServicioRepository servicios, UsuarioRepository usuarios, OperadorDeCibercafeRepository operadores)
	{
		this.facturas = facturas;
		this.detalles = detalles;
		this.productos = productos;
		this.ropa = ropa;
		this.servicios = servicios;
		this.usuarios = usuarios;
		this.operadores = operadores;
	}

	@Transactional
	public VentaResponse registrar(VentaRequest peticion, Sesion sesion)
	{
		int operadorId = operadorDeLaVenta(peticion, sesion);
		Map<String, Integer> cantidades = juntarRepetidos(peticion.articulos());

		double total = 0;
		List<Detalle_de_factura> lineas = new ArrayList<>();
		LocalDateTime ahora = LocalDateTime.now();

		for (Map.Entry<String, Integer> entrada : cantidades.entrySet())
		{
			String[] partes = entrada.getKey().split(":");
			String tipo = partes[0];
			int id = Integer.parseInt(partes[1]);
			int cantidad = entrada.getValue();

			Detalle_de_factura linea = new Detalle_de_factura();
			linea.setFechaGeneracion(ahora);
			switch (tipo)
			{
				case "producto" ->
				{
					ProductoPapeleria producto = productos.findById(id)
						.orElseThrow(() -> new RecursoNoEncontradoException("No existe un producto con el id " + id + "."));
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
					Ropa prenda = ropa.findById(id)
						.orElseThrow(() -> new RecursoNoEncontradoException("No existe una prenda de ropa con el id " + id + "."));
					linea.setDescripcion(descripcion(prenda.getNombre() != null ? prenda.getNombre() : prenda.getTipoRopa()));
					linea.setRopaId(id);
					linea.setCantidadRopa(cantidad);
					total += prenda.getPrecio() * cantidad;
				}
				default ->
				{
					Servicio servicio = servicios.findById(id)
						.orElseThrow(() -> new RecursoNoEncontradoException("No existe un servicio con el id " + id + "."));
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

		if (total <= 0)
		{
			throw new OperacionRechazadaException(HttpStatus.BAD_REQUEST, "Venta sin valor", "El total de la venta debe ser mayor que cero.");
		}

		LocalDate hoy = ahora.toLocalDate();
		Factura_del_negocio factura = facturas.save(new Factura_del_negocio(codigoNuevo(), TIPO_FACTURA, operadorId, total, hoy, null));
		for (Detalle_de_factura linea : lineas)
		{
			linea.setFacturaDelNegocioId(factura.getIdFactura());
		}
		detalles.saveAll(lineas);
		return armar(List.of(factura)).getFirst();
	}

	@Transactional(readOnly = true)
	public List<VentaResponse> listar(Sesion sesion, LocalDate desde, LocalDate hasta)
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
		return armar(encontradas);
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

	/** Anula la venta: devuelve las existencias de los productos y borra la factura con sus detalles. */
	@Transactional
	public void anular(Integer id)
	{
		Factura_del_negocio factura = buscar(id);
		List<Detalle_de_factura> suyas = detalles.findByFacturaDelNegocioId(factura.getIdFactura());
		for (Detalle_de_factura linea : suyas)
		{
			if (linea.getProductoPapeleriaId() > 0 && linea.getCantidadProducto() > 0)
			{
				productos.findById(linea.getProductoPapeleriaId()).ifPresent(producto -> producto.reponer(linea.getCantidadProducto()));
			}
		}
		detalles.deleteAll(suyas);
		facturas.delete(factura);
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
		if (!operadores.existsById(peticion.operadorId()))
		{
			throw new OperacionRechazadaException(HttpStatus.BAD_REQUEST, "Operador no válido",
				"No existe un operador de cibercafé con el id " + peticion.operadorId() + ".");
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
				factura.getMonto(), factura.getUsuarioId(), nombre, lineas);
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
