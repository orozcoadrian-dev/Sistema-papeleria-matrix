package com.papeleriamatrix.controller;

import com.papeleriamatrix.dto.ReporteDelNegocioRequest;
import com.papeleriamatrix.model.Reporte_del_negocio;
import com.papeleriamatrix.service.ReporteDelNegocioService;
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
@RequestMapping("/api/reportes-negocio")
@Tag(name = "Reportes del negocio")
public class ReporteDelNegocioController
{
	private final ReporteDelNegocioService reporteDelNegocioService;

	public ReporteDelNegocioController(ReporteDelNegocioService reporteDelNegocioService)
	{
		this.reporteDelNegocioService = reporteDelNegocioService;
	}

	@GetMapping
	@Operation(summary = "Listar reportes del negocio")
	public List<Reporte_del_negocio> listar()
	{
		return reporteDelNegocioService.listar();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consultar un reporte del negocio por su id")
	public Reporte_del_negocio obtener(@PathVariable Integer id)
	{
		return reporteDelNegocioService.obtener(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Crear un reporte del negocio", security = @SecurityRequirement(name = "claveApi"))
	public Reporte_del_negocio crear(@Valid @RequestBody ReporteDelNegocioRequest peticion)
	{
		return reporteDelNegocioService.crear(peticion);
	}

	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "Actualizar un reporte del negocio", security = @SecurityRequirement(name = "claveApi"))
	public Reporte_del_negocio actualizar(@PathVariable Integer id, @Valid @RequestBody ReporteDelNegocioRequest peticion)
	{
		return reporteDelNegocioService.actualizar(id, peticion);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Eliminar un reporte del negocio", security = @SecurityRequirement(name = "claveApi"))
	public void eliminar(@PathVariable Integer id)
	{
		reporteDelNegocioService.eliminar(id);
	}
}
