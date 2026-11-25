/** Lee "20000", "20.000" o "$ 20,000". Devuelve NaN si no es un entero. */
export function leerEntero(texto: string): number {
  const limpio = texto.replace(/[$\s]/g, '');
  if (/^\d+$/.test(limpio)) return Number(limpio);
  if (/^\d{1,3}(\.\d{3})+$/.test(limpio) || /^\d{1,3}(,\d{3})+$/.test(limpio)) return Number(limpio.replace(/[.,]/g, ''));
  return Number.NaN;
}

export type Cambio =
  | { estado: 'ilegible' }
  | { estado: 'falta'; valor: number }
  | { estado: 'exacto'; valor: 0 }
  | { estado: 'devolver'; valor: number };

export function calcularCambio(total: number, pago: string): Cambio {
  const recibido = leerEntero(pago);
  if (!Number.isFinite(recibido)) return { estado: 'ilegible' };
  if (recibido < total) return { estado: 'falta', valor: total - recibido };
  if (recibido === total) return { estado: 'exacto', valor: 0 };
  return { estado: 'devolver', valor: recibido - total };
}

/** Billetes comunes para no tener que escribir: el total exacto y los que alcanzan a pagarlo. */
export function pagosRapidos(total: number): number[] {
  const billetes = [2000, 5000, 10000, 20000, 50000, 100000];
  return [total, ...billetes.filter((billete) => billete > total)].slice(0, 4);
}
