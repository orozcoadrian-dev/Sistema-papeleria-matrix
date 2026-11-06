package com.papeleriamatrix.service;

import com.papeleriamatrix.exception.RecursoNoEncontradoException;
import com.papeleriamatrix.model.Historial_estadistica_del_negocio;
import com.papeleriamatrix.repository.HistorialEstadisticaDelNegocioRepository;
import com.papeleriamatrix.dto.HistorialEstadisticaDelNegocioRequest;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HistorialEstadisticaDelNegocioService
{
	private final HistorialEstadisticaDelNegocioRepository repositorio;

	public HistorialEstadisticaDelNegocioService(HistorialEstadisticaDelNegocioRepository repositorio)
	{
		this.repositorio = repositorio;
	}

	@Transactional(readOnly = true)
	public List<Historial_estadistica_del_negocio> listar()
	{
		return repositorio.findAll();
	}

	@Transactional(readOnly = true)
	public Historial_estadistica_del_negocio obtener(Integer id)
	{
		return repositorio.findById(id)
			.orElseThrow(() -> new RecursoNoEncontradoException("No existe un historial de estadística con el id " + id + "."));
	}

	@Transactional
	public Historial_estadistica_del_negocio crear(HistorialEstadisticaDelNegocioRequest peticion)
	{
		return repositorio.save(new Historial_estadistica_del_negocio(peticion.estadisticaDelNegocioId(), peticion.motivoEstadistica(), peticion.fechaGeneracion()));
	}

	@Transactional
	public Historial_estadistica_del_negocio actualizar(Integer id, HistorialEstadisticaDelNegocioRequest peticion)
	{
		Historial_estadistica_del_negocio entidad = repositorio.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("No existe un historial de estadística con el id " + id + "."));
		entidad.setEstadisticaDelNegocioId(peticion.estadisticaDelNegocioId());
		entidad.setMotivoEstadistica(peticion.motivoEstadistica());
		entidad.setFechaGeneracion(peticion.fechaGeneracion());
		return repositorio.save(entidad);
	}

	@Transactional
	public void eliminar(Integer id)
	{
		repositorio.delete(repositorio.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("No existe un historial de estadística con el id " + id + ".")));
	}
}
