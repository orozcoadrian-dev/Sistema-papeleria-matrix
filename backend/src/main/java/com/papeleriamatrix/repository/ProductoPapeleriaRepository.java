package com.papeleriamatrix.repository;

import com.papeleriamatrix.model.ProductoPapeleria;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductoPapeleriaRepository extends JpaRepository<ProductoPapeleria, Integer>
{
	boolean existsByCodigoProducto(String codigoProducto);
	boolean existsByCodigoProductoAndIdNot(String codigoProducto, Integer id);

	/**
	 * Trae el producto bloqueando su fila hasta que termine la transacción. Así dos ventas a la vez (o una venta
	 * y un cambio desde el panel) no leen las mismas existencias y se pisan: la segunda espera a la primera.
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT p FROM ProductoPapeleria p WHERE p.id = :id")
	Optional<ProductoPapeleria> bloquear(@Param("id") Integer id);
}
