package com.papeleriamatrix.service;

import com.papeleriamatrix.dto.ContadoresResponse;
import com.papeleriamatrix.model.EstadoUsuario;
import com.papeleriamatrix.model.Ropa;
import com.papeleriamatrix.repository.ClaveDePermisoRepository;
import com.papeleriamatrix.repository.DetalleDeFacturaRepository;
import com.papeleriamatrix.repository.FacturaDelNegocioRepository;
import com.papeleriamatrix.repository.InformacionDelNegocioRepository;
import com.papeleriamatrix.repository.OperadorDeCibercafeRepository;
import com.papeleriamatrix.repository.ProductoPapeleriaRepository;
import com.papeleriamatrix.repository.ReporteDelNegocioRepository;
import com.papeleriamatrix.repository.RopaRepository;
import com.papeleriamatrix.repository.ServicioRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cuenta lo registrado en el negocio. */
@Service
public class ContadoresService
{
	private final ServicioRepository servicios;
	private final ProductoPapeleriaRepository productos;
	private final RopaRepository ropa;
	private final DetalleDeFacturaRepository detalles;
	private final InformacionDelNegocioRepository informacion;
	private final OperadorDeCibercafeRepository operadores;
	private final ClaveDePermisoRepository claves;
	private final FacturaDelNegocioRepository facturas;
	private final ReporteDelNegocioRepository reportes;

	public ContadoresService(ServicioRepository servicios, ProductoPapeleriaRepository productos, RopaRepository ropa,
		DetalleDeFacturaRepository detalles, InformacionDelNegocioRepository informacion, OperadorDeCibercafeRepository operadores,
		ClaveDePermisoRepository claves, FacturaDelNegocioRepository facturas, ReporteDelNegocioRepository reportes)
	{
		this.servicios = servicios;
		this.productos = productos;
		this.ropa = ropa;
		this.detalles = detalles;
		this.informacion = informacion;
		this.operadores = operadores;
		this.claves = claves;
		this.facturas = facturas;
		this.reportes = reportes;
	}

	@Transactional(readOnly = true)
	public ContadoresResponse contar()
	{
		Set<Integer> vendidas = new HashSet<>(detalles.ropaVendida());
		List<Ropa> enCatalogo = ropa.findByActivoTrue(Sort.by("id"));
		long prendasVendidas = enCatalogo.stream().filter(prenda -> vendidas.contains(prenda.getId())).count();

		return new ContadoresResponse(
			servicios.countByActivo(true), servicios.countByActivo(false),
			productos.countByActivo(true), productos.countByActivo(false),
			enCatalogo.size() - prendasVendidas, prendasVendidas, ropa.countByActivo(false),
			informacion.countByActivo(true), informacion.countByActivo(false),
			operadores.countByEstado(EstadoUsuario.ACTIVO.getTexto()),
			operadores.countByEstado(EstadoUsuario.SUSPENDIDO.getTexto()),
			operadores.countByEstado(EstadoUsuario.DESPEDIDO.getTexto()),
			claves.count(),
			facturas.countByAnulada(false), facturas.countByAnulada(true),
			reportes.count());
	}
}
