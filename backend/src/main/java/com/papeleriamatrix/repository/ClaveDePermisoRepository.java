package com.papeleriamatrix.repository;

import com.papeleriamatrix.model.Clave_de_permiso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaveDePermisoRepository extends JpaRepository<Clave_de_permiso, Integer>
{
	boolean existsByUsuarioId(Integer usuarioId);
	boolean existsByUsuarioIdAndIdClaveNot(Integer usuarioId, Integer idClave);
}
