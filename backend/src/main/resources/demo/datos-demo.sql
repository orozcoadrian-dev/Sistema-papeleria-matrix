-- Datos de ejemplo del modo demo. Se cargan en la base H2 al arrancar y se pierden al reiniciar.
-- Este archivo tiene el catálogo, los usuarios de prueba y las ventas de ejemplo.
-- Los datos de los módulos del negocio (información, claves, ingresos, estadísticas y reportes) van en datos-demo-negocio.sql.
-- Los productos y la ropa salen del catálogo de ejemplo de la página. Los servicios y sus precios son solo de ejemplo.

INSERT INTO servicio (Codigo_servicio, Nombre, Categoria, Precio, Descripcion, Fecha_vinculacion) VALUES
('SRV-001', 'Impresión blanco y negro', 'Impresión', 300, 'Impresión de documentos en blanco y negro, por página.', CURRENT_DATE),
('SRV-002', 'Impresión a color', 'Impresión', 800, 'Impresión de documentos a color, por página.', CURRENT_DATE),
('SRV-003', 'Fotocopia', 'Copias', 100, 'Fotocopia en blanco y negro, por página.', CURRENT_DATE),
('SRV-004', 'Escáner', 'Escáner', 500, 'Digitalización de documentos, por página.', CURRENT_DATE),
('SRV-005', 'Transcripción', 'Transcripción', 3000, 'Transcripción de textos, por página.', CURRENT_DATE);

INSERT INTO producto_de_papeleria (Codigo_producto, Nombre, Cantidad, Precio_venta, Precio_compra, Descripcion, Lugar_compra, Fecha_publicacion) VALUES
('PAP-001', 'Plumones de colores', 40, 2000, 1400, 'Plumones de colores.', 'Proveedor de ejemplo', CURRENT_DATE),
('PAP-002', 'Carpeta blanca', 59, 800, 500, 'Carpeta blanca.', 'Proveedor de ejemplo', CURRENT_DATE),
('PAP-003', 'Carpeta de cartón oficio', 30, 1000, 650, 'Carpeta de cartón, tamaño oficio.', 'Proveedor de ejemplo', CURRENT_DATE),
('PAP-004', 'Carpeta de cartón carta', 30, 800, 500, 'Carpeta de cartón, tamaño carta.', 'Proveedor de ejemplo', CURRENT_DATE),
('PAP-005', 'Sobre de manila oficio', 80, 600, 350, 'Sobre de manila, tamaño oficio.', 'Proveedor de ejemplo', CURRENT_DATE),
('PAP-006', 'Sobre de manila carta', 80, 500, 300, 'Sobre de manila, tamaño carta.', 'Proveedor de ejemplo', CURRENT_DATE);

INSERT INTO ropa (Codigo_ropa, Tipo_ropa, Material, Talla, Descripcion, Nombre, Precio, Color, Fecha_registro) VALUES
('ROP-001', 'Blusa', 'Algodón', 'M', 'Blusa de flores casual.', 'Blusa de flores casual', 15000, 'Estampado de flores', CURRENT_DATE),
('ROP-002', 'Blusa', 'Algodón', 'M', 'Blusa rosada sencilla.', 'Blusa rosada sencilla', 12000, 'Rosado', CURRENT_DATE),
('ROP-003', 'Chaqueta', 'Jean', '12', 'Chaqueta de jean para niña.', 'Chaqueta de jean para niña', 20000, 'Azul', CURRENT_DATE),
('ROP-004', 'Falda', 'Jean', '6', 'Falda de jean para niña.', 'Falda de jean para niña', 15000, 'Azul', CURRENT_DATE);

-- Usuarios de prueba. Solo existen en el modo demo.
--   Administrador:          usuario admin.matrix     contraseña Matrix-Admin-2026
--   Operador de cibercafé:  usuario operador.matrix  contraseña Matrix-Operador-2026
-- Las contraseñas se guardan cifradas con BCrypt, nunca en texto plano.
INSERT INTO usuario (Primer_nombre, Segundo_nombre, Primer_apellido, Segundo_apellido, Numero_documento, Tipo_documento, Nombre_usuario, Contrasenia, Rol, Email, Telefono, Ultima_sesion) VALUES
('Marta', NULL, 'Gómez', 'Ruiz', '100000000000001', 'CC', 'admin.matrix', '$2a$10$6LXNG8hNpCEN1SlQivdYKeek19gbjZKDjpNQPHT9rBEqink8rkBvy', 'Administrador', 'administracion@papeleriamatrix.com', '3000000001', CURRENT_TIMESTAMP),
('Daniel', NULL, 'Pérez', 'Mora', '100000000000002', 'CC', 'operador.matrix', '$2a$10$yiiBg5vk4mw.tVmxlTVAu.t371JoriQIrr3C6apV3FZYgDq0OPX0y', 'Operador de cibercafé', 'cibercafe@papeleriamatrix.com', '3000000002', CURRENT_TIMESTAMP);

INSERT INTO administrador (Id_usuario, Fecha_posicionamiento, Area_acceso) VALUES
(1, CURRENT_DATE, 'General');

INSERT INTO operador_de_cibercafe (Id_usuario, Salario, Horario, Direccion, Fecha_contrato) VALUES
(2, 1300000, 'Lunes a sábado de 8 a 6', 'Calle principal del barrio', CURRENT_DATE);

-- Dos ventas de ejemplo registradas por el operador. Las existencias de arriba ya tienen descontada la carpeta.
INSERT INTO factura_del_negocio (Codigo_factura, Tipo_factura, Usuario_id, Monto, Fecha_generacion) VALUES
('100000000000001', 'Venta en tienda', 2, 2400, CURRENT_DATE),
('100000000000002', 'Venta en tienda', 2, 12000, CURRENT_DATE);

INSERT INTO detalle_de_factura (Factura_del_negocio_id, Descripcion, Servicio_id, Cantidad_servicio, Producto_de_papeleria_id, Cantidad_producto, Ropa_id, Cantidad_ropa, Fecha_generacion) VALUES
(1, 'Venta de Impresión a color', 2, 2, NULL, NULL, NULL, NULL, CURRENT_TIMESTAMP),
(1, 'Venta de Carpeta blanca', NULL, NULL, 2, 1, NULL, NULL, CURRENT_TIMESTAMP),
(2, 'Venta de Blusa rosada sencilla', NULL, NULL, NULL, NULL, 2, 1, CURRENT_TIMESTAMP);

-- Lo que sale entre los destacados de la página.
UPDATE servicio SET Destacado = TRUE WHERE Nombre = 'Impresión a color';
UPDATE producto_de_papeleria SET Destacado = TRUE WHERE Codigo_producto = 'PAP-001';
UPDATE ropa SET Destacado = TRUE WHERE Codigo_ropa = 'ROP-001';
