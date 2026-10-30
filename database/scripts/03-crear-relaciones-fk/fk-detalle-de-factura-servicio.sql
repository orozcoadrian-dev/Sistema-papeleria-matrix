ALTER TABLE detalle_de_factura
ADD CONSTRAINT fk_detalle_de_factura_servicio
FOREIGN KEY (Servicio_id) REFERENCES Servicio(Id_servicio);