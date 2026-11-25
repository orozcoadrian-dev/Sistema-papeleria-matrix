import type { TipoDeArticulo } from '../constants/articulos';

export interface Venta {
  id: number;
  codigo: string;
  /** Fecha en formato AAAA-MM-DD. */
  fecha: string;
  total: number;
  operador: string;
  articulos: { tipo: TipoDeArticulo; nombre: string; cantidad: number; precioUnitario: number | null }[];
}

export interface NuevaVenta {
  articulos: { tipo: TipoDeArticulo; id: number; cantidad: number }[];
  totalEsperado: number;
  operadorId?: number;
}

export interface Operador {
  id: number;
  nombre: string;
}
