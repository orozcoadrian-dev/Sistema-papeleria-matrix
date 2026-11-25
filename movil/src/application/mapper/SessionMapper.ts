import type { Sesion } from '../../domain/model/Session';
import type { UsuarioActual } from '../dto/responses/responses';

export const aUsuarioActual = (sesion: Sesion): UsuarioActual => ({
  nombre: sesion.nombre,
  rol: sesion.rol,
  rolNombre: sesion.rolNombre,
  esAdministrador: sesion.rol === 'ADMINISTRADOR',
});
