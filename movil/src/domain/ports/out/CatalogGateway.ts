import type { TipoDeArticulo } from '../../constants/articulos';
import type { Articulo, DatosDeArticulo } from '../../model/Articulo';

export interface CatalogGateway {
  listar(tipo: TipoDeArticulo): Promise<Articulo[]>;
  crear(tipo: TipoDeArticulo, datos: DatosDeArticulo): Promise<void>;
  actualizar(tipo: TipoDeArticulo, id: number, datos: DatosDeArticulo): Promise<void>;
  eliminar(tipo: TipoDeArticulo, id: number): Promise<void>;
}
