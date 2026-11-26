-- Para una base de datos que ya existía antes de completar los casos de uso.
-- Se ejecuta una sola vez. En una base nueva no hace falta: las tablas ya traen estas columnas.
USE papeleria_matrix;

ALTER TABLE servicio
    ADD COLUMN Activo BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN Motivo_retiro VARCHAR(150) NULL,
    ADD COLUMN Destacado BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE producto_de_papeleria
    ADD COLUMN Activo BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN Motivo_retiro VARCHAR(150) NULL,
    ADD COLUMN Destacado BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE ropa
    ADD COLUMN Activo BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN Motivo_retiro VARCHAR(150) NULL,
    ADD COLUMN Destacado BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE informacion_del_negocio
    ADD COLUMN Activo BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN Motivo_retiro VARCHAR(150) NULL;

ALTER TABLE usuario
    ADD COLUMN Estado VARCHAR(12) NOT NULL DEFAULT 'Activo';

ALTER TABLE factura_del_negocio
    ADD COLUMN Anulada BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN Fecha_anulacion DATE NULL;
