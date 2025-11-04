# Script para cargar el plan de estudios de Ingeniería en Informática (Plan 1621 - Año 2021)
# Ejecutar después de que la aplicación esté corriendo
#
# IMPORTANTE: Si ya ejecutaste este script y ves materias con properties en null,
# es necesario limpiar la base de datos antes de volver a ejecutar.
# Para limpiar Neo4j, ejecuta en el Neo4j Browser (http://localhost:7474):
#   MATCH (n) DETACH DELETE n

$BASE_URL = "http://localhost:8080"

Write-Host "🚀 Iniciando carga del Plan de Estudios - Ingeniería en Informática..." -ForegroundColor Green
Write-Host "📋 Plan: 1621 - Año: 2021" -ForegroundColor Cyan
Write-Host ""
Write-Host "⚠️  IMPORTANTE: Si tienes datos previos con errores, limpia la base de datos primero:" -ForegroundColor Yellow
Write-Host "   Abre Neo4j Browser (http://localhost:7474) y ejecuta: MATCH (n) DETACH DELETE n" -ForegroundColor Yellow
Write-Host ""

# Verificar que el servidor esté corriendo
Write-Host "📡 Verificando conectividad..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$BASE_URL/ping" -Method GET -UseBasicParsing -ErrorAction Stop
    Write-Host "✅ Servidor conectado" -ForegroundColor Green
} catch {
    Write-Host "❌ Error: La aplicación no está corriendo en $BASE_URL" -ForegroundColor Red
    Write-Host "   Ejecuta: docker-compose up -d" -ForegroundColor Yellow
    exit 1
}
Write-Host ""

# ============================================================================
# 1° AÑO - 1° CUATRIMESTRE
# ============================================================================
Write-Host "📚 Creando materias - 1° Año, 1° Cuatrimestre..." -ForegroundColor Cyan

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"3.4.069","name":"Fundamentos de Informática","credits":0,"hours":68,"difficulty":3,"prereqs":[]}' -UseBasicParsing

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"3.4.164","name":"Sistemas de Información I","credits":0,"hours":68,"difficulty":3,"prereqs":[]}' -UseBasicParsing

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"2.1.002","name":"Pensamiento Crítico y Comunicación","credits":0,"hours":68,"difficulty":2,"prereqs":[]}' -UseBasicParsing

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"3.4.043","name":"Teoría de Sistemas","credits":0,"hours":68,"difficulty":3,"prereqs":[]}' -UseBasicParsing

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"3.1.050","name":"Elementos de Álgebra y Geometría","credits":0,"hours":85,"difficulty":4,"prereqs":[]}' -UseBasicParsing

Write-Host "✅ 1° Año, 1° Cuatrimestre: 5 materias creadas" -ForegroundColor Green

# ============================================================================
# 1° AÑO - 2° CUATRIMESTRE
# ============================================================================
Write-Host "📚 Creando materias - 1° Año, 2° Cuatrimestre..." -ForegroundColor Cyan

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"3.4.071","name":"Programación I","credits":0,"hours":68,"difficulty":3,"prereqs":[{"code":"3.4.069"}]}' -UseBasicParsing

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"3.3.121","name":"Sistemas de Representación","credits":0,"hours":34,"difficulty":2,"prereqs":[]}' -UseBasicParsing

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"3.2.178","name":"Fundamentos de Química","credits":0,"hours":68,"difficulty":3,"prereqs":[]}' -UseBasicParsing

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"3.4.072","name":"Arquitectura de Computadores","credits":0,"hours":68,"difficulty":4,"prereqs":[]}' -UseBasicParsing

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"3.1.024","name":"Matemática Discreta","credits":0,"hours":68,"difficulty":4,"prereqs":[]}' -UseBasicParsing

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"3.1.051","name":"Álgebra","credits":0,"hours":85,"difficulty":4,"prereqs":[{"code":"3.1.050"}]}' -UseBasicParsing

Write-Host "✅ 1° Año, 2° Cuatrimestre: 6 materias creadas" -ForegroundColor Green

