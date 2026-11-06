package com.papeleriamatrix.service;

import com.papeleriamatrix.exception.RecursoNoEncontradoException;
import com.papeleriamatrix.model.Administrador;
import com.papeleriamatrix.repository.AdministradorRepository;
import com.papeleriamatrix.dto.AdministradorRequest;
import com.papeleriamatrix.model.RolUsuario;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdministradorService
{
	private final AdministradorRepository repositorio;

	public AdministradorService(AdministradorRepository repositorio)
	{
		this.repositorio = repositorio;
	}

	@Transactional(readOnly = true)
	public List<Administrador> listar()
	{
		return repositorio.findAll();
	}

	@Transactional(readOnly = true)
	public Administrador obtener(Integer id)
	{
		return repositorio.findById(id)
			.orElseThrow(() -> new RecursoNoEncontradoException("No existe un administrador con el id " + id + "."));
	}

	@Transactional
	public Administrador crear(AdministradorRequest peticion)
	{
		return repositorio.save(new Administrador(peticion.primerNombre(), peticion.segundoNombre(), peticion.primerApellido(), peticion.segundoApellido(),
			peticion.numeroDocumento(), peticion.tipoDocumento(), peticion.nombreUsuario(), peticion.contrasenia(),
			RolUsuario.fromString(peticion.rol()), peticion.email(), peticion.telefono(), LocalDateTime.now(),
			peticion.fechaPosicionamiento(), peticion.areaAcceso()));
	}

	@Transactional
	public Administrador actualizar(Integer id, AdministradorRequest peticion)
	{
		Administrador entidad = repositorio.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("No existe un administrador con el id " + id + "."));
		entidad.setPrimerNombre(peticion.primerNombre());
		entidad.setSegundoNombre(peticion.segundoNombre());
		entidad.setPrimerApellido(peticion.primerApellido());
		entidad.setSegundoApellido(peticion.segundoApellido());
		entidad.setNumeroDocumento(peticion.numeroDocumento());
		entidad.setTipoDocumento(peticion.tipoDocumento());
		entidad.setNombreUsuario(peticion.nombreUsuario());
		entidad.setContrasenia(peticion.contrasenia());
		entidad.setRol(RolUsuario.fromString(peticion.rol()));
		entidad.setEmail(peticion.email());
		entidad.setTelefono(peticion.telefono());
		entidad.setFechaPosicionamiento(peticion.fechaPosicionamiento());
		entidad.setAreaAcceso(peticion.areaAcceso());
		return repositorio.save(entidad);
	}

	@Transactional
	public void eliminar(Integer id)
	{
		repositorio.delete(repositorio.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("No existe un administrador con el id " + id + ".")));
	}
}
