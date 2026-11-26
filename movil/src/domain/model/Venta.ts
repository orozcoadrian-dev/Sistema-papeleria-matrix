import type { TipoDeArticulo } from '../constants/articulos';
import type { Articulo } from './Articulo';
import { Carrito } from './Carrito';

export interface Venta {
  id: number;
  codigo: string;
  /** Fecha en formato AAAA-MM-DD. */
  fecha: string;
  total: number;
  operador: string;
  /** Solo las ventas hechas en la caja se pueden cambiar. */
  esDeCaja: boolean;
  /** El id es null cuando el artículo ya no está en el catálogo. */
  articulos: { tipo: TipoDeArticulo; id: number | null; nombre: string; cantidad: number; precioUnitario: number | null }[];
}

export interface CambioDeVenta {
  articulos: { tipo: TipoDeArticulo; id: number; cantidad: number }[];
  totalEsperado: number;
}

export interface NuevaVenta extends CambioDeVenta {
  operadorId?: number;
}

export interface Operador {
  id: number;
  nombre: string;
}

/** Al cambiar una venta, lo que ella ya tenía vuelve a estar disponible para ella. */
export function conUnidadesDeLaVenta(catalogo: Articulo[], venta: Venta): Articulo[] {
  return catalogo.map((articulo) => {
    const suyas = venta.articulos
      .filter((linea) => linea.tipo === articulo.tipo && linea.id === articulo.id)
      .reduce((suma, linea) => suma + linea.cantidad, 0);
    if (articulo.existencias === null || !suyas) return articulo;
    const disponibles = articulo.existencias + suyas;
    return { ...articulo, existencias: articulo.tipo === 'ropa' ? Math.min(disponibles, 1) : disponibles };
  });
}

/** El carrito con lo que tenía la venta. «faltan» cuenta lo que ya no está en el catálogo. */
export function carritoDeLaVenta(venta: Venta, catalogo: Articulo[]): { carrito: Carrito; faltan: number } {
  let carrito = Carrito.vacio();
  let faltan = 0;
  for (const linea of venta.articulos) {
    const articulo = catalogo.find((uno) => uno.tipo === linea.tipo && uno.id === linea.id);
    if (articulo) carrito = carrito.cambiar(articulo, linea.cantidad);
    else faltan += 1;
  }
  return { carrito, faltan };
}
