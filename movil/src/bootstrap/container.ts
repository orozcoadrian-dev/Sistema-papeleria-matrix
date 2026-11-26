import { AuthHttpAdapter } from '../adapters/http/AuthHttpAdapter';
import { CatalogHttpAdapter } from '../adapters/http/CatalogHttpAdapter';
import { HttpClient } from '../adapters/http/HttpClient';
import { SaleHttpAdapter } from '../adapters/http/SaleHttpAdapter';
import { BrowserSessionStore } from '../adapters/storage/BrowserSessionStore';
import type { AuthUseCases, CatalogUseCases, SaleUseCases } from '../application/ports/in/use-cases';
import { AuthService } from '../application/services/AuthService';
import { CatalogService } from '../application/services/CatalogService';
import { SaleService } from '../application/services/SaleService';

export interface Aplicacion {
  auth: AuthUseCases;
  catalogo: CatalogUseCases;
  ventas: SaleUseCases;
  cuandoVenzaLaSesion(aviso: () => void): void;
}

/** Único lugar donde se eligen las implementaciones concretas. */
export function crearAplicacion(base: string, storage: Storage, pedir?: typeof fetch): Aplicacion {
  const store = new BrowserSessionStore(storage);
  // Sin «pedir» se usa el fetch del navegador; la aplicación instalada entrega el de Android.
  const http = new HttpClient(base, store, pedir);
  return {
    auth: new AuthService(new AuthHttpAdapter(http), store),
    catalogo: new CatalogService(new CatalogHttpAdapter(http)),
    ventas: new SaleService(new SaleHttpAdapter(http), store),
    cuandoVenzaLaSesion: (aviso) => http.cuandoVenzaLaSesion(aviso),
  };
}
