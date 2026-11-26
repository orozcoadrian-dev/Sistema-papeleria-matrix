'use strict';

/* Escribe un PDF sencillo (texto en hojas tamaño carta) y lo descarga, sin librerías de terceros.
   Cada renglón es un objeto:
     { texto, tipo }      tipo: 'titulo', 'subtitulo', 'texto' (por defecto), 'nota' o 'total'
     { texto, derecha }   lo de «derecha» queda alineado al margen derecho (valores)
     { linea: true }      una raya separadora
     { espacio: true }    un renglón en blanco */
const Pdf = (() => {
    const ANCHO = 612;
    const ALTO = 792;
    const MARGEN = 56;
    const ESTILOS = {
        titulo: { fuente: 'F2', tamano: 20, alto: 28 },
        subtitulo: { fuente: 'F2', tamano: 13, alto: 22 },
        texto: { fuente: 'F1', tamano: 11, alto: 17 },
        nota: { fuente: 'F1', tamano: 9, alto: 14 },
        total: { fuente: 'F2', tamano: 13, alto: 22 }
    };
    // Letras de la codificación WinAnsi que no coinciden con el número de Unicode.
    const ESPECIALES = { '€': 128, '…': 133, '‘': 145, '’': 146, '“': 147, '”': 148, '•': 149, '–': 150, '—': 151 };
    // Anchos de Helvetica (en milésimas) de lo que aparece en los valores, para alinearlos a la derecha.
    const ANCHOS = { ' ': 278, '.': 278, ',': 278, '-': 333, '$': 556, '%': 889 };

    /* Cada letra pasa a un solo byte. Lo que la fuente no tiene (por ejemplo un emoji) se omite. */
    function enWinAnsi(texto) {
        let salida = '';
        for (const letra of String(texto ?? '').replace(/[   ]/g, ' ')) {
            const numero = letra.codePointAt(0);
            if ((numero >= 32 && numero <= 126) || (numero >= 160 && numero <= 255)) salida += letra;
            else if (ESPECIALES[letra]) salida += String.fromCharCode(ESPECIALES[letra]);
        }
        return salida;
    }

    function escapar(texto) {
        return texto.replace(/[\\()]/g, '\\$&');
    }

    function anchoDe(texto, tamano) {
        let suma = 0;
        for (const letra of texto) suma += ANCHOS[letra] ?? 556;
        return (suma * tamano) / 1000;
    }

    /* Parte un texto largo en varios renglones, sin cortar palabras (salvo que una sola no quepa). */
    function partir(texto, tamano, anchoMaximo) {
        const cabenLetras = Math.max(10, Math.floor(anchoMaximo / (tamano * 0.52)));
        const renglones = [];
        let actual = '';
        for (const palabra of texto.split(/\s+/).filter(Boolean)) {
            let resto = palabra;
            while (resto.length > cabenLetras) {
                if (actual) renglones.push(actual);
                actual = '';
                renglones.push(resto.slice(0, cabenLetras));
                resto = resto.slice(cabenLetras);
            }
            if (!actual) actual = resto;
            else if (actual.length + 1 + resto.length <= cabenLetras) actual += ` ${resto}`;
            else {
                renglones.push(actual);
                actual = resto;
            }
        }
        if (actual) renglones.push(actual);
        return renglones.length ? renglones : [''];
    }

    function paginas(contenido) {
        const hojas = [[]];
        let y = ALTO - MARGEN;
        const bajar = (alto) => {
            if (y - alto < MARGEN) {
                hojas.push([]);
                y = ALTO - MARGEN;
            }
            y -= alto;
        };
        const escribir = (fuente, tamano, x, texto) => {
            hojas[hojas.length - 1].push(`BT /${fuente} ${tamano} Tf ${x.toFixed(2)} ${y.toFixed(2)} Td (${escapar(texto)}) Tj ET`);
        };
        for (const renglon of contenido) {
            if (renglon.espacio) {
                bajar(10);
                continue;
            }
            if (renglon.linea) {
                bajar(10);
                hojas[hojas.length - 1].push(`0.75 G ${MARGEN} ${(y + 3).toFixed(2)} m ${ANCHO - MARGEN} ${(y + 3).toFixed(2)} l S 0 G`);
                continue;
            }
            const estilo = ESTILOS[renglon.tipo] || ESTILOS.texto;
            const derecha = renglon.derecha ? enWinAnsi(renglon.derecha) : '';
            const espacio = ANCHO - MARGEN * 2 - (derecha ? anchoDe(derecha, estilo.tamano) + 16 : 0);
            partir(enWinAnsi(renglon.texto), estilo.tamano, espacio).forEach((trozo, posicion) => {
                bajar(estilo.alto);
                escribir(estilo.fuente, estilo.tamano, MARGEN, trozo);
                if (posicion === 0 && derecha) {
                    escribir(estilo.fuente, estilo.tamano, ANCHO - MARGEN - anchoDe(derecha, estilo.tamano), derecha);
                }
            });
        }
        return hojas;
    }

    /* Devuelve el documento como texto de un byte por letra. */
    function crear(contenido) {
        const hojas = paginas(contenido);
        const objetos = [];
        const numeroDeHoja = (posicion) => 5 + posicion * 2;
        objetos[1] = '<< /Type /Catalog /Pages 2 0 R >>';
        objetos[2] = `<< /Type /Pages /Kids [${hojas.map((_, i) => `${numeroDeHoja(i)} 0 R`).join(' ')}] /Count ${hojas.length} >>`;
        objetos[3] = '<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>';
        objetos[4] = '<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>';
        hojas.forEach((instrucciones, i) => {
            const dibujo = instrucciones.join('\n');
            objetos[numeroDeHoja(i)] = `<< /Type /Page /Parent 2 0 R /MediaBox [0 0 ${ANCHO} ${ALTO}] `
                + `/Resources << /Font << /F1 3 0 R /F2 4 0 R >> >> /Contents ${numeroDeHoja(i) + 1} 0 R >>`;
            objetos[numeroDeHoja(i) + 1] = `<< /Length ${dibujo.length} >>\nstream\n${dibujo}\nendstream`;
        });

        let documento = '%PDF-1.4\n';
        const posiciones = [];
        for (let numero = 1; numero < objetos.length; numero++) {
            posiciones[numero] = documento.length;
            documento += `${numero} 0 obj\n${objetos[numero]}\nendobj\n`;
        }
        const indice = documento.length;
        documento += `xref\n0 ${objetos.length}\n0000000000 65535 f \n`;
        for (let numero = 1; numero < objetos.length; numero++) {
            documento += `${String(posiciones[numero]).padStart(10, '0')} 00000 n \n`;
        }
        documento += `trailer\n<< /Size ${objetos.length} /Root 1 0 R >>\nstartxref\n${indice}\n%%EOF\n`;
        return documento;
    }

    function descargar(nombre, contenido) {
        const documento = crear(contenido);
        const bytes = Uint8Array.from(documento, (letra) => letra.charCodeAt(0));
        const direccion = URL.createObjectURL(new Blob([bytes], { type: 'application/pdf' }));
        const enlace = document.createElement('a');
        enlace.href = direccion;
        enlace.download = nombre;
        document.body.append(enlace);
        enlace.click();
        enlace.remove();
        setTimeout(() => URL.revokeObjectURL(direccion), 10000);
    }

    return { crear, descargar };
})();

if (typeof module !== 'undefined') module.exports = Pdf;
