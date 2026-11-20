'use strict';

/* Panel de la administradora: servicios, papelería y ropa.
   Lo usa una persona que no es técnica, así que los textos son cotidianos ("Agregar", "Cambiar",
   "Quitar"), los códigos se llenan solos y cada error dice qué hacer.
   Cada sección se describe una vez en SECCIONES; el resto del código sirve para todas.
   Los datos se insertan con textContent, nunca con innerHTML, para que nada guardado ejecute código. */

const CONFIG = window.MATRIX_CONFIG;
const LLAVE_ALMACEN = 'matrix.llave';
const SECCION_ALMACEN = 'matrix.seccion';

// Topes para atrapar ceros de más al escribir. La base acepta más, pero estos valores no son normales en el negocio.
const PRECIO_MAXIMO = 10000000;
const CANTIDAD_MAXIMA = 100000;

const pesos = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 });
const miles = new Intl.NumberFormat('es-CO', { maximumFractionDigits: 0 });

// Las mismas categorías que usa la página pública para elegir el dibujo de cada servicio (js/main.js).
const CATEGORIAS_SERVICIO = ['Impresión', 'Copias', 'Escáner', 'Transcripción'];

const SECCIONES = {
    servicios: {
        icono: '🖨️',
        titulo: 'Servicios',
        plural: 'servicios',
        singular: 'servicio',
        femenino: false,
        ayuda: 'Lo que ofreces en el cibercafé: impresiones, copias, escáner…',
        buscarPor: 'nombre o tipo',
        ruta: '/api/servicios',
        prefijoCodigo: 'SER',
        buscarEn: (s) => [s.nombre, s.categoria, s.descripcion],
        tarjeta: (s) => ({
            nombre: s.nombre,
            precio: s.precio,
            etiquetas: [s.categoria],
            descripcion: s.descripcion
        }),
        // Dos servicios con el mismo nombre confunden en la página pública.
        mismoQue: (a, b) => normalizar(a.nombre) === normalizar(b.nombre),
        campos: [
            { nombre: 'nombre', etiqueta: 'Nombre del servicio', ejemplo: 'Ej: Impresión a color', max: 30, requerido: true, ancho: true },
            { nombre: 'categoria', etiqueta: 'Tipo de servicio', ejemplo: 'Ej: Impresión', max: 30, requerido: true,
                sugerencias: (registros) => unicos([...CATEGORIAS_SERVICIO, ...registros.map((r) => r.categoria)]) },
            { nombre: 'precio', etiqueta: 'Precio', tipo: 'pesos', requerido: true },
            { nombre: 'descripcion', etiqueta: 'Descripción', ejemplo: 'Algo que ayude a explicarlo', max: 150, area: true }
        ],
        paraFormulario: (s) => s
    },

    productos: {
        icono: '✏️',
        titulo: 'Papelería',
        plural: 'productos',
        singular: 'producto',
        femenino: false,
        ayuda: 'Cuadernos, lapiceros, carpetas y todo lo que vendes.',
        buscarPor: 'nombre',
        ruta: '/api/productos',
        prefijoCodigo: 'PAP',
        buscarEn: (p) => [p.nombre, p.descripcion, p.disponible ? 'hay en la tienda disponible' : 'agotado'],
        tarjeta: (p) => ({
            nombre: p.nombre,
            precio: p.precio,
            estado: p.disponible ? { texto: 'Hay en la tienda', bien: true } : { texto: 'Agotado', bien: false },
            descripcion: p.descripcion
        }),
        mismoQue: (a, b) => normalizar(a.nombre) === normalizar(b.nombre),
        campos: [
            { nombre: 'nombre', etiqueta: 'Nombre del producto', ejemplo: 'Ej: Cuaderno cuadriculado', max: 30, requerido: true, ancho: true },
            { nombre: 'cantidad', etiqueta: '¿Cuántas unidades tienes?', tipo: 'cantidad', requerido: true, privado: true },
            { nombre: 'precioVenta', etiqueta: '¿A cuánto lo vendes?', tipo: 'pesos', requerido: true },
            { nombre: 'precioCompra', etiqueta: '¿A cuánto lo compraste?', tipo: 'pesos', requerido: true, privado: true },
            { nombre: 'lugarCompra', etiqueta: '¿Dónde lo compras?', ejemplo: 'Ej: Distribuidora del centro', max: 40, privado: true, ancho: true },
            { nombre: 'descripcion', etiqueta: 'Descripción', ejemplo: 'Marca, color, tamaño…', max: 150, area: true }
        ],
        // La lista pública no trae cantidad, precio de compra ni lugar de compra (son datos internos).
        notaAlCambiar: 'Por seguridad, la lista no muestra cuántas unidades tienes, a cuánto lo compraste ni dónde. Escríbelos otra vez en los recuadros amarillos.',
        paraFormulario: (p) => ({ codigo: p.codigo, nombre: p.nombre, precioVenta: p.precio, descripcion: p.descripcion }),
        // Vender más barato de lo que costó puede ser un error al escribir.
        advertencias: (c) => (c.precioCompra > c.precioVenta
            ? [`Lo vas a vender a ${pesos.format(c.precioVenta)} pero lo compraste a ${pesos.format(c.precioCompra)}: perderías plata.`]
            : [])
    },

    ropa: {
        icono: '👗',
        titulo: 'Ropa',
        plural: 'prendas',
        singular: 'prenda',
        femenino: true,
        ayuda: 'Las prendas de Pulguero Glam.',
        buscarPor: 'nombre, talla o color',
        ruta: '/api/ropa',
        prefijoCodigo: 'ROP',
        buscarEn: (r) => [r.nombre, r.tipo, `talla ${r.talla}`, r.color, r.material, r.descripcion],
        tarjeta: (r) => ({
            nombre: r.nombre || r.tipo,
            precio: r.precio,
            etiquetas: [r.tipo, r.talla && `Talla ${r.talla}`, r.color, r.material],
            descripcion: r.descripcion
        }),
        // La misma prenda puede estar en varias tallas: solo se avisa si coinciden nombre y talla.
        mismoQue: (a, b) => normalizar(a.nombre) === normalizar(b.nombre) && normalizar(a.talla) === normalizar(b.talla),
        campos: [
            // La base deja el nombre vacío, pero la página pública lo usa como título de la tarjeta.
            { nombre: 'nombre', etiqueta: 'Nombre de la prenda', ejemplo: 'Ej: Blusa de flores', max: 30, requerido: true, ancho: true },
            { nombre: 'tipoRopa', etiqueta: 'Tipo de prenda', ejemplo: 'Ej: Blusa', max: 20, requerido: true,
                sugerencias: (registros) => unicos(['Blusa', 'Camisa', 'Camiseta', 'Pantalón', 'Falda', 'Vestido', 'Chaqueta', 'Short', 'Saco', ...registros.map((r) => r.tipo)]) },
            { nombre: 'talla', etiqueta: 'Talla', ejemplo: 'Ej: M', max: 5, requerido: true, mayusculas: true,
                sugerencias: () => ['XS', 'S', 'M', 'L', 'XL', 'XXL', '2', '4', '6', '8', '10', '12', '14', '16'] },
            { nombre: 'color', etiqueta: 'Color', ejemplo: 'Ej: Rosado', max: 40 },
            { nombre: 'material', etiqueta: 'Material o tela', ejemplo: 'Ej: Algodón', max: 30, requerido: true,
                sugerencias: (registros) => unicos(['Algodón', 'Jean', 'Poliéster', 'Lino', 'Seda', 'Licra', ...registros.map((r) => r.material)]) },
            { nombre: 'precio', etiqueta: 'Precio', tipo: 'pesos', requerido: true },
            { nombre: 'descripcion', etiqueta: 'Descripción', ejemplo: 'Estado, detalles…', max: 150, area: true }
        ],
        paraFormulario: (r) => ({ ...r, tipoRopa: r.tipo })
    }
};

