package com.papeleriamatrix.controller;

import com.papeleriamatrix.dto.ProductoResponse;
import com.papeleriamatrix.service.ProductoPapeleriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos de papelería", description = "Catálogo de papelería para el público. Solo consulta.")
public class ProductoPapeleriaController
{
	private final ProductoPapeleriaService productoService;

	public ProductoPapeleriaController(ProductoPapeleriaService productoService)
	{
		this.productoService = productoService;
	}

	@GetMapping
	@Operation(summary = "Listar los productos de papelería")
	public List<ProductoResponse> listar()
	{
		return productoService.listar();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consultar un producto por su id")
	public ProductoResponse obtener(@PathVariable Integer id)
	{
		return productoService.obtener(id);
	}
}
