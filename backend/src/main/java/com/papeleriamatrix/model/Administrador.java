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
@Table(name = "administrador")
@PrimaryKeyJoinColumn(name = "Id_usuario")
public class Administrador extends Usuario
{
	@Column(name = "Fecha_posicionamiento", nullable = false)
	private LocalDate fechaPosicionamiento;

	@Column(name = "Area_acceso", nullable = false, length = 15)
	private String areaAcceso = "";

	@Transient
	private Utils utilidades = new Utils();

	public Administrador()
	{
		super();
	}

	public Administrador(int idUsuario, String primerNombre, String segundoNombre, String primerApellido, String segundoApellido, String numeroDocumento, String tipoDocumento, String nombreUsuario, String contrasenia, RolUsuario rol, String email, String telefono, LocalDateTime ultimaSesion, LocalDate fechaPosicionamiento, String areaAcceso)
	{
		super(idUsuario, primerNombre, segundoNombre, primerApellido, segundoApellido, numeroDocumento, tipoDocumento, nombreUsuario, contrasenia, rol, email, telefono, ultimaSesion);
		setFechaPosicionamiento(fechaPosicionamiento);
		setAreaAcceso(areaAcceso);
	}

	public Administrador(String primerNombre, String segundoNombre, String primerApellido, String segundoApellido, String numeroDocumento, String tipoDocumento, String nombreUsuario, String contrasenia, RolUsuario rol, String email, String telefono, LocalDateTime ultimaSesion, LocalDate fechaPosicionamiento, String areaAcceso)
	{
		super(primerNombre, segundoNombre, primerApellido, segundoApellido, numeroDocumento, tipoDocumento, nombreUsuario, contrasenia, rol, email, telefono, ultimaSesion);
		setFechaPosicionamiento(fechaPosicionamiento);
		setAreaAcceso(areaAcceso);
	}

	public Administrador(int idUsuario, String primerNombre, String segundoNombre, String primerApellido, String segundoApellido, String numeroDocumento, String tipoDocumento, String nombreUsuario, String contrasenia, RolUsuario rol, String email, String telefono, LocalDateTime ultimaSesion, Date fechaPosicionamiento, String areaAcceso)
	{
		super(idUsuario, primerNombre, segundoNombre, primerApellido, segundoApellido, numeroDocumento, tipoDocumento, nombreUsuario, contrasenia, rol, email, telefono, ultimaSesion);
		setFechaPosicionamiento(fechaPosicionamiento);
		setAreaAcceso(areaAcceso);
	}

	public Administrador(String primerNombre, String segundoNombre, String primerApellido, String segundoApellido, String numeroDocumento, String tipoDocumento, String nombreUsuario, String contrasenia, RolUsuario rol, String email, String telefono, LocalDateTime ultimaSesion, Date fechaPosicionamiento, String areaAcceso)
	{
		super(primerNombre, segundoNombre, primerApellido, segundoApellido, numeroDocumento, tipoDocumento, nombreUsuario, contrasenia, rol, email, telefono, ultimaSesion);
		setFechaPosicionamiento(fechaPosicionamiento);
		setAreaAcceso(areaAcceso);
	}

	public void setFechaPosicionamiento(LocalDate fechaPosicionamiento) throws IllegalArgumentException
	{
		if (fechaPosicionamiento == null)
		{
			throw new IllegalArgumentException("La fecha de posicionamiento no puede quedar vacía.");
		}
		this.fechaPosicionamiento = fechaPosicionamiento;
	}

	public void setFechaPosicionamiento(Date fechaPosicionamiento) throws IllegalArgumentException
	{
		if (fechaPosicionamiento == null)
		{
			throw new IllegalArgumentException("La fecha de posicionamiento no puede quedar vacía.");
		}
		this.fechaPosicionamiento = fechaPosicionamiento.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}

	public LocalDate getFechaPosicionamiento()
	{
		return fechaPosicionamiento;
	}

	public void setAreaAcceso(String areaAcceso) throws IllegalArgumentException
	{
		if (areaAcceso == null || areaAcceso.isBlank() || areaAcceso.isEmpty())
		{
			throw new IllegalArgumentException("El área de acceso no puede ir vacía.");
		} else if (areaAcceso.length() < 3)
		{
			throw new IllegalArgumentException("La longitud del área de acceso es demasiado corta.");
		} else if (areaAcceso.length() > 15)
		{
			throw new IllegalArgumentException("La longitud del área de acceso es demasiado larga.");
		} else if (utilidades.tieneSoloTexto(areaAcceso))
		{
			this.areaAcceso = areaAcceso;
		}
	}

	public String getAreaAcceso()
	{
		return areaAcceso;
	}
}