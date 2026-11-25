import { createRouter, createWebHistory } from 'vue-router';
import type { AuthUseCases } from '../../application/ports/in/use-cases';
import ArticleFormView from './views/ArticleFormView.vue';
import CatalogView from './views/CatalogView.vue';
import HomeView from './views/HomeView.vue';
import LoginView from './views/LoginView.vue';
import SalesView from './views/SalesView.vue';
import SellView from './views/SellView.vue';

export function crearRouter(auth: AuthUseCases) {
  const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
      { path: '/entrar', name: 'entrar', component: LoginView, meta: { publica: true } },
      { path: '/', name: 'inicio', component: HomeView },
      { path: '/vender', name: 'vender', component: SellView },
      { path: '/ventas', name: 'ventas', component: SalesView },
      { path: '/catalogo', name: 'catalogo', component: CatalogView },
      { path: '/articulos/:tipo(servicio|producto|ropa)/nuevo', name: 'articulo-nuevo', component: ArticleFormView, meta: { soloAdministrador: true } },
      { path: '/articulos/:tipo(servicio|producto|ropa)/:id(\\d+)', name: 'articulo-editar', component: ArticleFormView, meta: { soloAdministrador: true } },
      { path: '/:resto(.*)*', redirect: '/' },
    ],
    scrollBehavior: () => ({ top: 0 }),
  });

  router.beforeEach((destino) => {
    const usuario = auth.usuarioActual();
    if (destino.meta.publica) return usuario ? { name: 'inicio' } : true;
    if (!usuario) return { name: 'entrar', query: { volver: destino.fullPath } };
    if (destino.meta.soloAdministrador && !usuario.esAdministrador) return { name: 'catalogo' };
    return true;
  });

  return router;
}
