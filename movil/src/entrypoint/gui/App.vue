<script setup lang="ts">
import { House, LogOut, Package, ReceiptText, ShoppingCart } from 'lucide-vue-next';
import { computed, onBeforeUnmount, onMounted } from 'vue';
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router';
import { aviso, carrito, olvidarVenta, useAplicacion } from './context';

const { auth } = useAplicacion();
const route = useRoute();
const router = useRouter();

// La sesión vive en el almacenamiento, que no es reactivo: se vuelve a leer al cambiar de ruta.
const usuario = computed(() => (route.fullPath ? auth.usuarioActual() : null));
const privada = computed(() => !route.meta.publica && usuario.value !== null);

const secciones = [
  { name: 'inicio', texto: 'Inicio', icono: House },
  { name: 'vender', texto: 'Vender', icono: ShoppingCart },
  { name: 'ventas', texto: 'Vendido', icono: ReceiptText },
  { name: 'catalogo', texto: 'Catálogo', icono: Package },
];

async function salir() {
  await auth.salir();
  olvidarVenta();
  await router.replace({ name: 'entrar', query: { motivo: 'salio' } });
}

const usar = () => auth.registrarUso();
let vigilante: ReturnType<typeof setInterval>;

function revisarSesion() {
  if (!route.meta.publica && !auth.usuarioActual()) {
    olvidarVenta();
    void router.replace({ name: 'entrar', query: { motivo: 'inactividad' } });
  }
}

onMounted(() => {
  window.addEventListener('pointerdown', usar, { passive: true });
  window.addEventListener('keydown', usar);
  // El teléfono detiene los relojes en segundo plano: al volver se revisa de una vez.
  document.addEventListener('visibilitychange', revisarSesion);
  vigilante = setInterval(revisarSesion, 30000);
});

onBeforeUnmount(() => {
  window.removeEventListener('pointerdown', usar);
  window.removeEventListener('keydown', usar);
  document.removeEventListener('visibilitychange', revisarSesion);
  clearInterval(vigilante);
});
</script>

<template>
  <header v-if="privada" class="barra">
    <img src="/logo.ico" alt="" width="32" height="32" />
    <div class="barra__nombre">
      <strong>{{ usuario?.nombre }}</strong>
      <span>{{ usuario?.rolNombre }}</span>
    </div>
    <button type="button" class="barra__salir" @click="salir"><LogOut :size="20" aria-hidden="true" /> Salir</button>
  </header>

  <main :class="privada ? 'contenido' : 'contenido contenido--publico'">
    <RouterView :key="route.path" />
  </main>

  <nav v-if="privada" class="menu" aria-label="Secciones">
    <RouterLink v-for="seccion in secciones" :key="seccion.name" :to="{ name: seccion.name }" class="menu__item">
      <component :is="seccion.icono" :size="24" aria-hidden="true" />
      <span>{{ seccion.texto }}</span>
      <b v-if="seccion.name === 'vender' && carrito.unidades" class="menu__insignia">{{ carrito.unidades }}</b>
    </RouterLink>
  </nav>

  <p v-if="aviso.texto" class="aviso-flotante" :class="{ 'aviso-flotante--error': aviso.esError }" role="status">{{ aviso.texto }}</p>
</template>
