package com.papeleriamatrix.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.papeleriamatrix.common.Utils;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Entity
@Table(name = "clave_de_permiso")
public class Clave_de_permiso 
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Id_clave")
	private Integer idClave;

	@Column(name = "Usuario_id", nullable = false, unique = true)
	private Integer usuarioId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Usuario_id", insertable = false, updatable = false)
	@JsonIgnore
	private Operador_de_cibercafe operadorDeCibercafe;

	@Column(name = "Clave", nullable = false, length = 60)
	@JsonIgnore
	private String clave = "";

	@Column(name = "Fecha_creacion", nullable = false)
	private LocalDate fechaCreacion;

	@Column(name = "Fecha_actualizacion")
	private LocalDate fechaActualizacion;

	public Clave_de_permiso()
	{
	}

	public Clave_de_permiso(int idClave, int usuarioId, String clave, LocalDate fechaCreacion, LocalDate fechaActualizacion)
	{
		this.idClave = idClave;
		setUsuarioId(usuarioId);
		setClave(clave);
		setFechaCreacion(fechaCreacion);
		setFechaActualizacion(fechaActualizacion);
	}

	public Clave_de_permiso(int usuarioId, String clave, LocalDate fechaCreacion, LocalDate fechaActualizacion)
	{
		setUsuarioId(usuarioId);
		setClave(clave);
		setFechaCreacion(fechaCreacion);
		setFechaActualizacion(fechaActualizacion);
	}

	public Clave_de_permiso(int idClave, int usuarioId, String clave, Date fechaCreacion, Date fechaActualizacion)
	{
		this.idClave = idClave;
		setUsuarioId(usuarioId);
		setClave(clave);
		setFechaCreacion(fechaCreacion);
		setFechaActualizacion(fechaActualizacion);
	}

	public Clave_de_permiso(int usuarioId, String clave, Date fechaCreacion, Date fechaActualizacion)
	{
		setUsuarioId(usuarioId);
		setClave(clave);
		setFechaCreacion(fechaCreacion);
		setFechaActualizacion(fechaActualizacion);
	}

	public int getIdClave()
	{
		return idClave != null ? idClave : 0;
	}

	public void setIdClave(int idClave)
	{
		this.idClave = idClave;
	}

	public void setUsuarioId(int usuarioId) throws IllegalArgumentException
	{
		if (usuarioId == 0)
		{
			throw new IllegalArgumentException("El ID del usuario no puede ser 0.");
		} else if (usuarioId <= -1)
		{
			throw new IllegalArgumentException("El ID del usuario no puede ser negativo.");
		}
		this.usuarioId = usuarioId;
	}

	public int getUsuarioId()
	{
		return usuarioId != null ? usuarioId : 0;
	}

	@JsonIgnore
	public Operador_de_cibercafe getOperadorDeCibercafe()
	{
		return operadorDeCibercafe;
	}

	public void setOperadorDeCibercafe(Operador_de_cibercafe operadorDeCibercafe)
	{
		this.operadorDeCibercafe = operadorDeCibercafe;
		if (operadorDeCibercafe != null)
		{
			this.usuarioId = operadorDeCibercafe.getIdUsuario();
		}
	}

	public void setClave(String clave) throws IllegalArgumentException
	{
		if (!Utils.esHashBCrypt(clave))
		{
			throw new IllegalArgumentException("La clave debe almacenarse con BCrypt.");
		}
		this.clave = clave;
	}

	@JsonIgnore
	public String getClave()
	{
		return clave;
	}

	public void setFechaCreacion(LocalDate fechaCreacion) throws IllegalArgumentException
	{
		if (fechaCreacion == null)
		{
			throw new IllegalArgumentException("La fecha de creación no puede ir vacía.");
		}
		this.fechaCreacion = fechaCreacion;
	}

	public void setFechaCreacion(Date fechaCreacion) throws IllegalArgumentException
	{
		if (fechaCreacion == null)
		{
			throw new IllegalArgumentException("La fecha de creación no puede ir vacía.");
		}
		this.fechaCreacion = fechaCreacion.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}

	public LocalDate getFechaCreacion()
	{
		return fechaCreacion;
	}

	public void setFechaActualizacion(LocalDate fechaActualizacion) throws IllegalArgumentException
	{
		this.fechaActualizacion = fechaActualizacion;
	}

	public void setFechaActualizacion(Date fechaActualizacion) throws IllegalArgumentException
	{
		if (fechaActualizacion != null)
		{
			this.fechaActualizacion = fechaActualizacion.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		} else 
		{
			this.fechaActualizacion = null;
		}
	}

	public LocalDate getFechaActualizacion()
	{
		return fechaActualizacion;
	}
}