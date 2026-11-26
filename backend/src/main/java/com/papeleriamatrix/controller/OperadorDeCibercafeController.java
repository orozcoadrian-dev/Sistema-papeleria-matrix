package com.papeleriamatrix.controller;

import com.papeleriamatrix.dto.OperadorDeCibercafeRequest;
import com.papeleriamatrix.dto.OperadorResponse;
import com.papeleriamatrix.service.OperadorDeCibercafeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
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
	@Operation(summary = "Listar operadores de cibercafé", security = @SecurityRequirement(name = "claveApi"))
	public List<OperadorResponse> listar()
	{
		return operadorDeCibercafeService.listar().stream()
			.map(OperadorResponse::desde)
			.toList();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consultar un operador de cibercafé por su id", security = @SecurityRequirement(name = "claveApi"))
	public OperadorResponse obtener(@PathVariable Integer id)
	{
		return OperadorResponse.desde(operadorDeCibercafeService.obtener(id));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Contratar un operador de cibercafé", security = @SecurityRequirement(name = "claveApi"))
	public OperadorResponse crear(@Valid @RequestBody OperadorDeCibercafeRequest peticion)
	{
		return OperadorResponse.desde(operadorDeCibercafeService.crear(peticion));
	}

	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "Actualizar un operador de cibercafé", security = @SecurityRequirement(name = "claveApi"))
	public OperadorResponse actualizar(@PathVariable Integer id, @Valid @RequestBody OperadorDeCibercafeRequest peticion)
	{
		return OperadorResponse.desde(operadorDeCibercafeService.actualizar(id, peticion));
	}

	@PutMapping("/{id}/estado")
	@Operation(summary = "Cambiar el estado de un operador", description = "Activo, Suspendido o Despedido. Quien no está activo no puede entrar.",
		security = @SecurityRequirement(name = "claveApi"))
	public OperadorResponse cambiarEstado(@PathVariable Integer id, @Valid @RequestBody com.papeleriamatrix.dto.EstadoOperadorRequest peticion)
	{
		return OperadorResponse.desde(operadorDeCibercafeService.cambiarEstado(id, com.papeleriamatrix.model.EstadoUsuario.desde(peticion.estado())));
	}

	@org.springframework.web.bind.annotation.DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Retirar un operador", description = "Queda como Despedido. Sus datos y sus ventas se conservan.",
		security = @SecurityRequirement(name = "claveApi"))
	public void eliminar(@PathVariable Integer id)
	{
		operadorDeCibercafeService.eliminar(id);
	}
}
