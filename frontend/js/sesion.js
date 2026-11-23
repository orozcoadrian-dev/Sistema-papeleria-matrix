'use strict';

/* Sesión de las pantallas internas (panel y ventas). Se carga después de config.js.
   La sesión se guarda solo en esta pestaña (sessionStorage): al cerrar el navegador hay que volver a entrar.
   El servidor la cierra tras 15 minutos sin uso; aquí también se cuenta ese tiempo para llevar a la
   persona al inicio de sesión con un aviso claro, en vez de mostrar errores raros. */

const Sesion = (() => {
    const CONFIG = window.MATRIX_CONFIG;
    const ALMACEN = 'matrix.sesion';
    const ULTIMO_USO = 'matrix.ultimo-uso';
    const RUTA_LOGIN = 'login.html';

    class ErrorDelSistema extends Error {
        constructor(contenido, estado) {
            super(contenido?.detail || contenido?.title || '');
            this.estado = estado;
            this.titulo = contenido?.title || '';
            this.errores = contenido?.errores || {}; // errores por campo cuando la API rechaza los datos (400)
        }
    }

    function leer() {
        try {
            const guardada = JSON.parse(sessionStorage.getItem(ALMACEN) || 'null');
            return guardada && guardada.codigo ? guardada : null;
        } catch {
            return null;
        }
    }

    function guardar(sesion) {
        try {
            sessionStorage.setItem(ALMACEN, JSON.stringify(sesion));
            anotarUso();
        } catch { /* sin almacenamiento la sesión dura solo mientras la página esté abierta */ }
    }

    function borrar() {
        // Se borra todo lo de esta pestaña (sesión, sección y pestaña elegidas) para que la siguiente persona empiece de cero.
        try {
            for (const llave of Object.keys(sessionStorage)) {
                if (llave.startsWith('matrix.')) sessionStorage.removeItem(llave);
            }
        } catch { /* nada que borrar */ }
    }

    function anotarUso() {
        try { sessionStorage.setItem(ULTIMO_USO, String(Date.now())); } catch { /* solo es un aviso */ }
    }

    function minutosSinUso() {
        try {
            const ultimo = Number(sessionStorage.getItem(ULTIMO_USO));
            return ultimo ? (Date.now() - ultimo) / 60000 : 0;
        } catch {
            return 0;
        }
    }

    /* Lleva al inicio de sesión. motivo: 'inactividad', 'salio' o 'permiso'. */
    function irAlLogin(motivo) {
        borrar();
        const volver = encodeURIComponent(location.pathname.split('/').pop() || '');
        location.replace(`${RUTA_LOGIN}?motivo=${motivo}&volver=${volver}`);
    }

    /* Revisa que haya sesión y que el rol pueda ver esta pantalla. Devuelve la sesión o null si redirige. */
    function exigir(rolesPermitidos) {
        const sesion = leer();
        if (!sesion) {
            irAlLogin('entrar');
            return null;
        }
        if (minutosSinUso() > sesion.minutosInactividad) {
            irAlLogin('inactividad');
            return null;
        }
        if (rolesPermitidos && !rolesPermitidos.includes(sesion.rol)) {
            location.replace(pantallaDe(sesion.rol));
            return null;
        }
        vigilarInactividad(sesion);
        return sesion;
    }

    function pantallaDe(rol) {
        return rol === 'ADMINISTRADOR' ? 'panel.html' : 'ventas.html';
    }

    /* Cualquier toque o tecla cuenta como uso. Cada 30 segundos se revisa si ya pasó el tiempo. */
    function vigilarInactividad(sesion) {
        anotarUso();
        let ultimoAnotado = 0;
        const usar = () => {
            if (Date.now() - ultimoAnotado > 5000) {
                ultimoAnotado = Date.now();
                anotarUso();
            }
        };
        for (const evento of ['pointerdown', 'keydown', 'scroll', 'touchstart']) {
            window.addEventListener(evento, usar, { passive: true });
        }
        setInterval(() => {
            if (minutosSinUso() > sesion.minutosInactividad) irAlLogin('inactividad');
        }, 30000);
    }

    async function llamar(ruta, { metodo = 'GET', cuerpo } = {}) {
        const sesion = leer();
        const cabeceras = { Accept: 'application/json' };
        if (cuerpo !== undefined) cabeceras['Content-Type'] = 'application/json';
        if (sesion) cabeceras.Authorization = `Bearer ${sesion.codigo}`;

        const control = new AbortController();
        const temporizador = setTimeout(() => control.abort(), CONFIG.tiempoMaximoMs);
        let respuesta;
        try {
            respuesta = await fetch(CONFIG.apiUrl + ruta, {
                method: metodo,
                headers: cabeceras,
                body: cuerpo === undefined ? undefined : JSON.stringify(cuerpo),
                signal: control.signal
            });
        } catch {
            throw new ErrorDelSistema(null, 0);
        } finally {
            clearTimeout(temporizador);
        }

        anotarUso();
        if (respuesta.status === 204) return null;
        const contenido = await respuesta.json().catch(() => null);
        if (!respuesta.ok) {
            // Si la sesión ya no sirve (se venció o el servidor se reinició), se vuelve a entrar.
            if (respuesta.status === 401 && sesion && !ruta.startsWith('/api/sesion')) irAlLogin('inactividad');
            throw new ErrorDelSistema(contenido, respuesta.status);
        }
        return contenido;
    }

    async function salir() {
        try { await llamar('/api/sesion', { metodo: 'DELETE' }); } catch { /* igual se sale */ }
        irAlLogin('salio');
    }

    /* Pone el nombre de la persona y el botón «Salir» en la barra de arriba. */
    function dibujarUsuario(sesion, contenedor) {
        const nombre = document.createElement('span');
        nombre.className = 'barra__usuario';
        nombre.textContent = sesion.nombre;
        const rol = document.createElement('span');
        rol.className = 'barra__rol';
        rol.textContent = sesion.rolNombre;
        nombre.append(rol);

        const boton = document.createElement('button');
        boton.type = 'button';
        boton.className = 'barra__salir';
        boton.textContent = 'Salir';
        boton.addEventListener('click', salir);
        contenedor.append(nombre, boton);
    }

    return { ErrorDelSistema, leer, guardar, borrar, exigir, llamar, salir, pantallaDe, dibujarUsuario };
})();
