'use strict';

/* Cuentas del negocio: solo la administradora.
   Las cifras de la pantalla se calculan aquí con las ventas, el catálogo y los operadores.
   «Guardar este corte» le pide al servidor que calcule y guarde los ingresos y las estadísticas del
   periodo (/api/ingresos/calcular y /api/estadisticas/calcular), con su historial. Los historiales y
   los reportes se consultan y se eliminan más abajo; cada reporte se descarga en PDF.
   Los datos se insertan con textContent, nunca con innerHTML. */

const SESION = Sesion.exigir(['ADMINISTRADOR']);

const pesos = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 });
const miles = new Intl.NumberFormat('es-CO', { maximumFractionDigits: 0 });
const $ = (id) => document.getElementById(id);
const POCAS_UNIDADES = 5;
const MESES = ['enero', 'febrero', 'marzo', 'abril', 'mayo', 'junio', 'julio', 'agosto', 'septiembre', 'octubre', 'noviembre', 'diciembre'];

function iso(fecha) {
    return `${fecha.getFullYear()}-${String(fecha.getMonth() + 1).padStart(2, '0')}-${String(fecha.getDate()).padStart(2, '0')}`;
}

/* Cada periodo da sus fechas y el tipo con el que se guarda el corte (entre 10 y 15 letras, como pide el sistema). */
const PERIODOS = {
    mes: {
        titulo: 'Este mes', tipo: 'Corte del mes',
        fechas: () => { const hoy = new Date(); return [new Date(hoy.getFullYear(), hoy.getMonth(), 1), hoy]; }
    },
    anterior: {
        titulo: 'Mes pasado', tipo: 'Corte del mes',
        fechas: () => { const hoy = new Date(); return [new Date(hoy.getFullYear(), hoy.getMonth() - 1, 1), new Date(hoy.getFullYear(), hoy.getMonth(), 0)]; }
    },
    semana: {
        titulo: 'Últimos 7 días', tipo: 'Corte semanal',
        fechas: () => { const hoy = new Date(); const inicio = new Date(hoy); inicio.setDate(hoy.getDate() - 6); return [inicio, hoy]; }
    },
    hoy: {
        titulo: 'Hoy', tipo: 'Corte del día',
        fechas: () => { const hoy = new Date(); return [hoy, hoy]; }
    },
    todo: {
        titulo: 'Todo', tipo: 'Corte general',
        fechas: () => [new Date(2020, 0, 1), new Date()]
    }
};

let periodo = 'mes';
let cifras = null; // lo calculado para el periodo elegido, listo para guardar

function avisar(texto, tipo = 'bien') {
    const aviso = $('aviso-flotante');
    aviso.textContent = texto;
    aviso.className = 'aviso-flotante aviso-flotante--visible' + (tipo === 'error' ? ' aviso-flotante--error' : '');
    clearTimeout(avisar.temporizador);
    avisar.temporizador = setTimeout(() => { aviso.className = 'aviso-flotante'; }, tipo === 'error' ? 7000 : 3800);
}

function fechaLarga(fecha) {
    return `${fecha.getDate()} de ${MESES[fecha.getMonth()]} de ${fecha.getFullYear()}`;
}

/* ---------- Periodos ---------- */

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
            cargar();
        });
        contenedor.append(boton);
    }
}

/* ---------- Carga y cálculo ---------- */

