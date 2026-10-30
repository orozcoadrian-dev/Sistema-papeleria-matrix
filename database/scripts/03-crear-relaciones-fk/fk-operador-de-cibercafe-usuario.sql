ALTER TABLE operador_de_cibercafe
ADD CONSTRAINT fk_operador_de_cibercafe_usuario
FOREIGN KEY (Id_usuario) REFERENCES usuario(Id_usuario);