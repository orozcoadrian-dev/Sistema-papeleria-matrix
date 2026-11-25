import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './tests/e2e',
  fullyParallel: false,
  workers: 1,
  // Las pruebas corren con el tamaño y el toque de un teléfono.
  use: { ...devices['Pixel 7'], baseURL: 'http://127.0.0.1:5174', trace: 'retain-on-failure' },
  webServer: [
    {
      command: 'postman simulation run postman/simulations/matrix/matrix.sim.yaml --port 4020 --no-history --output ndjson > postman/simulation.log',
      url: 'http://127.0.0.1:4020/matrix-api/__ready',
      reuseExistingServer: false,
      timeout: 120000,
    },
    {
      command: 'npm run dev -- --port 5174 --strictPort',
      url: 'http://127.0.0.1:5174',
      env: { API_PROXY_TARGET: 'http://127.0.0.1:4020/matrix-api', VITE_API_BASE_URL: '/api' },
      reuseExistingServer: false,
    },
  ],
});