# ============================================================================
# 2° AÑO - 1° CUATRIMESTRE
# ============================================================================
Write-Host "📚 Creando materias - 2° Año, 1° Cuatrimestre..." -ForegroundColor Cyan

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"3.4.074","name":"Programación II","credits":0,"hours":68,"difficulty":4,"prereqs":[{"code":"3.4.071"}]}' -UseBasicParsing

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"3.4.207","name":"Sistemas de Información II","credits":0,"hours":68,"difficulty":4,"prereqs":[{"code":"3.4.164"}]}' -UseBasicParsing

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"3.4.075","name":"Sistemas Operativos","credits":0,"hours":68,"difficulty":4,"prereqs":[{"code":"3.4.072"}]}' -UseBasicParsing

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"3.1.052","name":"Física I","credits":0,"hours":119,"difficulty":5,"prereqs":[{"code":"3.1.051"}]}' -UseBasicParsing

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"3.1.053","name":"Cálculo I","credits":0,"hours":102,"difficulty":4,"prereqs":[]}' -UseBasicParsing

Write-Host "✅ 2° Año, 1° Cuatrimestre: 5 materias creadas" -ForegroundColor Green

# ============================================================================
# 2° AÑO - 2° CUATRIMESTRE
# ============================================================================
Write-Host "📚 Creando materias - 2° Año, 2° Cuatrimestre..." -ForegroundColor Cyan

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"3.4.077","name":"Programación III","credits":0,"hours":68,"difficulty":4,"prereqs":[{"code":"3.4.074"}]}' -UseBasicParsing

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"3.4.208","name":"Paradigma Orientado a Objetos","credits":0,"hours":68,"difficulty":4,"prereqs":[{"code":"3.4.071"}]}' -UseBasicParsing

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"3.4.078","name":"Fundamentos de Telecomunicaciones","credits":0,"hours":68,"difficulty":3,"prereqs":[]}' -UseBasicParsing

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"3.4.209","name":"Ingeniería de Datos I","credits":0,"hours":68,"difficulty":4,"prereqs":[{"code":"3.1.024"}]}' -UseBasicParsing

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"3.1.054","name":"Cálculo II","credits":0,"hours":102,"difficulty":5,"prereqs":[{"code":"3.1.053"}]}' -UseBasicParsing

Write-Host "✅ 2° Año, 2° Cuatrimestre: 5 materias creadas" -ForegroundColor Green

# ============================================================================
# 3° AÑO - 1° CUATRIMESTRE
# ============================================================================
Write-Host "📚 Creando materias - 3° Año, 1° Cuatrimestre..." -ForegroundColor Cyan

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"3.4.210","name":"Proceso de Desarrollo de Software","credits":0,"hours":68,"difficulty":4,"prereqs":[{"code":"3.4.208"}]}' -UseBasicParsing

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"3.4.211","name":"Seminario de Integración Profesional","credits":0,"hours":118,"difficulty":5,"prereqs":[{"code":"3.4.074"},{"code":"3.4.207"},{"code":"3.4.209"}]}' -UseBasicParsing

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"3.4.212","name":"Teleinformática y Redes","credits":0,"hours":68,"difficulty":4,"prereqs":[{"code":"3.4.078"}]}' -UseBasicParsing

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"3.4.213","name":"Ingeniería de Datos II","credits":0,"hours":68,"difficulty":4,"prereqs":[{"code":"3.4.209"}]}' -UseBasicParsing

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"3.1.049","name":"Probabilidad y Estadística","credits":0,"hours":68,"difficulty":4,"prereqs":[{"code":"3.1.053"}]}' -UseBasicParsing

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"2.4.216","name":"Examen de Inglés","credits":0,"hours":0,"difficulty":2,"prereqs":[]}' -UseBasicParsing

Write-Host "✅ 3° Año, 1° Cuatrimestre: 6 materias creadas (incluye Examen de Inglés)" -ForegroundColor Green
Write-Host "🎓 Título intermedio: Analista en Informática" -ForegroundColor Magenta