// El código lo exige la base de datos, pero a la administradora no le dice nada: se llena solo.
const CAMPO_CODIGO = { nombre: 'codigo', etiqueta: 'Código interno', max: 20, requerido: true, codigo: true,
    ayuda: 'Se llena solo. Cámbialo solo si te lo piden.' };

/* ---------- Estado ---------- */

const $ = (id) => document.getElementById(id);
const datos = {};           // registros cargados por sección (undefined = cargando, null = sin conexión)
let seccionActual = 'servicios';
let editando = null;        // registro que se está cambiando, o null si es nuevo
let porQuitar = null;       // registro que se va a quitar
let accionPendiente = null; // lo que se reintenta después de escribir la llave
let formularioCambiado = false;
let advertenciasVistas = ''; // avisos que la persona ya vio y aceptó guardar igual

/* ---------- Utilidades ---------- */

function unicos(lista) {
    const vistos = new Map();
    for (const valor of lista) {
        if (valor && !vistos.has(normalizar(valor))) vistos.set(normalizar(valor), valor);
    }
    return [...vistos.values()];
}

function normalizar(texto) {
    return String(texto ?? '').normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase().trim();
}

/* Quita espacios de más: "  blusa   de  flores " pasa a "blusa de flores". */
function limpiarTexto(texto) {
    return String(texto).replace(/\s+/g, ' ').trim();
}

function primeraMayuscula(texto) {
    return texto ? texto.charAt(0).toUpperCase() + texto.slice(1) : texto;
}

/* Textos con el género de la sección: "el primero" / "la primera", "este" / "esta". */
function primero(seccion) {
    return seccion.femenino ? 'la primera' : 'el primero';
}

