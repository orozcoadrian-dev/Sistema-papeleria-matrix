package com.papeleriamatrix.service;

import com.papeleriamatrix.exception.RecursoNoEncontradoException;
import com.papeleriamatrix.model.Usuario;
import com.papeleriamatrix.repository.UsuarioRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService
{
	private final UsuarioRepository repositorio;

	public UsuarioService(UsuarioRepository repositorio)
	{
		this.repositorio = repositorio;
	}

	@Transactional(readOnly = true)
	public List<Usuario> listar()
	{
		return repositorio.findAll();
	}

	@Transactional(readOnly = true)
	public Usuario obtener(Integer id)
	{
		return repositorio.findById(id)
			.orElseThrow(() -> new RecursoNoEncontradoException("No existe un usuario con el id " + id + "."));
	}
}
