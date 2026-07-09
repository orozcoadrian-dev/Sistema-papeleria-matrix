CREATE TABLE Ropa
(
	Id_ropa INT PRIMARY KEY AUTO_INCREMENT,
    Codigo_ropa VARCHAR(20) NOT NULL UNIQUE,
    Tipo_ropa VARCHAR(20) NOT NULL,
    Material VARCHAR(30) NOT NULL,
    Talla VARCHAR(30) NOT NULL,
    Descripcion VARCHAR(150) NULL,
    Nombre VARCHAR(30),
    Precio DOUBLE NOT NULL,
    Color VARCHAR(40) NULL,
    Fecha_registro DATE NOT NULL,
    Fecha_actualizacion DATE NULL
);