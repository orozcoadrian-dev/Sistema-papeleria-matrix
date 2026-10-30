package com.papeleriamatrix;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Comprueba que la aplicación completa arranca en modo demo, sin necesitar XAMPP.
 */
@SpringBootTest
@ActiveProfiles("demo")
class PapeleriaMatrixApplicationTests
{
	@Test
	void elContextoArranca()
	{
		// Si el contexto de Spring no arranca, esta prueba falla antes de llegar aquí.
	}
}
