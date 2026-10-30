ALTER TABLE detalle_de_factura
ADD CONSTRAINT fk_detalle_de_factura_ropa
FOREIGN KEY (Ropa_id) REFERENCES Ropa(Id_ropa);