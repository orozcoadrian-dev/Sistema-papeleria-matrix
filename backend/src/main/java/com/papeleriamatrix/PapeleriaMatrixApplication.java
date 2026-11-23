package com.papeleriamatrix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Punto de entrada del sistema. Arranca el servidor y la API REST.
 */
@SpringBootApplication
public class PapeleriaMatrixApplication
{
	@Bean
	public PasswordEncoder passwordEncoder()
	{
		return new BCryptPasswordEncoder();
	}

	public static void main(String[] args)
	{
		SpringApplication.run(PapeleriaMatrixApplication.class, args);
	}
}