function este(seccion) {
    return seccion.femenino ? `esta ${seccion.singular}` : `este ${seccion.singular}`;
}

function leerLlave() {
    try { return localStorage.getItem(LLAVE_ALMACEN) || ''; } catch { return ''; }
}

function guardarLlave(valor) {
    try {
        if (valor) localStorage.setItem(LLAVE_ALMACEN, valor);
        else localStorage.removeItem(LLAVE_ALMACEN);
    } catch { /* sin almacenamiento la llave se pide cada vez */ }
}

/* Convierte lo que escribe la persona en pesos enteros. En Colombia no se usan centavos, así que
   el punto y la coma solo valen como separadores de miles: "1.500", "1,500" y "$ 1500" valen 1500.
   "1.5" o "2.000,50" se rechazan en vez de adivinar: podrían ser 15, 1.500 o centavos.
   Devuelve NaN si no es un número escrito de forma clara. */
function leerEntero(texto) {
    const limpio = String(texto).replace(/[$\s]/g, '');
    if (/^\d+$/.test(limpio)) return Number(limpio);
    if (/^\d{1,3}(\.\d{3})+$/.test(limpio) || /^\d{1,3}(,\d{3})+$/.test(limpio)) {
        return Number(limpio.replace(/[.,]/g, ''));
    }
    return NaN;
}

function avisar(texto, tipo = 'bien') {
    const aviso = $('aviso-flotante');
    aviso.textContent = texto;
    aviso.className = 'aviso-flotante aviso-flotante--visible' + (tipo === 'error' ? ' aviso-flotante--error' : '');
    clearTimeout(avisar.temporizador);
    avisar.temporizador = setTimeout(() => { aviso.className = 'aviso-flotante'; }, tipo === 'error' ? 6000 : 3800);
}

/* ---------- Comunicación con el sistema ---------- */

class ErrorDelSistema extends Error {
    constructor(contenido, estado) {
        super(contenido?.detail || contenido?.title || '');
        this.estado = estado;
        this.titulo = contenido?.title || '';
        this.errores = contenido?.errores || {}; // errores por campo cuando la API rechaza los datos (400)
    }
}

async function llamar(ruta, { metodo = 'GET', cuerpo } = {}) {
    const cabeceras = { Accept: 'application/json' };
    if (cuerpo !== undefined) cabeceras['Content-Type'] = 'application/json';
    if (metodo !== 'GET') cabeceras['X-API-Key'] = leerLlave();

    const control = new AbortController();
    const temporizador = setTimeout(() => control.abort(), CONFIG.tiempoMaximoMs);
    let respuesta;
    try {
        respuesta = await fetch(CONFIG.apiUrl + ruta, {
            method: metodo,
            headers: cabeceras,
            body: cuerpo === undefined ? undefined : JSON.stringify(cuerpo),
            signal: control.signal
        });
    } catch {
        throw new ErrorDelSistema(null, 0);
    } finally {
        clearTimeout(temporizador);
    }

    if (respuesta.status === 204) return null;
    const contenido = await respuesta.json().catch(() => null);
    if (!respuesta.ok) throw new ErrorDelSistema(contenido, respuesta.status);
    return contenido;
}

/* ---------- Saludo y secciones ---------- */

function dibujarSaludo() {
    const hora = new Date().getHours();
    $('saludo-texto').textContent = hora < 12 ? '¡Buenos días!' : hora < 19 ? '¡Buenas tardes!' : '¡Buenas noches!';
    $('fecha-hoy').textContent = new Date().toLocaleDateString('es-CO', { weekday: 'long', day: 'numeric', month: 'long' });
}

function contarTexto(seccion, cantidad) {
    return cantidad === 1 ? `1 ${seccion.singular}` : `${cantidad} ${seccion.plural}`;
}

function dibujarMosaicos() {
    const contenedor = $('mosaicos');
    contenedor.replaceChildren();
    for (const [clave, seccion] of Object.entries(SECCIONES)) {
        const boton = document.createElement('button');
        boton.type = 'button';
        boton.className = 'mosaico';
        if (clave === seccionActual) boton.setAttribute('aria-current', 'page');
        boton.addEventListener('click', () => elegirSeccion(clave));

        const icono = document.createElement('span');
        icono.className = 'mosaico__icono';
        icono.setAttribute('aria-hidden', 'true');
        icono.textContent = seccion.icono;

        const textos = document.createElement('span');
        const titulo = document.createElement('span');
        titulo.className = 'mosaico__titulo';
        titulo.textContent = seccion.titulo;
        const detalle = document.createElement('span');
        detalle.className = 'mosaico__detalle';
        const lista = datos[clave];
        detalle.textContent = lista === undefined ? 'Cargando…' : lista === null ? 'Sin conexión' : contarTexto(seccion, lista.length);
        textos.append(titulo, detalle);

        boton.append(icono, textos);
        contenedor.append(boton);
    }
}

