CREATE TABLE Detalle_de_factura
(
	Id_detalle INT PRIMARY KEY AUTO_INCREMENT,
	Factura_de_negocio_id INT NOT NULL,
    Descripcion VARCHAR(120) NOT NULL,
    Servicio_id INT,
    Cantidad_servicio INT NULL,
    Producto_de_papeleria_id INT,
    Cantidad_producto INT NULL,
    Ropa_id INT NULL,
    Fecha_generacion DATETIME NOT NULL,
    Fecha_actualizacion DATETIME NULL
);