package Backend.Dao.Base;
import Backend.Config.ConexionBD;
import java.sql.Connection;

public abstract class Dao extends Object 
{

	/**
	 * Establece y retorna una conexión activa con la base de datos utilizando las credenciales configuradas.
	 * @author Orozco Adrián Dev.
	 * @return {@code Connection}
	 * @param none
	 */
	protected Connection obtenerConexion()
	{
		Connection conexion = null;
		try {
			conexion = ConexionBD.establecerConexion();
			System.out.println(conexion);
		} catch (Exception e) 
		{
			System.out.println("Error al establecer la conexión: " + e);
		}
		return conexion;
	}
}