async function cargar() {
    const [inicio, fin] = PERIODOS[periodo].fechas();
    $('periodo-texto').textContent = periodo === 'todo' ? 'Todo desde el comienzo'
        : inicio.toDateString() === fin.toDateString() ? fechaLarga(fin)
            : `Del ${fechaLarga(inicio)} al ${fechaLarga(fin)}`;
    $('estado-carga').textContent = 'Sacando las cuentas…';
    $('boton-guardar-corte').disabled = true;
    $('boton-reporte').disabled = true;
    try {
        const [ventas, productos, ropa, servicios, operadores] = await Promise.all([
            Sesion.llamar(`/api/ventas?desde=${iso(inicio)}&hasta=${iso(fin)}`),
            Sesion.llamar('/api/productos/interno'),
            Sesion.llamar('/api/ropa'),
            Sesion.llamar('/api/servicios'),
            Sesion.llamar('/api/operadores-cibercafe')
        ]);
        cifras = calcular({ ventas, productos, ropa, servicios, operadores, inicio, fin });
        $('estado-carga').textContent = '';
        $('cuentas').hidden = false;
        $('boton-guardar-corte').disabled = false;
        $('boton-reporte').disabled = false;
        dibujar(cifras);
    } catch (error) {
        if (error.estado === 401) return;
        $('cuentas').hidden = true;
        $('estado-carga').textContent = error.estado === 0
            ? 'No pudimos conectar con el sistema. Revisa el internet e intenta otra vez.'
            : 'No se pudieron sacar las cuentas. Intenta otra vez; si sigue pasando, avísale a Osvaldo.';
    }
    cargarContadores();
    cargarGuardados();
}

function calcular({ ventas, productos, ropa, servicios, operadores: todos, inicio, fin }) {
    // Quien ya salió del equipo no cuenta en los sueldos.
    const operadores = todos.filter((o) => o.estado !== 'Despedido');
    const porTipo = { servicio: 0, producto: 0, ropa: 0 };
    const unidades = { servicio: 0, producto: 0, ropa: 0 };
    const masVendido = new Map();
    const costo = new Map(productos.map((p) => [p.id, p.precioCompra]));
    let gananciaPapeleria = 0;

    for (const venta of ventas) {
        for (const articulo of venta.articulos) {
            const valor = (articulo.precioUnitario ?? 0) * articulo.cantidad;
            porTipo[articulo.tipo] += valor;
            unidades[articulo.tipo] += articulo.cantidad;
            const llave = `${articulo.tipo}:${articulo.id}`;
            const actual = masVendido.get(llave) || { nombre: articulo.nombre, cantidad: 0, valor: 0 };
            actual.cantidad += articulo.cantidad;
            actual.valor += valor;
            masVendido.set(llave, actual);
            if (articulo.tipo === 'producto' && costo.has(articulo.id)) {
                gananciaPapeleria += ((articulo.precioUnitario ?? 0) - costo.get(articulo.id)) * articulo.cantidad;
            }
        }
    }

    const totalVendido = ventas.reduce((suma, venta) => suma + venta.total, 0);
    const sumaPorTipo = porTipo.servicio + porTipo.producto + porTipo.ropa;

    return {
        inicio, fin,
        totalVendido,
        porTipo,
        unidades,
        facturas: ventas.length,
        diferenciaPorPrecios: Math.abs(sumaPorTipo - totalVendido) >= 1,
        gananciaPapeleria,
        masVendido: [...masVendido.values()].sort((a, b) => b.cantidad - a.cantidad).slice(0, 5),
        inversionPapeleria: productos.reduce((suma, p) => suma + p.cantidad * p.precioCompra, 0),
        recaudoEsperado: productos.reduce((suma, p) => suma + p.cantidad * p.precio, 0),
        unidadesEnTienda: productos.reduce((suma, p) => suma + p.cantidad, 0),
        pocas: productos.filter((p) => p.cantidad < POCAS_UNIDADES).sort((a, b) => a.cantidad - b.cantidad),
        productosEnCatalogo: productos.length,
        ropaEnCatalogo: ropa.length,
        serviciosEnCatalogo: servicios.length,
        sueldos: operadores.reduce((suma, o) => suma + (o.salario || 0), 0),
        operadores: operadores.length
    };
}

/* ---------- Dibujo ---------- */

function cifra(titulo, valor, detalle) {
    const caja = document.createElement('div');
    caja.className = 'cifra';
    const nombre = document.createElement('span');
    nombre.className = 'cifra__titulo';
    nombre.textContent = titulo;
    const numero = document.createElement('strong');
    numero.className = 'cifra__valor';
    numero.textContent = valor;
    caja.append(nombre, numero);
    if (detalle) {
        const extra = document.createElement('span');
        extra.className = 'cifra__titulo';
        extra.textContent = detalle;
        caja.append(extra);
    }
    return caja;
}

