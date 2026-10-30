package com.papeleriamatrix.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.papeleriamatrix.dto.EstadoRespuesta;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class EstadoControllerTest
{
	@Test
	void devuelveElModoDemoCuandoEstaActivo()
	{
		MockEnvironment entorno = new MockEnvironment();
		entorno.setActiveProfiles("demo");

		EstadoRespuesta respuesta = new EstadoController(entorno).consultarEstado();

		assertThat(respuesta.aplicacion()).isEqualTo("Papelería Matrix");
		assertThat(respuesta.estado()).isEqualTo("en línea");
		assertThat(respuesta.modo()).isEqualTo("demo");
	}

	@Test
	void devuelveElModoLocalCuandoNoHayPerfilActivo()
	{
		EstadoRespuesta respuesta = new EstadoController(new MockEnvironment()).consultarEstado();

		assertThat(respuesta.modo()).isEqualTo("local");
	}
}
