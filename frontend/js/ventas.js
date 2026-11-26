'use strict';

/* Pantalla de ventas. El operador del cibercafé toca lo que se lleva el cliente, revisa el total y cobra.
   La administradora también entra aquí: puede vender a nombre de un operador y ver todas las ventas.
   Una venta registrada se puede cambiar o anular; el operador necesita para eso su clave de permiso.
   Su registro se puede descargar en PDF o enviar por correo. En «Existencias» se anota lo que hay.
   Las existencias las cuida el servidor: si no alcanzan, la venta no se hace y se dice cuántas quedan.
   Los datos se insertan con textContent, nunca con innerHTML. */

const SESION = Sesion.exigir(['ADMINISTRADOR', 'OPERADOR_DE_CIBERCAFE']);
const ES_ADMIN = SESION?.rol === 'ADMINISTRADOR';
const PESTANA_ALMACEN = 'matrix.ventas.pestana';

const pesos = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 });
const miles = new Intl.NumberFormat('es-CO', { maximumFractionDigits: 0 });
const $ = (id) => document.getElementById(id);

// Tope por artículo que acepta el servidor.
const CANTIDAD_MAXIMA = 150;
const POCAS_UNIDADES = 5;

const TIPOS = {
    servicio: { icono: '🖨️', titulo: 'Servicios', ruta: '/api/servicios', detalle: (s) => s.categoria },
    producto: { icono: '✏️', titulo: 'Papelería', ruta: '/api/productos/interno', detalle: () => '' },
    ropa: { icono: '👗', titulo: 'Ropa', ruta: '/api/ropa', detalle: (r) => [r.tipo, r.talla && `Talla ${r.talla}`, r.color].filter(Boolean).join(' · ') }
};

// Cada tipo de la caja y la carpeta de sus fotos en el servidor.
const CARPETA_DE_FOTOS = { servicio: 'servicios', producto: 'productos', ropa: 'ropa' };

const PERIODOS = {
    hoy: { titulo: 'Hoy', dias: 0 },
    semana: { titulo: 'Últimos 7 días', dias: 6 },
    mes: { titulo: 'Últimos 30 días', dias: 29 },
    todo: { titulo: 'Todo', dias: null }
};
const PRIMERA_FECHA = '2020-01-01';

const CLASES = {
    todas: { titulo: 'Todas', pasa: () => true },
    servicios: { titulo: 'Con servicios', pasa: (v) => v.articulos.some((a) => a.tipo === 'servicio') },
    anuladas: { titulo: 'Anuladas', pasa: (v) => v.anulada }
};

const PESTANAS = ['vender', 'vendido', 'existencias'];

/* ---------- Estado ---------- */

let catalogo = [];          // todos los artículos: { tipo, id, nombre, precio, detalle, cantidad? }
let catalogoCargado = false;
let filtro = 'todo';
const carrito = new Map();   // clave "tipo:id" → { articulo, cantidad }
let periodo = 'hoy';
let ventas = undefined;      // undefined = cargando, null = sin conexión
let porAnular = null;
let operadores = [];
let editando = null;         // la venta registrada que se está cambiando
let clase = 'todas';         // qué ventas se muestran en «Lo vendido»
const clientes = new Map();  // id de la venta → datos temporales del cliente; se pierden al salir
let ventaDelRecibo = null;
let ventaDelCorreo = null;
let productoContado = null;  // producto al que se le anotan las existencias

/* ---------- Utilidades ---------- */

function normalizar(texto) {
    return String(texto ?? '').normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase().trim();
}

function leerEntero(texto) {
    const limpio = String(texto).replace(/[$\s]/g, '');
    if (/^\d+$/.test(limpio)) return Number(limpio);
    if (/^\d{1,3}(\.\d{3})+$/.test(limpio) || /^\d{1,3}(,\d{3})+$/.test(limpio)) return Number(limpio.replace(/[.,]/g, ''));
    return NaN;
}

function avisar(texto, tipo = 'bien') {
    const aviso = $('aviso-flotante');
    aviso.textContent = texto;
    aviso.className = 'aviso-flotante aviso-flotante--visible' + (tipo === 'error' ? ' aviso-flotante--error' : '');
    clearTimeout(avisar.temporizador);
    avisar.temporizador = setTimeout(() => { aviso.className = 'aviso-flotante'; }, tipo === 'error' ? 7000 : 3800);
}

function mensajeGeneral(error) {
    if (error.estado === 0) return 'No pudimos conectar con el sistema. Revisa el internet e intenta otra vez.';
    if (error.estado === 403) return 'Tu usuario no tiene permiso para hacer esto.';
    return 'Algo salió mal. Intenta otra vez en un momento; si sigue pasando, avísale a Osvaldo.';
}

function hoyISO(restarDias = 0) {
    const fecha = new Date();
    fecha.setDate(fecha.getDate() - restarDias);
    const mes = String(fecha.getMonth() + 1).padStart(2, '0');
    const dia = String(fecha.getDate()).padStart(2, '0');
    return `${fecha.getFullYear()}-${mes}-${dia}`;
}

function fechaLegible(iso) {
    if (iso === hoyISO()) return 'Hoy';
    if (iso === hoyISO(1)) return 'Ayer';
    const [anio, mes, dia] = iso.split('-').map(Number);
    return new Date(anio, mes - 1, dia).toLocaleDateString('es-CO', { weekday: 'long', day: 'numeric', month: 'long' });
}

function clave(tipo, id) {
    return `${tipo}:${id}`;
}

function mostrarEstado(prefijo, { icono, titulo, texto, boton }) {
    $(`${prefijo}estado`).hidden = false;
    $(`${prefijo}estado-icono`).textContent = icono;
    $(`${prefijo}estado-titulo`).textContent = titulo;
    $(`${prefijo}estado-texto`).textContent = texto;
    const accion = $(`${prefijo}estado-boton`);
    accion.hidden = !boton;
    if (boton) {
        accion.textContent = boton.texto;
        accion.onclick = boton.accion;
    }
}

/* ---------- Pestañas ---------- */

function elegirPestana(cual) {
    for (const nombre of PESTANAS) {
        const elegida = nombre === cual;
        $(`pestana-${nombre}`).setAttribute('aria-selected', String(elegida));
        $(`pestana-${nombre}`).tabIndex = elegida ? 0 : -1;
        $(`vista-${nombre}`).hidden = !elegida;
    }
    $('saludo-sub').textContent = {
        vender: `Toca lo que se lleva el cliente y después «${editando ? 'Guardar cambios' : 'Cobrar'}».`,
        vendido: 'Revisa cuánto se ha vendido.',
        existencias: 'Anota cuántas unidades hay en la tienda.'
    }[cual];
    try { sessionStorage.setItem(PESTANA_ALMACEN, cual); } catch { /* solo es una comodidad */ }
    $('barra-cobro').hidden = cual !== 'vender' || carrito.size === 0;
    if (cual === 'vendido' && ventas === undefined) cargarVentas();
    if (cual === 'existencias') dibujarExistencias();
}

