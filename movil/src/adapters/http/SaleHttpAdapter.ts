import type { CambioDeVenta, NuevaVenta, Operador, Venta } from '../../domain/model/Venta';
import type { SaleGateway } from '../../domain/ports/out/SaleGateway';
import type { VentaRequest } from '../dto/requests/requests';
import type { OperadorResponse, VentaResponse } from '../dto/responses/responses';
import { aOperador, aVenta } from '../mapper/ApiMapper';
import type { HttpClient } from './HttpClient';

/** La clave viaja codificada para que admita tildes y eñes. */
const permiso = (clave?: string): Record<string, string> | undefined =>
  clave ? { 'X-Clave-Permiso': encodeURIComponent(clave) } : undefined;

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

  async modificar(id: number, cambio: CambioDeVenta, clavePermiso?: string): Promise<Venta> {
    const cuerpo: VentaRequest = cambio;
    return aVenta(await this.http.enviar<VentaResponse>(`/ventas/${id}`, { metodo: 'PUT', cuerpo, cabeceras: permiso(clavePermiso) }));
  }

  anular(id: number, clavePermiso?: string): Promise<void> {
    return this.http.enviar<void>(`/ventas/${id}`, { metodo: 'DELETE', cabeceras: permiso(clavePermiso) });
  }

  async operadores(): Promise<Operador[]> {
    return (await this.http.enviar<OperadorResponse[]>('/operadores-cibercafe')).map(aOperador);
  }
}
