CREATE TABLE Reporte_del_negocio
(
	Id_reporte INT PRIMARY KEY AUTO_INCREMENT,
    Codigo_reporte VARCHAR(15) NOT NULL UNIQUE,
    Historial_estadistica_del_negocio_id INT NULL,
    Historial_ingreso_del_negocio_id INT NULL, 
    Usuario_id INT NULL,
    Ruta_archivo VARCHAR(255) NOT NULL,
    Fecha_generacion DATE NOT NULL
);