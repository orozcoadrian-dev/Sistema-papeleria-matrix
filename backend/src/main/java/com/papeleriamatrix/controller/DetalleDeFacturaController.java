package com.papeleriamatrix.controller;

import com.papeleriamatrix.dto.DetalleDeFacturaRequest;
import com.papeleriamatrix.model.Detalle_de_factura;
import com.papeleriamatrix.service.DetalleDeFacturaService;
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
@RequestMapping("/api/detalles-factura")
@Tag(name = "Detalles de factura")
public class DetalleDeFacturaController
{
	private final DetalleDeFacturaService detalleDeFacturaService;

	public DetalleDeFacturaController(DetalleDeFacturaService detalleDeFacturaService)
	{
		this.detalleDeFacturaService = detalleDeFacturaService;
	}

	@GetMapping
	@Operation(summary = "Listar detalles de factura")
	public List<Detalle_de_factura> listar()
	{
		return detalleDeFacturaService.listar();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consultar un detalle de factura por su id")
	public Detalle_de_factura obtener(@PathVariable Integer id)
	{
		return detalleDeFacturaService.obtener(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Crear un detalle de factura", security = @SecurityRequirement(name = "claveApi"))
	public Detalle_de_factura crear(@Valid @RequestBody DetalleDeFacturaRequest peticion)
	{
		return detalleDeFacturaService.crear(peticion);
	}

	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "Actualizar un detalle de factura", security = @SecurityRequirement(name = "claveApi"))
	public Detalle_de_factura actualizar(@PathVariable Integer id, @Valid @RequestBody DetalleDeFacturaRequest peticion)
	{
		return detalleDeFacturaService.actualizar(id, peticion);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Eliminar un detalle de factura", security = @SecurityRequirement(name = "claveApi"))
	public void eliminar(@PathVariable Integer id)
	{
		detalleDeFacturaService.eliminar(id);
	}
}
