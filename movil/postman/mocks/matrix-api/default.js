const http = require('http');

// Datos y claves inventados: solo existen en esta simulación.
const PERMISO = 'Clave-Prueba-123';
const semilla = () => ({
  usuarios: [
    { id: 1, usuario: 'admin.prueba', contrasenia: 'Prueba-Admin-1', nombre: 'Marta Prueba', rol: 'ADMINISTRADOR', rolNombre: 'Administrador' },
    { id: 2, usuario: 'caja.prueba', contrasenia: 'Prueba-Caja-1', nombre: 'Daniel Prueba', rol: 'OPERADOR_DE_CIBERCAFE', rolNombre: 'Operador de cibercafé' },
  ],
  servicios: [{ id: 1, codigo: 'SER-001', nombre: 'Impresión a color', categoria: 'Impresión', precio: 1000, descripcion: '' }],
  productos: [{ id: 1, codigo: 'PAP-001', nombre: 'Carpeta blanca', cantidad: 3, precio: 400, precioCompra: 250, lugarCompra: 'Centro', descripcion: '' }],
  ropa: [{ id: 1, codigo: 'ROP-001', nombre: 'Blusa rosada', tipo: 'Blusa', talla: 'M', material: 'Algodón', color: 'Rosado', precio: 12000, descripcion: '', disponible: true }],
  ventas: [],
  falla: 0,
});

