<script setup lang="ts">
import { CircleCheck, Minus, Plus, SearchX, ShoppingCart, Trash2, WifiOff } from 'lucide-vue-next';
import { computed, onMounted, ref } from 'vue';
import { POCAS_UNIDADES } from '../../../domain/constants/articulos';
import { ErrorDeApi } from '../../../domain/exceptions/errors';
import type { Articulo } from '../../../domain/model/Articulo';
import { claveDe } from '../../../domain/model/Carrito';
import type { Operador, Venta } from '../../../domain/model/Venta';
import { calcularCambio, leerEntero, pagosRapidos } from '../../../domain/valueobjects/dinero';
import ArticleFilter from '../components/ArticleFilter.vue';
import BottomSheet from '../components/BottomSheet.vue';
import { ICONO_DEL_TIPO, avisar, carrito, mensajeDe, operadorElegido, pesos, useAplicacion } from '../context';
import { useCatalog } from '../useCatalog';

const { auth, ventas } = useAplicacion();
const esAdministrador = auth.usuarioActual()?.esAdministrador ?? false;
const { visibles, cargando, error, busqueda, filtro, cargar } = useCatalog((articulos) => {
  carrito.value = carrito.value.actualizar(articulos);
});

const operadores = ref<Operador[]>([]);
const cobrando = ref(false);
const registrando = ref(false);
const pago = ref('');
const errorDeCobro = ref('');
const recibo = ref<{ venta: Venta; devolver: number }>();

const cambio = computed(() => (pago.value.trim() ? calcularCambio(carrito.value.total, pago.value) : undefined));

onMounted(async () => {
  if (!esAdministrador) return;
  try {
    operadores.value = await ventas.operadores();
    if (operadores.value.length === 1) operadorElegido.value = operadores.value[0]?.id;
  } catch (fallo) {
    avisar(mensajeDe(fallo), true);
  }
});

function cambiar(articulo: Articulo, cuantos: number) {
  try {
    carrito.value = carrito.value.cambiar(articulo, cuantos);
    if (!carrito.value.unidades) cobrando.value = false;
  } catch (fallo) {
    avisar(mensajeDe(fallo), true);
  }
}

function existencias(articulo: Articulo): string {
  const hay = articulo.existencias;
  if (hay === null) return '';
  if (articulo.tipo === 'ropa') return hay === 0 ? 'Vendida' : carrito.value.cantidadDe(articulo) ? 'Ya está en la venta' : 'Pieza única';
  if (hay === 0) return 'Agotado';
  const quedan = hay - carrito.value.cantidadDe(articulo);
  return quedan <= 0 ? 'Ya agregaste todas' : quedan === 1 ? 'Queda 1' : `Quedan ${quedan}`;
}

function abrirCobro() {
  pago.value = '';
  errorDeCobro.value = '';
  cobrando.value = true;
}

async function registrar() {
  errorDeCobro.value = '';
  if (cambio.value?.estado === 'falta') {
    errorDeCobro.value = 'Lo que paga no alcanza. Revisa el valor o déjalo vacío.';
    return;
  }
  registrando.value = true;
  try {
    const recibido = leerEntero(pago.value);
    const venta = await ventas.cobrar({ carrito: carrito.value, operadorId: operadorElegido.value });
    recibo.value = { venta, devolver: Number.isFinite(recibido) ? Math.max(0, recibido - venta.total) : 0 };
    carrito.value = carrito.value.actualizar([]);
    cobrando.value = false;
    busqueda.value = '';
    await cargar();
  } catch (fallo) {
    errorDeCobro.value = mensajeDe(fallo);
    // Alguien vendió lo mismo, cambió un precio o borró el artículo: se vuelve a leer el catálogo.
    if (fallo instanceof ErrorDeApi && (fallo.estado === 409 || fallo.estado === 404)) await cargar();
  } finally {
    registrando.value = false;
  }
}
</script>

