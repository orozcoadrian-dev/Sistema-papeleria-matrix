package com.papeleriamatrix.repository;

import java.util.List;
import com.papeleriamatrix.model.Informacion_del_negocio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InformacionDelNegocioRepository extends JpaRepository<Informacion_del_negocio, Integer>
{

	/** Lo publicado, sin lo retirado. */
	List<Informacion_del_negocio> findByActivoTrue();

	long countByActivo(boolean activo);
}
