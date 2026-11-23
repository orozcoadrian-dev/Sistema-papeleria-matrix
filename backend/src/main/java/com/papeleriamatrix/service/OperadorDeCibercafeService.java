package com.papeleriamatrix.service;

import com.papeleriamatrix.common.Utils;
import com.papeleriamatrix.exception.RecursoNoEncontradoException;
import com.papeleriamatrix.model.Operador_de_cibercafe;
import com.papeleriamatrix.repository.OperadorDeCibercafeRepository;
import com.papeleriamatrix.dto.OperadorDeCibercafeRequest;
import com.papeleriamatrix.model.RolUsuario;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class OperadorDeCibercafeService
{
	private final OperadorDeCibercafeRepository repositorio;
	private final PasswordEncoder passwordEncoder;

	public OperadorDeCibercafeService(OperadorDeCibercafeRepository repositorio, PasswordEncoder passwordEncoder)
	{
		this.repositorio = repositorio;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional(readOnly = true)
	public List<Operador_de_cibercafe> listar()
	{
		return repositorio.findAll();
	}

	@Transactional(readOnly = true)
	public Operador_de_cibercafe obtener(Integer id)
	{
		return repositorio.findById(id)
			.orElseThrow(() -> new RecursoNoEncontradoException("No existe un operador de cibercafé con el id " + id + "."));
	}

	@Transactional
	public Operador_de_cibercafe crear(OperadorDeCibercafeRequest peticion)
	{
		return repositorio.save(new Operador_de_cibercafe(peticion.primerNombre(), peticion.segundoNombre(), peticion.primerApellido(), peticion.segundoApellido(),
			peticion.numeroDocumento(), peticion.tipoDocumento(), peticion.nombreUsuario(), codificarContrasenia(peticion.contrasenia()),
			RolUsuario.fromString(peticion.rol()), peticion.email(), peticion.telefono(), LocalDateTime.now(),
			peticion.salario(), peticion.horario(), peticion.direccion(), peticion.fechaContrato()));
	}

	@Transactional
	public Operador_de_cibercafe actualizar(Integer id, OperadorDeCibercafeRequest peticion)
	{
		Operador_de_cibercafe entidad = repositorio.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("No existe un operador de cibercafé con el id " + id + "."));
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
		} else if (!Utils.esHashBCrypt(entidad.getContrasenia()))
		{
			entidad.setContrasenia(passwordEncoder.encode(entidad.getContrasenia()));
		}
		entidad.setRol(RolUsuario.fromString(peticion.rol()));
		entidad.setEmail(peticion.email());
		entidad.setTelefono(peticion.telefono());
		entidad.setSalario(peticion.salario());
		entidad.setHorario(peticion.horario());
		entidad.setDireccion(peticion.direccion());
		entidad.setFechaContrato(peticion.fechaContrato());
		return repositorio.save(entidad);
	}

	@Transactional
	public void eliminar(Integer id)
	{
		repositorio.delete(repositorio.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("No existe un operador de cibercafé con el id " + id + ".")));
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
