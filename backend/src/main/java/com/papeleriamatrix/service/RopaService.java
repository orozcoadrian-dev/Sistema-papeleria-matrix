package com.papeleriamatrix.service;

import com.papeleriamatrix.dto.RopaResponse;
import com.papeleriamatrix.exception.RecursoNoEncontradoException;
import com.papeleriamatrix.repository.RopaRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consulta del catálogo de ropa del aliado Pulguero Glam.
 */
@Service
public class RopaService
{
	private final RopaRepository repositorio;

	public RopaService(RopaRepository repositorio)
	{
		this.repositorio = repositorio;
	}

	@Transactional(readOnly = true)
	public List<RopaResponse> listar()
	{
		return repositorio.findAll(Sort.by("id")).stream().map(RopaResponse::desde).toList();
	}

	@Transactional(readOnly = true)
	public RopaResponse obtener(Integer id)
	{
		return repositorio.findById(id)
			.map(RopaResponse::desde)
			.orElseThrow(() -> new RecursoNoEncontradoException("No existe una prenda con el id " + id + "."));
	}
}
