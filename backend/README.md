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

- `GET /api/estado`: comprueba que la API está en línea y en qué modo corre.
- `GET /actuator/health`: la usa Render para saber si la aplicación está viva.
- `/swagger-ui.html`: documentación de la API, que también se puede probar desde ahí.
- `/v3/api-docs`: la misma documentación en formato OpenAPI, que Postman puede importar.

## Pruebas

```
mvn verify
```

## Despliegue en Render

1. En Render, elegir New y luego Blueprint, y seleccionar este repositorio. Render lee `render.yaml`.
2. Confirmar que el plan es gratuito y que admite Docker.
3. Cuando termine, Render muestra la dirección pública del servicio.

En el plan gratuito el servicio se duerme tras 15 minutos sin uso y tarda cerca de un minuto en despertar. Conviene abrir la dirección unos minutos antes de una demostración.
