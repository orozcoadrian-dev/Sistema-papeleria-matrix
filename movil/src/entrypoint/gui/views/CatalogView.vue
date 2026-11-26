<script setup lang="ts">
import { Pencil, Plus, SearchX, Trash2, WifiOff } from 'lucide-vue-next';
import { ref } from 'vue';
import { RouterLink } from 'vue-router';
import { TIPOS_DE_ARTICULO, TITULO_DEL_TIPO } from '../../../domain/constants/articulos';
import type { Articulo } from '../../../domain/model/Articulo';
import { claveDe } from '../../../domain/model/Carrito';
import ArticleFilter from '../components/ArticleFilter.vue';
import BottomSheet from '../components/BottomSheet.vue';
import { ICONO_DEL_TIPO, avisar, mensajeDe, pesos, useAplicacion } from '../context';
import { useCatalog } from '../useCatalog';

const { auth, catalogo } = useAplicacion();
const esAdministrador = auth.usuarioActual()?.esAdministrador ?? false;
const { visibles, cargando, error, busqueda, filtro, cargar } = useCatalog();

const agregando = ref(false);
const porEliminar = ref<Articulo>();
const eliminando = ref(false);

function existencias(articulo: Articulo): string {
  if (articulo.existencias === null) return '';
  if (articulo.tipo === 'ropa') return articulo.existencias ? 'Disponible' : 'Vendida';
  return articulo.existencias ? `Hay ${articulo.existencias}` : 'Agotado';
}

async function eliminar() {
  const articulo = porEliminar.value;
  if (!articulo) return;
  eliminando.value = true;
  try {
    await catalogo.eliminar(articulo.tipo, articulo.id);
    avisar(`Se eliminó «${articulo.nombre}».`);
    await cargar();
  } catch (fallo) {
    avisar(mensajeDe(fallo), true);
  } finally {
    eliminando.value = false;
    porEliminar.value = undefined;
  }
}
</script>

<template>
  <h1>{{ esAdministrador ? 'Mis artículos' : 'Catálogo' }}</h1>
  <button v-if="esAdministrador" type="button" class="boton boton--principal" @click="agregando = true"><Plus :size="20" aria-hidden="true" /> Agregar artículo</button>

  <ArticleFilter v-model:busqueda="busqueda" v-model:filtro="filtro" />

  <div v-if="cargando" class="lista" role="status" aria-label="Cargando artículos">
    <div v-for="n in 4" :key="n" class="esqueleto" />
  </div>
  <div v-else-if="error" class="tarjeta estado">
    <WifiOff :size="32" aria-hidden="true" />
    <p>{{ error }}</p>
    <button type="button" class="boton boton--suave" @click="cargar">Intentar de nuevo</button>
  </div>
  <div v-else-if="!visibles.length" class="estado"><SearchX :size="32" aria-hidden="true" /><p>No encontramos artículos.</p></div>

  <ul v-else class="lista">
    <li v-for="articulo in visibles" :key="claveDe(articulo)" class="tarjeta ficha">
      <span class="sello" :class="`sello--${articulo.tipo}`" aria-hidden="true"><component :is="ICONO_DEL_TIPO[articulo.tipo]" :size="22" /></span>
      <div class="ficha__texto">
        <b>{{ articulo.nombre }}</b>
        <small>{{ [articulo.detalle, existencias(articulo)].filter(Boolean).join(' · ') }}</small>
      </div>
      <strong>{{ pesos(articulo.precio) }}</strong>
      <div v-if="esAdministrador" class="ficha__acciones">
        <RouterLink :to="{ name: 'articulo-editar', params: { tipo: articulo.tipo, id: articulo.id } }" class="boton boton--suave" :aria-label="`Editar ${articulo.nombre}`"><Pencil :size="17" aria-hidden="true" /> Editar</RouterLink>
        <button type="button" class="boton boton--peligro" :aria-label="`Eliminar ${articulo.nombre}`" @click="porEliminar = articulo"><Trash2 :size="17" aria-hidden="true" /> Eliminar</button>
      </div>
    </li>
  </ul>

  <BottomSheet v-if="agregando" titulo="¿Qué quieres agregar?" @cerrar="agregando = false">
    <RouterLink v-for="tipo in TIPOS_DE_ARTICULO" :key="tipo" :to="{ name: 'articulo-nuevo', params: { tipo } }" class="boton boton--suave">
      <component :is="ICONO_DEL_TIPO[tipo]" :size="20" aria-hidden="true" /> {{ TITULO_DEL_TIPO[tipo] }}
    </RouterLink>
    <button type="button" class="boton boton--suave" @click="agregando = false">Cancelar</button>
  </BottomSheet>

  <BottomSheet v-if="porEliminar" :titulo="`¿Eliminar «${porEliminar.nombre}»?`" @cerrar="porEliminar = undefined">
    <p>Sale del catálogo y no se puede deshacer.</p>
    <button type="button" class="boton boton--peligro" :disabled="eliminando" @click="eliminar">Sí, eliminar</button>
    <button type="button" class="boton boton--suave" @click="porEliminar = undefined">No, dejarlo</button>
  </BottomSheet>
</template>
