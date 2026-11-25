package com.papeleriamatrix.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.papeleriamatrix.repository.UsuarioRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * El rol se guarda como texto. Antes era un enum y Hibernate creaba en H2 una restricción CHECK con la «é»
 * de «Operador de cibercafé» que fallaba en cada conexión nueva del grupo (a los 30 minutos de arrancar):
 * desde ahí el operador ya no podía entrar ni guardarse.
 */
@SpringBootTest
@ActiveProfiles("demo")
class RolEnLaBaseTest
{
	@Autowired
	private JdbcTemplate jdbc;

	@Autowired
	private UsuarioRepository usuarios;

	@Test
	void laTablaDeUsuariosNoTieneRestriccionSobreElRol()
	{
		List<String> restricciones = jdbc.queryForList(
			"SELECT cc.CHECK_CLAUSE FROM INFORMATION_SCHEMA.CHECK_CONSTRAINTS cc "
				+ "JOIN INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc ON tc.CONSTRAINT_NAME = cc.CONSTRAINT_NAME "
				+ "WHERE LOWER(tc.TABLE_NAME) = 'usuario'", String.class);
		assertTrue(restricciones.stream().noneMatch(regla -> regla.toLowerCase().contains("rol")),
			"No debe haber restricción CHECK sobre el rol: " + restricciones);
	}

	@Test
	void elRolSeLeeYSeGuardaComoAntes()
	{
		Usuario operador = usuarios.findByNombreUsuario("operador.matrix").orElseThrow();
		assertEquals(RolUsuario.OPERADOR_DE_CIBERCAFE, operador.getRol());
		String guardado = jdbc.queryForObject("SELECT rol FROM usuario WHERE nombre_usuario = 'operador.matrix'", String.class);
		assertEquals("Operador de cibercafé", guardado);
	}
}
