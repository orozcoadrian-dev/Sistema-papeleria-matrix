package com.papeleriamatrix.service;

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

import com.papeleriamatrix.dto.IngresoDelNegocioRequest;
import com.papeleriamatrix.dto.IngresoResponse;
import com.papeleriamatrix.exception.RecursoDuplicadoException;
import com.papeleriamatrix.model.Historial_ingreso_del_negocio;
import com.papeleriamatrix.model.Ingreso_del_negocio;
import com.papeleriamatrix.repository.FacturaDelNegocioRepository;
import com.papeleriamatrix.repository.HistorialIngresoDelNegocioRepository;
import com.papeleriamatrix.repository.IngresoDelNegocioRepository;
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
class IngresoDelNegocioServiceTest
{
	@Mock
	private IngresoDelNegocioRepository repositorio;
	@Mock
	private FacturaDelNegocioRepository facturaRepositorio;
	@Mock
	private HistorialIngresoDelNegocioRepository historialRepositorio;

	private IngresoDelNegocioService servicio;
	private final LocalDate desde = LocalDate.of(2026, 3, 1);
	private final LocalDate hasta = LocalDate.of(2026, 3, 31);

	@BeforeEach
	void preparar()
	{
		servicio = new IngresoDelNegocioService(repositorio, facturaRepositorio, historialRepositorio);
	}

	@Test
	void resumirSumaLosMontosDeCadaTipoYRedondea()
	{
		when(facturaRepositorio.sumarMontoPorTipoFactura(TiposFactura.SERVICIO, desde, hasta)).thenReturn(10.125);
		when(facturaRepositorio.sumarMontoPorTipoFactura(TiposFactura.ROPA, desde, hasta)).thenReturn(5.555);
		when(facturaRepositorio.sumarMontoPorTipoFactura(TiposFactura.PAPELERIA, desde, hasta)).thenReturn(2.345);
		when(facturaRepositorio.sumarMontoPorTipoFactura(TiposFactura.VARIEDAD, desde, hasta)).thenReturn(3.335);

		IngresoResponse respuesta = servicio.resumir(desde, hasta);

		assertEquals(new BigDecimal("10.13"), respuesta.totalVendidoServicios());
		assertEquals(new BigDecimal("5.56"), respuesta.totalVendidoRopa());
		assertEquals(new BigDecimal("5.69"), respuesta.totalVendidoProductos());
		assertEquals(new BigDecimal("21.38"), respuesta.totalGeneral());
		verify(repositorio, never()).save(any(Ingreso_del_negocio.class));
		verify(historialRepositorio, never()).save(any(Historial_ingreso_del_negocio.class));
	}

	@Test
	void resumirSinFacturasDevuelveCerosSinGuardar()
	{
		IngresoResponse respuesta = servicio.resumir(desde, hasta);

		assertEquals(new BigDecimal("0.00"), respuesta.totalGeneral());
		assertEquals(new BigDecimal("0.00"), respuesta.totalVendidoProductos());
		verify(repositorio, never()).save(any(Ingreso_del_negocio.class));
		verify(historialRepositorio, never()).save(any(Historial_ingreso_del_negocio.class));
	}

