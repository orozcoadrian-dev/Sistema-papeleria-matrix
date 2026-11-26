# Papelería Matrix móvil

Versión para teléfono de Papelería Matrix, hecha con Vue 3 y TypeScript. Usa la misma API que la versión web y no cambia `frontend/` ni `backend/`.

## Qué hace

| Pantalla | Administradora | Operador |
|----------|----------------|----------|
| Entrar y salir | Sí | Sí |
| Inicio con lo vendido hoy | Todas las ventas | Sus ventas |
| Vender y cobrar con cálculo del cambio | A nombre de un operador | A su nombre |
| Lo vendido (hoy, 7 días, 30 días) | Ve todo, cambia y anula | Ve, cambia y anula lo suyo con su clave de permiso |
| Catálogo | Crea, edita y elimina | Solo consulta |

## Cómo abrirlo

Se necesita Node 22 y el backend encendido.

1. En una terminal, dentro de `backend`, encender la API en modo demo:

   ```powershell
   mvn spring-boot:run "-Dspring-boot.run.profiles=demo"
   ```

2. En otra terminal, dentro de `movil`:

   ```powershell
   npm install
   ```

   ```powershell
   npm run dev
   ```

3. En el computador, abrir `http://localhost:5173`. Los usuarios de prueba están al final de `backend/src/main/resources/demo/datos-demo.sql`.

### En un teléfono

El teléfono y el computador deben estar en la misma red wifi. Al correr `npm run dev`, Vite muestra una dirección `Network`, por ejemplo `http://192.168.1.20:5173`. Esa se escribe en el teléfono. Si no abre, Windows puede estar bloqueando Node en el firewall.

### Si la API está en otra dirección

Se copia `.env.example` como `.env` y se cambia `API_PROXY_TARGET`.

## Comprobaciones

| Orden | Qué comprueba |
|-------|---------------|
| `npm test` | Reglas, casos de uso y conexión (Vitest) |
| `npm run build` | Tipos y compilación de producción |
| `npm run test:e2e` | Recorridos en un navegador con tamaño de teléfono contra la simulación de Postman |

Para las pruebas de navegador se necesita Postman CLI y el navegador de Playwright (`npx playwright install chromium`).

## Publicada en internet

La versión web está en `https://matrix-movil.vercel.app`, en el proyecto `matrix-movil` de Vercel. `vercel.json` pasa las peticiones de `/api` al servidor de Render. Si cambia la dirección de la app o la del servidor, se actualiza ese archivo.

Para instalarla en un iPhone se abre en Safari, se toca **Compartir** y luego **Agregar a inicio**. En Android, desde el menú de Chrome.

## APK para Android

GitHub genera el APK en cada cambio de una rama que toque `movil/`. Se descarga en la pestaña Actions, proceso **APK móvil**, archivo `PapeleriaMatrix-apk`. En `main` se lanza a mano con **Run workflow**.

Para instalarlo se pasa `PapeleriaMatrix.apk` al teléfono, se abre y se permite instalar aplicaciones de esa fuente. Si Android no deja actualizar una versión anterior, se desinstala primero.

El APK usa la API escrita en `.env.apk`. El servidor gratuito tarda en despertar la primera vez. Con Android Studio también se puede compilar en el computador:

```powershell
npm run build:apk
```

```powershell
npx cap open android
```

## Estructura

```
src/domain/           reglas del negocio, sin Vue ni HTTP
src/application/      casos de uso
src/adapters/         API y sesión en el navegador
src/bootstrap/        armado de dependencias
src/entrypoint/gui/   pantallas Vue y estilos
tests/                pruebas y recorrido con capturas
postman/              simulación de la API
android/              proyecto de Android para el APK
docs/                 documento del taller y capturas
```

El detalle por guía está en [docs/TALLER.md](docs/TALLER.md).
