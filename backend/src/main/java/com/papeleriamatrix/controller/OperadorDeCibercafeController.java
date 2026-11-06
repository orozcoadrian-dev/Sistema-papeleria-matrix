package com.papeleriamatrix.controller;

import com.papeleriamatrix.dto.OperadorDeCibercafeRequest;
import com.papeleriamatrix.model.Operador_de_cibercafe;
import com.papeleriamatrix.service.OperadorDeCibercafeService;
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
@RequestMapping("/api/operadores-cibercafe")
@Tag(name = "Operadores de cibercafé")
public class OperadorDeCibercafeController
{
	private final OperadorDeCibercafeService operadorDeCibercafeService;

	public OperadorDeCibercafeController(OperadorDeCibercafeService operadorDeCibercafeService)
	{
		this.operadorDeCibercafeService = operadorDeCibercafeService;
	}

	@GetMapping
	@Operation(summary = "Listar operadores de cibercafé")
	public List<Operador_de_cibercafe> listar()
	{
		return operadorDeCibercafeService.listar();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consultar un operador de cibercafé por su id")
	public Operador_de_cibercafe obtener(@PathVariable Integer id)
	{
		return operadorDeCibercafeService.obtener(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Crear un operador de cibercafé", security = @SecurityRequirement(name = "claveApi"))
	public Operador_de_cibercafe crear(@Valid @RequestBody OperadorDeCibercafeRequest peticion)
	{
		return operadorDeCibercafeService.crear(peticion);
	}

	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "Actualizar un operador de cibercafé", security = @SecurityRequirement(name = "claveApi"))
	public Operador_de_cibercafe actualizar(@PathVariable Integer id, @Valid @RequestBody OperadorDeCibercafeRequest peticion)
	{
		return operadorDeCibercafeService.actualizar(id, peticion);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Eliminar un operador de cibercafé", security = @SecurityRequirement(name = "claveApi"))
	public void eliminar(@PathVariable Integer id)
	{
		operadorDeCibercafeService.eliminar(id);
	}
}
