package com.papeleriamatrix.service;

import com.papeleriamatrix.dto.ServicioRequest;
import com.papeleriamatrix.dto.ServicioResponse;
import com.papeleriamatrix.exception.RecursoDuplicadoException;
import com.papeleriamatrix.exception.RecursoNoEncontradoException;
import com.papeleriamatrix.model.Servicio;
import com.papeleriamatrix.repository.ServicioRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reglas de los servicios del negocio: consultar, crear, actualizar y eliminar.
 */
@Service
public class ServicioService
{
	private final ServicioRepository repositorio;

	private final ImagenService imagenes;

	public ServicioService(ServicioRepository repositorio, ImagenService imagenes)
	{
		this.repositorio = repositorio;
		this.imagenes = imagenes;
	}

	@Transactional(readOnly = true)
	public List<ServicioResponse> listar()
	{
		return repositorio.findAll(Sort.by("id")).stream().map(ServicioResponse::desde).toList();
	}

	@Transactional(readOnly = true)
	public ServicioResponse obtener(Integer id)
	{
		return ServicioResponse.desde(buscar(id));
	}

	@Transactional
	public ServicioResponse crear(ServicioRequest peticion)
	{
		if(repositorio.existsByCodigoServicio(peticion.codigo()))
		{
			throw new RecursoDuplicadoException("Ya existe un servicio con el código " + peticion.codigo() + ".");
		}

		Servicio servicio = new Servicio(peticion.codigo(), peticion.nombre(), peticion.categoria(), peticion.precio(), peticion.descripcion());
		return ServicioResponse.desde(repositorio.save(servicio));
	}

	@Transactional
	public ServicioResponse actualizar(Integer id, ServicioRequest peticion)
	{
		Servicio servicio = buscar(id);

		if(repositorio.existsByCodigoServicioAndIdNot(peticion.codigo(), id))
		{
			throw new RecursoDuplicadoException("Ya existe otro servicio con el código " + peticion.codigo() + ".");
		}

		servicio.actualizar(peticion.codigo(), peticion.nombre(), peticion.categoria(), peticion.precio(), peticion.descripcion());
		return ServicioResponse.desde(repositorio.save(servicio));
	}

	@Transactional
	public void eliminar(Integer id)
	{
		repositorio.delete(buscar(id));
		// Se confirma el borrado en la base antes de quitar la foto: si no se puede borrar (está en ventas), la foto se queda.
		repositorio.flush();
		imagenes.quitar("servicios", id);
	}

	private Servicio buscar(Integer id)
	{
		return repositorio.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("No existe un servicio con el id " + id + "."));
	}
}
