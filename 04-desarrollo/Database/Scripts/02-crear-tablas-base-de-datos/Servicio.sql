CREATE TABLE Servicio
(
	Id_servicio INT PRIMARY KEY AUTO_INCREMENT,
    Codigo_servicio VARCHAR(20) NOT NULL UNIQUE,
    Nombre VARCHAR(30) NOT NULL,
	Categoria VARCHAR(30) NOT NULL,
    Precio DOUBLE NOT NULL,
    Descripcion VARCHAR(150) NULL,
    Fecha_vinculacion DATE NOT NULL,
    Fecha_actualizacion DATE NULL
);