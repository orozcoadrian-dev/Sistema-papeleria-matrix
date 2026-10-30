package com.papeleriamatrix.dto;

/**
 * Respuesta de la ruta GET /api/estado.
 * @param aplicacion nombre de la aplicación.
 * @param estado estado de la API.
 * @param modo modo en que corre: local (XAMPP) o demo (Render).
 */
public record EstadoRespuesta(String aplicacion, String estado, String modo)
{
}
