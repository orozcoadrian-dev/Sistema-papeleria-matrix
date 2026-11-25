export const TIPOS_DE_ARTICULO = ['servicio', 'producto', 'ropa'] as const;
export type TipoDeArticulo = (typeof TIPOS_DE_ARTICULO)[number];

export const TITULO_DEL_TIPO: Record<TipoDeArticulo, string> = {
  servicio: 'Servicios',
  producto: 'Papelería',
  ropa: 'Ropa',
};

/** Tope por artículo que acepta el servidor en una venta. */
export const CANTIDAD_MAXIMA = 150;
export const POCAS_UNIDADES = 5;

export interface Campo {
  nombre: string;
  etiqueta: string;
  clase: 'texto' | 'dinero' | 'entero';
  obligatorio: boolean;
  maximo?: number;
}

const codigo: Campo = { nombre: 'codigo', etiqueta: 'Código', clase: 'texto', obligatorio: true, maximo: 20 };
const descripcion: Campo = { nombre: 'descripcion', etiqueta: 'Descripción', clase: 'texto', obligatorio: false, maximo: 150 };

/** Los mismos límites que valida la API para cada tipo de artículo. */
export const CAMPOS: Record<TipoDeArticulo, readonly Campo[]> = {
  servicio: [
    codigo,
    { nombre: 'nombre', etiqueta: 'Nombre', clase: 'texto', obligatorio: true, maximo: 30 },
    { nombre: 'categoria', etiqueta: 'Categoría', clase: 'texto', obligatorio: true, maximo: 30 },
    { nombre: 'precio', etiqueta: 'Precio', clase: 'dinero', obligatorio: true },
    descripcion,
  ],
  producto: [
    codigo,
    { nombre: 'nombre', etiqueta: 'Nombre', clase: 'texto', obligatorio: true, maximo: 30 },
    { nombre: 'cantidad', etiqueta: 'Unidades que hay', clase: 'entero', obligatorio: true },
    { nombre: 'precio', etiqueta: 'Precio de venta', clase: 'dinero', obligatorio: true },
    { nombre: 'precioCompra', etiqueta: 'Precio de compra', clase: 'dinero', obligatorio: true },
    { nombre: 'lugarCompra', etiqueta: 'Dónde se compra', clase: 'texto', obligatorio: false, maximo: 40 },
    descripcion,
  ],
  ropa: [
    codigo,
    { nombre: 'nombre', etiqueta: 'Nombre', clase: 'texto', obligatorio: false, maximo: 30 },
    { nombre: 'tipoRopa', etiqueta: 'Tipo de prenda', clase: 'texto', obligatorio: true, maximo: 20 },
    { nombre: 'talla', etiqueta: 'Talla', clase: 'texto', obligatorio: true, maximo: 5 },
    { nombre: 'material', etiqueta: 'Material', clase: 'texto', obligatorio: true, maximo: 30 },
    { nombre: 'color', etiqueta: 'Color', clase: 'texto', obligatorio: false, maximo: 40 },
    { nombre: 'precio', etiqueta: 'Precio', clase: 'dinero', obligatorio: true },
    descripcion,
  ],
};
