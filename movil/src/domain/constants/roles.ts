export const ROLES = ['ADMINISTRADOR', 'OPERADOR_DE_CIBERCAFE'] as const;
export type Rol = (typeof ROLES)[number];
