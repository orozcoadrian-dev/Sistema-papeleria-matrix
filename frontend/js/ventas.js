'use strict';

/* Pantalla de ventas. El operador del cibercafé toca lo que se lleva el cliente, revisa el total y cobra.
   La administradora también entra aquí: puede vender a nombre de un operador, ver todas las ventas y anularlas.
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

const PERIODOS = {
    hoy: { titulo: 'Hoy', dias: 0 },
    semana: { titulo: 'Últimos 7 días', dias: 6 },
    mes: { titulo: 'Últimos 30 días', dias: 29 }
};

/* ---------- Estado ---------- */

let catalogo = [];          // todos los artículos: { tipo, id, nombre, precio, detalle, cantidad? }
let catalogoCargado = false;
let filtro = 'todo';
const carrito = new Map();   // clave "tipo:id" → { articulo, cantidad }
let periodo = 'hoy';
let ventas = undefined;      // undefined = cargando, null = sin conexión
let porAnular = null;
let operadores = [];

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
    const vender = cual === 'vender';
    $('pestana-vender').setAttribute('aria-selected', String(vender));
    $('pestana-vendido').setAttribute('aria-selected', String(!vender));
    $('pestana-vender').tabIndex = vender ? 0 : -1;
    $('pestana-vendido').tabIndex = vender ? -1 : 0;
    $('vista-vender').hidden = !vender;
    $('vista-vendido').hidden = vender;
    $('saludo-sub').textContent = vender
        ? 'Toca lo que se lleva el cliente y después «Cobrar».'
        : 'Revisa cuánto se ha vendido.';
    try { sessionStorage.setItem(PESTANA_ALMACEN, cual); } catch { /* solo es una comodidad */ }
    $('barra-cobro').hidden = !vender || carrito.size === 0;
    if (!vender && ventas === undefined) cargarVentas();
}

$('pestana-vender').addEventListener('click', () => elegirPestana('vender'));
$('pestana-vendido').addEventListener('click', () => elegirPestana('vendido'));
for (const pestana of [$('pestana-vender'), $('pestana-vendido')]) {
    pestana.addEventListener('keydown', (evento) => {
        if (evento.key === 'ArrowRight' || evento.key === 'ArrowLeft') {
            const otra = pestana.id === 'pestana-vender' ? $('pestana-vendido') : $('pestana-vender');
            otra.click();
            otra.focus();
        }
    });
}

/* ---------- Catálogo ---------- */

