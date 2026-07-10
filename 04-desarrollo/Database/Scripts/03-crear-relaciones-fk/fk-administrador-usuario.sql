ALTER TABLE Administrador
ADD CONSTRAINT fk_administrador_usuario
FOREIGN KEY (Id_usuario) REFERENCES Usuario(Id_usuario);