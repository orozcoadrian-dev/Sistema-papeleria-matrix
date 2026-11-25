package com.papeleriamatrix.service;

import com.papeleriamatrix.common.Utils;
import com.papeleriamatrix.exception.RecursoNoEncontradoException;
import com.papeleriamatrix.model.Administrador;
import com.papeleriamatrix.repository.AdministradorRepository;
import com.papeleriamatrix.dto.AdministradorRequest;
import com.papeleriamatrix.model.RolUsuario;
import java.time.LocalDateTime;
import java.util.List;
import com.papeleriamatrix.security.AlmacenDeSesiones;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class AdministradorService
{
	private final AdministradorRepository repositorio;
	private final PasswordEncoder passwordEncoder;
	private final AlmacenDeSesiones sesiones;

	public AdministradorService(AdministradorRepository repositorio, PasswordEncoder passwordEncoder, AlmacenDeSesiones sesiones)
	{
		this.sesiones = sesiones;
		this.repositorio = repositorio;
		this.passwordEncoder = passwordEncoder;
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
			peticion.numeroDocumento(), peticion.tipoDocumento(), peticion.nombreUsuario(), codificarContrasenia(peticion.contrasenia()),
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
		if (peticion.contrasenia() != null)
		{
			entidad.setContrasenia(codificarContrasenia(peticion.contrasenia()));
			// Con la contraseña nueva hay que volver a entrar en todos los equipos.
			sesiones.cerrarDelUsuario(id);
		} else if (!Utils.esHashBCrypt(entidad.getContrasenia()))
		{
			entidad.setContrasenia(passwordEncoder.encode(entidad.getContrasenia()));
		}
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

	private String codificarContrasenia(String contrasenia)
	{
		if (contrasenia == null || contrasenia.isBlank() || contrasenia.length() < 10 || contrasenia.length() > 255)
		{
			throw new IllegalArgumentException("La contraseña debe tener entre 10 y 255 caracteres.");
		}
		return Utils.esHashBCrypt(contrasenia) ? contrasenia : passwordEncoder.encode(contrasenia);
	}
}
