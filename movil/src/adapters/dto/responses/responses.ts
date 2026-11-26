import type { TipoDeArticulo } from '../../../domain/constants/articulos';

export interface SesionResponse {
  codigo: string;
  idUsuario: number;
  nombreUsuario: string;
  nombre: string;
  rol: string;
  rolNombre: string;
  minutosInactividad: number;
}

export interface ServicioResponse {
  id: number;
  codigo: string;
  nombre: string;
  categoria: string;
  precio: number;
  descripcion: string | null;
}

export interface ProductoInternoResponse {
  id: number;
  codigo: string;
  nombre: string;
  descripcion: string | null;
  precio: number;
  cantidad: number;
  /** El operador no recibe los costos. */
  precioCompra: number | null;
  lugarCompra: string | null;
}

export interface RopaResponse {
  id: number;
  codigo: string;
  nombre: string | null;
  tipo: string;
  talla: string;
  color: string | null;
  material: string;
  descripcion: string | null;
  precio: number;
  disponible: boolean;
}

export interface VentaResponse {
  id: number;
  codigo: string;
  fecha: string;
  total: number;
  operador: string;
  /** Tipo de factura. */
  tipo: string;
  articulos: { tipo: TipoDeArticulo; id: number | null; nombre: string; cantidad: number; precioUnitario: number | null }[];
}

export interface OperadorResponse {
  idUsuario: number;
  primerNombre: string;
  primerApellido: string;
  estado?: string;
}

/** Formato de error de la API (application/problem+json). */
export interface ProblemaResponse {
  title?: string;
  detail?: string;
  errores?: Record<string, string>;
}
