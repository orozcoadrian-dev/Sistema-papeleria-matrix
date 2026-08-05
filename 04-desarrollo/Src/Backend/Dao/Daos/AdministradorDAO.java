package Backend.Dao.Daos;
import Backend.Dao.Base.Dao;
import Backend.Models.Administrador;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class AdministradorDAO extends Dao
{
	
	/**
	 * Registra de manera permanente un nuevo administrador en la base de datos.
	 * @author Orozco Adrián Dev.
	 * @param administrador
	 */
	public void insertarAdministrador(Administrador administrador)
	{
		try (Connection conexion = obtenerConexion()) {
			System.out.println("La conexión fue exitosa, lista para insertar un administrador.");
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}

	/**
	 * Elimina de manera permanente un administrador en la base de datos.
	 * @author Orozco Adrián dev.
	 * @param idUsuario
	 * @return none
	 */
	public void eliminarAdministrador(int idUsuario)
	{
		try (Connection conexion = obtenerConexion();) {
		
		// Mensaje de que fue correcto
		System.out.println("La conexión fue exitosa, lista para eliminar un por su primary key.");

		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}

	/**
	 * Recupera la información detallada de todos los adninistradores de la base de datos.
	 * @author Orozco Adrián dev.
	 * @return {@code administradores}
	 */
	public List<Administrador> listarAdministradores()
	{
		List<Administrador> administradores = new ArrayList<>();

		try (Connection conexion = obtenerConexion()) {
			System.out.println("La conexión fue exitosa, lista para insertar un administrador.");
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}

		return administradores;
	}

	/**
	 * Actualizar la contraseña de un usuario mediante su identificador principal
	 * @param idUsuario
	 * @param areaAcceso
	 */
	public void actualizarAreaAcceso(int idUsuario, String areaAcceso)
	{

		String sql = "UPDATE Administrador SET area_acceso = " + areaAcceso + " WHERE idUsuario =" + idUsuario;

	try (Connection conexion = obtenerConexion()) {
			System.out.println("La conexión fue exitosa, lista para insertar un administrador.");
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}

}