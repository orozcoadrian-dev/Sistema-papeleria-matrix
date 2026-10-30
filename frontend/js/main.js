/* =====================================================================
   MATRIX PAPELERÍA — main.js
   -----------------------------------------------------------------
   ÍNDICE:
     1. Utilidades
     2. Menú móvil (hamburguesa)
     3. Modales (buscador general, registro, términos)
     4. Aviso de privacidad y términos — RNF-16
     5. Catálogos conectados a la API (servicios, papelería, ropa) — CU-05, CU-10, CU-40
     6. Buscador general del negocio — CU-57
     7. Año dinámico del footer
   ===================================================================== */

'use strict';

const CONFIG = window.MATRIX_CONFIG;
const RESPALDO = window.MATRIX_RESPALDO;


/* =====================================================================
   1. UTILIDADES
   ===================================================================== */

/** Pasa a minúsculas y quita tildes, para que "papeleria" encuentre "Papelería". */
function normalizar(texto) {
    return String(texto ?? '')
        .normalize('NFD')
        .replace(/[̀-ͯ]/g, '')
        .toLowerCase()
        .trim();
}

const formatoPrecio = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 });

function formatearPrecio(valor) {
    return formatoPrecio.format(valor);
}

/** Crea un elemento con su clase y su texto. El texto se escribe como texto, nunca como HTML. */
function crear(etiqueta, clase, texto) {
    const elemento = document.createElement(etiqueta);
    if (clase) {
        elemento.className = clase;
    }
    if (texto !== undefined) {
        elemento.textContent = texto;
    }
    return elemento;
}

/** Pide un JSON a la API. Se rinde si pasa el tiempo máximo o si la respuesta no es correcta. */
async function pedir(ruta) {
    const control = new AbortController();
    const temporizador = setTimeout(() => control.abort(), CONFIG.tiempoMaximoMs);

    try {
        const respuesta = await fetch(CONFIG.apiUrl + ruta, {
            headers: { Accept: 'application/json' },
            signal: control.signal
        });
        if (!respuesta.ok) {
            throw new Error('La API respondió ' + respuesta.status);
        }
        return await respuesta.json();
    } finally {
        clearTimeout(temporizador);
    }
}


/* =====================================================================
   2. MENÚ MÓVIL (HAMBURGUESA)
   En pantallas pequeñas el botón abre y cierra el <nav> con las clases
   "header__toggle--active" y "nav--open". Se cierra al elegir un enlace
   y con la tecla Escape.
   ===================================================================== */
const botonMenu = document.getElementById('nav-toggle');
const menu = document.querySelector('.nav');

function cambiarMenu(abierto) {
    botonMenu.classList.toggle('header__toggle--active', abierto);
    menu.classList.toggle('nav--open', abierto);
    botonMenu.setAttribute('aria-expanded', String(abierto));
    botonMenu.setAttribute('aria-label', abierto ? 'Cerrar menú de navegación' : 'Abrir menú de navegación');
}

if (botonMenu && menu) {
    botonMenu.addEventListener('click', () => cambiarMenu(!menu.classList.contains('nav--open')));

    menu.addEventListener('click', (evento) => {
        if (evento.target.closest('a, [data-dialog-open]')) {
            cambiarMenu(false);
        }
    });

    document.addEventListener('keydown', (evento) => {
        if (evento.key === 'Escape' && menu.classList.contains('nav--open')) {
            cambiarMenu(false);
            botonMenu.focus();
        }
    });
}


/* =====================================================================
   3. MODALES
   Los <dialog> se abren con showModal() y se cierran con close().
   Los botones llevan data-dialog-open="id-del-dialogo" para abrir y
   data-dialog-close para cerrar. Un clic en el fondo oscuro también cierra.
   ===================================================================== */
document.querySelectorAll('[data-dialog-open]').forEach((boton) => {
    boton.addEventListener('click', () => {
        const dialogo = document.getElementById(boton.dataset.dialogOpen);
        if (dialogo) {
            dialogo.showModal();
        }
    });
});

document.querySelectorAll('[data-dialog-close]').forEach((boton) => {
    boton.addEventListener('click', () => boton.closest('dialog').close());
});

document.querySelectorAll('dialog').forEach((dialogo) => {
    dialogo.addEventListener('click', (evento) => {
        if (evento.target === dialogo) {
            dialogo.close();
        }
    });
});

