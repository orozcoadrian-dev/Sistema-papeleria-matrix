package com.papeleriamatrix.service;

import com.papeleriamatrix.dto.ProductoResponse;
import com.papeleriamatrix.exception.RecursoNoEncontradoException;
import com.papeleriamatrix.repository.ProductoPapeleriaRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consulta del catálogo de papelería para el público.
 */
@Service
public class ProductoPapeleriaService
{
	private final ProductoPapeleriaRepository repositorio;

	public ProductoPapeleriaService(ProductoPapeleriaRepository repositorio)
	{
		this.repositorio = repositorio;
	}

	@Transactional(readOnly = true)
	public List<ProductoResponse> listar()
	{
		return repositorio.findAll(Sort.by("id")).stream().map(ProductoResponse::desde).toList();
	}

	@Transactional(readOnly = true)
	public ProductoResponse obtener(Integer id)
	{
		return repositorio.findById(id)
			.map(ProductoResponse::desde)
			.orElseThrow(() -> new RecursoNoEncontradoException("No existe un producto con el id " + id + "."));
	}
}
