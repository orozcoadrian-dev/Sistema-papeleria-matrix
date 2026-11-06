package com.papeleriamatrix.service;

import com.papeleriamatrix.dto.ProductoResponse;
import com.papeleriamatrix.dto.ProductoPapeleriaRequest;
import com.papeleriamatrix.exception.RecursoDuplicadoException;
import com.papeleriamatrix.exception.RecursoNoEncontradoException;
import com.papeleriamatrix.model.ProductoPapeleria;
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

	@Transactional
	public ProductoResponse crear(ProductoPapeleriaRequest peticion)
	{
		if (repositorio.existsByCodigoProducto(peticion.codigo()))
		{
			throw new RecursoDuplicadoException("Ya existe un producto con el código " + peticion.codigo() + ".");
		}
		ProductoPapeleria producto = new ProductoPapeleria(
			peticion.codigo(), peticion.nombre(), peticion.cantidad(), peticion.precioVenta(),
			peticion.precioCompra(), peticion.descripcion(), peticion.lugarCompra());
		return ProductoResponse.desde(repositorio.save(producto));
	}

	@Transactional
	public ProductoResponse actualizar(Integer id, ProductoPapeleriaRequest peticion)
	{
		ProductoPapeleria producto = repositorio.findById(id)
			.orElseThrow(() -> new RecursoNoEncontradoException("No existe un producto con el id " + id + "."));
		if (repositorio.existsByCodigoProductoAndIdNot(peticion.codigo(), id))
		{
			throw new RecursoDuplicadoException("Ya existe otro producto con el código " + peticion.codigo() + ".");
		}
		producto.actualizar(
			peticion.codigo(), peticion.nombre(), peticion.cantidad(), peticion.precioVenta(),
			peticion.precioCompra(), peticion.descripcion(), peticion.lugarCompra());
		return ProductoResponse.desde(repositorio.save(producto));
	}

	@Transactional
	public void eliminar(Integer id)
	{
		ProductoPapeleria producto = repositorio.findById(id)
			.orElseThrow(() -> new RecursoNoEncontradoException("No existe un producto con el id " + id + "."));
		repositorio.delete(producto);
	}
}
