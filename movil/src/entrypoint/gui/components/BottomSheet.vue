<script setup lang="ts">
import { onMounted, ref } from 'vue';

/** Ventana que sube desde abajo. Se abre al montarse y avisa cuando se quiere cerrar. */
defineProps<{ titulo: string }>();
const emit = defineEmits<{ cerrar: [] }>();
const ventana = ref<HTMLDialogElement>();

onMounted(() => ventana.value?.showModal());
</script>

<template>
  <dialog ref="ventana" class="hoja" aria-labelledby="hoja-titulo" @cancel.prevent="emit('cerrar')" @click.self="emit('cerrar')">
    <div class="hoja__cuerpo">
      <h2 id="hoja-titulo">{{ titulo }}</h2>
      <slot />
    </div>
  </dialog>
</template>
