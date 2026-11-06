package com.papeleriamatrix.controller;

import com.papeleriamatrix.dto.HistorialEstadisticaDelNegocioRequest;
import com.papeleriamatrix.model.Historial_estadistica_del_negocio;
import com.papeleriamatrix.service.HistorialEstadisticaDelNegocioService;
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
@RequestMapping("/api/historiales-estadisticas-negocio")
@Tag(name = "Historiales de estadísticas del negocio")
public class HistorialEstadisticaDelNegocioController
{
	private final HistorialEstadisticaDelNegocioService historialEstadisticaDelNegocioService;

	public HistorialEstadisticaDelNegocioController(HistorialEstadisticaDelNegocioService historialEstadisticaDelNegocioService)
	{
		this.historialEstadisticaDelNegocioService = historialEstadisticaDelNegocioService;
	}

	@GetMapping
	@Operation(summary = "Listar historiales de estadísticas del negocio")
	public List<Historial_estadistica_del_negocio> listar()
	{
		return historialEstadisticaDelNegocioService.listar();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consultar un historial de estadísticas por su id")
	public Historial_estadistica_del_negocio obtener(@PathVariable Integer id)
	{
		return historialEstadisticaDelNegocioService.obtener(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Crear un historial de estadísticas", security = @SecurityRequirement(name = "claveApi"))
	public Historial_estadistica_del_negocio crear(@Valid @RequestBody HistorialEstadisticaDelNegocioRequest peticion)
	{
		return historialEstadisticaDelNegocioService.crear(peticion);
	}

	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "Actualizar un historial de estadísticas", security = @SecurityRequirement(name = "claveApi"))
	public Historial_estadistica_del_negocio actualizar(@PathVariable Integer id, @Valid @RequestBody HistorialEstadisticaDelNegocioRequest peticion)
	{
		return historialEstadisticaDelNegocioService.actualizar(id, peticion);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Eliminar un historial de estadísticas", security = @SecurityRequirement(name = "claveApi"))
	public void eliminar(@PathVariable Integer id)
	{
		historialEstadisticaDelNegocioService.eliminar(id);
	}
}
