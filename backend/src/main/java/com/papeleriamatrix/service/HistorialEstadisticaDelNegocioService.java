package com.papeleriamatrix.service;

import com.papeleriamatrix.exception.RecursoNoEncontradoException;
import com.papeleriamatrix.model.Historial_estadistica_del_negocio;
import com.papeleriamatrix.repository.HistorialEstadisticaDelNegocioRepository;
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
}