/* El registro de clientes todavía no existe: el formulario lo dice con claridad en vez de fingir que guardó algo. */
const formRegistro = document.querySelector('[data-form="register"]');
if (formRegistro) {
    formRegistro.addEventListener('submit', (evento) => {
        evento.preventDefault();
        document.getElementById('register-mensaje').textContent = 'El registro todavía no está disponible.';
    });
}


/* =====================================================================
   4. AVISO DE PRIVACIDAD Y TÉRMINOS (RNF-16)
   La página no usa cookies de seguimiento. Solo recuerda en el navegador
   que el visitante ya vio el aviso.
   ===================================================================== */
const CLAVE_AVISO = 'matrix-aviso-aceptado';
const aviso = document.getElementById('aviso');

function avisoAceptado() {
    try {
        return localStorage.getItem(CLAVE_AVISO) === 'si';
    } catch {
        return false;
    }
}

if (aviso && !avisoAceptado()) {
    aviso.hidden = false;
}

const botonAceptar = document.querySelector('[data-action="aceptar-aviso"]');
if (botonAceptar) {
    botonAceptar.addEventListener('click', () => {
        try {
            localStorage.setItem(CLAVE_AVISO, 'si');
        } catch {
            /* Sin almacenamiento, el aviso volverá a salir en la próxima visita. */
        }
        aviso.hidden = true;
    });
}


/* =====================================================================
   5. CATÁLOGOS CONECTADOS A LA API
   Cada catálogo (servicios, papelería, ropa) pide su lista a la API, la
   dibuja como tarjetas y la filtra con su buscador. Si la API no responde,
   papelería y ropa muestran el catálogo de respaldo; los servicios avisan
   que no pudieron cargarse, porque sus precios no tienen respaldo.
   ===================================================================== */

/* La API no guarda fotos todavía: cada código de producto tiene su imagen aquí. */
const IMAGENES = {
    'PAP-001': 'assets/img/insumos-papeleria/plumones-de-colores.png',
    'PAP-002': 'assets/img/insumos-papeleria/carpeta-blanca.png',
    'PAP-003': 'assets/img/insumos-papeleria/carpeta-de-carton.png',
    'PAP-004': 'assets/img/insumos-papeleria/carpeta-de-carton.png',
    'PAP-005': 'assets/img/insumos-papeleria/sobre-de-manila.png',
    'PAP-006': 'assets/img/insumos-papeleria/sobre-de-manila.png',
    'ROP-001': 'assets/img/pulguero-glam/Blusa-de-flores.jpeg',
    'ROP-002': 'assets/img/pulguero-glam/Blusa-de-sencilla-rosada.jpeg',
    'ROP-003': 'assets/img/pulguero-glam/Chaqueta-de-jean-ninia.jpeg',
    'ROP-004': 'assets/img/pulguero-glam/Falda-de-jean-nina.jpeg'
};

const ICONOS_SERVICIO = { 'Impresión': '🖨️', 'Copias': '📄', 'Escáner': '🖼️', 'Transcripción': '⌨️' };

/**
 * Arma una tarjeta del catálogo.
 * @param {object} datos clase, imagen, icono, categoria, titulo, texto, precio y aviso (todos opcionales menos titulo y precio).
 */
function crearTarjeta(datos) {
    const tarjeta = crear('li', datos.clase ? 'card ' + datos.clase : 'card');
    const marco = crear('div', 'card__image-wrap');

    if (datos.imagen) {
        const foto = crear('img', 'card__img');
        foto.src = datos.imagen;
        foto.alt = datos.titulo;
        foto.loading = 'lazy';
        foto.addEventListener('error', () => foto.replaceWith(crear('span', 'card__placeholder', datos.icono || '📦')));
        marco.append(foto);
    } else {
        const icono = crear('span', 'card__placeholder', datos.icono || '📦');
        icono.setAttribute('aria-hidden', 'true');
        marco.append(icono);
    }
    tarjeta.append(marco);

    if (datos.categoria) {
        tarjeta.append(crear('p', 'card__category', datos.categoria));
    }
    tarjeta.append(crear('h3', 'card__title', datos.titulo));
    if (datos.texto) {
        tarjeta.append(crear('p', 'card__text', datos.texto));
    }

    const precio = crear('p', 'card__price', formatearPrecio(datos.precio));
    if (datos.aviso) {
        precio.append(' ', crear('span', 'card__badge', datos.aviso));
    }
    tarjeta.append(precio);

    return tarjeta;
}

const catalogos = {};

