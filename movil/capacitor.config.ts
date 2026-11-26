import type { CapacitorConfig } from '@capacitor/cli';

// Capacitor empaqueta la aplicación Vue ya compilada (dist) dentro de una aplicación de Android.
const config: CapacitorConfig = {
  appId: 'com.papeleriamatrix.app',
  appName: 'Papelería Matrix',
  webDir: 'dist',
  backgroundColor: '#2A014F',
};

export default config;
