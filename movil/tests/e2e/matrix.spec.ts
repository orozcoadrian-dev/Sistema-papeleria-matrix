import { expect, test, type Page } from '@playwright/test';

const SIMULACION = 'http://127.0.0.1:4020/matrix-api';

async function entrar(page: Page, usuario: string, contrasenia: string) {
  await page.goto('/entrar');
  await page.getByLabel('Usuario').fill(usuario);
  await page.getByLabel('Contraseña').fill(contrasenia);
  await page.getByRole('button', { name: 'Entrar' }).click();
  await expect(page.getByRole('heading', { name: /¡Hola/ })).toBeVisible();
}

const comoAdmin = (page: Page) => entrar(page, 'admin.prueba', 'Prueba-Admin-1');
const comoCaja = (page: Page) => entrar(page, 'caja.prueba', 'Prueba-Caja-1');
const ir = (page: Page, seccion: string) => page.getByRole('navigation').getByRole('link', { name: seccion }).click();

test.beforeEach(async ({ request }) => {
  await request.post(`${SIMULACION}/__reset`, { data: {} });
});

test('protege las pantallas, rechaza una clave mala y vuelve a donde se quería ir', async ({ page }) => {
  await page.goto('/ventas');
  await expect(page).toHaveURL(/\/entrar\?volver=/);
  await page.getByLabel('Usuario').fill('caja.prueba');
  await page.getByLabel('Contraseña').fill('mala');
  await page.getByRole('button', { name: 'Entrar' }).click();
  await expect(page.getByText('no son correctos')).toBeVisible();
  await page.getByLabel('Contraseña').fill('Prueba-Caja-1');
  await page.getByRole('button', { name: 'Entrar' }).click();
  await expect(page.getByRole('heading', { name: 'Lo vendido' })).toBeVisible();
});

test('el operador cobra con cambio y solo consulta el catálogo', async ({ page }) => {
  await comoCaja(page);
  await ir(page, 'Vender');
  await page.getByRole('button', { name: /Carpeta blanca/ }).click();
  await page.getByRole('button', { name: /Carpeta blanca/ }).click();
  await page.getByRole('button', { name: /Cobrar/ }).click();
  await page.getByLabel(/Con cuánto paga/).fill('1000');
  await expect(page.getByText(/Hay que devolver/)).toContainText('200');
  await page.getByRole('button', { name: 'Listo, registrar venta' }).click();
  await expect(page.getByRole('heading', { name: /Venta registrada/ })).toBeVisible();
  await page.getByRole('button', { name: 'Nueva venta' }).click();
  await expect(page.getByRole('button', { name: /Carpeta blanca/ })).toContainText('Queda 1');

  await ir(page, 'Vendido');
  await expect(page.getByText('2 × Carpeta blanca')).toBeVisible();
  await expect(page.getByRole('button', { name: /Anular/ })).toHaveCount(0);

  await ir(page, 'Catálogo');
  await expect(page.getByText('Carpeta blanca')).toBeVisible();
  await expect(page.getByRole('button', { name: /Agregar artículo|Eliminar/ })).toHaveCount(0);
  await page.goto('/articulos/servicio/nuevo');
  await expect(page).toHaveURL(/\/catalogo$/);
});

test('la administradora crea, edita y elimina un artículo', async ({ page }) => {
  await comoAdmin(page);
  await ir(page, 'Catálogo');
  await page.getByRole('button', { name: /Agregar artículo/ }).click();
  await page.getByRole('link', { name: /Servicios/ }).click();
  await page.getByRole('button', { name: 'Guardar' }).click();
  await expect(page.getByText('Falta: código.')).toBeVisible();
  await page.getByLabel('Código').fill('SER-002');
  await page.getByLabel('Nombre').fill('Fotocopia');
  await page.getByLabel('Categoría').fill('Copias');
  await page.getByLabel('Precio').fill('200');
  await page.getByRole('button', { name: 'Guardar' }).click();
  await expect(page.getByText('Fotocopia', { exact: true })).toBeVisible();

  await page.getByRole('link', { name: 'Editar Fotocopia' }).click();
  await expect(page.getByLabel('Nombre')).toHaveValue('Fotocopia');
  await page.getByLabel('Precio').fill('300');
  await page.getByRole('button', { name: 'Guardar' }).click();
  await expect(page.getByRole('listitem').filter({ hasText: 'Fotocopia' })).toContainText('300');

  await page.getByRole('button', { name: 'Eliminar Fotocopia' }).click();
  await page.getByRole('button', { name: 'Sí, eliminar' }).click();
  await expect(page.getByRole('link', { name: 'Editar Fotocopia' })).toHaveCount(0);
});

test('la administradora vende a nombre de un operador y anula la venta', async ({ page }) => {
  await comoAdmin(page);
  await ir(page, 'Vender');
  await expect(page.getByLabel('La venta queda a nombre de')).toContainText('Daniel Prueba');
  await page.getByRole('button', { name: /Blusa rosada/ }).click();
  await page.getByRole('button', { name: /Cobrar/ }).click();
  await page.getByRole('button', { name: 'Listo, registrar venta' }).click();
  await page.getByRole('button', { name: 'Nueva venta' }).click();
  await expect(page.getByRole('button', { name: /Blusa rosada/ })).toBeDisabled();

  await ir(page, 'Vendido');
  await page.getByRole('button', { name: /Anular la venta/ }).click();
  await page.getByRole('button', { name: 'Sí, anular' }).click();
  await expect(page.getByText('No hay ventas en estos días')).toBeVisible();
});

test('un 403 conserva la sesión y un 401 la cierra', async ({ page, request }) => {
  await comoAdmin(page);
  await request.post(`${SIMULACION}/__falla`, { data: { estado: 403 } });
  await ir(page, 'Vendido');
  await expect(page.getByText('no tiene permiso')).toBeVisible();
  await request.post(`${SIMULACION}/__falla`, { data: { estado: 401 } });
  await page.getByRole('button', { name: 'Intentar de nuevo' }).click();
  await expect(page).toHaveURL(/motivo=inactividad/);
  await expect(page.getByText('Tu sesión se cerró')).toBeVisible();
});

test('la sesión sigue al recargar y salir la borra', async ({ page }) => {
  await comoCaja(page);
  await page.reload();
  await expect(page.getByRole('heading', { name: /¡Hola/ })).toBeVisible();
  // Se visita otra pantalla para que «Atrás» tenga a dónde volver dentro de la aplicación.
  await ir(page, 'Vendido');
  await page.getByRole('button', { name: 'Salir' }).click();
  await expect(page.getByText('Saliste del sistema')).toBeVisible();
  await page.goBack();
  await expect(page).toHaveURL(/\/entrar/);
});

test('ninguna pantalla se sale del ancho del teléfono', async ({ page }) => {
  await comoAdmin(page);
  for (const seccion of ['Inicio', 'Vender', 'Vendido', 'Catálogo']) {
    await ir(page, seccion);
    await page.waitForLoadState('networkidle');
    const sobra = await page.evaluate(() => document.documentElement.scrollWidth - window.innerWidth);
    expect(sobra, seccion).toBeLessThanOrEqual(0);
  }
});
