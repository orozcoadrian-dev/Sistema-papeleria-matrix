CREATE TABLE Factura_de_negocio
(
	Id_factura INT PRIMARY KEY AUTO_INCREMENT,
    Codigo_factura VARCHAR(15) NOT NULL UNIQUE,
    Tipo_factura VARCHAR(15) NOT NULL,
	Usuario_id INT NOT NULL,
    Monto DOUBLE NOT NULL,
    Fecha_generacion DATE NOT NULL,
    Fecha_actualizacion DATE NULL
);