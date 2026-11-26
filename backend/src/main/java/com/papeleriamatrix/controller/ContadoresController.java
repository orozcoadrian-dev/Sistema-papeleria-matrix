package com.papeleriamatrix.controller;

import com.papeleriamatrix.dto.ContadoresResponse;
import com.papeleriamatrix.service.ContadoresService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contadores")
@Tag(name = "Contadores", description = "Cuántos registros hay de cada cosa. Solo para la administradora.")
public class ContadoresController
{
	private final ContadoresService contadoresService;

	public ContadoresController(ContadoresService contadoresService)
	{
		this.contadoresService = contadoresService;
	}

	@GetMapping
	@Operation(summary = "Consultar los contadores del negocio",
		security = { @SecurityRequirement(name = "sesion"), @SecurityRequirement(name = "claveApi") })
	public ContadoresResponse contar()
	{
		return contadoresService.contar();
	}
}
