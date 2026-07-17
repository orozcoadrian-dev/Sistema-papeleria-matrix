package Backend.Common;
public class Utils extends Object
{
	/**
	 * Método que valida que cualquier String solo tenga números, y no letras.
	 * @author Orozco Adrián Dev.
	 * @param texto
	 * @return {@code true}
	 * @throws IllegalArgumentException Si el argumento tiene letras o símbolos no permitidos.
	 */
	public boolean tieneSoloNumeros(String texto)
	{
		boolean soloNumeros = false;

		if(texto.matches("^[0-9]+$"))
		{
			soloNumeros = true;
		} else {
			throw new IllegalArgumentException(texto + " contiene letras, esto no es válido.");
		}

		return soloNumeros;
	}

	/**
	 * Método que valida que cualquier String solo contenta letras o símbolos, y no números.
	 * @author Orozco Adrián Dev.
	 * @param texto
	 * @return {@code true}
	 * @throws IllegalArgumentException Si el argumento tiene números.
	 */
	public boolean tieneSoloTexto(String texto)
	{
		boolean soloTexto = false;

		if(texto.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$"))
		{
			soloTexto = true;
		} else {
			throw new IllegalArgumentException(texto + " contiene números, esto no es válido.");
		}

		return soloTexto;
	}

	/**
	 * Método para verificar que el email sea válido y al menos tenga un '@' junto un '.com'
	 * @author Orozco Adrián Dev.
	 * @param email
	 * @return {@code true}
	 * @throws IllegalArgumentException Si el email no tiene un '@' o la extensión '.com'
	 */
	public boolean esEmailValido(String email)
	{
		boolean esValido = false;

		if(email.matches("^[\\w.-]+@[\\w.-]+\\.com$"))
		{
			esValido = true;
		} else {
			throw new IllegalArgumentException(email + " no es un email válido.");
		}

		return esValido;
	}
}