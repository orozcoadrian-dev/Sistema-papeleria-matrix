package Backend.Dao.Base;
import Backend.Config.ConexionBD;
import java.sql.Connection;

public abstract class Dao extends Object 
{

	/**
	 * 
	 * @author Orozco Adrián Dev.
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

	/**
	 * 
	 * @author Orozco Adrián Dev.
	 * @param conexion
	 * @return {@code Connection}
	 */
	protected void cerrarConexion(Connection conexion)
	{
		if(conexion != null)
		{
			try {
				conexion.close();
			} catch (Exception e) 
			{
				System.out.println("Error al cerrar la base de datos: " + e);
			}
		}
	}
}