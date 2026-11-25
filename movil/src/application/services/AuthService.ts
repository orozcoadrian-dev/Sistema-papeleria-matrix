import { sesionVencida } from '../../domain/model/Session';
import type { AuthGateway } from '../../domain/ports/out/AuthGateway';
import type { SessionStore } from '../../domain/ports/out/SessionStore';
import { Credenciales } from '../../domain/valueobjects/credenciales';
import type { EntrarCommand } from '../dto/commands/commands';
import type { UsuarioActual } from '../dto/responses/responses';
import { aUsuarioActual } from '../mapper/SessionMapper';
import type { AuthUseCases } from '../ports/in/use-cases';

export class AuthService implements AuthUseCases {
  constructor(
    private readonly gateway: AuthGateway,
    private readonly store: SessionStore,
    private readonly ahora: () => number = Date.now,
  ) {}

  async entrar(command: EntrarCommand): Promise<UsuarioActual> {
    const credenciales = Credenciales.crear(command.usuario, command.contrasenia);
    const sesion = await this.gateway.entrar(credenciales);
    this.store.guardar(sesion);
    this.store.anotarUso(this.ahora());
    return aUsuarioActual(sesion);
  }

  async salir(): Promise<void> {
    try {
      await this.gateway.salir();
    } catch {
      // Aunque el servidor no conteste, en este teléfono la sesión se cierra.
    } finally {
      this.store.borrar();
    }
  }

  usuarioActual(): UsuarioActual | null {
    const sesion = this.store.leer();
    if (!sesion) return null;
    if (sesionVencida(sesion, this.store.ultimoUso(), this.ahora())) {
      this.store.borrar();
      return null;
    }
    return aUsuarioActual(sesion);
  }

  registrarUso(): void {
    // Un toque no revive una sesión que ya venció.
    if (this.usuarioActual()) this.store.anotarUso(this.ahora());
  }
}
