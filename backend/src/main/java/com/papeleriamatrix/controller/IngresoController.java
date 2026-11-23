package com.papeleriamatrix.controller;

import com.papeleriamatrix.dto.IngresoDelNegocioRequest;
import com.papeleriamatrix.dto.IngresoDelNegocioResponse;
import com.papeleriamatrix.dto.IngresoResponse;
import com.papeleriamatrix.dto.RangoFechasRequest;
import com.papeleriamatrix.service.IngresoDelNegocioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ingresos")
@Tag(name = "Ingresos del negocio")
@SecurityRequirement(name = "claveApi")
public class IngresoController
{
	private final IngresoDelNegocioService ingresoService;

	public IngresoController(IngresoDelNegocioService ingresoService)
	{
		this.ingresoService = ingresoService;
	}

	@GetMapping
	@Operation(summary = "Listar ingresos del negocio")
	public List<IngresoDelNegocioResponse> listar()
	{
		return ingresoService.listar().stream().map(IngresoDelNegocioResponse::desde).toList();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consultar un ingreso del negocio por su ID")
	public IngresoDelNegocioResponse obtener(@PathVariable Integer id)
	{
		return IngresoDelNegocioResponse.desde(ingresoService.obtener(id));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Crear un ingreso del negocio")
	public IngresoDelNegocioResponse crear(@Valid @RequestBody IngresoDelNegocioRequest peticion)
	{
		return IngresoDelNegocioResponse.desde(ingresoService.crear(peticion));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Actualizar un ingreso del negocio")
	public IngresoDelNegocioResponse actualizar(@PathVariable Integer id, @Valid @RequestBody IngresoDelNegocioRequest peticion)
	{
		return IngresoDelNegocioResponse.desde(ingresoService.actualizar(id, peticion));
	}

	@GetMapping("/resumen")
	@Operation(summary = "Consultar el resumen de ingresos para un rango de fechas")
	public IngresoResponse resumir(
		@RequestParam("desde") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
		@RequestParam("hasta") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta)
	{
		return ingresoService.resumir(desde, hasta);
	}

	@PostMapping("/calcular")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Calcular y guardar ingresos para un rango de fechas")
	public IngresoResponse calcularYGuardar(@Valid @RequestBody RangoFechasRequest peticion)
	{
		return ingresoService.calcularYGuardar(peticion.desde(), peticion.hasta());
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Eliminar un ingreso del negocio")
	public void eliminar(@PathVariable Integer id)
	{
		ingresoService.eliminar(id);
	}
}