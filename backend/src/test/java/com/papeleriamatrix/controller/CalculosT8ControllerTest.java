package com.papeleriamatrix.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.papeleriamatrix.dto.EstadisticaResponse;
import com.papeleriamatrix.dto.IngresoResponse;
import com.papeleriamatrix.exception.ManejadorDeErrores;
import com.papeleriamatrix.service.EstadisticaDelNegocioService;
import com.papeleriamatrix.service.IngresoDelNegocioService;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@ExtendWith(MockitoExtension.class)
class CalculosT8ControllerTest
{
	@Mock
	private IngresoDelNegocioService ingresoService;
	@Mock
	private EstadisticaDelNegocioService estadisticaService;

	private MockMvc ingresosMvc;
	private MockMvc estadisticasMvc;
	private final LocalDate desde = LocalDate.of(2026, 3, 1);
	private final LocalDate hasta = LocalDate.of(2026, 3, 31);

	@BeforeEach
	void preparar()
	{
		LocalValidatorFactoryBean validador = new LocalValidatorFactoryBean();
		validador.afterPropertiesSet();
		ingresosMvc = MockMvcBuilders.standaloneSetup(new IngresoController(ingresoService))
			.setValidator(validador)
			.setControllerAdvice(new ManejadorDeErrores())
			.build();
		estadisticasMvc = MockMvcBuilders.standaloneSetup(new EstadisticaController(estadisticaService))
			.setValidator(validador)
			.setControllerAdvice(new ManejadorDeErrores())
			.build();
	}

	@Test
	void calcularIngresoRechazaUnRangoInvertido()
		throws Exception
	{
		ejecutarPostInvalido(ingresosMvc, "/api/ingresos/calcular");
		verifyNoInteractions(ingresoService);
	}

	@Test
	void calcularIngresoAceptaUnRangoValido()
		throws Exception
	{
		when(ingresoService.calcularYGuardar(desde, hasta)).thenReturn(new IngresoResponse(
			1, "123456789012345", desde, hasta, BigDecimal.ZERO, BigDecimal.ZERO,
			BigDecimal.ZERO, BigDecimal.ZERO, "Ingreso ventas"));

		ingresosMvc.perform(post("/api/ingresos/calcular")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"desde\":\"2026-03-01\",\"hasta\":\"2026-03-31\"}"))
			.andExpect(status().isCreated());

		verify(ingresoService).calcularYGuardar(eq(desde), eq(hasta));
	}

	@Test
	void calcularEstadisticaRechazaUnRangoInvertido()
		throws Exception
	{
		ejecutarPostInvalido(estadisticasMvc, "/api/estadisticas/calcular");
		verifyNoInteractions(estadisticaService);
	}

	@Test
	void calcularEstadisticaAceptaUnRangoValido()
		throws Exception
	{
		when(estadisticaService.calcularYGuardar(desde, hasta)).thenReturn(new EstadisticaResponse(
			1, "123456789012345", desde, hasta, 0, 0, 0, 0, BigDecimal.ZERO, "Estadistica"));

		estadisticasMvc.perform(post("/api/estadisticas/calcular")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"desde\":\"2026-03-01\",\"hasta\":\"2026-03-31\"}"))
			.andExpect(status().isCreated());

		verify(estadisticaService).calcularYGuardar(eq(desde), eq(hasta));
	}

	@Test
	void resumenIngresoRechazaUnParametroDeFechaMalFormado()
		throws Exception
	{
		ingresosMvc.perform(get("/api/ingresos/resumen")
				.param("desde", "2026-13-01")
				.param("hasta", "2026-03-31"))
			.andExpect(status().isBadRequest());
	}

	@Test
	void resumenEstadisticaRechazaUnParametroDeFechaMalFormado()
		throws Exception
	{
		estadisticasMvc.perform(get("/api/estadisticas/resumen")
				.param("desde", "no-es-fecha")
				.param("hasta", "2026-03-31"))
			.andExpect(status().isBadRequest());
	}

	private void ejecutarPostInvalido(MockMvc mvc, String ruta)
		throws Exception
	{
		mvc.perform(post(ruta)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"desde\":\"2026-03-31\",\"hasta\":\"2026-03-01\"}"))
			.andExpect(status().isBadRequest());
	}
}
