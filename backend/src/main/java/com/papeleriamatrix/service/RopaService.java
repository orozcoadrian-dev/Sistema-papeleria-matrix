package com.papeleriamatrix.service;

import com.papeleriamatrix.dto.RopaResponse;
import com.papeleriamatrix.dto.RopaRequest;
import com.papeleriamatrix.exception.RecursoNoEncontradoException;
import com.papeleriamatrix.exception.RecursoDuplicadoException;
import com.papeleriamatrix.model.Ropa;
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

	@Transactional
	public RopaResponse crear(RopaRequest peticion)
	{
		if (repositorio.existsByCodigoRopa(peticion.codigo()))
		{
			throw new RecursoDuplicadoException("Ya existe una prenda con el código " + peticion.codigo() + ".");
		}
		Ropa ropa = new Ropa(peticion.codigo(), peticion.tipoRopa(), peticion.material(), peticion.talla(), peticion.descripcion(), peticion.nombre(), peticion.precio(), peticion.color());
		return RopaResponse.desde(repositorio.save(ropa));
	}

	@Transactional
	public RopaResponse actualizar(Integer id, RopaRequest peticion)
	{
		Ropa ropa = repositorio.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("No existe una prenda con el id " + id + "."));
		if (repositorio.existsByCodigoRopaAndIdNot(peticion.codigo(), id))
		{
			throw new RecursoDuplicadoException("Ya existe otra prenda con el código " + peticion.codigo() + ".");
		}
		ropa.actualizar(peticion.codigo(), peticion.tipoRopa(), peticion.material(), peticion.talla(), peticion.descripcion(), peticion.nombre(), peticion.precio(), peticion.color());
		return RopaResponse.desde(repositorio.save(ropa));
	}

	@Transactional
	public void eliminar(Integer id)
	{
		repositorio.delete(repositorio.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("No existe una prenda con el id " + id + ".")));
	}
}
