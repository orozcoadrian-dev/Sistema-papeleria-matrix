import type { TipoDeArticulo } from '../../domain/constants/articulos';
import type { Articulo, DatosDeArticulo } from '../../domain/model/Articulo';
import type { CatalogGateway } from '../../domain/ports/out/CatalogGateway';
import type { ProductoInternoResponse, RopaResponse, ServicioResponse } from '../dto/responses/responses';
import { aArticuloRequest, deProducto, deRopa, deServicio } from '../mapper/ApiMapper';
import type { HttpClient } from './HttpClient';

const RUTA: Record<TipoDeArticulo, string> = { servicio: '/servicios', producto: '/productos', ropa: '/ropa' };

export class CatalogHttpAdapter implements CatalogGateway {
  constructor(private readonly http: HttpClient) {}

  async listar(tipo: TipoDeArticulo): Promise<Articulo[]> {
    if (tipo === 'servicio') return (await this.http.enviar<ServicioResponse[]>(RUTA.servicio)).map(deServicio);
    if (tipo === 'ropa') return (await this.http.enviar<RopaResponse[]>(RUTA.ropa)).map(deRopa);
    // La vista interna trae las existencias; la pública solo dice si hay o no.
    return (await this.http.enviar<ProductoInternoResponse[]>(`${RUTA.producto}/interno`)).map(deProducto);
  }

  async crear(tipo: TipoDeArticulo, datos: DatosDeArticulo): Promise<void> {
    await this.http.enviar(RUTA[tipo], { metodo: 'POST', cuerpo: aArticuloRequest(tipo, datos) });
  }

  async actualizar(tipo: TipoDeArticulo, id: number, datos: DatosDeArticulo): Promise<void> {
    await this.http.enviar(`${RUTA[tipo]}/${id}`, { metodo: 'PUT', cuerpo: aArticuloRequest(tipo, datos) });
  }

  eliminar(tipo: TipoDeArticulo, id: number): Promise<void> {
    return this.http.enviar<void>(`${RUTA[tipo]}/${id}`, { metodo: 'DELETE' });
  }
}
