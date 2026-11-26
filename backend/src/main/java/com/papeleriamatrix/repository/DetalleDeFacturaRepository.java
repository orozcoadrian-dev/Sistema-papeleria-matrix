package com.papeleriamatrix.repository;

import com.papeleriamatrix.model.Detalle_de_factura;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;

public interface DetalleDeFacturaRepository extends JpaRepository<Detalle_de_factura, Integer>
{
	@Query("SELECT COALESCE(SUM(d.cantidadProducto), 0L) FROM Detalle_de_factura d JOIN d.factura f WHERE f.anulada = false AND f.fechaGeneracion BETWEEN :desde AND :hasta")
	Long sumarCantidadProductoPorFechaFactura(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

	@Query("SELECT COALESCE(SUM(d.cantidadRopa), 0L) FROM Detalle_de_factura d JOIN d.factura f WHERE f.anulada = false AND f.fechaGeneracion BETWEEN :desde AND :hasta")
	Long sumarCantidadRopaPorFechaFactura(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

	@Query("SELECT COALESCE(SUM(d.cantidadServicio), 0L) FROM Detalle_de_factura d JOIN d.factura f WHERE f.anulada = false AND f.fechaGeneracion BETWEEN :desde AND :hasta")
	Long sumarCantidadServicioPorFechaFactura(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

	List<Detalle_de_factura> findByFacturaDelNegocioIdIn(Collection<Integer> facturas);
	List<Detalle_de_factura> findByFacturaDelNegocioId(Integer factura);

	// Cada prenda de Pulguero Glam es única: si aparece en una venta sin anular, está vendida.
	@Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM Detalle_de_factura d JOIN d.factura f WHERE d.ropaId = :ropaId AND f.anulada = false")
	boolean existsByRopaId(@Param("ropaId") Integer ropaId);

	@Query("SELECT DISTINCT d.ropaId FROM Detalle_de_factura d JOIN d.factura f WHERE d.ropaId IS NOT NULL AND f.anulada = false")
	List<Integer> ropaVendida();
}