function elegirSeccion(clave) {
    seccionActual = clave;
    try { sessionStorage.setItem(SECCION_ALMACEN, clave); } catch { /* solo es una comodidad */ }
    const seccion = SECCIONES[clave];
    $('seccion-titulo').textContent = seccion.titulo;
    $('seccion-ayuda').textContent = seccion.ayuda;
    $('boton-agregar-texto').textContent = `Agregar ${seccion.singular}`;
    $('buscar').value = '';
    $('buscar').placeholder = `Buscar por ${seccion.buscarPor}…`;
    dibujarMosaicos();
    dibujarTarjetas();
}

/* ---------- Carga ---------- */

async function cargar(clave) {
    try {
        datos[clave] = await llamar(SECCIONES[clave].ruta);
    } catch {
        datos[clave] = null;
    }
    dibujarMosaicos();
    if (clave === seccionActual) dibujarTarjetas();
}

function recargar(clave) {
    datos[clave] = undefined;
    dibujarMosaicos();
    if (clave === seccionActual) dibujarTarjetas();
    return cargar(clave);
}

function cargarTodo() {
    const claves = Object.keys(SECCIONES);
    for (const clave of claves) datos[clave] = undefined;
    dibujarTarjetas();
    // Si el sistema estaba dormido (Render gratis), se avisa para que nadie piense que se dañó.
    const despertando = setTimeout(() => {
        if (datos[seccionActual] === undefined) {
            $('contador').textContent = 'Estamos despertando el sistema. La primera vez del día puede tardar hasta un minuto ☕';
        }
    }, 4000);
    Promise.all(claves.map(cargar)).finally(() => clearTimeout(despertando));
}

/* ---------- Tarjetas ---------- */

function mostrarEstado({ icono, titulo, texto, boton }) {
    $('estado').hidden = false;
    $('estado-icono').textContent = icono;
    $('estado-titulo').textContent = titulo;
    $('estado-texto').textContent = texto;
    const accion = $('estado-boton');
    accion.hidden = !boton;
    if (boton) {
        accion.textContent = boton.texto;
        accion.onclick = boton.accion;
    }
}

/* Cada palabra buscada debe ser el comienzo de alguna palabra del registro: "blusa m" encuentra la
   blusa talla M, pero la "m" no se cuela en "camisa" o "algodón". */
function coincide(seccion, registro, palabras) {
    const propias = normalizar(seccion.buscarEn(registro).filter(Boolean).join(' ')).split(/[\s,.;:()/-]+/);
    return palabras.every((palabra) => propias.some((propia) => propia.startsWith(palabra)));
}

function dibujarTarjetas() {
    const seccion = SECCIONES[seccionActual];
    const contenedor = $('tarjetas');
    const contador = $('contador');
    contenedor.replaceChildren();
    $('estado').hidden = true;

    const lista = datos[seccionActual];

    if (lista === undefined) {
        contenedor.setAttribute('aria-busy', 'true');
        contador.textContent = 'Cargando…';
        for (let i = 0; i < 3; i++) {
            const esqueleto = document.createElement('div');
            esqueleto.className = 'tarjeta tarjeta--cargando';
            contenedor.append(esqueleto);
        }
        return;
    }
    contenedor.setAttribute('aria-busy', 'false');

    if (lista === null) {
        contador.textContent = '';
        mostrarEstado({
            icono: '📡',
            titulo: 'No pudimos conectar con el sistema',
            texto: 'Revisa que haya internet. Si sigue pasando, avísale a Osvaldo.',
            boton: { texto: 'Intentar de nuevo', accion: () => recargar(seccionActual) }
        });
        return;
    }

    if (lista.length === 0) {
        contador.textContent = '';
        mostrarEstado({
            icono: seccion.icono,
            titulo: `Todavía no hay ${seccion.plural}`,
            texto: `Toca «Agregar ${seccion.singular}» para poner ${primero(seccion)}.`,
            boton: { texto: `Agregar ${seccion.singular}`, accion: () => abrirFormulario() }
        });
        return;
    }

    const palabras = normalizar($('buscar').value).split(/\s+/).filter(Boolean);
    const visibles = palabras.length ? lista.filter((r) => coincide(seccion, r, palabras)) : lista;

    if (visibles.length === 0) {
        contador.textContent = '';
        mostrarEstado({
            icono: '🔍',
            titulo: 'No encontramos nada con esa búsqueda',
            texto: 'Revisa cómo está escrito o borra la búsqueda.',
            boton: { texto: 'Borrar búsqueda', accion: () => { $('buscar').value = ''; dibujarTarjetas(); $('buscar').focus(); } }
        });
        return;
    }

    contador.textContent = palabras.length
        ? `Encontramos ${contarTexto(seccion, visibles.length)} de ${lista.length}`
        : `Tienes ${contarTexto(seccion, lista.length)}`;

    for (const registro of visibles) {
        contenedor.append(crearTarjeta(registro, seccion));
    }
}

