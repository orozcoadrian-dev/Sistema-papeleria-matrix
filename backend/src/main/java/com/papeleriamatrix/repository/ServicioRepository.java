package com.papeleriamatrix.repository;

import java.util.List;
import org.springframework.data.domain.Sort;
import com.papeleriamatrix.model.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicioRepository extends JpaRepository<Servicio, Integer>
{
	boolean existsByCodigoServicio(String codigoServicio);
	boolean existsByCodigoServicioAndIdNot(String codigoServicio, Integer id);

	/** Lo que está en el catálogo, sin lo retirado. */
	List<Servicio> findByActivoTrue(Sort orden);

	boolean existsByCodigoServicioAndActivoFalse(String codigo);

	long countByActivo(boolean activo);
}
