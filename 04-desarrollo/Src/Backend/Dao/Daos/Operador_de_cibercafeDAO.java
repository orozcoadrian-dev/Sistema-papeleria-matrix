package Backend.Dao.Daos;
import Backend.Dao.Base.Dao;
import Backend.Models.Operador_de_cibercafe;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class Operador_de_cibercafeDAO extends Dao
{

	/**
	 * Registra de manera permanente un nuevo operador de cibercafé en la base de datos.
	 * @author Orozco Adrián Dev.
	 * @param administrador
	 */
	public void insertarOperadorDeCibercafe(Operador_de_cibercafe operadorDeCibercafe)
	{
		try (Connection conexion = obtenerConexion();) {
			
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}

	/**
	 * Elimina de manera permanente un operador de cibercafé en la base de datos.
	 * @author Orozco Adrián dev.
	 * @param idUsuario
	 * @return none
	 */
	public void eliminarOperadorDeCibercafe(int idUsuario)
	{
		String sql = "DELETE FROM Operador_de_cibercafe WHERE Id_usuario = " + idUsuario;
		
		try (Connection conexion = obtenerConexion();) {
			
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}

	/**
	 * Recupera la información detallada de todos los adninistradores de la base de datos.
	 * @author Orozco Adrián dev.
	 * @return {@code administradores}
	 */
	public List<Operador_de_cibercafe> listarOperadoresDeCibercafe()
	{
		List<Operador_de_cibercafe> listaOperadoresDeCibercafe = new ArrayList<>();

		try (Connection conexion = obtenerConexion();) {
			
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}

		return listaOperadoresDeCibercafe;
	}
	
	/**
	 * Recupera la información detallada de un operador de cibercafé registrado en la base de datos a partir de su identificador único.
	 * @author Orozco Adrián Dev
	 * @param idUsuario
	 * @return {@code operadorDeCibercafe}
	 */
	public Operador_de_cibercafe buscarOperadorDeCibercafePorId(int idUsuario)
	{
		Operador_de_cibercafe operadorDeCibercafe = null;

		try (Connection conexion = obtenerConexion();) {
			
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}

		return operadorDeCibercafe;
	}

	/**
	 * Actualizar el salario de un operador de cibercafé mediante su identificador principal
	 * @author Orozco Adrián Dev.
	 * @param operadorDeCibercafe
	 */
	public void actualizarSalarioOperadorDeCibercafe(Operador_de_cibercafe operadorDeCibercafe)
	{
		try (Connection conexion = obtenerConexion();) {
			
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}

	/**
	 * Actualizar el horario de un operador de cibercafé mediante su identificador principal
	 * @author Orozco Adrián Dev.
	 * @param operadorDeCibercafe
	 */
	public void actualizarHorarioOperadorDeCibercafe(Operador_de_cibercafe operadorDeCibercafe)
	{
		try (Connection conexion = obtenerConexion();) {
			
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}

	/**
	 * Actualizar la dirección residencial de un operador de cibercafé mediante su identificador principal
	 * @author Orozco Adrián Dev.
	 * @param operadorDeCibercafe
	 */
	public void actualizarDireccionOperadorDeCibercafe(Operador_de_cibercafe operadorDeCibercafe)
	{
		try (Connection conexion = obtenerConexion();) {
			
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}
}