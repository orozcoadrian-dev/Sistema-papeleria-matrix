ALTER TABLE administrador
ADD CONSTRAINT fk_administrador_usuario
FOREIGN KEY (Id_usuario) REFERENCES usuario(Id_usuario);