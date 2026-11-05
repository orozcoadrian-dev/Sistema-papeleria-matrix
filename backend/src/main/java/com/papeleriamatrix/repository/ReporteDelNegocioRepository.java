package com.papeleriamatrix.repository;

import com.papeleriamatrix.model.Reporte_del_negocio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReporteDelNegocioRepository extends JpaRepository<Reporte_del_negocio, Integer>
{
	boolean existsByCodigoReporte(String codigoReporte);
	boolean existsByCodigoReporteAndIdReporteNot(String codigoReporte, Integer idReporte);
}
