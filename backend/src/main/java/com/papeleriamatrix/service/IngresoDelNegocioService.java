package com.papeleriamatrix.service;

import com.papeleriamatrix.exception.RecursoNoEncontradoException;
import com.papeleriamatrix.model.Ingreso_del_negocio;
import com.papeleriamatrix.repository.IngresoDelNegocioRepository;
import com.papeleriamatrix.dto.IngresoDelNegocioRequest;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IngresoDelNegocioService
{
	private final IngresoDelNegocioRepository repositorio;

	public IngresoDelNegocioService(IngresoDelNegocioRepository repositorio)
	{
		this.repositorio = repositorio;
	}

	@Transactional(readOnly = true)
	public List<Ingreso_del_negocio> listar()
	{
		return repositorio.findAll();
	}

	@Transactional(readOnly = true)
	public Ingreso_del_negocio obtener(Integer id)
	{
		return repositorio.findById(id)
			.orElseThrow(() -> new RecursoNoEncontradoException("No existe un ingreso del negocio con el id " + id + "."));
	}

	@Transactional
	public Ingreso_del_negocio crear(IngresoDelNegocioRequest peticion)
	{
		return repositorio.save(new Ingreso_del_negocio(peticion.codigoIngreso(), peticion.totalSalarioOperadores(), peticion.totalVendidoRopa(),
			peticion.inversionTotalPapeleria(), peticion.totalVendidoServicios(), peticion.totalVendidoProductos(), peticion.totalRecaudoEsperadoPapeleria(), peticion.tipoIngreso()));
	}

	@Transactional
	public Ingreso_del_negocio actualizar(Integer id, IngresoDelNegocioRequest peticion)
	{
		Ingreso_del_negocio entidad = repositorio.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("No existe un ingreso del negocio con el id " + id + "."));
		entidad.setCodigoIngreso(peticion.codigoIngreso());
		entidad.setTotalSalarioOperadores(peticion.totalSalarioOperadores());
		entidad.setTotalVendidoRopa(peticion.totalVendidoRopa());
		entidad.setInversionTotalPapeleria(peticion.inversionTotalPapeleria());
		entidad.setTotalVendidoServicios(peticion.totalVendidoServicios());
		entidad.setTotalVendidoProductos(peticion.totalVendidoProductos());
		entidad.setTotalRecaudoEsperadoPapeleria(peticion.totalRecaudoEsperadoPapeleria());
		entidad.setTipoIngreso(peticion.tipoIngreso());
		return repositorio.save(entidad);
	}

	@Transactional
	public void eliminar(Integer id)
	{
		repositorio.delete(repositorio.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("No existe un ingreso del negocio con el id " + id + ".")));
	}
}