# ============================================================================
# 3° AÑO - 2° CUATRIMESTRE
# ============================================================================
Write-Host "📚 Creando materias - 3° Año, 2° Cuatrimestre..." -ForegroundColor Cyan

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"3.4.082","name":"Aplicaciones Interactivas","credits":0,"hours":68,"difficulty":4,"prereqs":[{"code":"3.4.208"}]}' -UseBasicParsing

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"3.4.214","name":"Ingeniería de Software","credits":0,"hours":68,"difficulty":4,"prereqs":[{"code":"3.4.207"}]}' -UseBasicParsing

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"3.1.055","name":"Física II","credits":0,"hours":119,"difficulty":5,"prereqs":[{"code":"3.1.052"}]}' -UseBasicParsing

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"3.4.215","name":"Teoría de la Computación","credits":0,"hours":102,"difficulty":5,"prereqs":[{"code":"3.1.024"},{"code":"3.4.077"}]}' -UseBasicParsing

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"3.1.056","name":"Estadística Avanzada","credits":0,"hours":68,"difficulty":4,"prereqs":[{"code":"3.1.049"}]}' -UseBasicParsing

Write-Host "✅ 3° Año, 2° Cuatrimestre: 5 materias creadas" -ForegroundColor Green

# ============================================================================
# 4° AÑO - 1° CUATRIMESTRE
# ============================================================================
Write-Host "📚 Creando materias - 4° Año, 1° Cuatrimestre..." -ForegroundColor Cyan

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"3.4.216","name":"Desarrollo de Aplicaciones I","credits":0,"hours":108,"difficulty":5,"prereqs":[{"code":"3.4.210"}]}' -UseBasicParsing

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"3.4.089","name":"Dirección de Proyectos Informáticos","credits":0,"hours":68,"difficulty":4,"prereqs":[{"code":"3.4.207"}]}' -UseBasicParsing

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"3.4.217","name":"Ciencia de Datos","credits":0,"hours":68,"difficulty":5,"prereqs":[{"code":"3.1.049"},{"code":"3.4.213"}]}' -UseBasicParsing

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"3.4.092","name":"Seguridad e Integridad de la Información","credits":0,"hours":68,"difficulty":4,"prereqs":[{"code":"3.4.212"}]}' -UseBasicParsing

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"3.1.025","name":"Modelado y Simulación","credits":0,"hours":68,"difficulty":4,"prereqs":[{"code":"3.1.054"}]}' -UseBasicParsing

Write-Host "✅ 4° Año, 1° Cuatrimestre: 5 materias creadas" -ForegroundColor Green

# ============================================================================
# 4° AÑO - 2° CUATRIMESTRE
# ============================================================================
Write-Host "📚 Creando materias - 4° Año, 2° Cuatrimestre..." -ForegroundColor Cyan

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"1","name":"Optativa I","credits":0,"hours":68,"difficulty":3,"prereqs":[]}' -UseBasicParsing

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"3.4.218","name":"Desarrollo de Aplicaciones II","credits":0,"hours":168,"difficulty":5,"prereqs":[{"code":"3.4.082"},{"code":"3.4.210"}]}' -UseBasicParsing

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"3.4.086","name":"Evaluación de Proyectos Informáticos","credits":0,"hours":68,"difficulty":4,"prereqs":[{"code":"3.1.049"}]}' -UseBasicParsing

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"3.4.096","name":"Inteligencia Artificial","credits":0,"hours":68,"difficulty":5,"prereqs":[{"code":"3.1.056"}]}' -UseBasicParsing

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"3.4.219","name":"Tecnología y Medio Ambiente","credits":0,"hours":68,"difficulty":3,"prereqs":[]}' -UseBasicParsing

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"PPS06","name":"Práctica Profesional Supervisada","credits":0,"hours":200,"difficulty":4,"prereqs":[]}' -UseBasicParsing

Write-Host "✅ 4° Año, 2° Cuatrimestre: 6 materias creadas (incluye Optativa I y PPS)" -ForegroundColor Green

