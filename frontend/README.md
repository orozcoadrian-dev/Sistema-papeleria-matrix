# Frontend de Papelería Matrix

Página pública y pantallas internas hechas con HTML, CSS y JavaScript, sin librerías. La página pública consulta la API para mostrar los servicios, la papelería y la ropa. Las pantallas internas (panel de la administradora y ventas) piden iniciar sesión.

## Pantallas internas

| Pantalla | Quién entra | Qué hace |
|----------|-------------|----------|
| `views/login.html` | Todos | Inicio de sesión con usuario y contraseña. Cada rol va a su pantalla. |
| `views/panel.html` | Administradora | Servicios, papelería (con existencias y precio de compra), ropa, operadores (con su clave de permiso) y datos de la tienda. Lleva a ventas y cuentas. |
| `views/ventas.html` | Administradora y operador | Caja: elegir artículos, cobrar con cálculo del cambio y ver lo vendido. Solo la administradora anula ventas y ve las de todos. |
| `views/cuentas.html` | Administradora | Lo vendido por tipo, ganancia en papelería, inventario, sueldos y lo más vendido. Guarda cortes en ingresos y estadísticas, e imprime. |

La sesión se guarda en la pestaña (`sessionStorage`) y se cierra tras 15 minutos sin uso. En el modo demo los usuarios de prueba están al final de `backend/src/main/resources/demo/datos-demo.sql`.

## Cómo abrirlo

**Solo la página**, sin servidor: abrir `index.html` con doble clic. Funciona, pero el navegador no deja que una página abierta como archivo consulte la API, así que muestra el catálogo de respaldo.

**Con la API** (lo recomendado):

1. Encender el backend, como dice `backend/README.md`. Queda en `http://localhost:8080`.
2. Servir esta carpeta en el puerto 5500, desde la raíz del repositorio:

   ```
   jwebserver -d frontend -p 5500
   ```

   `jwebserver` viene con el JDK 21. Otra opción es la extensión Live Server de VS Code, que usa ese mismo puerto.
3. Abrir `http://localhost:5500`.

El backend solo acepta peticiones del puerto 5500. Para otra dirección, se agrega en la variable de entorno `CORS_ORIGENES` del backend.

## Dirección de la API

Está en `js/config.js`. En el computador local usa `http://localhost:8080`. Publicada, usa la API de Render. Si Render asigna otro nombre al servicio, se cambia ahí.

Render gratis se duerme y tarda cerca de un minuto en despertar. Mientras tanto la página avisa que el servidor se está despertando, y si la API no responde muestra el catálogo de respaldo (`js/datos-respaldo.js`). Los servicios no tienen respaldo, porque sus precios son de ejemplo, y se muestra un aviso con un botón para reintentar.

## Estructura

```
index.html            Página principal
views/login.html      Inicio de sesión
views/panel.html      Panel de la administradora
views/ventas.html     Caja y ventas
views/cuentas.html    Cuentas del negocio
css/styles.css        Punto de entrada de los estilos
css/bases/            Fuentes, variables de la marca y reset
css/vistas/           Estilos de cada vista
js/config.js          Dirección de la API
js/datos-respaldo.js  Catálogo que se muestra si la API no responde
js/main.js            Menú, modales, catálogos y buscadores
js/sesion.js          Sesión, permisos por rol y cierre por inactividad
js/panel.js           Panel de la administradora
js/ventas.js          Caja y ventas
js/cuentas.js         Cuentas del negocio
assets/               Fuentes, íconos e imágenes
```

## Estado frente a la documentación

| Requisito o caso de uso | Estado |
|-------------------------|--------|
| RF-01, CU-05, CU-10, CU-40, CU-57, CU-59, CU-61: búsqueda del catálogo y de la información del negocio | Hecho. Cada catálogo tiene su buscador y hay un buscador general. |
| RF-02: información destacada sin criterio de búsqueda | Parcial. Sin criterio se muestra todo el catálogo. La base no tiene un campo de destacado. |
| CU-15, CU-56: información del negocio | Parcial. Los horarios y el contacto están escritos en la página, no vienen de la API. |
| RNF-05: sin errores técnicos para el cliente | Hecho. Los fallos se explican con mensajes claros. |
| RNF-12: responsive | Hecho. Revisado en 375 px y en 1280 px de ancho. |
| RNF-16: términos y condiciones, cookies | Hecho. El texto de los términos debe revisarlo el negocio. |
| RNF-02: accesibilidad | Parcial. Hay enlace para saltar al contenido, etiquetas, foco visible, textos alternativos y respeto a la preferencia de menos movimiento. No hay una auditoría formal. |
| RNF-09: respuesta en menos de 2,5 s | Sin medir. Con la API despierta la carga es inmediata. Con Render dormido, tarda hasta un minuto. |
| RF-38: inicio de sesión | Hecho. Usuario y contraseña, con roles de administradora y operador. |
| Panel de la administradora: servicios, productos y ropa | Hecho. |
| Ventas y facturas, con descuento de existencias | Hecho. |
| Panel: operadores y claves de permiso | Hecho. |
| Panel: información del negocio | Hecho en la pantalla. El servidor falla al listarla (ver el PR). |
| Ingresos, estadísticas y reportes | Parcial. Las cuentas se calculan en la pantalla y se guardan como cortes; los historiales y reportes en archivo esperan al backend. |
| RNF-18: cierre de sesión a los 15 minutos | Hecho, en el servidor y en el navegador. |

La página no tiene botón de registro: según RF-41 solo entran la administradora y los operadores, y sus cuentas las crea la administradora desde el panel.
