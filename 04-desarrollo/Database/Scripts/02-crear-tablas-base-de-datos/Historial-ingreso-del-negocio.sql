CREATE TABLE Historial_ingreso_del_negocio
(
	Id_historial_ingreso INT PRIMARY KEY AUTO_INCREMENT,
    Ingreso_id INT NOT NULL,
    Motivo_ingreso VARCHAR(100) NOT NULL,
    Fecha_generacion DATETIME NOT NULL
);