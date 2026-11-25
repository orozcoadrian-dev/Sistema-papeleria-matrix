"""Arma informe-de-pruebas.html con los resultados reales de `mvn test`.

Uso, desde la raíz del repositorio, después de correr `mvn test` en backend/:
    python 07-entrega-final/fuentes/generar-informe-pruebas.py
Luego se convierte a PDF con generar-pdf.ps1.
"""
import html
import re
import xml.etree.ElementTree as ET
from collections import OrderedDict
from pathlib import Path

FUENTES = Path(__file__).resolve().parent
REPORTES = FUENTES.parents[1] / 'backend' / 'target' / 'surefire-reports'

QUE_SE_PRUEBA = {
    'PapeleriaMatrixApplicationTests': 'Arranque de la aplicación',
    'CorsConfigTest': 'Permisos del navegador (CORS)',
    'CatalogoControllerTest': 'Catálogo público de productos y ropa',
    'EstadoControllerTest': 'Estado de la API',
    'ProductoControllerTest': 'Productos de papelería',
    'RutasTest': 'Rutas inexistentes y raíz',
    'ServicioControllerTest': 'Servicios del cibercafé',
    'SesionControllerTest': 'Inicio de sesión y permisos por rol',
    'UsuarioControllerTest': 'Usuarios',
    'VentaControllerTest': 'Ventas, existencias y anulación',
    'CalculosT8ControllerTest': 'Cálculo de ingresos y estadísticas (API)',
    'ValidacionesT8Test': 'Validaciones de ingresos y estadísticas',
    'EstadisticaDelNegocioServiceTest': 'Servicio de estadísticas',
    'IngresoDelNegocioServiceTest': 'Servicio de ingresos',
    'AlmacenDeSesionesTest': 'Cierre de sesión por inactividad',
}

# Los nombres de los métodos no llevan tildes; aquí se las ponemos para leerlos como frase.
CON_TILDE = {
    'contrasenia': 'contraseña', 'informacion': 'información', 'estadistica': 'estadística',
    'estadisticas': 'estadísticas', 'codigo': 'código', 'despues': 'después', 'sesion': 'sesión',
    'pagina': 'página', 'raiz': 'raíz', 'numero': 'número', 'papeleria': 'papelería', 'linea': 'línea',
    'telefono': 'teléfono', 'validos': 'válidos', 'invalidos': 'inválidos', 'metodo': 'método',
    'publico': 'público', 'unico': 'único', 'dias': 'días', 'tambien': 'también', 'calculo': 'cálculo',
    'categoria': 'categoría', 'catalogo': 'catálogo', 'mas': 'más', 'invalido': 'inválido',
    'valido': 'válido', 'rapido': 'rápido', 'electronico': 'electrónico', 'documentacion': 'documentación',
    'vacia': 'vacía', 'articulo': 'artículo', 'numerico': 'numérico', 'digitos': 'dígitos', 'esta': 'está',
    'parametro': 'parámetro', 'publica': 'pública', 'json': 'JSON',
}


def frase(metodo):
    # Cada mayúscula y cada número empiezan palabra: «YRopa» → «y ropa», «Dan404» → «dan 404».
    palabras = re.sub(r'(\d+)', r' \1', re.sub(r'([A-Z])', r' \1', metodo)).lower().split()
    palabras = [CON_TILDE.get(p, p) for p in palabras]
    texto = ' '.join(palabras)
    return texto[:1].upper() + texto[1:] + '.'


def main():
    grupos = OrderedDict()
    for archivo in sorted(REPORTES.glob('TEST-*.xml')):
        raiz = ET.parse(archivo).getroot()
        clase = raiz.get('name').split('.')[-1]
        for caso in raiz.findall('testcase'):
            paso = caso.find('failure') is None and caso.find('error') is None
            grupos.setdefault(clase, []).append((caso.get('name'), paso))

    total = sum(len(casos) for casos in grupos.values())
    pasan = sum(1 for casos in grupos.values() for _, paso in casos if paso)

    resumen = ''.join(
        f'<tr><td>{html.escape(QUE_SE_PRUEBA.get(clase, clase))}</td><td><code>{clase}</code></td>'
        f'<td>{len(casos)}</td><td class="ok">{sum(1 for _, p in casos if p)}</td></tr>'
        for clase, casos in grupos.items())

    detalle = []
    for clase, casos in grupos.items():
        detalle.append(f'<tr><th colspan="3">{html.escape(QUE_SE_PRUEBA.get(clase, clase))} · <code>{clase}</code></th></tr>')
        for metodo, paso in casos:
            detalle.append(f'<tr><td>{html.escape(frase(metodo))}</td><td><code>{metodo}</code></td>'
                           f'<td class="ok">{"Pasa" if paso else "Falla"}</td></tr>')

    plantilla = (FUENTES / 'informe-de-pruebas.plantilla.html').read_text(encoding='utf-8')
    salida = (plantilla.replace('{{TOTAL}}', str(total)).replace('{{OK}}', str(pasan))
              .replace('{{FALLAS}}', str(total - pasan)).replace('{{RESUMEN}}', resumen)
              .replace('{{DETALLE}}', '\n        '.join(detalle)))
    (FUENTES / 'informe-de-pruebas.html').write_text(salida, encoding='utf-8')
    print(f'Informe listo: {pasan} de {total} pruebas pasan.')


if __name__ == '__main__':
    main()
