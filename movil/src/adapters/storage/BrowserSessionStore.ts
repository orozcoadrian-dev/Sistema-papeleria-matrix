import type { Sesion } from '../../domain/model/Session';
import type { SessionStore } from '../../domain/ports/out/SessionStore';

const SESION = 'matrix.movil.sesion';
const ULTIMO_USO = 'matrix.movil.ultimo-uso';

/** Guarda la sesión en la pestaña: al cerrarla hay que volver a entrar. */
export class BrowserSessionStore implements SessionStore {
  constructor(private readonly storage: Storage) {}

  leer(): Sesion | null {
    try {
      const guardada = JSON.parse(this.storage.getItem(SESION) ?? 'null') as Sesion | null;
      return guardada?.codigo ? guardada : null;
    } catch {
      this.borrar();
      return null;
    }
  }

  guardar(sesion: Sesion): void {
    this.storage.setItem(SESION, JSON.stringify(sesion));
  }

  borrar(): void {
    this.storage.removeItem(SESION);
    this.storage.removeItem(ULTIMO_USO);
  }

  ultimoUso(): number {
    return Number(this.storage.getItem(ULTIMO_USO)) || 0;
  }

  anotarUso(momento: number): void {
    this.storage.setItem(ULTIMO_USO, String(momento));
  }
}
