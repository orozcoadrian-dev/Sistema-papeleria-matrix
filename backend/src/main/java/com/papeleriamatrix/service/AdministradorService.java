package com.papeleriamatrix.service;

import com.papeleriamatrix.exception.RecursoNoEncontradoException;
import com.papeleriamatrix.model.Administrador;
import com.papeleriamatrix.repository.AdministradorRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdministradorService
{
	private final AdministradorRepository repositorio;

	public AdministradorService(AdministradorRepository repositorio)
	{
		this.repositorio = repositorio;
	}

	@Transactional(readOnly = true)
	public List<Administrador> listar()
	{
		return repositorio.findAll();
	}

	@Transactional(readOnly = true)
	public Administrador obtener(Integer id)
	{
		return repositorio.findById(id)
			.orElseThrow(() -> new RecursoNoEncontradoException("No existe un administrador con el id " + id + "."));
	}
}