function dibujar(c) {
    $('resumen-ventas').replaceChildren(
        cifra('Total vendido', pesos.format(c.totalVendido)),
        cifra('Ventas', miles.format(c.facturas), c.facturas ? `Promedio ${pesos.format(Math.round(c.totalVendido / c.facturas))}` : ''),
        cifra('Ganancia en papelería', pesos.format(c.gananciaPapeleria), 'Venta menos lo que costó')
    );

    const barras = $('barras-ventas');
    barras.replaceChildren();
    const tipos = [
        ['servicio', '🖨️ Servicios', ['vez', 'veces']],
        ['producto', '✏️ Papelería', ['unidad', 'unidades']],
        ['ropa', '👗 Ropa', ['prenda', 'prendas']]
    ];
    const mayor = Math.max(1, ...tipos.map(([tipo]) => c.porTipo[tipo]));
    for (const [tipo, nombre, medida] of tipos) {
        const fila = document.createElement('div');
        fila.className = 'barra-fila';
        const etiqueta = document.createElement('span');
        etiqueta.className = 'barra-fila__nombre';
        etiqueta.textContent = nombre;
        const pista = document.createElement('span');
        pista.className = 'barra-fila__pista';
        const relleno = document.createElement('span');
        relleno.className = `barra-fila__relleno barra-fila__relleno--${tipo}`;
        relleno.style.width = `${Math.round((c.porTipo[tipo] / mayor) * 100)}%`;
        pista.append(relleno);
        const valor = document.createElement('span');
        valor.className = 'barra-fila__valor';
        valor.textContent = `${pesos.format(c.porTipo[tipo])} · ${miles.format(c.unidades[tipo])} ${c.unidades[tipo] === 1 ? medida[0] : medida[1]}`;
        fila.append(etiqueta, pista, valor);
        barras.append(fila);
    }
    $('nota-precios').hidden = !c.diferenciaPorPrecios;

    $('resumen-tienda').replaceChildren(
        cifra('Lo que costó lo que hay', pesos.format(c.inversionPapeleria), `${miles.format(c.unidadesEnTienda)} unidades en la tienda`),
        cifra('Si se vende todo', pesos.format(c.recaudoEsperado), `Ganarías ${pesos.format(c.recaudoEsperado - c.inversionPapeleria)}`),
        cifra('En el catálogo', `${c.productosEnCatalogo + c.ropaEnCatalogo + c.serviciosEnCatalogo}`,
            `${c.productosEnCatalogo} de papelería, ${c.ropaEnCatalogo} prendas, ${c.serviciosEnCatalogo} servicios`)
    );

    llenarLista($('lista-pocas'), c.pocas.map((p) => `${p.nombre}: ${p.cantidad === 0 ? 'agotado' : p.cantidad === 1 ? 'queda 1' : `quedan ${p.cantidad}`}`),
        'Todo tiene buenas existencias.');
    llenarLista($('lista-mas-vendido'), c.masVendido.map((a) => `${a.nombre}: ${miles.format(a.cantidad)}`),
        'Todavía no hay ventas en estos días.');

    $('resumen-gastos').replaceChildren(
        cifra('Sueldos al mes', pesos.format(c.sueldos), c.operadores === 1 ? '1 operador' : `${c.operadores} operadores`),
        cifra('Vendido menos sueldos', pesos.format(c.totalVendido - c.sueldos), periodo === 'mes' || periodo === 'anterior' ? 'En este mes' : 'Ojo: es el sueldo de un solo mes')
    );
}

function llenarLista(lista, textos, vacio) {
    lista.replaceChildren();
    if (!textos.length) {
        const item = document.createElement('li');
        item.className = 'cuentas__vacio';
        item.textContent = vacio;
        lista.append(item);
        return;
    }
    for (const texto of textos) {
        const item = document.createElement('li');
        item.textContent = texto;
        lista.append(item);
    }
}

/* ---------- Lo registrado ---------- */

