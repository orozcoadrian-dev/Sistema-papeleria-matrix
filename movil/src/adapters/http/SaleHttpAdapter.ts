import type { NuevaVenta, Operador, Venta } from '../../domain/model/Venta';
import type { SaleGateway } from '../../domain/ports/out/SaleGateway';
import type { VentaRequest } from '../dto/requests/requests';
import type { OperadorResponse, VentaResponse } from '../dto/responses/responses';
import { aOperador, aVenta } from '../mapper/ApiMapper';
import type { HttpClient } from './HttpClient';

export class SaleHttpAdapter implements SaleGateway {
  constructor(private readonly http: HttpClient) {}

  async registrar(venta: NuevaVenta): Promise<Venta> {
    const cuerpo: VentaRequest = venta;
    return aVenta(await this.http.enviar<VentaResponse>('/ventas', { metodo: 'POST', cuerpo }));
  }

  async listar(desde: string, hasta: string): Promise<Venta[]> {
    const consulta = new URLSearchParams({ desde, hasta });
    return (await this.http.enviar<VentaResponse[]>(`/ventas?${consulta}`)).map(aVenta);
  }

  anular(id: number): Promise<void> {
    return this.http.enviar<void>(`/ventas/${id}`, { metodo: 'DELETE' });
  }

  async operadores(): Promise<Operador[]> {
    return (await this.http.enviar<OperadorResponse[]>('/operadores-cibercafe')).map(aOperador);
  }
}
