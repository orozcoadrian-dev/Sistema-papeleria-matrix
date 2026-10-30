ALTER TABLE reporte_del_negocio
ADD CONSTRAINT fk_reporte_del_negocio_administrador
FOREIGN KEY (Usuario_id) REFERENCES administrador(Id_usuario);