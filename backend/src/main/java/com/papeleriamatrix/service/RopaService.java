package com.papeleriamatrix.service;

import com.papeleriamatrix.dto.RopaResponse;
import com.papeleriamatrix.dto.RopaRequest;
import com.papeleriamatrix.exception.RecursoNoEncontradoException;
import com.papeleriamatrix.exception.RecursoDuplicadoException;
import com.papeleriamatrix.model.Ropa;
import com.papeleriamatrix.repository.DetalleDeFacturaRepository;
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

	private final DetalleDeFacturaRepository detalles;

	private final ImagenService imagenes;

	public RopaService(RopaRepository repositorio, DetalleDeFacturaRepository detalles, ImagenService imagenes)
	{
		this.imagenes = imagenes;
		this.repositorio = repositorio;
		this.detalles = detalles;
	}

	@Transactional(readOnly = true)
	public List<RopaResponse> listar()
	{
		java.util.Set<Integer> vendidas = new java.util.HashSet<>(detalles.ropaVendida());
		return repositorio.findAll(Sort.by("id")).stream().map(r -> RopaResponse.desde(r, vendidas.contains(r.getId()))).toList();
	}

	@Transactional(readOnly = true)
	public RopaResponse obtener(Integer id)
	{
		return repositorio.findById(id)
			.map(r -> RopaResponse.desde(r, detalles.existsByRopaId(r.getId())))
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
		return RopaResponse.desde(repositorio.save(ropa), detalles.existsByRopaId(id));
	}

	@Transactional
	public void eliminar(Integer id)
	{
		repositorio.delete(repositorio.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("No existe una prenda con el id " + id + ".")));
		// Se confirma el borrado en la base antes de quitar la foto: si no se puede borrar (está en ventas), la foto se queda.
		repositorio.flush();
		imagenes.quitar("ropa", id);
	}
}
