package com.papeleriamatrix.repository;

import com.papeleriamatrix.model.Ropa;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RopaRepository extends JpaRepository<Ropa, Integer>
{
	boolean existsByCodigoRopa(String codigoRopa);
	boolean existsByCodigoRopaAndIdNot(String codigoRopa, Integer id);

	/** Trae la prenda bloqueando su fila: dos operadores no pueden vender la misma prenda a la vez. */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT r FROM Ropa r WHERE r.id = :id")
	Optional<Ropa> bloquear(@Param("id") Integer id);
}
