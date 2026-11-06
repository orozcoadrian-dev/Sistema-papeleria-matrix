package com.papeleriamatrix.service;

import com.papeleriamatrix.exception.RecursoNoEncontradoException;
import com.papeleriamatrix.model.Reporte_del_negocio;
import com.papeleriamatrix.repository.ReporteDelNegocioRepository;
import com.papeleriamatrix.dto.ReporteDelNegocioRequest;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReporteDelNegocioService
{
	private final ReporteDelNegocioRepository repositorio;

	public ReporteDelNegocioService(ReporteDelNegocioRepository repositorio)
	{
		this.repositorio = repositorio;
	}

	@Transactional(readOnly = true)
	public List<Reporte_del_negocio> listar()
	{
		return repositorio.findAll();
	}

	@Transactional(readOnly = true)
	public Reporte_del_negocio obtener(Integer id)
	{
		return repositorio.findById(id)
			.orElseThrow(() -> new RecursoNoEncontradoException("No existe un reporte del negocio con el id " + id + "."));
	}

	@Transactional
	public Reporte_del_negocio crear(ReporteDelNegocioRequest peticion)
	{
		return repositorio.save(new Reporte_del_negocio(peticion.codigoReporte(), peticion.historialEstadisticaDelNegocioId(),
			peticion.historialIngresoDelNegocioId(), peticion.usuarioId(), peticion.rutaArchivo(), peticion.fechaGeneracion()));
	}

	@Transactional
	public Reporte_del_negocio actualizar(Integer id, ReporteDelNegocioRequest peticion)
	{
		Reporte_del_negocio entidad = repositorio.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("No existe un reporte del negocio con el id " + id + "."));
		entidad.setCodigoReporte(peticion.codigoReporte());
		entidad.setHistorialEstadisticaDelNegocioId(peticion.historialEstadisticaDelNegocioId());
		entidad.setHistorialIngresoDelNegocioId(peticion.historialIngresoDelNegocioId());
		entidad.setUsuarioId(peticion.usuarioId());
		entidad.setRutaArchivo(peticion.rutaArchivo());
		entidad.setFechaGeneracion(peticion.fechaGeneracion());
		return repositorio.save(entidad);
	}

	@Transactional
	public void eliminar(Integer id)
	{
		repositorio.delete(repositorio.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("No existe un reporte del negocio con el id " + id + ".")));
	}
}
