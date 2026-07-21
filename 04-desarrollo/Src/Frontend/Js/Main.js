/* =====================================================================
   MATRIX PAPELERÍA — main.js
   -----------------------------------------------------------------
   Esto es un ESQUELETO, no la lógica terminada. La idea es que tú
   vayas llenando cada función con tu propio código a medida que
   aprendes. Dejé listos:
     - las referencias a los elementos del HTML (document.querySelector)
     - dónde debe ir cada evento (addEventListener)
     - comentarios TODO explicando qué debería pasar en cada función

   ÍNDICE:
     1. Menú móvil (hamburguesa)
     2. Modales (buscador general, login, registro)
     3. Buscadores de catálogo (papelería y ropa) — CU-57/59/61
     4. Utilidades varias (año dinámico en el footer, etc.)
   ===================================================================== */

// "use strict" ayuda a detectar errores comunes de JS más temprano.
'use strict';


/* =====================================================================
   1. MENÚ MÓVIL (HAMBURGUESA)
   El botón con id="nav-toggle" debe abrir/cerrar el <nav> en pantallas
   pequeñas, agregando/quitando las clases:
     - "header__toggle--active" al botón (para que se vea como una X)
     - "nav--open" al <nav> (para que se deslice a la vista, ver CSS)
   ===================================================================== */
const navToggle = document.getElementById('nav-toggle');
const navList = document.getElementById('nav-list'); // El <ul> con los links
const nav = document.querySelector('.nav');

if (navToggle && nav) {
    navToggle.addEventListener('click', () => {
        // TODO: alternar (toggle) las clases "header__toggle--active" en
        // navToggle y "nav--open" en nav.
        // Pista: revisa el método .classList.toggle()
        // TODO: también sería buena práctica actualizar el atributo
        // aria-expanded de navToggle (true/false) para accesibilidad.
    });
}


/* =====================================================================
   2. MODALES (buscador general, login, registro)
   Los tres <dialog> del HTML se controlan con los métodos nativos:
     dialogElement.showModal()  -> abre el modal
     dialogElement.close()      -> lo cierra
   Cada botón que abre o cierra un modal ya tiene un atributo
   data-action en el HTML (ej: data-action="open-login").
   ===================================================================== */

// Referencias a los 3 <dialog>
const dialogBuscar = document.getElementById('dialog-buscar');
const dialogLogin = document.getElementById('dialog-login');
const dialogRegister = document.getElementById('dialog-register');

// Botones que abren cada modal (usan [data-action="..."] como selector)
const btnOpenSearch = document.querySelector('[data-action="open-search"]');
const btnOpenLogin = document.querySelector('[data-action="open-login"]');
const btnOpenRegister = document.querySelector('[data-action="open-register"]');

// Botones que cierran cada modal
const btnCloseSearch = document.querySelector('[data-action="close-search"]');
const btnCloseLogin = document.querySelector('[data-action="close-login"]');
const btnCloseRegister = document.querySelector('[data-action="close-register"]');

// TODO: agrega un addEventListener('click', ...) a cada botón "open"
// que llame a dialogElement.showModal() sobre el dialog correspondiente.
//
// Ejemplo de la idea (bórralo y hazlo tú mismo con los 3 modales):
// btnOpenLogin.addEventListener('click', () => {
//   dialogLogin.showModal();
// });

// TODO: agrega un addEventListener('click', ...) a cada botón "close"
// que llame a dialogElement.close().


/* =====================================================================
3. BUSCADORES DE CATÁLOGO
Hay 3 formularios de búsqueda en el HTML, identificados con
[data-form="..."]:
    - "buscar-negocio"   (dentro del dialog, CU-57)
    - "buscar-papeleria" (sección #papeleria, CU-61)
    - "buscar-ropa"      (sección #ropa-glam, CU-59)

Los 3 disparan el evento "submit". Recuerda usar
event.preventDefault() para que la página no se recargue.
   ===================================================================== */

const formBuscarNegocio = document.querySelector('[data-form="buscar-negocio"]');
const formBuscarPapeleria = document.querySelector('[data-form="buscar-papeleria"]');
const formBuscarRopa = document.querySelector('[data-form="buscar-ropa"]');

// Contenedores donde ya existen las tarjetas de producto (por si luego
// quieres mostrar/ocultar tarjetas según el texto buscado)
const papeleriaGrid = document.getElementById('papeleria-grid');
const papeleriaEmptyState = document.getElementById('papeleria-empty');

const ropaGrid = document.getElementById('ropa-grid');
const ropaEmptyState = document.getElementById('ropa-empty');


/**
 * CU-61 — Consultar información de papelería.
 * TODO: 1) obtener el texto que el cliente escribió en el input
 *          (el input tiene id="buscar-papeleria").
 *       2) recorrer las tarjetas dentro de papeleriaGrid
 *          (cada una es un <li class="card">) y comparar su título
 *          (.card__title) con el texto buscado.
 *       3) mostrar solo las tarjetas que coincidan (puedes usar
 *          estilo.display = "none" para ocultar las demás).
 *       4) si ninguna coincide, mostrar papeleriaEmptyState quitando
 *          su atributo "hidden".
 */
function buscarPapeleria(textoBusqueda) {
    // Tu código aquí.
}

/**
 * CU-59 — Consultar información de ropa.
 * Misma idea que buscarPapeleria(), pero sobre ropaGrid.
 */
function buscarRopa(textoBusqueda) {
    // Tu código aquí.
}

/**
 * CU-57 — Consultar información del negocio.
 * Esta búsqueda es más general (horarios, precios, requisitos, etc.),
 * así que probablemente no tengas "tarjetas" que filtrar, sino que
 * más adelante consultarás información desde un backend o un arreglo
 * de datos del negocio. Por ahora, deja la función lista para usar.
 */
function buscarInformacionNegocio(textoBusqueda) {
    // Tu código aquí.
}

if (formBuscarPapeleria) {
    formBuscarPapeleria.addEventListener('submit', (event) => {
        event.preventDefault();
        // TODO: llama a buscarPapeleria() pasándole el valor del input.
    });
}

if (formBuscarRopa) {
    formBuscarRopa.addEventListener('submit', (event) => {
        event.preventDefault();
        // TODO: llama a buscarRopa() pasándole el valor del input.
    });
}

if (formBuscarNegocio) {
    formBuscarNegocio.addEventListener('submit', (event) => {
        event.preventDefault();
        // TODO: llama a buscarInformacionNegocio() pasándole el valor del input.
    });
}


/* =====================================================================
4. UTILIDADES VARIAS
   ===================================================================== */

// Este ya viene resuelto como ejemplo sencillo: pone el año actual
// en el <span id="year"> del footer, para que nunca quede desactualizado.
const yearSpan = document.getElementById('year');
if (yearSpan) {
    yearSpan.textContent = new Date().getFullYear();
}