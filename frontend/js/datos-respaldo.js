'use strict';

/* Catálogo de respaldo. Se muestra solo si la API no responde, para que la página no quede vacía.
   Tiene la misma forma que las respuestas de /api/productos y /api/ropa. */
window.MATRIX_RESPALDO = {
    papeleria: [
        { id: 1, codigo: 'PAP-001', nombre: 'Plumones de colores', descripcion: 'Plumones de colores.', precio: 2000, disponible: true },
        { id: 2, codigo: 'PAP-002', nombre: 'Carpeta blanca', descripcion: 'Carpeta blanca.', precio: 800, disponible: true },
        { id: 3, codigo: 'PAP-003', nombre: 'Carpeta de cartón oficio', descripcion: 'Carpeta de cartón, tamaño oficio.', precio: 1000, disponible: true },
        { id: 4, codigo: 'PAP-004', nombre: 'Carpeta de cartón carta', descripcion: 'Carpeta de cartón, tamaño carta.', precio: 800, disponible: true },
        { id: 5, codigo: 'PAP-005', nombre: 'Sobre de manila oficio', descripcion: 'Sobre de manila, tamaño oficio.', precio: 600, disponible: true },
        { id: 6, codigo: 'PAP-006', nombre: 'Sobre de manila carta', descripcion: 'Sobre de manila, tamaño carta.', precio: 500, disponible: true }
    ],
    ropa: [
        { id: 1, codigo: 'ROP-001', nombre: 'Blusa de flores casual', tipo: 'Blusa', talla: 'M', color: 'Estampado de flores', material: 'Algodón', descripcion: 'Blusa de flores casual.', precio: 15000 },
        { id: 2, codigo: 'ROP-002', nombre: 'Blusa rosada sencilla', tipo: 'Blusa', talla: 'M', color: 'Rosado', material: 'Algodón', descripcion: 'Blusa rosada sencilla.', precio: 12000 },
        { id: 3, codigo: 'ROP-003', nombre: 'Chaqueta de jean para niña', tipo: 'Chaqueta', talla: '12', color: 'Azul', material: 'Jean', descripcion: 'Chaqueta de jean para niña.', precio: 20000 },
        { id: 4, codigo: 'ROP-004', nombre: 'Falda de jean para niña', tipo: 'Falda', talla: '6', color: 'Azul', material: 'Jean', descripcion: 'Falda de jean para niña.', precio: 15000 }
    ]
};
