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
		return repositorio.findByActivoTrue(Sort.by("id")).stream().map(r -> RopaResponse.desde(r, vendidas.contains(r.getId()))).toList();
	}

	@Transactional(readOnly = true)
	public RopaResponse obtener(Integer id)
	{
		Ropa prenda = buscar(id);
		return RopaResponse.desde(prenda, detalles.existsByRopaId(id));
	}

	@Transactional
	public RopaResponse crear(RopaRequest peticion)
	{
		if (repositorio.existsByCodigoRopa(peticion.codigo()))
		{
			throw new RecursoDuplicadoException(repositorio.existsByCodigoRopaAndActivoFalse(peticion.codigo())
				? "Hay una prenda retirada con el código " + peticion.codigo() + ". Usa otro código."
				: "Ya existe una prenda con el código " + peticion.codigo() + ".");
		}
		Ropa ropa = new Ropa(peticion.codigo(), peticion.tipoRopa(), peticion.material(), peticion.talla(), peticion.descripcion(), peticion.nombre(), peticion.precio(), peticion.color());
		return RopaResponse.desde(repositorio.save(ropa));
	}

	@Transactional
	public RopaResponse actualizar(Integer id, RopaRequest peticion)
	{
		Ropa ropa = buscar(id);
		if (repositorio.existsByCodigoRopaAndIdNot(peticion.codigo(), id))
		{
			throw new RecursoDuplicadoException("Ya existe otra prenda con el código " + peticion.codigo() + ".");
		}
		ropa.actualizar(peticion.codigo(), peticion.tipoRopa(), peticion.material(), peticion.talla(), peticion.descripcion(), peticion.nombre(), peticion.precio(), peticion.color());
		return RopaResponse.desde(repositorio.save(ropa), detalles.existsByRopaId(id));
	}

	@Transactional
	public void eliminar(Integer id, String motivo)
	{
		// La saca del catálogo sin borrarla de la base: las ventas anteriores la siguen nombrando.
		buscar(id).retirar(motivo);
		imagenes.quitar("ropa", id);
	}

	@Transactional
	public RopaResponse destacar(Integer id, boolean destacado)
	{
		Ropa prenda = buscar(id);
		prenda.setDestacado(destacado);
		return RopaResponse.desde(prenda, detalles.existsByRopaId(id));
	}

	private Ropa buscar(Integer id)
	{
		return repositorio.findById(id)
			.filter(Ropa::isActivo)
			.orElseThrow(() -> new RecursoNoEncontradoException("No existe una prenda con el id " + id + "."));
	}
}
