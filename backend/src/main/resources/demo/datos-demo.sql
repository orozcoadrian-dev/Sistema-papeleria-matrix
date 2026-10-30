-- Datos de ejemplo del modo demo. Se cargan en la base H2 al arrancar y se pierden al reiniciar.
-- Los productos y la ropa salen del catálogo de ejemplo de la página. Los servicios y sus precios son solo de ejemplo.

INSERT INTO servicio (Codigo_servicio, Nombre, Categoria, Precio, Descripcion, Fecha_vinculacion) VALUES
('SRV-001', 'Impresión blanco y negro', 'Impresión', 300, 'Impresión de documentos en blanco y negro, por página.', CURRENT_DATE),
('SRV-002', 'Impresión a color', 'Impresión', 800, 'Impresión de documentos a color, por página.', CURRENT_DATE),
('SRV-003', 'Fotocopia', 'Copias', 100, 'Fotocopia en blanco y negro, por página.', CURRENT_DATE),
('SRV-004', 'Escáner', 'Escáner', 500, 'Digitalización de documentos, por página.', CURRENT_DATE),
('SRV-005', 'Transcripción', 'Transcripción', 3000, 'Transcripción de textos, por página.', CURRENT_DATE);

INSERT INTO producto_de_papeleria (Codigo_producto, Nombre, Cantidad, Precio_venta, Precio_compra, Descripcion, Lugar_compra, Fecha_publicacion) VALUES
('PAP-001', 'Plumones de colores', 40, 2000, 1400, 'Plumones de colores.', 'Proveedor de ejemplo', CURRENT_DATE),
('PAP-002', 'Carpeta blanca', 60, 800, 500, 'Carpeta blanca.', 'Proveedor de ejemplo', CURRENT_DATE),
('PAP-003', 'Carpeta de cartón oficio', 30, 1000, 650, 'Carpeta de cartón, tamaño oficio.', 'Proveedor de ejemplo', CURRENT_DATE),
('PAP-004', 'Carpeta de cartón carta', 30, 800, 500, 'Carpeta de cartón, tamaño carta.', 'Proveedor de ejemplo', CURRENT_DATE),
('PAP-005', 'Sobre de manila oficio', 80, 600, 350, 'Sobre de manila, tamaño oficio.', 'Proveedor de ejemplo', CURRENT_DATE),
('PAP-006', 'Sobre de manila carta', 80, 500, 300, 'Sobre de manila, tamaño carta.', 'Proveedor de ejemplo', CURRENT_DATE);

INSERT INTO ropa (Codigo_ropa, Tipo_ropa, Material, Talla, Descripcion, Nombre, Precio, Color, Fecha_registro) VALUES
('ROP-001', 'Blusa', 'Algodón', 'M', 'Blusa de flores casual.', 'Blusa de flores casual', 15000, 'Estampado de flores', CURRENT_DATE),
('ROP-002', 'Blusa', 'Algodón', 'M', 'Blusa rosada sencilla.', 'Blusa rosada sencilla', 12000, 'Rosado', CURRENT_DATE),
('ROP-003', 'Chaqueta', 'Jean', '12', 'Chaqueta de jean para niña.', 'Chaqueta de jean para niña', 20000, 'Azul', CURRENT_DATE),
('ROP-004', 'Falda', 'Jean', '6', 'Falda de jean para niña.', 'Falda de jean para niña', 15000, 'Azul', CURRENT_DATE);
