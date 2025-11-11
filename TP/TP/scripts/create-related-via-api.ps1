# Script PowerShell para crear relaciones RELATED usando la API
# Este script usa los endpoints de la aplicación para crear las relaciones

$BASE_URL = "http://localhost:8080"

Write-Host "🔗 Creando relaciones RELATED via API..." -ForegroundColor Cyan
Write-Host ""

# Verificar que el servidor esté corriendo
try {
    $response = Invoke-WebRequest -Uri "$BASE_URL/ping" -Method GET -UseBasicParsing -ErrorAction Stop
    Write-Host "✅ Servidor conectado" -ForegroundColor Green
} catch {
    Write-Host "❌ Error: La aplicación no está corriendo en $BASE_URL" -ForegroundColor Red
    Write-Host "   Ejecuta la aplicación primero" -ForegroundColor Yellow
    exit 1
}

Write-Host ""

# Crear relaciones RELATED según el seed
$relationships = @(
    @{fromCode="POO"; toCode="AYP"; similarity=1.0},
    @{fromCode="PROG3"; toCode="POO"; similarity=0.9},
    @{fromCode="PROG3"; toCode="BD"; similarity=0.6},
    @{fromCode="AYED2"; toCode="AYP"; similarity=0.8},
    @{fromCode="IA"; toCode="AYED2"; similarity=0.7},
    @{fromCode="SO"; toCode="POO"; similarity=0.5},
    @{fromCode="RED"; toCode="SO"; similarity=0.4}
)

foreach ($rel in $relationships) {
    $body = @{
        fromCode = $rel.fromCode
        toCode = $rel.toCode
        similarity = $rel.similarity
    } | ConvertTo-Json

    try {
        $response = Invoke-WebRequest -Uri "$BASE_URL/relationships" `
            -Method POST `
            -ContentType "application/json" `
            -Body $body `
            -UseBasicParsing `
            -ErrorAction Stop
        
        Write-Host "✅ Creada: $($rel.fromCode) <-> $($rel.toCode) (sim: $($rel.similarity))" -ForegroundColor Green
    } catch {
        $errorMessage = $_.Exception.Message
        if ($_.Exception.Response) {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $errorMessage = $reader.ReadToEnd()
        }
        Write-Host "❌ Error creando $($rel.fromCode) <-> $($rel.toCode): $errorMessage" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "✅ Relaciones RELATED creadas" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "🧪 Verificar relaciones:" -ForegroundColor Yellow
Write-Host "   GET $BASE_URL/relationships" -ForegroundColor Gray
Write-Host "   GET $BASE_URL/graph/mst?algo=prim" -ForegroundColor Gray
Write-Host ""

