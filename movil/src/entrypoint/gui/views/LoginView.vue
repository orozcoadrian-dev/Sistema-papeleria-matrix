<script setup lang="ts">
import { Eye, EyeOff, LockKeyhole, UserRound } from 'lucide-vue-next';
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
    mensaje.value = 'Estamos despertando el sistema. La primera vez del día puede tardar hasta un minuto.';
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
    <div class="entrada__marca">
      <img src="/logo.png" alt="" width="104" height="104" />
      <h1>Papelería <span>Matrix</span></h1>
      <p>Ventas e inventario en tu teléfono</p>
    </div>
    <p v-if="avisoDeEntrada" class="nota">{{ avisoDeEntrada }}</p>

    <form class="tarjeta formulario entrada__panel" novalidate @submit.prevent="entrar">
      <h2>Iniciar sesión</h2>
      <label for="usuario">Usuario</label>
      <div class="con-icono">
        <UserRound :size="20" aria-hidden="true" />
        <input id="usuario" v-model="formulario.usuario" autocomplete="username" autocapitalize="none" spellcheck="false" maxlength="20" :aria-invalid="!!errores.usuario" />
      </div>
      <small v-if="errores.usuario" class="error">{{ errores.usuario }}</small>

      <label for="contrasenia">Contraseña</label>
      <div class="con-icono">
        <LockKeyhole :size="20" aria-hidden="true" />
        <input id="contrasenia" v-model="formulario.contrasenia" :type="verClave ? 'text' : 'password'" autocomplete="current-password" maxlength="255" :aria-invalid="!!errores.contrasenia" />
        <button type="button" class="boton-icono" :aria-label="verClave ? 'Ocultar clave' : 'Ver clave'" :aria-pressed="verClave" @click="verClave = !verClave">
          <component :is="verClave ? EyeOff : Eye" :size="20" aria-hidden="true" />
        </button>
      </div>
      <small v-if="errores.contrasenia" class="error">{{ errores.contrasenia }}</small>

      <button type="submit" class="boton boton--principal" :disabled="entrando">{{ entrando ? 'Entrando…' : 'Entrar' }}</button>
      <p class="error" role="status">{{ mensaje }}</p>
    </form>
  </section>
</template>