function crearTarjeta(registro, seccion) {
    const info = seccion.tarjeta(registro);
    const tarjeta = document.createElement('article');
    tarjeta.className = 'tarjeta';

    const nombre = document.createElement('h3');
    nombre.className = 'tarjeta__nombre';
    nombre.textContent = info.nombre || 'Sin nombre';

    const precio = document.createElement('p');
    precio.className = 'tarjeta__precio';
    precio.textContent = typeof info.precio === 'number' ? pesos.format(info.precio) : 'Sin precio';

    tarjeta.append(nombre, precio);

    const etiquetas = document.createElement('div');
    etiquetas.className = 'etiquetas';
    if (info.estado) {
        etiquetas.append(crearEtiqueta(info.estado.texto, info.estado.bien ? 'etiqueta--bien' : 'etiqueta--mal'));
    }
    for (const texto of (info.etiquetas || []).filter(Boolean)) {
        etiquetas.append(crearEtiqueta(texto));
    }
    if (etiquetas.childElementCount) tarjeta.append(etiquetas);

    if (info.descripcion) {
        const descripcion = document.createElement('p');
        descripcion.className = 'tarjeta__descripcion';
        descripcion.textContent = info.descripcion;
        tarjeta.append(descripcion);
    }

    const acciones = document.createElement('div');
    acciones.className = 'tarjeta__acciones';
    const nombreVisible = info.nombre || seccion.singular;
    acciones.append(
        crearBoton('✏️ Cambiar', 'boton boton--suave', `Cambiar ${nombreVisible}`, () => abrirFormulario(registro)),
        crearBoton('🗑️ Quitar', 'boton boton--peligro-suave', `Quitar ${nombreVisible}`, () => pedirQuitar(registro))
    );
    tarjeta.append(acciones);
    return tarjeta;
}

function crearEtiqueta(texto, extra = '') {
    const etiqueta = document.createElement('span');
    etiqueta.className = `etiqueta ${extra}`.trim();
    etiqueta.textContent = texto;
    return etiqueta;
}

function crearBoton(texto, clase, nombreAccesible, accion) {
    const boton = document.createElement('button');
    boton.type = 'button';
    boton.className = clase;
    boton.textContent = texto;
    boton.setAttribute('aria-label', nombreAccesible);
    boton.addEventListener('click', accion);
    return boton;
}

/* ---------- Formulario ---------- */

function crearCampo(campo, valor, registros, resaltar) {
    const envoltorio = document.createElement('div');
    envoltorio.className = 'campo';
    if (campo.ancho || campo.area) envoltorio.classList.add('campo--ancho');
    if (resaltar) envoltorio.classList.add('campo--resaltado');

    const id = `campo-${campo.nombre}`;
    const etiqueta = document.createElement('label');
    etiqueta.className = 'campo__etiqueta';
    etiqueta.htmlFor = id;
    etiqueta.textContent = campo.etiqueta + ' ';
    if (!campo.requerido) {
        const opcional = document.createElement('span');
        opcional.className = 'campo__opcional';
        opcional.textContent = '(opcional)';
        etiqueta.append(opcional);
    }

    const entrada = document.createElement(campo.area ? 'textarea' : 'input');
    entrada.className = 'campo__entrada';
    entrada.id = id;
    entrada.name = campo.nombre;
    if (campo.area) entrada.rows = 3;
    else entrada.type = 'text';
    if (campo.max) entrada.maxLength = campo.max;
    if (campo.ejemplo) entrada.placeholder = campo.ejemplo;

    const caja = document.createElement('div');
    caja.className = 'campo__caja';

    if (campo.tipo === 'pesos' || campo.tipo === 'cantidad') {
        entrada.inputMode = 'numeric';
        entrada.autocomplete = 'off';
        if (campo.tipo === 'pesos') {
            const prefijo = document.createElement('span');
            prefijo.className = 'campo__prefijo';
            prefijo.setAttribute('aria-hidden', 'true');
            prefijo.textContent = '$';
            caja.append(prefijo);
            entrada.classList.add('campo__entrada--con-prefijo');
            entrada.placeholder = 'Ej: 1.500';
            // Al salir del recuadro se ponen los puntos de miles, para que se lea fácil y se note un cero de más.
            entrada.addEventListener('blur', () => {
                const numero = leerEntero(entrada.value);
                if (Number.isFinite(numero)) entrada.value = miles.format(numero);
            });
        } else {
            entrada.placeholder = 'Ej: 20';
        }
        entrada.value = typeof valor === 'number' ? miles.format(Math.round(valor)) : '';
    } else {
        entrada.value = valor ?? '';
    }

    if (campo.sugerencias) {
        const lista = document.createElement('datalist');
        lista.id = `${id}-sugerencias`;
        for (const opcion of campo.sugerencias(registros)) {
            const item = document.createElement('option');
            item.value = opcion;
            lista.append(item);
        }
        entrada.setAttribute('list', lista.id);
        caja.append(lista);
    }

    caja.prepend(entrada);
    envoltorio.append(etiqueta, caja);

    if (campo.ayuda) {
        const ayuda = document.createElement('p');
        ayuda.className = 'campo__ayuda';
        ayuda.textContent = campo.ayuda;
        envoltorio.append(ayuda);
    }

    // En los textos largos se muestra cuánto queda, para que nada se corte sin aviso.
    if (campo.area && campo.max) {
        const cuenta = document.createElement('p');
        cuenta.className = 'campo__ayuda campo__cuenta';
        const actualizar = () => { cuenta.textContent = `${entrada.value.length} de ${campo.max} letras`; };
        entrada.addEventListener('input', actualizar);
        actualizar();
        envoltorio.append(cuenta);
    }

    const error = document.createElement('p');
    error.className = 'campo__error';
    error.id = `${id}-error`;
    error.hidden = true;
    envoltorio.append(error);
    entrada.setAttribute('aria-describedby', error.id);
    entrada.addEventListener('input', () => marcarError(campo.nombre, ''));

    return envoltorio;
}

