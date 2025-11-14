# Script PowerShell para crear relaciones RELATED automáticas entre materias del Plan 1621
# Este script debe ejecutarse DESPUÉS de init-data-ingenieria.ps1 o init-data-ingenieria.sh
# Crea relaciones entre materias relacionadas temáticamente usando el endpoint /relationships/auto

$BASE_URL = "http://localhost:8080"

Write-Host "🔗 Iniciando creación de relaciones RELATED..." -ForegroundColor Cyan
Write-Host "📋 Plan: Ingeniería en Informática 1621"
Write-Host ""

# Verificar que el servidor esté corriendo
Write-Host "📡 Verificando conectividad..." -ForegroundColor Yellow
try {
    $null = Invoke-RestMethod -Uri "$BASE_URL/ping" -Method GET -ErrorAction Stop
    Write-Host "✅ Servidor conectado" -ForegroundColor Green
} catch {
    Write-Host "❌ Error: La aplicación no está corriendo en $BASE_URL" -ForegroundColor Red
    Write-Host "   Ejecuta: docker-compose up -d"
    exit 1
}
Write-Host ""

# Contadores
$CREATED = 0
$FAILED = 0

# Función para crear relación
function Create-Relation {
    param(
        [string]$from,
        [string]$to,
        [string]$desc
    )
    
    Write-Host "  Creando: $desc"
    
    try {
        $body = @{
            fromCode = $from
            toCode = $to
        } | ConvertTo-Json
        
        $response = Invoke-RestMethod -Uri "$BASE_URL/relationships/auto" `
            -Method POST `
            -ContentType "application/json" `
            -Body $body `
            -ErrorAction Stop
        
        $script:CREATED++
    } catch {
        $script:FAILED++
        Write-Host "    ⚠️  Falló: $from <-> $to" -ForegroundColor Yellow
    }
}

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "🔗 Creando relaciones RELATED" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# ============================================================================
# SECUENCIAS DE PROGRAMACIÓN
# ============================================================================
Write-Host "💻 Secuencia de Programación..." -ForegroundColor Green
Create-Relation "3.4.069" "3.4.071" "Fundamentos de Informática <-> Programación I"
Create-Relation "3.4.071" "3.4.074" "Programación I <-> Programación II"
Create-Relation "3.4.074" "3.4.077" "Programación II <-> Programación III"
Create-Relation "3.4.071" "3.4.208" "Programación I <-> Paradigma OO"
Create-Relation "3.4.074" "3.4.208" "Programación II <-> Paradigma OO"
Create-Relation "3.4.208" "3.4.082" "Paradigma OO <-> Aplicaciones Interactivas"
Create-Relation "3.4.077" "3.4.210" "Programación III <-> Proceso Desarrollo Software"
Write-Host ""

# ============================================================================
# SECUENCIAS DE SISTEMAS DE INFORMACIÓN
# ============================================================================
Write-Host "📊 Secuencia de Sistemas de Información..." -ForegroundColor Green
Create-Relation "3.4.164" "3.4.207" "Sistemas de Información I <-> II"
Create-Relation "3.4.207" "3.4.214" "Sistemas de Información II <-> Ingeniería de Software"
Create-Relation "3.4.207" "3.4.089" "Sistemas de Información II <-> Dirección de Proyectos"
Create-Relation "3.4.207" "3.4.094" "Sistemas de Información II <-> Arquitectura de Aplicaciones"
Write-Host ""

# ============================================================================
# SECUENCIAS DE DESARROLLO DE SOFTWARE
# ============================================================================
Write-Host "🏗️  Secuencia de Desarrollo de Software..." -ForegroundColor Green
Create-Relation "3.4.210" "3.4.214" "Proceso Desarrollo <-> Ingeniería de Software"
Create-Relation "3.4.210" "3.4.216" "Proceso Desarrollo <-> Desarrollo Aplicaciones I"
Create-Relation "3.4.216" "3.4.218" "Desarrollo Aplicaciones I <-> II"
Create-Relation "3.4.214" "3.4.098" "Ingeniería de Software <-> Calidad de Software"
Create-Relation "3.4.082" "3.4.218" "Aplicaciones Interactivas <-> Desarrollo Aplicaciones II"
Write-Host ""

