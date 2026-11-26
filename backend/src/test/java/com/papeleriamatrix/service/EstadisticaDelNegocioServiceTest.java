package com.papeleriamatrix.service;

import com.papeleriamatrix.dto.ContadoresResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.papeleriamatrix.dto.EstadisticaDelNegocioRequest;
import com.papeleriamatrix.dto.EstadisticaResponse;
import com.papeleriamatrix.exception.RecursoDuplicadoException;
import com.papeleriamatrix.model.Estadistica_del_negocio;
import com.papeleriamatrix.model.Historial_estadistica_del_negocio;
import com.papeleriamatrix.repository.DetalleDeFacturaRepository;
import com.papeleriamatrix.repository.EstadisticaDelNegocioRepository;
import com.papeleriamatrix.repository.FacturaDelNegocioRepository;
import com.papeleriamatrix.repository.HistorialEstadisticaDelNegocioRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EstadisticaDelNegocioServiceTest
{
	@Mock
	private EstadisticaDelNegocioRepository repositorio;
	@Mock
	private FacturaDelNegocioRepository facturaRepositorio;
	@Mock
	private DetalleDeFacturaRepository detalleRepositorio;
	@Mock
	private HistorialEstadisticaDelNegocioRepository historialRepositorio;
	@Mock
	private ContadoresService contadores;

	private EstadisticaDelNegocioService servicio;
	private final LocalDate desde = LocalDate.of(2026, 3, 1);
	private final LocalDate hasta = LocalDate.of(2026, 3, 31);

	@BeforeEach
	void preparar()
	{
		servicio = new EstadisticaDelNegocioService(repositorio, facturaRepositorio, detalleRepositorio, historialRepositorio, contadores);
	}

	@Test
	void resumirCuentaFacturasCantidadesYTicketPromedio()
	{
		when(facturaRepositorio.countByFechaGeneracionBetween(desde, hasta)).thenReturn(2L);
		when(facturaRepositorio.sumarMontoPorFechaGeneracion(desde, hasta)).thenReturn(65.0);
		when(detalleRepositorio.sumarCantidadProductoPorFechaFactura(desde, hasta)).thenReturn(3L);
		when(detalleRepositorio.sumarCantidadRopaPorFechaFactura(desde, hasta)).thenReturn(5L);
		when(detalleRepositorio.sumarCantidadServicioPorFechaFactura(desde, hasta)).thenReturn(7L);

		EstadisticaResponse respuesta = servicio.resumir(desde, hasta);

		assertEquals(2, respuesta.numeroFacturasGeneradas());
		assertEquals(3, respuesta.numeroProductosVendidos());
		assertEquals(5, respuesta.numeroRopaVendida());
		assertEquals(7, respuesta.numeroServicios());
		assertEquals(new BigDecimal("32.50"), respuesta.ticketPromedio());
		verify(repositorio, never()).save(any(Estadistica_del_negocio.class));
		verify(historialRepositorio, never()).save(any(Historial_estadistica_del_negocio.class));
	}

	@Test
	void resumirSinFacturasDevuelveCerosSinGuardar()
	{
		EstadisticaResponse respuesta = servicio.resumir(desde, hasta);

		assertEquals(0, respuesta.numeroFacturasGeneradas());
		assertEquals(0, respuesta.numeroProductosVendidos());
		assertEquals(new BigDecimal("0.00"), respuesta.ticketPromedio());
		verify(repositorio, never()).save(any(Estadistica_del_negocio.class));
		verify(historialRepositorio, never()).save(any(Historial_estadistica_del_negocio.class));
	}

	@Test
	void calcularYGuardarGuardaEstadisticaEHistorialYReintentaCodigoRepetido()
	{
		when(facturaRepositorio.countByFechaGeneracionBetween(desde, hasta)).thenReturn(2L);
		when(facturaRepositorio.sumarMontoPorFechaGeneracion(desde, hasta)).thenReturn(65.0);
		when(detalleRepositorio.sumarCantidadProductoPorFechaFactura(desde, hasta)).thenReturn(3L);
		when(detalleRepositorio.sumarCantidadRopaPorFechaFactura(desde, hasta)).thenReturn(5L);
		when(detalleRepositorio.sumarCantidadServicioPorFechaFactura(desde, hasta)).thenReturn(7L);
		when(repositorio.existsByCodigoEstadistica(anyString())).thenReturn(true, false);
		when(repositorio.save(any(Estadistica_del_negocio.class))).thenAnswer(invocacion -> {
			Estadistica_del_negocio estadistica = invocacion.getArgument(0);
			estadistica.setIdEstadistica(9);
			return estadistica;
		});
		when(historialRepositorio.save(any(Historial_estadistica_del_negocio.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
		when(contadores.contar()).thenReturn(new ContadoresResponse(4, 1, 6, 2, 3, 1, 0, 5, 1, 2, 0, 1, 1, 2, 0, 0));

		EstadisticaResponse respuesta = servicio.calcularYGuardar(desde, hasta);

		assertEquals(9, respuesta.id());
		assertEquals(2, respuesta.numeroFacturasGeneradas());
		assertEquals(new BigDecimal("32.50"), respuesta.ticketPromedio());
		ArgumentCaptor<Estadistica_del_negocio> estadisticaCaptor = ArgumentCaptor.forClass(Estadistica_del_negocio.class);
		verify(repositorio).save(estadisticaCaptor.capture());
		assertTrue(estadisticaCaptor.getValue().getCodigoEstadistica().matches("\\d{15}"));
		assertEquals(3, estadisticaCaptor.getValue().getNumeroProductosVendidos());
		assertEquals(4, estadisticaCaptor.getValue().getNumeroServiciosOfrecidos());
		assertEquals(6, estadisticaCaptor.getValue().getNumeroInformacionAgregada());
		assertEquals(1, estadisticaCaptor.getValue().getOperadoresCibercafeDespedidos());
		ArgumentCaptor<Historial_estadistica_del_negocio> historialCaptor = ArgumentCaptor.forClass(Historial_estadistica_del_negocio.class);
		verify(historialRepositorio).save(historialCaptor.capture());
		assertEquals(9, historialCaptor.getValue().getEstadisticaDelNegocioId());
		assertTrue(historialCaptor.getValue().getMotivoEstadistica().matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]{20,100}"));
		verify(repositorio, times(2)).existsByCodigoEstadistica(anyString());
	}

	@Test
	void crearConvierteCamposNulosACero()
	{
		when(repositorio.existsByCodigoEstadistica("123456789012345")).thenReturn(false);
		when(repositorio.save(any(Estadistica_del_negocio.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

		Estadistica_del_negocio estadistica = servicio.crear(peticionNula());

		assertEquals(0, estadistica.getNumeroInformacionEliminada());
		assertEquals(0, estadistica.getServiciosEliminados());
		assertEquals(0, estadistica.getNumeroInformacionAgregada());
		assertEquals(0, estadistica.getNumeroServiciosOfrecidos());
		assertEquals(0, estadistica.getNumeroProductosVendidos());
		assertEquals(0, estadistica.getOperadoresCibercafeDespedidos());
		assertEquals(0, estadistica.getRopaActualCatalogo());
		assertEquals(0, estadistica.getNumeroProductosPapeleria());
		assertEquals(0, estadistica.getRopaRemovidaCatalogo());
		assertEquals(0, estadistica.getProductosRemovidos());
		assertEquals(0, estadistica.getNumeroRopaVendida());
		assertEquals(0, estadistica.getNumeroServicios());
		assertEquals(0, estadistica.getNumeroFacturasGeneradas());
	}

	@Test
	void actualizarConvierteCamposNulosACero()
	{
		Estadistica_del_negocio existente = new Estadistica_del_negocio("123456789012345", "Estadistica");
		existente.setIdEstadistica(9);
		when(repositorio.findById(9)).thenReturn(Optional.of(existente));
		when(repositorio.existsByCodigoEstadisticaAndIdEstadisticaNot("123456789012345", 9)).thenReturn(false);
		when(repositorio.save(any(Estadistica_del_negocio.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

		Estadistica_del_negocio estadistica = servicio.actualizar(9, peticionNula());

		assertEquals(0, estadistica.getNumeroInformacionEliminada());
		assertEquals(0, estadistica.getNumeroProductosVendidos());
		assertEquals(0, estadistica.getNumeroFacturasGeneradas());
	}

	@Test
	void crearRechazaCodigoDuplicado()
	{
		when(repositorio.existsByCodigoEstadistica("123456789012345")).thenReturn(true);

		assertThrows(RecursoDuplicadoException.class, () -> servicio.crear(peticionNula()));

		verify(repositorio, never()).save(any(Estadistica_del_negocio.class));
	}

	@Test
	void actualizarRechazaCodigoDuplicado()
	{
		Estadistica_del_negocio existente = new Estadistica_del_negocio("123456789012345", "Estadistica");
		when(repositorio.findById(9)).thenReturn(Optional.of(existente));
		when(repositorio.existsByCodigoEstadisticaAndIdEstadisticaNot(eq("123456789012345"), eq(9))).thenReturn(true);

		assertThrows(RecursoDuplicadoException.class, () -> servicio.actualizar(9, peticionNula()));

		verify(repositorio, never()).save(any(Estadistica_del_negocio.class));
	}

	private EstadisticaDelNegocioRequest peticionNula()
	{
		return new EstadisticaDelNegocioRequest(
			"123456789012345", "Estadistica", null, null, null, null, null, null,
			null, null, null, null, null, null, null);
	}
}