PESTANAS.forEach((nombre, posicion) => {
    const pestana = $(`pestana-${nombre}`);
    pestana.addEventListener('click', () => elegirPestana(nombre));
    pestana.addEventListener('keydown', (evento) => {
        const paso = evento.key === 'ArrowRight' ? 1 : evento.key === 'ArrowLeft' ? -1 : 0;
        if (!paso) return;
        const otra = $(`pestana-${PESTANAS[(posicion + paso + PESTANAS.length) % PESTANAS.length]}`);
        otra.click();
        otra.focus();
    });
});

/* ---------- Catálogo ---------- */

async function cargarCatalogo() {
    catalogoCargado = false;
    dibujarCatalogo();
    const despertando = setTimeout(() => {
        if (!catalogoCargado) $('contador').textContent = 'Estamos despertando el sistema. La primera vez del día puede tardar hasta un minuto ☕';
    }, 4000);
    try {
        const fotosListas = Fotos.cargar();
        const listas = await Promise.all(Object.entries(TIPOS).map(async ([tipo, info]) => {
            const registros = await Sesion.llamar(info.ruta);
            return registros.map((r) => ({
                // Lo que hay en la tienda según el servidor; «cantidad» suma lo de la venta que se esté cambiando.
                enTienda: tipo === 'producto' ? r.cantidad : tipo === 'ropa' ? (r.disponible === false ? 0 : 1) : null,
                tipo,
                id: r.id,
                nombre: r.nombre || r.tipo || 'Sin nombre',
                precio: r.precio,
                detalle: info.detalle(r),
                // La papelería tiene existencias; cada prenda es única (1 si está disponible, 0 si ya se vendió).
                cantidad: tipo === 'producto' ? r.cantidad : tipo === 'ropa' ? (r.disponible === false ? 0 : 1) : null
            }));
        }));
        catalogo = listas.flat();
        catalogoCargado = true;
        ajustarExistencias();
        await fotosListas;
        // Lo que ya estaba en la venta se cruza con las existencias nuevas; lo que se borró del catálogo sale.
        for (const [llave, linea] of carrito) {
            const nuevo = catalogo.find((a) => clave(a.tipo, a.id) === llave);
            if (nuevo) linea.articulo = nuevo;
            else carrito.delete(llave);
        }
    } catch (error) {
        catalogo = null;
        catalogoCargado = true;
    } finally {
        clearTimeout(despertando);
    }
    dibujarFiltros();
    dibujarCatalogo();
    dibujarCarrito();
    dibujarExistencias();
}

function dibujarFiltros() {
    const opciones = { todo: { icono: '✨', titulo: 'Todo' }, ...TIPOS };
    const contenedor = $('filtros');
    contenedor.replaceChildren();
    for (const [valor, info] of Object.entries(opciones)) {
        const boton = document.createElement('button');
        boton.type = 'button';
        boton.className = 'filtro';
        boton.setAttribute('aria-pressed', String(valor === filtro));
        boton.textContent = `${info.icono} ${info.titulo}`;
        boton.addEventListener('click', () => {
            filtro = valor;
            dibujarFiltros();
            dibujarCatalogo();
        });
        contenedor.append(boton);
    }
}

function coincide(articulo, palabras) {
    const propias = normalizar(`${articulo.nombre} ${articulo.detalle}`).split(/[\s,.;:()/·-]+/);
    return palabras.every((palabra) => propias.some((propia) => propia.startsWith(palabra)));
}

function dibujarCatalogo() {
    const contenedor = $('articulos');
    const contador = $('contador');
    contenedor.replaceChildren();
    $('estado').hidden = true;

    if (!catalogoCargado) {
        contenedor.setAttribute('aria-busy', 'true');
        contador.textContent = 'Cargando…';
        for (let i = 0; i < 6; i++) {
            const esqueleto = document.createElement('div');
            esqueleto.className = 'articulo articulo--cargando';
            contenedor.append(esqueleto);
        }
        return;
    }
    contenedor.setAttribute('aria-busy', 'false');

    if (catalogo === null) {
        contador.textContent = '';
        mostrarEstado('', {
            icono: '📡',
            titulo: 'No pudimos conectar con el sistema',
            texto: 'Revisa que haya internet. Si sigue pasando, avísale a Osvaldo.',
            boton: { texto: 'Intentar de nuevo', accion: cargarCatalogo }
        });
        return;
    }

    const palabras = normalizar($('buscar').value).split(/\s+/).filter(Boolean);
    const visibles = catalogo.filter((a) => (filtro === 'todo' || a.tipo === filtro) && (!palabras.length || coincide(a, palabras)));

    if (visibles.length === 0) {
        contador.textContent = '';
        mostrarEstado('', palabras.length
            ? { icono: '🔍', titulo: 'No encontramos nada con esa búsqueda', texto: 'Revisa cómo está escrito o borra la búsqueda.',
                boton: { texto: 'Borrar búsqueda', accion: () => { $('buscar').value = ''; dibujarCatalogo(); $('buscar').focus(); } } }
            : { icono: '📦', titulo: 'No hay nada aquí todavía', texto: 'La administradora puede agregarlo desde «Mi negocio».' });
        return;
    }

    contador.textContent = palabras.length ? `Encontramos ${visibles.length}` : '';
    for (const articulo of visibles) contenedor.append(crearArticulo(articulo));
}

function disponibles(articulo) {
    if (articulo.cantidad === null) return Infinity;
    return articulo.cantidad - (carrito.get(clave(articulo.tipo, articulo.id))?.cantidad || 0);
}

