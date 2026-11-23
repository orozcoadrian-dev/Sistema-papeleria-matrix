package com.papeleriamatrix.service;

import java.time.LocalDate;
import java.util.Locale;

final class RangoFechasEnPalabras
{
	private static final String[] DIAS = {
		"", "uno", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nueve",
		"diez", "once", "doce", "trece", "catorce", "quince", "dieciséis", "diecisiete",
		"dieciocho", "diecinueve", "veinte", "veintiuno", "veintidós", "veintitrés",
		"veinticuatro", "veinticinco", "veintiséis", "veintisiete", "veintiocho",
		"veintinueve", "treinta", "treinta y uno"
	};
	private static final String[] MESES = {
		"", "ene", "feb", "mar", "abr", "may", "jun",
		"jul", "ago", "sep", "oct", "nov", "dic"
	};
	private static final String[] DIGITOS = {
		"cero", "uno", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nueve"
	};

	private RangoFechasEnPalabras()
	{
	}

	static String desdeHasta(LocalDate desde, LocalDate hasta)
	{
		String motivo = "Rango " + fechaEnPalabras(desde) + " a " + fechaEnPalabras(hasta);
		if (motivo.length() < 20 || motivo.length() > 100)
		{
			throw new IllegalArgumentException("El rango de fechas no cabe en el motivo del historial.");
		}
		return motivo;
	}

	private static String fechaEnPalabras(LocalDate fecha)
	{
		if (fecha.getYear() < 0 || fecha.getYear() > 9999)
		{
			throw new IllegalArgumentException("El año de la fecha debe tener cuatro dígitos como máximo.");
		}
		// El motivo usa solo letras y espacios, como exige Utils.tieneSoloTexto.
		String anio = String.format(Locale.ROOT, "%04d", fecha.getYear());
		StringBuilder anioEnPalabras = new StringBuilder();
		for (int indice = 0; indice < anio.length(); indice++)
		{
			if (indice > 0) anioEnPalabras.append(' ');
			anioEnPalabras.append(DIGITOS[anio.charAt(indice) - '0']);
		}
		return DIAS[fecha.getDayOfMonth()] + " " + MESES[fecha.getMonthValue()] + " " + anioEnPalabras;
	}
}
