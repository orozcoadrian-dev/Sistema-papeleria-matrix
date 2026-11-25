<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { RouterLink } from 'vue-router';
import type { ResumenDeVentas } from '../../../application/dto/responses/responses';
import { mensajeDe, pesos, useAplicacion } from '../context';

const { auth, ventas } = useAplicacion();
const usuario = auth.usuarioActual();
const resumen = ref<ResumenDeVentas>();
const error = ref('');

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
  <h1>¡Hola, {{ usuario?.nombre.split(' ')[0] }}! 👋</h1>
  <p class="suave">{{ usuario?.esAdministrador ? 'Así va tu negocio hoy.' : 'Así van tus ventas de hoy.' }}</p>

  <div v-if="error" class="tarjeta estado">
    <p>📡 {{ error }}</p>
    <button type="button" class="boton boton--suave" @click="cargar">Intentar de nuevo</button>
  </div>
  <div v-else class="cifras" aria-live="polite">
    <div class="cifra cifra--grande"><span>Vendido hoy</span><strong>{{ resumen ? pesos(resumen.total) : '…' }}</strong></div>
    <div class="cifra"><span>Ventas</span><strong>{{ resumen?.cantidad ?? '…' }}</strong></div>
  </div>

  <div class="accesos">
    <RouterLink :to="{ name: 'vender' }" class="acceso acceso--principal"><span aria-hidden="true">🛒</span><b>Hacer una venta</b><small>Elige y cobra</small></RouterLink>
    <RouterLink :to="{ name: 'ventas' }" class="acceso"><span aria-hidden="true">🧾</span><b>Ver lo vendido</b><small>Hoy, semana o mes</small></RouterLink>
    <RouterLink :to="{ name: 'catalogo' }" class="acceso"><span aria-hidden="true">📦</span><b>{{ usuario?.esAdministrador ? 'Mis artículos' : 'Consultar precios' }}</b><small>{{ usuario?.esAdministrador ? 'Agregar, cambiar o quitar' : 'Precios y existencias' }}</small></RouterLink>
  </div>
</template>
