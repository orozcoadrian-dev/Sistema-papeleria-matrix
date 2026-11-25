import { inject, reactive, shallowRef, type InjectionKey } from 'vue';
import type { Aplicacion } from '../../bootstrap/container';
import type { TipoDeArticulo } from '../../domain/constants/articulos';
import { ErrorDeApi, ErrorDeDominio } from '../../domain/exceptions/errors';
import { Carrito } from '../../domain/model/Carrito';

export const APLICACION: InjectionKey<Aplicacion> = Symbol('aplicacion');

export function useAplicacion(): Aplicacion {
  const aplicacion = inject(APLICACION);
  if (!aplicacion) throw new Error('La aplicación no se ha provisto.');
  return aplicacion;
}

/** La venta en curso se conserva al cambiar de pantalla. */
export const carrito = shallowRef(Carrito.vacio());
export const operadorElegido = shallowRef<number>();

export function olvidarVenta(): void {
  carrito.value = Carrito.vacio();
  operadorElegido.value = undefined;
}

export const aviso = reactive({ texto: '', esError: false });
let temporizador: ReturnType<typeof setTimeout> | undefined;

export function avisar(texto: string, esError = false): void {
  aviso.texto = texto;
  aviso.esError = esError;
  clearTimeout(temporizador);
  temporizador = setTimeout(() => { aviso.texto = ''; }, esError ? 7000 : 3800);
}

export function mensajeDe(error: unknown): string {
  if (error instanceof ErrorDeApi && error.estado === 403) return 'Tu usuario no tiene permiso para hacer esto.';
  if (error instanceof ErrorDeApi && error.estado >= 500) return 'Algo salió mal en el sistema. Intenta otra vez en un momento.';
  if (error instanceof ErrorDeApi || error instanceof ErrorDeDominio) return error.message;
  return 'Algo salió mal. Intenta otra vez en un momento.';
}

const formatoPesos = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 });
export const pesos = (valor: number): string => formatoPesos.format(valor);

export const ICONO_DEL_TIPO: Record<TipoDeArticulo, string> = { servicio: '🖨️', producto: '✏️', ropa: '👗' };
