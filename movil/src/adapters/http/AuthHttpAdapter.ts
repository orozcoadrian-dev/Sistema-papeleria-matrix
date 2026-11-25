import type { Sesion } from '../../domain/model/Session';
import type { AuthGateway } from '../../domain/ports/out/AuthGateway';
import type { Credenciales } from '../../domain/valueobjects/credenciales';
import type { InicioSesionRequest } from '../dto/requests/requests';
import type { SesionResponse } from '../dto/responses/responses';
import { aSesion } from '../mapper/ApiMapper';
import type { HttpClient } from './HttpClient';

export class AuthHttpAdapter implements AuthGateway {
  constructor(private readonly http: HttpClient) {}

  async entrar(credenciales: Credenciales): Promise<Sesion> {
    const cuerpo: InicioSesionRequest = { nombreUsuario: credenciales.usuario, contrasenia: credenciales.contrasenia };
    return aSesion(await this.http.enviar<SesionResponse>('/sesion', { metodo: 'POST', cuerpo }));
  }

  salir(): Promise<void> {
    return this.http.enviar<void>('/sesion', { metodo: 'DELETE' });
  }
}
