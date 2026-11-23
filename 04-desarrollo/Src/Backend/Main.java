package Backend;
import java.sql.SQLException;
import backend.config.ConexionBD;

public class Main {
	public static void main(String[] args)
	{
		try {
			ConexionBD.establecerConexion();
		} catch (SQLException e) {
			System.out.println("No pudo conectarse a la base de datos | Problema: " + e);
		}
	}
}