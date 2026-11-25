import { describe, expect, it } from 'vitest';
import { ErrorDeDominio } from '../../src/domain/exceptions/errors';
import { coincide, validarArticulo, type Articulo } from '../../src/domain/model/Articulo';
import { Carrito } from '../../src/domain/model/Carrito';
import { Credenciales } from '../../src/domain/valueobjects/credenciales';
import { calcularCambio, leerEntero, pagosRapidos } from '../../src/domain/valueobjects/dinero';

const articulo = (cambios: Partial<Articulo> = {}): Articulo => ({
  tipo: 'producto', id: 1, nombre: 'Lápiz mirado', precio: 1200, detalle: '', existencias: 2, datos: {}, ...cambios,
});

describe('dinero', () => {
  it('lee números con puntos, comas y signo de pesos', () => {
    expect(['20000', '20.000', '$ 20,000'].map(leerEntero)).toEqual([20000, 20000, 20000]);
    expect(leerEntero('20,5')).toBeNaN();
  });

  it('calcula el cambio y avisa si falta dinero', () => {
    expect(calcularCambio(7000, '10000')).toEqual({ estado: 'devolver', valor: 3000 });
    expect(calcularCambio(7000, '5000')).toEqual({ estado: 'falta', valor: 2000 });
    expect(calcularCambio(7000, '7000').estado).toBe('exacto');
    expect(pagosRapidos(7000)).toEqual([7000, 10000, 20000, 50000]);
  });
});

describe('credenciales', () => {
  it('exige usuario y contraseña', () => {
    expect(() => Credenciales.crear('  ', '')).toThrow(ErrorDeDominio);
    expect(Credenciales.crear(' marta ', 'secreta').usuario).toBe('marta');
  });
});

describe('carrito', () => {
  it('suma el total sin modificar el carrito anterior', () => {
    const vacio = Carrito.vacio();
    const lleno = vacio.cambiar(articulo(), 2);
    expect(lleno.total).toBe(2400);
    expect(lleno.unidades).toBe(2);
    expect(vacio.unidades).toBe(0);
  });

  it('no deja vender más de lo que hay', () => {
    expect(() => Carrito.vacio().cambiar(articulo(), 3)).toThrow('Solo quedan 2 unidades');
  });

  it('trata cada prenda como única', () => {
    const prenda = articulo({ tipo: 'ropa', nombre: 'Blusa', existencias: 1 });
    expect(() => Carrito.vacio().cambiar(prenda, 1).cambiar(prenda, 1)).toThrow('Cada prenda es única');
  });

  it('limita a 150 lo que no tiene existencias', () => {
    const servicio = articulo({ tipo: 'servicio', existencias: null });
    expect(() => Carrito.vacio().cambiar(servicio, 151)).toThrow('150');
  });

  it('quita la línea al llegar a cero y toma los precios nuevos', () => {
    const lleno = Carrito.vacio().cambiar(articulo(), 1);
    expect(lleno.cambiar(articulo(), -1).lineas).toHaveLength(0);
    expect(lleno.actualizar([articulo({ precio: 1500 })]).total).toBe(1500);
    expect(lleno.actualizar([]).unidades).toBe(0);
  });
});

describe('artículos', () => {
  it('busca por el comienzo de las palabras y sin tildes', () => {
    expect(coincide(articulo(), 'lapiz mir')).toBe(true);
    expect(coincide(articulo(), 'rado')).toBe(false);
  });

  it('valida todos los campos a la vez', () => {
    try {
      validarArticulo('servicio', { codigo: '', nombre: 'x'.repeat(31), categoria: 'Impresión', precio: '0' });
      expect.unreachable();
    } catch (error) {
      expect(Object.keys((error as ErrorDeDominio).campos).sort()).toEqual(['codigo', 'nombre', 'precio']);
    }
  });

  it('convierte los precios escritos con puntos en números', () => {
    const datos = validarArticulo('servicio', { codigo: 'S1', nombre: 'Copia', categoria: 'Impresión', precio: '1.500' });
    expect(datos.precio).toBe(1500);
  });
});
