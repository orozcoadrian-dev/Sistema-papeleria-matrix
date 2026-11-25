import type { Sesion } from '../../model/Session';

export interface SessionStore {
  leer(): Sesion | null;
  guardar(sesion: Sesion): void;
  borrar(): void;
  ultimoUso(): number;
  anotarUso(momento: number): void;
}
