import type { TipoDeArticulo } from '../../../domain/constants/articulos';
import type { Articulo } from '../../../domain/model/Articulo';
import type { Operador, Venta } from '../../../domain/model/Venta';
import type { AnularVentaCommand, CambiarVentaCommand, CobrarCommand, EntrarCommand, GuardarArticuloCommand } from '../../dto/commands/commands';
import type { Periodo, ResumenDeVentas, UsuarioActual } from '../../dto/responses/responses';

export interface AuthUseCases {
  entrar(command: EntrarCommand): Promise<UsuarioActual>;
  salir(): Promise<void>;
  /** Devuelve null si no hay sesión o si venció por inactividad. */
  usuarioActual(): UsuarioActual | null;
  registrarUso(): void;
}

export interface CatalogUseCases {
  listar(): Promise<Articulo[]>;
  guardar(command: GuardarArticuloCommand): Promise<void>;
  eliminar(tipo: TipoDeArticulo, id: number): Promise<void>;
}

export interface SaleUseCases {
  cobrar(command: CobrarCommand): Promise<Venta>;
  resumen(periodo: Periodo): Promise<ResumenDeVentas>;
  modificar(command: CambiarVentaCommand): Promise<Venta>;
  anular(command: AnularVentaCommand): Promise<void>;
  operadores(): Promise<Operador[]>;
}
