package com.papeleriamatrix.service;

import com.papeleriamatrix.common.Utils;
import com.papeleriamatrix.dto.ClaveDePermisoCambioRequest;
import com.papeleriamatrix.exception.RecursoDuplicadoException;
import com.papeleriamatrix.exception.RecursoNoEncontradoException;
import com.papeleriamatrix.model.Clave_de_permiso;
import com.papeleriamatrix.repository.ClaveDePermisoRepository;
import com.papeleriamatrix.dto.ClaveDePermisoRequest;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class ClaveDePermisoService
{
	private final ClaveDePermisoRepository repositorio;
	private final PasswordEncoder passwordEncoder;

	public ClaveDePermisoService(ClaveDePermisoRepository repositorio, PasswordEncoder passwordEncoder)
	{
		this.repositorio = repositorio;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional(readOnly = true)
	public List<Clave_de_permiso> listar()
	{
		return repositorio.findAll();
	}

	@Transactional(readOnly = true)
	public Clave_de_permiso obtener(Integer id)
	{
		return repositorio.findById(id)
			.orElseThrow(() -> new RecursoNoEncontradoException("No existe una clave de permiso con el id " + id + "."));
	}

	@Transactional
	public Clave_de_permiso crear(ClaveDePermisoRequest peticion)
	{
		if (repositorio.existsByUsuarioId(peticion.usuarioId()))
		{
			throw new RecursoDuplicadoException("El operador ya tiene una clave de permiso vigente.");
		}
		LocalDate ahora = LocalDate.now();
		return repositorio.save(new Clave_de_permiso(peticion.usuarioId(), codificarClave(peticion.clave()), ahora, ahora));
	}

	@Transactional
	public Clave_de_permiso actualizar(Integer id, ClaveDePermisoRequest peticion)
	{
		Clave_de_permiso entidad = repositorio.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("No existe una clave de permiso con el id " + id + "."));
		if (repositorio.existsByUsuarioIdAndIdClaveNot(peticion.usuarioId(), id))
		{
			throw new RecursoDuplicadoException("El operador ya tiene una clave de permiso vigente.");
		}
		entidad.setUsuarioId(peticion.usuarioId());
		if (peticion.clave() != null)
		{
			entidad.setClave(codificarClave(peticion.clave()));
		} else if (!Utils.esHashBCrypt(entidad.getClave()))
		{
			entidad.setClave(passwordEncoder.encode(entidad.getClave()));
		}
		entidad.setFechaActualizacion(LocalDate.now());
		return repositorio.save(entidad);
	}

	@Transactional
	public Clave_de_permiso cambiar(Integer id, ClaveDePermisoCambioRequest peticion)
	{
		Clave_de_permiso entidad = repositorio.findById(id)
			.orElseThrow(() -> new RecursoNoEncontradoException("No existe una clave de permiso con el id " + id + "."));
		entidad.setClave(codificarClave(peticion.nuevaClave()));
		entidad.setFechaActualizacion(LocalDate.now());
		return repositorio.save(entidad);
	}

	@Transactional
	public void eliminar(Integer id)
	{
		repositorio.delete(repositorio.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("No existe una clave de permiso con el id " + id + ".")));
	}

	private String codificarClave(String clave)
	{
		if (clave == null || clave.isBlank() || clave.length() < 10 || clave.length() > 30)
		{
			throw new IllegalArgumentException("La clave debe tener entre 10 y 30 caracteres.");
		}
		if (Utils.esHashBCrypt(clave))
		{
			throw new IllegalArgumentException("La clave debe tener entre 10 y 30 caracteres en texto plano.");
		}
		return passwordEncoder.encode(clave);
	}
}
