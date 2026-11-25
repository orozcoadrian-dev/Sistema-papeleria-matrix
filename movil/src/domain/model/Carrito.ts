import { CANTIDAD_MAXIMA } from '../constants/articulos';
import { ErrorDeDominio } from '../exceptions/errors';
import type { Articulo } from './Articulo';

export interface Linea {
  articulo: Articulo;
  cantidad: number;
}

export const claveDe = (articulo: Pick<Articulo, 'tipo' | 'id'>): string => `${articulo.tipo}:${articulo.id}`;

/** Lo que se lleva el cliente. No cambia: cada operación devuelve un carrito nuevo. */
export class Carrito {
  private constructor(private readonly contenido: ReadonlyMap<string, Linea>) {}

  static vacio(): Carrito {
    return new Carrito(new Map());
  }

  get lineas(): Linea[] {
    return [...this.contenido.values()];
  }

  get total(): number {
    return this.lineas.reduce((suma, linea) => suma + linea.articulo.precio * linea.cantidad, 0);
  }

  get unidades(): number {
    return this.lineas.reduce((suma, linea) => suma + linea.cantidad, 0);
  }

  cantidadDe(articulo: Articulo): number {
    return this.contenido.get(claveDe(articulo))?.cantidad ?? 0;
  }

  cambiar(articulo: Articulo, cuantos: number): Carrito {
    const nueva = this.cantidadDe(articulo) + cuantos;
    const copia = new Map(this.contenido);
    if (nueva <= 0) {
      copia.delete(claveDe(articulo));
      return new Carrito(copia);
    }
    if (cuantos > 0) this.revisar(articulo, nueva);
    copia.set(claveDe(articulo), { articulo, cantidad: nueva });
    return new Carrito(copia);
  }

  /** Cruza la venta con el catálogo recién cargado: toma precios nuevos y quita lo que ya no existe. */
  actualizar(catalogo: Articulo[]): Carrito {
    const copia = new Map<string, Linea>();
    for (const [clave, linea] of this.contenido) {
      const nuevo = catalogo.find((articulo) => claveDe(articulo) === clave);
      if (nuevo) copia.set(clave, { articulo: nuevo, cantidad: linea.cantidad });
    }
    return new Carrito(copia);
  }

  private revisar(articulo: Articulo, cantidad: number): void {
    const hay = articulo.existencias;
    if (hay !== null && cantidad > hay) {
      if (articulo.tipo === 'ropa') {
        throw new ErrorDeDominio(hay === 0 ? `«${articulo.nombre}» ya se vendió.` : `Cada prenda es única: «${articulo.nombre}» ya está en la venta.`);
      }
      throw new ErrorDeDominio(
        hay === 0 ? `Ya no quedan unidades de «${articulo.nombre}».`
          : hay === 1 ? `Solo queda 1 unidad de «${articulo.nombre}».`
            : `Solo quedan ${hay} unidades de «${articulo.nombre}».`,
      );
    }
    if (cantidad > CANTIDAD_MAXIMA) {
      throw new ErrorDeDominio(`No se pueden vender más de ${CANTIDAD_MAXIMA} de lo mismo en una venta.`);
    }
  }
}
