package com.papeleriamatrix.model;

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
import jakarta.persistence.Transient;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Entity
@Table(name = "informacion_del_negocio")
public class Informacion_del_negocio 
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Id_informacion")
	private Integer idInformacion;

	@Column(name = "Usuario_id", nullable = false)
	private Integer usuarioId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Usuario_id", insertable = false, updatable = false)
	private Administrador administrador;

	@Column(name = "Valor_informacion", nullable = false)
	private Integer valorInformacion = 0;

	@Column(name = "Tipo_informacion", nullable = false, length = 15)
	private String tipoInformacion = "";

	@Column(name = "Informacion", nullable = false, length = 255)
	private String informacion = "";

	@Column(name = "Fecha_creacion", nullable = false)
	private LocalDate fechaCreacion;

	@Column(name = "Fecha_actualizacion", nullable = false)
	private LocalDate fechaActualizacion;

	@Transient
	private Utils utilidades = new Utils();

	public Informacion_del_negocio()
	{
	}

	public Informacion_del_negocio(int idInformacion, int usuarioId, int valorInformacion, String tipoInformacion, String informacion, LocalDate fechaCreacion, LocalDate fechaActualizacion)
	{
		this.idInformacion = idInformacion;
		setUsuarioId(usuarioId);
		setValorInformacion(valorInformacion);
		setTipoInformacion(tipoInformacion);
		setInformacion(informacion);
		setFechaCreacion(fechaCreacion);
		setFechaActualizacion(fechaActualizacion);
	}

	public Informacion_del_negocio(int usuarioId, int valorInformacion, String tipoInformacion, String informacion, LocalDate fechaCreacion, LocalDate fechaActualizacion)
	{
		setUsuarioId(usuarioId);
		setValorInformacion(valorInformacion);
		setTipoInformacion(tipoInformacion);
		setInformacion(informacion);
		setFechaCreacion(fechaCreacion);
		setFechaActualizacion(fechaActualizacion);
	}

	public Informacion_del_negocio(int idInformacion, int usuarioId, int valorInformacion, String tipoInformacion, String informacion, Date fechaCreacion, Date fechaActualizacion)
	{
		this.idInformacion = idInformacion;
		setUsuarioId(usuarioId);
		setValorInformacion(valorInformacion);
		setTipoInformacion(tipoInformacion);
		setInformacion(informacion);
		setFechaCreacion(fechaCreacion);
		setFechaActualizacion(fechaActualizacion);
	}

	public Informacion_del_negocio(int usuarioId, int valorInformacion, String tipoInformacion, String informacion, Date fechaCreacion, Date fechaActualizacion)
	{
		setUsuarioId(usuarioId);
		setValorInformacion(valorInformacion);
		setTipoInformacion(tipoInformacion);
		setInformacion(informacion);
		setFechaCreacion(fechaCreacion);
		setFechaActualizacion(fechaActualizacion);
	}

	public int getIdInformacion()
	{
		return idInformacion != null ? idInformacion : 0;
	}

	public void setIdInformacion(int idInformacion)
	{
		this.idInformacion = idInformacion;
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

	public Administrador getAdministrador()
	{
		return administrador;
	}

	public void setAdministrador(Administrador administrador)
	{
		this.administrador = administrador;
		if (administrador != null)
		{
			this.usuarioId = administrador.getIdUsuario();
		}
	}

	public void setValorInformacion(int valorInformacion) throws IllegalArgumentException
	{
		if (valorInformacion == 0)
		{
			throw new IllegalArgumentException("El valor de la información no puede ser 0.");
		} else if (valorInformacion <= -1)
		{
			throw new IllegalArgumentException("El valor de la información no puede ser negativo.");
		}
		this.valorInformacion = valorInformacion;
	}

	public int getValorInformacion()
	{
		return valorInformacion != null ? valorInformacion : 0;
	}

	public void setTipoInformacion(String tipoInformacion) throws IllegalArgumentException
	{
		if (tipoInformacion == null || tipoInformacion.isEmpty() || tipoInformacion.isBlank())
		{
			throw new IllegalArgumentException("El tipo de información no puede estar vacío.");
		} else if (tipoInformacion.length() < 10)
		{
			throw new IllegalArgumentException("La longitud del tipo de información es demasiado corta.");
		} else if (tipoInformacion.length() > 15)
		{
			throw new IllegalArgumentException("El tipo de información no puede superar una longitud de 15 caracteres.");
		} else if (utilidades.tieneSoloTexto(tipoInformacion))
		{
			this.tipoInformacion = tipoInformacion;
		}
	}

	public String getTipoInformacion()
	{
		return tipoInformacion;
	}

	public void setInformacion(String informacion) throws IllegalArgumentException
	{
		if (informacion == null || informacion.isEmpty() || informacion.isBlank())
		{
			throw new IllegalArgumentException("La información no puede estar vacía.");
		} else if (informacion.length() < 10)
		{
			throw new IllegalArgumentException("La longitud de la información es demasiado corta.");
		} else if (informacion.length() > 255)
		{
			throw new IllegalArgumentException("La información no puede superar una longitud de 255 caracteres.");
		} else if (utilidades.tieneSoloTexto(informacion))
		{
			this.informacion = informacion;
		}
	}

	public String getInformacion()
	{
		return informacion;
	}

	public void setFechaCreacion(LocalDate fechaCreacion) throws IllegalArgumentException
	{
		if (fechaCreacion == null)
		{
			throw new IllegalArgumentException("La fecha de creación no puede estar vacía.");
		}
		this.fechaCreacion = fechaCreacion;
	}

	public void setFechaCreacion(Date fechaCreacion) throws IllegalArgumentException
	{
		if (fechaCreacion == null)
		{
			throw new IllegalArgumentException("La fecha de creación no puede estar vacía.");
		}
		this.fechaCreacion = fechaCreacion.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}

	public LocalDate getFechaCreacion()
	{
		return fechaCreacion;
	}

	public void setFechaActualizacion(LocalDate fechaActualizacion) throws IllegalArgumentException
	{
		if (fechaActualizacion == null)
		{
			throw new IllegalArgumentException("La fecha de actualización no puede estar vacía.");
		}
		this.fechaActualizacion = fechaActualizacion;
	}

	public void setFechaActualizacion(Date fechaActualizacion) throws IllegalArgumentException
	{
		if (fechaActualizacion == null)
		{
			throw new IllegalArgumentException("La fecha de actualización no puede estar vacía.");
		}
		this.fechaActualizacion = fechaActualizacion.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}

	public LocalDate getFechaActualizacion()
	{
		return fechaActualizacion;
	}
}