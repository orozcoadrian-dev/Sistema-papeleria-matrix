package com.papeleriamatrix.controller;

import com.papeleriamatrix.dto.HistorialIngresoDelNegocioRequest;
import com.papeleriamatrix.dto.HistorialIngresoDelNegocioResponse;
import com.papeleriamatrix.service.HistorialIngresoDelNegocioService;
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
@RequestMapping("/api/historiales-ingresos-negocio")
@Tag(name = "Historiales de ingresos del negocio")
@SecurityRequirement(name = "claveApi")
public class HistorialIngresoDelNegocioController
{
	private final HistorialIngresoDelNegocioService historialIngresoDelNegocioService;

	public HistorialIngresoDelNegocioController(HistorialIngresoDelNegocioService historialIngresoDelNegocioService)
	{
		this.historialIngresoDelNegocioService = historialIngresoDelNegocioService;
	}

	@GetMapping
	@Operation(summary = "Listar historiales de ingresos del negocio")
	public List<HistorialIngresoDelNegocioResponse> listar()
	{
		return historialIngresoDelNegocioService.listar().stream().map(HistorialIngresoDelNegocioResponse::desde).toList();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consultar un historial de ingresos por su id")
	public HistorialIngresoDelNegocioResponse obtener(@PathVariable Integer id)
	{
		return HistorialIngresoDelNegocioResponse.desde(historialIngresoDelNegocioService.obtener(id));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Crear un historial de ingresos")
	public HistorialIngresoDelNegocioResponse crear(@Valid @RequestBody HistorialIngresoDelNegocioRequest peticion)
	{
		return HistorialIngresoDelNegocioResponse.desde(historialIngresoDelNegocioService.crear(peticion));
	}

	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "Actualizar un historial de ingresos")
	public HistorialIngresoDelNegocioResponse actualizar(@PathVariable Integer id, @Valid @RequestBody HistorialIngresoDelNegocioRequest peticion)
	{
		return HistorialIngresoDelNegocioResponse.desde(historialIngresoDelNegocioService.actualizar(id, peticion));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Eliminar un historial de ingresos")
	public void eliminar(@PathVariable Integer id)
	{
		historialIngresoDelNegocioService.eliminar(id);
	}
}
