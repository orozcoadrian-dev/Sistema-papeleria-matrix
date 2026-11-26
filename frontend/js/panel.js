'use strict';

/* Panel de la administradora: servicios, papelería y ropa.
   Lo usa una persona que no es técnica, así que los textos son cotidianos ("Agregar", "Cambiar",
   "Quitar"), los códigos se llenan solos y cada error dice qué hacer.
   Cada sección se describe una vez en SECCIONES; el resto del código sirve para todas.
   Los datos se insertan con textContent, nunca con innerHTML, para que nada guardado ejecute código.
   Solo entra la administradora: sin sesión se va al inicio de sesión y el operador va a la pantalla de ventas. */

const SESION = Sesion.exigir(['ADMINISTRADOR']);
const SECCION_ALMACEN = 'matrix.seccion';

// Con menos de estas unidades se avisa que el producto se está acabando.
const POCAS_UNIDADES = 5;

// Topes para atrapar ceros de más al escribir. La base acepta más, pero estos valores no son normales en el negocio.
const PRECIO_MAXIMO = 10000000;
const CANTIDAD_MAXIMA = 100000;

const pesos = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 });
const miles = new Intl.NumberFormat('es-CO', { maximumFractionDigits: 0 });

// Las mismas categorías que usa la página pública para elegir el dibujo de cada servicio (js/main.js).
const CATEGORIAS_SERVICIO = ['Impresión', 'Copias', 'Escáner', 'Transcripción'];

