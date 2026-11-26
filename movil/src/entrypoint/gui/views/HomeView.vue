<script setup lang="ts">
import { ChevronRight, Package, ReceiptText, ShoppingCart, TrendingUp, WifiOff } from 'lucide-vue-next';
import { onMounted, ref } from 'vue';
import { RouterLink } from 'vue-router';
import type { ResumenDeVentas } from '../../../application/dto/responses/responses';
import { mensajeDe, pesos, useAplicacion } from '../context';

const { auth, ventas } = useAplicacion();
const usuario = auth.usuarioActual();
const resumen = ref<ResumenDeVentas>();
const error = ref('');
const hoy = new Date().toLocaleDateString('es-CO', { weekday: 'long', day: 'numeric', month: 'long' });

async function cargar() {
  error.value = '';
  try {
    resumen.value = await ventas.resumen('hoy');
  } catch (fallo) {
    error.value = mensajeDe(fallo);
  }
}

onMounted(cargar);
</script>

<template>
  <header class="encabezado">
    <p class="encabezado__fecha">{{ hoy }}</p>
    <h1>¡Hola, {{ usuario?.nombre.split(' ')[0] }}!</h1>
    <p class="suave">{{ usuario?.esAdministrador ? 'Así va tu negocio hoy.' : 'Así van tus ventas de hoy.' }}</p>
  </header>

  <div v-if="error" class="tarjeta estado">
    <WifiOff :size="32" aria-hidden="true" />
    <p>{{ error }}</p>
    <button type="button" class="boton boton--suave" @click="cargar">Intentar de nuevo</button>
  </div>
  <div v-else class="cifras" aria-live="polite">
    <div class="cifra cifra--grande">
      <span><TrendingUp :size="16" aria-hidden="true" /> Vendido hoy</span>
      <strong>{{ resumen ? pesos(resumen.total) : '…' }}</strong>
    </div>
    <div class="cifra"><span>Ventas</span><strong>{{ resumen?.cantidad ?? '…' }}</strong></div>
    <div class="cifra"><span>Promedio</span><strong>{{ resumen ? (resumen.cantidad ? pesos(resumen.promedio) : '—') : '…' }}</strong></div>
  </div>

  <div class="accesos">
    <RouterLink :to="{ name: 'vender' }" class="acceso acceso--principal"><i><ShoppingCart :size="24" aria-hidden="true" /></i><b>Hacer una venta</b><small>Elige y cobra</small><ChevronRight :size="20" aria-hidden="true" /></RouterLink>
    <RouterLink :to="{ name: 'ventas' }" class="acceso"><i><ReceiptText :size="24" aria-hidden="true" /></i><b>Ver lo vendido</b><small>Hoy, semana o mes</small><ChevronRight :size="20" aria-hidden="true" /></RouterLink>
    <RouterLink :to="{ name: 'catalogo' }" class="acceso"><i><Package :size="24" aria-hidden="true" /></i><b>{{ usuario?.esAdministrador ? 'Mis artículos' : 'Consultar precios' }}</b><small>{{ usuario?.esAdministrador ? 'Agregar, cambiar o quitar' : 'Precios y existencias' }}</small><ChevronRight :size="20" aria-hidden="true" /></RouterLink>
  </div>
</template>