function crearArticulo(articulo) {
    const boton = document.createElement('button');
    boton.type = 'button';
    boton.className = 'articulo';
    boton.dataset.clave = clave(articulo.tipo, articulo.id);
    const quedan = disponibles(articulo);
    const enCarrito = carrito.get(clave(articulo.tipo, articulo.id))?.cantidad || 0;
    const agotado = articulo.cantidad !== null && articulo.cantidad <= 0;
    if (enCarrito) boton.classList.add('articulo--elegido');
    if (agotado) {
        boton.disabled = true;
        boton.classList.add('articulo--agotado');
    }

    // Con foto, se ve la foto; si no, el dibujo del tipo.
    const foto = Fotos.url(CARPETA_DE_FOTOS[articulo.tipo], articulo.id);
    let icono;
    if (foto) {
        icono = document.createElement('img');
        icono.className = 'articulo__foto';
        icono.src = foto;
        icono.alt = '';
        icono.loading = 'lazy';
    } else {
        icono = document.createElement('span');
        icono.className = 'articulo__icono';
        icono.setAttribute('aria-hidden', 'true');
        icono.textContent = TIPOS[articulo.tipo].icono;
    }

    const nombre = document.createElement('span');
    nombre.className = 'articulo__nombre';
    nombre.textContent = articulo.nombre;

    const precio = document.createElement('span');
    precio.className = 'articulo__precio';
    precio.textContent = pesos.format(articulo.precio);

    boton.append(icono, nombre, precio);

    if (articulo.detalle) {
        const detalle = document.createElement('span');
        detalle.className = 'articulo__detalle';
        detalle.textContent = articulo.detalle;
        boton.append(detalle);
    }

    if (articulo.cantidad !== null) {
        const existencias = document.createElement('span');
        existencias.className = 'articulo__existencias';
        if (articulo.tipo === 'ropa') existencias.textContent = agotado ? 'Vendida' : quedan <= 0 ? 'Ya está en la venta' : 'Pieza única';
        else if (agotado) existencias.textContent = 'Agotado';
        else if (quedan <= 0) existencias.textContent = 'Ya agregaste todas';
        else existencias.textContent = quedan === 1 ? 'Queda 1' : `Quedan ${miles.format(quedan)}`;
        if (agotado || quedan < POCAS_UNIDADES) existencias.classList.add('articulo__existencias--pocas');
        boton.append(existencias);
    }

    if (enCarrito) {
        const insignia = document.createElement('span');
        insignia.className = 'articulo__insignia';
        insignia.textContent = `×${enCarrito}`;
        boton.append(insignia);
    }

    const textoAccesible = [`Agregar ${articulo.nombre}`, pesos.format(articulo.precio)];
    if (enCarrito) textoAccesible.push(`ya van ${enCarrito}`);
    if (agotado) textoAccesible.push('agotado');
    boton.setAttribute('aria-label', textoAccesible.join(', '));
    boton.addEventListener('click', () => agregar(articulo));
    return boton;
}

/* ---------- Carrito ---------- */

function agregar(articulo, cuantos = 1) {
    const llave = clave(articulo.tipo, articulo.id);
    const actual = carrito.get(llave)?.cantidad || 0;
    const nueva = actual + cuantos;
    if (nueva <= 0) {
        carrito.delete(llave);
    } else if (articulo.tipo === 'ropa' && nueva > articulo.cantidad) {
        avisar(articulo.cantidad === 0 ? `«${articulo.nombre}» ya se vendió.` : `Cada prenda es única: «${articulo.nombre}» ya está en la venta.`, 'error');
        return;
    } else if (articulo.cantidad !== null && nueva > articulo.cantidad) {
        avisar(articulo.cantidad === 1
            ? `Solo queda 1 unidad de «${articulo.nombre}».`
            : `Solo quedan ${articulo.cantidad} unidades de «${articulo.nombre}».`, 'error');
        return;
    } else if (nueva > CANTIDAD_MAXIMA) {
        avisar(`No se pueden vender más de ${CANTIDAD_MAXIMA} de lo mismo en una venta.`, 'error');
        return;
    } else {
        carrito.set(llave, { articulo, cantidad: nueva });
    }
    // Al volver a dibujar, el foco se queda en el mismo artículo para seguir con el teclado.
    const enfocado = document.activeElement?.dataset?.clave;
    dibujarCarrito();
    dibujarCatalogo();
    if (enfocado) $('articulos').querySelector(`[data-clave="${enfocado}"]`)?.focus();
}

function totalDelCarrito() {
    let total = 0;
    for (const { articulo, cantidad } of carrito.values()) total += articulo.precio * cantidad;
    return total;
}

function dibujarCarrito() {
    const lista = $('carrito-lineas');
    lista.replaceChildren();
    for (const { articulo, cantidad } of carrito.values()) {
        const linea = document.createElement('li');
        linea.className = 'linea';

        const textos = document.createElement('div');
        textos.className = 'linea__textos';
        const nombre = document.createElement('span');
        nombre.className = 'linea__nombre';
        nombre.textContent = articulo.nombre;
        const precio = document.createElement('span');
        precio.className = 'linea__precio';
        precio.textContent = `${pesos.format(articulo.precio)} c/u`;
        textos.append(nombre, precio);

        const cantidades = document.createElement('div');
        cantidades.className = 'linea__cantidad';
        const menos = document.createElement('button');
        menos.type = 'button';
        menos.className = 'linea__boton';
        menos.textContent = cantidad === 1 ? '🗑️' : '−';
        menos.setAttribute('aria-label', cantidad === 1 ? `Quitar ${articulo.nombre}` : `Uno menos de ${articulo.nombre}`);
        menos.addEventListener('click', () => agregar(articulo, -1));
        const numero = document.createElement('span');
        numero.className = 'linea__numero';
        numero.textContent = cantidad;
        numero.setAttribute('aria-label', `${cantidad} de ${articulo.nombre}`);
        const mas = document.createElement('button');
        mas.type = 'button';
        mas.className = 'linea__boton';
        mas.textContent = '+';
        mas.setAttribute('aria-label', `Uno más de ${articulo.nombre}`);
        mas.disabled = disponibles(articulo) <= 0;
        mas.addEventListener('click', () => agregar(articulo, 1));
        cantidades.append(menos, numero, mas);

        const subtotal = document.createElement('span');
        subtotal.className = 'linea__subtotal';
        subtotal.textContent = pesos.format(articulo.precio * cantidad);

        linea.append(textos, cantidades, subtotal);
        lista.append(linea);
    }

    const vacio = carrito.size === 0;
    $('carrito-vacio').hidden = !vacio;
    $('carrito-vacio').textContent = editando
        ? 'No queda nada. Si el cliente no se lleva nada, anula la venta en «Lo vendido».'
        : 'Todavía no hay nada. Toca un producto o servicio para agregarlo.';
    $('boton-vaciar').hidden = vacio || Boolean(editando);
    $('boton-cobrar').disabled = vacio;
    const total = totalDelCarrito();
    $('carrito-total').textContent = pesos.format(total);
    const accion = editando ? 'Guardar cambios' : 'Cobrar';
    $('boton-cobrar').textContent = vacio ? accion : `${accion} ${pesos.format(total)}`;

    const unidades = [...carrito.values()].reduce((suma, linea) => suma + linea.cantidad, 0);
    $('barra-cobro').hidden = vacio || $('vista-vender').hidden;
    $('barra-cobro-cantidad').textContent = unidades === 1 ? '1 artículo · ver' : `${unidades} artículos · ver`;
    $('barra-cobro-total').textContent = pesos.format(total);
}

$('barra-cobro-boton').addEventListener('click', abrirCobro);
$('barra-cobro-ver').addEventListener('click', () => {
    $('carrito-titulo').scrollIntoView({ behavior: 'smooth', block: 'start' });
});

$('boton-vaciar').addEventListener('click', () => {
    if (carrito.size > 1 && !window.confirm('¿Quitar todo lo que se iba a llevar?')) return;
    carrito.clear();
    dibujarCarrito();
    dibujarCatalogo();
});

/* ---------- Cobrar ---------- */

