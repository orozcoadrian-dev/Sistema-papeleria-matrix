package com.papeleriamatrix.service;

import com.papeleriamatrix.dto.IngresoResponse;
import com.papeleriamatrix.exception.RecursoNoEncontradoException;
import com.papeleriamatrix.exception.RecursoDuplicadoException;
import com.papeleriamatrix.model.Historial_ingreso_del_negocio;
import com.papeleriamatrix.model.Ingreso_del_negocio;
import com.papeleriamatrix.repository.FacturaDelNegocioRepository;
import com.papeleriamatrix.repository.HistorialIngresoDelNegocioRepository;
import com.papeleriamatrix.repository.IngresoDelNegocioRepository;
import com.papeleriamatrix.dto.IngresoDelNegocioRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IngresoDelNegocioService
{
	private final IngresoDelNegocioRepository repositorio;
	private final FacturaDelNegocioRepository facturaRepositorio;
	private final HistorialIngresoDelNegocioRepository historialRepositorio;

	public IngresoDelNegocioService(
		IngresoDelNegocioRepository repositorio,
		FacturaDelNegocioRepository facturaRepositorio,
		HistorialIngresoDelNegocioRepository historialRepositorio)
	{
		this.repositorio = repositorio;
		this.facturaRepositorio = facturaRepositorio;
		this.historialRepositorio = historialRepositorio;
	}

	@Transactional(readOnly = true)
	public List<Ingreso_del_negocio> listar()
	{
		return repositorio.findAll();
	}

	@Transactional(readOnly = true)
	public Ingreso_del_negocio obtener(Integer id)
	{
		return repositorio.findById(id)
			.orElseThrow(() -> new RecursoNoEncontradoException("No existe un ingreso del negocio con el id " + id + "."));
	}

	@Transactional
	public Ingreso_del_negocio crear(IngresoDelNegocioRequest peticion)
	{
		if (repositorio.existsByCodigoIngreso(peticion.codigoIngreso()))
		{
			throw new RecursoDuplicadoException("Ya existe un ingreso con ese código.");
		}
		return repositorio.save(new Ingreso_del_negocio(
			peticion.codigoIngreso(),
			aCeroSiEsNulo(peticion.totalSalarioOperadores()),
			aCeroSiEsNulo(peticion.totalVendidoRopa()),
			aCeroSiEsNulo(peticion.inversionTotalPapeleria()),
			aCeroSiEsNulo(peticion.totalVendidoServicios()),
			aCeroSiEsNulo(peticion.totalVendidoProductos()),
			aCeroSiEsNulo(peticion.totalRecaudoEsperadoPapeleria()),
			peticion.tipoIngreso()));
	}

	@Transactional
	public Ingreso_del_negocio actualizar(Integer id, IngresoDelNegocioRequest peticion)
	{
		Ingreso_del_negocio entidad = repositorio.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("No existe un ingreso del negocio con el id " + id + "."));
		if (repositorio.existsByCodigoIngresoAndIdIngresoNot(peticion.codigoIngreso(), id))
		{
			throw new RecursoDuplicadoException("Ya existe otro ingreso con ese código.");
		}
		entidad.setCodigoIngreso(peticion.codigoIngreso());
		entidad.setTotalSalarioOperadores(aCeroSiEsNulo(peticion.totalSalarioOperadores()));
		entidad.setTotalVendidoRopa(aCeroSiEsNulo(peticion.totalVendidoRopa()));
		entidad.setInversionTotalPapeleria(aCeroSiEsNulo(peticion.inversionTotalPapeleria()));
		entidad.setTotalVendidoServicios(aCeroSiEsNulo(peticion.totalVendidoServicios()));
		entidad.setTotalVendidoProductos(aCeroSiEsNulo(peticion.totalVendidoProductos()));
		entidad.setTotalRecaudoEsperadoPapeleria(aCeroSiEsNulo(peticion.totalRecaudoEsperadoPapeleria()));
		entidad.setTipoIngreso(peticion.tipoIngreso());
		return repositorio.save(entidad);
	}

	@Transactional(readOnly = true)
	public IngresoResponse resumir(LocalDate desde, LocalDate hasta)
	{
		validarRango(desde, hasta);
		BigDecimal totalServicios = importe(facturaRepositorio.sumarMontoPorTipoFactura(TiposFactura.SERVICIO, desde, hasta));
		BigDecimal totalRopa = importe(facturaRepositorio.sumarMontoPorTipoFactura(TiposFactura.ROPA, desde, hasta));
		BigDecimal totalProductos = importe(facturaRepositorio.sumarMontoPorTipoFactura(TiposFactura.PAPELERIA, desde, hasta))
			.add(importe(facturaRepositorio.sumarMontoPorTipoFactura(TiposFactura.PAPELERIA_SIN_TILDE, desde, hasta)))
			.add(importe(facturaRepositorio.sumarMontoPorTipoFactura(TiposFactura.VARIEDAD, desde, hasta)))
			.setScale(2, RoundingMode.HALF_UP);
		BigDecimal totalGeneral = totalServicios.add(totalRopa).add(totalProductos).setScale(2, RoundingMode.HALF_UP);
		return new IngresoResponse(null, null, desde, hasta, totalServicios, totalRopa, totalProductos, totalGeneral, "Ingreso ventas");
	}

	@Transactional
	public IngresoResponse calcularYGuardar(LocalDate desde, LocalDate hasta)
	{
		IngresoResponse resumen = resumir(desde, hasta);
		Ingreso_del_negocio ingreso = new Ingreso_del_negocio(
			generarCodigoUnico(),
			0,
			resumen.totalVendidoRopa().doubleValue(),
			0,
			resumen.totalVendidoServicios().doubleValue(),
			resumen.totalVendidoProductos().doubleValue(),
			0,
			"Ingreso ventas");
		Ingreso_del_negocio guardado = repositorio.save(ingreso);
		historialRepositorio.save(new Historial_ingreso_del_negocio(
			guardado.getIdIngreso(),
			RangoFechasEnPalabras.desdeHasta(desde, hasta),
			LocalDateTime.now()));
		return new IngresoResponse(
			guardado.getIdIngreso(),
			guardado.getCodigoIngreso(),
			desde,
			hasta,
			resumen.totalVendidoServicios(),
			resumen.totalVendidoRopa(),
			resumen.totalVendidoProductos(),
			resumen.totalGeneral(),
			guardado.getTipoIngreso());
	}

	@Transactional
	public void eliminar(Integer id)
	{
		repositorio.delete(repositorio.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("No existe un ingreso del negocio con el id " + id + ".")));
	}

	private BigDecimal importe(Double valor)
	{
		return BigDecimal.valueOf(valor != null ? valor : 0).setScale(2, RoundingMode.HALF_UP);
	}

	private double aCeroSiEsNulo(Double valor)
	{
		return valor != null ? valor : 0;
	}

	private String generarCodigoUnico()
	{
		for (int intento = 0; intento < 100; intento++)
		{
			String codigo = Long.toString(ThreadLocalRandom.current().nextLong(100_000_000_000_000L, 1_000_000_000_000_000L));
			if (!repositorio.existsByCodigoIngreso(codigo))
			{
				return codigo;
			}
		}
		throw new RecursoDuplicadoException("No se pudo generar un código único para el ingreso.");
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
