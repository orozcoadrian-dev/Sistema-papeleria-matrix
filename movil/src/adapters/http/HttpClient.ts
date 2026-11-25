import { ErrorDeApi } from '../../domain/exceptions/errors';
import type { SessionStore } from '../../domain/ports/out/SessionStore';
import type { ProblemaResponse } from '../dto/responses/responses';

export interface Opciones {
  metodo?: 'GET' | 'POST' | 'PUT' | 'DELETE';
  cuerpo?: unknown;
}

export class HttpClient {
  private alVencer: () => void = () => {};

  constructor(
    private readonly base: string,
    private readonly store: SessionStore,
    private readonly pedir: typeof fetch = (...args) => fetch(...args),
    // Render gratis tarda cerca de un minuto en despertar.
    private readonly esperaMaxima = 65000,
  ) {}

  /** Se avisa cuando el servidor ya no acepta la sesión. */
  cuandoVenzaLaSesion(aviso: () => void): void {
    this.alVencer = aviso;
  }

  async enviar<T>(ruta: string, { metodo = 'GET', cuerpo }: Opciones = {}): Promise<T> {
    const sesion = this.store.leer();
    const headers: Record<string, string> = { Accept: 'application/json' };
    if (cuerpo !== undefined) headers['Content-Type'] = 'application/json';
    if (sesion) headers.Authorization = `Bearer ${sesion.codigo}`;

    const control = new AbortController();
    const temporizador = setTimeout(() => control.abort(), this.esperaMaxima);
    let respuesta: Response;
    try {
      respuesta = await this.pedir(this.base + ruta, {
        method: metodo, headers, signal: control.signal,
        body: cuerpo === undefined ? undefined : JSON.stringify(cuerpo),
      });
    } catch {
      throw new ErrorDeApi(0, 'Sin conexión', 'No pudimos conectar con el sistema. Revisa el internet e intenta otra vez.');
    } finally {
      clearTimeout(temporizador);
    }

    if (respuesta.status === 204) return undefined as T;
    const contenido = (await respuesta.json().catch(() => null)) as unknown;
    if (respuesta.ok && contenido !== null) return contenido as T;

    // Entrar con una contraseña equivocada también es 401, pero ahí no hay sesión que cerrar.
    if (respuesta.status === 401 && sesion && !ruta.startsWith('/sesion')) {
      this.store.borrar();
      this.alVencer();
    }
    const problema = (contenido ?? {}) as ProblemaResponse;
    throw new ErrorDeApi(
      respuesta.ok ? 502 : respuesta.status,
      problema.title ?? 'Error',
      problema.detail ?? 'Algo salió mal. Intenta otra vez en un momento.',
      problema.errores ?? {},
    );
  }
}