function abrirCobro() {
    if (carrito.size === 0) return;
    if (!editando && ES_ADMIN && !$('operador').value) {
        avisar('Elige arriba a nombre de qué operador queda la venta.', 'error');
        $('operador').focus();
        return;
    }
    const total = totalDelCarrito();
    const unidades = [...carrito.values()].reduce((suma, linea) => suma + linea.cantidad, 0);
    const cambiando = Boolean(editando);
    $('cobrar-icono').textContent = cambiando ? '✏️' : '💵';
    $('cobrar-verbo').textContent = cambiando ? 'Queda en' : 'Cobrar';
    $('cobrar-total').textContent = pesos.format(total);
    $('cobrar-detalle').textContent = cambiando ? diferencia(editando.total, total) : unidades === 1 ? '1 artículo.' : `${unidades} artículos.`;
    $('caja-pago').hidden = cambiando;
    $('caja-permiso').hidden = !cambiando || ES_ADMIN;
    $('clave-permiso').value = '';
    $('boton-confirmar-cobro').textContent = cambiando ? 'Guardar cambios' : 'Listo, registrar venta';
    $('pago').value = '';
    $('cambio').textContent = '';
    $('cobrar-error').textContent = '';
    $('caja-cliente').hidden = cambiando;
    $('caja-cliente').open = false;
    for (const campo of ['cliente-nombre', 'cliente-celular', 'cliente-correo']) $(campo).value = '';
    dibujarPagosRapidos(total);
    $('ventana-cobrar').showModal();
    (!cambiando ? $('pago') : ES_ADMIN ? $('boton-confirmar-cobro') : $('clave-permiso')).focus();
}

/* Billetes comunes para no tener que escribir: el total exacto y los billetes que alcanzan a pagarlo. */
function dibujarPagosRapidos(total) {
    const billetes = [2000, 5000, 10000, 20000, 50000, 100000];
    const opciones = [total, ...billetes.filter((b) => b > total)].slice(0, 4);
    const contenedor = $('pagos-rapidos');
    contenedor.replaceChildren();
    for (const valor of opciones) {
        const boton = document.createElement('button');
        boton.type = 'button';
        boton.className = 'filtro';
        boton.textContent = valor === total ? 'Exacto' : pesos.format(valor);
        boton.addEventListener('click', () => {
            $('pago').value = miles.format(valor);
            calcularCambio();
        });
        contenedor.append(boton);
    }
}

function calcularCambio() {
    const texto = $('pago').value.trim();
    const cambio = $('cambio');
    cambio.className = 'cambio';
    if (!texto) {
        cambio.textContent = '';
        return;
    }
    const pago = leerEntero(texto);
    const total = totalDelCarrito();
    if (!Number.isFinite(pago)) {
        cambio.textContent = 'Escribe solo el número, por ejemplo 20.000';
        cambio.classList.add('cambio--mal');
    } else if (pago < total) {
        cambio.textContent = `Faltan ${pesos.format(total - pago)}`;
        cambio.classList.add('cambio--mal');
    } else {
        cambio.textContent = pago === total ? 'Pago exacto, sin cambio.' : `Hay que devolver ${pesos.format(pago - total)}`;
        cambio.classList.add('cambio--bien');
    }
}

$('pago').addEventListener('input', calcularCambio);
$('pago').addEventListener('blur', () => {
    const numero = leerEntero($('pago').value);
    if (Number.isFinite(numero)) $('pago').value = miles.format(numero);
});
$('boton-cobrar').addEventListener('click', abrirCobro);

/* Lo que se escribió en la caja, como lo pide el servidor. */
function cuerpoDeLaVenta() {
    return {
        articulos: [...carrito.values()].map(({ articulo, cantidad }) => ({ tipo: articulo.tipo, id: articulo.id, cantidad })),
        // Lo que se le dijo al cliente: si un precio cambió mientras tanto, el servidor no registra otro total.
        totalEsperado: totalDelCarrito()
    };
}

$('formulario-cobrar').addEventListener('submit', async (evento) => {
    evento.preventDefault();
    if (editando) {
        guardarCambios();
        return;
    }
    const pagoTexto = $('pago').value.trim();
    const pago = pagoTexto ? leerEntero(pagoTexto) : NaN;
    if (pagoTexto && Number.isFinite(pago) && pago < totalDelCarrito()) {
        $('cobrar-error').textContent = 'Lo que paga no alcanza. Revisa el valor o déjalo vacío.';
        $('pago').focus();
        return;
    }

    const cliente = leerCliente();
    if (cliente === null) return;

    const boton = $('boton-confirmar-cobro');
    boton.disabled = true;
    boton.textContent = 'Registrando…';
    const cuerpo = cuerpoDeLaVenta();
    if (ES_ADMIN) cuerpo.operadorId = Number($('operador').value);

    try {
        const venta = await Sesion.llamar('/api/ventas', { metodo: 'POST', cuerpo });
        $('ventana-cobrar').close();
        if (Object.values(cliente).some(Boolean)) clientes.set(venta.id, cliente);
        mostrarRecibo(venta, Number.isFinite(pago) ? pago : null);
        carrito.clear();
        ventas = undefined; // la lista de lo vendido se vuelve a pedir
        cargarCatalogo();   // las existencias cambiaron
    } catch (error) {
        if (error.estado === 409) {
            // Alguien más vendió lo mismo, se vendió la prenda o cambió un precio: se avisa y se recarga el catálogo.
            const consejo = error.titulo === 'No hay suficientes existencias' ? ' Quita algunas unidades y vuelve a cobrar.'
                : error.titulo === 'Prenda ya vendida' ? ' Quítala de la venta y vuelve a cobrar.' : '';
            $('cobrar-error').textContent = `${error.message}${consejo}`;
            cargarCatalogo();
        } else if (error.estado === 404) {
            $('cobrar-error').textContent = 'Algo de la lista ya no existe en el catálogo. Quítalo y vuelve a cobrar.';
            cargarCatalogo();
        } else if (error.estado !== 401) {
            $('cobrar-error').textContent = error.estado === 400 && error.message ? error.message : mensajeGeneral(error);
        }
    } finally {
        boton.disabled = false;
        boton.textContent = 'Listo, registrar venta';
    }
});

function mostrarRecibo(venta, pago) {
    ventaDelRecibo = venta;
    const recibo = $('recibo');
    recibo.replaceChildren();
    const lista = document.createElement('ul');
    lista.className = 'recibo__lineas';
    for (const articulo of venta.articulos) {
        const linea = document.createElement('li');
        const nombre = document.createElement('span');
        nombre.textContent = `${articulo.cantidad} × ${articulo.nombre}`;
        const valor = document.createElement('span');
        valor.textContent = articulo.precioUnitario != null ? pesos.format(articulo.precioUnitario * articulo.cantidad) : '';
        linea.append(nombre, valor);
        lista.append(linea);
    }
    const total = document.createElement('p');
    total.className = 'recibo__total';
    total.textContent = `Total: ${pesos.format(venta.total)}`;
    recibo.append(lista, total);
    if (pago !== null && pago > venta.total) {
        const cambio = document.createElement('p');
        cambio.className = 'cambio cambio--bien';
        cambio.textContent = `Devolver ${pesos.format(pago - venta.total)}`;
        recibo.append(cambio);
    }
    const codigo = document.createElement('p');
    codigo.className = 'recibo__codigo';
    codigo.textContent = `Factura n.º ${venta.codigo}`;
    recibo.append(codigo);
    const cliente = clientes.get(venta.id);
    if (cliente?.nombre) {
        const paraQuien = document.createElement('p');
        paraQuien.className = 'recibo__codigo';
        paraQuien.textContent = `Cliente: ${cliente.nombre}`;
        recibo.append(paraQuien);
    }
    $('ventana-recibo').showModal();
    $('boton-nueva-venta').focus();
}