function marcarError(nombre, mensaje) {
    const entrada = $(`campo-${nombre}`);
    const error = $(`campo-${nombre}-error`);
    if (!entrada || !error) return;
    error.textContent = mensaje;
    error.hidden = !mensaje;
    if (mensaje) entrada.setAttribute('aria-invalid', 'true');
    else entrada.removeAttribute('aria-invalid');
}

function abrirFormulario(registro = null) {
    const seccion = SECCIONES[seccionActual];
    editando = registro;
    const valores = registro ? seccion.paraFormulario(registro) : {};
    const nombreVisible = registro ? seccion.tarjeta(registro).nombre : '';

    $('formulario-titulo').textContent = registro ? `Cambiar «${nombreVisible || seccion.singular}»` : `Agregar ${seccion.singular}`;
    const nota = $('formulario-nota');
    const conNota = Boolean(registro && seccion.notaAlCambiar);
    nota.textContent = conNota ? seccion.notaAlCambiar : '';
    nota.hidden = !conNota;
    $('formulario-error').textContent = '';

    const registros = datos[seccionActual] || [];
    const campos = $('campos');
    campos.replaceChildren(...seccion.campos.map((c) => crearCampo(c, valores[c.nombre], registros, conNota && c.privado)));

    const codigo = valores.codigo || `${seccion.prefijoCodigo}-${Date.now().toString(36).toUpperCase().slice(-6)}`;
    $('campos-avanzados').replaceChildren(crearCampo(CAMPO_CODIGO, codigo, registros, false));
    $('avanzado').open = false;

    formularioCambiado = false;
    advertenciasVistas = '';
    $('ventana-formulario').showModal();
    campos.querySelector('input, textarea')?.focus();
}

/* Pone el texto como ya se escribe en el sistema: si "impresion" coincide con la sugerencia "Impresión",
   se guarda "Impresión". Así no quedan "Blusa", "blusa" y "BLUSA" como tres tipos distintos. */
function ajustarTexto(campo, texto, registros) {
    let valor = limpiarTexto(texto);
    if (!valor) return valor;
    if (campo.codigo) return valor.replace(/\s+/g, '-').toUpperCase();
    if (campo.mayusculas) return valor.toUpperCase();
    if (campo.sugerencias) {
        const igual = campo.sugerencias(registros).find((s) => normalizar(s) === normalizar(valor));
        if (igual) return igual;
        return primeraMayuscula(valor);
    }
    return campo.area ? valor : primeraMayuscula(valor);
}

