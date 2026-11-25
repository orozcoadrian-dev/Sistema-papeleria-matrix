import { ErrorDeDominio } from '../../domain/exceptions/errors';
import type { Operador, Venta } from '../../domain/model/Venta';
import type { SaleGateway } from '../../domain/ports/out/SaleGateway';
import type { SessionStore } from '../../domain/ports/out/SessionStore';
import type { CobrarCommand } from '../dto/commands/commands';
import type { Periodo, ResumenDeVentas } from '../dto/responses/responses';
import type { SaleUseCases } from '../ports/in/use-cases';

const DIAS_ATRAS: Record<Periodo, number> = { hoy: 0, semana: 6, mes: 29 };

/** Fecha local en formato AAAA-MM-DD. */
export function fechaIso(fecha: Date, restarDias = 0): string {
  const copia = new Date(fecha);
  copia.setDate(copia.getDate() - restarDias);
  const mes = String(copia.getMonth() + 1).padStart(2, '0');
  const dia = String(copia.getDate()).padStart(2, '0');
  return `${copia.getFullYear()}-${mes}-${dia}`;
}

export class SaleService implements SaleUseCases {
  constructor(
    private readonly gateway: SaleGateway,
    private readonly store: SessionStore,
    private readonly hoy: () => Date = () => new Date(),
  ) {}

  async cobrar({ carrito, operadorId }: CobrarCommand): Promise<Venta> {
    if (carrito.unidades === 0) throw new ErrorDeDominio('Agrega al menos un artículo a la venta.');
    const esAdministrador = this.store.leer()?.rol === 'ADMINISTRADOR';
    if (esAdministrador && !operadorId) throw new ErrorDeDominio('Elige a nombre de qué operador queda la venta.');
    return this.gateway.registrar({
      articulos: carrito.lineas.map(({ articulo, cantidad }) => ({ tipo: articulo.tipo, id: articulo.id, cantidad })),
      // Lo que se le dijo al cliente: si un precio cambió mientras tanto, el servidor no registra otro total.
      totalEsperado: carrito.total,
      ...(esAdministrador ? { operadorId } : {}),
    });
  }

  async resumen(periodo: Periodo): Promise<ResumenDeVentas> {
    const hoy = this.hoy();
    const ventas = await this.gateway.listar(fechaIso(hoy, DIAS_ATRAS[periodo]), fechaIso(hoy));
    const total = ventas.reduce((suma, venta) => suma + venta.total, 0);
    return { ventas, total, cantidad: ventas.length, promedio: ventas.length ? Math.round(total / ventas.length) : 0 };
  }

  anular(id: number): Promise<void> {
    return this.gateway.anular(id);
  }

  operadores(): Promise<Operador[]> {
    return this.gateway.operadores();
  }
}