$('boton-nueva-venta').addEventListener('click', () => $('ventana-recibo').close());
$('ventana-recibo').addEventListener('close', () => {
    dibujarCarrito();
    $('buscar').value = '';
    dibujarCatalogo();
    $('buscar').focus();
});

/* ---------- Cambiar una venta registrada ---------- */

function unidadesEnLaVentaQueSeCambia(articulo) {
    if (!editando) return 0;
    return editando.articulos
        .filter((linea) => linea.tipo === articulo.tipo && linea.id === articulo.id)
        .reduce((suma, linea) => suma + linea.cantidad, 0);
}

/* Al cambiar una venta, lo que ella ya tenía vuelve a estar disponible para ella. */
function ajustarExistencias() {
    for (const articulo of catalogo || []) {
        if (articulo.enTienda === null) continue;
        const suma = articulo.enTienda + unidadesEnLaVentaQueSeCambia(articulo);
        articulo.cantidad = articulo.tipo === 'ropa' ? Math.min(suma, 1) : suma;
    }
}

function diferencia(antes, ahora) {
    const texto = `Antes eran ${pesos.format(antes)}. `;
    if (ahora > antes) return `${texto}Cobra ${pesos.format(ahora - antes)} más.`;
    if (ahora < antes) return `${texto}Devuelve ${pesos.format(antes - ahora)}.`;
    return `${texto}El total no cambia.`;
}

function dibujarModo() {
    $('aviso-cambio').hidden = !editando;
    if (editando) $('aviso-cambio-texto').textContent = `Estás cambiando la factura n.º ${editando.codigo}, de ${pesos.format(editando.total)}.`;
    $('caja-operador').hidden = !ES_ADMIN || Boolean(editando);
    $('barra-cobro-boton').textContent = editando ? 'Guardar' : 'Cobrar';
}

async function empezarCambio(venta) {
    if (!editando && carrito.size > 0 && !window.confirm('Hay una venta sin cobrar. ¿La quitamos para cambiar esta?')) return;
    editando = venta;
    carrito.clear();
    dibujarModo();
    elegirPestana('vender');
    await cargarCatalogo(); // con las existencias al día
    if (editando !== venta || !catalogo) return;
    let faltan = 0;
    for (const linea of venta.articulos) {
        const articulo = catalogo.find((a) => a.tipo === linea.tipo && a.id === linea.id);
        if (articulo) carrito.set(clave(articulo.tipo, articulo.id), { articulo, cantidad: linea.cantidad });
        else faltan++;
    }
    if (faltan) avisar('Algo de esa venta ya no está en el catálogo y no aparece en la lista.', 'error');
    dibujarCarrito();
    dibujarCatalogo();
}

function terminarCambio() {
    editando = null;
    carrito.clear();
    ajustarExistencias();
    dibujarModo();
    dibujarCarrito();
    dibujarCatalogo();
}

/* La clave viaja codificada para que admita tildes y eñes. */
function cabeceraDePermiso(permiso) {
    return ES_ADMIN ? {} : { 'X-Clave-Permiso': encodeURIComponent(permiso) };
}

async function guardarCambios() {
    const venta = editando;
    const permiso = $('clave-permiso').value;
    if (!ES_ADMIN && !permiso.trim()) {
        $('cobrar-error').textContent = 'Escribe tu clave de permiso.';
        $('clave-permiso').focus();
        return;
    }

    const boton = $('boton-confirmar-cobro');
    boton.disabled = true;
    boton.textContent = 'Guardando…';
    $('cobrar-error').textContent = '';
    try {
        await Sesion.llamar(`/api/ventas/${venta.id}`, { metodo: 'PUT', cuerpo: cuerpoDeLaVenta(), cabeceras: cabeceraDePermiso(permiso) });
        $('ventana-cobrar').close();
        terminarCambio();
        ventas = undefined;
        elegirPestana('vendido');
        cargarCatalogo();
        avisar(`✓ Se guardaron los cambios de la factura n.º ${venta.codigo}.`);
    } catch (error) {
        if ([400, 403, 404, 409, 429].includes(error.estado) && error.message) {
            $('cobrar-error').textContent = error.message;
            if (error.estado === 404 || error.estado === 409) cargarCatalogo();
        } else if (error.estado !== 401) {
            $('cobrar-error').textContent = mensajeGeneral(error);
        }
    } finally {
        boton.disabled = false;
        boton.textContent = 'Guardar cambios';
    }
}

$('boton-dejar-venta').addEventListener('click', () => {
    terminarCambio();
    elegirPestana('vendido');
});

/* ---------- Operadores (solo la administradora) ---------- */

async function cargarOperadores() {
    $('caja-operador').hidden = false;
    const selector = $('operador');
    selector.replaceChildren(new Option('Cargando…', ''));
    try {
        // Solo se puede vender a nombre de quien está activo.
        operadores = (await Sesion.llamar('/api/operadores-cibercafe')).filter((o) => !o.estado || o.estado === 'Activo');
        selector.replaceChildren();
        if (operadores.length !== 1) selector.append(new Option('Elige el operador', ''));
        for (const operador of operadores) {
            selector.append(new Option(`${operador.primerNombre} ${operador.primerApellido}`.trim(), String(operador.idUsuario)));
        }
        if (operadores.length === 0) selector.replaceChildren(new Option('No hay operadores registrados', ''));
    } catch {
        selector.replaceChildren(new Option('No se pudo cargar la lista', ''));
    }
}

/* ---------- Lo vendido ---------- */

function dibujarPeriodos() {
    const contenedor = $('periodos');
    contenedor.replaceChildren();
    for (const [valor, info] of Object.entries(PERIODOS)) {
        const boton = document.createElement('button');
        boton.type = 'button';
        boton.className = 'filtro';
        boton.setAttribute('aria-pressed', String(valor === periodo));
        boton.textContent = info.titulo;
        boton.addEventListener('click', () => {
            periodo = valor;
            dibujarPeriodos();
            cargarVentas();
        });
        contenedor.append(boton);
    }
}

async function cargarVentas() {
    ventas = undefined;
    dibujarVentas();
    const { dias } = PERIODOS[periodo];
    try {
        const desde = dias === null ? PRIMERA_FECHA : hoyISO(dias);
        ventas = await Sesion.llamar(`/api/ventas?desde=${desde}&hasta=${hoyISO()}&incluirAnuladas=true`);
    } catch (error) {
        ventas = error.estado === 401 ? undefined : null;
    }
    dibujarVentas();
}

