package com.papeleriamatrix.controller;

import com.papeleriamatrix.dto.ClaveDePermisoRequest;
import com.papeleriamatrix.model.Clave_de_permiso;
import com.papeleriamatrix.service.ClaveDePermisoService;
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
@RequestMapping("/api/claves-permiso")
@Tag(name = "Claves de permiso")
public class ClaveDePermisoController
{
	private final ClaveDePermisoService claveDePermisoService;

	public ClaveDePermisoController(ClaveDePermisoService claveDePermisoService)
	{
		this.claveDePermisoService = claveDePermisoService;
	}

	@GetMapping
	@Operation(summary = "Listar claves de permiso")
	public List<Clave_de_permiso> listar()
	{
		return claveDePermisoService.listar();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consultar una clave de permiso por su id")
	public Clave_de_permiso obtener(@PathVariable Integer id)
	{
		return claveDePermisoService.obtener(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Crear una clave de permiso", security = @SecurityRequirement(name = "claveApi"))
	public Clave_de_permiso crear(@Valid @RequestBody ClaveDePermisoRequest peticion)
	{
		return claveDePermisoService.crear(peticion);
	}

	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "Actualizar una clave de permiso", security = @SecurityRequirement(name = "claveApi"))
	public Clave_de_permiso actualizar(@PathVariable Integer id, @Valid @RequestBody ClaveDePermisoRequest peticion)
	{
		return claveDePermisoService.actualizar(id, peticion);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Eliminar una clave de permiso", security = @SecurityRequirement(name = "claveApi"))
	public void eliminar(@PathVariable Integer id)
	{
		claveDePermisoService.eliminar(id);
	}
}
