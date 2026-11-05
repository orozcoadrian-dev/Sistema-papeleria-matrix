package com.papeleriamatrix.service;

import com.papeleriamatrix.exception.RecursoNoEncontradoException;
import com.papeleriamatrix.model.Estadistica_del_negocio;
import com.papeleriamatrix.repository.EstadisticaDelNegocioRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EstadisticaDelNegocioService
{
	private final EstadisticaDelNegocioRepository repositorio;

	public EstadisticaDelNegocioService(EstadisticaDelNegocioRepository repositorio)
	{
		this.repositorio = repositorio;
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
}
