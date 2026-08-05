package Backend.Dao.Daos;
import Backend.Dao.Base.Dao;
import Backend.Models.Usuario;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO extends Dao
{

	/**
	 * Registra de manera permanente un nuevo usuario en la base de datos.
	 * @author Orozco Adrián Dev.
	 * @param usuario
	 */
	public void insertarUsuario(Usuario usuario)
	{
		try(Connection conexion = obtenerConexion();)
		{
		// Mensaje de que fue correcto
		System.out.println("La conexión fue exitosa, lista para insertar un usuario.");
		}catch(SQLException e)
		{
			System.out.println("Error: " + e.getMessage());
		}
	}

	/**
	 * Recupera la información detallada de un usuario registrado en la base de datos a partir de su identificador único.
	 * @author Orozco Adrián Dev
	 * @param idUsuario
	 * @return {@code usuario}
	 */
	public Usuario buscarUsuarioPorPK(int idUsuario)
	{
		String sql = "SELECT * FROM Usuario WHERE idUsuario = " + idUsuario;
		Usuario usuario = null;

		try(Connection conexion = obtenerConexion();)
		{

		// Mensaje de que fue correcto
		System.out.println("La conexión fue exitosa, lista para buscar un usuario por su primary key.");
		}catch(SQLException e)
		{
			System.out.println("Error: " + e.getMessage());
		}
		return usuario;
	}

	/**
	 * Recupera la información detallada de un usuario registrado en la base de datos a partir de su número y tipo de documento.
	 * @author Orozco Adrián dev.
	 * @param numeroDocumento
	 * @param tipoDocumento
	 * @return {@code usuario}
	 */
	public Usuario buscarUsuarioPorNumeroDocumento(String numeroDocumento, String tipoDocumento)
	{
		String sql = "SELECT * FROM Usuario WHERE numero_documento = " + numeroDocumento + " AND " + "tipo_documento = " + tipoDocumento;
		Usuario usuario = null;

		try(Connection conexion = obtenerConexion();)
		{

		// Mensaje de que fue correcto
		System.out.println("La conexión fue exitosa, lista para buscar un usuario por su número de documento.");
		}catch(SQLException e)
		{
			System.out.println("Error: " + e.getMessage());
		}

		return usuario;
	}

	/**
	 * Recupera la información detallada de un usuario registrado en la base de datos a partir de su nombre de usuario.
	 * @author Orozco Adrián dev.
	 * @param nombreUsuario
	 * @return {@code usuario}
	 */
	public Usuario buscarUsuarioPorNombreUsuario(String nombreUsuario)
	{

		Usuario usuario = null;
		String sql = "SELECT * FROM Usuario WHERE nombre_usuario = " + nombreUsuario;

		try(Connection conexion = obtenerConexion();)
		{

		// Mensaje de que fue correcto
		System.out.println("La conexión fue exitosa, lista para buscar un usuario por su nombre de usuario.");
		}catch(SQLException e)
		{
			System.out.println("Error: " + e.getMessage());
		}
		return usuario;
	}

	public Usuario buscarUsuarioPorEmail(String email)
	{

		Usuario usuario = null;
		String sql = "SELECT * FROM Usuario WHERE nombre_usuario = " + email;

		try(Connection conexion = obtenerConexion();)
		{

		// Mensaje de que fue correcto
		System.out.println("La conexión fue exitosa, lista para buscar un usuario por su email.");

		
		}catch(SQLException e)
		{
			System.out.println("Error: " + e.getMessage());
		}

		return usuario;
	}

	/**
	 * Recupera la información detallada de todos los usuarios de la base de datos.
	 * @author Orozco Adrián dev.
	 * @return {@code listaUsuarios}
	 */
	public List<Usuario> listarUsuarios()
	{
		List<Usuario> listaUsuarios = new ArrayList<>();

		try(Connection conexion = obtenerConexion();)
		{

		// Mensaje de que fue correcto
		System.out.println("La conexión fue exitosa, lista para listar usuarios.");
		}catch(SQLException e)
		{
			System.out.println("Error: " + e.getMessage());
		}
		return listaUsuarios;
	}

	/**
	 * Elimina de manera permanente un usuario en la base de datos.
	 * @author Orozco Adrián dev.
	 * @param idUsuario
	 * @return none
	 */
	public void eliminarUsuario(int idUsuario)
	{
		try(Connection conexion = obtenerConexion();)
		{

		// Mensaje de que fue correcto
		System.out.println("La conexión fue exitosa, lista para eliminar un por su primary key.");
		}catch(SQLException e)
		{
			System.out.println("Error: " + e.getMessage());
		}
	}

	/**
	 * Actualizar la contraseña de un usuario mediante su identificador principal
	 * @param idUsuario
	 * @param nuevaContrasenia
	 */
	public void actualizarContraseniaUsuario(int idUsuario, String nuevaContrasenia)
	{
		String sql = "UPDATE Usuario SET contrasenia = " + nuevaContrasenia + " WHERE id_usuario = " + idUsuario;
		try(Connection conexion = obtenerConexion();)
		{

		// Mensaje de que fue correcto
		System.out.println("La conexión fue exitosa, lista para actualizar la contraseña de un usuario.");
		}catch(SQLException e)
		{
			System.out.println("Error: " + e.getMessage());
		}
	}
}