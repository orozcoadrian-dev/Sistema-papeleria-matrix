# Backend de Papelería Matrix

API REST hecha con Spring Boot 4.1 y Java 21.

## Modos de ejecución

| Modo | Base de datos | Para qué sirve |
|------|---------------|----------------|
| `local` | MySQL de XAMPP | El sistema real. Es el modo por defecto. |
| `demo` | H2 dentro del contenedor, con datos de ejemplo | Render y las pruebas. Los datos se pierden al reiniciar. |

El modo se elige con la variable `SPRING_PROFILES_ACTIVE`.

## Cómo correrlo

**Modo local, con XAMPP**

1. Encender MySQL en XAMPP y ejecutar los scripts de `database/scripts` en orden: carpetas 01, 02 y 03.
2. Dentro de `backend`, ejecutar `mvn spring-boot:run`.

Si la base no usa el usuario `root` sin contraseña en el puerto 3306, se cambian las variables de entorno `DB_URL`, `DB_USER` y `DB_PASSWORD`.

**Modo demo, sin XAMPP**

```
mvn spring-boot:run -Dspring-boot.run.profiles=demo
```

**Con Docker**, desde la raíz del repositorio:

```
docker build -t papeleria-matrix .
docker run -p 8080:8080 papeleria-matrix
```

## Rutas

| Ruta | Qué hace | Clave |
|------|----------|-------|
| `GET /api/estado` | Comprueba que la API está en línea y en qué modo corre. | No |
| `GET /actuator/health` | La usa Render para saber si la aplicación está viva. | No |
| `GET /api/servicios` y `GET /api/servicios/{id}` | Consultan los servicios del negocio. | No |
| `POST /api/servicios` | Crea un servicio. Responde 201 con la dirección del nuevo. | Sí |
| `PUT /api/servicios/{id}` | Edita un servicio. | Sí |
| `DELETE /api/servicios/{id}` | Elimina un servicio. Responde 204. | Sí |
| `GET /api/productos` y `GET /api/productos/{id}` | Catálogo de papelería, sin precios de compra ni proveedor. | No |
| `GET /api/ropa` y `GET /api/ropa/{id}` | Catálogo de ropa. | No |
| `/swagger-ui.html` | Documentación de la API, que también se puede probar desde ahí. | No |
| `/v3/api-docs` | La misma documentación en formato OpenAPI. | No |

Los errores salen en JSON con un título y un detalle claros: 400 si los datos son inválidos, 401 si falta la clave, 404 si no existe y 409 si el código del servicio ya está en uso.

## Clave de la API

Consultar es libre. Crear, editar y eliminar exigen la cabecera `X-API-Key`.

- En local y en las pruebas la clave es `clave-de-prueba`.
- En Render la clave la inventa el propio servicio. Se ve en el panel del servicio, en Environment, variable `API_KEY`. No se escribe en el repositorio.
- Para usar otra clave, se define la variable de entorno `API_KEY`.

## Datos de ejemplo

En modo demo la API arranca con 5 servicios, 6 productos de papelería y 4 prendas de ropa, definidos en `src/main/resources/demo/datos-demo.sql`. Se pierden al reiniciar. En modo local las tablas empiezan vacías: los datos reales se cargan en XAMPP.

## Postman

En la carpeta `postman` del repositorio hay una colección con 15 peticiones y sus pruebas, y dos entornos.

1. En Postman, Import, y se eligen los tres archivos de la carpeta `postman`.
2. Arriba a la derecha se elige el entorno: Local o Render.
3. En Render, se pega la clave de la API en la variable `apiKey` del entorno.
4. Se ejecuta la colección con Run. Las peticiones de servicios se hacen en orden: crean un servicio, lo consultan, lo editan y lo eliminan.

La colección espera los datos de ejemplo, así que se corre contra el modo demo.

## Pruebas

```
mvn verify
```

## Aviso sobre MariaDB

El modo local se probó contra MariaDB 10.4, la que trae XAMPP, y funciona. Al arrancar, Hibernate avisa con una línea `HHH000511` porque MariaDB se presenta como "5.5.5". El aviso es inofensivo. Hibernate 7 oficialmente pide MariaDB 10.6 o MySQL 8, así que conviene actualizar XAMPP si se puede.

## Despliegue en Render

1. En Render, elegir New y luego Blueprint, y seleccionar este repositorio. Render lee `render.yaml`.
2. Confirmar que el plan es gratuito y que admite Docker.
3. Cuando termine, Render muestra la dirección pública del servicio.

En el plan gratuito el servicio se duerme tras 15 minutos sin uso y tarda cerca de un minuto en despertar. Conviene abrir la dirección unos minutos antes de una demostración.
