package Backend.Dao.Daos;
import Backend.Dao.Base.Dao;
import Backend.Models.Usuario;
import java.sql.Connection;

public class UsuarioDAO extends Dao
{

	/**
	 * Registra de manera permanente un nuevo usuario en la base de datos.
	 * @author Orozco Adrián Dev.
	 * @param usuario
	 */
	public void insertarUsuario(Usuario usuario)
	{
		Connection conexion = obtenerConexion();
	
		cerrarConexion(conexion);
	}

	/**
	 * Recupera la información detallada de un usuario registrado en la base de datos a partir de su identificador único.
	 * @author Orozco Adrián Dev
	 * @param idUsuario
	 * @return {@code usuario}
	 */
	public Usuario buscarUsuario(int idUsuario)
	{
		Usuario usuario = null;

		// Abrimos la conexión
		Connection conexion = obtenerConexion();
	
		//Cerramos la conexión
		cerrarConexion(conexion);
		
		return usuario;
	}

}