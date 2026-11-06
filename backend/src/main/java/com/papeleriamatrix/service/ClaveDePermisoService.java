package com.papeleriamatrix.service;

import com.papeleriamatrix.exception.RecursoNoEncontradoException;
import com.papeleriamatrix.model.Clave_de_permiso;
import com.papeleriamatrix.repository.ClaveDePermisoRepository;
import com.papeleriamatrix.dto.ClaveDePermisoRequest;
import java.time.LocalDate;
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

	@Transactional
	public Clave_de_permiso crear(ClaveDePermisoRequest peticion)
	{
		LocalDate ahora = LocalDate.now();
		return repositorio.save(new Clave_de_permiso(peticion.usuarioId(), peticion.clave(), ahora, ahora));
	}

	@Transactional
	public Clave_de_permiso actualizar(Integer id, ClaveDePermisoRequest peticion)
	{
		Clave_de_permiso entidad = repositorio.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("No existe una clave de permiso con el id " + id + "."));
		entidad.setUsuarioId(peticion.usuarioId());
		entidad.setClave(peticion.clave());
		entidad.setFechaActualizacion(LocalDate.now());
		return repositorio.save(entidad);
	}

	@Transactional
	public void eliminar(Integer id)
	{
		repositorio.delete(repositorio.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("No existe una clave de permiso con el id " + id + ".")));
	}
}
