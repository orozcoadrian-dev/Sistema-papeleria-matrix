CREATE TABLE Operador_de_cibercafe
(
	Id_usuario INT PRIMARY KEY,
    Salario DOUBLE NOT NULL,
    Horario VARCHAR(80) NULL,
    Direccion VARCHAR(80) NOT NULL,
    Fecha_contrato DATE NOT NULL
);