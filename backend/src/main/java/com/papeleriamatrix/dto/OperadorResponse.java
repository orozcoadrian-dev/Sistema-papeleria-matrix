package com.papeleriamatrix.dto;

import com.papeleriamatrix.model.Operador_de_cibercafe;
import com.papeleriamatrix.model.RolUsuario;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record OperadorResponse(
	int idUsuario,
	String primerNombre,
	String segundoNombre,
	String primerApellido,
	String segundoApellido,
	String numeroDocumento,
	String tipoDocumento,
	String nombreUsuario,
	RolUsuario rol,
	String email,
	String telefono,
	LocalDateTime ultimaSesion,
	double salario,
	String horario,
	String direccion,
	LocalDate fechaContrato,
	String estado
)
{
	public static OperadorResponse desde(Operador_de_cibercafe operador)
	{
		return new OperadorResponse(
			operador.getIdUsuario(),
			operador.getPrimerNombre(),
			operador.getSegundoNombre(),
			operador.getPrimerApellido(),
			operador.getSegundoApellido(),
			operador.getNumeroDocumento(),
			operador.getTipoDocumento(),
			operador.getNombreUsuario(),
			operador.getRol(),
			operador.getEmail(),
			operador.getTelefono(),
			operador.getUltima_sesion(),
			operador.getSalario(),
			operador.getHorario(),
			operador.getDireccion(),
			operador.getFechaContrato(),
			operador.getEstado()
		);
	}
}