async function cargarContadores() {
    try {
        const c = await Sesion.llamar('/api/contadores');
        $('resumen-registrado').replaceChildren(
            cifra('Servicios ofrecidos', miles.format(c.serviciosOfrecidos), cuantos(c.serviciosRetirados, 'retirado', 'retirados')),
            cifra('Productos de papelería', miles.format(c.productosEnCatalogo), cuantos(c.productosRetirados, 'retirado', 'retirados')),
            cifra('Prendas disponibles', miles.format(c.ropaDisponible),
                `${cuantos(c.ropaVendida, 'vendida', 'vendidas')} · ${cuantos(c.ropaRetirada, 'retirada', 'retiradas')}`),
            cifra('Datos de la tienda', miles.format(c.datosPublicados), cuantos(c.datosRetirados, 'retirado', 'retirados')),
            cifra('Operadores activos', miles.format(c.operadoresActivos),
                `${cuantos(c.operadoresSuspendidos, 'suspendido', 'suspendidos')} · ${cuantos(c.operadoresDespedidos, 'despedido', 'despedidos')}`),
            cifra('Ventas registradas', miles.format(c.ventas), cuantos(c.ventasAnuladas, 'anulada', 'anuladas'))
        );
    } catch (error) {
        if (error.estado !== 401) $('resumen-registrado').replaceChildren(vacio('No se pudo contar lo registrado.'));
    }
}

function cuantos(cantidad, uno, varios) {
    return `${miles.format(cantidad)} ${cantidad === 1 ? uno : varios}`;
}

function vacio(texto) {
    const parrafo = document.createElement('p');
    parrafo.className = 'cuentas__vacio';
    parrafo.textContent = texto;
    return parrafo;
}

/* ---------- Historiales y reportes ---------- */

const GUARDADOS = {
    ingresos: { titulo: '💰 Ingresos', ruta: '/api/historiales-ingresos-negocio', vacio: 'Todavía no has guardado ningún corte de ingresos.' },
    estadisticas: { titulo: '📈 Estadísticas', ruta: '/api/historiales-estadisticas-negocio', vacio: 'Todavía no has guardado estadísticas.' },
    reportes: { titulo: '📄 Reportes', ruta: '/api/reportes', vacio: 'Todavía no has generado ningún reporte.' }
};

let pestana = 'ingresos';
let guardados;          // undefined = cargando, null = no se pudo
let porEliminar = null; // lo que se va a eliminar

function normalizar(texto) {
    return String(texto ?? '').normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase();
}

/* "2026-11-26" o "2026-11-26T14:05:00" como se dice en español. */
function cuando(valor) {
    const [dia, hora] = String(valor).split('T');
    const [anio, mes, numero] = dia.split('-').map(Number);
    const texto = fechaLarga(new Date(anio, mes - 1, numero));
    return hora ? `${texto}, ${hora.slice(0, 5)}` : texto;
}

/* El sistema guarda el periodo escrito en palabras («Rango uno ene dos cero dos seis a …»): aquí se
   vuelve a escribir como una fecha normal. Si no tiene esa forma, se muestra tal cual. */
const DIAS_EN_PALABRAS = ('- uno dos tres cuatro cinco seis siete ocho nueve diez once doce trece catorce quince dieciséis diecisiete '
    + 'dieciocho diecinueve veinte veintiuno veintidós veintitrés veinticuatro veinticinco veintiséis veintisiete veintiocho veintinueve treinta').split(' ');
const DIGITOS_EN_PALABRAS = 'cero uno dos tres cuatro cinco seis siete ocho nueve'.split(' ');
const MESES_CORTOS = 'ene feb mar abr may jun jul ago sep oct nov dic'.split(' ');

