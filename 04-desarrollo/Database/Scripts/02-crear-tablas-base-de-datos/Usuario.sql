CREATE TABLE Usuario
(
	Id_usuario INT PRIMARY KEY AUTO_INCREMENT,
    Primer_nombre VARCHAR(30) NOT NULL,
    Segundo_nombre VARCHAR(30) NULL,
    Primer_apellido VARCHAR(30) NOT NULL,
    Segundo_apellido VARCHAR(30) NOT NULL,
    Numero_documento VARCHAR(20) NOT NULL UNIQUE,
    Tipo_documento VARCHAR(5) NOT NULL,
    Nombre_usuario VARCHAR(15) NOT NULL UNIQUE,
    Contrasenia VARCHAR(255) NOT NULL,
    Rol ENUM('Administrador','Operador de cibercafé') NOT NULL, --Aquí el ENUM solo toma 2 roles válidos en la lógica de negocio.
    Email VARCHAR(60) NULL UNIQUE,
    Telefono VARCHAR(15) UNIQUE NULL,
    Ultima_sesion TIMESTAMP NOT NULL
);