<template>
  <h1>Vender</h1>
  <p class="suave">Toca lo que se lleva el cliente y después «Cobrar».</p>

  <label v-if="esAdministrador" class="campo">
    <span>La venta queda a nombre de</span>
    <select v-model="operadorElegido">
      <option :value="undefined" disabled>Elige el operador</option>
      <option v-for="operador in operadores" :key="operador.id" :value="operador.id">{{ operador.nombre }}</option>
    </select>
  </label>

  <ArticleFilter v-model:busqueda="busqueda" v-model:filtro="filtro" />

  <div v-if="cargando" class="mosaico" role="status" aria-label="Cargando artículos">
    <div v-for="n in 6" :key="n" class="esqueleto esqueleto--articulo" />
  </div>
  <div v-else-if="error" class="tarjeta estado">
    <WifiOff :size="32" aria-hidden="true" />
    <p>{{ error }}</p>
    <button type="button" class="boton boton--suave" @click="cargar">Intentar de nuevo</button>
  </div>
  <div v-else-if="!visibles.length" class="estado"><SearchX :size="32" aria-hidden="true" /><p>No encontramos artículos.</p></div>

  <div v-else class="mosaico">
    <button
      v-for="articulo in visibles" :key="claveDe(articulo)" type="button" class="articulo"
      :class="{ 'articulo--elegido': carrito.cantidadDe(articulo) }" :disabled="articulo.existencias === 0"
      @click="cambiar(articulo, 1)"
    >
      <span class="sello" :class="`sello--${articulo.tipo}`" aria-hidden="true"><component :is="ICONO_DEL_TIPO[articulo.tipo]" :size="22" /></span>
      <span class="articulo__nombre">{{ articulo.nombre }}</span>
      <strong class="articulo__precio">{{ pesos(articulo.precio) }}</strong>
      <small v-if="articulo.detalle">{{ articulo.detalle }}</small>
      <small v-if="existencias(articulo)" :class="{ pocas: (articulo.existencias ?? 0) - carrito.cantidadDe(articulo) < POCAS_UNIDADES }">{{ existencias(articulo) }}</small>
      <b v-if="carrito.cantidadDe(articulo)" class="articulo__insignia">×{{ carrito.cantidadDe(articulo) }}</b>
    </button>
  </div>

  <div v-if="carrito.unidades" class="barra-cobro">
    <button type="button" class="boton boton--dorado" @click="abrirCobro">
      <span><ShoppingCart :size="20" aria-hidden="true" /> {{ carrito.unidades === 1 ? '1 artículo' : `${carrito.unidades} artículos` }}</span>
      <strong>Cobrar {{ pesos(carrito.total) }}</strong>
    </button>
  </div>

  <BottomSheet v-if="cobrando" titulo="Cobrar" @cerrar="cobrando = false">
    <ul class="lineas">
      <li v-for="linea in carrito.lineas" :key="claveDe(linea.articulo)">
        <div><b>{{ linea.articulo.nombre }}</b><small>{{ pesos(linea.articulo.precio) }} c/u</small></div>
        <div class="cantidad">
          <button type="button" :aria-label="`Uno menos de ${linea.articulo.nombre}`" @click="cambiar(linea.articulo, -1)"><component :is="linea.cantidad === 1 ? Trash2 : Minus" :size="18" aria-hidden="true" /></button>
          <span>{{ linea.cantidad }}</span>
          <button type="button" :aria-label="`Uno más de ${linea.articulo.nombre}`" @click="cambiar(linea.articulo, 1)"><Plus :size="18" aria-hidden="true" /></button>
        </div>
        <strong>{{ pesos(linea.articulo.precio * linea.cantidad) }}</strong>
      </li>
    </ul>
    <p class="total"><span>Total</span><strong>{{ pesos(carrito.total) }}</strong></p>

    <form class="formulario" @submit.prevent="registrar">
      <label for="pago">¿Con cuánto paga? (opcional)</label>
      <input id="pago" v-model="pago" inputmode="numeric" autocomplete="off" />
      <div class="filtros">
        <button v-for="billete in pagosRapidos(carrito.total)" :key="billete" type="button" class="filtro" @click="pago = String(billete)">
          {{ billete === carrito.total ? 'Exacto' : pesos(billete) }}
        </button>
      </div>
      <p v-if="cambio" class="cambio" :class="{ 'cambio--mal': cambio.estado === 'falta' || cambio.estado === 'ilegible' }" role="status">
        <template v-if="cambio.estado === 'ilegible'">Escribe solo el número, por ejemplo 20000</template>
        <template v-else-if="cambio.estado === 'falta'">Faltan {{ pesos(cambio.valor) }}</template>
        <template v-else-if="cambio.estado === 'exacto'">Pago exacto, sin cambio.</template>
        <template v-else>Hay que devolver {{ pesos(cambio.valor) }}</template>
      </p>
      <p v-if="errorDeCobro" class="error" role="alert">{{ errorDeCobro }}</p>
      <button type="submit" class="boton boton--principal" :disabled="registrando">{{ registrando ? 'Registrando…' : 'Listo, registrar venta' }}</button>
      <button type="button" class="boton boton--suave" @click="cobrando = false">Seguir eligiendo</button>
    </form>
  </BottomSheet>

  <BottomSheet v-if="recibo" titulo="Venta registrada" @cerrar="recibo = undefined">
    <CircleCheck class="exito" :size="56" aria-hidden="true" />
    <ul class="recibo">
      <li v-for="(linea, indice) in recibo.venta.articulos" :key="indice"><span>{{ linea.cantidad }} × {{ linea.nombre }}</span></li>
    </ul>
    <p class="total"><span>Total</span><strong>{{ pesos(recibo.venta.total) }}</strong></p>
    <p v-if="recibo.devolver" class="cambio">Devolver {{ pesos(recibo.devolver) }}</p>
    <p class="suave">Factura n.º {{ recibo.venta.codigo }}</p>
    <button type="button" class="boton boton--principal" @click="recibo = undefined">Nueva venta</button>
  </BottomSheet>
</template>