function periodoDe(motivo) {
    const mes = `(${MESES_CORTOS.join('|')})`;
    // «([^ ]+ …)» son las cuatro cifras del año, cada una escrita en palabras.
    const fecha = `(.+?) ${mes} ([^ ]+ [^ ]+ [^ ]+ [^ ]+)`;
    const partes = String(motivo ?? '').match(new RegExp(`^Rango ${fecha} a ${fecha}$`, 'u'));
    if (!partes) return motivo || '';
    const leer = (dia, nombreMes, anio) => {
        const numero = dia === 'treinta y uno' ? 31 : DIAS_EN_PALABRAS.indexOf(dia);
        const cifrasDelAnio = anio.split(' ').map((p) => DIGITOS_EN_PALABRAS.indexOf(p));
        if (numero < 1 || cifrasDelAnio.includes(-1)) return null;
        return new Date(Number(cifrasDelAnio.join('')), MESES_CORTOS.indexOf(nombreMes), numero);
    };
    const desde = leer(partes[1], partes[2], partes[3]);
    const hasta = leer(partes[4], partes[5], partes[6]);
    if (!desde || !hasta) return motivo;
    return desde.getTime() === hasta.getTime() ? `Periodo: ${fechaLarga(hasta)}` : `Periodo: del ${fechaLarga(desde)} al ${fechaLarga(hasta)}`;
}

async function cargarGuardados() {
    try {
        const [ingresos, deIngresos, estadisticas, deEstadisticas, reportes] = await Promise.all([
            Sesion.llamar('/api/ingresos'),
            Sesion.llamar(GUARDADOS.ingresos.ruta),
            Sesion.llamar('/api/estadisticas'),
            Sesion.llamar(GUARDADOS.estadisticas.ruta),
            Sesion.llamar(GUARDADOS.reportes.ruta)
        ]);
        const ingresoDe = new Map(ingresos.map((i) => [i.idIngreso, i]));
        const estadisticaDe = new Map(estadisticas.map((e) => [e.idEstadistica, e]));
        const recientes = (lista) => lista.sort((a, b) => b.id - a.id);
        const cortes = recientes(deIngresos.map((h) => ({
            id: h.idHistorialIngreso, fecha: h.fechaGeneracion, motivo: h.motivoIngreso, ingreso: ingresoDe.get(h.ingresoId)
        })));
        const cuentas = recientes(deEstadisticas.map((h) => ({
            id: h.idHistorialEstadistica, fecha: h.fechaGeneracion, motivo: h.motivoEstadistica,
            estadistica: estadisticaDe.get(h.estadisticaDelNegocioId)
        })));
        guardados = {
            ingresos: cortes,
            estadisticas: cuentas,
            reportes: recientes(reportes.map((r) => {
                const corte = cortes.find((c) => c.id === r.historialIngresoDelNegocioId);
                const cuenta = cuentas.find((c) => c.id === r.historialEstadisticaDelNegocioId);
                return { id: r.idReporte, fecha: r.fechaGeneracion, motivo: (corte || cuenta)?.motivo || '', reporte: r, corte, cuenta };
            }))
        };
    } catch (error) {
        if (error.estado === 401) return;
        guardados = null;
    }
    dibujarGuardados();
}

function dibujarGuardados() {
    const pestanas = $('pestanas-guardados');
    pestanas.replaceChildren();
    for (const [valor, info] of Object.entries(GUARDADOS)) {
        const boton = document.createElement('button');
        boton.type = 'button';
        boton.className = 'filtro';
        boton.setAttribute('aria-pressed', String(valor === pestana));
        boton.textContent = guardados ? `${info.titulo} (${guardados[valor].length})` : info.titulo;
        boton.addEventListener('click', () => {
            pestana = valor;
            dibujarGuardados();
        });
        pestanas.append(boton);
    }

    const contenedor = $('guardados');
    contenedor.replaceChildren();
    if (guardados === undefined) return;
    if (guardados === null) {
        contenedor.append(vacio('No se pudo traer lo guardado. Intenta otra vez en un momento.'));
        return;
    }
    const lista = guardados[pestana];
    if (!lista.length) {
        contenedor.append(vacio(GUARDADOS[pestana].vacio));
        return;
    }
    const palabras = normalizar($('buscar-guardados').value).split(/\s+/).filter(Boolean);
    const visibles = lista.filter((item) => {
        const texto = normalizar([periodoDe(item.motivo), cuando(item.fecha), item.ingreso?.codigoIngreso,
            item.estadistica?.codigoEstadistica, item.reporte?.codigoReporte].join(' '));
        return palabras.every((palabra) => texto.includes(palabra));
    });
    if (!visibles.length) {
        contenedor.append(vacio('No encontramos nada con esa búsqueda.'));
        return;
    }
    const dibujo = { ingresos: tarjetaDeCorte, estadisticas: tarjetaDeEstadisticas, reportes: tarjetaDeReporte }[pestana];
    for (const item of visibles) contenedor.append(dibujo(item));
}