http.createServer(async (req, res) => {
  const estado = typeof pm !== 'undefined' ? pm.state : null;
  const clave = 'matrix-api.datos';
  const leer = async () => (estado ? await estado.get(clave) : globalThis[clave]);
  const guardar = async (valor) => { if (estado) await estado.set(clave, valor); else globalThis[clave] = valor; };

  const url = new URL(req.url, 'http://localhost');
  const ruta = url.pathname.replace(/^\/matrix-api/, '');
  const enviar = (codigo, cuerpo) => {
    res.writeHead(codigo, { 'Content-Type': 'application/json' });
    res.end(codigo === 204 ? undefined : JSON.stringify(cuerpo));
  };
  const problema = (codigo, title, detail, errores) => enviar(codigo, { title, status: codigo, detail, errores });

  let cuerpo = req.body;
  if (!cuerpo) {
    let crudo = '';
    for await (const parte of req) crudo += parte;
    try { cuerpo = crudo ? JSON.parse(crudo) : {}; } catch { return problema(400, 'Cuerpo ilegible', 'El cuerpo no es JSON.'); }
  }
  if (Buffer.isBuffer(cuerpo) || typeof cuerpo === 'string') {
    try { cuerpo = JSON.parse(cuerpo.toString() || '{}'); } catch { return problema(400, 'Cuerpo ilegible', 'El cuerpo no es JSON.'); }
  }

  if (ruta === '/__ready') return enviar(200, { ready: true });
  // Se trabaja sobre una copia: si una petición falla a la mitad, no queda nada cambiado.
  let datos = JSON.parse(JSON.stringify((await leer()) ?? null));
  if (!datos || ruta === '/__reset') { datos = semilla(); await guardar(datos); }
  if (ruta === '/__reset') return enviar(200, {});
  if (ruta === '/__falla') { datos.falla = cuerpo.estado; await guardar(datos); return enviar(200, {}); }

  const sesionDe = (usuario) => ({
    codigo: `simulado-${usuario.id}`, idUsuario: usuario.id, nombreUsuario: usuario.usuario, nombre: usuario.nombre,
    rol: usuario.rol, rolNombre: usuario.rolNombre, minutosInactividad: 15,
  });

  if (ruta === '/api/sesion') {
    if (req.method === 'DELETE') return enviar(204);
    const usuario = datos.usuarios.find((u) => u.usuario === cuerpo.nombreUsuario && u.contrasenia === cuerpo.contrasenia);
    return usuario ? enviar(200, sesionDe(usuario)) : problema(401, 'No se pudo entrar', 'El usuario o la contraseña no son correctos.');
  }

  if (datos.falla) return problema(datos.falla, 'Falla de prueba', 'Falla provocada por la prueba.');
  const actual = datos.usuarios.find((u) => req.headers.authorization === `Bearer simulado-${u.id}`);
  if (!actual) return problema(401, 'No autorizado', 'Inicia sesión para continuar.');
  const esAdmin = actual.rol === 'ADMINISTRADOR';

  if (ruta === '/api/operadores-cibercafe') {
    if (!esAdmin) return problema(403, 'Sin permiso', 'Tu usuario no tiene permiso.');
    return enviar(200, [{ idUsuario: 2, primerNombre: 'Daniel', primerApellido: 'Prueba' }]);
  }

  const partes = ruta.split('/').filter(Boolean); // api, recurso, id
  const recurso = partes[1];

  if (recurso === 'ventas') {
    const id = Number(partes[2]);
    if (req.method === 'GET') return enviar(200, datos.ventas.filter((v) => esAdmin || v.operadorId === actual.id));
    const devolver = (venta) => {
      for (const linea of venta.articulos) {
        if (linea.tipo === 'producto') datos.productos.find((p) => p.id === linea.id).cantidad += linea.cantidad;
        if (linea.tipo === 'ropa') datos.ropa.find((p) => p.id === linea.id).disponible = true;
      }
    };
    let anterior;
    if (req.method === 'DELETE' || req.method === 'PUT') {
      anterior = datos.ventas.find((v) => v.id === id);
      if (!anterior || (!esAdmin && anterior.operadorId !== actual.id)) return problema(404, 'No encontrado', 'No existe esa venta.');
      if (!esAdmin) {
        const permiso = decodeURIComponent(req.headers['x-clave-permiso'] || '');
        if (!permiso) return problema(403, 'Falta la clave de permiso', 'Escribe tu clave de permiso.');
        if (permiso !== PERMISO) return problema(403, 'Clave incorrecta', 'La clave de permiso no es correcta.');
      }
      devolver(anterior);
      if (req.method === 'DELETE') {
        datos.ventas = datos.ventas.filter((v) => v.id !== id);
        await guardar(datos);
        return enviar(204);
      }
    }
    if (req.method === 'POST' || req.method === 'PUT') {
      const lista = { servicio: datos.servicios, producto: datos.productos, ropa: datos.ropa };
      const lineas = [];
      for (const pedido of cuerpo.articulos || []) {
        const articulo = lista[pedido.tipo]?.find((a) => a.id === pedido.id);
        if (!articulo) return problema(404, 'No encontrado', 'Ese artículo no existe.');
        if (pedido.tipo === 'producto' && articulo.cantidad < pedido.cantidad) return problema(409, 'No hay suficientes existencias', `Solo quedan ${articulo.cantidad}.`);
        if (pedido.tipo === 'ropa' && !articulo.disponible) return problema(409, 'Prenda ya vendida', 'Esa prenda ya se vendió.');
        lineas.push({ articulo, linea: { tipo: pedido.tipo, id: pedido.id, nombre: articulo.nombre, cantidad: pedido.cantidad, precioUnitario: articulo.precio } });
      }
      const total = lineas.reduce((suma, { linea }) => suma + linea.precioUnitario * linea.cantidad, 0);
      if (cuerpo.totalEsperado !== total) return problema(409, 'Los precios cambiaron', 'Revisa la venta y vuelve a cobrar.');
      const operadorId = anterior ? anterior.operadorId : esAdmin ? cuerpo.operadorId : actual.id;
      if (!operadorId) return problema(400, 'Falta el operador', 'Indica el operador.');
      for (const { articulo, linea } of lineas) {
        if (linea.tipo === 'producto') articulo.cantidad -= linea.cantidad;
        if (linea.tipo === 'ropa') articulo.disponible = false;
      }
      const ahora = new Date();
      const fecha = [ahora.getFullYear(), String(ahora.getMonth() + 1).padStart(2, '0'), String(ahora.getDate()).padStart(2, '0')].join('-');
      const nuevo = Date.now();
      const venta = {
        id: anterior?.id ?? nuevo, codigo: anterior?.codigo ?? String(nuevo), fecha: anterior?.fecha ?? fecha,
        tipo: 'Venta en tienda', total, operadorId, operador: 'Daniel Prueba', articulos: lineas.map((l) => l.linea),
      };
      if (anterior) datos.ventas = datos.ventas.map((v) => (v.id === venta.id ? venta : v));
      else datos.ventas.unshift(venta);
      await guardar(datos);
      return enviar(anterior ? 200 : 201, venta);
    }
  }

  const coleccion = { servicios: 'servicios', productos: 'productos', ropa: 'ropa' }[recurso];
  if (coleccion) {
    if (req.method === 'GET') return enviar(200, datos[coleccion].map((a) => (coleccion === 'productos' && !esAdmin ? { ...a, precioCompra: null, lugarCompra: null } : a)));
    if (!esAdmin) return problema(403, 'Sin permiso', 'Tu usuario no tiene permiso.');
    const id = Number(partes[2]);
    const existente = datos[coleccion].find((a) => a.id === id);
    if (req.method !== 'POST' && !existente) return problema(404, 'No encontrado', 'Ese artículo no existe.');
    if (req.method === 'DELETE') {
      datos[coleccion] = datos[coleccion].filter((a) => a.id !== id);
      await guardar(datos);
      return enviar(204);
    }
    if (datos[coleccion].some((a) => a.codigo === cuerpo.codigo && a.id !== id)) return problema(409, 'Código repetido', 'Ya hay un artículo con ese código.');
    const { precioVenta, cantidadAnterior, tipoRopa, ...campos } = cuerpo;
    if (coleccion === 'productos') campos.precio = precioVenta;
    if (coleccion === 'ropa') campos.tipo = tipoRopa;
    if (existente) Object.assign(existente, campos);
    else datos[coleccion].push({ ...campos, id: Math.max(0, ...datos[coleccion].map((a) => a.id)) + 1, ...(coleccion === 'ropa' ? { disponible: true } : {}) });
    await guardar(datos);
    return enviar(existente ? 200 : 201, existente || datos[coleccion].at(-1));
  }

  problema(501, 'Fuera del contrato', 'La simulación no modela esta ruta.');
}).listen(process.env.PORT || 4020);
