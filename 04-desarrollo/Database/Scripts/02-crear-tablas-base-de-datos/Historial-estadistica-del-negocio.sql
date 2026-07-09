CREATE TABLE Historial_estadistica_del_negocio
(
	Id_historial_estadistica INT PRIMARY KEY AUTO_INCREMENT,
    Estadistica_del_negocio_id INT NOT NULL,
    Motivo_estadistica VARCHAR(100) NOT NULL,
    Fecha_generacion DATETIME NOT NULL
);