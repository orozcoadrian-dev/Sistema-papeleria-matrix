package com.papeleriamatrix.service;

import com.papeleriamatrix.exception.RecursoNoEncontradoException;
import com.papeleriamatrix.model.Informacion_del_negocio;
import com.papeleriamatrix.repository.InformacionDelNegocioRepository;
import com.papeleriamatrix.dto.InformacionDelNegocioRequest;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InformacionDelNegocioService
{
	private final InformacionDelNegocioRepository repositorio;

	public InformacionDelNegocioService(InformacionDelNegocioRepository repositorio)
	{
		this.repositorio = repositorio;
	}

	@Transactional(readOnly = true)
	public List<Informacion_del_negocio> listar()
	{
		return repositorio.findByActivoTrue();
	}

	@Transactional(readOnly = true)
	public Informacion_del_negocio obtener(Integer id)
	{
		return buscar(id);
	}

	@Transactional
	public Informacion_del_negocio crear(InformacionDelNegocioRequest peticion)
	{
		LocalDate ahora = LocalDate.now();
		return repositorio.save(new Informacion_del_negocio(peticion.usuarioId(), peticion.valorInformacion(), peticion.tipoInformacion(), peticion.informacion(), ahora, ahora));
	}

	@Transactional
	public Informacion_del_negocio actualizar(Integer id, InformacionDelNegocioRequest peticion)
	{
		Informacion_del_negocio entidad = buscar(id);
		entidad.setUsuarioId(peticion.usuarioId());
		entidad.setValorInformacion(peticion.valorInformacion());
		entidad.setTipoInformacion(peticion.tipoInformacion());
		entidad.setInformacion(peticion.informacion());
		entidad.setFechaActualizacion(LocalDate.now());
		return repositorio.save(entidad);
	}

	/** Deja de mostrarse en la página y en el panel, pero queda guardada en la base. */
	@Transactional
	public void eliminar(Integer id, String motivo)
	{
		buscar(id).retirar(motivo);
	}

	private Informacion_del_negocio buscar(Integer id)
	{
		return repositorio.findById(id)
			.filter(Informacion_del_negocio::isActivo)
			.orElseThrow(() -> new RecursoNoEncontradoException("No existe una información del negocio con el id " + id + "."));
	}
}
