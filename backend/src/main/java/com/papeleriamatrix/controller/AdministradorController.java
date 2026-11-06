package com.papeleriamatrix.controller;

import com.papeleriamatrix.dto.AdministradorRequest;
import com.papeleriamatrix.model.Administrador;
import com.papeleriamatrix.service.AdministradorService;
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
@RequestMapping("/api/administradores")
@Tag(name = "Administradores")
public class AdministradorController
{
	private final AdministradorService administradorService;

	public AdministradorController(AdministradorService administradorService)
	{
		this.administradorService = administradorService;
	}

	@GetMapping
	@Operation(summary = "Listar administradores")
	public List<Administrador> listar()
	{
		return administradorService.listar();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consultar un administrador por su id")
	public Administrador obtener(@PathVariable Integer id)
	{
		return administradorService.obtener(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Crear un administrador", security = @SecurityRequirement(name = "claveApi"))
	public Administrador crear(@Valid @RequestBody AdministradorRequest peticion)
	{
		return administradorService.crear(peticion);
	}

	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "Actualizar un administrador", security = @SecurityRequirement(name = "claveApi"))
	public Administrador actualizar(@PathVariable Integer id, @Valid @RequestBody AdministradorRequest peticion)
	{
		return administradorService.actualizar(id, peticion);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Eliminar un administrador", security = @SecurityRequirement(name = "claveApi"))
	public void eliminar(@PathVariable Integer id)
	{
		administradorService.eliminar(id);
	}
}
