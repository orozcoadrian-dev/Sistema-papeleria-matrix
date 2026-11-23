package com.papeleriamatrix.repository;

import com.papeleriamatrix.model.ReporteDelNegocio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReporteDelNegocioRepository extends JpaRepository<ReporteDelNegocio, Integer>
{
	boolean existsByCodigoReporte(String codigoReporte);
	boolean existsByCodigoReporteAndIdReporteNot(String codigoReporte, Integer idReporte);
}
