CREATE TABLE clave_de_permiso
(
	Id_clave INT PRIMARY KEY AUTO_INCREMENT,
	Usuario_id INT NOT NULL UNIQUE,
    Clave VARCHAR(60) NOT NULL,
    Fecha_creacion DATE NOT NULL,
    Fecha_actualizacion DATE NULL
);