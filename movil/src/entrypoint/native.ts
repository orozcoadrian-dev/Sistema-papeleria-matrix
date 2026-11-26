import { App } from '@capacitor/app';
import { Capacitor, CapacitorHttp, SystemBars, SystemBarsStyle, SystemBarType } from '@capacitor/core';
import type { Router } from 'vue-router';
import { crearPedirNativo } from '../adapters/http/pedirNativo';

/** En la aplicación instalada las peticiones las hace Android; en el navegador se usa fetch. */
export const pedirDelTelefono = (): typeof fetch | undefined =>
  Capacitor.isNativePlatform() ? crearPedirNativo((solicitud) => CapacitorHttp.request(solicitud)) : undefined;

export function prepararTelefono(router: Router): void {
  if (!Capacitor.isNativePlatform()) return;

  // El botón «Atrás» de Android cierra la ventana abierta, vuelve de pantalla o minimiza.
  void App.addListener('backButton', ({ canGoBack }) => {
    const ventana = document.querySelector('dialog[open]');
    const ruta = router.currentRoute.value;
    if (ventana) ventana.dispatchEvent(new Event('cancel', { cancelable: true }));
    else if (canGoBack && ruta.name !== 'inicio' && !ruta.meta.publica) router.back();
    else void App.minimizeApp();
  });

  // Iconos claros sobre la barra morada; oscuros sobre el menú blanco de abajo.
  router.afterEach((destino) => {
    void SystemBars.setStyle({ style: SystemBarsStyle.Dark, bar: SystemBarType.StatusBar });
    void SystemBars.setStyle({
      style: destino.meta.publica ? SystemBarsStyle.Dark : SystemBarsStyle.Light,
      bar: SystemBarType.NavigationBar,
    });
  });
}
