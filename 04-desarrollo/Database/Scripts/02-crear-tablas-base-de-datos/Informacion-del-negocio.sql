CREATE TABLE Informacion_del_negocio
(
	Id_informacion INT PRIMARY KEY AUTO_INCREMENT,
    Usuario_id INT NOT NULL,
    Valor_informacion INT NOT NULL,
    Tipo_informacion VARCHAR(15)  NOT NULL,
    Informacion VARCHAR(255) NOT NULL,
    Fecha_creacion DATE NOT NULL, 
    Fecha_actualizacion DATE NOT NULL
);