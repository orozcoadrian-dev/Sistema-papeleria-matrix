ALTER TABLE Reporte_del_negocio
ADD CONSTRAINT fk_reporte_del_negocio_administrador
FOREIGN KEY (Usuario_id) REFERENCES Administrador(Id_usuario);