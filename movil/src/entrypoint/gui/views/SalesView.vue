<script setup lang="ts">
import { Pencil, ReceiptText, Undo2, WifiOff } from 'lucide-vue-next';
import { onMounted, ref } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import type { Periodo, ResumenDeVentas } from '../../../application/dto/responses/responses';
import { fechaIso } from '../../../application/services/SaleService';
import { ErrorDeApi } from '../../../domain/exceptions/errors';
import type { Venta } from '../../../domain/model/Venta';
import BottomSheet from '../components/BottomSheet.vue';
import { avisar, carrito, empezarCambio, mensajeDe, pesos, terminarCambio, useAplicacion, ventaEnCambio } from '../context';

const { auth, ventas } = useAplicacion();
const esAdministrador = auth.usuarioActual()?.esAdministrador ?? false;
const PERIODOS: { valor: Periodo; texto: string }[] = [
  { valor: 'hoy', texto: 'Hoy' },
  { valor: 'semana', texto: 'Últimos 7 días' },
  { valor: 'mes', texto: 'Últimos 30 días' },
];

const periodo = ref<Periodo>('hoy');
const resumen = ref<ResumenDeVentas>();
const error = ref('');
const porAnular = ref<Venta>();
const anulando = ref(false);
const porCambiar = ref<Venta>();
const clavePermiso = ref('');
const errorAlAnular = ref('');
const router = useRouter();
let pedido = 0;

function pedirCambio(venta: Venta) {
  // Antes de reemplazar una venta sin cobrar, se pregunta.
  if (carrito.value.unidades && !ventaEnCambio.value) porCambiar.value = venta;
  else void cambiarVenta(venta);
}

async function cambiarVenta(venta: Venta) {
  porCambiar.value = undefined;
  empezarCambio(venta);
  await router.push({ name: 'vender' });
}

function pedirAnular(venta: Venta) {
  clavePermiso.value = '';
  errorAlAnular.value = '';
  porAnular.value = venta;
}

async function cargar() {
  const turno = ++pedido;
  resumen.value = undefined;
  error.value = '';
  try {
    const nuevo = await ventas.resumen(periodo.value);
    // Si se tocó otro periodo mientras llegaba la respuesta, esta ya no sirve.
    if (turno === pedido) resumen.value = nuevo;
  } catch (fallo) {
    if (turno === pedido) error.value = mensajeDe(fallo);
  }
}

function elegir(valor: Periodo) {
  periodo.value = valor;
  void cargar();
}

function fechaLegible(iso: string): string {
  if (iso === fechaIso(new Date())) return 'Hoy';
  if (iso === fechaIso(new Date(), 1)) return 'Ayer';
  return new Date(`${iso}T00:00:00`).toLocaleDateString('es-CO', { weekday: 'long', day: 'numeric', month: 'long' });
}

async function anular() {
  const venta = porAnular.value;
  if (!venta) return;
  anulando.value = true;
  errorAlAnular.value = '';
  try {
    await ventas.anular({ id: venta.id, clavePermiso: clavePermiso.value });
    avisar(`Se anuló la venta de ${pesos(venta.total)}.`);
  } catch (fallo) {
    if (!(fallo instanceof ErrorDeApi && fallo.estado === 404)) {
      // La ventana sigue abierta para corregir la clave o intentar otra vez.
      errorAlAnular.value = mensajeDe(fallo);
      return;
    }
    avisar('Esa venta ya estaba anulada.');
  } finally {
    anulando.value = false;
  }
  if (ventaEnCambio.value?.venta.id === venta.id) terminarCambio();
  porAnular.value = undefined;
  await cargar();
}

onMounted(cargar);
</script>