/**
 * Conecta un catálogo: su lista, su buscador, su mensaje de estado y su botón de reintento.
 * Los elementos del HTML se encuentran por el prefijo: servicios-grid, servicios-status, servicios-empty...
 */
function iniciarCatalogo(opciones) {
    const rejilla = document.getElementById(opciones.id + '-grid');
    const estado = document.getElementById(opciones.id + '-status');
    const vacio = document.getElementById(opciones.id + '-empty');
    const reintento = document.querySelector('[data-reintentar="' + opciones.id + '"]');
    const formulario = document.querySelector('[data-form="buscar-' + opciones.id + '"]');
    const campo = formulario.querySelector('input');

    const catalogo = { elementos: [] };
    catalogos[opciones.id] = catalogo;
    let mensajeBase = '';

    function pintar() {
        const palabras = normalizar(campo.value).split(/\s+/).filter(Boolean);
        const visibles = catalogo.elementos.filter((elemento) => {
            const texto = normalizar(opciones.camposBusqueda(elemento).join(' '));
            return palabras.every((palabra) => texto.includes(palabra));
        });

        rejilla.replaceChildren(...visibles.map(opciones.crearTarjeta));
        vacio.hidden = visibles.length > 0 || catalogo.elementos.length === 0;

        if (palabras.length > 0) {
            estado.textContent = visibles.length === 1 ? '1 resultado.' : visibles.length + ' resultados.';
        } else {
            estado.textContent = mensajeBase;
        }
    }

    async function cargar() {
        rejilla.setAttribute('aria-busy', 'true');
        reintento.hidden = true;
        mensajeBase = 'Cargando el catálogo…';
        estado.textContent = mensajeBase;

        /* Render gratis se duerme: si tarda, se le explica la espera al visitante. */
        const avisoLento = setTimeout(() => {
            mensajeBase = 'El servidor se está despertando, puede tardar hasta un minuto…';
            estado.textContent = mensajeBase;
        }, 4000);

        try {
            catalogo.elementos = await pedir(opciones.ruta);
            mensajeBase = '';
        } catch {
            if (opciones.respaldo) {
                catalogo.elementos = opciones.respaldo;
                mensajeBase = 'No pudimos actualizar el catálogo. Te mostramos la última información guardada.';
            } else {
                catalogo.elementos = [];
                mensajeBase = 'No pudimos cargar esta información en este momento. Intenta de nuevo en unos minutos.';
            }
            reintento.hidden = false;
        } finally {
            clearTimeout(avisoLento);
            rejilla.removeAttribute('aria-busy');
        }

        pintar();
    }

    formulario.addEventListener('submit', (evento) => {
        evento.preventDefault();
        pintar();
    });
    campo.addEventListener('input', pintar);
    reintento.addEventListener('click', cargar);

    catalogo.cargado = cargar();
}

iniciarCatalogo({
    id: 'servicios',
    ruta: '/api/servicios',
    respaldo: null,
    camposBusqueda: (s) => [s.nombre, s.categoria, s.descripcion, s.codigo],
    crearTarjeta: (s) => crearTarjeta({
        clase: 'card--servicio',
        icono: ICONOS_SERVICIO[s.categoria] || '🛠️',
        categoria: s.categoria,
        titulo: s.nombre,
        texto: s.descripcion,
        precio: s.precio
    })
});

iniciarCatalogo({
    id: 'papeleria',
    ruta: '/api/productos',
    respaldo: RESPALDO.papeleria,
    camposBusqueda: (p) => [p.nombre, p.descripcion, p.codigo],
    crearTarjeta: (p) => crearTarjeta({
        imagen: IMAGENES[p.codigo],
        icono: '📚',
        titulo: p.nombre,
        precio: p.precio,
        aviso: p.disponible ? '' : 'Agotado'
    })
});

iniciarCatalogo({
    id: 'ropa',
    ruta: '/api/ropa',
    respaldo: RESPALDO.ropa,
    camposBusqueda: (r) => [r.nombre, r.tipo, r.color, r.material, 'talla ' + r.talla, r.descripcion, r.codigo],
    crearTarjeta: (r) => crearTarjeta({
        imagen: IMAGENES[r.codigo],
        icono: '👗',
        titulo: r.nombre,
        texto: 'Talla ' + r.talla + ' · ' + r.color,
        precio: r.precio
    })
});


