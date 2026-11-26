import { describe, expect, it, vi } from 'vitest';
import { HttpClient } from '../../src/adapters/http/HttpClient';
import { crearPedirNativo, type Solicitar } from '../../src/adapters/http/pedirNativo';
import { BrowserSessionStore } from '../../src/adapters/storage/BrowserSessionStore';

const almacenVacio = () => new BrowserSessionStore({
  getItem: () => null, setItem: () => {}, removeItem: () => {}, clear: () => {}, key: () => null, length: 0,
});

describe('peticiones desde la aplicación instalada', () => {
  it('entrega el cuerpo como objeto y lee la respuesta', async () => {
    const solicitar = vi.fn<Solicitar>(async () => ({ status: 201, data: { id: 7 } }));
    const http = new HttpClient('https://api.ejemplo/api', almacenVacio(), crearPedirNativo(solicitar));
    await expect(http.enviar('/ventas', { metodo: 'POST', cuerpo: { total: 100 } })).resolves.toEqual({ id: 7 });
    expect(solicitar).toHaveBeenCalledWith(expect.objectContaining({
      url: 'https://api.ejemplo/api/ventas', method: 'POST', data: { total: 100 }, readTimeout: 65000,
      headers: expect.objectContaining({ 'Content-Type': 'application/json' }),
    }));
  });

  it('entiende un 204 y un error que llega como texto', async () => {
    const vacio = new HttpClient('/api', almacenVacio(), crearPedirNativo(async () => ({ status: 204, data: '' })));
    await expect(vacio.enviar('/ropa/1', { metodo: 'DELETE' })).resolves.toBeUndefined();

    const conError = new HttpClient('/api', almacenVacio(), crearPedirNativo(async () => ({
      status: 409, data: '{"title":"Código repetido","detail":"Ya existe"}',
    })));
    await expect(conError.enviar('/ropa')).rejects.toMatchObject({ estado: 409, message: 'Ya existe' });
  });

  it('trata un fallo de red como falta de conexión', async () => {
    const http = new HttpClient('/api', almacenVacio(), crearPedirNativo(async () => { throw new Error('sin red'); }));
    await expect(http.enviar('/ropa')).rejects.toMatchObject({ estado: 0 });
  });
});
