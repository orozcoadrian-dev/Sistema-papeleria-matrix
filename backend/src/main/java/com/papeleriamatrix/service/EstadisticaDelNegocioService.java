package com.papeleriamatrix.service;

import com.papeleriamatrix.exception.RecursoNoEncontradoException;
import com.papeleriamatrix.dto.EstadisticaDelNegocioRequest;
import com.papeleriamatrix.model.Estadistica_del_negocio;
import com.papeleriamatrix.repository.EstadisticaDelNegocioRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EstadisticaDelNegocioService
{
	private final EstadisticaDelNegocioRepository repositorio;

	public EstadisticaDelNegocioService(EstadisticaDelNegocioRepository repositorio)
	{
		this.repositorio = repositorio;
	}

	@Transactional(readOnly = true)
	public List<Estadistica_del_negocio> listar()
	{
		return repositorio.findAll();
	}

	@Transactional(readOnly = true)
	public Estadistica_del_negocio obtener(Integer id)
	{
		return repositorio.findById(id)
			.orElseThrow(() -> new RecursoNoEncontradoException("No existe una estadística del negocio con el id " + id + "."));
	}

	@Transactional
	public Estadistica_del_negocio crear(EstadisticaDelNegocioRequest peticion)
	{
		Estadistica_del_negocio entidad = new Estadistica_del_negocio(peticion.codigoEstadistica(), peticion.tipoEstadistica());
		entidad.setNumeroInformacionEliminada(peticion.numeroInformacionEliminada());
		entidad.setServiciosEliminados(peticion.serviciosEliminados());
		entidad.setNumeroInformacionAgregada(peticion.numeroInformacionAgregada());
		entidad.setNumeroServiciosOfrecidos(peticion.numeroServiciosOfrecidos());
		entidad.setNumeroProductosVendidos(peticion.numeroProductosVendidos());
		entidad.setOperadoresCibercafeDespedidos(peticion.operadoresCibercafeDespedidos());
		entidad.setRopaActualCatalogo(peticion.ropaActualCatalogo());
		entidad.setNumeroProductosPapeleria(peticion.numeroProductosPapeleria());
		entidad.setRopaRemovidaCatalogo(peticion.ropaRemovidaCatalogo());
		entidad.setProductosRemovidos(peticion.productosRemovidos());
		entidad.setNumeroRopaVendida(peticion.numeroRopaVendida());
		entidad.setNumeroServicios(peticion.numeroServicios());
		entidad.setNumeroFacturasGeneradas(peticion.numeroFacturasGeneradas());
		return repositorio.save(entidad);
	}

	@Transactional
	public Estadistica_del_negocio actualizar(Integer id, EstadisticaDelNegocioRequest peticion)
	{
		Estadistica_del_negocio entidad = repositorio.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("No existe una estadística del negocio con el id " + id + "."));
		entidad.setCodigoEstadistica(peticion.codigoEstadistica());
		entidad.setTipoEstadistica(peticion.tipoEstadistica());
		entidad.setNumeroInformacionEliminada(peticion.numeroInformacionEliminada());
		entidad.setServiciosEliminados(peticion.serviciosEliminados());
		entidad.setNumeroInformacionAgregada(peticion.numeroInformacionAgregada());
		entidad.setNumeroServiciosOfrecidos(peticion.numeroServiciosOfrecidos());
		entidad.setNumeroProductosVendidos(peticion.numeroProductosVendidos());
		entidad.setOperadoresCibercafeDespedidos(peticion.operadoresCibercafeDespedidos());
		entidad.setRopaActualCatalogo(peticion.ropaActualCatalogo());
		entidad.setNumeroProductosPapeleria(peticion.numeroProductosPapeleria());
		entidad.setRopaRemovidaCatalogo(peticion.ropaRemovidaCatalogo());
		entidad.setProductosRemovidos(peticion.productosRemovidos());
		entidad.setNumeroRopaVendida(peticion.numeroRopaVendida());
		entidad.setNumeroServicios(peticion.numeroServicios());
		entidad.setNumeroFacturasGeneradas(peticion.numeroFacturasGeneradas());
		return repositorio.save(entidad);
	}

	@Transactional
	public void eliminar(Integer id)
	{
		repositorio.delete(repositorio.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("No existe una estadística del negocio con el id " + id + ".")));
	}
}