# ============================================================================
# SECUENCIAS DE DATOS E IA
# ============================================================================
Write-Host "🤖 Secuencia de Datos e Inteligencia Artificial..." -ForegroundColor Green
Create-Relation "3.4.209" "3.4.213" "Ingeniería de Datos I <-> II"
Create-Relation "3.4.213" "3.4.217" "Ingeniería de Datos II <-> Ciencia de Datos"
Create-Relation "3.4.217" "3.4.096" "Ciencia de Datos <-> Inteligencia Artificial"
Create-Relation "3.1.049" "3.4.217" "Probabilidad y Estadística <-> Ciencia de Datos"
Create-Relation "3.1.056" "3.4.096" "Estadística Avanzada <-> Inteligencia Artificial"
Write-Host ""

# ============================================================================
# SECUENCIAS DE REDES Y TELECOMUNICACIONES
# ============================================================================
Write-Host "🌐 Secuencia de Redes y Telecomunicaciones..." -ForegroundColor Green
Create-Relation "3.4.072" "3.4.075" "Arquitectura de Computadores <-> Sistemas Operativos"
Create-Relation "3.4.078" "3.4.212" "Fundamentos Telecomunicaciones <-> Teleinformática"
Create-Relation "3.4.212" "3.4.092" "Teleinformática <-> Seguridad e Integridad"
Write-Host ""

# ============================================================================
# SECUENCIAS DE MATEMÁTICA
# ============================================================================
Write-Host "📐 Secuencia de Matemática..." -ForegroundColor Green
Create-Relation "3.1.050" "3.1.051" "Elementos de Álgebra <-> Álgebra"
Create-Relation "3.1.053" "3.1.054" "Cálculo I <-> Cálculo II"
Create-Relation "3.1.051" "3.1.052" "Álgebra <-> Física I"
Create-Relation "3.1.052" "3.1.055" "Física I <-> Física II"
Create-Relation "3.1.053" "3.1.049" "Cálculo I <-> Probabilidad y Estadística"
Create-Relation "3.1.049" "3.1.056" "Probabilidad y Estadística <-> Estadística Avanzada"
Create-Relation "3.1.024" "3.4.209" "Matemática Discreta <-> Ingeniería de Datos I"
Create-Relation "3.1.024" "3.4.215" "Matemática Discreta <-> Teoría de la Computación"
Create-Relation "3.1.054" "3.1.025" "Cálculo II <-> Modelado y Simulación"
Write-Host ""

# ============================================================================
# RELACIONES DE GESTIÓN Y PROYECTOS
# ============================================================================
Write-Host "📈 Área de Gestión y Proyectos..." -ForegroundColor Green
Create-Relation "3.4.089" "3.4.086" "Dirección de Proyectos <-> Evaluación de Proyectos"
Create-Relation "3.4.211" "3.4.089" "Seminario Integración <-> Dirección de Proyectos"
Create-Relation "3.4.211" "3.4.214" "Seminario Integración <-> Ingeniería de Software"
Write-Host ""

# ============================================================================
# RELACIONES DE TECNOLOGÍA Y TENDENCIAS
# ============================================================================
Write-Host "🚀 Área de Tecnología y Tendencias..." -ForegroundColor Green
Create-Relation "3.4.220" "3.4.135" "Tendencias Tecnológicas <-> Tecnología e Innovación"
Create-Relation "3.4.219" "3.4.135" "Tecnología y Medio Ambiente <-> Tecnología e Innovación"
Create-Relation "3.4.221" "3.4.086" "Negocios Tecnológicos <-> Evaluación de Proyectos"
Write-Host ""

# ============================================================================
# RELACIONES INTERDISCIPLINARIAS
# ============================================================================
Write-Host "🔄 Relaciones Interdisciplinarias..." -ForegroundColor Green
Create-Relation "3.4.164" "3.4.043" "Sistemas de Información I <-> Teoría de Sistemas"
Create-Relation "3.4.077" "3.4.215" "Programación III <-> Teoría de la Computación"
Create-Relation "3.4.094" "3.4.075" "Arquitectura de Aplicaciones <-> Sistemas Operativos"
Create-Relation "3.4.094" "3.4.218" "Arquitectura de Aplicaciones <-> Desarrollo Aplicaciones II"
Write-Host ""

