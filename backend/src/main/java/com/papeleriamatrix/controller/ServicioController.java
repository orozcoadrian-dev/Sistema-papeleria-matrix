package com.papeleriamatrix.controller;

import com.papeleriamatrix.dto.ServicioRequest;
import com.papeleriamatrix.dto.ServicioResponse;
import com.papeleriamatrix.service.ServicioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/servicios")
@Tag(name = "Servicios", description = "Servicios que ofrece el negocio. Consultar es público; crear, modificar y borrar exige la clave de la API.")
public class ServicioController
{
	private final ServicioService servicioService;

	public ServicioController(ServicioService servicioService)
	{
		this.servicioService = servicioService;
	}

	@GetMapping
	@Operation(summary = "Listar los servicios")
	public List<ServicioResponse> listar()
	{
		return servicioService.listar();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consultar un servicio por su id")
	public ServicioResponse obtener(@PathVariable Integer id)
	{
		return servicioService.obtener(id);
	}

	@PostMapping
	@Operation(summary = "Crear un servicio", security = @SecurityRequirement(name = "claveApi"))
	public ResponseEntity<ServicioResponse> crear(@Valid @RequestBody ServicioRequest peticion)
	{
		ServicioResponse creado = servicioService.crear(peticion);
		URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(creado.id()).toUri();
		return ResponseEntity.created(ubicacion).body(creado);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Actualizar un servicio", security = @SecurityRequirement(name = "claveApi"))
	public ServicioResponse actualizar(@PathVariable Integer id, @Valid @RequestBody ServicioRequest peticion)
	{
		return servicioService.actualizar(id, peticion);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Eliminar un servicio", security = @SecurityRequirement(name = "claveApi"))
	public void eliminar(@PathVariable Integer id)
	{
		servicioService.eliminar(id);
	}
}
