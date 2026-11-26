package com.papeleriamatrix.security;

import com.papeleriamatrix.dto.InicioSesionRequest;
import com.papeleriamatrix.dto.SesionResponse;
import com.papeleriamatrix.exception.OperacionRechazadaException;
import com.papeleriamatrix.model.Usuario;
import com.papeleriamatrix.repository.UsuarioRepository;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Revisa el usuario y la contraseña y abre la sesión.
 * Después de 5 intentos fallidos seguidos, ese usuario queda bloqueado 5 minutos.
 */
@Service
public class InicioDeSesionService
{
	private static final int INTENTOS_PERMITIDOS = 5;
	private static final Duration BLOQUEO = Duration.ofMinutes(5);
	private static final String MENSAJE_INCORRECTO = "El usuario o la contraseña no son correctos.";

	private record Intentos(int fallidos, Instant ultimo)
	{
	}

	private final UsuarioRepository usuarios;
	private final PasswordEncoder codificador;
	private final AlmacenDeSesiones sesiones;
	private final Map<String, Intentos> intentos = new ConcurrentHashMap<>();

	/** Se compara contra este código cuando el usuario no existe, para que tarde lo mismo y no se sepa qué usuarios hay. */
	private final String codigoDeRelleno;

	public InicioDeSesionService(UsuarioRepository usuarios, PasswordEncoder codificador, AlmacenDeSesiones sesiones)
	{
		this.usuarios = usuarios;
		this.codificador = codificador;
		this.sesiones = sesiones;
		this.codigoDeRelleno = codificador.encode("relleno-para-igualar-el-tiempo");
	}

	@Transactional
	public SesionResponse entrar(InicioSesionRequest peticion)
	{
		String clave = peticion.nombreUsuario().trim().toLowerCase(Locale.ROOT);
		revisarBloqueo(clave);

		Optional<Usuario> encontrado = usuarios.findByNombreUsuario(peticion.nombreUsuario().trim());
		String guardada = encontrado.map(Usuario::getContrasenia).orElse(codigoDeRelleno);
		boolean coincide = codificador.matches(peticion.contrasenia(), guardada);

		if (encontrado.isEmpty() || !coincide || encontrado.get().getRol() == null)
		{
			anotarFallo(clave);
			throw new OperacionRechazadaException(HttpStatus.UNAUTHORIZED, "No se pudo entrar", MENSAJE_INCORRECTO);
		}

		intentos.remove(clave);
		Usuario usuario = encontrado.get();
		if (!usuario.isActivo())
		{
			// Solo se dice después de comprobar la contraseña, para no revelar qué usuarios existen.
			throw new OperacionRechazadaException(HttpStatus.FORBIDDEN, "Usuario no disponible",
				"Tu usuario está " + usuario.getEstado().toLowerCase(Locale.ROOT) + ". Habla con la administradora.");
		}
		usuario.setUltimaSesion(LocalDateTime.now());
		usuarios.save(usuario);

		Sesion sesion = new Sesion(usuario.getIdUsuario(), usuario.getNombreUsuario(),
			(usuario.getPrimerNombre() + " " + usuario.getPrimerApellido()).trim(), usuario.getRol());
		return SesionResponse.desde(sesiones.abrir(sesion), sesion, sesiones.minutosDeInactividad());
	}

	public void salir(String codigo)
	{
		sesiones.cerrar(codigo);
	}

	private void revisarBloqueo(String clave)
	{
		Intentos actuales = intentos.get(clave);
		if (actuales == null)
		{
			return;
		}
		if (actuales.ultimo().plus(BLOQUEO).isBefore(Instant.now()))
		{
			intentos.remove(clave);
			return;
		}
		if (actuales.fallidos() >= INTENTOS_PERMITIDOS)
		{
			throw new OperacionRechazadaException(HttpStatus.TOO_MANY_REQUESTS, "Demasiados intentos",
				"Fallaste la contraseña varias veces. Espera 5 minutos y vuelve a intentarlo.");
		}
	}

	private void anotarFallo(String clave)
	{
		intentos.merge(clave, new Intentos(1, Instant.now()),
			(anterior, nuevo) -> new Intentos(anterior.fallidos() + 1, nuevo.ultimo()));
	}
}
