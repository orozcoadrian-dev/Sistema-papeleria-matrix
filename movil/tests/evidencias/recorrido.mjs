// Recorre la aplicación contra la API real en modo demo, guarda capturas y anota las peticiones.
// Uso, con la API y `npm run dev` encendidos:  node tests/evidencias/recorrido.mjs http://127.0.0.1:5175
import { readFileSync } from 'node:fs';
import { chromium, devices, expect } from '@playwright/test';

const direccion = process.argv[2] ?? 'http://127.0.0.1:5173';
const semilla = readFileSync(new URL('../../../backend/src/main/resources/demo/datos-demo.sql', import.meta.url), 'utf8');

// Los usuarios de prueba están escritos en el propio proyecto, en los datos del modo demo.
function usuarioDemo(nombre) {
  const encontrado = semilla.match(new RegExp(`usuario (${nombre})\\s+contraseña (\\S+)`));
  if (!encontrado) throw new Error(`No está el usuario de prueba ${nombre}`);
  return { usuario: encontrado[1], contrasenia: encontrado[2] };
}

const navegador = await chromium.launch();
const pagina = await (await navegador.newContext({ ...devices['Pixel 7'], baseURL: direccion, locale: 'es-CO' })).newPage();
pagina.on('response', (r) => {
  const url = new URL(r.url());
  if (url.pathname.startsWith('/api/')) console.log(`${r.request().method()} ${url.pathname}${url.search} → ${r.status()}`);
});

const foto = async (nombre) => {
  await pagina.waitForLoadState('networkidle');
  await pagina.waitForTimeout(350);
  await pagina.screenshot({ path: `docs/evidencias/${nombre}.png` });
};
const boton = (nombre) => pagina.getByRole('button', { name: nombre });
const ir = (seccion) => pagina.getByRole('navigation').getByRole('link', { name: seccion }).click();
async function entrar({ usuario, contrasenia }) {
  await pagina.getByLabel('Usuario').fill(usuario);
  await pagina.getByLabel('Contraseña').fill(contrasenia);
  await boton('Entrar').click();
  await expect(pagina.getByRole('heading', { name: /¡Hola/ })).toBeVisible();
}

await pagina.goto('/entrar');
await foto('01-entrar');
await boton('Entrar').click();
await foto('02-campos-obligatorios');
await pagina.getByLabel('Usuario').fill('operador.matrix');
await pagina.getByLabel('Contraseña').fill('clave-equivocada');
await boton('Entrar').click();
await expect(pagina.getByText('no son correctos')).toBeVisible();
await foto('03-clave-equivocada');

// Operador
await entrar(usuarioDemo('operador.matrix'));
await foto('04-inicio-operador');
await ir('Vender');
await boton(/Carpeta/).first().click();
await boton(/Impresión a color/).click();
await boton(/Impresión a color/).click();
await foto('05-vender');
await boton(/Cobrar/).click();
await pagina.getByLabel(/Con cuánto paga/).fill('20000');
await foto('06-cobrar');
await boton('Listo, registrar venta').click();
await expect(pagina.getByRole('heading', { name: /Venta registrada/ })).toBeVisible();
await foto('07-recibo');
await boton('Nueva venta').click();
await ir('Vendido');
await foto('08-vendido-operador');
await ir('Catálogo');
await foto('09-catalogo-operador');
await boton('Salir').click();
await expect(pagina.getByText('Saliste del sistema')).toBeVisible();
await foto('10-salida');

// Administradora
await entrar(usuarioDemo('admin.matrix'));
await foto('11-inicio-admin');
await ir('Catálogo');
await foto('12-articulos-admin');
await boton(/Agregar artículo/).click();
await foto('13-elegir-tipo');
await pagina.getByRole('link', { name: /Papelería/ }).click();
await boton('Guardar').click();
await foto('14-validacion');
await pagina.getByLabel('Código').fill('PAP-MOVIL');
await pagina.getByLabel('Nombre').fill('Borrador de nata');
await pagina.getByLabel('Unidades que hay').fill('12');
await pagina.getByLabel('Precio de venta').fill('800');
await pagina.getByLabel('Precio de compra').fill('500');
await foto('15-crear');
await boton('Guardar').click();
await expect(pagina.getByText('Borrador de nata', { exact: true })).toBeVisible();
await pagina.getByRole('searchbox').fill('borrador');
await foto('16-creado');
await pagina.getByRole('link', { name: 'Editar Borrador de nata' }).click();
await expect(pagina.getByLabel('Nombre')).toHaveValue('Borrador de nata');
await pagina.getByLabel('Precio de venta').fill('900');
await foto('17-editar');
await boton('Guardar').click();
await pagina.getByRole('searchbox').fill('borrador');
await foto('18-editado');
await boton('Eliminar Borrador de nata').click();
await foto('19-confirmar-eliminar');
await boton('Sí, eliminar').click();
await expect(pagina.getByRole('link', { name: 'Editar Borrador de nata' })).toHaveCount(0);
await foto('20-eliminado');
await ir('Vender');
await pagina.getByLabel('La venta queda a nombre de').selectOption({ index: 1 });
await foto('21-vender-admin');
await ir('Vendido');
await foto('22-vendido-admin');
await boton(/Anular la venta/).first().click();
await foto('23-confirmar-anular');
await boton('Sí, anular').click();
await expect(pagina.getByText('Se anuló la venta')).toBeVisible();
await foto('24-anulada');

await navegador.close();
console.log('Recorrido completo.');
