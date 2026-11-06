package com.papeleriamatrix.service;

import com.papeleriamatrix.exception.RecursoNoEncontradoException;
import com.papeleriamatrix.model.Detalle_de_factura;
import com.papeleriamatrix.repository.DetalleDeFacturaRepository;
import com.papeleriamatrix.dto.DetalleDeFacturaRequest;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DetalleDeFacturaService
{
	private final DetalleDeFacturaRepository repositorio;

	public DetalleDeFacturaService(DetalleDeFacturaRepository repositorio)
	{
		this.repositorio = repositorio;
	}

	@Transactional(readOnly = true)
	public List<Detalle_de_factura> listar()
	{
		return repositorio.findAll();
	}

	@Transactional(readOnly = true)
	public Detalle_de_factura obtener(Integer id)
	{
		return repositorio.findById(id)
			.orElseThrow(() -> new RecursoNoEncontradoException("No existe un detalle de factura con el id " + id + "."));
	}

	@Transactional
	public Detalle_de_factura crear(DetalleDeFacturaRequest peticion)
	{
		LocalDateTime ahora = LocalDateTime.now();
		return repositorio.save(new Detalle_de_factura(peticion.facturaDelNegocioId(), peticion.descripcion(),
			peticion.servicioId() == null ? 0 : peticion.servicioId(), peticion.cantidadServicio() == null ? 0 : peticion.cantidadServicio(),
			peticion.productoDePapeleriaId() == null ? 0 : peticion.productoDePapeleriaId(), peticion.cantidadProducto() == null ? 0 : peticion.cantidadProducto(),
			peticion.ropaId() == null ? 0 : peticion.ropaId(), peticion.cantidadRopa() == null ? 0 : peticion.cantidadRopa(), ahora, ahora));
	}

	@Transactional
	public Detalle_de_factura actualizar(Integer id, DetalleDeFacturaRequest peticion)
	{
		Detalle_de_factura entidad = repositorio.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("No existe un detalle de factura con el id " + id + "."));
		entidad.setFacturaDelNegocioId(peticion.facturaDelNegocioId());
		entidad.setDescripcion(peticion.descripcion());
		entidad.setServicioId(peticion.servicioId() == null ? 0 : peticion.servicioId());
		entidad.setCantidadServicio(peticion.cantidadServicio() == null ? 0 : peticion.cantidadServicio());
		entidad.setProductoPapeleriaId(peticion.productoDePapeleriaId() == null ? 0 : peticion.productoDePapeleriaId());
		entidad.setCantidadProducto(peticion.cantidadProducto() == null ? 0 : peticion.cantidadProducto());
		entidad.setRopaId(peticion.ropaId() == null ? 0 : peticion.ropaId());
		entidad.setCantidadRopa(peticion.cantidadRopa() == null ? 0 : peticion.cantidadRopa());
		entidad.setFechaActualizacion(LocalDateTime.now());
		return repositorio.save(entidad);
	}

	@Transactional
	public void eliminar(Integer id)
	{
		repositorio.delete(repositorio.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("No existe un detalle de factura con el id " + id + ".")));
	}
}
