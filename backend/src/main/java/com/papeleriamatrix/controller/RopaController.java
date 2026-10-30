package com.papeleriamatrix.controller;

import com.papeleriamatrix.dto.RopaResponse;
import com.papeleriamatrix.service.RopaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ropa")
@Tag(name = "Ropa", description = "Catálogo de ropa de Pulguero Glam para el público. Solo consulta.")
public class RopaController
{
	private final RopaService ropaService;

	public RopaController(RopaService ropaService)
	{
		this.ropaService = ropaService;
	}

	@GetMapping
	@Operation(summary = "Listar las prendas")
	public List<RopaResponse> listar()
	{
		return ropaService.listar();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consultar una prenda por su id")
	public RopaResponse obtener(@PathVariable Integer id)
	{
		return ropaService.obtener(id);
	}
}
