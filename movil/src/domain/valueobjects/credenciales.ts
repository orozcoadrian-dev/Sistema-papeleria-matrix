import { ErrorDeDominio, type ErroresPorCampo } from '../exceptions/errors';

export class Credenciales {
  private constructor(readonly usuario: string, readonly contrasenia: string) {}

  static crear(usuario: string, contrasenia: string): Credenciales {
    const errores: ErroresPorCampo = {};
    const nombre = usuario.trim();
    if (!nombre) errores.usuario = 'Escribe tu usuario.';
    else if (nombre.length > 20) errores.usuario = 'El nombre de usuario es demasiado largo.';
    if (!contrasenia.trim()) errores.contrasenia = 'Escribe tu contraseña.';
    if (Object.keys(errores).length) throw new ErrorDeDominio('Revisa los datos para entrar.', errores);
    return new Credenciales(nombre, contrasenia);
  }
}
