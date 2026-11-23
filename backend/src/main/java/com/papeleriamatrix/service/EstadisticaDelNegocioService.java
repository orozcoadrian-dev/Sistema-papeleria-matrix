package com.papeleriamatrix.service;

import com.papeleriamatrix.dto.EstadisticaDelNegocioRequest;
import com.papeleriamatrix.dto.EstadisticaResponse;
import com.papeleriamatrix.exception.RecursoDuplicadoException;
import com.papeleriamatrix.exception.RecursoNoEncontradoException;
import com.papeleriamatrix.model.Estadistica_del_negocio;
import com.papeleriamatrix.model.Historial_estadistica_del_negocio;
import com.papeleriamatrix.repository.DetalleDeFacturaRepository;
import com.papeleriamatrix.repository.FacturaDelNegocioRepository;
import com.papeleriamatrix.repository.EstadisticaDelNegocioRepository;
import com.papeleriamatrix.repository.HistorialEstadisticaDelNegocioRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EstadisticaDelNegocioService
{
	private final EstadisticaDelNegocioRepository repositorio;
	private final FacturaDelNegocioRepository facturaRepositorio;
	private final DetalleDeFacturaRepository detalleRepositorio;
	private final HistorialEstadisticaDelNegocioRepository historialRepositorio;

	public EstadisticaDelNegocioService(
		EstadisticaDelNegocioRepository repositorio,
		FacturaDelNegocioRepository facturaRepositorio,
		DetalleDeFacturaRepository detalleRepositorio,
		HistorialEstadisticaDelNegocioRepository historialRepositorio)
	{
		this.repositorio = repositorio;
		this.facturaRepositorio = facturaRepositorio;
		this.detalleRepositorio = detalleRepositorio;
		this.historialRepositorio = historialRepositorio;
	}

	@Transactional(readOnly = true)
	public List<Estadistica_del_negocio> listar()
	{
		return repositorio.findAll();
	}

	@Transactional(readOnly = true)
	public Estadistica_del_negocio obtener(Integer id)
	{
		return repositorio.findById(id)
			.orElseThrow(() -> new RecursoNoEncontradoException("No existe una estadística del negocio con el id " + id + "."));
	}

	@Transactional
	public Estadistica_del_negocio crear(EstadisticaDelNegocioRequest peticion)
	{
		if (repositorio.existsByCodigoEstadistica(peticion.codigoEstadistica()))
		{
			throw new RecursoDuplicadoException("Ya existe una estadística con ese código.");
		}
		Estadistica_del_negocio entidad = new Estadistica_del_negocio(peticion.codigoEstadistica(), peticion.tipoEstadistica());
		entidad.setNumeroInformacionEliminada(aCeroSiEsNulo(peticion.numeroInformacionEliminada()));
		entidad.setServiciosEliminados(aCeroSiEsNulo(peticion.serviciosEliminados()));
		entidad.setNumeroInformacionAgregada(aCeroSiEsNulo(peticion.numeroInformacionAgregada()));
		entidad.setNumeroServiciosOfrecidos(aCeroSiEsNulo(peticion.numeroServiciosOfrecidos()));
		entidad.setNumeroProductosVendidos(aCeroSiEsNulo(peticion.numeroProductosVendidos()));
		entidad.setOperadoresCibercafeDespedidos(aCeroSiEsNulo(peticion.operadoresCibercafeDespedidos()));
		entidad.setRopaActualCatalogo(aCeroSiEsNulo(peticion.ropaActualCatalogo()));
		entidad.setNumeroProductosPapeleria(aCeroSiEsNulo(peticion.numeroProductosPapeleria()));
		entidad.setRopaRemovidaCatalogo(aCeroSiEsNulo(peticion.ropaRemovidaCatalogo()));
		entidad.setProductosRemovidos(aCeroSiEsNulo(peticion.productosRemovidos()));
		entidad.setNumeroRopaVendida(aCeroSiEsNulo(peticion.numeroRopaVendida()));
		entidad.setNumeroServicios(aCeroSiEsNulo(peticion.numeroServicios()));
		entidad.setNumeroFacturasGeneradas(aCeroSiEsNulo(peticion.numeroFacturasGeneradas()));
		return repositorio.save(entidad);
	}

	@Transactional
	public Estadistica_del_negocio actualizar(Integer id, EstadisticaDelNegocioRequest peticion)
	{
		Estadistica_del_negocio entidad = repositorio.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("No existe una estadística del negocio con el id " + id + "."));
		if (repositorio.existsByCodigoEstadisticaAndIdEstadisticaNot(peticion.codigoEstadistica(), id))
		{
			throw new RecursoDuplicadoException("Ya existe otra estadística con ese código.");
		}
		entidad.setCodigoEstadistica(peticion.codigoEstadistica());
		entidad.setTipoEstadistica(peticion.tipoEstadistica());
		entidad.setNumeroInformacionEliminada(aCeroSiEsNulo(peticion.numeroInformacionEliminada()));
		entidad.setServiciosEliminados(aCeroSiEsNulo(peticion.serviciosEliminados()));
		entidad.setNumeroInformacionAgregada(aCeroSiEsNulo(peticion.numeroInformacionAgregada()));
		entidad.setNumeroServiciosOfrecidos(aCeroSiEsNulo(peticion.numeroServiciosOfrecidos()));
		entidad.setNumeroProductosVendidos(aCeroSiEsNulo(peticion.numeroProductosVendidos()));
		entidad.setOperadoresCibercafeDespedidos(aCeroSiEsNulo(peticion.operadoresCibercafeDespedidos()));
		entidad.setRopaActualCatalogo(aCeroSiEsNulo(peticion.ropaActualCatalogo()));
		entidad.setNumeroProductosPapeleria(aCeroSiEsNulo(peticion.numeroProductosPapeleria()));
		entidad.setRopaRemovidaCatalogo(aCeroSiEsNulo(peticion.ropaRemovidaCatalogo()));
		entidad.setProductosRemovidos(aCeroSiEsNulo(peticion.productosRemovidos()));
		entidad.setNumeroRopaVendida(aCeroSiEsNulo(peticion.numeroRopaVendida()));
		entidad.setNumeroServicios(aCeroSiEsNulo(peticion.numeroServicios()));
		entidad.setNumeroFacturasGeneradas(aCeroSiEsNulo(peticion.numeroFacturasGeneradas()));
		return repositorio.save(entidad);
	}

	@Transactional(readOnly = true)
	public EstadisticaResponse resumir(LocalDate desde, LocalDate hasta)
	{
		validarRango(desde, hasta);
		int facturas = Math.toIntExact(facturaRepositorio.countByFechaGeneracionBetween(desde, hasta));
		int productos = Math.toIntExact(aCeroSiEsNulo(detalleRepositorio.sumarCantidadProductoPorFechaFactura(desde, hasta)));
		int ropa = Math.toIntExact(aCeroSiEsNulo(detalleRepositorio.sumarCantidadRopaPorFechaFactura(desde, hasta)));
		int servicios = Math.toIntExact(aCeroSiEsNulo(detalleRepositorio.sumarCantidadServicioPorFechaFactura(desde, hasta)));
		BigDecimal total = importe(facturaRepositorio.sumarMontoPorFechaGeneracion(desde, hasta));
		BigDecimal ticketPromedio = facturas == 0
			? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
			: total.divide(BigDecimal.valueOf(facturas), 2, RoundingMode.HALF_UP);
		return new EstadisticaResponse(null, null, desde, hasta, facturas, productos, ropa, servicios, ticketPromedio, "Estadistica");
	}

	@Transactional
	public EstadisticaResponse calcularYGuardar(LocalDate desde, LocalDate hasta)
	{
		EstadisticaResponse resumen = resumir(desde, hasta);
		Estadistica_del_negocio estadistica = new Estadistica_del_negocio();
		estadistica.setCodigoEstadistica(generarCodigoUnico());
		estadistica.setTipoEstadistica("Estadistica");
		estadistica.setNumeroFacturasGeneradas(resumen.numeroFacturasGeneradas());
		estadistica.setNumeroProductosVendidos(resumen.numeroProductosVendidos());
		estadistica.setNumeroRopaVendida(resumen.numeroRopaVendida());
		estadistica.setNumeroServicios(resumen.numeroServicios());
		Estadistica_del_negocio guardada = repositorio.save(estadistica);
		historialRepositorio.save(new Historial_estadistica_del_negocio(
			guardada.getIdEstadistica(),
			RangoFechasEnPalabras.desdeHasta(desde, hasta),
			LocalDateTime.now()));
		return new EstadisticaResponse(
			guardada.getIdEstadistica(),
			guardada.getCodigoEstadistica(),
			desde,
			hasta,
			resumen.numeroFacturasGeneradas(),
			resumen.numeroProductosVendidos(),
			resumen.numeroRopaVendida(),
			resumen.numeroServicios(),
			resumen.ticketPromedio(),
			guardada.getTipoEstadistica());
	}

	@Transactional
	public void eliminar(Integer id)
	{
		repositorio.delete(repositorio.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("No existe una estadística del negocio con el id " + id + ".")));
	}

	private int aCeroSiEsNulo(Integer valor)
	{
		return valor != null ? valor : 0;
	}

	private long aCeroSiEsNulo(Long valor)
	{
		return valor != null ? valor : 0;
	}

	private BigDecimal importe(Double valor)
	{
		return BigDecimal.valueOf(valor != null ? valor : 0).setScale(2, RoundingMode.HALF_UP);
	}

	private String generarCodigoUnico()
	{
		for (int intento = 0; intento < 100; intento++)
		{
			String codigo = Long.toString(ThreadLocalRandom.current().nextLong(100_000_000_000_000L, 1_000_000_000_000_000L));
			if (!repositorio.existsByCodigoEstadistica(codigo))
			{
				return codigo;
			}
		}
		throw new RecursoDuplicadoException("No se pudo generar un código único para la estadística.");
	}

	private void validarRango(LocalDate desde, LocalDate hasta)
	{
		if (desde == null || hasta == null)
		{
			throw new IllegalArgumentException("Las fechas del rango son obligatorias.");
		}
		if (hasta.isBefore(desde))
		{
			throw new IllegalArgumentException("La fecha final no puede ser anterior a la inicial.");
		}
	}
}
