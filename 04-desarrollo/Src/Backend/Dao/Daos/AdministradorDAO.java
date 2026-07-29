package Backend.Dao.Daos;
import Backend.Dao.Base.Dao;
import Backend.Models.Administrador;
import java.sql.Connection;

public class AdministradorDAO extends Dao
{
	
	/**
	 * Registra de manera permanente un nuevo administrador en la base de datos.
	 * @author Orozco Adrián Dev.
	 * @param administrador
	 */
	public void insertartAdministrador(Administrador administrador)
	{
		
	}

	/**
	 * Recupera la información detallada de un usuario registrado en la base de datos a partir de su identificador único.
	 * @param idUsuario
	 * @return {@code administrador}
	 */
	public Administrador buscarAdministradorPorPK(int idUsuario)
	{
		Administrador administrador = null;
		return administrador;
	}

	public void eliminarAdministrador(int idUsuario)
	{
		
	}
}