function tarjeta({ titulo, principal, renglones, acciones }) {
    const caja = document.createElement('article');
    caja.className = 'tarjeta venta';
    const nombre = document.createElement('h3');
    nombre.className = 'tarjeta__nombre';
    nombre.textContent = titulo;
    caja.append(nombre);
    if (principal) {
        const valor = document.createElement('p');
        valor.className = 'tarjeta__precio';
        valor.textContent = principal;
        caja.append(valor);
    }
    for (const texto of renglones.filter(Boolean)) {
        const renglon = document.createElement('p');
        renglon.className = 'tarjeta__descripcion';
        renglon.textContent = texto;
        caja.append(renglon);
    }
    const botones = document.createElement('div');
    botones.className = 'tarjeta__acciones';
    for (const accion of acciones) {
        const boton = document.createElement('button');
        boton.type = 'button';
        boton.className = `boton ${accion.peligro ? 'boton--peligro-suave' : 'boton--suave'}`;
        boton.textContent = accion.texto;
        boton.setAttribute('aria-label', `${accion.descripcion}: ${titulo}`);
        boton.addEventListener('click', accion.hacer);
        botones.append(boton);
    }
    caja.append(botones);
    return caja;
}

function totalDe(ingreso) {
    return (ingreso?.totalVendidoServicios || 0) + (ingreso?.totalVendidoProductos || 0) + (ingreso?.totalVendidoRopa || 0);
}

function accionEliminar(tipo, item, titulo) {
    return { texto: '🗑️ Eliminar', descripcion: 'Eliminar', peligro: true, hacer: () => pedirEliminar(tipo, item, titulo) };
}

function tarjetaDeCorte(item) {
    const titulo = `Corte del ${cuando(item.fecha)}`;
    const i = item.ingreso;
    return tarjeta({
        titulo,
        principal: i ? `${pesos.format(totalDe(i))} vendido` : 'Sin cifras',
        renglones: [
            i && `Servicios ${pesos.format(i.totalVendidoServicios || 0)} · Papelería ${pesos.format(i.totalVendidoProductos || 0)} · Ropa ${pesos.format(i.totalVendidoRopa || 0)}`,
            periodoDe(item.motivo)
        ],
        acciones: [
            { texto: '📄 Reporte en PDF', descripcion: 'Generar reporte', hacer: () => generarReporte({ ingresos: item.id }) },
            accionEliminar('ingresos', item, titulo)
        ]
    });
}

function tarjetaDeEstadisticas(item) {
    const titulo = `Estadísticas del ${cuando(item.fecha)}`;
    const e = item.estadistica;
    return tarjeta({
        titulo,
        principal: e ? (e.numeroFacturasGeneradas === 1 ? '1 venta' : `${miles.format(e.numeroFacturasGeneradas)} ventas`) : 'Sin cifras',
        renglones: [
            e && `Vendido: ${miles.format(e.numeroProductosVendidos)} de papelería · ${miles.format(e.numeroRopaVendida)} prendas · ${miles.format(e.numeroServicios)} servicios`,
            e && `Registrado: ${miles.format(e.numeroServiciosOfrecidos)} servicios · ${miles.format(e.numeroProductosPapeleria)} productos · ${miles.format(e.ropaActualCatalogo)} prendas`,
            periodoDe(item.motivo)
        ],
        acciones: [
            { texto: '📄 Reporte en PDF', descripcion: 'Generar reporte', hacer: () => generarReporte({ estadisticas: item.id }) },
            accionEliminar('estadisticas', item, titulo)
        ]
    });
}

function tarjetaDeReporte(item) {
    const titulo = `Reporte del ${cuando(item.fecha)}`;
    return tarjeta({
        titulo,
        renglones: [periodoDe(item.motivo), `Archivo: ${item.reporte.rutaArchivo}`],
        acciones: [
            { texto: '📥 Descargar PDF', descripcion: 'Descargar', hacer: () => descargarReporte(item) },
            accionEliminar('reportes', item, titulo)
        ]
    });
}

