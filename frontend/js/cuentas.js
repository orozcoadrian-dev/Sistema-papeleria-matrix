'use strict';

/* Cuentas del negocio: solo la administradora.
   Las cifras se calculan aquí con las ventas, el catálogo y los operadores, porque el servidor todavía
   no las calcula. «Guardar este corte» las deja en los ingresos y las estadísticas del negocio.
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
    $('periodo-texto').textContent = inicio.toDateString() === fin.toDateString()
        ? fechaLarga(fin)
        : `Del ${fechaLarga(inicio)} al ${fechaLarga(fin)}`;
    $('estado-carga').textContent = 'Sacando las cuentas…';
    $('boton-guardar-corte').disabled = true;
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
        dibujar(cifras);
    } catch (error) {
        if (error.estado === 401) return;
        $('cuentas').hidden = true;
        $('estado-carga').textContent = error.estado === 0
            ? 'No pudimos conectar con el sistema. Revisa el internet e intenta otra vez.'
            : 'No se pudieron sacar las cuentas. Intenta otra vez; si sigue pasando, avísale a Osvaldo.';
    }
    cargarCortes();
}

function calcular({ ventas, productos, ropa, servicios, operadores, inicio, fin }) {
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
        cifra('Vendido menos sueldos', pesos.format(c.totalVendido - c.sueldos), periodo === 'mes' || periodo === 'anterior' ? 'En este mes' : 'Ojo: los sueldos son del mes completo')
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

/* ---------- Cortes guardados ---------- */

/* El código del corte lleva la fecha y la hora (aammddhhmm), así se sabe cuándo se guardó. */
function fechaDelCodigo(codigo) {
    const partes = /^(\d{2})(\d{2})(\d{2})(\d{2})(\d{2})/.exec(codigo || '');
    if (!partes) return null;
    const [, anio, mes, dia, hora, minuto] = partes.map(Number);
    if (mes < 1 || mes > 12 || dia < 1 || dia > 31) return null;
    return new Date(2000 + anio, mes - 1, dia, hora, minuto);
}

async function cargarCortes() {
    const contenedor = $('cortes');
    try {
        const ingresos = await Sesion.llamar('/api/ingresos-negocio');
        contenedor.replaceChildren();
        if (!ingresos.length) {
            const vacio = document.createElement('p');
            vacio.className = 'cuentas__vacio';
            vacio.textContent = 'Todavía no has guardado ningún corte.';
            contenedor.append(vacio);
            return;
        }
        const ordenados = [...ingresos].sort((a, b) => String(b.codigoIngreso).localeCompare(String(a.codigoIngreso)));
        for (const ingreso of ordenados) contenedor.append(tarjetaDeCorte(ingreso));
    } catch (error) {
        if (error.estado === 401) return;
        contenedor.replaceChildren();
        const fallo = document.createElement('p');
        fallo.className = 'cuentas__vacio';
        fallo.textContent = 'No se pudieron traer los cortes guardados.';
        contenedor.append(fallo);
    }
}

function tarjetaDeCorte(ingreso) {
    const tarjeta = document.createElement('article');
    tarjeta.className = 'tarjeta';
    const vendido = (ingreso.totalVendidoServicios || 0) + (ingreso.totalVendidoProductos || 0) + (ingreso.totalVendidoRopa || 0);
    const titulo = document.createElement('h3');
    titulo.className = 'tarjeta__nombre';
    const fecha = fechaDelCodigo(ingreso.codigoIngreso);
    titulo.textContent = fecha
        ? `${ingreso.tipoIngreso} · ${fecha.toLocaleDateString('es-CO', { day: 'numeric', month: 'long', year: 'numeric' })}`
        : ingreso.tipoIngreso;
    const total = document.createElement('p');
    total.className = 'tarjeta__precio';
    total.textContent = `${pesos.format(vendido)} vendido`;
    const detalle = document.createElement('p');
    detalle.className = 'tarjeta__descripcion';
    detalle.textContent = `Servicios ${pesos.format(ingreso.totalVendidoServicios || 0)} · Papelería ${pesos.format(ingreso.totalVendidoProductos || 0)} · `
        + `Ropa ${pesos.format(ingreso.totalVendidoRopa || 0)} · Sueldos ${pesos.format(ingreso.totalSalarioOperadores || 0)}`;
    tarjeta.append(titulo, total, detalle);
    return tarjeta;
}

/* Guarda lo calculado en los ingresos y las estadísticas del negocio. */
async function guardarCorte() {
    if (!cifras) return;
    const boton = $('boton-guardar-corte');
    boton.disabled = true;
    boton.textContent = 'Guardando…';
    const ahora = new Date();
    const dos = (n) => String(n).padStart(2, '0');
    const sello = `${dos(ahora.getFullYear() % 100)}${dos(ahora.getMonth() + 1)}${dos(ahora.getDate())}${dos(ahora.getHours())}${dos(ahora.getMinutes())}`;
    const tipo = PERIODOS[periodo].tipo;
    try {
        await Sesion.llamar('/api/ingresos-negocio', {
            metodo: 'POST',
            cuerpo: {
                codigoIngreso: sello,
                tipoIngreso: tipo,
                totalSalarioOperadores: cifras.sueldos,
                totalVendidoRopa: cifras.porTipo.ropa,
                inversionTotalPapeleria: cifras.inversionPapeleria,
                totalVendidoServicios: cifras.porTipo.servicio,
                totalVendidoProductos: cifras.porTipo.producto,
                totalRecaudoEsperadoPapeleria: cifras.recaudoEsperado
            }
        });
        await Sesion.llamar('/api/estadisticas-negocio', {
            metodo: 'POST',
            cuerpo: {
                // 15 cifras: la fecha y hora (10), los segundos (2) y 3 al azar.
                codigoEstadistica: `${sello}${dos(ahora.getSeconds())}${String(Math.floor(Math.random() * 1000)).padStart(3, '0')}`,
                tipoEstadistica: tipo,
                numeroInformacionEliminada: 0,
                serviciosEliminados: 0,
                numeroInformacionAgregada: 0,
                numeroServiciosOfrecidos: cifras.unidades.servicio,
                numeroProductosVendidos: cifras.unidades.producto,
                operadoresCibercafeDespedidos: 0,
                ropaActualCatalogo: cifras.ropaEnCatalogo,
                numeroProductosPapeleria: cifras.productosEnCatalogo,
                ropaRemovidaCatalogo: 0,
                productosRemovidos: 0,
                numeroRopaVendida: cifras.unidades.ropa,
                numeroServicios: cifras.serviciosEnCatalogo,
                numeroFacturasGeneradas: cifras.facturas
            }
        });
        avisar('✓ Se guardó el corte.');
        cargarCortes();
    } catch (error) {
        if (error.estado === 409) avisar('Ya guardaste un corte en este mismo minuto. Espera un momento.', 'error');
        else if (error.estado !== 401) avisar('No se pudo guardar el corte. Intenta otra vez; si sigue pasando, avísale a Osvaldo.', 'error');
    } finally {
        boton.disabled = false;
        boton.textContent = '💾 Guardar este corte';
    }
}

/* ---------- Inicio ---------- */

$('boton-guardar-corte').addEventListener('click', guardarCorte);
$('boton-imprimir').addEventListener('click', () => window.print());

if (SESION) {
    Sesion.dibujarUsuario(SESION, $('barra-usuario'));
    dibujarPeriodos();
    cargar();
}
