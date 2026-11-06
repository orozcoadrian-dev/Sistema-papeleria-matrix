package com.papeleriamatrix.controller;

import com.papeleriamatrix.dto.ProductoResponse;
import com.papeleriamatrix.service.ProductoPapeleriaService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.papeleriamatrix.dto.ProductoPapeleriaRequest;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos de papelería", description = "Catálogo de papelería para el público. Solo consulta.")
public class ProductoPapeleriaController {
	private final ProductoPapeleriaService productoService;

	public ProductoPapeleriaController(ProductoPapeleriaService productoService) {
		this.productoService = productoService;
	}

	@GetMapping
	@Operation(summary = "Listar los productos de papelería")
	public List<ProductoResponse> listar() {
		return productoService.listar();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consultar un producto por su id")
	public ProductoResponse obtener(@PathVariable Integer id) {
		return productoService.obtener(id);
	}
	
	@Operation(summary = "Crear producto")
	@SecurityRequirement(name = "claveApi")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ProductoResponse crear(@Valid @RequestBody ProductoPapeleriaRequest req) {
		return productoService.crear(req);
	}

	@Operation(summary = "Editar producto")
	@SecurityRequirement(name = "claveApi")
	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public ProductoResponse actualizar(@PathVariable Integer id, @Valid @RequestBody ProductoPapeleriaRequest req) {
		return productoService.actualizar(id, req);
	}

	@Operation(summary = "Eliminar producto")
	@SecurityRequirement(name = "claveApi")
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void eliminar(@PathVariable Integer id) {
		productoService.eliminar(id);
	}

}
