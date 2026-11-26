package com.papeleriamatrix.service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Envía los correos con la API web de Brevo. Se usa una API web porque el plan gratuito de Render
 * no deja salir correos por SMTP. Necesita las variables CORREO_CLAVE y CORREO_REMITENTE.
 */
@Component
public class CorreoPorBrevo implements EnviadorDeCorreo
{
	private final String clave;
	private final String remitente;
	private final RestClient cliente;

	public CorreoPorBrevo(@Value("${app.correo.clave:}") String clave, @Value("${app.correo.remitente:}") String remitente,
		@Value("${app.correo.direccion:https://api.brevo.com/v3/smtp/email}") String direccion)
	{
		this.clave = clave.trim();
		this.remitente = remitente.trim();
		SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
		fabrica.setConnectTimeout(Duration.ofSeconds(10));
		fabrica.setReadTimeout(Duration.ofSeconds(20));
		this.cliente = RestClient.builder().requestFactory(fabrica).baseUrl(direccion).build();
	}

	@Override
	public boolean disponible()
	{
		return !clave.isEmpty() && !remitente.isEmpty();
	}

	@Override
	public void enviar(String para, String asunto, String texto)
	{
		cliente.post()
			.header("api-key", clave)
			.contentType(MediaType.APPLICATION_JSON)
			.body(Map.of(
				"sender", Map.of("name", "Papelería Matrix", "email", remitente),
				"to", List.of(Map.of("email", para)),
				"subject", asunto,
				"textContent", texto))
			.retrieve()
			.toBodilessEntity();
	}
}
