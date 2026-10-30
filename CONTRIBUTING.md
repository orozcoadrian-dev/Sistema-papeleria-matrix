# Cómo trabajamos en el repositorio

Nadie sube cambios directo a `main`. Todo entra por un pull request (PR), y `main` siempre debe estar en buen estado.

## Paso a paso

1. **Actualizar `main` y crear una rama nueva**

   ```
   git switch main
   git pull
   git switch -c Feat/nombre-corto
   ```

   El nombre empieza con el tipo de cambio: `Feat/` (algo nuevo), `Fix/` (un arreglo), `Refactor/` (ordenar sin cambiar lo que hace), `Docs/` (documentación).

2. **Hacer los cambios y guardarlos en commits pequeños**, con un mensaje que empieza con el tipo: `feat:`, `fix:`, `refactor:`, `docs:`, `ci:` o `chore:`.

   ```
   git add <archivos>
   git commit -m "feat: descripción corta de lo que se hizo"
   ```

3. **Subir la rama y abrir el PR hacia `main`**

   ```
   git push -u origin Feat/nombre-corto
   ```

   En GitHub aparece el botón "Compare & pull request". El PR debe explicar qué cambia, cómo probarlo y qué falta.

4. **Esperar las revisiones automáticas (CI).** Abajo en el PR aparecen dos:
   - **Backend (compilar y probar):** compila el proyecto y corre las pruebas.
   - **Docker (construir la imagen):** construye la imagen y comprueba que la API arranca.

   Si una sale en rojo, se abre su detalle, se corrige en la misma rama y se vuelve a subir. El PR se actualiza y las revisiones corren otra vez.

5. **Otra persona del equipo lo revisa.** Se leen los cambios y se deja un comentario si algo no está claro.

6. **Mergear solo con las revisiones en verde.** Se usa "Create a merge commit". Después se actualiza `main` local con `git pull`.

## Qué no se sube

- Contraseñas, claves ni datos reales de clientes. La clave de la API en Render la genera Render y se consulta en su panel.
- Carpetas generadas, como `backend/target`.

## Cómo correr el proyecto

Las instrucciones para encender la base de datos y el backend están en [backend/README.md](backend/README.md).
