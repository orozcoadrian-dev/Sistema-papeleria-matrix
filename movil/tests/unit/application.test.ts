import { describe, expect, it, vi } from 'vitest';
import { HttpClient } from '../../src/adapters/http/HttpClient';
import { SaleHttpAdapter } from '../../src/adapters/http/SaleHttpAdapter';
import { aArticuloRequest, aSesion } from '../../src/adapters/mapper/ApiMapper';
import { BrowserSessionStore } from '../../src/adapters/storage/BrowserSessionStore';
import { AuthService } from '../../src/application/services/AuthService';
import { CatalogService } from '../../src/application/services/CatalogService';
import { SaleService } from '../../src/application/services/SaleService';
import { Carrito } from '../../src/domain/model/Carrito';
import type { Sesion } from '../../src/domain/model/Session';
import type { CatalogGateway } from '../../src/domain/ports/out/CatalogGateway';
import type { SaleGateway } from '../../src/domain/ports/out/SaleGateway';

/** Almacenamiento en memoria con la misma forma que sessionStorage. */
function memoria(): Storage {
  const datos = new Map<string, string>();
  return {
    getItem: (llave) => datos.get(llave) ?? null,
    setItem: (llave, valor) => void datos.set(llave, valor),
    removeItem: (llave) => void datos.delete(llave),
    clear: () => datos.clear(),
    key: () => null,
    get length() { return datos.size; },
  };
}

const sesion: Sesion = {
  codigo: 'secreto', idUsuario: 1, nombreUsuario: 'marta', nombre: 'Marta Gómez',
  rol: 'ADMINISTRADOR', rolNombre: 'Administrador', minutosInactividad: 15,
};

const respuesta = (estado: number, cuerpo?: unknown) =>
  new Response(estado === 204 ? null : JSON.stringify(cuerpo), { status: estado });

describe('sesión', () => {
  it('al entrar no entrega el código secreto a la interfaz', async () => {
    const store = new BrowserSessionStore(memoria());
    const auth = new AuthService({ entrar: async () => sesion, salir: async () => {} }, store);
    const usuario = await auth.entrar({ usuario: 'marta', contrasenia: 'secreta' });
    expect(usuario).not.toHaveProperty('codigo');
    expect(usuario.esAdministrador).toBe(true);
    expect(store.leer()?.codigo).toBe('secreto');
  });

  it('se cierra después de 15 minutos sin uso y un toque no la revive', async () => {
    let ahora = 1_000_000;
    const store = new BrowserSessionStore(memoria());
    const auth = new AuthService({ entrar: async () => sesion, salir: async () => {} }, store, () => ahora);
    await auth.entrar({ usuario: 'marta', contrasenia: 'secreta' });
    ahora += 14 * 60000;
    expect(auth.usuarioActual()).not.toBeNull();
    ahora += 2 * 60000;
    auth.registrarUso();
    expect(auth.usuarioActual()).toBeNull();
    expect(store.leer()).toBeNull();
  });

  it('borra la sesión local aunque el servidor no conteste', async () => {
    const store = new BrowserSessionStore(memoria());
    store.guardar(sesion);
    const auth = new AuthService({ entrar: async () => sesion, salir: async () => { throw new Error('sin red'); } }, store);
    await auth.salir();
    expect(store.leer()).toBeNull();
  });

  it('rechaza un rol que no conoce', () => {
    expect(() => aSesion({ ...sesion, rol: 'OTRO' })).toThrow();
  });
});

describe('HttpClient', () => {
  it('envía el código de sesión y entiende un 204', async () => {
    const store = new BrowserSessionStore(memoria());
    store.guardar(sesion);
    const pedir = vi.fn(async () => respuesta(204));
    await new HttpClient('/api', store, pedir).enviar('/ventas/1', { metodo: 'DELETE' });
    expect(pedir).toHaveBeenCalledWith('/api/ventas/1', expect.objectContaining({
      method: 'DELETE', headers: expect.objectContaining({ Authorization: 'Bearer secreto' }),
    }));
  });

  it('ante un 401 cierra la sesión y avisa', async () => {
    const store = new BrowserSessionStore(memoria());
    store.guardar(sesion);
    const http = new HttpClient('/api', store, async () => respuesta(401, { title: 'No autorizado' }));
    const aviso = vi.fn();
    http.cuandoVenzaLaSesion(aviso);
    await expect(http.enviar('/ventas')).rejects.toMatchObject({ estado: 401 });
    expect(aviso).toHaveBeenCalledOnce();
    expect(store.leer()).toBeNull();
  });

  it('un 403 conserva la sesión y los errores por campo llegan completos', async () => {
    const store = new BrowserSessionStore(memoria());
    store.guardar(sesion);
    const http = new HttpClient('/api', store, async () => respuesta(403, { detail: 'Sin permiso', errores: { codigo: 'Repetido' } }));
    await expect(http.enviar('/ropa')).rejects.toMatchObject({ estado: 403, campos: { codigo: 'Repetido' } });
    expect(store.leer()).not.toBeNull();
  });

  it('explica la falta de conexión', async () => {
    const http = new HttpClient('/api', new BrowserSessionStore(memoria()), async () => { throw new TypeError('red'); });
    await expect(http.enviar('/ropa')).rejects.toMatchObject({ estado: 0 });
  });
});

