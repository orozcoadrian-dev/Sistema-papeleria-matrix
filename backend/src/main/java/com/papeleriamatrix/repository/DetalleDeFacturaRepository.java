package com.papeleriamatrix.repository;

import com.papeleriamatrix.model.Detalle_de_factura;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetalleDeFacturaRepository extends JpaRepository<Detalle_de_factura, Integer>
{
	List<Detalle_de_factura> findByFacturaDelNegocioIdIn(Collection<Integer> facturas);
	List<Detalle_de_factura> findByFacturaDelNegocioId(Integer factura);
}
