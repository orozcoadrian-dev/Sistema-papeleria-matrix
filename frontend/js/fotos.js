'use strict';

/* Fotos de productos, ropa y servicios que sube la administradora. Se carga después de config.js.
   El servidor dice qué elementos tienen foto y una versión que cambia con cada foto nueva:
   la versión va en la dirección (?v=) para que el navegador no muestre la foto vieja. */

const Fotos = (() => {
    const CONFIG = window.MATRIX_CONFIG;
    // Las fotos del celular pesan varios MB: se achican antes de subirlas.
    const LADO_MAXIMO = 1000;
    const CALIDAD = 0.85;
    let inventario = {};

    async function cargar() {
        try {
            const respuesta = await fetch(CONFIG.apiUrl + '/api/imagenes', { headers: { Accept: 'application/json' } });
            inventario = respuesta.ok ? await respuesta.json() : {};
        } catch {
            inventario = {};
        }
        return inventario;
    }

    /** Dirección de la foto, o null si el elemento no tiene. tipo: 'productos', 'ropa' o 'servicios'. */
    function url(tipo, id) {
        const version = inventario[tipo]?.[id];
        return version ? `${CONFIG.apiUrl}/api/imagenes/${tipo}/${id}?v=${version}` : null;
    }

    /** Convierte la foto elegida en un JPEG de máximo 1000 px de lado, listo para subir. */
    async function preparar(archivo) {
        if (!archivo.type.startsWith('image/')) {
            throw new Error('El archivo no es una foto.');
        }
        const imagen = await new Promise((resolver, rechazar) => {
            const elemento = new Image();
            const direccion = URL.createObjectURL(archivo);
            elemento.onload = () => { URL.revokeObjectURL(direccion); resolver(elemento); };
            elemento.onerror = () => { URL.revokeObjectURL(direccion); rechazar(new Error('No se pudo abrir la foto.')); };
            elemento.src = direccion;
        });
        const escala = Math.min(1, LADO_MAXIMO / Math.max(imagen.naturalWidth, imagen.naturalHeight));
        const lienzo = document.createElement('canvas');
        lienzo.width = Math.max(1, Math.round(imagen.naturalWidth * escala));
        lienzo.height = Math.max(1, Math.round(imagen.naturalHeight * escala));
        const dibujo = lienzo.getContext('2d');
        // Fondo blanco: un PNG transparente no queda negro al pasarlo a JPEG.
        dibujo.fillStyle = '#FFFFFF';
        dibujo.fillRect(0, 0, lienzo.width, lienzo.height);
        dibujo.drawImage(imagen, 0, 0, lienzo.width, lienzo.height);
        return new Promise((resolver, rechazar) => {
            lienzo.toBlob((blob) => (blob ? resolver(blob) : rechazar(new Error('No se pudo preparar la foto.'))), 'image/jpeg', CALIDAD);
        });
    }

    return { cargar, url, preparar };
})();
