import type { Sesion } from '../../model/Session';
import type { Credenciales } from '../../valueobjects/credenciales';

export interface AuthGateway {
  entrar(credenciales: Credenciales): Promise<Sesion>;
  salir(): Promise<void>;
}
