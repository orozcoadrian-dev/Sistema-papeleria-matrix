import type { NuevaVenta, Operador, Venta } from '../../model/Venta';

export interface SaleGateway {
  registrar(venta: NuevaVenta): Promise<Venta>;
  listar(desde: string, hasta: string): Promise<Venta[]>;
  anular(id: number): Promise<void>;
  operadores(): Promise<Operador[]>;
}
