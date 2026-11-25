import type { TipoDeArticulo } from '../../../domain/constants/articulos';

export interface InicioSesionRequest {
  nombreUsuario: string;
  contrasenia: string;
}

export interface VentaRequest {
  articulos: { tipo: TipoDeArticulo; id: number; cantidad: number }[];
  totalEsperado: number;
  operadorId?: number;
}

export type ArticuloRequest = Record<string, string | number>;