async function cargarCatalogo() {
    catalogoCargado = false;
    dibujarCatalogo();
    const despertando = setTimeout(() => {
        if (!catalogoCargado) $('contador').textContent = 'Estamos despertando el sistema. La primera vez del día puede tardar hasta un minuto ☕';
    }, 4000);
    try {
        const listas = await Promise.all(Object.entries(TIPOS).map(async ([tipo, info]) => {
            const registros = await Sesion.llamar(info.ruta);
            return registros.map((r) => ({
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

    const icono = document.createElement('span');
    icono.className = 'articulo__icono';
    icono.setAttribute('aria-hidden', 'true');
    icono.textContent = TIPOS[articulo.tipo].icono;

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
    $('boton-vaciar').hidden = vacio;
    $('boton-cobrar').disabled = vacio;
    const total = totalDelCarrito();
    $('carrito-total').textContent = pesos.format(total);
    $('boton-cobrar').textContent = vacio ? 'Cobrar' : `Cobrar ${pesos.format(total)}`;

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
    if (ES_ADMIN && !$('operador').value) {
        avisar('Elige arriba a nombre de qué operador queda la venta.', 'error');
        $('operador').focus();
        return;
    }
    const total = totalDelCarrito();
    const unidades = [...carrito.values()].reduce((suma, linea) => suma + linea.cantidad, 0);
    $('cobrar-total').textContent = pesos.format(total);
    $('cobrar-detalle').textContent = unidades === 1 ? '1 artículo.' : `${unidades} artículos.`;
    $('pago').value = '';
    $('cambio').textContent = '';
    $('cobrar-error').textContent = '';
    dibujarPagosRapidos(total);
    $('ventana-cobrar').showModal();
    $('pago').focus();
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

$('formulario-cobrar').addEventListener('submit', async (evento) => {
    evento.preventDefault();
    const pagoTexto = $('pago').value.trim();
    const pago = pagoTexto ? leerEntero(pagoTexto) : NaN;
    if (pagoTexto && Number.isFinite(pago) && pago < totalDelCarrito()) {
        $('cobrar-error').textContent = 'Lo que paga no alcanza. Revisa el valor o déjalo vacío.';
        $('pago').focus();
        return;
    }

    const boton = $('boton-confirmar-cobro');
    boton.disabled = true;
    boton.textContent = 'Registrando…';
    const cuerpo = {
        articulos: [...carrito.values()].map(({ articulo, cantidad }) => ({ tipo: articulo.tipo, id: articulo.id, cantidad })),
        // Lo que se le dijo al cliente: si un precio cambió mientras tanto, el servidor no registra otro total.
        totalEsperado: totalDelCarrito()
    };
    if (ES_ADMIN) cuerpo.operadorId = Number($('operador').value);

    try {
        const venta = await Sesion.llamar('/api/ventas', { metodo: 'POST', cuerpo });
        $('ventana-cobrar').close();
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

/* ---------- Operadores (solo la administradora) ---------- */

async function cargarOperadores() {
    $('caja-operador').hidden = false;
    const selector = $('operador');
    selector.replaceChildren(new Option('Cargando…', ''));
    try {
        operadores = await Sesion.llamar('/api/operadores-cibercafe');
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
        ventas = await Sesion.llamar(`/api/ventas?desde=${hoyISO(dias)}&hasta=${hoyISO()}`);
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
    $('vendido-estado').hidden = true;
    $('vendido-ayuda').textContent = ES_ADMIN ? 'Todas las ventas del negocio.' : 'Solo aparecen las ventas que registraste tú.';

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

    const total = ventas.reduce((suma, venta) => suma + venta.total, 0);
    resumen.append(
        crearCifra('Total vendido', pesos.format(total)),
        crearCifra('Ventas', miles.format(ventas.length)),
        crearCifra('Promedio por venta', ventas.length ? pesos.format(Math.round(total / ventas.length)) : '—')
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

    let fechaAnterior = '';
    for (const venta of ventas) {
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
    tarjeta.className = 'tarjeta venta';

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

    if (ES_ADMIN) {
        const etiquetas = document.createElement('div');
        etiquetas.className = 'etiquetas';
        const quien = document.createElement('span');
        quien.className = 'etiqueta';
        quien.textContent = `Vendió: ${venta.operador}`;
        etiquetas.append(quien);
        tarjeta.append(etiquetas);

        const acciones = document.createElement('div');
        acciones.className = 'tarjeta__acciones';
        const anular = document.createElement('button');
        anular.type = 'button';
        anular.className = 'boton boton--peligro-suave';
        anular.textContent = '↩️ Anular venta';
        anular.setAttribute('aria-label', `Anular la venta de ${pesos.format(venta.total)}, factura ${venta.codigo}`);
        anular.addEventListener('click', () => pedirAnular(venta));
        acciones.append(anular);
        tarjeta.append(acciones);
    }
    return tarjeta;
}

function pedirAnular(venta) {
    porAnular = venta;
    $('anular-titulo').textContent = `¿Anular la venta de ${pesos.format(venta.total)}?`;
    $('ventana-anular').showModal();
    $('ventana-anular').querySelector('[data-cerrar]').focus();
}

$('boton-confirmar-anular').addEventListener('click', async () => {
    const venta = porAnular;
    if (!venta) return;
    const boton = $('boton-confirmar-anular');
    boton.disabled = true;
    try {
        await Sesion.llamar(`/api/ventas/${venta.id}`, { metodo: 'DELETE' });
        $('ventana-anular').close();
        avisar(`✓ Se anuló la venta de ${pesos.format(venta.total)}.`);
        cargarVentas();
        cargarCatalogo();
    } catch (error) {
        $('ventana-anular').close();
        if (error.estado === 404) {
            avisar('Esa venta ya estaba anulada.');
            cargarVentas();
        } else if (error.estado !== 401) {
            avisar(mensajeGeneral(error), 'error');
        }
    } finally {
        boton.disabled = false;
        porAnular = null;
    }
});

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
    elegirPestana(pestana === 'vendido' ? 'vendido' : 'vender');
}
