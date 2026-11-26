// Ejecuta en el navegador los casos de pantalla del informe de pruebas y toma las capturas de los manuales.
//
// Antes de correrlo:
//   1. El backend en modo demo recién encendido, en el puerto 8080.
//   2. La carpeta frontend servida en el puerto 5500 (jwebserver -d frontend -p 5500).
//   3. En movil/, haber hecho «npm install» y «npx playwright install chromium».
//   4. El programa pdftotext en el equipo (viene con Git para Windows).
// Uso, desde la raíz del repositorio:  node 07-entrega-final/fuentes/probar-pantallas.mjs
import { execFileSync } from 'node:child_process';
import { mkdirSync, mkdtempSync, readFileSync, writeFileSync } from 'node:fs';
import { createRequire } from 'node:module';
import { tmpdir } from 'node:os';
import { dirname, join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const raiz = resolve(dirname(fileURLToPath(import.meta.url)), '../..');
// Aquí quedan los PDF descargados y el resumen de resultados.
const temporal = process.argv[2] ?? mkdtempSync(join(tmpdir(), 'matrix-pantallas-'));
const { chromium, expect } = createRequire(join(raiz, 'movil/package.json'))('@playwright/test');
const CAPTURAS = join(raiz, '07-entrega-final/capturas');
mkdirSync(temporal, { recursive: true });
const semilla = readFileSync(join(raiz, 'backend/src/main/resources/demo/datos-demo.sql'), 'utf8');
const demo = (nombre) => {
  const [, usuario, contrasenia] = semilla.match(new RegExp(`usuario (${nombre})\\s+contraseña (\\S+)`));
  return { usuario, contrasenia };
};
const ADMIN = demo('admin\\.matrix');
const OPERADOR = demo('operador\\.matrix');
// Usuarios y claves inventados para esta prueba en el equipo local.
const CAMILA = { usuario: 'camila.rojas', contrasenia: 'Camila-Clave-2026' };
const PERMISO = 'Permiso-Prueba-2026';
const WEB = 'http://localhost:5500';
const API = 'http://localhost:8080/api';

for (let i = 0; ; i++) {
  try { if ((await fetch('http://localhost:8080/actuator/health')).ok) break; } catch { /* todavía no */ }
  if (i > 90) throw new Error('El servidor no encendió');
  await new Promise((r) => setTimeout(r, 1000));
}
async function api(ruta, token, metodo = 'GET', cuerpo) {
  const r = await fetch(API + ruta, {
    method: metodo,
    headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}) },
    body: cuerpo && JSON.stringify(cuerpo),
  });
  const texto = await r.text();
  return { estado: r.status, datos: texto ? JSON.parse(texto) : null };
}
const sesionAdmin = (await api('/sesion', null, 'POST', { nombreUsuario: ADMIN.usuario, contrasenia: ADMIN.contrasenia })).datos.codigo;
const unidades = async (nombre) => (await api('/productos/interno', sesionAdmin)).datos.find((p) => p.nombre === nombre).cantidad;
await api('/productos', sesionAdmin, 'POST', { codigo: 'PAP-007', nombre: 'Resaltador amarillo', cantidad: 2, precioVenta: 1500, precioCompra: 900, descripcion: 'Resaltador punta biselada.' });

const navegador = await chromium.launch();
const errores = [];
const nueva = async (movil = false) => {
  const contexto = await navegador.newContext(movil
    ? { locale: 'es-CO', viewport: { width: 375, height: 812 }, isMobile: true, hasTouch: true }
    : { locale: 'es-CO', viewport: { width: 1280, height: 800 }, deviceScaleFactor: 0.625 });
  const pagina = await contexto.newPage();
  pagina.setDefaultTimeout(8000);
  pagina.on('pageerror', (e) => errores.push(`${pagina.url()}: ${e.message}`));
  return pagina;
};
const login = async (pagina, quien) => {
  await pagina.goto(`${WEB}/views/login.html`);
  await pagina.evaluate(() => sessionStorage.clear());
  await pagina.goto(`${WEB}/views/login.html`);
  await pagina.fill('#usuario', quien.usuario);
  await pagina.fill('#clave', quien.contrasenia);
  await pagina.click('#boton-entrar');
};
const foto = async (pagina, nombre, arriba) => {
  if (arriba) await pagina.locator(arriba).evaluate((e) => window.scrollTo(0, e.getBoundingClientRect().top + window.scrollY - 12));
  await pagina.waitForTimeout(350);
  await pagina.screenshot({ path: join(CAPTURAS, `${nombre}.jpg`), type: 'jpeg', quality: 82, style: '.aviso-flotante{visibility:hidden}' });
};
const pdf = async (pagina, accion, nombre) => {
  const [descarga] = await Promise.all([pagina.waitForEvent('download'), accion()]);
  const ruta = join(temporal, nombre);
  await descarga.saveAs(ruta);
  return execFileSync('pdftotext', ['-layout', '-enc', 'UTF-8', ruta, '-']).toString('utf8');
};

