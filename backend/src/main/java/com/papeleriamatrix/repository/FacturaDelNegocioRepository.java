package com.papeleriamatrix.repository;

import com.papeleriamatrix.model.Factura_del_negocio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;

public interface FacturaDelNegocioRepository extends JpaRepository<Factura_del_negocio, Integer>
{
	boolean existsByCodigoFactura(String codigoFactura);
	boolean existsByCodigoFacturaAndIdFacturaNot(String codigoFactura, Integer idFactura);

	@Query("SELECT COALESCE(SUM(f.monto), 0.0) FROM Factura_del_negocio f WHERE LOWER(f.tipoFactura) = LOWER(:tipoFactura) AND f.fechaGeneracion BETWEEN :desde AND :hasta")
	Double sumarMontoPorTipoFactura(@Param("tipoFactura") String tipoFactura, @Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

	@Query("SELECT COALESCE(SUM(f.monto), 0.0) FROM Factura_del_negocio f WHERE f.fechaGeneracion BETWEEN :desde AND :hasta")
	Double sumarMontoPorFechaGeneracion(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

	long countByFechaGeneracionBetween(LocalDate desde, LocalDate hasta);
}
