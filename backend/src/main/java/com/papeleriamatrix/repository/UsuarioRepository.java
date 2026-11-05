package com.papeleriamatrix.repository;

import com.papeleriamatrix.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer>
{
	boolean existsByNumeroDocumento(String numeroDocumento);
	boolean existsByNumeroDocumentoAndIdUsuarioNot(String numeroDocumento, int idUsuario);

	boolean existsByNombreUsuario(String nombreUsuario);
	boolean existsByNombreUsuarioAndIdUsuarioNot(String nombreUsuario, int idUsuario);

	boolean existsByEmail(String email);
	boolean existsByEmailAndIdUsuarioNot(String email, int idUsuario);

	boolean existsByTelefono(String telefono);
	boolean existsByTelefonoAndIdUsuarioNot(String telefono, int idUsuario);
}