/* ---------- Reportes en PDF ---------- */

function contenidoDelReporte(item) {
    const lineas = [
        { texto: 'Papelería Matrix', tipo: 'titulo' },
        { texto: 'Reporte del negocio', tipo: 'subtitulo' },
        { texto: `Generado el ${cuando(item.fecha)}` },
        { texto: `Código ${item.reporte.codigoReporte}`, tipo: 'nota' },
        { linea: true }
    ];
    const i = item.corte?.ingreso;
    if (i) {
        lineas.push(
            { texto: 'Ingresos', tipo: 'subtitulo' },
            { texto: periodoDe(item.corte.motivo), tipo: 'nota' },
            { texto: 'Servicios', derecha: pesos.format(i.totalVendidoServicios || 0) },
            { texto: 'Papelería', derecha: pesos.format(i.totalVendidoProductos || 0) },
            { texto: 'Ropa', derecha: pesos.format(i.totalVendidoRopa || 0) },
            { texto: 'Total vendido', derecha: pesos.format(totalDe(i)), tipo: 'total' },
            { espacio: true }
        );
    }
    const e = item.cuenta?.estadistica;
    if (e) {
        const fila = (texto, valor) => ({ texto, derecha: miles.format(valor) });
        lineas.push(
            { texto: 'Estadísticas', tipo: 'subtitulo' },
            { texto: periodoDe(item.cuenta.motivo), tipo: 'nota' },
            fila('Ventas hechas', e.numeroFacturasGeneradas),
            fila('Unidades de papelería vendidas', e.numeroProductosVendidos),
            fila('Prendas vendidas', e.numeroRopaVendida),
            fila('Servicios prestados', e.numeroServicios),
            { espacio: true },
            { texto: 'Lo registrado cuando se guardó', tipo: 'subtitulo' },
            fila('Servicios ofrecidos', e.numeroServiciosOfrecidos),
            fila('Servicios retirados', e.serviciosEliminados),
            fila('Productos de papelería', e.numeroProductosPapeleria),
            fila('Productos retirados', e.productosRemovidos),
            fila('Prendas disponibles', e.ropaActualCatalogo),
            fila('Prendas retiradas', e.ropaRemovidaCatalogo),
            fila('Datos de la tienda registrados', e.numeroInformacionAgregada),
            fila('Datos de la tienda retirados', e.numeroInformacionEliminada),
            fila('Operadores despedidos', e.operadoresCibercafeDespedidos)
        );
    }
    if (!i && !e) lineas.push({ texto: 'Los historiales de este reporte ya no están disponibles.' });
    return lineas;
}

function descargarReporte(item) {
    Pdf.descargar(item.reporte.rutaArchivo, contenidoDelReporte(item));
    avisar('✓ Se descargó el PDF. Búscalo en tus descargas.');
}

/* Veinte cifras: la fecha y la hora, más seis al azar. */
function codigoDeReporte(ahora) {
    const dos = (n) => String(n).padStart(2, '0');
    const momento = `${iso(ahora).replaceAll('-', '')}${dos(ahora.getHours())}${dos(ahora.getMinutes())}${dos(ahora.getSeconds())}`;
    return momento + String(Math.floor(Math.random() * 1000000)).padStart(6, '0');
}

/* Guarda el reporte enlazado a los historiales indicados y lo descarga. */
async function generarReporte({ ingresos = null, estadisticas = null }) {
    const ahora = new Date();
    const codigoReporte = codigoDeReporte(ahora);
    try {
        const creado = await Sesion.llamar('/api/reportes', {
            metodo: 'POST',
            cuerpo: {
                codigoReporte,
                historialIngresoDelNegocioId: ingresos,
                historialEstadisticaDelNegocioId: estadisticas,
                usuarioId: SESION.idUsuario,
                rutaArchivo: `reporte-${codigoReporte}.pdf`,
                fechaGeneracion: iso(ahora)
            }
        });
        pestana = 'reportes';
        await cargarGuardados();
        const item = guardados?.reportes.find((r) => r.id === creado.idReporte);
        if (item) descargarReporte(item);
    } catch (error) {
        if (error.estado !== 401) avisar('No se pudo generar el reporte. Intenta otra vez.', 'error');
    }
}