const resultados = [];
async function caso(nombre, pasos, esperado, prueba) {
  const id = `P-${String(resultados.length + 1).padStart(2, '0')}`;
  try {
    const nota = await prueba();
    resultados.push({ id, nombre, pasos, esperado: typeof nota === 'string' ? nota : esperado, pasa: true });
    console.log(`✓ ${id} ${nombre}`);
  } catch (error) {
    resultados.push({ id, nombre, pasos, esperado, pasa: false });
    console.log(`✗ ${id} ${nombre}: ${error.message.split('\n').slice(0, 6).join(' | ')}`);
  }
}

const panel = await nueva();
const caja = await nueva();
const mosaico = (texto) => panel.locator('button.mosaico', { hasText: texto }).click();
const tarjeta = (texto) => panel.locator('#tarjetas article', { hasText: texto });
const facturas = caja.locator('#ventas article');
const agregar = (nombre) => caja.getByRole('button', { name: new RegExp(`^Agregar ${nombre}`) }).click();
const guardarPanel = async () => {
  const ventana = panel.locator('#ventana-formulario');
  await panel.click('#boton-guardar');
  // Si el sistema pide confirmar (un aviso), hay que tocar «Guardar» otra vez.
  await expect.poll(async () => !(await ventana.isVisible()) || (await panel.locator('#formulario-error').textContent()).includes('otra vez')).toBe(true);
  if (await ventana.isVisible()) await panel.click('#boton-guardar');
  await expect(ventana).toBeHidden();
};

