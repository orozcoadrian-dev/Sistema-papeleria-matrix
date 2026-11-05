package com.papeleriamatrix.repository;

import com.papeleriamatrix.model.Estadistica_del_negocio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstadisticaDelNegocioRepository extends JpaRepository<Estadistica_del_negocio, Integer>
{
	boolean existsByCodigoEstadistica(String codigoEstadistica);
	boolean existsByCodigoEstadisticaAndIdEstadisticaNot(String codigoEstadistica, Integer idEstadistica);
}
