<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { RouterLink, useRoute, useRouter } from 'vue-router';
import { CAMPOS, TITULO_DEL_TIPO, type TipoDeArticulo } from '../../../domain/constants/articulos';
import { ErrorDeApi, ErrorDeDominio, type ErroresPorCampo } from '../../../domain/exceptions/errors';
import { avisar, mensajeDe, useAplicacion } from '../context';

const { catalogo } = useAplicacion();
const route = useRoute();
const router = useRouter();

const tipo = route.params.tipo as TipoDeArticulo;
const id = route.params.id ? Number(route.params.id) : undefined;
const campos = CAMPOS[tipo];
const titulo = computed(() => `${id ? 'Editar' : 'Agregar'}: ${TITULO_DEL_TIPO[tipo].toLowerCase()}`);

const formulario = reactive<Record<string, string>>(Object.fromEntries(campos.map((campo) => [campo.nombre, ''])));
const errores = ref<ErroresPorCampo>({});
const mensaje = ref('');
const cargando = ref(id !== undefined);
const noEncontrado = ref(false);
const guardando = ref(false);
let cantidadAnterior: number | undefined;

onMounted(async () => {
  if (id === undefined) return;
  try {
    const articulo = (await catalogo.listar()).find((a) => a.tipo === tipo && a.id === id);
    if (!articulo) {
      noEncontrado.value = true;
      mensaje.value = 'Ese artículo ya no existe.';
      return;
    }
    Object.assign(formulario, articulo.datos);
    if (tipo === 'producto') cantidadAnterior = articulo.existencias ?? undefined;
  } catch (fallo) {
    noEncontrado.value = true;
    mensaje.value = mensajeDe(fallo);
  } finally {
    cargando.value = false;
  }
});

async function guardar() {
  errores.value = {};
  mensaje.value = '';
  guardando.value = true;
  try {
    await catalogo.guardar({ tipo, id, formulario: { ...formulario }, cantidadAnterior });
    avisar(`✓ Se guardó «${formulario.nombre || formulario.tipoRopa}».`);
    await router.push({ name: 'catalogo' });
  } catch (fallo) {
    if (fallo instanceof ErrorDeDominio || fallo instanceof ErrorDeApi) {
      // La API llama precioVenta a lo que aquí es el precio.
      const { precioVenta, ...resto } = fallo.campos;
      errores.value = precioVenta ? { ...resto, precio: precioVenta } : resto;
    }
    mensaje.value = mensajeDe(fallo);
  } finally {
    guardando.value = false;
  }
}
</script>

<template>
  <h1>{{ titulo }}</h1>
  <p v-if="cargando" class="estado" role="status">Cargando…</p>
  <div v-else-if="noEncontrado" class="tarjeta estado">
    <p>{{ mensaje }}</p>
    <RouterLink :to="{ name: 'catalogo' }" class="boton boton--suave">Volver al catálogo</RouterLink>
  </div>
  <form v-else class="tarjeta formulario" novalidate @submit.prevent="guardar">
    <template v-for="campo in campos" :key="campo.nombre">
      <label :for="campo.nombre">{{ campo.etiqueta }}<span v-if="!campo.obligatorio" class="suave"> (opcional)</span></label>
      <input :id="campo.nombre" v-model="formulario[campo.nombre]" :maxlength="campo.maximo" :inputmode="campo.clase === 'texto' ? 'text' : 'numeric'" autocomplete="off" :aria-invalid="!!errores[campo.nombre]" />
      <small v-if="errores[campo.nombre]" class="error">{{ errores[campo.nombre] }}</small>
    </template>
    <p v-if="mensaje" class="error" role="alert">{{ mensaje }}</p>
    <button type="submit" class="boton boton--principal" :disabled="guardando">{{ guardando ? 'Guardando…' : 'Guardar' }}</button>
    <RouterLink :to="{ name: 'catalogo' }" class="boton boton--suave">Cancelar</RouterLink>
  </form>
</template>
