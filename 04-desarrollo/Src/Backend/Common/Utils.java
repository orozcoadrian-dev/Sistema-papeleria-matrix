public class Utils extends Object
{
	
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