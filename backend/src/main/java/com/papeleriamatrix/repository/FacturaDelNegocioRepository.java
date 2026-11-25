package com.papeleriamatrix.repository;

import com.papeleriamatrix.model.Factura_del_negocio;
import java.util.List;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;

public interface FacturaDelNegocioRepository extends JpaRepository<Factura_del_negocio, Integer>
{
	boolean existsByCodigoFactura(String codigoFactura);
	boolean existsByCodigoFacturaAndIdFacturaNot(String codigoFactura, Integer idFactura);

	/** Trae la factura bloqueando su fila: si dos personas anulan la misma venta a la vez, la segunda ya no la encuentra. */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT f FROM Factura_del_negocio f WHERE f.idFactura = :id")
	Optional<Factura_del_negocio> bloquear(@Param("id") Integer id);

	@Query("SELECT COALESCE(SUM(f.monto), 0.0) FROM Factura_del_negocio f WHERE LOWER(f.tipoFactura) = LOWER(:tipoFactura) AND f.fechaGeneracion BETWEEN :desde AND :hasta")
	Double sumarMontoPorTipoFactura(@Param("tipoFactura") String tipoFactura, @Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

	@Query("SELECT COALESCE(SUM(f.monto), 0.0) FROM Factura_del_negocio f WHERE f.fechaGeneracion BETWEEN :desde AND :hasta")
	Double sumarMontoPorFechaGeneracion(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

	// Las ventas de la caja («Venta en tienda») pueden mezclar servicios, papelería y ropa en una misma factura,
	// así que lo de cada tipo se suma por línea: la cantidad por el precio del catálogo.
	@Query("SELECT COALESCE(SUM(d.cantidadServicio * s.precio), 0.0) FROM Detalle_de_factura d JOIN d.factura f JOIN d.servicio s WHERE LOWER(f.tipoFactura) = LOWER(:tipoFactura) AND f.fechaGeneracion BETWEEN :desde AND :hasta")
	Double sumarServiciosVendidosPorLinea(@Param("tipoFactura") String tipoFactura, @Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

	@Query("SELECT COALESCE(SUM(d.cantidadProducto * p.precioVenta), 0.0) FROM Detalle_de_factura d JOIN d.factura f JOIN d.productoPapeleria p WHERE LOWER(f.tipoFactura) = LOWER(:tipoFactura) AND f.fechaGeneracion BETWEEN :desde AND :hasta")
	Double sumarProductosVendidosPorLinea(@Param("tipoFactura") String tipoFactura, @Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

	@Query("SELECT COALESCE(SUM(d.cantidadRopa * r.precio), 0.0) FROM Detalle_de_factura d JOIN d.factura f JOIN d.ropa r WHERE LOWER(f.tipoFactura) = LOWER(:tipoFactura) AND f.fechaGeneracion BETWEEN :desde AND :hasta")
	Double sumarRopaVendidaPorLinea(@Param("tipoFactura") String tipoFactura, @Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

	long countByFechaGeneracionBetween(LocalDate desde, LocalDate hasta);

	List<Factura_del_negocio> findByFechaGeneracionBetweenOrderByIdFacturaDesc(LocalDate desde, LocalDate hasta);
	List<Factura_del_negocio> findByUsuarioIdAndFechaGeneracionBetweenOrderByIdFacturaDesc(Integer usuarioId, LocalDate desde, LocalDate hasta);
}
