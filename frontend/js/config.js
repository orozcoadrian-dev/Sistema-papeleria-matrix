'use strict';

/* Configuración del frontend. Se carga antes que main.js.
   En el computador local la página usa el backend en el puerto 8080.
   Publicada, usa la API de Render: se ajusta la dirección si Render asigna otro nombre. */
window.MATRIX_CONFIG = {
    apiUrl: ['localhost', '127.0.0.1', ''].includes(window.location.hostname)
        ? 'http://localhost:8080'
        : 'https://papeleria-matrix-api.onrender.com',

    // Render gratis se duerme y tarda cerca de un minuto en despertar: se espera ese tiempo antes de rendirse.
    tiempoMaximoMs: 65000
};
