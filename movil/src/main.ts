import { createApp } from 'vue';
import { crearAplicacion } from './bootstrap/container';
import App from './entrypoint/gui/App.vue';
import { APLICACION, olvidarVenta } from './entrypoint/gui/context';
import { crearRouter } from './entrypoint/gui/router';
import './entrypoint/gui/styles.css';

const aplicacion = crearAplicacion(import.meta.env.VITE_API_BASE_URL || '/api', window.sessionStorage);
const router = crearRouter(aplicacion.auth);

aplicacion.cuandoVenzaLaSesion(() => {
  olvidarVenta();
  void router.replace({ name: 'entrar', query: { motivo: 'inactividad' } });
});

createApp(App).provide(APLICACION, aplicacion).use(router).mount('#app');
