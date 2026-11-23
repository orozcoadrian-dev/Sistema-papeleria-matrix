package com.papeleriamatrix.repository;

import com.papeleriamatrix.model.Detalle_de_factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;

public interface DetalleDeFacturaRepository extends JpaRepository<Detalle_de_factura, Integer>
{
	@Query("SELECT COALESCE(SUM(d.cantidadProducto), 0L) FROM Detalle_de_factura d JOIN d.factura f WHERE f.fechaGeneracion BETWEEN :desde AND :hasta")
	Long sumarCantidadProductoPorFechaFactura(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

	@Query("SELECT COALESCE(SUM(d.cantidadRopa), 0L) FROM Detalle_de_factura d JOIN d.factura f WHERE f.fechaGeneracion BETWEEN :desde AND :hasta")
	Long sumarCantidadRopaPorFechaFactura(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

	@Query("SELECT COALESCE(SUM(d.cantidadServicio), 0L) FROM Detalle_de_factura d JOIN d.factura f WHERE f.fechaGeneracion BETWEEN :desde AND :hasta")
	Long sumarCantidadServicioPorFechaFactura(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);
}
