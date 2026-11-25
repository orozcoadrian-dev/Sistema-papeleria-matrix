import type { Rol } from '../../../domain/constants/roles';
import type { Venta } from '../../../domain/model/Venta';

/** Lo que la interfaz sabe de la sesión: nunca el código secreto. */
export interface UsuarioActual {
  nombre: string;
  rol: Rol;
  rolNombre: string;
  esAdministrador: boolean;
}

export type Periodo = 'hoy' | 'semana' | 'mes';

export interface ResumenDeVentas {
  ventas: Venta[];
  total: number;
  cantidad: number;
  promedio: number;
}
