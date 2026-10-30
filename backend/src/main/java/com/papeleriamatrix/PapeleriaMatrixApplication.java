package com.papeleriamatrix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del sistema. Arranca el servidor y la API REST.
 */
@SpringBootApplication
public class PapeleriaMatrixApplication
{
	public static void main(String[] args)
	{
		SpringApplication.run(PapeleriaMatrixApplication.class, args);
	}
}
