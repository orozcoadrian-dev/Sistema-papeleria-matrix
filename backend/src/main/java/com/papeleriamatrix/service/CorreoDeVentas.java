package com.papeleriamatrix.service;

import com.papeleriamatrix.dto.CorreoDeVentaRequest;
import com.papeleriamatrix.dto.VentaResponse;
import com.papeleriamatrix.exception.OperacionRechazadaException;
import com.papeleriamatrix.security.Sesion;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

/** Envía por correo el registro de una venta. El correo del cliente no se guarda. */
@Service
public class CorreoDeVentas
{
	private static final Logger REGISTRO = LoggerFactory.getLogger(CorreoDeVentas.class);
	private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("d/MM/yyyy");

	private final VentaService ventas;
	private final EnviadorDeCorreo enviador;

	public CorreoDeVentas(VentaService ventas, EnviadorDeCorreo enviador)
	{
		this.ventas = ventas;
		this.enviador = enviador;
	}

	public void enviar(Integer id, CorreoDeVentaRequest peticion, Sesion sesion)
	{
		// Revisa que la venta exista y que esta persona pueda verla.
		VentaResponse venta = ventas.obtener(id, sesion);
		if (venta.anulada())
		{
			throw new OperacionRechazadaException(HttpStatus.CONFLICT, "Venta anulada", "Esta venta se anuló y no se puede enviar.");
		}
		if (!enviador.disponible())
		{
			throw new OperacionRechazadaException(HttpStatus.SERVICE_UNAVAILABLE, "Correo sin configurar",
				"El envío desde el sistema todavía no está configurado. Puedes enviarlo desde tu propio correo.");
		}
		try
		{
			enviador.enviar(peticion.correo().trim(), "Tu compra en Papelería Matrix", texto(venta, peticion.nombre()));
		}
		catch (RestClientException error)
		{
			REGISTRO.warn("No se pudo enviar el correo de la venta {}: {}", id, error.getMessage());
			throw new OperacionRechazadaException(HttpStatus.BAD_GATEWAY, "No se pudo enviar",
				"El correo no salió. Revisa la dirección e intenta otra vez en unos minutos.");
		}
	}

	static String texto(VentaResponse venta, String nombre)
	{
		StringBuilder texto = new StringBuilder();
		texto.append(nombre == null || nombre.isBlank() ? "Hola:" : "Hola, " + nombre.trim() + ":").append("\n\n");
		texto.append("Este es el registro de tu compra en Papelería Matrix.\n\n");
		texto.append("Registro n.º ").append(venta.codigo()).append('\n');
		texto.append("Fecha: ").append(venta.fecha().format(FECHA)).append('\n');
		texto.append("Te atendió: ").append(venta.operador()).append("\n\n");
		for (VentaResponse.Linea linea : venta.articulos())
		{
			texto.append("- ").append(linea.cantidad()).append(" x ").append(linea.nombre()).append('\n');
		}
		texto.append("\nTotal pagado: ").append(pesos(venta.total())).append("\n\n");
		texto.append("¡Gracias por tu compra!\n");
		return texto.toString();
	}

	private static String pesos(double valor)
	{
		return "$ " + String.format(Locale.ROOT, "%,.0f", valor).replace(',', '.');
	}
}