const SECCIONES = {
    servicios: {
        destacable: true,
        pideMotivo: true,
        textoQuitar: 'Dejará de verse en la página y ya no se podrá vender. Las ventas anteriores se conservan.',
        icono: '🖨️',
        titulo: 'Servicios',
        plural: 'servicios',
        singular: 'servicio',
        femenino: false,
        ayuda: 'Lo que ofreces en el cibercafé: impresiones, copias, escáner…',
        buscarPor: 'nombre o tipo',
        ruta: '/api/servicios',
        fotos: 'servicios',
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
        destacable: true,
        pideMotivo: true,
        textoQuitar: 'Dejará de verse en la página y ya no se podrá vender. Las ventas anteriores se conservan.',
        icono: '✏️',
        titulo: 'Papelería',
        plural: 'productos',
        singular: 'producto',
        femenino: false,
        ayuda: 'Cuadernos, lapiceros, carpetas y todo lo que vendes.',
        buscarPor: 'nombre',
        ruta: '/api/productos',
        // La lista interna trae las unidades, el precio de compra y el lugar de compra.
        rutaLista: '/api/productos/interno',
        fotos: 'productos',
        prefijoCodigo: 'PAP',
        buscarEn: (p) => [p.nombre, p.descripcion, p.lugarCompra, p.cantidad > 0 ? (p.cantidad < POCAS_UNIDADES ? 'pocas quedan' : 'hay disponible') : 'agotado'],
        tarjeta: (p) => ({
            nombre: p.nombre,
            precio: p.precio,
            estado: existencias(p.cantidad),
            etiquetas: [p.lugarCompra],
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
        paraFormulario: (p) => ({ ...p, precioVenta: p.precio }),
        // Al cambiar, se manda cuántas unidades había al abrir el formulario: si mientras tanto se vendió algo, no se pisa.
        cuerpoExtra: (registro) => (registro ? { cantidadAnterior: registro.cantidad } : {}),
        // Vender más barato de lo que costó puede ser un error al escribir.
        advertencias: (c) => (c.precioCompra > c.precioVenta
            ? [`Lo vas a vender a ${pesos.format(c.precioVenta)} pero lo compraste a ${pesos.format(c.precioCompra)}: perderías plata.`]
            : [])
    },

    ropa: {
        destacable: true,
        pideMotivo: true,
        textoQuitar: 'Dejará de verse en la página y ya no se podrá vender. Las ventas anteriores se conservan.',
        icono: '👗',
        titulo: 'Ropa',
        plural: 'prendas',
        singular: 'prenda',
        femenino: true,
        ayuda: 'Las prendas de Pulguero Glam.',
        buscarPor: 'nombre, talla o color',
        ruta: '/api/ropa',
        fotos: 'ropa',
        prefijoCodigo: 'ROP',
        buscarEn: (r) => [r.nombre, r.tipo, `talla ${r.talla}`, r.color, r.material, r.descripcion],
        tarjeta: (r) => ({
            nombre: r.nombre || r.tipo,
            precio: r.precio,
            // Cada prenda es única: cuando se vende, ya no está en la tienda.
            estado: r.disponible === false ? { texto: 'Vendida', bien: false } : { texto: 'Disponible', bien: true },
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
    },

    operadores: {
        icono: '🧑‍💻',
        titulo: 'Operadores',
        plural: 'operadores',
        singular: 'operador',
        femenino: false,
        ayuda: 'Las personas que atienden el cibercafé. Cada una entra con su usuario y registra sus ventas.',
        buscarPor: 'nombre o usuario',
        ruta: '/api/operadores-cibercafe',
        id: (o) => o.idUsuario,
        sinCodigo: true,
        buscarEn: (o) => [o.primerNombre, o.segundoNombre, o.primerApellido, o.segundoApellido, o.nombreUsuario, o.telefono],
        tarjeta: (o) => ({
            nombre: nombreCompleto(o),
            precio: o.salario,
            precioSufijo: ' al mes',
            estado: estadoDelOperador(o),
            etiquetas: [clavesDePermiso.has(o.idUsuario) ? '🔑 Tiene clave de permiso' : 'Sin clave de permiso',
                `Usuario: ${o.nombreUsuario}`, o.telefono && `Tel. ${o.telefono}`, o.horario],
            descripcion: o.direccion
        }),
        mismoQue: (a, b) => normalizar(a.nombreUsuario) === normalizar(b.nombreUsuario),
        campos: [
            { nombre: 'primerNombre', etiqueta: 'Primer nombre', ejemplo: 'Ej: Daniel', min: 3, max: 30, requerido: true, soloLetras: true },
            { nombre: 'segundoNombre', etiqueta: 'Segundo nombre', min: 3, max: 30, soloLetras: true },
            { nombre: 'primerApellido', etiqueta: 'Primer apellido', ejemplo: 'Ej: Pérez', min: 3, max: 30, requerido: true, soloLetras: true },
            { nombre: 'segundoApellido', etiqueta: 'Segundo apellido', ejemplo: 'Ej: Mora', min: 3, max: 30, requerido: true, soloLetras: true },
            { nombre: 'tipoDocumento', etiqueta: 'Tipo de documento', ejemplo: 'Ej: CC', max: 5, requerido: true, mayusculas: true,
                sugerencias: () => ['CC', 'CE', 'TI', 'PPT', 'PA'] },
            { nombre: 'numeroDocumento', etiqueta: 'Número de documento', min: 15, max: 20, requerido: true, soloNumeros: true,
                ayuda: 'El sistema pide entre 15 y 20 números.' },
            { nombre: 'nombreUsuario', etiqueta: 'Usuario para entrar', ejemplo: 'Ej: daniel.perez', min: 8, max: 15, requerido: true, minusculas: true,
                patron: { regla: /^[a-z0-9._-]+$/, mensaje: 'Usa solo letras sin tilde, números, punto o guion. Sin espacios.' } },
            { nombre: 'contrasenia', etiqueta: 'Contraseña', tipo: 'clave', min: 10, max: 255, requeridoAlCrear: true,
                ayuda: 'Mínimo 10 letras o números. Al cambiar un operador, déjala vacía para no cambiarla.' },
            { nombre: 'salario', etiqueta: 'Sueldo al mes', tipo: 'pesos', requerido: true },
            { nombre: 'fechaContrato', etiqueta: 'Fecha en que empezó', tipo: 'fecha', requerido: true },
            { nombre: 'telefono', etiqueta: 'Celular', ejemplo: 'Ej: 3001234567', min: 8, max: 15, soloNumeros: true },
            { nombre: 'email', etiqueta: 'Correo', ejemplo: 'Ej: daniel.perez@gmail.com', min: 20, max: 60, minusculas: true,
                patron: { regla: /^[\w.-]+@[\w.-]+\.com$/, mensaje: 'Escribe un correo completo que termine en .com' } },
            { nombre: 'horario', etiqueta: 'Horario', ejemplo: 'Ej: Lunes a sábado de 8 a 6', min: 10, max: 80, ancho: true },
            { nombre: 'direccion', etiqueta: 'Dirección de la casa', ejemplo: 'Ej: Calle 10 # 20-30, barrio Centro', min: 10, max: 80, requerido: true, ancho: true }
        ],
        valoresNuevos: () => ({ fechaContrato: hoyISO() }),
        cuerpoExtra: () => ({ rol: 'Operador de cibercafé' }),
        paraFormulario: (o) => ({ ...o, contrasenia: '' }),
        accionesExtra: accionesDelOperador,
        cargarExtra: cargarClavesDePermiso,
        // La primera opción es la que se ve al entrar: quienes siguen en el equipo.
        filtros: [
            { texto: 'En el equipo', pasa: (o) => o.estado !== 'Despedido' },
            { texto: 'Activos', pasa: (o) => o.estado === 'Activo' },
            { texto: 'Suspendidos', pasa: (o) => o.estado === 'Suspendido' },
            { texto: 'Despedidos', pasa: (o) => o.estado === 'Despedido' }
        ],
        // No se borran: «Retirar» los deja como despedidos y sus ventas quedan a su nombre.
        sinQuitar: true
    },

    informacion: {
        icono: '🏪',
        pideMotivo: true,
        textoQuitar: 'Dejará de verse en la página y en esta lista. Queda guardado en la base de datos.',
        titulo: 'Datos de la tienda',
        plural: 'datos',
        singular: 'dato',
        femenino: false,
        ayuda: 'Horarios, dirección, medios de pago… lo que quieras que se sepa de la tienda.',
        buscarPor: 'nombre o texto',
        ruta: '/api/informacion-negocio',
        id: (i) => i.idInformacion,
        sinCodigo: true,
        ordenar: (a, b) => (a.valorInformacion ?? 0) - (b.valorInformacion ?? 0),
        buscarEn: (i) => [i.tipoInformacion, i.informacion],
        tarjeta: (i) => ({
            nombre: i.tipoInformacion,
            etiquetas: [`Puesto ${i.valorInformacion}`],
            descripcion: i.informacion
        }),
        mismoQue: (a, b) => normalizar(a.tipoInformacion) === normalizar(b.tipoInformacion),
        campos: [
            { nombre: 'tipoInformacion', etiqueta: '¿Qué dato es?', ejemplo: 'Ej: Horario tienda', min: 10, max: 15, requerido: true, soloLetras: true, ancho: true,
                sugerencias: () => ['Horario tienda', 'Dirección local', 'Medios de pago', 'Servicio a casa', 'Días festivos'] },
            { nombre: 'informacion', etiqueta: 'Escribe el dato', ejemplo: 'Ej: Lunes a sábado de 8 a. m. a 7 p. m.', min: 10, max: 255,
                requerido: true, area: true },
            { nombre: 'valorInformacion', etiqueta: 'Puesto en la lista', tipo: 'cantidad', requerido: true, ayuda: 'El 1 sale primero.' }
        ],
        valoresNuevos: (registros) => ({ valorInformacion: registros.length + 1 }),
        cuerpoExtra: () => ({ usuarioId: SESION.idUsuario }),
        paraFormulario: (i) => i
    }
};

// El sistema solo acepta letras y espacios en los nombres de personas y en el tipo de dato de la tienda.
const SOLO_LETRAS = /^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$/;

// El código lo exige la base de datos, pero a la administradora no le dice nada: se llena solo.
const CAMPO_CODIGO = { nombre: 'codigo', etiqueta: 'Código interno', max: 20, requerido: true, codigo: true,
    ayuda: 'Se llena solo. Cámbialo solo si te lo piden.' };

function nombreCompleto(o) {
    return [o.primerNombre, o.segundoNombre, o.primerApellido, o.segundoApellido].filter(Boolean).join(' ');
}

function hoyISO() {
    const fecha = new Date();
    return `${fecha.getFullYear()}-${String(fecha.getMonth() + 1).padStart(2, '0')}-${String(fecha.getDate()).padStart(2, '0')}`;
}

/* El id de un registro: los operadores usan idUsuario y los datos de la tienda idInformacion. */
function idDe(seccion, registro) {
    return seccion.id ? seccion.id(registro) : registro.id;
}

function existencias(cantidad) {
    if (!(cantidad > 0)) return { texto: 'Agotado', bien: false };
    if (cantidad < POCAS_UNIDADES) return { texto: cantidad === 1 ? 'Queda 1' : `Quedan ${cantidad}: ya se acaba`, bien: false };
    return { texto: `Quedan ${miles.format(cantidad)}`, bien: true };
}

/* ---------- Estado ---------- */

const $ = (id) => document.getElementById(id);
const datos = {};           // registros cargados por sección (undefined = cargando, null = sin conexión)
let seccionActual = 'servicios';
let editando = null;        // registro que se está cambiando, o null si es nuevo
let porConfirmar = null;    // lo que hace el botón de la ventana de confirmar
let filtroActual = 0;       // opción elegida en las secciones que se pueden filtrar
let formularioInicial = ''; // lo que tenía el formulario al abrirlo
let fotoNueva = null;      // foto elegida en el formulario (ya achicada), lista para subir
let quitarFoto = false;    // se tocó «Quitar foto» en el formulario
const fallosDelServidor = new Set(); // secciones cuya lista el servidor no pudo armar (error 500)
const clavesDePermiso = new Map(); // id del operador → id de su clave de permiso
let operadorDeLaClave = null;      // operador al que se le pone o cambia la clave
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

// La sesión va en cada petición. Si se vence, sesion.js lleva al inicio de sesión.
const llamar = Sesion.llamar;

/* ---------- Saludo y secciones ---------- */

function dibujarSaludo() {
    const hora = new Date().getHours();
    const nombre = SESION.nombre.split(' ')[0];
    $('saludo-texto').textContent = (hora < 12 ? '¡Buenos días' : hora < 19 ? '¡Buenas tardes' : '¡Buenas noches') + `, ${nombre}!`;
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
        detalle.textContent = lista === undefined ? 'Cargando…'
            : lista === null ? (fallosDelServidor.has(clave) ? 'No se pudo abrir' : 'Sin conexión')
            : contarTexto(seccion, seccion.filtros ? lista.filter(seccion.filtros[0].pasa).length : lista.length);
        textos.append(titulo, detalle);

        boton.append(icono, textos);
        contenedor.append(boton);
    }
    contenedor.append(
        mosaicoEnlace('ventas.html', '🧾', 'Ventas', 'Vender y ver lo vendido'),
        mosaicoEnlace('cuentas.html', '📊', 'Cuentas', 'Cuánto entra y cuánto sale')
    );
}

/* Las ventas y las cuentas tienen su propia pantalla: estos mosaicos llevan allá. */
function mosaicoEnlace(ruta, simbolo, nombre, texto) {
    const enlace = document.createElement('a');
    enlace.className = 'mosaico mosaico--enlace';
    enlace.href = ruta;
    const icono = document.createElement('span');
    icono.className = 'mosaico__icono';
    icono.setAttribute('aria-hidden', 'true');
    icono.textContent = simbolo;
    const textos = document.createElement('span');
    const titulo = document.createElement('span');
    titulo.className = 'mosaico__titulo';
    titulo.textContent = nombre;
    const detalle = document.createElement('span');
    detalle.className = 'mosaico__detalle';
    detalle.textContent = texto;
    textos.append(titulo, detalle);
    enlace.append(icono, textos);
    return enlace;
}

function elegirSeccion(clave) {
    seccionActual = clave;
    filtroActual = 0;
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
        const seccion = SECCIONES[clave];
        const [lista] = await Promise.all([llamar(seccion.rutaLista || seccion.ruta), seccion.cargarExtra?.()]);
        datos[clave] = seccion.ordenar ? [...lista].sort(seccion.ordenar) : lista;
        fallosDelServidor.delete(clave);
    } catch (error) {
        datos[clave] = null;
        if (error.estado >= 500) fallosDelServidor.add(clave);
        else fallosDelServidor.delete(clave);
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
    // Las fotos llegan aparte: cuando están, se vuelven a dibujar las tarjetas.
    Fotos.cargar().then(dibujarTarjetas);
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
    dibujarFiltros(seccion, lista);

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

    if (lista === null && fallosDelServidor.has(seccionActual)) {
        contador.textContent = '';
        mostrarEstado({
            icono: '🛠️',
            titulo: 'Esta lista no se pudo abrir',
            texto: 'El sistema tuvo un problema al traerla. No es el internet: avísale a Osvaldo para que lo revise.',
            boton: { texto: 'Intentar de nuevo', accion: () => recargar(seccionActual) }
        });
        return;
    }

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

    const filtradas = seccion.filtros ? lista.filter(seccion.filtros[filtroActual].pasa) : lista;
    if (filtradas.length === 0) {
        contador.textContent = '';
        mostrarEstado({
            icono: seccion.icono,
            titulo: `No hay nadie en «${seccion.filtros[filtroActual].texto}»`,
            texto: 'Toca otra de las opciones de arriba para ver a las demás personas.'
        });
        return;
    }

    const palabras = normalizar($('buscar').value).split(/\s+/).filter(Boolean);
    const visibles = palabras.length ? filtradas.filter((r) => coincide(seccion, r, palabras)) : filtradas;

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
        ? `Encontramos ${contarTexto(seccion, visibles.length)} de ${filtradas.length}`
        : `Tienes ${contarTexto(seccion, filtradas.length)}`;

    for (const registro of visibles) {
        contenedor.append(crearTarjeta(registro, seccion));
    }
}

function crearTarjeta(registro, seccion) {
    const info = seccion.tarjeta(registro);
    const tarjeta = document.createElement('article');
    tarjeta.className = 'tarjeta';

    const foto = seccion.fotos && Fotos.url(seccion.fotos, idDe(seccion, registro));
    if (foto) {
        const imagen = document.createElement('img');
        imagen.className = 'tarjeta__foto';
        imagen.src = foto;
        imagen.alt = '';
        imagen.loading = 'lazy';
        tarjeta.append(imagen);
    }

    const nombre = document.createElement('h3');
    nombre.className = 'tarjeta__nombre';
    nombre.textContent = info.nombre || 'Sin nombre';

    tarjeta.append(nombre);
    // Los datos de la tienda no tienen precio: en esas tarjetas no se muestra.
    if ('precio' in info) {
        const precio = document.createElement('p');
        precio.className = 'tarjeta__precio';
        precio.textContent = typeof info.precio === 'number' ? pesos.format(info.precio) + (info.precioSufijo || '') : 'Sin precio';
        tarjeta.append(precio);
    }

    const etiquetas = document.createElement('div');
    etiquetas.className = 'etiquetas';
    if (seccion.destacable && registro.destacado) {
        etiquetas.append(crearEtiqueta('⭐ Destacado', 'etiqueta--destacada'));
    }
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
    acciones.append(crearBoton('✏️ Cambiar', 'boton boton--suave', `Cambiar ${nombreVisible}`, () => abrirFormulario(registro)));
    for (const extra of seccion.accionesExtra?.(registro) || []) {
        acciones.append(crearBoton(extra.texto, extra.peligro ? 'boton boton--peligro-suave' : 'boton boton--suave', extra.nombre, extra.accion));
    }
    if (seccion.destacable) {
        acciones.append(crearBoton(registro.destacado ? '☆ No destacar' : '⭐ Destacar', 'boton boton--suave',
            `${registro.destacado ? 'Dejar de destacar' : 'Destacar'} ${nombreVisible}`, () => alternarDestacado(registro)));
    }
    if (!seccion.sinQuitar) {
        acciones.append(crearBoton('🗑️ Quitar', 'boton boton--peligro-suave', `Quitar ${nombreVisible}`, () => pedirQuitar(registro)));
    }
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
    if (!campo.requerido && !(campo.requeridoAlCrear && !editando)) {
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
    else if (campo.tipo === 'fecha') entrada.type = 'date';
    else if (campo.tipo === 'clave') {
        entrada.type = 'password';
        entrada.autocomplete = 'new-password';
    } else entrada.type = 'text';
    if (campo.soloNumeros) entrada.inputMode = 'numeric';
    if (campo.minusculas) {
        entrada.autocapitalize = 'none';
        entrada.spellcheck = false;
    }
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
    const valores = registro ? seccion.paraFormulario(registro) : (seccion.valoresNuevos?.(datos[seccionActual] || []) || {});
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
    fotoNueva = null;
    quitarFoto = false;
    if (seccion.fotos) campos.prepend(crearCampoFoto(seccion, registro));

    $('avanzado').hidden = Boolean(seccion.sinCodigo);
    if (seccion.sinCodigo) {
        $('campos-avanzados').replaceChildren();
    } else {
        const codigo = valores.codigo || `${seccion.prefijoCodigo}-${Date.now().toString(36).toUpperCase().slice(-6)}`;
        $('campos-avanzados').replaceChildren(crearCampo(CAMPO_CODIGO, codigo, registros, false));
    }
    $('avanzado').open = false;

    formularioCambiado = false;
    advertenciasVistas = '';
    $('ventana-formulario').showModal();
    formularioInicial = copiaDelFormulario();
    campos.querySelector('input, textarea')?.focus();
}

/* Pone el texto como ya se escribe en el sistema: si "impresion" coincide con la sugerencia "Impresión",
   se guarda "Impresión". Así no quedan "Blusa", "blusa" y "BLUSA" como tres tipos distintos. */
function ajustarTexto(campo, texto, registros) {
    // La contraseña se guarda tal cual: un espacio también cuenta.
    if (campo.tipo === 'clave' || campo.tipo === 'fecha') return String(texto);
    let valor = limpiarTexto(texto);
    if (!valor) return valor;
    if (campo.codigo) return valor.replace(/\s+/g, '-').toUpperCase();
    if (campo.mayusculas) return valor.toUpperCase();
    if (campo.minusculas) return valor.toLowerCase();
    if (campo.soloNumeros) return valor.replace(/[\s.-]/g, '');
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

    for (const campo of seccion.sinCodigo ? seccion.campos : [...seccion.campos, CAMPO_CODIGO]) {
        const requerido = campo.requerido || (campo.requeridoAlCrear && !editando);
        const entrada = formulario.elements[campo.nombre];
        const crudo = entrada.value;
        let mensaje = '';

        if (campo.tipo === 'pesos' || campo.tipo === 'cantidad') {
            const texto = crudo.trim();
            const numero = leerEntero(texto);
            if (texto === '') {
                if (requerido) mensaje = 'Este dato hace falta.';
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
            if (requerido && texto === '') mensaje = 'Este dato hace falta.';
            else if (texto !== '' && campo.min && texto.length < campo.min) mensaje = `Es muy corto: mínimo ${campo.min} ${campo.soloNumeros ? 'números' : 'letras'}.`;
            else if (campo.max && texto.length > campo.max) mensaje = `Es muy largo: máximo ${campo.max} ${campo.soloNumeros ? 'números' : 'letras'}.`;
            else if (texto !== '' && campo.soloNumeros && !/^\d+$/.test(texto)) mensaje = 'Escribe solo números, sin letras ni espacios.';
            else if (texto !== '' && campo.soloLetras && !SOLO_LETRAS.test(texto)) mensaje = 'Por ahora aquí solo se pueden usar letras y espacios (sin números, puntos ni comas).';
            else if (texto !== '' && campo.patron && !campo.patron.regla.test(texto)) mensaje = campo.patron.mensaje;
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
    return { ...cuerpo, ...seccion.cuerpoExtra?.(editando) };
}

/* Cosas que no son errores pero conviene confirmar: un nombre repetido o un precio de venta menor al de compra. */
function buscarAdvertencias(seccion, cuerpo) {
    const avisos = [];
    const comparable = { ...cuerpo, tipo: cuerpo.tipoRopa };
    const repetido = (datos[seccionActual] || []).find((r) => (!editando || idDe(seccion, r) !== idDe(seccion, editando)) && seccion.mismoQue(r, comparable));
    if (repetido) {
        avisos.push(`Ya tienes ${seccion.femenino ? 'una' : 'uno'} que se llama «${seccion.tarjeta(repetido).nombre}»${repetido.talla ? ` en talla ${repetido.talla}` : ''}.`);
    }
    if (seccion.advertencias) avisos.push(...seccion.advertencias(cuerpo));
    return avisos;
}

async function guardar() {
    const seccion = SECCIONES[seccionActual];
    if (editando && !fotoNueva && !quitarFoto && copiaDelFormulario() === formularioInicial) {
        $('formulario-error').textContent = 'No cambiaste nada: los datos son los mismos que ya estaban. Cambia algo o toca «Cancelar».';
        return;
    }
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
        const guardado = editando
            ? await llamar(`${seccion.ruta}/${idDe(seccion, editando)}`, { metodo: 'PUT', cuerpo })
            : await llamar(seccion.ruta, { metodo: 'POST', cuerpo });

        formularioCambiado = false;
        $('ventana-formulario').close();
        const nombre = seccion.tarjeta(cuerpo).nombre || seccion.singular;
        const fotoBien = await guardarFoto(seccion, guardado ? idDe(seccion, guardado) : idDe(seccion, editando), nombre);
        if (fotoBien) avisar(`✓ ¡Listo! Se guardó «${nombre}».`);
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
    if (error.estado === 401) return; // la sesión se venció: ya se está yendo al inicio de sesión
    if (error.estado === 403) {
        caja.textContent = 'Tu usuario no tiene permiso para hacer cambios aquí.';
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
    if (error.estado === 409 && error.titulo === 'Las unidades cambiaron') {
        // Mientras se editaba se vendieron unidades: se trae el número de ahora y se pide revisarlo.
        caja.textContent = error.message;
        const registro = editando;
        llamar(SECCIONES.productos.rutaLista).then((lista) => {
            const actual = lista.find((p) => p.id === registro?.id);
            if (!actual || editando !== registro) return;
            editando = { ...registro, cantidad: actual.cantidad };
            const entrada = $('campo-cantidad');
            entrada.value = miles.format(actual.cantidad);
            marcarError('cantidad', 'Este es el número de ahora. Revísalo y guarda otra vez.');
            entrada.focus();
            datos.productos = lista;
            dibujarMosaicos();
        }).catch(() => { /* el mensaje ya explica qué pasó */ });
        return;
    }
    if (error.estado === 409 && error.titulo === 'Código repetido' && SECCIONES[seccionActual].sinCodigo) {
        caja.textContent = error.message || 'Ya existe uno igual. Revisa los datos.';
        return;
    }
    if (error.estado === 409 && error.titulo === 'Operación no permitida' && seccionActual === 'operadores') {
        caja.textContent = 'Ya hay alguien con ese usuario, documento, celular o correo. Revisa esos datos.';
        return;
    }
    if (error.estado === 400 && !Object.keys(error.errores).length && error.titulo === 'Datos inválidos') {
        caja.textContent = 'El sistema no aceptó algún dato. Revisa que cada recuadro tenga el largo pedido.';
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

/* ---------- Confirmar: quitar y cambiar el estado ---------- */

function confirmar({ icono, titulo, texto, boton, peligro, conMotivo, accion }) {
    $('quitar-icono').textContent = icono;
    $('quitar-titulo').textContent = titulo;
    $('quitar-texto').textContent = texto;
    $('quitar-motivo-caja').hidden = !conMotivo;
    $('quitar-motivo').value = '';
    const confirmarBoton = $('boton-confirmar-quitar');
    confirmarBoton.textContent = boton;
    confirmarBoton.className = `boton ${peligro ? 'boton--peligro' : 'boton--principal'}`;
    porConfirmar = accion;
    $('ventana-quitar').showModal();
    $('ventana-quitar').querySelector('[data-cerrar]').focus();
}

async function alConfirmar() {
    const accion = porConfirmar;
    if (!accion) return;
    const boton = $('boton-confirmar-quitar');
    boton.disabled = true;
    try {
        await accion();
    } finally {
        boton.disabled = false;
    }
}

function falloAlConfirmar(error, nombre, clave) {
    $('ventana-quitar').close();
    if (error.estado === 401) return; // la sesión se venció: ya se está yendo al inicio de sesión
    if (error.estado === 404) {
        // Ya no estaba: el resultado es el mismo que se quería.
        avisar(`«${nombre}» ya no estaba en la lista.`);
        recargar(clave);
    } else {
        avisar(error.estado === 409 ? error.message : mensajeGeneral(error), 'error');
    }
}

function pedirQuitar(registro) {
    const clave = seccionActual;
    const seccion = SECCIONES[clave];
    const nombre = seccion.tarjeta(registro).nombre;
    confirmar({
        icono: '🗑️',
        titulo: nombre ? `¿Quitar «${nombre}»?` : `¿Quitar ${este(seccion)}?`,
        texto: seccion.textoQuitar,
        boton: 'Sí, quitar',
        peligro: true,
        conMotivo: seccion.pideMotivo,
        accion: () => quitar(registro, clave)
    });
}

async function quitar(registro, clave) {
    const seccion = SECCIONES[clave];
    const nombre = seccion.tarjeta(registro).nombre || seccion.singular;
    const motivo = $('quitar-motivo').value.trim();
    const extra = motivo ? `?motivo=${encodeURIComponent(motivo)}` : '';
    try {
        await llamar(`${seccion.ruta}/${idDe(seccion, registro)}${extra}`, { metodo: 'DELETE' });
        $('ventana-quitar').close();
        avisar(`✓ Se quitó «${nombre}».`);
        await cargar(clave);
    } catch (error) {
        falloAlConfirmar(error, nombre, clave);
    }
}

/* ---------- Estado de los operadores ---------- */

function estadoDelOperador(operador) {
    if (operador.estado === 'Suspendido') return { texto: '⏸️ Suspendido', bien: false };
    if (operador.estado === 'Despedido') return { texto: '🚪 Despedido', bien: false };
    return { texto: '✅ Activo', bien: true };
}

function accionesDelOperador(operador) {
    const nombre = nombreCompleto(operador);
    const despedido = operador.estado === 'Despedido';
    const acciones = [];
    if (!despedido) {
        acciones.push({
            texto: clavesDePermiso.has(operador.idUsuario) ? '🔑 Cambiar clave' : '🔑 Poner clave',
            nombre: `Clave de permiso de ${nombre}`,
            accion: () => abrirClave(operador)
        });
    }
    if (operador.estado === 'Activo') {
        acciones.push({ texto: '⏸️ Suspender', nombre: `Suspender a ${nombre}`, accion: () => pedirEstado(operador, 'Suspendido') });
    } else {
        acciones.push({
            texto: despedido ? '✅ Volver a contratar' : '✅ Reactivar',
            nombre: `Reactivar a ${nombre}`,
            accion: () => pedirEstado(operador, 'Activo')
        });
    }
    if (!despedido) {
        acciones.push({ texto: '🚪 Retirar', nombre: `Retirar a ${nombre}`, peligro: true, accion: () => pedirEstado(operador, 'Despedido') });
    }
    return acciones;
}

function pedirEstado(operador, estado) {
    const nombre = operador.primerNombre;
    const textos = {
        Suspendido: {
            icono: '⏸️', titulo: `¿Suspender a ${nombre}?`, boton: 'Sí, suspender', peligro: true, listo: `Se suspendió a ${nombre}.`,
            texto: 'No podrá entrar al sistema hasta que reactives su usuario. Si tiene la sesión abierta, se cierra.'
        },
        Despedido: {
            icono: '🚪', titulo: `¿Retirar a ${nombre}?`, boton: 'Sí, retirar', peligro: true, listo: `${nombre} salió del equipo.`,
            texto: 'Saldrá del equipo y no podrá entrar. Sus datos y sus ventas se conservan, y queda en «Despedidos».'
        },
        Activo: {
            icono: '✅', titulo: `¿Reactivar a ${nombre}?`, boton: 'Sí, reactivar', listo: `Se reactivó a ${nombre}.`,
            texto: 'Podrá volver a entrar con su usuario y su contraseña.'
        }
    }[estado];
    confirmar({ ...textos, accion: () => cambiarEstado(operador, estado, textos.listo) });
}

async function cambiarEstado(operador, estado, listo) {
    try {
        await llamar(`/api/operadores-cibercafe/${operador.idUsuario}/estado`, { metodo: 'PUT', cuerpo: { estado } });
        $('ventana-quitar').close();
        avisar(`✓ ${listo}`);
        await cargar('operadores');
    } catch (error) {
        falloAlConfirmar(error, operador.primerNombre, 'operadores');
    }
}

/* ---------- Destacados y filtros ---------- */

async function alternarDestacado(registro) {
    const clave = seccionActual;
    const seccion = SECCIONES[clave];
    const nombre = seccion.tarjeta(registro).nombre || seccion.singular;
    const destacado = !registro.destacado;
    try {
        await llamar(`${seccion.ruta}/${idDe(seccion, registro)}/destacado`, { metodo: 'PUT', cuerpo: { destacado } });
        avisar(destacado ? `⭐ «${nombre}» ahora sale entre los destacados de la página.` : `«${nombre}» ya no sale entre los destacados.`);
        await cargar(clave);
    } catch (error) {
        if (error.estado !== 401) avisar(mensajeGeneral(error), 'error');
    }
}

function dibujarFiltros(seccion, lista) {
    const contenedor = $('filtros');
    contenedor.replaceChildren();
    contenedor.hidden = !seccion.filtros || !lista;
    if (contenedor.hidden) return;
    seccion.filtros.forEach((filtro, posicion) => {
        const boton = document.createElement('button');
        boton.type = 'button';
        boton.className = 'filtro';
        boton.setAttribute('aria-pressed', String(posicion === filtroActual));
        boton.textContent = `${filtro.texto} (${lista.filter(filtro.pasa).length})`;
        boton.addEventListener('click', () => {
            filtroActual = posicion;
            dibujarTarjetas();
        });
        contenedor.append(boton);
    });
}

function copiaDelFormulario() {
    return [...$('formulario').querySelectorAll('input, textarea, select')]
        .filter((entrada) => entrada.type !== 'file')
        .map((entrada) => `${entrada.id}=${entrada.value.trim()}`)
        .join('\n');
}

/* ---------- Foto de productos, ropa y servicios ---------- */

function crearCampoFoto(seccion, registro) {
    const caja = document.createElement('div');
    caja.className = 'campo campo--ancho foto';

    const etiqueta = document.createElement('span');
    etiqueta.className = 'campo__etiqueta';
    etiqueta.textContent = 'Foto ';
    const opcional = document.createElement('span');
    opcional.className = 'campo__opcional';
    opcional.textContent = '(opcional)';
    etiqueta.append(opcional);

    const fila = document.createElement('div');
    fila.className = 'foto__fila';
    const vista = document.createElement('div');
    vista.className = 'foto__vista';
    const actual = registro ? Fotos.url(seccion.fotos, idDe(seccion, registro)) : null;

    const botones = document.createElement('div');
    botones.className = 'foto__botones';
    const entrada = document.createElement('input');
    entrada.type = 'file';
    entrada.accept = 'image/jpeg,image/png,image/webp,image/*';
    entrada.className = 'oculto';
    entrada.id = 'campo-foto';
    const elegir = document.createElement('label');
    elegir.className = 'boton boton--suave';
    elegir.htmlFor = 'campo-foto';
    elegir.tabIndex = 0;
    const quitar = document.createElement('button');
    quitar.type = 'button';
    quitar.className = 'boton boton--peligro-suave';
    quitar.textContent = 'Quitar foto';
    const ayuda = document.createElement('p');
    ayuda.className = 'campo__ayuda';
    ayuda.textContent = 'Desde el celular puedes tomarla con la cámara. Se achica sola antes de subirla.';
    const error = document.createElement('p');
    error.className = 'campo__error';
    error.hidden = true;

    function mostrar(direccion) {
        vista.replaceChildren();
        if (direccion) {
            const imagen = document.createElement('img');
            imagen.src = direccion;
            imagen.alt = 'Foto elegida';
            vista.append(imagen);
        } else {
            const vacio = document.createElement('span');
            vacio.textContent = '📷';
            vacio.setAttribute('aria-hidden', 'true');
            vista.append(vacio);
        }
        elegir.textContent = direccion ? '📷 Cambiar foto' : '📷 Elegir foto';
        quitar.hidden = !direccion;
    }

    entrada.addEventListener('change', async () => {
        const archivo = entrada.files[0];
        entrada.value = '';
        if (!archivo) return;
        error.hidden = true;
        elegir.textContent = 'Preparando…';
        try {
            fotoNueva = await Fotos.preparar(archivo);
            quitarFoto = false;
            formularioCambiado = true;
            mostrar(URL.createObjectURL(fotoNueva));
        } catch (problema) {
            error.textContent = `${problema.message} Prueba con otra foto (JPG o PNG).`;
            error.hidden = false;
            mostrar(fotoNueva ? URL.createObjectURL(fotoNueva) : (quitarFoto ? null : actual));
        }
    });
    elegir.addEventListener('keydown', (evento) => {
        if (evento.key === 'Enter' || evento.key === ' ') {
            evento.preventDefault();
            entrada.click();
        }
    });
    quitar.addEventListener('click', () => {
        fotoNueva = null;
        quitarFoto = Boolean(actual);
        formularioCambiado = true;
        mostrar(null);
    });

    botones.append(elegir, quitar);
    fila.append(vista, botones);
    caja.append(etiqueta, fila, entrada, ayuda, error);
    mostrar(actual);
    return caja;
}

/* Sube o quita la foto después de guardar los datos. Devuelve false si la foto falló (ya se avisó). */
async function guardarFoto(seccion, id, nombre) {
    if (!seccion.fotos || !id || (!fotoNueva && !quitarFoto)) return true;
    const ruta = `/api/imagenes/${seccion.fotos}/${id}`;
    try {
        if (fotoNueva) await llamar(ruta, { metodo: 'PUT', cuerpo: fotoNueva });
        else await llamar(ruta, { metodo: 'DELETE' });
        await Fotos.cargar();
        return true;
    } catch (error) {
        if (error.estado !== 401) {
            const motivo = error.estado === 413 ? 'pesa demasiado' : error.estado === 415 ? 'no es una foto válida' : 'no se pudo subir';
            avisar(`Se guardó «${nombre}», pero la foto ${motivo}. Ábrelo con «Cambiar» e intenta otra vez.`, 'error');
        }
        return false;
    } finally {
        fotoNueva = null;
        quitarFoto = false;
    }
}

/* ---------- Clave de permiso de los operadores ---------- */

async function cargarClavesDePermiso() {
    const claves = await llamar('/api/claves-permiso');
    clavesDePermiso.clear();
    for (const clave of claves) clavesDePermiso.set(clave.usuarioId, clave.idClave);
}

function abrirClave(operador) {
    operadorDeLaClave = operador;
    const tiene = clavesDePermiso.has(operador.idUsuario);
    $('clave-titulo').textContent = tiene ? `Cambiar la clave de ${operador.primerNombre}` : `Poner clave a ${operador.primerNombre}`;
    $('clave-permiso').value = '';
    $('clave-error').textContent = '';
    $('boton-quitar-clave').hidden = !tiene;
    $('ventana-clave').showModal();
    $('clave-permiso').focus();
}

$('formulario-clave').addEventListener('submit', async (evento) => {
    evento.preventDefault();
    const operador = operadorDeLaClave;
    const clave = $('clave-permiso').value;
    if (clave.length < 10 || clave.length > 30) {
        $('clave-error').textContent = 'La clave debe tener entre 10 y 30 letras o números.';
        $('clave-permiso').focus();
        return;
    }
    const idClave = clavesDePermiso.get(operador.idUsuario);
    const cuerpo = { usuarioId: operador.idUsuario, clave };
    try {
        if (idClave) await llamar(`/api/claves-permiso/${idClave}`, { metodo: 'PUT', cuerpo });
        else await llamar('/api/claves-permiso', { metodo: 'POST', cuerpo });
        $('ventana-clave').close();
        avisar(`✓ Se guardó la clave de ${operador.primerNombre}.`);
        recargar('operadores');
    } catch (error) {
        if (error.estado !== 401) $('clave-error').textContent = error.estado === 409 ? error.message : mensajeGeneral(error);
    }
});

$('boton-quitar-clave').addEventListener('click', async () => {
    const operador = operadorDeLaClave;
    if (!window.confirm(`¿Quitar la clave de permiso de ${operador.primerNombre}?`)) return;
    try {
        await llamar(`/api/claves-permiso/${clavesDePermiso.get(operador.idUsuario)}`, { metodo: 'DELETE' });
        $('ventana-clave').close();
        avisar(`✓ Se quitó la clave de ${operador.primerNombre}.`);
        recargar('operadores');
    } catch (error) {
        if (error.estado !== 401) $('clave-error').textContent = mensajeGeneral(error);
    }
});

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

$('boton-confirmar-quitar').addEventListener('click', alConfirmar);
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

if (SESION) {
    Sesion.dibujarUsuario(SESION, $('barra-usuario'));
    dibujarSaludo();
    elegirSeccion(seccionActual);
    cargarTodo();
}
