package com.papeleriamatrix.repository;

import com.papeleriamatrix.model.Factura_del_negocio;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FacturaDelNegocioRepository extends JpaRepository<Factura_del_negocio, Integer>
{
	boolean existsByCodigoFactura(String codigoFactura);
	boolean existsByCodigoFacturaAndIdFacturaNot(String codigoFactura, Integer idFactura);

	List<Factura_del_negocio> findByFechaGeneracionBetweenOrderByIdFacturaDesc(LocalDate desde, LocalDate hasta);
	List<Factura_del_negocio> findByUsuarioIdAndFechaGeneracionBetweenOrderByIdFacturaDesc(Integer usuarioId, LocalDate desde, LocalDate hasta);
}
