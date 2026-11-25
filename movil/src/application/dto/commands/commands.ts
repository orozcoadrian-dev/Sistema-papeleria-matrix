import type { TipoDeArticulo } from '../../../domain/constants/articulos';
import type { Carrito } from '../../../domain/model/Carrito';

export interface EntrarCommand {
  usuario: string;
  contrasenia: string;
}

export interface GuardarArticuloCommand {
  tipo: TipoDeArticulo;
  /** Sin id se crea; con id se edita. */
  id?: number;
  formulario: Record<string, string>;
  /** Unidades que había al abrir el formulario, para no borrar ventas hechas mientras tanto. */
  cantidadAnterior?: number;
}

export interface CobrarCommand {
  carrito: Carrito;
  operadorId?: number;
}
