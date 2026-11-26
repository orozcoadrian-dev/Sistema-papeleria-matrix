export interface SolicitudNativa {
  url: string;
  method: string;
  headers: Record<string, string>;
  data?: unknown;
  connectTimeout: number;
  readTimeout: number;
}

export type Solicitar = (solicitud: SolicitudNativa) => Promise<{ status: number; data: unknown }>;

/**
 * Hace el trabajo de fetch con la red de Android, que no aplica CORS:
 * así el servidor no necesita cambios para aceptar la aplicación instalada.
 */
export function crearPedirNativo(solicitar: Solicitar, espera = 65000): typeof fetch {
  return async (entrada, opciones = {}) => {
    const cuerpo = typeof opciones.body === 'string' ? opciones.body : undefined;
    const respuesta = await solicitar({
      url: String(entrada),
      method: opciones.method ?? 'GET',
      headers: { ...(opciones.headers as Record<string, string> | undefined) },
      ...(cuerpo === undefined ? {} : { data: JSON.parse(cuerpo) as unknown }),
      connectTimeout: espera,
      readTimeout: espera,
    });
    // Android entrega el JSON ya convertido en objeto, o como texto en las respuestas de error.
    const texto = typeof respuesta.data === 'string' ? respuesta.data : JSON.stringify(respuesta.data);
    return new Response(respuesta.status === 204 ? null : texto, { status: respuesta.status });
  };
}
