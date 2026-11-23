package com.papeleriamatrix.service;

import com.papeleriamatrix.exception.RecursoNoEncontradoException;
import com.papeleriamatrix.exception.RecursoDuplicadoException;
import com.papeleriamatrix.model.ReporteDelNegocio;
import com.papeleriamatrix.repository.HistorialEstadisticaDelNegocioRepository;
import com.papeleriamatrix.repository.HistorialIngresoDelNegocioRepository;
import com.papeleriamatrix.repository.ReporteDelNegocioRepository;
import com.papeleriamatrix.dto.ReporteDelNegocioRequest;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReporteDelNegocioService
{
	private final ReporteDelNegocioRepository repositorio;
	private final HistorialEstadisticaDelNegocioRepository historialEstadisticaRepositorio;
	private final HistorialIngresoDelNegocioRepository historialIngresoRepositorio;

	public ReporteDelNegocioService(
		ReporteDelNegocioRepository repositorio,
		HistorialEstadisticaDelNegocioRepository historialEstadisticaRepositorio,
		HistorialIngresoDelNegocioRepository historialIngresoRepositorio)
	{
		this.repositorio = repositorio;
		this.historialEstadisticaRepositorio = historialEstadisticaRepositorio;
		this.historialIngresoRepositorio = historialIngresoRepositorio;
	}

	@Transactional(readOnly = true)
	public List<ReporteDelNegocio> listar()
	{
		return repositorio.findAll();
	}

	@Transactional(readOnly = true)
	public ReporteDelNegocio obtener(Integer id)
	{
		return repositorio.findById(id)
			.orElseThrow(() -> new RecursoNoEncontradoException("No existe un reporte del negocio con el id " + id + "."));
	}

	@Transactional
	public ReporteDelNegocio crear(ReporteDelNegocioRequest peticion)
	{
		if (repositorio.existsByCodigoReporte(peticion.codigoReporte()))
		{
			throw new RecursoDuplicadoException("Ya existe un reporte con ese código.");
		}
		validarHistoriales(peticion);
		return repositorio.save(new ReporteDelNegocio(peticion.codigoReporte(),
			aCeroSiEsNulo(peticion.historialEstadisticaDelNegocioId()),
			aCeroSiEsNulo(peticion.historialIngresoDelNegocioId()),
			aCeroSiEsNulo(peticion.usuarioId()),
			peticion.rutaArchivo(), peticion.fechaGeneracion()));
	}

	@Transactional
	public ReporteDelNegocio actualizar(Integer id, ReporteDelNegocioRequest peticion)
	{
		ReporteDelNegocio entidad = repositorio.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("No existe un reporte del negocio con el id " + id + "."));
		if (repositorio.existsByCodigoReporteAndIdReporteNot(peticion.codigoReporte(), id))
		{
			throw new RecursoDuplicadoException("Ya existe un reporte con ese código.");
		}
		validarHistoriales(peticion);
		entidad.setCodigoReporte(peticion.codigoReporte());
		entidad.setHistorialEstadisticaDelNegocioId(aCeroSiEsNulo(peticion.historialEstadisticaDelNegocioId()));
		entidad.setHistorialIngresoDelNegocioId(aCeroSiEsNulo(peticion.historialIngresoDelNegocioId()));
		entidad.setUsuarioId(aCeroSiEsNulo(peticion.usuarioId()));
		entidad.setRutaArchivo(peticion.rutaArchivo());
		entidad.setFechaGeneracion(peticion.fechaGeneracion());
		return repositorio.save(entidad);
	}

	@Transactional
	public void eliminar(Integer id)
	{
		repositorio.delete(repositorio.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("No existe un reporte del negocio con el id " + id + ".")));
	}

	private int aCeroSiEsNulo(Integer valor)
	{
		return valor != null ? valor : 0;
	}

	private void validarHistoriales(ReporteDelNegocioRequest peticion)
	{
		int historialEstadisticaId = aCeroSiEsNulo(peticion.historialEstadisticaDelNegocioId());
		if (historialEstadisticaId != 0 && !historialEstadisticaRepositorio.existsById(historialEstadisticaId))
		{
			throw new RecursoNoEncontradoException("No existe el historial de estadística indicado.");
		}

		int historialIngresoId = aCeroSiEsNulo(peticion.historialIngresoDelNegocioId());
		if (historialIngresoId != 0 && !historialIngresoRepositorio.existsById(historialIngresoId))
		{
			throw new RecursoNoEncontradoException("No existe el historial de ingreso indicado.");
		}
	}
}
