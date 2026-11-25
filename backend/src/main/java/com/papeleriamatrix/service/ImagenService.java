package com.papeleriamatrix.service;

import com.papeleriamatrix.exception.OperacionRechazadaException;
import com.papeleriamatrix.exception.RecursoNoEncontradoException;
import com.papeleriamatrix.repository.ProductoPapeleriaRepository;
import com.papeleriamatrix.repository.RopaRepository;
import com.papeleriamatrix.repository.ServicioRepository;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

/**
 * Fotos de los productos, la ropa y los servicios. Se guardan como archivos en la carpeta app.imagenes.carpeta
 * (una por tipo, con el id como nombre), así no hace falta cambiar la base de datos.
 * Solo se aceptan JPEG, PNG y WebP de hasta 2 MB, y el formato se revisa por el contenido, no por el nombre.
 */
@Service
public class ImagenService
{
	public static final List<String> TIPOS = List.of("productos", "ropa", "servicios");
	public static final int TAMANIO_MAXIMO = 2 * 1024 * 1024;

	private record Formato(String extension, MediaType tipo)
	{
	}

	private static final List<Formato> FORMATOS = List.of(
		new Formato("jpg", MediaType.IMAGE_JPEG),
		new Formato("png", MediaType.IMAGE_PNG),
		new Formato("webp", MediaType.parseMediaType("image/webp")));

	public record Imagen(byte[] contenido, MediaType tipo)
	{
	}

	private final Path carpeta;
	private final ProductoPapeleriaRepository productos;
	private final RopaRepository ropa;
	private final ServicioRepository servicios;

	public ImagenService(@Value("${app.imagenes.carpeta:imagenes}") String carpeta, ProductoPapeleriaRepository productos,
		RopaRepository ropa, ServicioRepository servicios)
	{
		this.carpeta = Path.of(carpeta).toAbsolutePath().normalize();
		this.productos = productos;
		this.ropa = ropa;
		this.servicios = servicios;
	}

	/** Qué elementos tienen foto: tipo → (id → versión). La versión cambia con cada foto nueva, para que el navegador no muestre la vieja. */
	public Map<String, Map<Integer, Long>> inventario()
	{
		Map<String, Map<Integer, Long>> resultado = new LinkedHashMap<>();
		for (String tipo : TIPOS)
		{
			Map<Integer, Long> fotos = new TreeMap<>();
			Path dondeVan = carpeta.resolve(tipo);
			if (Files.isDirectory(dondeVan))
			{
				try (Stream<Path> archivos = Files.list(dondeVan))
				{
					archivos.forEach(archivo -> idDelArchivo(archivo).ifPresent(id -> fotos.put(id, version(archivo))));
				} catch (IOException error)
				{
					throw new UncheckedIOException(error);
				}
			}
			resultado.put(tipo, fotos);
		}
		return resultado;
	}

	public Imagen leer(String tipo, int id)
	{
		revisarTipo(tipo);
		Path archivo = buscarArchivo(tipo, id)
			.orElseThrow(() -> new RecursoNoEncontradoException("No hay foto para ese elemento."));
		try
		{
			return new Imagen(Files.readAllBytes(archivo), formatoDe(archivo).tipo());
		} catch (IOException error)
		{
			throw new UncheckedIOException(error);
		}
	}

	public void guardar(String tipo, int id, byte[] contenido)
	{
		revisarTipo(tipo);
		revisarQueExiste(tipo, id);
		if (contenido == null || contenido.length == 0)
		{
			throw new OperacionRechazadaException(HttpStatus.BAD_REQUEST, "Foto vacía", "No llegó ninguna foto.");
		}
		if (contenido.length > TAMANIO_MAXIMO)
		{
			throw new OperacionRechazadaException(HttpStatus.PAYLOAD_TOO_LARGE, "Foto muy pesada", "La foto no puede pasar de 2 MB.");
		}
		Formato formato = detectar(contenido)
			.orElseThrow(() -> new OperacionRechazadaException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Formato no válido",
				"La foto debe ser JPG, PNG o WebP."));
		try
		{
			Path dondeVan = Files.createDirectories(carpeta.resolve(tipo));
			// Se escribe en un archivo temporal y luego se mueve, para que nadie vea una foto a medio guardar.
			Path temporal = Files.createTempFile(dondeVan, "subida-", ".tmp");
			Files.write(temporal, contenido);
			borrarArchivos(tipo, id);
			Files.move(temporal, dondeVan.resolve(id + "." + formato.extension()), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
		} catch (IOException error)
		{
			throw new UncheckedIOException(error);
		}
	}

	public void quitar(String tipo, int id)
	{
		revisarTipo(tipo);
		borrarArchivos(tipo, id);
	}

	private void revisarTipo(String tipo)
	{
		if (!TIPOS.contains(tipo))
		{
			throw new RecursoNoEncontradoException("Solo hay fotos de productos, ropa y servicios.");
		}
	}

	private void revisarQueExiste(String tipo, int id)
	{
		boolean existe = switch (tipo)
		{
			case "productos" -> productos.existsById(id);
			case "ropa" -> ropa.existsById(id);
			default -> servicios.existsById(id);
		};
		if (!existe)
		{
			throw new RecursoNoEncontradoException("No existe ese elemento para ponerle foto.");
		}
	}

	private Optional<Path> buscarArchivo(String tipo, int id)
	{
		return FORMATOS.stream()
			.map(formato -> carpeta.resolve(tipo).resolve(id + "." + formato.extension()))
			.filter(Files::isRegularFile)
			.findFirst();
	}

	private void borrarArchivos(String tipo, int id)
	{
		for (Formato formato : FORMATOS)
		{
			try
			{
				Files.deleteIfExists(carpeta.resolve(tipo).resolve(id + "." + formato.extension()));
			} catch (IOException error)
			{
				throw new UncheckedIOException(error);
			}
		}
	}

	private static Optional<Integer> idDelArchivo(Path archivo)
	{
		String nombre = archivo.getFileName().toString();
		int punto = nombre.lastIndexOf('.');
		if (punto < 1 || FORMATOS.stream().noneMatch(f -> f.extension().equals(nombre.substring(punto + 1))))
		{
			return Optional.empty();
		}
		try
		{
			return Optional.of(Integer.parseInt(nombre.substring(0, punto)));
		} catch (NumberFormatException error)
		{
			return Optional.empty();
		}
	}

	private static long version(Path archivo)
	{
		try
		{
			return Files.getLastModifiedTime(archivo).toMillis();
		} catch (IOException error)
		{
			return 0;
		}
	}

	private static Formato formatoDe(Path archivo)
	{
		String nombre = archivo.getFileName().toString();
		String extension = nombre.substring(nombre.lastIndexOf('.') + 1);
		return FORMATOS.stream().filter(f -> f.extension().equals(extension)).findFirst().orElse(FORMATOS.getFirst());
	}

	/** Reconoce el formato por los primeros bytes del archivo. */
	static Optional<Formato> detectar(byte[] b)
	{
		if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF)
		{
			return Optional.of(FORMATOS.get(0));
		}
		if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G' && b[4] == 0x0D && b[5] == 0x0A && b[6] == 0x1A && b[7] == 0x0A)
		{
			return Optional.of(FORMATOS.get(1));
		}
		if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F' && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P')
		{
			return Optional.of(FORMATOS.get(2));
		}
		return Optional.empty();
	}
}
