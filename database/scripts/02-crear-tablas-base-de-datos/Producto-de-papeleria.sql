CREATE TABLE Producto_de_papeleria
(
	Id_producto INT PRIMARY KEY AUTO_INCREMENT,
    Codigo_producto VARCHAR(20) NOT NULL UNIQUE,
    Nombre VARCHAR(30) NOT NULL,
    Cantidad INT NOT NULL,
    Precio_venta DOUBLE NOT NULL,
    Precio_compra DOUBLE NOT NULL,
    Descripcion VARCHAR(150) NULL,
    Lugar_compra VARCHAR(40) NULL,
    Fecha_publicacion DATE NOT NULL,
    Fecha_actualizacion DATE NULL
);