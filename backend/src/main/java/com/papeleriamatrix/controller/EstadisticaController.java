package com.papeleriamatrix.controller;

import com.papeleriamatrix.dto.EstadisticaDelNegocioRequest;
import com.papeleriamatrix.dto.EstadisticaDelNegocioResponse;
import com.papeleriamatrix.dto.EstadisticaResponse;
import com.papeleriamatrix.dto.RangoFechasRequest;
import com.papeleriamatrix.service.EstadisticaDelNegocioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/estadisticas")
@Tag(name = "Estadísticas del negocio")
@SecurityRequirement(name = "claveApi")
public class EstadisticaController
{
	private final EstadisticaDelNegocioService estadisticaService;

	public EstadisticaController(EstadisticaDelNegocioService estadisticaService)
	{
		this.estadisticaService = estadisticaService;
	}

	@GetMapping
	@Operation(summary = "Listar estadísticas del negocio")
	public List<EstadisticaDelNegocioResponse> listar()
	{
		return estadisticaService.listar().stream().map(EstadisticaDelNegocioResponse::desde).toList();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consultar una estadística del negocio por su ID")
	public EstadisticaDelNegocioResponse obtener(@PathVariable Integer id)
	{
		return EstadisticaDelNegocioResponse.desde(estadisticaService.obtener(id));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Crear una estadística del negocio")
	public EstadisticaDelNegocioResponse crear(@Valid @RequestBody EstadisticaDelNegocioRequest peticion)
	{
		return EstadisticaDelNegocioResponse.desde(estadisticaService.crear(peticion));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Actualizar una estadística del negocio")
	public EstadisticaDelNegocioResponse actualizar(@PathVariable Integer id, @Valid @RequestBody EstadisticaDelNegocioRequest peticion)
	{
		return EstadisticaDelNegocioResponse.desde(estadisticaService.actualizar(id, peticion));
	}

	@GetMapping("/resumen")
	@Operation(summary = "Consultar el resumen de estadísticas para un rango de fechas")
	public EstadisticaResponse resumir(
		@RequestParam("desde") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
		@RequestParam("hasta") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta)
	{
		return estadisticaService.resumir(desde, hasta);
	}

	@PostMapping("/calcular")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Calcular y guardar estadísticas para un rango de fechas")
	public EstadisticaResponse calcularYGuardar(@Valid @RequestBody RangoFechasRequest peticion)
	{
		return estadisticaService.calcularYGuardar(peticion.desde(), peticion.hasta());
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Eliminar una estadística del negocio")
	public void eliminar(@PathVariable Integer id)
	{
		estadisticaService.eliminar(id);
	}
}