function dibujarVentas() {
    const contenedor = $('ventas');
    const resumen = $('resumen');
    contenedor.replaceChildren();
    resumen.replaceChildren();
    $('vendido-contador').textContent = '';
    $('vendido-estado').hidden = true;
    dibujarClases();
    $('vendido-ayuda').textContent = ES_ADMIN ? 'Todas las ventas del negocio.'
        : 'Solo aparecen las ventas que registraste tú. Para cambiar o anular una necesitas tu clave de permiso.';

    if (ventas === undefined) {
        contenedor.setAttribute('aria-busy', 'true');
        for (let i = 0; i < 2; i++) {
            const esqueleto = document.createElement('div');
            esqueleto.className = 'tarjeta tarjeta--cargando';
            contenedor.append(esqueleto);
        }
        return;
    }
    contenedor.setAttribute('aria-busy', 'false');

    if (ventas === null) {
        mostrarEstado('vendido-', {
            icono: '📡', titulo: 'No pudimos conectar con el sistema', texto: 'Revisa que haya internet.',
            boton: { texto: 'Intentar de nuevo', accion: cargarVentas }
        });
        return;
    }

    // Las anuladas se ven en la lista, pero no cuentan.
    const vigentes = ventas.filter((venta) => !venta.anulada);
    const total = vigentes.reduce((suma, venta) => suma + venta.total, 0);
    resumen.append(
        crearCifra('Total vendido', pesos.format(total)),
        crearCifra('Ventas', miles.format(vigentes.length)),
        crearCifra('Promedio por venta', vigentes.length ? pesos.format(Math.round(total / vigentes.length)) : '—')
    );

    if (ventas.length === 0) {
        mostrarEstado('vendido-', {
            icono: '🧾',
            titulo: periodo === 'hoy' ? 'Todavía no hay ventas hoy' : 'No hay ventas en estos días',
            texto: 'Cuando registres una venta aparece aquí.',
            boton: { texto: 'Hacer una venta', accion: () => elegirPestana('vender') }
        });
        return;
    }

    const visibles = ventasVisibles();
    if (visibles.length === 0) {
        mostrarEstado('vendido-', {
            icono: '🔍', titulo: 'No encontramos ninguna factura así', texto: 'Cambia la búsqueda o elige «Todas».',
            boton: { texto: 'Ver todas', accion: () => { $('buscar-vendido').value = ''; clase = 'todas'; dibujarVentas(); } }
        });
        return;
    }
    if (visibles.length !== ventas.length) $('vendido-contador').textContent = `Mostrando ${visibles.length} de ${ventas.length}`;

    let fechaAnterior = '';
    for (const venta of visibles) {
        if (venta.fecha !== fechaAnterior) {
            fechaAnterior = venta.fecha;
            const dia = document.createElement('h3');
            dia.className = 'ventas__dia';
            dia.textContent = fechaLegible(venta.fecha);
            contenedor.append(dia);
        }
        contenedor.append(crearVenta(venta));
    }
}

function crearCifra(titulo, valor) {
    const cifra = document.createElement('div');
    cifra.className = 'cifra';
    const nombre = document.createElement('span');
    nombre.className = 'cifra__titulo';
    nombre.textContent = titulo;
    const numero = document.createElement('strong');
    numero.className = 'cifra__valor';
    numero.textContent = valor;
    cifra.append(nombre, numero);
    return cifra;
}

function crearVenta(venta) {
    const tarjeta = document.createElement('article');
    tarjeta.className = venta.anulada ? 'tarjeta venta venta--anulada' : 'tarjeta venta';

    const cabecera = document.createElement('div');
    cabecera.className = 'venta__cabecera';
    const total = document.createElement('p');
    total.className = 'tarjeta__precio';
    total.textContent = pesos.format(venta.total);
    const codigo = document.createElement('p');
    codigo.className = 'venta__codigo';
    codigo.textContent = `Factura n.º ${venta.codigo}`;
    cabecera.append(total, codigo);
    tarjeta.append(cabecera);

    const lista = document.createElement('ul');
    lista.className = 'venta__lineas';
    for (const articulo of venta.articulos) {
        const linea = document.createElement('li');
        linea.textContent = `${articulo.cantidad} × ${articulo.nombre}`;
        lista.append(linea);
    }
    tarjeta.append(lista);

    if (ES_ADMIN || venta.anulada) {
        const etiquetas = document.createElement('div');
        etiquetas.className = 'etiquetas';
        const agregarEtiqueta = (texto, extra = '') => {
            const etiqueta = document.createElement('span');
            etiqueta.className = `etiqueta ${extra}`.trim();
            etiqueta.textContent = texto;
            etiquetas.append(etiqueta);
        };
        if (venta.anulada) agregarEtiqueta('🚫 Anulada', 'etiqueta--mal');
        if (ES_ADMIN) agregarEtiqueta(`Vendió: ${venta.operador}`);
        tarjeta.append(etiquetas);
    }
    if (venta.anulada) return tarjeta;

    const acciones = document.createElement('div');
    acciones.className = 'tarjeta__acciones';
    const crearAccion = (texto, clase, descripcion, accion) => {
        const boton = document.createElement('button');
        boton.type = 'button';
        boton.className = `boton ${clase}`;
        boton.textContent = texto;
        boton.setAttribute('aria-label', `${descripcion} de ${pesos.format(venta.total)}, factura ${venta.codigo}`);
        boton.addEventListener('click', () => accion(venta));
        acciones.append(boton);
    };
    crearAccion('📄 PDF', 'boton--suave', 'Descargar en PDF la venta', descargarPdf);
    crearAccion('✉️ Correo', 'boton--suave', 'Enviar por correo la venta', abrirCorreo);
    if (venta.tipo === 'Venta en tienda') crearAccion('✏️ Cambiar venta', 'boton--suave', 'Cambiar la venta', empezarCambio);
    crearAccion('↩️ Anular venta', 'boton--peligro-suave', 'Anular la venta', pedirAnular);
    tarjeta.append(acciones);
    return tarjeta;
}

function pedirAnular(venta) {
    porAnular = venta;
    $('anular-titulo').textContent = `¿Anular la venta de ${pesos.format(venta.total)}?`;
    $('anular-clave-caja').hidden = ES_ADMIN;
    $('anular-clave').value = '';
    $('anular-error').textContent = '';
    $('ventana-anular').showModal();
    (ES_ADMIN ? $('ventana-anular').querySelector('[data-cerrar]') : $('anular-clave')).focus();
}

