CREATE TABLE Estadistica_del_negocio
(
	Id_estadistica INT PRIMARY KEY AUTO_INCREMENT,
    Codigo_estadistica VARCHAR(15) NOT NULL,
    Numero_informacion_eliminada INT NULL,
    Servicios_eliminados INT NULL,
    Numero_informacion_agregada INT NULL,
    Numero_servicios_ofrecidos INT NULL,
    Numero_productos_vendidos INT NULL,
    Operadores_cibercafe_despedidos INT NULL,
    Rpa_actual_catalogo INT NULL,
    Numero_productos_papeleria INT NULL,
    Ropa_removida_catalogo INT NULL,
    Productos_removidos INT NULL,
    Numero_ropa_vendida INT NULL,
    Numero_servicios INT NULL,
    Numero_facturas_generadas INT NULL
);