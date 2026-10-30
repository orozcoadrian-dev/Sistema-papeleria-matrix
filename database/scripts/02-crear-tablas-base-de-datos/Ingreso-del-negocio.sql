CREATE TABLE Ingreso_del_negocio
(
	Id_ingreso INT PRIMARY KEY AUTO_INCREMENT,
    Codigo_ingreso VARCHAR(15) NOT NULL UNIQUE,
    Total_salario_operadores DOUBLE NULL,
    Total_vendido_ropa DOUBLE NULL,
    Inversion_total_papeleria DOUBLE NULL,
    Total_vendido_servicios DOUBLE NULL,
    Total_vendido_productos DOUBLE NULL,
    Total_recaudo_esperado_papeleria DOUBLE NULL,
    Tipo_ingreso VARCHAR(15) NOT NULL
);