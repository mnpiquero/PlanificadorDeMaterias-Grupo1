# Script PowerShell para cargar datos de prueba en el sistema
# Ejecutar después de que la aplicación esté corriendo

$BASE_URL = "http://localhost:8080"

Write-Host "🚀 Iniciando carga de datos de prueba..." -ForegroundColor Green
Write-Host ""

# Verificar que el servidor esté corriendo
Write-Host "📡 Verificando conectividad..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$BASE_URL/ping" -UseBasicParsing -TimeoutSec 5
    Write-Host "✅ Servidor conectado" -ForegroundColor Green
} catch {
    Write-Host "❌ Error: La aplicación no está corriendo en $BASE_URL" -ForegroundColor Red
    Write-Host "   Ejecuta: docker-compose up -d" -ForegroundColor Yellow
    exit 1
}
Write-Host ""

# 1. Crear materias base
Write-Host "📚 Creando materias base..." -ForegroundColor Yellow
$materias = @(
    @{code="MAT101"; name="Matemática I"; credits=6; hours=8; difficulty=4; prereqs=@()},
    @{code="FIS101"; name="Física I"; credits=6; hours=6; difficulty=3; prereqs=@()},
    @{code="ALG101"; name="Álgebra I"; credits=4; hours=6; difficulty=3; prereqs=@()},
    @{code="PRO101"; name="Programación I"; credits=6; hours=8; difficulty=3; prereqs=@()}
)

foreach ($materia in $materias) {
    $json = $materia | ConvertTo-Json -Compress
    Invoke-RestMethod -Uri "$BASE_URL/courses" -Method Put -Body $json -ContentType "application/json" | Out-Null
}
Write-Host "✅ Materias base creadas (MAT101, FIS101, ALG101, PRO101)" -ForegroundColor Green
Write-Host ""

# 2. Crear materias con prerequisitos
Write-Host "📚 Creando materias con prerequisitos..." -ForegroundColor Yellow
$materiasConPrereq = @(
    @{code="MAT201"; name="Matemática II"; credits=6; hours=8; difficulty=5; prereqs=@(@{code="MAT101"})},
    @{code="FIS201"; name="Física II"; credits=6; hours=6; difficulty=4; prereqs=@(@{code="FIS101"}, @{code="MAT101"})},
    @{code="ALG201"; name="Álgebra II"; credits=4; hours=6; difficulty=4; prereqs=@(@{code="ALG101"})},
    @{code="PRO201"; name="Programación II"; credits=6; hours=8; difficulty=4; prereqs=@(@{code="PRO101"})},
    @{code="MAT301"; name="Matemática III"; credits=6; hours=8; difficulty=5; prereqs=@(@{code="MAT201"})}
)

foreach ($materia in $materiasConPrereq) {
    $json = $materia | ConvertTo-Json -Depth 3 -Compress
    Invoke-RestMethod -Uri "$BASE_URL/courses" -Method Put -Body $json -ContentType "application/json" | Out-Null
}
Write-Host "✅ Materias con prerequisitos creadas (MAT201, FIS201, ALG201, PRO201, MAT301)" -ForegroundColor Green
Write-Host ""

# 3. Crear relaciones RELATED
Write-Host "🔗 Creando relaciones de similaridad..." -ForegroundColor Yellow

$relaciones = @(
    @{fromCode="MAT101"; toCode="FIS101"; similarity=0.8},
    @{fromCode="MAT101"; toCode="ALG101"; similarity=0.75},
    @{fromCode="ALG101"; toCode="PRO101"; similarity=0.6}
)

foreach ($rel in $relaciones) {
    $json = $rel | ConvertTo-Json -Compress
    Invoke-RestMethod -Uri "$BASE_URL/relationships" -Method Post -Body $json -ContentType "application/json" | Out-Null
}

# Relaciones automáticas
$relacionesAuto = @(
    @{fromCode="MAT201"; toCode="FIS201"},
    @{fromCode="PRO101"; toCode="PRO201"}
)

foreach ($rel in $relacionesAuto) {
    $json = $rel | ConvertTo-Json -Compress
    Invoke-RestMethod -Uri "$BASE_URL/relationships/auto" -Method Post -Body $json -ContentType "application/json" | Out-Null
}

Write-Host "✅ Relaciones RELATED creadas" -ForegroundColor Green
Write-Host ""

# Resumen
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "✅ Datos de prueba cargados exitosamente!" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "📊 Resumen:" -ForegroundColor Yellow
Write-Host "  - 9 materias creadas"
Write-Host "  - 5 relaciones de similaridad"
Write-Host ""
Write-Host "🧪 Prueba los endpoints:" -ForegroundColor Yellow
Write-Host "  curl $BASE_URL/courses"
Write-Host "  curl $BASE_URL/relationships"
Write-Host "  curl '$BASE_URL/graph/toposort'"
Write-Host "  curl '$BASE_URL/schedule/greedy?maxHours=20'"
Write-Host ""
Write-Host "🌐 Neo4j Browser: http://localhost:7474" -ForegroundColor Cyan
Write-Host "   Usuario: neo4j"
Write-Host "   Password: grupo123"
Write-Host ""

