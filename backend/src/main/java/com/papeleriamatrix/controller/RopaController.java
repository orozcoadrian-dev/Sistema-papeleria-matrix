package com.papeleriamatrix.controller;

import com.papeleriamatrix.dto.RopaResponse;
import com.papeleriamatrix.dto.RopaRequest;
import com.papeleriamatrix.service.RopaService;
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
@RequestMapping("/api/ropa")
@Tag(name = "Ropa", description = "Catálogo de ropa de Pulguero Glam para el público. Solo consulta.")
public class RopaController
{
	private final RopaService ropaService;

	public RopaController(RopaService ropaService)
	{
		this.ropaService = ropaService;
	}

	@GetMapping
	@Operation(summary = "Listar las prendas")
	public List<RopaResponse> listar()
	{
		return ropaService.listar();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consultar una prenda por su id")
	public RopaResponse obtener(@PathVariable Integer id)
	{
		return ropaService.obtener(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Crear una prenda", security = @SecurityRequirement(name = "claveApi"))
	public RopaResponse crear(@Valid @RequestBody RopaRequest peticion)
	{
		return ropaService.crear(peticion);
	}

	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "Actualizar una prenda", security = @SecurityRequirement(name = "claveApi"))
	public RopaResponse actualizar(@PathVariable Integer id, @Valid @RequestBody RopaRequest peticion)
	{
		return ropaService.actualizar(id, peticion);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Eliminar una prenda", security = @SecurityRequirement(name = "claveApi"))
	public void eliminar(@PathVariable Integer id)
	{
		ropaService.eliminar(id);
	}
}
