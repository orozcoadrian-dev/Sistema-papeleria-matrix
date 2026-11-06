package com.papeleriamatrix.controller;

import com.papeleriamatrix.dto.EstadisticaDelNegocioRequest;
import com.papeleriamatrix.model.Estadistica_del_negocio;
import com.papeleriamatrix.service.EstadisticaDelNegocioService;
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
@RequestMapping("/api/estadisticas-negocio")
@Tag(name = "Estadísticas del negocio")
public class EstadisticaDelNegocioController
{
	private final EstadisticaDelNegocioService estadisticaDelNegocioService;

	public EstadisticaDelNegocioController(EstadisticaDelNegocioService estadisticaDelNegocioService)
	{
		this.estadisticaDelNegocioService = estadisticaDelNegocioService;
	}

	@GetMapping
	@Operation(summary = "Listar estadísticas del negocio")
	public List<Estadistica_del_negocio> listar()
	{
		return estadisticaDelNegocioService.listar();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consultar una estadística del negocio por su id")
	public Estadistica_del_negocio obtener(@PathVariable Integer id)
	{
		return estadisticaDelNegocioService.obtener(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Crear una estadística del negocio", security = @SecurityRequirement(name = "claveApi"))
	public Estadistica_del_negocio crear(@Valid @RequestBody EstadisticaDelNegocioRequest peticion)
	{
		return estadisticaDelNegocioService.crear(peticion);
	}

	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "Actualizar una estadística del negocio", security = @SecurityRequirement(name = "claveApi"))
	public Estadistica_del_negocio actualizar(@PathVariable Integer id, @Valid @RequestBody EstadisticaDelNegocioRequest peticion)
	{
		return estadisticaDelNegocioService.actualizar(id, peticion);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Eliminar una estadística del negocio", security = @SecurityRequirement(name = "claveApi"))
	public void eliminar(@PathVariable Integer id)
	{
		estadisticaDelNegocioService.eliminar(id);
	}
}
