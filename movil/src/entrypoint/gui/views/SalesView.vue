<script setup lang="ts">
import { ReceiptText, Undo2, WifiOff } from 'lucide-vue-next';
import { onMounted, ref } from 'vue';
import { RouterLink } from 'vue-router';
import type { Periodo, ResumenDeVentas } from '../../../application/dto/responses/responses';
import { fechaIso } from '../../../application/services/SaleService';
import { ErrorDeApi } from '../../../domain/exceptions/errors';
import type { Venta } from '../../../domain/model/Venta';
import BottomSheet from '../components/BottomSheet.vue';
import { avisar, mensajeDe, pesos, useAplicacion } from '../context';

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
let pedido = 0;

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
  try {
    await ventas.anular(venta.id);
    avisar(`Se anuló la venta de ${pesos(venta.total)}.`);
  } catch (fallo) {
    if (fallo instanceof ErrorDeApi && fallo.estado === 404) avisar('Esa venta ya estaba anulada.');
    else avisar(mensajeDe(fallo), true);
  } finally {
    anulando.value = false;
    porAnular.value = undefined;
    await cargar();
  }
}

onMounted(cargar);
</script>

<template>
  <h1>Lo vendido</h1>
  <p class="suave">{{ esAdministrador ? 'Todas las ventas del negocio.' : 'Solo aparecen las ventas que registraste tú.' }}</p>

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
      <button v-if="esAdministrador" type="button" class="boton boton--peligro" :aria-label="`Anular la venta ${venta.codigo}`" @click="porAnular = venta"><Undo2 :size="18" aria-hidden="true" /> Anular venta</button>
    </article>
  </template>

  <BottomSheet v-if="porAnular" :titulo="`¿Anular la venta de ${pesos(porAnular.total)}?`" @cerrar="porAnular = undefined">
    <p>La venta se borra y la papelería vuelve a las existencias. No se puede deshacer.</p>
    <button type="button" class="boton boton--peligro" :disabled="anulando" @click="anular">Sí, anular</button>
    <button type="button" class="boton boton--suave" @click="porAnular = undefined">No, dejarla</button>
  </BottomSheet>
</template>
