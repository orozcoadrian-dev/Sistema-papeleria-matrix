import { TIPOS_DE_ARTICULO, type TipoDeArticulo } from '../../domain/constants/articulos';
import { ErrorDeDominio } from '../../domain/exceptions/errors';
import { validarArticulo, type Articulo } from '../../domain/model/Articulo';
import type { CatalogGateway } from '../../domain/ports/out/CatalogGateway';
import type { GuardarArticuloCommand } from '../dto/commands/commands';
import type { CatalogUseCases } from '../ports/in/use-cases';

export class CatalogService implements CatalogUseCases {
  constructor(private readonly gateway: CatalogGateway) {}

  async listar(): Promise<Articulo[]> {
    const listas = await Promise.all(TIPOS_DE_ARTICULO.map((tipo) => this.gateway.listar(tipo)));
    return listas.flat();
  }

  async guardar({ tipo, id, formulario, cantidadAnterior }: GuardarArticuloCommand): Promise<void> {
    const datos = validarArticulo(tipo, formulario);
    if (id === undefined) return this.gateway.crear(tipo, datos);
    if (tipo === 'producto' && cantidadAnterior !== undefined) datos.cantidadAnterior = cantidadAnterior;
    return this.gateway.actualizar(tipo, this.exigirId(id), datos);
  }

  async eliminar(tipo: TipoDeArticulo, id: number): Promise<void> {
    return this.gateway.eliminar(tipo, this.exigirId(id));
  }

  private exigirId(id: number): number {
    if (!Number.isInteger(id) || id <= 0) throw new ErrorDeDominio('El artículo no es válido.');
    return id;
  }
}
