import { CAMPOS, type TipoDeArticulo } from '../constants/articulos';
import { ErrorDeDominio, type ErroresPorCampo } from '../exceptions/errors';
import { leerEntero } from '../valueobjects/dinero';

export interface Articulo {
  tipo: TipoDeArticulo;
  id: number;
  nombre: string;
  precio: number;
  /** Texto corto que acompaña al nombre: categoría, o tipo, talla y color. */
  detalle: string;
  /** null: no se cuenta (servicios). La ropa es única: 1 disponible, 0 vendida. */
  existencias: number | null;
  /** Valores tal como se editan en el formulario. */
  datos: Record<string, string>;
}

/** Lo que se escribe en el formulario, ya validado. */
export type DatosDeArticulo = Record<string, string | number>;

export function validarArticulo(tipo: TipoDeArticulo, formulario: Record<string, string>): DatosDeArticulo {
  const errores: ErroresPorCampo = {};
  const datos: DatosDeArticulo = {};
  for (const campo of CAMPOS[tipo]) {
    const texto = (formulario[campo.nombre] ?? '').trim();
    if (!texto) {
      if (campo.obligatorio) errores[campo.nombre] = `Falta: ${campo.etiqueta.toLowerCase()}.`;
      else datos[campo.nombre] = '';
      continue;
    }
    if (campo.clase === 'texto') {
      if (campo.maximo && texto.length > campo.maximo) errores[campo.nombre] = `Máximo ${campo.maximo} letras.`;
      datos[campo.nombre] = texto;
      continue;
    }
    const numero = leerEntero(texto);
    if (!Number.isFinite(numero)) errores[campo.nombre] = 'Escribe solo el número.';
    else if (campo.clase === 'dinero' && numero <= 0) errores[campo.nombre] = 'Debe ser mayor que cero.';
    datos[campo.nombre] = numero;
  }
  if (Object.keys(errores).length) throw new ErrorDeDominio('Revisa los campos marcados.', errores);
  return datos;
}

function normalizar(texto: string): string {
  return texto.normalize('NFD').replace(/\p{Diacritic}/gu, '').toLowerCase().trim();
}

/** Cada palabra buscada debe ser el comienzo de alguna palabra del nombre o del detalle. */
export function coincide(articulo: Articulo, busqueda: string): boolean {
  const propias = normalizar(`${articulo.nombre} ${articulo.detalle}`).split(/[\s,.;:()/·-]+/);
  return normalizar(busqueda)
    .split(/\s+/)
    .filter(Boolean)
    .every((palabra) => propias.some((propia) => propia.startsWith(palabra)));
}
