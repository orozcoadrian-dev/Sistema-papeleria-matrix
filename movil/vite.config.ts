import { defineConfig, loadEnv } from 'vite';
import vue from '@vitejs/plugin-vue';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  return {
    plugins: [vue()],
    server: {
      // En el computador, Vite hace de intermediario: el teléfono habla con Vite y Vite habla con la API.
      proxy: {
        '/api': {
          target: env.API_PROXY_TARGET || 'http://localhost:8080',
          changeOrigin: true,
          // El backend solo admite el origen del frontend web; la petición sale de Vite, sin origen de navegador.
          configure: (proxy) => {
            proxy.on('proxyReq', (peticion) => peticion.removeHeader('origin'));
          },
        },
      },
    },
  };
});
