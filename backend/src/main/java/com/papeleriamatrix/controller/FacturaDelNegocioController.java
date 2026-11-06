package com.papeleriamatrix.controller;

import com.papeleriamatrix.dto.FacturaDelNegocioRequest;
import com.papeleriamatrix.model.Factura_del_negocio;
import com.papeleriamatrix.service.FacturaDelNegocioService;
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
@RequestMapping("/api/facturas-negocio")
@Tag(name = "Facturas del negocio")
public class FacturaDelNegocioController
{
	private final FacturaDelNegocioService facturaDelNegocioService;

	public FacturaDelNegocioController(FacturaDelNegocioService facturaDelNegocioService)
	{
		this.facturaDelNegocioService = facturaDelNegocioService;
	}

	@GetMapping
	@Operation(summary = "Listar facturas del negocio")
	public List<Factura_del_negocio> listar()
	{
		return facturaDelNegocioService.listar();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consultar una factura del negocio por su id")
	public Factura_del_negocio obtener(@PathVariable Integer id)
	{
		return facturaDelNegocioService.obtener(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Crear una factura del negocio", security = @SecurityRequirement(name = "claveApi"))
	public Factura_del_negocio crear(@Valid @RequestBody FacturaDelNegocioRequest peticion)
	{
		return facturaDelNegocioService.crear(peticion);
	}

	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "Actualizar una factura del negocio", security = @SecurityRequirement(name = "claveApi"))
	public Factura_del_negocio actualizar(@PathVariable Integer id, @Valid @RequestBody FacturaDelNegocioRequest peticion)
	{
		return facturaDelNegocioService.actualizar(id, peticion);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Eliminar una factura del negocio", security = @SecurityRequirement(name = "claveApi"))
	public void eliminar(@PathVariable Integer id)
	{
		facturaDelNegocioService.eliminar(id);
	}
}
