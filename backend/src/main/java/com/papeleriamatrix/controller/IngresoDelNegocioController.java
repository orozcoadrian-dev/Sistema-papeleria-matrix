package com.papeleriamatrix.controller;

import com.papeleriamatrix.dto.IngresoDelNegocioRequest;
import com.papeleriamatrix.model.Ingreso_del_negocio;
import com.papeleriamatrix.service.IngresoDelNegocioService;
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
@RequestMapping("/api/ingresos-negocio")
@Tag(name = "Ingresos del negocio")
public class IngresoDelNegocioController
{
	private final IngresoDelNegocioService ingresoDelNegocioService;

	public IngresoDelNegocioController(IngresoDelNegocioService ingresoDelNegocioService)
	{
		this.ingresoDelNegocioService = ingresoDelNegocioService;
	}

	@GetMapping
	@Operation(summary = "Listar ingresos del negocio")
	public List<Ingreso_del_negocio> listar()
	{
		return ingresoDelNegocioService.listar();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consultar un ingreso del negocio por su id")
	public Ingreso_del_negocio obtener(@PathVariable Integer id)
	{
		return ingresoDelNegocioService.obtener(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Crear un ingreso del negocio", security = @SecurityRequirement(name = "claveApi"))
	public Ingreso_del_negocio crear(@Valid @RequestBody IngresoDelNegocioRequest peticion)
	{
		return ingresoDelNegocioService.crear(peticion);
	}

	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "Actualizar un ingreso del negocio", security = @SecurityRequirement(name = "claveApi"))
	public Ingreso_del_negocio actualizar(@PathVariable Integer id, @Valid @RequestBody IngresoDelNegocioRequest peticion)
	{
		return ingresoDelNegocioService.actualizar(id, peticion);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Eliminar un ingreso del negocio", security = @SecurityRequirement(name = "claveApi"))
	public void eliminar(@PathVariable Integer id)
	{
		ingresoDelNegocioService.eliminar(id);
	}
}
