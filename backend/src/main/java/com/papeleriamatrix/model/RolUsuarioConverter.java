package com.papeleriamatrix.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RolUsuarioConverter implements AttributeConverter<RolUsuario, String>
{
	@Override
	public String convertToDatabaseColumn(RolUsuario attribute)
	{
		return attribute != null ? attribute.getRol() : null;
	}

	@Override
	public RolUsuario convertToEntityAttribute(String dbData)
	{
		return dbData != null ? RolUsuario.fromString(dbData) : null;
	}
}
