package com.papeleriamatrix.service;

import com.papeleriamatrix.exception.RecursoNoEncontradoException;
import com.papeleriamatrix.model.Historial_ingreso_del_negocio;
import com.papeleriamatrix.repository.HistorialIngresoDelNegocioRepository;
import com.papeleriamatrix.repository.IngresoDelNegocioRepository;
import com.papeleriamatrix.dto.HistorialIngresoDelNegocioRequest;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HistorialIngresoDelNegocioService
{
	private final HistorialIngresoDelNegocioRepository repositorio;
	private final IngresoDelNegocioRepository ingresoRepositorio;

	public HistorialIngresoDelNegocioService(
		HistorialIngresoDelNegocioRepository repositorio,
		IngresoDelNegocioRepository ingresoRepositorio)
	{
		this.repositorio = repositorio;
		this.ingresoRepositorio = ingresoRepositorio;
	}

	@Transactional(readOnly = true)
	public List<Historial_ingreso_del_negocio> listar()
	{
		return repositorio.findAll();
	}

	@Transactional(readOnly = true)
	public Historial_ingreso_del_negocio obtener(Integer id)
	{
		return repositorio.findById(id)
			.orElseThrow(() -> new RecursoNoEncontradoException("No existe un historial de ingreso con el id " + id + "."));
	}

	@Transactional
	public Historial_ingreso_del_negocio crear(HistorialIngresoDelNegocioRequest peticion)
	{
		validarIngreso(peticion.ingresoId());
		return repositorio.save(new Historial_ingreso_del_negocio(peticion.ingresoId(), peticion.motivoIngreso(), peticion.fechaGeneracion()));
	}

	@Transactional
	public Historial_ingreso_del_negocio actualizar(Integer id, HistorialIngresoDelNegocioRequest peticion)
	{
		Historial_ingreso_del_negocio entidad = repositorio.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("No existe un historial de ingreso con el id " + id + "."));
		validarIngreso(peticion.ingresoId());
		entidad.setIngresoId(peticion.ingresoId());
		entidad.setMotivoIngreso(peticion.motivoIngreso());
		entidad.setFechaGeneracion(peticion.fechaGeneracion());
		return repositorio.save(entidad);
	}

	@Transactional
	public void eliminar(Integer id)
	{
		repositorio.delete(repositorio.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("No existe un historial de ingreso con el id " + id + ".")));
	}

	private void validarIngreso(Integer ingresoId)
	{
		if (ingresoId == null || !ingresoRepositorio.existsById(ingresoId))
		{
			throw new RecursoNoEncontradoException("No existe un ingreso del negocio con el id indicado.");
		}
	}
}
