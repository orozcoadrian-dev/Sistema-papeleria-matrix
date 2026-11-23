package com.papeleriamatrix.service;

import com.papeleriamatrix.common.Utils;
import com.papeleriamatrix.exception.RecursoNoEncontradoException;
import com.papeleriamatrix.model.Usuario;
import com.papeleriamatrix.repository.UsuarioRepository;
import com.papeleriamatrix.dto.UsuarioRequest;
import com.papeleriamatrix.model.RolUsuario;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UsuarioService
{
	private final UsuarioRepository repositorio;
	private final PasswordEncoder passwordEncoder;

	public UsuarioService(UsuarioRepository repositorio, PasswordEncoder passwordEncoder)
	{
		this.repositorio = repositorio;
		this.passwordEncoder = passwordEncoder;
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
			peticion.numeroDocumento(), peticion.tipoDocumento(), peticion.nombreUsuario(), codificarContrasenia(peticion.contrasenia()),
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
		if (peticion.contrasenia() != null)
		{
			usuario.setContrasenia(codificarContrasenia(peticion.contrasenia()));
		} else if (!Utils.esHashBCrypt(usuario.getContrasenia()))
		{
			usuario.setContrasenia(passwordEncoder.encode(usuario.getContrasenia()));
		}
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

	private String codificarContrasenia(String contrasenia)
	{
		if (contrasenia == null || contrasenia.isBlank() || contrasenia.length() < 10 || contrasenia.length() > 255)
		{
			throw new IllegalArgumentException("La contraseña debe tener entre 10 y 255 caracteres.");
		}
		return Utils.esHashBCrypt(contrasenia) ? contrasenia : passwordEncoder.encode(contrasenia);
	}
}
