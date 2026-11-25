import type { Rol } from '../constants/roles';

export interface Sesion {
  codigo: string;
  idUsuario: number;
  nombreUsuario: string;
  nombre: string;
  rol: Rol;
  rolNombre: string;
  minutosInactividad: number;
}

export function sesionVencida(sesion: Sesion, ultimoUso: number, ahora: number): boolean {
  return (ahora - ultimoUso) / 60000 > sesion.minutosInactividad;
}
