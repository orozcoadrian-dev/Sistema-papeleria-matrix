package com.papeleriamatrix.service;

import com.papeleriamatrix.exception.RecursoNoEncontradoException;
import com.papeleriamatrix.model.Usuario;
import com.papeleriamatrix.repository.UsuarioRepository;
import com.papeleriamatrix.dto.UsuarioRequest;
import com.papeleriamatrix.model.RolUsuario;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService
{
	private final UsuarioRepository repositorio;

	public UsuarioService(UsuarioRepository repositorio)
	{
		this.repositorio = repositorio;
	}

	@Transactional(readOnly = true)
	public List<Usuario> listar()
	{
		return repositorio.findAll();
	}

	@Transactional(readOnly = true)
	public Usuario obtener(Integer id)
	{
		return repositorio.findById(id)
			.orElseThrow(() -> new RecursoNoEncontradoException("No existe un usuario con el id " + id + "."));
	}

	@Transactional
	public Usuario crear(UsuarioRequest peticion)
	{
		Usuario usuario = new Usuario(peticion.primerNombre(), peticion.segundoNombre(), peticion.primerApellido(), peticion.segundoApellido(),
			peticion.numeroDocumento(), peticion.tipoDocumento(), peticion.nombreUsuario(), peticion.contrasenia(),
			RolUsuario.fromString(peticion.rol()), peticion.email(), peticion.telefono(), LocalDateTime.now());
		return repositorio.save(usuario);
	}

	@Transactional
	public Usuario actualizar(Integer id, UsuarioRequest peticion)
	{
		Usuario usuario = repositorio.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("No existe un usuario con el id " + id + "."));
		usuario.setPrimerNombre(peticion.primerNombre());
		usuario.setSegundoNombre(peticion.segundoNombre());
		usuario.setPrimerApellido(peticion.primerApellido());
		usuario.setSegundoApellido(peticion.segundoApellido());
		usuario.setNumeroDocumento(peticion.numeroDocumento());
		usuario.setTipoDocumento(peticion.tipoDocumento());
		usuario.setNombreUsuario(peticion.nombreUsuario());
		usuario.setContrasenia(peticion.contrasenia());
		usuario.setRol(RolUsuario.fromString(peticion.rol()));
		usuario.setEmail(peticion.email());
		usuario.setTelefono(peticion.telefono());
		return repositorio.save(usuario);
	}

	@Transactional
	public void eliminar(Integer id)
	{
		repositorio.delete(repositorio.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("No existe un usuario con el id " + id + ".")));
	}
}
