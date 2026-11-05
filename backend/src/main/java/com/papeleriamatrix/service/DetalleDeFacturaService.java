package com.papeleriamatrix.service;

import com.papeleriamatrix.exception.RecursoNoEncontradoException;
import com.papeleriamatrix.model.Detalle_de_factura;
import com.papeleriamatrix.repository.DetalleDeFacturaRepository;
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
}
