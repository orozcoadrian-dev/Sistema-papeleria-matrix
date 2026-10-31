package com.papeleriamatrix.model;

import com.papeleriamatrix.common.Utils;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Entity
@Table(name = "operador_de_cibercafe")
@PrimaryKeyJoinColumn(name = "Id_usuario")
public class Operador_de_cibercafe extends Usuario
{
	@Column(name = "Salario", nullable = false)
	private double salario = 0;

	@Column(name = "Horario", length = 80)
	private String horario = "";

	@Column(name = "Direccion", nullable = false, length = 80)
	private String direccion = "";

	@Column(name = "Fecha_contrato", nullable = false)
	private LocalDate fechaContrato;

	@Transient
	private Utils utilidades = new Utils();

	public Operador_de_cibercafe()
	{
		super();
	}

	public Operador_de_cibercafe(int idUsuario, String primerNombre, String segundoNombre, String primerApellido, String segundoApellido, String numeroDocumento, String tipoDocumento, String nombreUsuario, String contrasenia, RolUsuario rol, String email, String telefono, LocalDateTime ultimaSesion, double salario, String horario, String direccion, LocalDate fechaContrato)
	{
		super(idUsuario, primerNombre, segundoNombre, primerApellido, segundoApellido, numeroDocumento, tipoDocumento, nombreUsuario, contrasenia, rol, email, telefono, ultimaSesion);
		setSalario(salario);
		setHorario(horario);
		setDireccion(direccion);
		setFechaContrato(fechaContrato);
	}

	public Operador_de_cibercafe(String primerNombre, String segundoNombre, String primerApellido, String segundoApellido, String numeroDocumento, String tipoDocumento, String nombreUsuario, String contrasenia, RolUsuario rol, String email, String telefono, LocalDateTime ultimaSesion, double salario, String horario, String direccion, LocalDate fechaContrato)
	{
		super(primerNombre, segundoNombre, primerApellido, segundoApellido, numeroDocumento, tipoDocumento, nombreUsuario, contrasenia, rol, email, telefono, ultimaSesion);
		setSalario(salario);
		setHorario(horario);
		setDireccion(direccion);
		setFechaContrato(fechaContrato);
	}

	public Operador_de_cibercafe(int idUsuario, String primerNombre, String segundoNombre, String primerApellido, String segundoApellido, String numeroDocumento, String tipoDocumento, String nombreUsuario, String contrasenia, RolUsuario rol, String email, String telefono, LocalDateTime ultimaSesion, double salario, String horario, String direccion, Date fechaContrato)
	{
		super(idUsuario, primerNombre, segundoNombre, primerApellido, segundoApellido, numeroDocumento, tipoDocumento, nombreUsuario, contrasenia, rol, email, telefono, ultimaSesion);
		setSalario(salario);
		setHorario(horario);
		setDireccion(direccion);
		setFechaContrato(fechaContrato);
	}

	public Operador_de_cibercafe(String primerNombre, String segundoNombre, String primerApellido, String segundoApellido, String numeroDocumento, String tipoDocumento, String nombreUsuario, String contrasenia, RolUsuario rol, String email, String telefono, LocalDateTime ultimaSesion, double salario, String horario, String direccion, Date fechaContrato)
	{
		super(primerNombre, segundoNombre, primerApellido, segundoApellido, numeroDocumento, tipoDocumento, nombreUsuario, contrasenia, rol, email, telefono, ultimaSesion);
		setSalario(salario);
		setHorario(horario);
		setDireccion(direccion);
		setFechaContrato(fechaContrato);
	}

	public void setSalario(double salario) throws IllegalArgumentException
	{
		if (salario == 0)
		{
			throw new IllegalArgumentException("El salario no puede ser igual a 0.");
		} else if (salario <= -1)
		{
			throw new IllegalArgumentException("El salario no puede ser negativo.");
		}
		this.salario = salario; 
	}

	public double getSalario()
	{
		return salario;
	}

	public void setHorario(String horario) throws IllegalArgumentException
	{
		if (horario == null || horario.isBlank() || horario.isEmpty())
		{
			this.horario = "";
			return;
		}
		if (horario.length() < 10)
		{
			throw new IllegalArgumentException("La longitud del horario es demasiado corta.");
		} else if (horario.length() > 80)
		{
			throw new IllegalArgumentException("La longitud del horario es demasiado larga.");
		}
		this.horario = horario; 
	}

	public String getHorario()
	{
		return horario;
	}

	public void setDireccion(String direccion) throws IllegalArgumentException
	{
		if (direccion == null || direccion.isBlank() || direccion.isEmpty())
		{
			throw new IllegalArgumentException("La dirección no puede ir vacía.");
		} else if (direccion.length() < 10)
		{
			throw new IllegalArgumentException("La longitud de la dirección es demasiado corta.");
		} else if (direccion.length() > 80)
		{
			throw new IllegalArgumentException("La longitud de la dirección es demasiado larga.");
		}
		this.direccion = direccion; 
	}

	public String getDireccion()
	{
		return direccion;
	}

	public void setFechaContrato(LocalDate fechaContrato) throws IllegalArgumentException
	{
		if (fechaContrato == null)
		{
			throw new IllegalArgumentException("La fecha de contrato no puede estar vacía.");
		}
		this.fechaContrato = fechaContrato; 
	}

	public void setFechaContrato(Date fechaContrato) throws IllegalArgumentException
	{
		if (fechaContrato == null)
		{
			throw new IllegalArgumentException("La fecha de contrato no puede estar vacía.");
		}
		this.fechaContrato = fechaContrato.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(); 
	}

	public LocalDate getFechaContrato()
	{
		return fechaContrato;
	}
}