package Backend.Config;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD extends Object
{
	// Variables como user, password, port y host, pueden cambiar según la configuración de entorno del desarrollador.
	private static final String USER = "root";
	private static final String PASSWORD = "";
	private static final String DATABASE = "papeleria_matrix";
	private static final String PORT = "3306";
	private static final String HOST = "localhost";
	private static final String URL = "jdbc:mysql://"+HOST+":"+PORT+"/"+DATABASE;

	public static Connection establecerConexion() throws SQLException
	{
		Connection conexion = DriverManager.getConnection(URL, USER, PASSWORD);
		System.out.println(DATABASE + " pudo conectarse exitosamente.");

		// Devolvemos la conexión
		return conexion;
	}
}