<template>
  <h1>Lo vendido</h1>
  <p class="suave">{{ esAdministrador ? 'Todas las ventas del negocio.' : 'Solo aparecen las ventas que registraste tú. Para cambiar o anular una necesitas tu clave de permiso.' }}</p>

  <div class="filtros" role="group" aria-label="Periodo">
    <button v-for="opcion in PERIODOS" :key="opcion.valor" type="button" class="filtro" :aria-pressed="periodo === opcion.valor" @click="elegir(opcion.valor)">{{ opcion.texto }}</button>
  </div>

  <div v-if="error" class="tarjeta estado">
    <WifiOff :size="32" aria-hidden="true" />
    <p>{{ error }}</p>
    <button type="button" class="boton boton--suave" @click="cargar">Intentar de nuevo</button>
  </div>
  <div v-else-if="!resumen" class="lista" role="status" aria-label="Cargando ventas">
    <div v-for="n in 3" :key="n" class="esqueleto" />
  </div>
  <template v-else>
    <div class="cifras">
      <div class="cifra cifra--grande"><span>Total vendido</span><strong>{{ pesos(resumen.total) }}</strong></div>
      <div class="cifra"><span>Ventas</span><strong>{{ resumen.cantidad }}</strong></div>
      <div class="cifra"><span>Promedio</span><strong>{{ resumen.cantidad ? pesos(resumen.promedio) : '—' }}</strong></div>
    </div>

    <div v-if="!resumen.cantidad" class="tarjeta estado">
      <ReceiptText :size="32" aria-hidden="true" />
      <p>No hay ventas en estos días.</p>
      <RouterLink :to="{ name: 'vender' }" class="boton boton--principal">Hacer una venta</RouterLink>
    </div>

    <article v-for="venta in resumen.ventas" :key="venta.id" class="tarjeta venta">
      <header><strong>{{ pesos(venta.total) }}</strong><span>{{ fechaLegible(venta.fecha) }}</span></header>
      <ul>
        <li v-for="(linea, indice) in venta.articulos" :key="indice">{{ linea.cantidad }} × {{ linea.nombre }}</li>
      </ul>
      <small>Factura n.º {{ venta.codigo }}<template v-if="esAdministrador"> · Vendió: {{ venta.operador }}</template></small>
      <div class="venta__acciones">
        <button v-if="venta.esDeCaja" type="button" class="boton boton--suave" :aria-label="`Cambiar la venta ${venta.codigo}`" @click="pedirCambio(venta)"><Pencil :size="18" aria-hidden="true" /> Cambiar</button>
        <button type="button" class="boton boton--peligro" :aria-label="`Anular la venta ${venta.codigo}`" @click="pedirAnular(venta)"><Undo2 :size="18" aria-hidden="true" /> Anular venta</button>
      </div>
    </article>
  </template>

  <BottomSheet v-if="porAnular" :titulo="`¿Anular la venta de ${pesos(porAnular.total)}?`" @cerrar="porAnular = undefined">
    <p>La venta queda anulada y la papelería vuelve a las existencias. No se puede deshacer.</p>
    <form class="formulario" @submit.prevent="anular">
      <template v-if="!esAdministrador">
        <label for="permiso-anular">Tu clave de permiso</label>
        <input id="permiso-anular" v-model="clavePermiso" type="password" autocomplete="new-password" maxlength="30" />
      </template>
      <p v-if="errorAlAnular" class="error" role="alert">{{ errorAlAnular }}</p>
      <button type="submit" class="boton boton--peligro" :disabled="anulando">Sí, anular</button>
      <button type="button" class="boton boton--suave" @click="porAnular = undefined">No, dejarla</button>
    </form>
  </BottomSheet>

  <BottomSheet v-if="porCambiar" titulo="Hay una venta sin cobrar" @cerrar="porCambiar = undefined">
    <p>Si cambias esta factura, se quita lo que habías elegido.</p>
    <button type="button" class="boton boton--principal" @click="cambiarVenta(porCambiar)">Quitarlo y cambiar la factura</button>
    <button type="button" class="boton boton--suave" @click="porCambiar = undefined">Volver</button>
  </BottomSheet>
</template>