/* Revisa el formulario. Devuelve el cuerpo listo para enviar, o null si hay errores (ya marcados). */
function leerFormulario(seccion) {
    const formulario = $('formulario');
    const registros = datos[seccionActual] || [];
    const cuerpo = {};
    let primerError = null;

    for (const campo of [...seccion.campos, CAMPO_CODIGO]) {
        const entrada = formulario.elements[campo.nombre];
        const crudo = entrada.value;
        let mensaje = '';

        if (campo.tipo === 'pesos' || campo.tipo === 'cantidad') {
            const texto = crudo.trim();
            const numero = leerEntero(texto);
            if (texto === '') {
                if (campo.requerido) mensaje = 'Este dato hace falta.';
                else cuerpo[campo.nombre] = null;
            } else if (!Number.isFinite(numero)) {
                mensaje = campo.tipo === 'pesos'
                    ? 'Escribe el precio completo, sin centavos. Por ejemplo 1.500 o 1500'
                    : 'Escribe solo números. Por ejemplo 20';
            } else if (campo.tipo === 'pesos' && numero === 0) {
                mensaje = 'El precio no puede ser cero.';
            } else if (campo.tipo === 'pesos' && numero > PRECIO_MAXIMO) {
                mensaje = `Ese precio parece muy alto (${pesos.format(numero)}). Revisa si sobra algún cero.`;
            } else if (campo.tipo === 'cantidad' && numero > CANTIDAD_MAXIMA) {
                mensaje = `Son demasiadas unidades (${miles.format(numero)}). Revisa si sobra algún cero.`;
            } else {
                cuerpo[campo.nombre] = numero;
            }
        } else {
            const texto = ajustarTexto(campo, crudo, registros);
            entrada.value = texto;
            if (campo.requerido && texto === '') mensaje = 'Este dato hace falta.';
            else if (campo.max && texto.length > campo.max) mensaje = `Es muy largo: máximo ${campo.max} letras.`;
            else cuerpo[campo.nombre] = texto === '' ? null : texto;
        }

        marcarError(campo.nombre, mensaje);
        if (mensaje && !primerError) primerError = campo;
    }

    if (primerError) {
        if (primerError === CAMPO_CODIGO) $('avanzado').open = true;
        $(`campo-${primerError.nombre}`).focus();
        $('formulario-error').textContent = 'Revisa los recuadros marcados en rojo.';
        return null;
    }
    $('formulario-error').textContent = '';
    return cuerpo;
}

/* Cosas que no son errores pero conviene confirmar: un nombre repetido o un precio de venta menor al de compra. */
function buscarAdvertencias(seccion, cuerpo) {
    const avisos = [];
    const comparable = { ...cuerpo, tipo: cuerpo.tipoRopa };
    const repetido = (datos[seccionActual] || []).find((r) => r.id !== editando?.id && seccion.mismoQue(r, comparable));
    if (repetido) {
        avisos.push(`Ya tienes ${seccion.femenino ? 'una' : 'uno'} que se llama «${seccion.tarjeta(repetido).nombre}»${repetido.talla ? ` en talla ${repetido.talla}` : ''}.`);
    }
    if (seccion.advertencias) avisos.push(...seccion.advertencias(cuerpo));
    return avisos;
}

async function guardar() {
    const seccion = SECCIONES[seccionActual];
    const cuerpo = leerFormulario(seccion);
    if (!cuerpo) return;

    // La primera vez se muestran los avisos; si la persona toca Guardar otra vez, se guarda igual.
    const avisos = buscarAdvertencias(seccion, cuerpo);
    const clave = avisos.join('|');
    if (avisos.length && clave !== advertenciasVistas) {
        advertenciasVistas = clave;
        $('formulario-error').textContent = `${avisos.join(' ')} Si está bien así, toca «Guardar» otra vez.`;
        return;
    }

    const boton = $('boton-guardar');
    boton.disabled = true;
    boton.textContent = 'Guardando…';
    try {
        if (editando) await llamar(`${seccion.ruta}/${editando.id}`, { metodo: 'PUT', cuerpo });
        else await llamar(seccion.ruta, { metodo: 'POST', cuerpo });

        formularioCambiado = false;
        $('ventana-formulario').close();
        avisar(`✓ ¡Listo! Se guardó «${cuerpo.nombre}».`);
        await cargar(seccionActual);
    } catch (error) {
        manejarErrorAlGuardar(error);
    } finally {
        boton.disabled = false;
        boton.textContent = 'Guardar';
    }
}

function manejarErrorAlGuardar(error) {
    const caja = $('formulario-error');
    if (error.estado === 401) {
        pedirLlaveOtraVez(guardar);
        return;
    }
    if (error.estado === 400 && Object.keys(error.errores).length) {
        // La API dice qué campo está mal: se marca ese recuadro con su mensaje.
        let primerCampo = null;
        for (const [nombre, mensaje] of Object.entries(error.errores)) {
            marcarError(nombre, mensaje);
            if (!primerCampo && $(`campo-${nombre}`)) primerCampo = nombre;
        }
        if (primerCampo === 'codigo') $('avanzado').open = true;
        if (primerCampo) $(`campo-${primerCampo}`).focus();
        caja.textContent = 'Revisa los recuadros marcados en rojo.';
        return;
    }
    if (error.estado === 409 && error.titulo === 'Código repetido') {
        $('avanzado').open = true;
        marcarError('codigo', 'Ya hay otro con este mismo código. Cámbialo por uno distinto.');
        $('campo-codigo').focus();
        caja.textContent = 'Revisa el código interno en «Opciones avanzadas».';
        return;
    }
    if (error.estado === 404) {
        caja.textContent = 'Esto ya no existe: puede que alguien lo haya quitado. Cierra esta ventana; la lista se actualizó.';
        recargar(seccionActual);
        return;
    }
    caja.textContent = mensajeGeneral(error);
}

