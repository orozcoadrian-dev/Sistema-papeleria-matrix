package com.papeleriamatrix.repository;

import com.papeleriamatrix.model.Ingreso_del_negocio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngresoDelNegocioRepository extends JpaRepository<Ingreso_del_negocio, Integer>
{
	boolean existsByCodigoIngreso(String codigoIngreso);
	boolean existsByCodigoIngresoAndIdIngresoNot(String codigoIngreso, Integer idIngreso);
}