$('boton-confirmar-anular').addEventListener('click', async () => {
    const venta = porAnular;
    if (!venta) return;
    const permiso = $('anular-clave').value;
    if (!ES_ADMIN && !permiso.trim()) {
        $('anular-error').textContent = 'Escribe tu clave de permiso.';
        $('anular-clave').focus();
        return;
    }
    const boton = $('boton-confirmar-anular');
    boton.disabled = true;
    try {
        await Sesion.llamar(`/api/ventas/${venta.id}`, { metodo: 'DELETE', cabeceras: cabeceraDePermiso(permiso) });
        $('ventana-anular').close();
        if (editando?.id === venta.id) terminarCambio();
        avisar(`✓ Se anuló la venta de ${pesos.format(venta.total)}.`);
        cargarVentas();
        cargarCatalogo();
    } catch (error) {
        // Si falló el permiso, la ventana sigue abierta para escribir la clave otra vez.
        if (error.estado === 403 || error.estado === 429) {
            $('anular-error').textContent = error.message;
            return;
        }
        $('ventana-anular').close();
        if (error.estado === 404 || (error.estado === 409 && error.titulo === 'Venta anulada')) {
            avisar('Esa venta ya estaba anulada.');
            cargarVentas();
        } else if (error.estado !== 401) {
            avisar(mensajeGeneral(error), 'error');
        }
    } finally {
        boton.disabled = false;
    }
});

function dibujarClases() {
    const contenedor = $('clases');
    contenedor.replaceChildren();
    for (const [valor, info] of Object.entries(CLASES)) {
        const boton = document.createElement('button');
        boton.type = 'button';
        boton.className = 'filtro';
        boton.setAttribute('aria-pressed', String(valor === clase));
        boton.textContent = info.titulo;
        boton.addEventListener('click', () => {
            clase = valor;
            dibujarVentas();
        });
        contenedor.append(boton);
    }
}

/* Cada palabra buscada debe aparecer en el número, el vendedor, la fecha o lo que se llevó. */
function ventasVisibles() {
    const palabras = normalizar($('buscar-vendido').value).split(/\s+/).filter(Boolean);
    return ventas.filter((venta) => {
        if (!CLASES[clase].pasa(venta)) return false;
        const texto = normalizar([venta.codigo, venta.operador, venta.fecha, fechaLegible(venta.fecha),
            ...venta.articulos.map((a) => a.nombre)].join(' '));
        return palabras.every((palabra) => texto.includes(palabra));
    });
}

$('buscar-vendido').addEventListener('input', () => { if (ventas) dibujarVentas(); });

/* ---------- Datos del cliente, PDF y correo ---------- */

