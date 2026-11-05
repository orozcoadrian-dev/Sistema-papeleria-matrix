package com.papeleriamatrix.repository;

import com.papeleriamatrix.model.Factura_del_negocio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FacturaDelNegocioRepository extends JpaRepository<Factura_del_negocio, Integer>
{
	boolean existsByCodigoFactura(String codigoFactura);
	boolean existsByCodigoFacturaAndIdFacturaNot(String codigoFactura, Integer idFactura);
}