await caso('Entrar sin sesión al panel', 'Abrir <code>views/panel.html</code> sin haber entrado.', 'Lleva al inicio de sesión con el aviso «Entra con tu usuario».', async () => {
  await panel.goto(`${WEB}/views/panel.html`);
  await expect(panel).toHaveURL(/login\.html/);
  await expect(panel.locator('#login-aviso')).toContainText('Entra con tu usuario');
  await foto(panel, '02-iniciar-sesion');
});
await caso('Contraseña incorrecta', 'Entrar como administradora con una contraseña falsa.', '«El usuario o la contraseña no son correctos.»', async () => {
  await login(panel, { usuario: ADMIN.usuario, contrasenia: 'no-es-la-clave' });
  await expect(panel.getByText('El usuario o la contraseña no son correctos')).toBeVisible();
});
await caso('Entrar por rol', 'Entrar como administradora y como operador.', 'La administradora llega al panel; el operador a la caja.', async () => {
  await login(panel, ADMIN);
  await expect(panel).toHaveURL(/panel\.html/);
  await login(caja, OPERADOR);
  await expect(caja).toHaveURL(/ventas\.html/);
  await expect(panel.locator('#tarjetas article').first()).toBeVisible();
  await foto(panel, '04-panel-inicio');
});
await caso('El operador no abre el panel', 'Con sesión de operador, abrir <code>panel.html</code>.', 'Lo devuelve a la caja.', async () => {
  await caja.goto(`${WEB}/views/panel.html`);
  await expect(caja).toHaveURL(/ventas\.html/);
});
await caso('Cierre por inactividad', 'Simular 16 minutos sin uso y recargar.', 'Cierra la sesión y avisa que pasaron 15 minutos.', async () => {
  const otra = await nueva();
  await login(otra, OPERADOR);
  await expect(otra).toHaveURL(/ventas\.html/);
  await otra.evaluate(() => sessionStorage.setItem('matrix.ultimo-uso', String(Date.now() - 16 * 60000)));
  await otra.reload();
  await expect(otra).toHaveURL(/motivo=inactividad/);
  await expect(otra.locator('#login-aviso')).toContainText('15 minutos');
  await foto(otra, '03-sesion-cerrada');
});
await caso('Existencias en el panel', 'Abrir Papelería y cambiar un producto.', 'Las tarjetas muestran «Quedan N» y el formulario trae unidades y precio de compra.', async () => {
  await mosaico('Papelería');
  await expect(tarjeta('Carpeta blanca')).toContainText('Quedan 59');
  await foto(panel, '05-papeleria', '#seccion-titulo');
  await tarjeta('Carpeta blanca').getByRole('button', { name: /^Cambiar/ }).click();
  await expect(panel.locator('#campo-cantidad')).toHaveValue('59');
  await expect(panel.locator('#campo-precioCompra')).toHaveValue(/500/);
  await foto(panel, '06-cambiar-producto');
  await panel.locator('#ventana-formulario [data-cerrar]').click();
});
await caso('Crear operador', 'Agregar un operador con contraseña y luego entrar con él.', 'Se guarda y puede entrar con su usuario.', async () => {
  await mosaico('Operadores');
  await panel.click('#boton-agregar');
  const datos = {
    primerNombre: 'Camila', primerApellido: 'Rojas', segundoApellido: 'Díaz', tipoDocumento: 'CC', numeroDocumento: '100000000000077',
    nombreUsuario: CAMILA.usuario, contrasenia: CAMILA.contrasenia, salario: '1200000', direccion: 'Calle 20 # 15-30, barrio Centro',
  };
  for (const [campo, valor] of Object.entries(datos)) await panel.fill(`#campo-${campo}`, valor);
  await guardarPanel();
  await expect(tarjeta('Camila')).toContainText('Activo');
  const suya = await nueva();
  await login(suya, CAMILA);
  await expect(suya).toHaveURL(/ventas\.html/);
});
await caso('Cambiar operador sin tocar la contraseña', 'Cambiar el sueldo dejando la contraseña vacía.', 'Guarda y la contraseña anterior sigue sirviendo.', async () => {
  await tarjeta('Camila').getByRole('button', { name: /^Cambiar Camila/ }).click();
  await panel.fill('#campo-salario', '1250000');
  await guardarPanel();
  await expect(tarjeta('Camila')).toContainText('1.250.000');
  const suya = await nueva();
  await login(suya, CAMILA);
  await expect(suya).toHaveURL(/ventas\.html/);
});
await caso('Clave de permiso', 'Poner una clave a un operador.', 'La tarjeta muestra «Tiene clave de permiso».', async () => {
  await tarjeta('Daniel').getByRole('button', { name: /^Clave de permiso/ }).click();
  await panel.fill('#clave-permiso', PERMISO);
  await panel.getByRole('button', { name: 'Guardar clave' }).click();
  await expect(tarjeta('Daniel')).toContainText('Tiene clave de permiso');
});
await caso('Datos de la tienda con números', 'Guardar «Lunes a sábado de 8 a. m. a 7 p. m.».', 'Se guarda y aparece en la lista.', async () => {
  await mosaico('Datos de la tienda');
  await panel.click('#boton-agregar');
  await panel.fill('#campo-tipoInformacion', 'Horario tienda');
  await panel.fill('#campo-informacion', 'Lunes a sábado de 8 a. m. a 7 p. m.');
  await panel.fill('#campo-valorInformacion', '1');
  await guardarPanel();
  await expect(tarjeta('Horario tienda')).toContainText('8 a. m.');
  await foto(panel, '08-datos-tienda', '#seccion-titulo');
});
await caso('Venta completa', 'Agregar 2 impresiones a color, 1 carpeta blanca y 1 sobre carta; cobrar con $ 5.000.', 'Total $ 2.900, cambio $ 2.100, recibo con número de factura.', async () => {
  await caja.goto(`${WEB}/views/ventas.html`);
  await caja.click('#pestana-vender');
  await agregar('Impresión a color');
  await agregar('Impresión a color');
  await agregar('Carpeta blanca');
  await agregar('Sobre de manila carta');
  await expect(caja.locator('#carrito-total')).toContainText('2.900');
  await foto(caja, '09-caja', '.pestanas');
  await caja.click('#boton-cobrar');
  await caja.fill('#pago', '5000');
  await expect(caja.locator('#cambio')).toContainText('2.100');
  await foto(caja, '10-cobrar');
  await caja.click('#boton-confirmar-cobro');
  await expect(caja.locator('#recibo')).toContainText('2.900');
  await expect(caja.locator('#recibo')).toContainText('Factura n.º');
  await foto(caja, '11-recibo');
  await caja.click('#boton-nueva-venta');
});
await caso('No vender más de lo que hay', 'Agregar tres veces un producto del que quedan 2.', 'Avisa cuántas quedan y no agrega la tercera.', async () => {
  await agregar('Resaltador amarillo');
  await agregar('Resaltador amarillo');
  await expect(caja.locator('#articulos .articulo', { hasText: 'Resaltador amarillo' })).toContainText('Ya agregaste todas');
  await agregar('Resaltador amarillo');
  await expect(caja.locator('#aviso-flotante')).toContainText('Solo quedan 2');
  await expect(caja.locator('.linea__numero')).toHaveText('2');
  await caja.click('#boton-cobrar');
  await caja.click('#boton-confirmar-cobro');
  await caja.click('#boton-nueva-venta');
});
const admin = await nueva();
await caso('Anular venta (administradora)', 'Vender 2 carpetas a nombre de otro operador y anular la venta.', 'La venta queda marcada como anulada y las unidades vuelven.', async () => {
  await login(admin, ADMIN);
  await expect(admin).toHaveURL(/panel\.html/);
  await admin.goto(`${WEB}/views/ventas.html`);
  await admin.click('#pestana-vender');
  const antes = await unidades('Carpeta blanca');
  await admin.selectOption('#operador', { label: 'Camila Rojas' });
  const boton = admin.getByRole('button', { name: /^Agregar Carpeta blanca/ });
  await boton.click();
  await boton.click();
  await admin.click('#boton-cobrar');
  await admin.click('#boton-confirmar-cobro');
  await admin.click('#boton-nueva-venta');
  expect(await unidades('Carpeta blanca')).toBe(antes - 2);
  await admin.click('#pestana-vendido');
  const primera = admin.locator('#ventas article').first();
  await primera.getByRole('button', { name: /^Anular la venta/ }).click();
  await admin.click('#boton-confirmar-anular');
  await expect(primera).toContainText('Anulada');
  expect(await unidades('Carpeta blanca')).toBe(antes);
  return `La venta queda marcada como anulada y las unidades vuelven (${antes - 2} → ${antes}).`;
});
await caso('Lo vendido por rol', 'Abrir «Lo vendido» como operador y como administradora.', 'El operador ve solo las suyas; la administradora ve todas y quién vendió.', async () => {
  await caja.click('#pestana-vendido');
  await caja.reload();
  await expect(facturas.first()).toBeVisible();
  await expect(caja.locator('#ventas')).not.toContainText('Camila');
  const propias = await facturas.count();
  await admin.reload();
  await expect(admin.locator('#ventas article').first()).toContainText('Vendió: Camila Rojas');
  expect(await admin.locator('#ventas article').count()).toBeGreaterThan(propias);
});
await caso('Cambiar una venta con la clave', 'Como operador, quitar una impresión de una venta con una clave equivocada y luego con la correcta.', 'La clave equivocada se rechaza; con la correcta se guarda y la factura conserva su número.', async () => {
  const venta = facturas.filter({ hasText: 'Impresión a color' }).filter({ hasText: 'Sobre de manila' });
  const numero = (await venta.locator('.venta__codigo').textContent()).match(/\d+/)[0];
  await venta.getByRole('button', { name: /^Cambiar la venta/ }).click();
  await expect(caja.locator('#aviso-cambio')).toBeVisible();
  await caja.getByRole('button', { name: 'Uno menos de Impresión a color' }).click();
  await foto(caja, '20-cambiar-venta', '.pestanas');
  await caja.click('#boton-cobrar');
  await caja.fill('#clave-permiso', 'clave-equivocada');
  await caja.click('#boton-confirmar-cobro');
  await expect(caja.locator('#cobrar-error')).toContainText('no es correcta');
  await caja.fill('#clave-permiso', PERMISO);
  await caja.click('#boton-confirmar-cobro');
  await expect(facturas.filter({ hasText: numero })).toContainText('1 × Impresión a color');
});
await caso('Anular con la clave de permiso', 'Como operador, anular una de sus ventas escribiendo su clave.', 'Sin clave no se anula; con la clave queda marcada como anulada.', async () => {
  const venta = facturas.filter({ hasText: 'Resaltador amarillo' });
  await venta.getByRole('button', { name: /^Anular la venta/ }).click();
  await caja.click('#boton-confirmar-anular');
  await expect(caja.locator('#anular-error')).toContainText('Escribe tu clave');
  await caja.fill('#anular-clave', PERMISO);
  await caja.click('#boton-confirmar-anular');
  await expect(venta).toContainText('Anulada');
});
await caso('Datos temporales del cliente', 'Vender escribiendo un celular incompleto y después corregirlo.', 'Avisa del dato mal escrito; al corregirlo, el recibo muestra al cliente.', async () => {
  await caja.click('#pestana-vender');
  await agregar('Plumones de colores');
  await agregar('Impresión a color');
  await caja.click('#boton-cobrar');
  await caja.locator('#caja-cliente summary').click();
  await caja.fill('#cliente-nombre', 'Lucía Márquez');
  await caja.fill('#cliente-celular', '12');
  await caja.click('#boton-confirmar-cobro');
  await expect(caja.locator('#cobrar-error')).toContainText('celular');
  await caja.fill('#cliente-celular', '3001234567');
  await caja.fill('#cliente-correo', 'lucia.marquez@ejemplo.com');
  await caja.click('#boton-confirmar-cobro');
  await expect(caja.locator('#recibo')).toContainText('Lucía Márquez');
});
await caso('Venta en PDF', 'En el recibo, tocar «Descargar PDF».', 'Se descarga un PDF con el número, el cliente, los artículos y el total.', async () => {
  const texto = await pdf(caja, () => caja.click('#boton-recibo-pdf'), 'venta.pdf');
  for (const parte of ['Papelería Matrix', 'Lucía Márquez', 'Plumones de colores', 'Total pagado']) expect(texto).toContain(parte);
});
await caso('Venta por correo', 'Tocar «Enviar por correo» sin cuenta de envío configurada.', 'Ofrece abrir el correo de la persona con el mensaje ya escrito.', async () => {
  await caja.click('#boton-recibo-correo');
  await caja.click('#boton-enviar-correo');
  await expect(caja.locator('#correo-propio')).toBeVisible();
  expect(await caja.locator('#correo-propio').getAttribute('href')).toMatch(/^mailto:lucia\.marquez@ejemplo\.com\?subject=/);
  await foto(caja, '18-correo');
  await caja.locator('#ventana-correo [data-cerrar]').click();
  await caja.click('#boton-nueva-venta');
});
await caso('Buscar y filtrar facturas', 'Buscar por número y usar «Con servicios» y «Anuladas».', 'Muestra solo las facturas que coinciden.', async () => {
  await caja.click('#pestana-vendido');
  await expect(facturas.first()).toContainText('Plumones');
  await foto(caja, '12-lo-vendido', '.pestanas');
  const numero = (await facturas.first().locator('.venta__codigo').textContent()).match(/\d+/)[0];
  await caja.fill('#buscar-vendido', numero);
  await expect(facturas).toHaveCount(1);
  await caja.fill('#buscar-vendido', '');
  await caja.getByRole('button', { name: 'Con servicios' }).click();
  await expect(facturas.first()).toContainText('Impresión');
  await caja.getByRole('button', { name: 'Anuladas' }).click();
  await expect(facturas.first()).toContainText('Anulada');
  await caja.getByRole('button', { name: 'Todas', exact: true }).click();
});
await caso('El operador anota existencias', 'En «Existencias», escribir una letra y después la cantidad contada.', 'Rechaza el texto y guarda la cantidad nueva.', async () => {
  await caja.click('#pestana-existencias');
  await expect(caja.locator('#existencias-lista article').first()).toBeVisible();
  await foto(caja, '19-existencias', '.pestanas');
  await caja.fill('#buscar-existencias', 'carpeta blanca');
  const producto = caja.locator('#existencias-lista article').first();
  await producto.getByRole('button', { name: /Cambiar la cantidad/ }).click();
  await caja.fill('#existencias-cantidad', 'doce');
  await caja.click('#boton-guardar-existencias');
  await expect(caja.locator('#existencias-error')).toContainText('número');
  await caja.fill('#existencias-cantidad', '75');
  await caja.click('#boton-guardar-existencias');
  await expect(producto).toContainText('Quedan 75');
});
await caso('Retirar sin borrar', 'Quitar, con un motivo, un producto que ya aparece en ventas.', 'Deja de mostrarse y las ventas anteriores lo siguen nombrando.', async () => {
  await panel.reload();
  await mosaico('Papelería');
  await tarjeta('Resaltador amarillo').getByRole('button', { name: /^Quitar/ }).click();
  await panel.fill('#quitar-motivo', 'Ya no se consigue');
  await foto(panel, '17-quitar');
  await panel.click('#boton-confirmar-quitar');
  await expect(tarjeta('Resaltador amarillo')).toHaveCount(0);
  const ventas = (await api('/ventas?incluirAnuladas=true', sesionAdmin)).datos;
  expect(JSON.stringify(ventas)).toContain('Resaltador amarillo');
});
await caso('Guardar sin cambiar nada', 'Abrir un producto y tocar «Guardar» sin modificarlo.', 'Avisa que no se cambió nada.', async () => {
  await tarjeta('Carpeta blanca').getByRole('button', { name: /^Cambiar/ }).click();
  await panel.click('#boton-guardar');
  await expect(panel.locator('#formulario-error')).toContainText('No cambiaste nada');
  await panel.locator('#ventana-formulario [data-cerrar]').click();
});
await caso('Estados de un operador', 'Suspender, intentar entrar, retirar y volver a contratar.', 'Suspendido no puede entrar; retirado queda en «Despedidos» y se puede volver a contratar.', async () => {
  await mosaico('Operadores');
  await tarjeta('Camila').getByRole('button', { name: /^Suspender/ }).click();
  await panel.click('#boton-confirmar-quitar');
  await expect(tarjeta('Camila')).toContainText('Suspendido');
  await foto(panel, '07-operadores', '#seccion-titulo');
  const suya = await nueva();
  await login(suya, CAMILA);
  await expect(suya.getByText('Tu usuario está suspendido')).toBeVisible();
  await tarjeta('Camila').getByRole('button', { name: /^Retirar/ }).click();
  await panel.click('#boton-confirmar-quitar');
  await panel.getByRole('button', { name: /^Despedidos/ }).click();
  await expect(tarjeta('Camila')).toContainText('Despedido');
  await tarjeta('Camila').getByRole('button', { name: /^Reactivar/ }).click();
  await panel.click('#boton-confirmar-quitar');
  await panel.getByRole('button', { name: /^Activos/ }).click();
  await expect(tarjeta('Camila')).toContainText('Activo');
});
const publica = await nueva();
await caso('Página pública', 'Abrir la página sin sesión.', 'Muestra servicios, papelería y ropa desde la API; no tiene botón «Registrarse».', async () => {
  await publica.goto(`${WEB}/index.html`);
  await publica.getByRole('button', { name: 'Entendido' }).click();
  await expect(publica.locator('#servicios-grid > li')).not.toHaveCount(0);
  await expect(publica.locator('#papeleria-grid > li')).not.toHaveCount(0);
  await expect(publica.locator('#ropa-grid > li')).not.toHaveCount(0);
  await expect(publica.getByText('Registrarse')).toHaveCount(0);
  await foto(publica, '01-pagina-publica');
});
await caso('Destacados', 'Mirar la página, escribir en un buscador y abrir la lupa.', 'Los destacados se ven mientras no se busca y aparecen en la lupa.', async () => {
  await expect(publica.locator('#destacados')).toBeVisible();
  await foto(publica, '16-destacados', '#destacados');
  await publica.fill('#buscar-papeleria', 'carpeta');
  await expect(publica.locator('#destacados')).toBeHidden();
  await publica.fill('#buscar-papeleria', '');
  await expect(publica.locator('#destacados')).toBeVisible();
  await publica.locator('[data-dialog-open="dialog-buscar"]').first().click();
  await expect(publica.locator('#resultados-negocio')).toContainText('lo destacado');
  await publica.keyboard.press('Escape');
});
await caso('Horarios publicados y retirados', 'Publicar un horario, mirar la página, quitarlo y recargar.', 'Se ve el horario publicado; al quitarlo vuelven los de la página.', async () => {
  await expect(publica.locator('#datos-tienda-lista')).toContainText('8 a. m.');
  await expect(publica.locator('.quick-info__list').first()).toBeHidden();
  await mosaico('Datos de la tienda');
  await tarjeta('Horario tienda').getByRole('button', { name: /^Quitar/ }).click();
  await panel.click('#boton-confirmar-quitar');
  await expect(tarjeta('Horario tienda')).toHaveCount(0);
  await publica.reload();
  await expect(publica.locator('.quick-info__list').first()).toBeVisible();
  await expect(publica.locator('#datos-tienda')).toBeHidden();
});
const cuentas = await nueva();
await caso('Cuentas y corte', 'Abrir Cuentas del mes y guardar el corte.', 'El total de la pantalla coincide con el calculado por el servidor y el corte aparece en el historial.', async () => {
  await login(cuentas, ADMIN);
  await expect(cuentas).toHaveURL(/panel\.html/);
  await cuentas.goto(`${WEB}/views/cuentas.html`);
  await expect(cuentas.locator('#resumen-ventas .cifra__valor').first()).toBeVisible();
  const hoy = new Date().toLocaleDateString('sv-SE');
  const servidor = (await api(`/ingresos/resumen?desde=${hoy.slice(0, 8)}01&hasta=${hoy}`, sesionAdmin)).datos.totalGeneral;
  const pantalla = Number((await cuentas.locator('#resumen-ventas .cifra__valor').first().textContent()).replace(/\D/g, ''));
  expect(pantalla).toBe(servidor);
  await foto(cuentas, '13-cuentas');
  await cuentas.click('#boton-guardar-corte');
  await expect(cuentas.locator('#guardados article')).toHaveCount(1);
  return `El total de la pantalla coincide con el del servidor ($ ${servidor.toLocaleString('es-CO')}) y el corte aparece en el historial.`;
});
await caso('Contadores y todos los ingresos', 'Revisar «Lo registrado» y elegir el periodo «Todo».', 'Muestra seis contadores y el consolidado desde el comienzo.', async () => {
  await expect(cuentas.locator('#resumen-registrado .cifra')).toHaveCount(6);
  await cuentas.getByRole('button', { name: 'Todo', exact: true }).click();
  await expect(cuentas.locator('#periodo-texto')).toHaveText('Todo desde el comienzo');
});
await caso('Historiales', 'Ver y buscar los historiales de ingresos y de estadísticas.', 'Cada historial muestra su fecha, sus cifras y el periodo; la búsqueda filtra.', async () => {
  await expect(cuentas.locator('#guardados article').first()).toContainText('Periodo');
  await cuentas.getByRole('button', { name: /Estadísticas \(/ }).click();
  await expect(cuentas.locator('#guardados article').first()).toContainText('Registrado:');
  await cuentas.fill('#buscar-guardados', 'zzzz');
  await expect(cuentas.getByText('No encontramos nada con esa búsqueda.')).toBeVisible();
  await cuentas.fill('#buscar-guardados', '');
});
await caso('Reporte en PDF', 'Generar un reporte y volver a descargarlo desde la lista.', 'Se descarga con ingresos y estadísticas y queda en «Reportes».', async () => {
  const primero = await pdf(cuentas, () => cuentas.click('#boton-reporte'), 'reporte.pdf');
  for (const parte of ['Reporte del negocio', 'Total vendido', 'Estadísticas']) expect(primero).toContain(parte);
  await expect(cuentas.locator('#guardados article')).toHaveCount(1);
  await cuentas.locator('.cuentas__cortes').evaluate((e) => window.scrollTo(0, e.getBoundingClientRect().top + window.scrollY - 12));
  await foto(cuentas, '14-cuentas-cortes');
  const otra = await pdf(cuentas, () => cuentas.locator('#guardados article').first().getByRole('button', { name: /Descargar/ }).click(), 'reporte2.pdf');
  expect(otra).toContain('Total vendido');
});
await caso('Eliminar historiales y reportes', 'Eliminar un historial que tiene reporte; después el reporte y el historial.', 'Pide borrar primero el reporte; después se eliminan ambos.', async () => {
  await cuentas.getByRole('button', { name: /Ingresos \(/ }).click();
  const total = await cuentas.locator('#guardados article').count();
  const borrar = async () => {
    await cuentas.locator('#guardados article').first().getByRole('button', { name: /^Eliminar/ }).click();
    await cuentas.click('#boton-confirmar-eliminar');
  };
  await borrar();
  await expect(cuentas.locator('#aviso-flotante')).toContainText('Elimina primero el reporte');
  await cuentas.getByRole('button', { name: /Reportes \(/ }).click();
  await borrar();
  await expect(cuentas.getByText('Todavía no has generado ningún reporte.')).toBeVisible();
  await cuentas.getByRole('button', { name: /Ingresos \(/ }).click();
  await borrar();
  await expect(cuentas.locator('#guardados article')).toHaveCount(total - 1);
});
await caso('Celular', 'Abrir la página, el panel, la caja y Cuentas en 375 px de ancho.', 'Sin desplazamiento lateral; total y «Cobrar» fijos abajo.', async () => {
  const movil = await nueva(true);
  const sobra = () => movil.evaluate(() => document.documentElement.scrollWidth - document.documentElement.clientWidth);
  await movil.goto(`${WEB}/index.html`);
  await movil.waitForLoadState('networkidle');
  expect(await sobra()).toBe(0);
  await login(movil, ADMIN);
  await expect(movil).toHaveURL(/panel\.html/);
  await movil.locator('button.mosaico', { hasText: 'Operadores' }).click();
  await expect(movil.locator('#tarjetas article').first()).toBeVisible();
  expect(await sobra()).toBe(0);
  await movil.goto(`${WEB}/views/cuentas.html`);
  await expect(movil.locator('#resumen-registrado .cifra').first()).toBeVisible();
  expect(await sobra()).toBe(0);
  await movil.goto(`${WEB}/views/ventas.html`);
  await movil.click('#pestana-vendido');
  await expect(movil.locator('#ventas article').first()).toBeVisible();
  expect(await sobra()).toBe(0);
  await movil.click('#pestana-vender');
  await movil.selectOption('#operador', { label: 'Daniel Pérez' });
  await movil.getByRole('button', { name: /^Agregar Carpeta blanca/ }).click();
  await movil.getByRole('button', { name: /^Agregar Impresión a color/ }).click();
  await expect(movil.locator('#barra-cobro')).toBeVisible();
  expect(await sobra()).toBe(0);
  await movil.evaluate(() => window.scrollTo(0, 0));
  await movil.waitForTimeout(400);
  await movil.screenshot({ path: join(CAPTURAS, '15-caja-celular.jpg'), type: 'jpeg', quality: 82 });
});

writeFileSync(join(temporal, 'resultados.json'), JSON.stringify(resultados, null, 1), 'utf8');
console.log(`${resultados.filter((r) => r.pasa).length} de ${resultados.length} casos pasan. Errores en páginas: ${errores.length ? errores.join(' / ') : 'ninguno'}`);
console.log(`Resumen en ${join(temporal, 'resultados.json')}`);
if (resultados.some((r) => !r.pasa) || errores.length) process.exitCode = 1;
await navegador.close();
