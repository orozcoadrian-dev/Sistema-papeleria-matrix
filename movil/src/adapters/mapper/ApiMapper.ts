import type { TipoDeArticulo } from '../../domain/constants/articulos';
import { ROLES, type Rol } from '../../domain/constants/roles';
import { ErrorDeApi } from '../../domain/exceptions/errors';
import type { Articulo, DatosDeArticulo } from '../../domain/model/Articulo';
import type { Sesion } from '../../domain/model/Session';
import type { Operador, Venta } from '../../domain/model/Venta';
import type { ArticuloRequest } from '../dto/requests/requests';
import type { OperadorResponse, ProductoInternoResponse, RopaResponse, ServicioResponse, SesionResponse, VentaResponse } from '../dto/responses/responses';

const texto = (valor: string | number | null | undefined): string => (valor === null || valor === undefined ? '' : String(valor));

export function aSesion(respuesta: SesionResponse): Sesion {
  if (!respuesta?.codigo || !ROLES.includes(respuesta.rol as Rol)) {
    throw new ErrorDeApi(502, 'Respuesta inesperada', 'El sistema respondió algo que no entendemos.');
  }
  return { ...respuesta, rol: respuesta.rol as Rol };
}

export const deServicio = (s: ServicioResponse): Articulo => ({
  tipo: 'servicio', id: s.id, nombre: s.nombre, precio: s.precio, detalle: s.categoria, existencias: null,
  datos: { codigo: s.codigo, nombre: s.nombre, categoria: s.categoria, precio: texto(s.precio), descripcion: texto(s.descripcion) },
});

export const deProducto = (p: ProductoInternoResponse): Articulo => ({
  tipo: 'producto', id: p.id, nombre: p.nombre, precio: p.precio, detalle: '', existencias: p.cantidad,
  datos: {
    codigo: p.codigo, nombre: p.nombre, cantidad: texto(p.cantidad), precio: texto(p.precio),
    precioCompra: texto(p.precioCompra), lugarCompra: texto(p.lugarCompra), descripcion: texto(p.descripcion),
  },
});

export const deRopa = (r: RopaResponse): Articulo => ({
  tipo: 'ropa', id: r.id, nombre: r.nombre || r.tipo, precio: r.precio,
  detalle: [r.tipo, r.talla && `Talla ${r.talla}`, r.color].filter(Boolean).join(' · '),
  existencias: r.disponible ? 1 : 0,
  datos: {
    codigo: r.codigo, nombre: texto(r.nombre), tipoRopa: r.tipo, talla: r.talla, material: r.material,
    color: texto(r.color), precio: texto(r.precio), descripcion: texto(r.descripcion),
  },
});

/** La API llama precioVenta al precio de la papelería cuando recibe los datos. */
export function aArticuloRequest(tipo: TipoDeArticulo, datos: DatosDeArticulo): ArticuloRequest {
  if (tipo !== 'producto') return { ...datos };
  const { precio, ...resto } = datos;
  return { ...resto, precioVenta: precio ?? '' };
}

export const aVenta = (v: VentaResponse): Venta => ({
  id: v.id, codigo: v.codigo, fecha: v.fecha, total: v.total, operador: v.operador, articulos: v.articulos,
});

export const aOperador = (o: OperadorResponse): Operador => ({
  id: o.idUsuario, nombre: `${o.primerNombre} ${o.primerApellido}`.trim(),
});