describe('ventas y catálogo', () => {
  const lapiz = { tipo: 'producto' as const, id: 3, nombre: 'Lápiz', precio: 1200, detalle: '', existencias: 9, datos: {} };

  function ventasFalsas() {
    return {
      registrar: vi.fn(async () => ({}) as never), listar: vi.fn(async () => []), modificar: vi.fn(async () => ({}) as never),
      anular: vi.fn(), operadores: vi.fn(),
    } satisfies SaleGateway;
  }

  it('la administradora debe elegir operador y se envía el total esperado', async () => {
    const store = new BrowserSessionStore(memoria());
    store.guardar(sesion);
    const gateway = ventasFalsas();
    const ventas = new SaleService(gateway, store);
    const carrito = Carrito.vacio().cambiar(lapiz, 2);
    await expect(ventas.cobrar({ carrito })).rejects.toThrow('operador');
    await ventas.cobrar({ carrito, operadorId: 2 });
    expect(gateway.registrar).toHaveBeenCalledWith({ articulos: [{ tipo: 'producto', id: 3, cantidad: 2 }], totalEsperado: 2400, operadorId: 2 });
  });

  it('el operador no manda operadorId', async () => {
    const store = new BrowserSessionStore(memoria());
    store.guardar({ ...sesion, rol: 'OPERADOR_DE_CIBERCAFE' });
    const gateway = ventasFalsas();
    await new SaleService(gateway, store).cobrar({ carrito: Carrito.vacio().cambiar(lapiz, 1), operadorId: 9 });
    expect(gateway.registrar).toHaveBeenCalledWith({ articulos: [{ tipo: 'producto', id: 3, cantidad: 1 }], totalEsperado: 1200 });
  });

  it('el operador necesita su clave para cambiar o anular; la administradora no', async () => {
    const store = new BrowserSessionStore(memoria());
    store.guardar({ ...sesion, rol: 'OPERADOR_DE_CIBERCAFE' });
    const gateway = ventasFalsas();
    const ventas = new SaleService(gateway, store);
    const carrito = Carrito.vacio().cambiar(lapiz, 1);
    await expect(ventas.anular({ id: 4 })).rejects.toThrow('clave de permiso');
    await expect(ventas.modificar({ id: 4, carrito, clavePermiso: '   ' })).rejects.toThrow('clave de permiso');
    await ventas.modificar({ id: 4, carrito, clavePermiso: 'mi-clave' });
    expect(gateway.modificar).toHaveBeenCalledWith(4, { articulos: [{ tipo: 'producto', id: 3, cantidad: 1 }], totalEsperado: 1200 }, 'mi-clave');

    store.guardar(sesion);
    await ventas.anular({ id: 4, clavePermiso: 'sobra' });
    expect(gateway.anular).toHaveBeenCalledWith(4, undefined);
  });

  it('envía la clave codificada en su cabecera', async () => {
    const pedir = vi.fn(async () => respuesta(204));
    await new SaleHttpAdapter(new HttpClient('/api', new BrowserSessionStore(memoria()), pedir)).anular(4, 'Cañón 2026');
    expect(pedir).toHaveBeenCalledWith('/api/ventas/4', expect.objectContaining({
      method: 'DELETE', headers: expect.objectContaining({ 'X-Clave-Permiso': 'Ca%C3%B1%C3%B3n%202026' }),
    }));
  });

  it('pide las fechas del periodo', async () => {
    const gateway = ventasFalsas();
    await new SaleService(gateway, new BrowserSessionStore(memoria()), () => new Date(2026, 10, 3)).resumen('semana');
    expect(gateway.listar).toHaveBeenCalledWith('2026-10-28', '2026-11-03');
  });

  it('al editar papelería manda las unidades que había', async () => {
    const gateway = { listar: vi.fn(), crear: vi.fn(), actualizar: vi.fn(), eliminar: vi.fn() } satisfies CatalogGateway;
    const formulario = { codigo: 'P1', nombre: 'Lápiz', cantidad: '8', precio: '1200', precioCompra: '800' };
    await new CatalogService(gateway).guardar({ tipo: 'producto', id: 3, formulario, cantidadAnterior: 9 });
    expect(gateway.actualizar).toHaveBeenCalledWith('producto', 3, expect.objectContaining({ cantidad: 8, cantidadAnterior: 9 }));
    expect(aArticuloRequest('producto', { precio: 1200 })).toEqual({ precioVenta: 1200 });
  });
});
