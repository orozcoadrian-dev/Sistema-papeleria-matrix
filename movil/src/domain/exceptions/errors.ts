export type ErroresPorCampo = Record<string, string>;

/** Una regla del negocio no se cumple. */
export class ErrorDeDominio extends Error {
  constructor(message: string, readonly campos: ErroresPorCampo = {}) {
    super(message);
    this.name = 'ErrorDeDominio';
  }
}

/** La API no respondió o rechazó la petición. El estado 0 significa sin conexión. */
export class ErrorDeApi extends Error {
  constructor(
    readonly estado: number,
    readonly titulo: string,
    message: string,
    readonly campos: ErroresPorCampo = {},
  ) {
    super(message);
    this.name = 'ErrorDeApi';
  }
}