function mensajeGeneral(error) {
    if (error.estado === 0) return 'No pudimos conectar con el sistema. Revisa el internet e intenta otra vez.';
    if (error.estado === 400) return 'Hay algo que no está bien escrito. Revisa los datos e intenta otra vez.';
    return 'Algo salió mal. Intenta otra vez en un momento; si sigue pasando, avísale a Osvaldo.';
}

/* ---------- Quitar ---------- */

function pedirQuitar(registro) {
    const seccion = SECCIONES[seccionActual];
    porQuitar = registro;
    const nombre = seccion.tarjeta(registro).nombre;
    $('quitar-titulo').textContent = nombre ? `¿Quitar «${nombre}»?` : `¿Quitar ${este(seccion)}?`;
    $('ventana-quitar').showModal();
    $('ventana-quitar').querySelector('[data-cerrar]').focus();
}

async function quitar() {
    const seccion = SECCIONES[seccionActual];
    const registro = porQuitar;
    if (!registro) return;
    const nombre = seccion.tarjeta(registro).nombre || seccion.singular;
    const boton = $('boton-confirmar-quitar');
    boton.disabled = true;
    try {
        await llamar(`${seccion.ruta}/${registro.id}`, { metodo: 'DELETE' });
        $('ventana-quitar').close();
        avisar(`✓ Se quitó «${nombre}».`);
        await cargar(seccionActual);
    } catch (error) {
        $('ventana-quitar').close();
        if (error.estado === 401) {
            pedirLlaveOtraVez(() => { porQuitar = registro; quitar(); });
        } else if (error.estado === 404) {
            // Ya no estaba: el resultado es el mismo que se quería.
            avisar(`«${nombre}» ya no estaba en la lista.`);
            recargar(seccionActual);
        } else if (error.estado === 409) {
            avisar(`No se puede quitar «${nombre}» porque ya aparece en facturas o ventas.`, 'error');
        } else {
            avisar(mensajeGeneral(error), 'error');
        }
    } finally {
        boton.disabled = false;
    }
}

/* ---------- Llave ---------- */

function pedirLlaveOtraVez(reintentar) {
    const habiaLlave = Boolean(leerLlave());
    guardarLlave('');
    pedirLlave(reintentar, habiaLlave ? 'Esa llave no funcionó. Revísala e intenta otra vez.' : '');
}

function pedirLlave(reintentar, mensaje = '') {
    accionPendiente = reintentar;
    $('llave').value = '';
    $('llave-error').textContent = mensaje;
    $('ventana-llave').showModal();
    $('llave').focus();
}

$('formulario-llave').addEventListener('submit', (evento) => {
    evento.preventDefault();
    const valor = $('llave').value.trim();
    if (!valor) {
        $('llave-error').textContent = 'Escribe la llave para continuar.';
        return;
    }
    guardarLlave(valor);
    $('ventana-llave').close();
    const accion = accionPendiente;
    accionPendiente = null;
    if (accion) accion();
});

$('ventana-llave').addEventListener('close', () => { accionPendiente = null; });

/* ---------- Salir sin guardar ---------- */

function cerrarFormulario() {
    if (formularioCambiado && !window.confirm('¿Salir sin guardar? Lo que escribiste se va a perder.')) return;
    formularioCambiado = false;
    $('ventana-formulario').close();
}

$('formulario').addEventListener('input', () => {
    formularioCambiado = true;
    advertenciasVistas = ''; // si cambió algo, los avisos se vuelven a mostrar
});

// La tecla Esc también cierra la ventana: se pregunta antes si hay cambios.
$('ventana-formulario').addEventListener('cancel', (evento) => {
    evento.preventDefault();
    cerrarFormulario();
});

/* ---------- Eventos generales ---------- */

$('formulario').addEventListener('submit', (evento) => {
    evento.preventDefault();
    if (!$('boton-guardar').disabled) guardar();
});

$('boton-confirmar-quitar').addEventListener('click', quitar);
$('boton-agregar').addEventListener('click', () => abrirFormulario());
$('buscar').addEventListener('input', dibujarTarjetas);

for (const boton of document.querySelectorAll('[data-cerrar]')) {
    const ventana = boton.closest('dialog');
    boton.addEventListener('click', () => (ventana.id === 'ventana-formulario' ? cerrarFormulario() : ventana.close()));
}

/* ---------- Inicio ---------- */

try {
    const guardada = sessionStorage.getItem(SECCION_ALMACEN);
    if (guardada && SECCIONES[guardada]) seccionActual = guardada;
} catch { /* se empieza en servicios */ }

dibujarSaludo();
elegirSeccion(seccionActual);
cargarTodo();
