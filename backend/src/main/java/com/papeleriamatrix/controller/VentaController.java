package com.papeleriamatrix.controller;

import com.papeleriamatrix.dto.CorreoDeVentaRequest;
import com.papeleriamatrix.service.CorreoDeVentas;
import com.papeleriamatrix.dto.VentaRequest;
import com.papeleriamatrix.dto.VentaResponse;
import com.papeleriamatrix.security.PermisoDeVentas;
import com.papeleriamatrix.security.Sesion;
import com.papeleriamatrix.service.VentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ventas")
@Tag(name = "Ventas", description = "Registrar, consultar, cambiar y anular ventas. Vender un producto descuenta sus existencias.")
public class VentaController
{
	private final VentaService ventaService;
	private final CorreoDeVentas correoDeVentas;

	public VentaController(VentaService ventaService, CorreoDeVentas correoDeVentas)
	{
		this.ventaService = ventaService;
		this.correoDeVentas = correoDeVentas;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Registrar una venta", description = "La puede registrar el operador (queda a su nombre) o el administrador indicando operadorId.",
		security = { @SecurityRequirement(name = "sesion"), @SecurityRequirement(name = "claveApi") })
	public VentaResponse registrar(@Valid @RequestBody VentaRequest peticion, @Parameter(hidden = true) @RequestAttribute(Sesion.ATRIBUTO) Sesion sesion)
	{
		return ventaService.registrar(peticion, sesion);
	}

	@GetMapping
	@Operation(summary = "Listar las ventas", description = "Por defecto, las de los últimos 30 días y sin las anuladas. El operador solo ve las suyas.",
		security = { @SecurityRequirement(name = "sesion"), @SecurityRequirement(name = "claveApi") })
	public List<VentaResponse> listar(
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
		@RequestParam(defaultValue = "false") boolean incluirAnuladas,
		@Parameter(hidden = true) @RequestAttribute(Sesion.ATRIBUTO) Sesion sesion)
	{
		return ventaService.listar(sesion, desde, hasta, incluirAnuladas);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consultar una venta", security = { @SecurityRequirement(name = "sesion"), @SecurityRequirement(name = "claveApi") })
	public VentaResponse obtener(@PathVariable Integer id, @Parameter(hidden = true) @RequestAttribute(Sesion.ATRIBUTO) Sesion sesion)
	{
		return ventaService.obtener(id, sesion);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Cambiar una venta",
		description = "Reemplaza los artículos y vuelve a calcular el total y las existencias. El operador solo cambia las suyas y envía su clave "
			+ "de permiso en la cabecera X-Clave-Permiso, codificada como en una dirección web. El administrador no la necesita.",
		security = { @SecurityRequirement(name = "sesion"), @SecurityRequirement(name = "claveApi") })
	public VentaResponse modificar(@PathVariable Integer id, @Valid @RequestBody VentaRequest peticion,
		@Parameter(hidden = true) @RequestAttribute(Sesion.ATRIBUTO) Sesion sesion,
		@RequestHeader(name = PermisoDeVentas.CABECERA, required = false) String clavePermiso)
	{
		return ventaService.modificar(id, peticion, sesion, clavePermiso);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Anular una venta",
		description = "La venta queda marcada como anulada y devuelve las existencias de los productos. El operador solo anula las suyas y envía su clave de permiso en la cabecera X-Clave-Permiso.",
		security = { @SecurityRequirement(name = "sesion"), @SecurityRequirement(name = "claveApi") })
	public void anular(@PathVariable Integer id, @Parameter(hidden = true) @RequestAttribute(Sesion.ATRIBUTO) Sesion sesion,
		@RequestHeader(name = PermisoDeVentas.CABECERA, required = false) String clavePermiso)
	{
		ventaService.anular(id, sesion, clavePermiso);
	}

	@PostMapping("/{id}/correo")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Enviar el registro de una venta por correo",
		description = "Responde 503 mientras no se hayan configurado CORREO_CLAVE y CORREO_REMITENTE.",
		security = { @SecurityRequirement(name = "sesion"), @SecurityRequirement(name = "claveApi") })
	public void enviarPorCorreo(@PathVariable Integer id, @Valid @RequestBody CorreoDeVentaRequest peticion,
		@Parameter(hidden = true) @RequestAttribute(Sesion.ATRIBUTO) Sesion sesion)
	{
		correoDeVentas.enviar(id, peticion, sesion);
	}
}