# ============================================================================
# 5° AÑO - 1° CUATRIMESTRE
# ============================================================================
Write-Host "📚 Creando materias - 5° Año, 1° Cuatrimestre..." -ForegroundColor Cyan

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"2","name":"Optativa II","credits":0,"hours":68,"difficulty":3,"prereqs":[]}' -UseBasicParsing

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"3.4.094","name":"Arquitectura de Aplicaciones","credits":0,"hours":68,"difficulty":4,"prereqs":[{"code":"3.4.207"}]}' -UseBasicParsing

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"3.4.220","name":"Tendencias Tecnológicas","credits":0,"hours":68,"difficulty":3,"prereqs":[]}' -UseBasicParsing

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"3.4.100","name":"Proyecto Final de Ingeniería en Informática","credits":0,"hours":300,"difficulty":5,"prereqs":[]}' -UseBasicParsing

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"3.4.098","name":"Calidad de Software","credits":0,"hours":68,"difficulty":4,"prereqs":[{"code":"3.4.214"}]}' -UseBasicParsing

Write-Host "✅ 5° Año, 1° Cuatrimestre: 5 materias creadas" -ForegroundColor Green

# ============================================================================
# 5° AÑO - 2° CUATRIMESTRE
# ============================================================================
Write-Host "📚 Creando materias - 5° Año, 2° Cuatrimestre..." -ForegroundColor Cyan

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"3","name":"Optativa III","credits":0,"hours":68,"difficulty":3,"prereqs":[]}' -UseBasicParsing

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"3.4.221","name":"Negocios Tecnológicos","credits":0,"hours":68,"difficulty":3,"prereqs":[]}' -UseBasicParsing

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"3.4.135","name":"Tecnología e Innovación","credits":0,"hours":68,"difficulty":3,"prereqs":[]}' -UseBasicParsing

Invoke-WebRequest -Uri "$BASE_URL/courses" -Method PUT -ContentType "application/json" `
  -Body '{"code":"2.3.056","name":"Derecho Informático","credits":0,"hours":68,"difficulty":3,"prereqs":[]}' -UseBasicParsing

Write-Host "✅ 5° Año, 2° Cuatrimestre: 4 materias creadas" -ForegroundColor Green
Write-Host "🎓 Título final: Ingeniero en Informática" -ForegroundColor Magenta
Write-Host ""

# ============================================================================
# RESUMEN
# ============================================================================
Write-Host "================================================" -ForegroundColor Green
Write-Host "✅ Plan de Estudios cargado exitosamente!" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Green
Write-Host ""
Write-Host "📊 Resumen:" -ForegroundColor Cyan
Write-Host "  📚 Total: 46 materias cargadas"
Write-Host "  - 1° Año: 11 materias (5+6)"
Write-Host "  - 2° Año: 10 materias (5+5)"
Write-Host "  - 3° Año: 11 materias (6+5)"
Write-Host "  - 4° Año: 11 materias (5+6)"
Write-Host "  - 5° Año: 9 materias (5+4)"
Write-Host ""
Write-Host "  🎓 Títulos:" -ForegroundColor Magenta
Write-Host "    - Analista en Informática (3° año)"
Write-Host "    - Ingeniero en Informática (5° año)"
Write-Host ""
Write-Host "  📖 Optativas: 3 (códigos 1, 2, 3)"
Write-Host "  💼 PPS: Práctica Profesional Supervisada (código PPS06)"
Write-Host "  🌐 Examen de Inglés (código 2.4.216)"
Write-Host ""
Write-Host "🧪 Prueba los endpoints:" -ForegroundColor Yellow
Write-Host "  curl $BASE_URL/courses"
Write-Host "  curl $BASE_URL/courses/3.4.069"
Write-Host "  curl '$BASE_URL/graph/toposort'"
Write-Host "  curl '$BASE_URL/schedule/greedy?maxHours=20'"
Write-Host ""
Write-Host "🌐 Neo4j Browser: http://localhost:7474" -ForegroundColor Cyan
Write-Host "   Usuario: neo4j"
Write-Host "   Password: grupo123"
Write-Host ""
Write-Host "📝 Consultas útiles en Neo4j:" -ForegroundColor Yellow
Write-Host "   MATCH (c:Course) RETURN c"
Write-Host "   MATCH (a:Course)-[:REQUIRES]->(b:Course) RETURN a.name, b.name"
Write-Host "   MATCH path = (a:Course {code:'3.4.069'})-[:REQUIRES*]->(b:Course) RETURN path"
Write-Host ""

