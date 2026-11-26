package com.papeleriamatrix.service;

import com.papeleriamatrix.dto.ExistenciasRequest;
import com.papeleriamatrix.dto.ProductoInternoResponse;
import com.papeleriamatrix.dto.ProductoResponse;
import com.papeleriamatrix.dto.ProductoPapeleriaRequest;
import com.papeleriamatrix.exception.OperacionRechazadaException;
import com.papeleriamatrix.exception.RecursoDuplicadoException;
import org.springframework.http.HttpStatus;
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

	private final ImagenService imagenes;

	public ProductoPapeleriaService(ProductoPapeleriaRepository repositorio, ImagenService imagenes)
	{
		this.repositorio = repositorio;
		this.imagenes = imagenes;
	}

	@Transactional(readOnly = true)
	public List<ProductoResponse> listar()
	{
		return repositorio.findByActivoTrue(Sort.by("id")).stream().map(ProductoResponse::desde).toList();
	}

	@Transactional(readOnly = true)
	public List<ProductoInternoResponse> listarInterno(boolean conCostos)
	{
		return repositorio.findByActivoTrue(Sort.by("id")).stream()
			.map(ProductoInternoResponse::desde)
			.map(producto -> conCostos ? producto : producto.sinCostos())
			.toList();
	}

	@Transactional(readOnly = true)
	public ProductoResponse obtener(Integer id)
	{
		return repositorio.findById(id)
			.filter(ProductoPapeleria::isActivo)
			.map(ProductoResponse::desde)
			.orElseThrow(() -> new RecursoNoEncontradoException("No existe un producto con el id " + id + "."));
	}

	@Transactional
	public ProductoResponse crear(ProductoPapeleriaRequest peticion)
	{
		if (repositorio.existsByCodigoProducto(peticion.codigo()))
		{
			throw new RecursoDuplicadoException(repositorio.existsByCodigoProductoAndActivoFalse(peticion.codigo())
				? "Hay un producto retirado con el código " + peticion.codigo() + ". Usa otro código."
				: "Ya existe un producto con el código " + peticion.codigo() + ".");
		}
		ProductoPapeleria producto = new ProductoPapeleria(
			peticion.codigo(), peticion.nombre(), peticion.cantidad(), peticion.precioVenta(),
			peticion.precioCompra(), peticion.descripcion(), peticion.lugarCompra());
		return ProductoResponse.desde(repositorio.save(producto));
	}

	@Transactional
	public ProductoResponse actualizar(Integer id, ProductoPapeleriaRequest peticion)
	{
		ProductoPapeleria producto = buscarBloqueado(id);
		revisarUnidades(producto, peticion.cantidadAnterior());
		if (repositorio.existsByCodigoProductoAndIdNot(peticion.codigo(), id))
		{
			throw new RecursoDuplicadoException("Ya existe otro producto con el código " + peticion.codigo() + ".");
		}
		producto.actualizar(
			peticion.codigo(), peticion.nombre(), peticion.cantidad(), peticion.precioVenta(),
			peticion.precioCompra(), peticion.descripcion(), peticion.lugarCompra());
		return ProductoResponse.desde(repositorio.save(producto));
	}

	/** Lo saca del catálogo sin borrarlo de la base: las ventas anteriores lo siguen nombrando. */
	@Transactional
	public void eliminar(Integer id, String motivo)
	{
		buscarBloqueado(id).retirar(motivo);
		imagenes.quitar("productos", id);
	}

	@Transactional
	public ProductoResponse destacar(Integer id, boolean destacado)
	{
		ProductoPapeleria producto = buscarBloqueado(id);
		producto.setDestacado(destacado);
		return ProductoResponse.desde(producto);
	}

	/** El operador o la administradora anotan cuántas unidades hay en la tienda. */
	@Transactional
	public ProductoInternoResponse ajustarExistencias(Integer id, ExistenciasRequest peticion)
	{
		ProductoPapeleria producto = buscarBloqueado(id);
		revisarUnidades(producto, peticion.cantidadAnterior());
		producto.ajustarExistencias(peticion.cantidad());
		return ProductoInternoResponse.desde(producto).sinCostos();
	}

	private ProductoPapeleria buscarBloqueado(Integer id)
	{
		return repositorio.bloquear(id)
			.filter(ProductoPapeleria::isActivo)
			.orElseThrow(() -> new RecursoNoEncontradoException("No existe un producto con el id " + id + "."));
	}

	/** No se guarda sobre unas unidades que ya cambiaron, para no borrar ventas de las existencias. */
	private static void revisarUnidades(ProductoPapeleria producto, Integer anteriores)
	{
		if (anteriores != null && !anteriores.equals(producto.getCantidad()))
		{
			throw new OperacionRechazadaException(HttpStatus.CONFLICT, "Las unidades cambiaron",
				"Mientras cambiabas este producto se vendieron unidades: ahora quedan " + producto.getCantidad()
					+ ". Revisa el número de unidades y guarda otra vez.");
		}
	}
}
