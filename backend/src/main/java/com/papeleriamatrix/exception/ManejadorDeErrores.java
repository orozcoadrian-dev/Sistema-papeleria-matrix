package com.papeleriamatrix.exception;

import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Convierte los errores en respuestas claras para quien consume la API.
 * Nunca se muestra el detalle técnico: ese queda en el registro del servidor (RNF-05).
 */
@RestControllerAdvice
public class ManejadorDeErrores
{
	private static final Logger registro = LoggerFactory.getLogger(ManejadorDeErrores.class);

	@ExceptionHandler(RecursoNoEncontradoException.class)
	public ProblemDetail noEncontrado(RecursoNoEncontradoException error)
	{
		return respuesta(HttpStatus.NOT_FOUND, "No encontrado", error.getMessage());
	}

	@ExceptionHandler(RecursoDuplicadoException.class)
	public ProblemDetail duplicado(RecursoDuplicadoException error)
	{
		return respuesta(HttpStatus.CONFLICT, "Código repetido", error.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail datosInvalidos(MethodArgumentNotValidException error)
	{
		Map<String, String> errores = new LinkedHashMap<>();
		error.getBindingResult().getFieldErrors().forEach(campo -> errores.putIfAbsent(campo.getField(), campo.getDefaultMessage()));

		ProblemDetail detalle = respuesta(HttpStatus.BAD_REQUEST, "Datos inválidos", "Revisa los datos enviados.");
		detalle.setProperty("errores", errores);
		return detalle;
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ProblemDetail cuerpoIlegible(HttpMessageNotReadableException error)
	{
		return respuesta(HttpStatus.BAD_REQUEST, "Cuerpo ilegible", "El cuerpo de la petición no es un JSON válido.");
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ProblemDetail parametroInvalido(MethodArgumentTypeMismatchException error)
	{
		return respuesta(HttpStatus.BAD_REQUEST, "Parámetro inválido", "El valor de " + error.getName() + " no es válido.");
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ProblemDetail rutaInexistente(NoResourceFoundException error)
	{
		return respuesta(HttpStatus.NOT_FOUND, "Ruta no encontrada", "La ruta solicitada no existe en esta API.");
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ProblemDetail metodoNoPermitido(HttpRequestMethodNotSupportedException error)
	{
		return respuesta(HttpStatus.METHOD_NOT_ALLOWED, "Método no permitido", "Esta ruta no admite el método " + error.getMethod() + ".");
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ProblemDetail restriccionDeLaBase(DataIntegrityViolationException error)
	{
		registro.warn("Restricción de la base de datos: {}", error.getMostSpecificCause().getMessage());
		return respuesta(HttpStatus.CONFLICT, "Operación no permitida", "La operación no se puede hacer porque otros datos dependen de este registro.");
	}

	@ExceptionHandler(Exception.class)
	public ProblemDetail errorInesperado(Exception error)
	{
		registro.error("Error inesperado", error);
		return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Error del servidor", "Ocurrió un problema. Intenta de nuevo en unos minutos.");
	}

	private ProblemDetail respuesta(HttpStatus estado, String titulo, String detalle)
	{
		ProblemDetail problema = ProblemDetail.forStatusAndDetail(estado, detalle);
		problema.setTitle(titulo);
		return problema;
	}
}