/* ---------- Eliminar ---------- */

function pedirEliminar(tipo, item, titulo) {
    porEliminar = { tipo, item };
    $('eliminar-titulo').textContent = `¿Eliminar «${titulo}»?`;
    $('ventana-eliminar').showModal();
    $('ventana-eliminar').querySelector('[data-cerrar]').focus();
}

async function eliminar() {
    const { tipo, item } = porEliminar;
    const boton = $('boton-confirmar-eliminar');
    boton.disabled = true;
    try {
        await Sesion.llamar(`${GUARDADOS[tipo].ruta}/${item.id}`, { metodo: 'DELETE' });
        avisar('✓ Se eliminó.');
        await cargarGuardados();
        cargarContadores();
    } catch (error) {
        if (error.estado === 409) avisar('Tiene un reporte guardado. Elimina primero el reporte en «Reportes».', 'error');
        else if (error.estado === 404) cargarGuardados();
        else if (error.estado !== 401) avisar('No se pudo eliminar. Intenta otra vez.', 'error');
    } finally {
        boton.disabled = false;
        $('ventana-eliminar').close();
    }
}

/* ---------- Guardar un corte ---------- */

/* El servidor calcula y guarda los ingresos y las estadísticas del periodo, cada uno con su historial.
   Devuelve los números de esos dos historiales. */
async function guardarHistoriales() {
    const rango = { desde: iso(cifras.inicio), hasta: iso(cifras.fin) };
    const ingreso = await Sesion.llamar('/api/ingresos/calcular', { metodo: 'POST', cuerpo: rango });
    const estadistica = await Sesion.llamar('/api/estadisticas/calcular', { metodo: 'POST', cuerpo: rango });
    const [deIngresos, deEstadisticas] = await Promise.all([
        Sesion.llamar(GUARDADOS.ingresos.ruta), Sesion.llamar(GUARDADOS.estadisticas.ruta)
    ]);
    return {
        ingresos: deIngresos.find((h) => h.ingresoId === ingreso.id)?.idHistorialIngreso ?? null,
        estadisticas: deEstadisticas.find((h) => h.estadisticaDelNegocioId === estadistica.id)?.idHistorialEstadistica ?? null
    };
}

async function conBotonOcupado(boton, textoOcupado, tarea) {
    const texto = boton.textContent;
    boton.disabled = true;
    boton.textContent = textoOcupado;
    try {
        await tarea();
    } catch (error) {
        if (error.estado !== 401) avisar('No se pudo guardar el corte. Intenta otra vez; si sigue pasando, avísale a Osvaldo.', 'error');
    } finally {
        boton.disabled = false;
        boton.textContent = texto;
    }
}

function guardarCorte() {
    if (!cifras) return;
    conBotonOcupado($('boton-guardar-corte'), 'Guardando…', async () => {
        await guardarHistoriales();
        avisar('✓ Se guardó el corte.');
        await cargarGuardados();
    });
}

function reporteDelPeriodo() {
    if (!cifras) return;
    conBotonOcupado($('boton-reporte'), 'Generando…', async () => generarReporte(await guardarHistoriales()));
}

/* ---------- Inicio ---------- */

$('boton-guardar-corte').addEventListener('click', guardarCorte);
$('boton-reporte').addEventListener('click', reporteDelPeriodo);
$('boton-imprimir').addEventListener('click', () => window.print());
$('buscar-guardados').addEventListener('input', dibujarGuardados);
$('boton-confirmar-eliminar').addEventListener('click', eliminar);
$('ventana-eliminar').querySelector('[data-cerrar]').addEventListener('click', () => $('ventana-eliminar').close());

if (SESION) {
    Sesion.dibujarUsuario(SESION, $('barra-usuario'));
    dibujarPeriodos();
    cargar();
}
