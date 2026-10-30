ALTER TABLE detalle_de_factura
ADD CONSTRAINT fk_detalle_de_factura_ropa
FOREIGN KEY (Ropa_id) REFERENCES ropa(Id_ropa);