package com.papeleriamatrix.service;

import com.papeleriamatrix.exception.RecursoNoEncontradoException;
import com.papeleriamatrix.model.Operador_de_cibercafe;
import com.papeleriamatrix.repository.OperadorDeCibercafeRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OperadorDeCibercafeService
{
	private final OperadorDeCibercafeRepository repositorio;

	public OperadorDeCibercafeService(OperadorDeCibercafeRepository repositorio)
	{
		this.repositorio = repositorio;
	}

	@Transactional(readOnly = true)
	public List<Operador_de_cibercafe> listar()
	{
		return repositorio.findAll();
	}

	@Transactional(readOnly = true)
	public Operador_de_cibercafe obtener(Integer id)
	{
		return repositorio.findById(id)
			.orElseThrow(() -> new RecursoNoEncontradoException("No existe un operador de cibercafé con el id " + id + "."));
	}
}
