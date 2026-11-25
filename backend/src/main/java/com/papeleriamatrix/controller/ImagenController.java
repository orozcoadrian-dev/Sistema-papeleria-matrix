package com.papeleriamatrix.controller;

import com.papeleriamatrix.service.ImagenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Duration;
import java.util.Map;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/imagenes")
@Tag(name = "Fotos", description = "Fotos de productos, ropa y servicios. Verlas es público; subirlas y quitarlas es de la administradora.")
public class ImagenController
{
	private final ImagenService imagenes;

	public ImagenController(ImagenService imagenes)
	{
		this.imagenes = imagenes;
	}

	@GetMapping
	@Operation(summary = "Qué elementos tienen foto", description = "Tipo → id → versión. La versión va en la dirección de la foto (?v=) para no ver una foto vieja.")
	public Map<String, Map<Integer, Long>> inventario()
	{
		return imagenes.inventario();
	}

	@GetMapping("/{tipo}/{id}")
	@Operation(summary = "Ver una foto")
	public ResponseEntity<byte[]> ver(@PathVariable String tipo, @PathVariable Integer id)
	{
		ImagenService.Imagen imagen = imagenes.leer(tipo, id);
		return ResponseEntity.ok()
			.contentType(imagen.tipo())
			.cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
			.header("X-Content-Type-Options", "nosniff")
			.body(imagen.contenido());
	}

	@PutMapping(value = "/{tipo}/{id}", consumes = { "image/jpeg", "image/png", "image/webp" })
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Subir o cambiar una foto", description = "El cuerpo es la imagen (JPG, PNG o WebP, hasta 2 MB).",
		security = { @SecurityRequirement(name = "sesion"), @SecurityRequirement(name = "claveApi") })
	public void guardar(@PathVariable String tipo, @PathVariable Integer id, @RequestBody byte[] contenido)
	{
		imagenes.guardar(tipo, id, contenido);
	}

	@DeleteMapping("/{tipo}/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Quitar una foto", security = { @SecurityRequirement(name = "sesion"), @SecurityRequirement(name = "claveApi") })
	public void quitar(@PathVariable String tipo, @PathVariable Integer id)
	{
		imagenes.quitar(tipo, id);
	}
}
