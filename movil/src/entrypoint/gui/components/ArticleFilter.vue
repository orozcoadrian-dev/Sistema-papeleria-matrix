<script setup lang="ts">
import { TIPOS_DE_ARTICULO, TITULO_DEL_TIPO, type TipoDeArticulo } from '../../../domain/constants/articulos';
import { ICONO_DEL_TIPO } from '../context';

const busqueda = defineModel<string>('busqueda', { required: true });
const filtro = defineModel<TipoDeArticulo | 'todo'>('filtro', { required: true });
</script>

<template>
  <label class="buscador">
    <span class="oculto">Buscar artículo</span>
    <input v-model="busqueda" type="search" placeholder="🔍 Buscar por nombre" />
  </label>
  <div class="filtros" role="group" aria-label="Tipo de artículo">
    <button type="button" class="filtro" :aria-pressed="filtro === 'todo'" @click="filtro = 'todo'">✨ Todo</button>
    <button v-for="tipo in TIPOS_DE_ARTICULO" :key="tipo" type="button" class="filtro" :aria-pressed="filtro === tipo" @click="filtro = tipo">
      {{ ICONO_DEL_TIPO[tipo] }} {{ TITULO_DEL_TIPO[tipo] }}
    </button>
  </div>
</template>
