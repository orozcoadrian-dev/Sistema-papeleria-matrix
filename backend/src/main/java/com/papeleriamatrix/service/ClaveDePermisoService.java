package com.papeleriamatrix.service;

import com.papeleriamatrix.exception.RecursoNoEncontradoException;
import com.papeleriamatrix.model.Clave_de_permiso;
import com.papeleriamatrix.repository.ClaveDePermisoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClaveDePermisoService
{
	private final ClaveDePermisoRepository repositorio;

	public ClaveDePermisoService(ClaveDePermisoRepository repositorio)
	{
		this.repositorio = repositorio;
	}

	@Transactional(readOnly = true)
	public List<Clave_de_permiso> listar()
	{
		return repositorio.findAll();
	}

	@Transactional(readOnly = true)
	public Clave_de_permiso obtener(Integer id)
	{
		return repositorio.findById(id)
			.orElseThrow(() -> new RecursoNoEncontradoException("No existe una clave de permiso con el id " + id + "."));
	}
}
