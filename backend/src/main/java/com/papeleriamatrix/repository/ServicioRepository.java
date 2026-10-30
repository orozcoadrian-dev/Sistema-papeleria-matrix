package com.papeleriamatrix.repository;

import com.papeleriamatrix.model.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicioRepository extends JpaRepository<Servicio, Integer>
{
	boolean existsByCodigoServicio(String codigoServicio);

	boolean existsByCodigoServicioAndIdNot(String codigoServicio, Integer id);
}
