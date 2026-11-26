import type { CambioDeVenta, NuevaVenta, Operador, Venta } from '../../model/Venta';

export interface SaleGateway {
  registrar(venta: NuevaVenta): Promise<Venta>;
  listar(desde: string, hasta: string): Promise<Venta[]>;
  modificar(id: number, cambio: CambioDeVenta, clavePermiso?: string): Promise<Venta>;
  anular(id: number, clavePermiso?: string): Promise<void>;
  operadores(): Promise<Operador[]>;
}
