# Convierte los documentos HTML de esta carpeta a PDF con Microsoft Edge (sin abrir ventanas).
# Uso, desde la raíz del repositorio:  powershell -File 07-entrega-final\fuentes\generar-pdf.ps1
param(
    [string[]]$Documentos = @(
        'manual-de-usuario.html|07-entrega-final\Manual-de-usuario-Papeleria-Matrix.pdf',
        'manual-tecnico.html|07-entrega-final\Manual-tecnico-Papeleria-Matrix.pdf',
        'informe-de-pruebas.html|05-pruebas\Informe-de-pruebas-Papeleria-Matrix.pdf'
    )
)

$edge = 'C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe'
$fuentes = $PSScriptRoot
$raiz = (Resolve-Path (Join-Path $fuentes '..\..')).Path

foreach ($par in $Documentos) {
    $html, $pdf = $par.Split('|')
    $origen = Join-Path $fuentes $html
    if (-not (Test-Path $origen)) { Write-Host "Falta $html, se salta."; continue }
    $destino = Join-Path $raiz $pdf
    $url = 'file:///' + ($origen -replace '\\', '/')
    & $edge --headless=new --disable-gpu --no-pdf-header-footer "--print-to-pdf=$destino" $url 2>$null | Out-Null
    Start-Sleep -Milliseconds 500
    if (Test-Path $destino) { Write-Host "Listo: $pdf" } else { Write-Host "No se pudo crear $pdf" }
}
