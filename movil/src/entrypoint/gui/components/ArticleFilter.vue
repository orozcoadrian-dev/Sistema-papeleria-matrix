<script setup lang="ts">
import { LayoutGrid, Search } from 'lucide-vue-next';
import { TIPOS_DE_ARTICULO, TITULO_DEL_TIPO, type TipoDeArticulo } from '../../../domain/constants/articulos';
import { ICONO_DEL_TIPO } from '../context';

const busqueda = defineModel<string>('busqueda', { required: true });
const filtro = defineModel<TipoDeArticulo | 'todo'>('filtro', { required: true });
</script>

<template>
  <label class="buscador">
    <span class="oculto">Buscar artículo</span>
    <Search :size="20" aria-hidden="true" />
    <input v-model="busqueda" type="search" placeholder="Buscar por nombre" />
  </label>
  <div class="filtros filtros--tipos" role="group" aria-label="Tipo de artículo">
    <button type="button" class="filtro" :aria-pressed="filtro === 'todo'" @click="filtro = 'todo'"><LayoutGrid :size="17" aria-hidden="true" /> Todo</button>
    <button v-for="tipo in TIPOS_DE_ARTICULO" :key="tipo" type="button" class="filtro" :aria-pressed="filtro === tipo" @click="filtro = tipo">
      <component :is="ICONO_DEL_TIPO[tipo]" :size="17" aria-hidden="true" /> {{ TITULO_DEL_TIPO[tipo] }}
    </button>
  </div>
</template>
