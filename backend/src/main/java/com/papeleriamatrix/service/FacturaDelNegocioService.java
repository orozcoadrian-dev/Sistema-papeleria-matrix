package com.papeleriamatrix.service;

import com.papeleriamatrix.exception.RecursoNoEncontradoException;
import com.papeleriamatrix.model.Factura_del_negocio;
import com.papeleriamatrix.repository.FacturaDelNegocioRepository;
import com.papeleriamatrix.dto.FacturaDelNegocioRequest;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FacturaDelNegocioService
{
	private final FacturaDelNegocioRepository repositorio;

	public FacturaDelNegocioService(FacturaDelNegocioRepository repositorio)
	{
		this.repositorio = repositorio;
	}

	@Transactional(readOnly = true)
	public List<Factura_del_negocio> listar()
	{
		return repositorio.findAll();
	}

	@Transactional(readOnly = true)
	public Factura_del_negocio obtener(Integer id)
	{
		return repositorio.findById(id)
			.orElseThrow(() -> new RecursoNoEncontradoException("No existe una factura del negocio con el id " + id + "."));
	}

	@Transactional
	public Factura_del_negocio crear(FacturaDelNegocioRequest peticion)
	{
		LocalDate ahora = LocalDate.now();
		return repositorio.save(new Factura_del_negocio(peticion.codigoFactura(), peticion.tipoFactura(), peticion.usuarioId(), peticion.monto(), ahora, ahora));
	}

	@Transactional
	public Factura_del_negocio actualizar(Integer id, FacturaDelNegocioRequest peticion)
	{
		Factura_del_negocio entidad = repositorio.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("No existe una factura del negocio con el id " + id + "."));
		entidad.setCodigoFactura(peticion.codigoFactura());
		entidad.setTipoFactura(peticion.tipoFactura());
		entidad.setUsuarioId(peticion.usuarioId());
		entidad.setMonto(peticion.monto());
		entidad.setFechaActualizacion(LocalDate.now());
		return repositorio.save(entidad);
	}

	@Transactional
	public void eliminar(Integer id)
	{
		repositorio.delete(repositorio.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("No existe una factura del negocio con el id " + id + ".")));
	}
}
