# Frontend de Papelería Matrix

Página pública hecha con HTML, CSS y JavaScript, sin librerías. Consulta la API del backend para mostrar los servicios, la papelería y la ropa.

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
css/styles.css        Punto de entrada de los estilos
css/bases/            Fuentes, variables de la marca y reset
css/vistas/           Estilos de cada vista
js/config.js          Dirección de la API
js/datos-respaldo.js  Catálogo que se muestra si la API no responde
js/main.js            Menú, modales, catálogos y buscadores
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
| RF-38: inicio de sesión | Solo la pantalla. Valida los datos, pero no da acceso hasta que el backend tenga autenticación. |
| Panel de la administradora y de los operadores (servicios, productos, ropa, operadores, facturas, ventas, ingresos, estadísticas y reportes) | Pendiente. |
| RNF-18: cierre de sesión a los 15 minutos | Pendiente, depende del inicio de sesión. |

El botón "Registrarse" abre un formulario que avisa que el registro no está disponible. La documentación lo menciona, pero RF-41 dice que el sistema solo tiene dos usuarios internos, así que falta decidir si se queda.
