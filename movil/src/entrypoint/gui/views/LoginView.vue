<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ErrorDeApi, ErrorDeDominio, type ErroresPorCampo } from '../../../domain/exceptions/errors';
import { mensajeDe, useAplicacion } from '../context';

const { auth } = useAplicacion();
const route = useRoute();
const router = useRouter();

const AVISOS: Record<string, string> = {
  inactividad: 'Tu sesión se cerró porque pasó mucho tiempo sin usar el sistema. Vuelve a entrar.',
  salio: 'Saliste del sistema. ¡Hasta pronto!',
};
const avisoDeEntrada = AVISOS[String(route.query.motivo ?? '')];

const formulario = reactive({ usuario: '', contrasenia: '' });
const errores = ref<ErroresPorCampo>({});
const mensaje = ref('');
const entrando = ref(false);
const verClave = ref(false);

/** Solo se vuelve a una pantalla de esta aplicación, nunca a otra dirección. */
function destino(): string {
  const volver = String(route.query.volver ?? '');
  return volver.startsWith('/') && !volver.startsWith('//') ? volver : '/';
}

async function entrar() {
  errores.value = {};
  mensaje.value = '';
  entrando.value = true;
  const despertando = setTimeout(() => {
    mensaje.value = 'Estamos despertando el sistema. La primera vez del día puede tardar hasta un minuto ☕';
  }, 4000);
  try {
    await auth.entrar({ ...formulario });
    await router.replace(destino());
  } catch (error) {
    if (error instanceof ErrorDeDominio) errores.value = error.campos;
    if (error instanceof ErrorDeDominio) mensaje.value = '';
    else if (error instanceof ErrorDeApi && error.estado === 401) mensaje.value = 'El usuario o la contraseña no son correctos. Revisa las mayúsculas.';
    else mensaje.value = mensajeDe(error);
  } finally {
    clearTimeout(despertando);
    entrando.value = false;
  }
}
</script>

<template>
  <section class="entrada">
    <img src="/logo.ico" alt="" width="72" height="72" class="entrada__logo" />
    <h1>Papelería <span>Matrix</span></h1>
    <p>Entra con tu usuario para usar el sistema.</p>
    <p v-if="avisoDeEntrada" class="nota">{{ avisoDeEntrada }}</p>

    <form class="tarjeta formulario" novalidate @submit.prevent="entrar">
      <label for="usuario">Usuario</label>
      <input id="usuario" v-model="formulario.usuario" autocomplete="username" autocapitalize="none" spellcheck="false" maxlength="20" :aria-invalid="!!errores.usuario" />
      <small v-if="errores.usuario" class="error">{{ errores.usuario }}</small>

      <label for="contrasenia">Contraseña</label>
      <div class="clave">
        <input id="contrasenia" v-model="formulario.contrasenia" :type="verClave ? 'text' : 'password'" autocomplete="current-password" maxlength="255" :aria-invalid="!!errores.contrasenia" />
        <button type="button" class="boton boton--suave" :aria-pressed="verClave" @click="verClave = !verClave">{{ verClave ? 'Ocultar' : 'Ver' }}</button>
      </div>
      <small v-if="errores.contrasenia" class="error">{{ errores.contrasenia }}</small>

      <button type="submit" class="boton boton--principal" :disabled="entrando">{{ entrando ? 'Entrando…' : 'Entrar' }}</button>
      <p class="error" role="status">{{ mensaje }}</p>
    </form>
  </section>
</template>
