package com.papeleriamatrix.controller;

import com.papeleriamatrix.dto.UsuarioRequest;
import com.papeleriamatrix.model.Usuario;
import com.papeleriamatrix.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios")
public class UsuarioController
{
	private final UsuarioService usuarioService;

	public UsuarioController(UsuarioService usuarioService)
	{
		this.usuarioService = usuarioService;
	}

	@GetMapping
	@Operation(summary = "Listar usuarios")
	public List<Usuario> listar()
	{
		return usuarioService.listar();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consultar un usuario por su id")
	public Usuario obtener(@PathVariable Integer id)
	{
		return usuarioService.obtener(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Crear un usuario", security = @SecurityRequirement(name = "claveApi"))
	public Usuario crear(@Valid @RequestBody UsuarioRequest peticion)
	{
		return usuarioService.crear(peticion);
	}

	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "Actualizar un usuario", security = @SecurityRequirement(name = "claveApi"))
	public Usuario actualizar(@PathVariable Integer id, @Valid @RequestBody UsuarioRequest peticion)
	{
		return usuarioService.actualizar(id, peticion);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Eliminar un usuario", security = @SecurityRequirement(name = "claveApi"))
	public void eliminar(@PathVariable Integer id)
	{
		usuarioService.eliminar(id);
	}
}
