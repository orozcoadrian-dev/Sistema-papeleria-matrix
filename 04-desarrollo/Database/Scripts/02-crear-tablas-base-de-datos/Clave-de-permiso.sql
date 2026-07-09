CREATE TABLE Clave_de_permiso
(
	Id_clave INT PRIMARY KEY AUTO_INCREMENT,
	Usuario_id INT,
    Clave VARCHAR(30) NOT NULL UNIQUE,
    Fecha_creacion DATE NOT NULL,
    Fecha_actualizacion DATE NULL
);