/* =====================================================================
   6. BUSCADOR GENERAL DEL NEGOCIO (CU-57)
   Busca a la vez en horarios, contacto, servicios, papelería y ropa.
   Los horarios y el contacto se leen de la propia página: mientras la
   información del negocio no salga de la API, el HTML es su única fuente.
   ===================================================================== */
const PALABRAS_SIN_VALOR = new Set(['de', 'del', 'la', 'el', 'los', 'las', 'y', 'en', 'un', 'una', 'para', 'por', 'con']);

function construirIndice() {
    const indice = [];

    document.querySelectorAll('.quick-info__item').forEach((fila) => {
        indice.push({
            tipo: 'Horario',
            titulo: fila.querySelector('.quick-info__day').textContent,
            detalle: fila.querySelector('.quick-info__hours').textContent,
            claves: 'horario hora atencion abierto',
            destino: '#horarios'
        });
    });

    document.querySelectorAll('.footer__contact .footer__list-item').forEach((fila) => {
        indice.push({ tipo: 'Contacto', titulo: fila.textContent.trim(), detalle: '', claves: 'contacto', destino: '#contacto' });
    });

    catalogos.servicios.elementos.forEach((s) => {
        indice.push({ tipo: 'Servicio', titulo: s.nombre, detalle: formatearPrecio(s.precio), claves: [s.categoria, s.descripcion, 'precio valor costo'].join(' '), destino: '#servicios' });
    });
    catalogos.papeleria.elementos.forEach((p) => {
        indice.push({ tipo: 'Papelería', titulo: p.nombre, detalle: formatearPrecio(p.precio), claves: [p.descripcion, 'precio valor costo'].join(' '), destino: '#papeleria' });
    });
    catalogos.ropa.elementos.forEach((r) => {
        indice.push({ tipo: 'Ropa', titulo: r.nombre, detalle: formatearPrecio(r.precio) + ' · Talla ' + r.talla, claves: [r.tipo, r.color, r.material, 'precio valor costo'].join(' '), destino: '#ropa-glam' });
    });

    return indice;
}

/**
 * Busca en el índice las entradas que contengan todas las palabras escritas.
 * @param {string} textoBusqueda lo que escribió el visitante.
 * @returns {object[]} las entradas encontradas, como máximo 12.
 */
function buscarInformacionNegocio(textoBusqueda) {
    const palabras = normalizar(textoBusqueda).split(/\s+/).filter((p) => p && !PALABRAS_SIN_VALOR.has(p));
    if (palabras.length === 0) {
        return [];
    }

    return construirIndice()
        .filter((entrada) => {
            const texto = normalizar([entrada.tipo, entrada.titulo, entrada.detalle, entrada.claves].join(' '));
            return palabras.every((palabra) => texto.includes(palabra));
        })
        .slice(0, 12);
}

const formBuscarNegocio = document.querySelector('[data-form="buscar-negocio"]');
const dialogoBuscar = document.getElementById('dialog-buscar');
const resultadosNegocio = document.getElementById('resultados-negocio');

if (formBuscarNegocio) {
    formBuscarNegocio.addEventListener('submit', (evento) => {
        evento.preventDefault();
        const texto = document.getElementById('dialog-buscar-input').value;
        const encontrados = buscarInformacionNegocio(texto);

        resultadosNegocio.replaceChildren();
        if (!texto.trim()) {
            resultadosNegocio.append(crear('li', 'resultados__aviso', 'Escribe qué quieres consultar.'));
            return;
        }
        if (encontrados.length === 0) {
            resultadosNegocio.append(crear('li', 'resultados__aviso', 'No encontramos información con ese texto. Prueba con otra palabra.'));
            return;
        }

        encontrados.forEach((entrada) => {
            const enlace = crear('a', 'resultados__enlace');
            enlace.href = entrada.destino;
            enlace.append(crear('span', 'resultados__tipo', entrada.tipo), crear('strong', 'resultados__titulo', entrada.titulo));
            if (entrada.detalle) {
                enlace.append(crear('span', 'resultados__detalle', entrada.detalle));
            }
            enlace.addEventListener('click', () => dialogoBuscar.close());

            const fila = crear('li', 'resultados__item');
            fila.append(enlace);
            resultadosNegocio.append(fila);
        });
    });
}


/* =====================================================================
   7. AÑO DINÁMICO DEL FOOTER
   ===================================================================== */
const yearSpan = document.getElementById('year');
if (yearSpan) {
    yearSpan.textContent = new Date().getFullYear();
}
