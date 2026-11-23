package com.papeleriamatrix.model;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ValidacionesT8Test
{
	@Test
	void ingresoRechazaCodigoNoNumerico()
	{
		Ingreso_del_negocio ingreso = new Ingreso_del_negocio();
		assertThrows(IllegalArgumentException.class, () -> ingreso.setCodigoIngreso("123456789a"));
	}

	@Test
	void ingresoRechazaTipoConDigitos()
	{
		Ingreso_del_negocio ingreso = new Ingreso_del_negocio();
		assertThrows(IllegalArgumentException.class, () -> ingreso.setTipoIngreso("Tipo123456"));
	}

	@Test
	void estadisticaRechazaCodigoNoNumerico()
	{
		Estadistica_del_negocio estadistica = new Estadistica_del_negocio();
		assertThrows(IllegalArgumentException.class, () -> estadistica.setCodigoEstadistica("12345678901234a"));
	}

	@Test
	void historialIngresoRechazaMotivoConDigitos()
	{
		Historial_ingreso_del_negocio historial = new Historial_ingreso_del_negocio();
		assertThrows(IllegalArgumentException.class, () -> historial.setMotivoIngreso("Motivo con fecha 2026 informada"));
	}

	@Test
	void historialEstadisticaRechazaMotivoConDigitos()
	{
		Historial_estadistica_del_negocio historial = new Historial_estadistica_del_negocio();
		assertThrows(IllegalArgumentException.class, () -> historial.setMotivoEstadistica("Motivo con fecha 2026 informada"));
	}

	@Test
	void reporteRechazaCodigoNoNumerico()
	{
		ReporteDelNegocio reporte = new ReporteDelNegocio();
		assertThrows(IllegalArgumentException.class, () -> reporte.setCodigoReporte("1234567890123456789a"));
	}
}
