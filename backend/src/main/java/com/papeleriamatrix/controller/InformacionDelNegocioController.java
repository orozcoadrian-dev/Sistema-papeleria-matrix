package com.papeleriamatrix.controller;

import com.papeleriamatrix.dto.InformacionDelNegocioRequest;
import com.papeleriamatrix.model.Informacion_del_negocio;
import com.papeleriamatrix.service.InformacionDelNegocioService;
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
@RequestMapping("/api/informacion-negocio")
@Tag(name = "Información del negocio")
public class InformacionDelNegocioController
{
	private final InformacionDelNegocioService informacionDelNegocioService;

	public InformacionDelNegocioController(InformacionDelNegocioService informacionDelNegocioService)
	{
		this.informacionDelNegocioService = informacionDelNegocioService;
	}

	@GetMapping
	@Operation(summary = "Listar información del negocio")
	public List<Informacion_del_negocio> listar()
	{
		return informacionDelNegocioService.listar();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consultar la información del negocio por su id")
	public Informacion_del_negocio obtener(@PathVariable Integer id)
	{
		return informacionDelNegocioService.obtener(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Crear la información del negocio", security = @SecurityRequirement(name = "claveApi"))
	public Informacion_del_negocio crear(@Valid @RequestBody InformacionDelNegocioRequest peticion)
	{
		return informacionDelNegocioService.crear(peticion);
	}

	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "Actualizar la información del negocio", security = @SecurityRequirement(name = "claveApi"))
	public Informacion_del_negocio actualizar(@PathVariable Integer id, @Valid @RequestBody InformacionDelNegocioRequest peticion)
	{
		return informacionDelNegocioService.actualizar(id, peticion);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Eliminar la información del negocio", security = @SecurityRequirement(name = "claveApi"))
	public void eliminar(@PathVariable Integer id)
	{
		informacionDelNegocioService.eliminar(id);
	}
}