# ============================================================================
# RELACIONES DE MATERIAS AVANZADAS
# ============================================================================
Write-Host "🎓 Materias Avanzadas..." -ForegroundColor Green
Create-Relation "3.4.100" "3.4.218" "Proyecto Final <-> Desarrollo Aplicaciones II"
Create-Relation "3.4.100" "3.4.214" "Proyecto Final <-> Ingeniería de Software"
Create-Relation "3.4.100" "PPS06" "Proyecto Final <-> Práctica Profesional"
Create-Relation "3.4.098" "3.4.216" "Calidad de Software <-> Desarrollo Aplicaciones I"
Write-Host ""

# ============================================================================
# RELACIONES DE OPTATIVAS
# ============================================================================
Write-Host "📚 Optativas..." -ForegroundColor Green
Create-Relation "1" "2" "Optativa I <-> Optativa II"
Create-Relation "2" "3" "Optativa II <-> Optativa III"
Write-Host ""

# ============================================================================
# RESUMEN Y VERIFICACIÓN
# ============================================================================
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "✅ Proceso completado" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "📊 Resumen:"
Write-Host "  ✅ Relaciones creadas: $CREATED" -ForegroundColor Green
Write-Host "  ⚠️  Relaciones fallidas: $FAILED" -ForegroundColor Yellow
Write-Host ""

if ($CREATED -gt 0) {
    Write-Host "🔍 Verificando relaciones en Neo4j..." -ForegroundColor Yellow
    Write-Host ""
    
    Write-Host "📋 Listando relaciones RELATED (primeras 5):"
    try {
        $relations = Invoke-RestMethod -Uri "$BASE_URL/relationships" -Method GET
        $relations | Select-Object -First 5 | Format-Table
    } catch {
        Write-Host "  ⚠️  No se pudieron obtener las relaciones" -ForegroundColor Yellow
    }
    Write-Host ""
    
    Write-Host "🧪 Probando algoritmos MST:" -ForegroundColor Yellow
    Write-Host ""
    
    try {
        Write-Host "  🔹 Prim:"
        $primResult = Invoke-RestMethod -Uri "$BASE_URL/graph/mst?algo=prim" -Method GET
        $primCount = ($primResult | Measure-Object).Count
        Write-Host "    Aristas en MST: $primCount" -ForegroundColor Cyan
    } catch {
        Write-Host "    ⚠️  Error al ejecutar Prim" -ForegroundColor Yellow
    }
    
    Write-Host ""
    
    try {
        Write-Host "  🔹 Kruskal:"
        $kruskalResult = Invoke-RestMethod -Uri "$BASE_URL/graph/mst?algo=kruskal" -Method GET
        $kruskalCount = ($kruskalResult | Measure-Object).Count
        Write-Host "    Aristas en MST: $kruskalCount" -ForegroundColor Cyan
    } catch {
        Write-Host "    ⚠️  Error al ejecutar Kruskal" -ForegroundColor Yellow
    }
    
    Write-Host ""
    if ($primCount -gt 0 -and $kruskalCount -gt 0) {
        Write-Host "✅ ¡Los algoritmos MST funcionan correctamente!" -ForegroundColor Green
    } else {
        Write-Host "⚠️  Los algoritmos MST devuelven resultados vacíos." -ForegroundColor Yellow
        Write-Host "    Esto puede suceder si las materias forman componentes desconexas."
    }
} else {
    Write-Host "❌ No se crearon relaciones. Verifica que:" -ForegroundColor Red
    Write-Host "   1. Las materias existan (ejecuta init-data-ingenieria.sh/ps1 primero)"
    Write-Host "   2. El servidor esté corriendo correctamente"
    Write-Host "   3. Neo4j esté conectado"
}

Write-Host ""
Write-Host "🌐 Para ver las relaciones en Neo4j Browser (http://localhost:7474):" -ForegroundColor Cyan
Write-Host "   MATCH (a:Course)-[r:RELATED]-(b:Course) RETURN a, r, b LIMIT 25"
Write-Host ""
Write-Host "📝 Para probar MST manualmente:" -ForegroundColor Cyan
Write-Host "   curl `"${BASE_URL}/graph/mst?algo=prim`" | jq"
Write-Host "   curl `"${BASE_URL}/graph/mst?algo=kruskal`" | jq"
Write-Host ""

