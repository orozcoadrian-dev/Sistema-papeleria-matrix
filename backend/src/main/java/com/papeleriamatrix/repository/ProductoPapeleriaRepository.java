package com.papeleriamatrix.repository;

import com.papeleriamatrix.model.ProductoPapeleria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoPapeleriaRepository extends JpaRepository<ProductoPapeleria, Integer>
{
	boolean existsByCodigoProducto(String codigoProducto);
	boolean existsByCodigoProductoAndIdNot(String codigoProducto, Integer id);
}