	@Test
	void calcularYGuardarGuardaIngresoEHistorialYReintentaCodigoRepetido()
	{
		when(facturaRepositorio.sumarMontoPorTipoFactura(TiposFactura.SERVICIO, desde, hasta)).thenReturn(10.0);
		when(facturaRepositorio.sumarMontoPorTipoFactura(TiposFactura.ROPA, desde, hasta)).thenReturn(20.0);
		when(facturaRepositorio.sumarMontoPorTipoFactura(TiposFactura.PAPELERIA, desde, hasta)).thenReturn(30.0);
		when(facturaRepositorio.sumarMontoPorTipoFactura(TiposFactura.VARIEDAD, desde, hasta)).thenReturn(0.0);
		when(repositorio.existsByCodigoIngreso(anyString())).thenReturn(true, false);
		when(repositorio.save(any(Ingreso_del_negocio.class))).thenAnswer(invocacion -> {
			Ingreso_del_negocio ingreso = invocacion.getArgument(0);
			ingreso.setIdIngreso(7);
			return ingreso;
		});
		when(historialRepositorio.save(any(Historial_ingreso_del_negocio.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

		IngresoResponse respuesta = servicio.calcularYGuardar(desde, hasta);

		assertEquals(7, respuesta.id());
		assertEquals(new BigDecimal("60.00"), respuesta.totalGeneral());
		assertEquals("Ingreso ventas", respuesta.tipoIngreso());
		ArgumentCaptor<Ingreso_del_negocio> ingresoCaptor = ArgumentCaptor.forClass(Ingreso_del_negocio.class);
		verify(repositorio).save(ingresoCaptor.capture());
		assertTrue(ingresoCaptor.getValue().getCodigoIngreso().matches("\\d{15}"));
		assertEquals(60.0, ingresoCaptor.getValue().getTotalVendidoProductos());
		ArgumentCaptor<Historial_ingreso_del_negocio> historialCaptor = ArgumentCaptor.forClass(Historial_ingreso_del_negocio.class);
		verify(historialRepositorio).save(historialCaptor.capture());
		assertEquals(7, historialCaptor.getValue().getIngresoId());
		assertTrue(historialCaptor.getValue().getMotivoIngreso().matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]{20,100}"));
		assertEquals("Rango uno mar dos cero dos seis a treinta y uno mar dos cero dos seis", historialCaptor.getValue().getMotivoIngreso());
		verify(repositorio, times(2)).existsByCodigoIngreso(anyString());
	}

	@Test
	void crearConvierteCamposNulosACero()
	{
		when(repositorio.existsByCodigoIngreso("1234567890")).thenReturn(false);
		when(repositorio.save(any(Ingreso_del_negocio.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

		Ingreso_del_negocio ingreso = servicio.crear(peticionNula());

		assertEquals(0.0, ingreso.getTotalSalarioOperadores());
		assertEquals(0.0, ingreso.getTotalVendidoRopa());
		assertEquals(0.0, ingreso.getInversionTotalPapeleria());
		assertEquals(0.0, ingreso.getTotalVendidoServicios());
		assertEquals(0.0, ingreso.getTotalVendidoProductos());
		assertEquals(0.0, ingreso.getTotalRecaudoEsperadoPapeleria());
	}

	@Test
	void actualizarConvierteCamposNulosACero()
	{
		Ingreso_del_negocio existente = new Ingreso_del_negocio("1234567890", 1, 1, 1, 1, 1, 1, "Ingreso ventas");
		existente.setIdIngreso(7);
		when(repositorio.findById(7)).thenReturn(Optional.of(existente));
		when(repositorio.existsByCodigoIngresoAndIdIngresoNot("1234567890", 7)).thenReturn(false);
		when(repositorio.save(any(Ingreso_del_negocio.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

		Ingreso_del_negocio ingreso = servicio.actualizar(7, peticionNula());

		assertEquals(0.0, ingreso.getTotalSalarioOperadores());
		assertEquals(0.0, ingreso.getTotalVendidoProductos());
		assertEquals(0.0, ingreso.getTotalRecaudoEsperadoPapeleria());
	}

	@Test
	void crearRechazaCodigoDuplicado()
	{
		when(repositorio.existsByCodigoIngreso("1234567890")).thenReturn(true);

		assertThrows(RecursoDuplicadoException.class, () -> servicio.crear(peticionNula()));

		verify(repositorio, never()).save(any(Ingreso_del_negocio.class));
	}

	@Test
	void actualizarRechazaCodigoDuplicado()
	{
		Ingreso_del_negocio existente = new Ingreso_del_negocio("1234567890", 0, 0, 0, 0, 0, 0, "Ingreso ventas");
		when(repositorio.findById(7)).thenReturn(Optional.of(existente));
		when(repositorio.existsByCodigoIngresoAndIdIngresoNot(eq("1234567890"), eq(7))).thenReturn(true);

		assertThrows(RecursoDuplicadoException.class, () -> servicio.actualizar(7, peticionNula()));

		verify(repositorio, never()).save(any(Ingreso_del_negocio.class));
	}

	private IngresoDelNegocioRequest peticionNula()
	{
		return new IngresoDelNegocioRequest("1234567890", null, null, null, null, null, null, "Ingreso ventas");
	}
}