/* Devuelve los datos escritos (todos opcionales), o null si alguno está mal. */
function leerCliente() {
    const nombre = $('cliente-nombre').value.replace(/\s+/g, ' ').trim();
    const celular = $('cliente-celular').value.replace(/[\s.-]/g, '');
    const correo = $('cliente-correo').value.trim();
    const fallo = (campo, mensaje) => {
        $('caja-cliente').open = true;
        $('cobrar-error').textContent = mensaje;
        $(campo).focus();
        return null;
    };
    if (nombre && !/^[\p{L} .'-]{3,60}$/u.test(nombre)) return fallo('cliente-nombre', 'El nombre del cliente solo lleva letras (mínimo 3).');
    if (celular && !/^\d{7,15}$/.test(celular)) return fallo('cliente-celular', 'El celular del cliente lleva entre 7 y 15 números.');
    if (correo && !correoValido(correo)) return fallo('cliente-correo', 'Revisa el correo del cliente.');
    return { nombre, celular, correo };
}

function correoValido(correo) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(correo);
}

function fechaCorta(iso) {
    const [anio, mes, dia] = iso.split('-');
    return `${Number(dia)}/${mes}/${anio}`;
}

/* Los valores por artículo solo se muestran si suman lo cobrado: los precios pueden haber cambiado después. */
function lineasDelRecibo(venta) {
    const cliente = clientes.get(venta.id) || {};
    const suma = venta.articulos.reduce((total, a) => total + (a.precioUnitario ?? NaN) * a.cantidad, 0);
    const conValores = Math.abs(suma - venta.total) < 0.5;
    return [
        { texto: 'Papelería Matrix', tipo: 'titulo' },
        { texto: `Registro de venta n.º ${venta.codigo}`, tipo: 'subtitulo' },
        { texto: `Fecha: ${fechaCorta(venta.fecha)}` },
        { texto: `Atendió: ${venta.operador}` },
        ...[cliente.nombre && `Cliente: ${cliente.nombre}`, cliente.celular && `Celular: ${cliente.celular}`,
            cliente.correo && `Correo: ${cliente.correo}`].filter(Boolean).map((texto) => ({ texto })),
        { linea: true },
        ...venta.articulos.map((a) => ({
            texto: `${a.cantidad} × ${a.nombre}`,
            derecha: conValores ? pesos.format(a.precioUnitario * a.cantidad) : ''
        })),
        { linea: true },
        { texto: 'Total pagado', derecha: pesos.format(venta.total), tipo: 'total' },
        { espacio: true },
        { texto: '¡Gracias por tu compra!' }
    ];
}

function descargarPdf(venta) {
    Pdf.descargar(`venta-${venta.codigo}.pdf`, lineasDelRecibo(venta));
    avisar('✓ Se descargó el PDF. Búscalo en tus descargas.');
}

function textoDelCorreo(venta, nombre) {
    return [
        nombre ? `Hola, ${nombre}:` : 'Hola:', '',
        'Este es el registro de tu compra en Papelería Matrix.', '',
        `Registro n.º ${venta.codigo}`, `Fecha: ${fechaCorta(venta.fecha)}`, `Te atendió: ${venta.operador}`, '',
        ...venta.articulos.map((a) => `- ${a.cantidad} x ${a.nombre}`), '',
        `Total pagado: ${pesos.format(venta.total)}`, '', '¡Gracias por tu compra!'
    ].join('\n');
}

function abrirCorreo(venta) {
    ventaDelCorreo = venta;
    $('correo-detalle').textContent = `Factura n.º ${venta.codigo}, de ${pesos.format(venta.total)}.`;
    $('correo-para').value = clientes.get(venta.id)?.correo || '';
    $('correo-error').textContent = '';
    $('correo-propio').hidden = true;
    $('boton-enviar-correo').hidden = false;
    $('ventana-correo').showModal();
    $('correo-para').focus();
}

/* Si el sistema no puede enviar, se abre el correo de la persona con el mensaje ya escrito. */
function ofrecerCorreoPropio(para) {
    const nombre = clientes.get(ventaDelCorreo.id)?.nombre;
    const asunto = encodeURIComponent('Tu compra en Papelería Matrix');
    const cuerpo = encodeURIComponent(textoDelCorreo(ventaDelCorreo, nombre));
    $('correo-propio').href = `mailto:${encodeURIComponent(para).replace('%40', '@')}?subject=${asunto}&body=${cuerpo}`;
    $('correo-propio').hidden = false;
    $('boton-enviar-correo').hidden = true;
    $('correo-error').textContent = 'El envío desde el sistema todavía no está activado. Toca «Abrir mi correo»: el mensaje ya va escrito.';
}

$('formulario-correo').addEventListener('submit', async (evento) => {
    evento.preventDefault();
    const para = $('correo-para').value.trim();
    if (!correoValido(para)) {
        $('correo-error').textContent = 'Escribe un correo completo, por ejemplo cliente@correo.com';
        $('correo-para').focus();
        return;
    }
    const venta = ventaDelCorreo;
    const boton = $('boton-enviar-correo');
    boton.disabled = true;
    boton.textContent = 'Enviando…';
    $('correo-error').textContent = '';
    try {
        const nombre = clientes.get(venta.id)?.nombre || null;
        await Sesion.llamar(`/api/ventas/${venta.id}/correo`, { metodo: 'POST', cuerpo: { correo: para, nombre } });
        $('ventana-correo').close();
        avisar(`✓ Se envió el registro a ${para}.`);
    } catch (error) {
        if (error.estado === 503) ofrecerCorreoPropio(para);
        else if (error.estado !== 401) $('correo-error').textContent = error.message || mensajeGeneral(error);
    } finally {
        boton.disabled = false;
        boton.textContent = 'Enviar';
    }
});

$('correo-para').addEventListener('input', () => {
    $('correo-propio').hidden = true;
    $('boton-enviar-correo').hidden = false;
});
$('correo-propio').addEventListener('click', () => setTimeout(() => $('ventana-correo').close(), 300));
$('boton-recibo-pdf').addEventListener('click', () => descargarPdf(ventaDelRecibo));
$('boton-recibo-correo').addEventListener('click', () => abrirCorreo(ventaDelRecibo));

/* ---------- Existencias ---------- */

function dibujarExistencias() {
    const contenedor = $('existencias-lista');
    const contador = $('existencias-contador');
    contenedor.replaceChildren();
    if (!catalogoCargado) {
        contador.textContent = 'Cargando…';
        return;
    }
    if (!catalogo) {
        contador.textContent = 'No pudimos conectar con el sistema. Revisa el internet e intenta otra vez.';
        return;
    }
    const palabras = normalizar($('buscar-existencias').value).split(/\s+/).filter(Boolean);
    const productos = catalogo.filter((a) => a.tipo === 'producto' && (!palabras.length || coincide(a, palabras)));
    contador.textContent = productos.length === 1 ? '1 producto' : `${productos.length} productos`;
    for (const producto of productos) {
        const tarjeta = document.createElement('article');
        tarjeta.className = 'tarjeta venta';
        const nombre = document.createElement('h3');
        nombre.className = 'tarjeta__nombre';
        nombre.textContent = producto.nombre;
        const cuantas = document.createElement('div');
        cuantas.className = 'etiquetas';
        const etiqueta = document.createElement('span');
        const hay = producto.enTienda;
        etiqueta.className = `etiqueta ${hay < POCAS_UNIDADES ? 'etiqueta--mal' : 'etiqueta--bien'}`;
        etiqueta.textContent = hay === 0 ? 'Agotado' : hay === 1 ? 'Queda 1' : `Quedan ${miles.format(hay)}`;
        cuantas.append(etiqueta);
        const acciones = document.createElement('div');
        acciones.className = 'tarjeta__acciones';
        const boton = document.createElement('button');
        boton.type = 'button';
        boton.className = 'boton boton--suave';
        boton.textContent = '✏️ Cambiar cantidad';
        boton.setAttribute('aria-label', `Cambiar la cantidad de ${producto.nombre}`);
        boton.addEventListener('click', () => abrirExistencias(producto));
        acciones.append(boton);
        tarjeta.append(nombre, cuantas, acciones);
        contenedor.append(tarjeta);
    }
}

function abrirExistencias(producto) {
    productoContado = producto;
    $('existencias-titulo').textContent = producto.nombre;
    $('existencias-detalle').textContent = `El sistema dice que hay ${miles.format(producto.enTienda)}.`;
    $('existencias-cantidad').value = '';
    $('existencias-error').textContent = '';
    $('ventana-existencias').showModal();
    $('existencias-cantidad').focus();
}

$('formulario-existencias').addEventListener('submit', async (evento) => {
    evento.preventDefault();
    const cantidad = leerEntero($('existencias-cantidad').value.trim());
    if (!Number.isFinite(cantidad) || cantidad > 100000) {
        $('existencias-error').textContent = 'Escribe un número entre 0 y 100.000.';
        $('existencias-cantidad').focus();
        return;
    }
    const producto = productoContado;
    const boton = $('boton-guardar-existencias');
    boton.disabled = true;
    try {
        await Sesion.llamar(`/api/productos/${producto.id}/existencias`, { metodo: 'PUT', cuerpo: { cantidad, cantidadAnterior: producto.enTienda } });
        $('ventana-existencias').close();
        avisar(`✓ ${producto.nombre}: quedan ${miles.format(cantidad)}.`);
        await cargarCatalogo();
    } catch (error) {
        if (error.estado === 409 || error.estado === 404) {
            $('ventana-existencias').close();
            avisar(error.estado === 404 ? 'Ese producto ya no está en el catálogo.' : 'Las unidades cambiaron mientras contabas. Revisa el número nuevo.', 'error');
            await cargarCatalogo();
        } else if (error.estado !== 401) {
            $('existencias-error').textContent = mensajeGeneral(error);
        }
    } finally {
        boton.disabled = false;
    }
});

$('buscar-existencias').addEventListener('input', dibujarExistencias);

/* ---------- Eventos generales ---------- */

$('buscar').addEventListener('input', dibujarCatalogo);
// Con Enter en el buscador se agrega el primer resultado: rápido cuando hay fila en el mostrador.
$('buscar').addEventListener('keydown', (evento) => {
    if (evento.key !== 'Enter') return;
    evento.preventDefault();
    const primero = $('articulos').querySelector('.articulo:not(:disabled)');
    if (primero) primero.click();
});

for (const boton of document.querySelectorAll('[data-cerrar]')) {
    boton.addEventListener('click', () => boton.closest('dialog').close());
}

/* ---------- Inicio ---------- */

if (SESION) {
    if (ES_ADMIN) {
        const volver = document.createElement('a');
        volver.href = 'panel.html';
        volver.className = 'barra__enlace';
        volver.textContent = 'Volver a mi negocio';
        $('barra-usuario').append(volver);
        cargarOperadores();
    }
    Sesion.dibujarUsuario(SESION, $('barra-usuario'));
    $('saludo-texto').textContent = `Ventas de ${SESION.nombre.split(' ')[0]}`;
    if (ES_ADMIN) $('saludo-texto').textContent = 'Ventas';
    dibujarFiltros();
    dibujarPeriodos();
    dibujarCarrito();
    cargarCatalogo();
    let pestana = 'vender';
    try { pestana = sessionStorage.getItem(PESTANA_ALMACEN) || 'vender'; } catch { /* empieza en vender */ }
    elegirPestana(PESTANAS.includes(pestana) ? pestana : 'vender');
}
