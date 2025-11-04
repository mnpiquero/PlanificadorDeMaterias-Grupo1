#!/bin/bash

# Script para cargar el plan de estudios de Ingeniería en Informática (Plan 1621 - Año 2021)
# Ejecutar después de que la aplicación esté corriendo
#
# IMPORTANTE: Si ya ejecutaste este script y ves materias con properties en null,
# es necesario limpiar la base de datos antes de volver a ejecutar.
# Para limpiar Neo4j, ejecuta en el Neo4j Browser (http://localhost:7474):
#   MATCH (n) DETACH DELETE n

BASE_URL="http://localhost:8080"

echo "🚀 Iniciando carga del Plan de Estudios - Ingeniería en Informática..."
echo "📋 Plan: 1621 - Año: 2021"
echo ""
echo "⚠️  IMPORTANTE: Si tienes datos previos con errores, limpia la base de datos primero:"
echo "   Abre Neo4j Browser (http://localhost:7474) y ejecuta: MATCH (n) DETACH DELETE n"
echo ""

# Verificar que el servidor esté corriendo
echo "📡 Verificando conectividad..."
if ! curl -s "${BASE_URL}/ping"; then
    echo "❌ Error: La aplicación no está corriendo en ${BASE_URL}"
    echo "   Ejecuta: docker-compose up -d"
    exit 1
fi
echo "✅ Servidor conectado"
echo ""

# ============================================================================
# 1° AÑO - 1° CUATRIMESTRE
# ============================================================================
echo "📚 Creando materias - 1° Año, 1° Cuatrimestre..."

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"3.4.069","name":"Fundamentos de Informática","credits":0,"hours":68,"difficulty":3,"prereqs":[]}'

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"3.4.164","name":"Sistemas de Información I","credits":0,"hours":68,"difficulty":3,"prereqs":[]}'

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"2.1.002","name":"Pensamiento Crítico y Comunicación","credits":0,"hours":68,"difficulty":2,"prereqs":[]}'

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"3.4.043","name":"Teoría de Sistemas","credits":0,"hours":68,"difficulty":3,"prereqs":[]}'

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"3.1.050","name":"Elementos de Álgebra y Geometría","credits":0,"hours":85,"difficulty":4,"prereqs":[]}'

echo "✅ 1° Año, 1° Cuatrimestre: 5 materias creadas"

# ============================================================================
# 1° AÑO - 2° CUATRIMESTRE
# ============================================================================
echo "📚 Creando materias - 1° Año, 2° Cuatrimestre..."

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"3.4.071","name":"Programación I","credits":0,"hours":68,"difficulty":3,"prereqs":[{"code":"3.4.069"}]}'

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"3.3.121","name":"Sistemas de Representación","credits":0,"hours":34,"difficulty":2,"prereqs":[]}'

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"3.2.178","name":"Fundamentos de Química","credits":0,"hours":68,"difficulty":3,"prereqs":[]}'

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"3.4.072","name":"Arquitectura de Computadores","credits":0,"hours":68,"difficulty":4,"prereqs":[]}'

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"3.1.024","name":"Matemática Discreta","credits":0,"hours":68,"difficulty":4,"prereqs":[]}'

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"3.1.051","name":"Álgebra","credits":0,"hours":85,"difficulty":4,"prereqs":[{"code":"3.1.050"}]}'

echo "✅ 1° Año, 2° Cuatrimestre: 6 materias creadas"

# ============================================================================
# 2° AÑO - 1° CUATRIMESTRE
# ============================================================================
echo "📚 Creando materias - 2° Año, 1° Cuatrimestre..."

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"3.4.074","name":"Programación II","credits":0,"hours":68,"difficulty":4,"prereqs":[{"code":"3.4.071"}]}'

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"3.4.207","name":"Sistemas de Información II","credits":0,"hours":68,"difficulty":4,"prereqs":[{"code":"3.4.164"}]}'

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"3.4.075","name":"Sistemas Operativos","credits":0,"hours":68,"difficulty":4,"prereqs":[{"code":"3.4.072"}]}'

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"3.1.052","name":"Física I","credits":0,"hours":119,"difficulty":5,"prereqs":[{"code":"3.1.051"}]}'

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"3.1.053","name":"Cálculo I","credits":0,"hours":102,"difficulty":4,"prereqs":[]}'

echo "✅ 2° Año, 1° Cuatrimestre: 5 materias creadas"

# ============================================================================
# 2° AÑO - 2° CUATRIMESTRE
# ============================================================================
echo "📚 Creando materias - 2° Año, 2° Cuatrimestre..."

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"3.4.077","name":"Programación III","credits":0,"hours":68,"difficulty":4,"prereqs":[{"code":"3.4.074"}]}'

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"3.4.208","name":"Paradigma Orientado a Objetos","credits":0,"hours":68,"difficulty":4,"prereqs":[{"code":"3.4.071"}]}'

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"3.4.078","name":"Fundamentos de Telecomunicaciones","credits":0,"hours":68,"difficulty":3,"prereqs":[]}'

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"3.4.209","name":"Ingeniería de Datos I","credits":0,"hours":68,"difficulty":4,"prereqs":[{"code":"3.1.024"}]}'

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"3.1.054","name":"Cálculo II","credits":0,"hours":102,"difficulty":5,"prereqs":[{"code":"3.1.053"}]}'

echo "✅ 2° Año, 2° Cuatrimestre: 5 materias creadas"

# ============================================================================
# 3° AÑO - 1° CUATRIMESTRE
# ============================================================================
echo "📚 Creando materias - 3° Año, 1° Cuatrimestre..."

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"3.4.210","name":"Proceso de Desarrollo de Software","credits":0,"hours":68,"difficulty":4,"prereqs":[{"code":"3.4.208"}]}'

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"3.4.211","name":"Seminario de Integración Profesional","credits":0,"hours":118,"difficulty":5,"prereqs":[{"code":"3.4.074"},{"code":"3.4.207"},{"code":"3.4.209"}]}'

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"3.4.212","name":"Teleinformática y Redes","credits":0,"hours":68,"difficulty":4,"prereqs":[{"code":"3.4.078"}]}'

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"3.4.213","name":"Ingeniería de Datos II","credits":0,"hours":68,"difficulty":4,"prereqs":[{"code":"3.4.209"}]}'

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"3.1.049","name":"Probabilidad y Estadística","credits":0,"hours":68,"difficulty":4,"prereqs":[{"code":"3.1.053"}]}'

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"2.4.216","name":"Examen de Inglés","credits":0,"hours":0,"difficulty":2,"prereqs":[]}'

echo "✅ 3° Año, 1° Cuatrimestre: 6 materias creadas (incluye Examen de Inglés)"
echo "🎓 Título intermedio: Analista en Informática"

# ============================================================================
# 3° AÑO - 2° CUATRIMESTRE
# ============================================================================
echo "📚 Creando materias - 3° Año, 2° Cuatrimestre..."

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"3.4.082","name":"Aplicaciones Interactivas","credits":0,"hours":68,"difficulty":4,"prereqs":[{"code":"3.4.208"}]}'

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"3.4.214","name":"Ingeniería de Software","credits":0,"hours":68,"difficulty":4,"prereqs":[{"code":"3.4.207"}]}'

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"3.1.055","name":"Física II","credits":0,"hours":119,"difficulty":5,"prereqs":[{"code":"3.1.052"}]}'

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"3.4.215","name":"Teoría de la Computación","credits":0,"hours":102,"difficulty":5,"prereqs":[{"code":"3.1.024"},{"code":"3.4.077"}]}'

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"3.1.056","name":"Estadística Avanzada","credits":0,"hours":68,"difficulty":4,"prereqs":[{"code":"3.1.049"}]}'

echo "✅ 3° Año, 2° Cuatrimestre: 5 materias creadas"

# ============================================================================
# 4° AÑO - 1° CUATRIMESTRE
# ============================================================================
echo "📚 Creando materias - 4° Año, 1° Cuatrimestre..."

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"3.4.216","name":"Desarrollo de Aplicaciones I","credits":0,"hours":108,"difficulty":5,"prereqs":[{"code":"3.4.210"}]}'

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"3.4.089","name":"Dirección de Proyectos Informáticos","credits":0,"hours":68,"difficulty":4,"prereqs":[{"code":"3.4.207"}]}'

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"3.4.217","name":"Ciencia de Datos","credits":0,"hours":68,"difficulty":5,"prereqs":[{"code":"3.1.049"},{"code":"3.4.213"}]}'

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"3.4.092","name":"Seguridad e Integridad de la Información","credits":0,"hours":68,"difficulty":4,"prereqs":[{"code":"3.4.212"}]}'

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"3.1.025","name":"Modelado y Simulación","credits":0,"hours":68,"difficulty":4,"prereqs":[{"code":"3.1.054"}]}'

echo "✅ 4° Año, 1° Cuatrimestre: 5 materias creadas"

# ============================================================================
# 4° AÑO - 2° CUATRIMESTRE
# ============================================================================
echo "📚 Creando materias - 4° Año, 2° Cuatrimestre..."

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"1","name":"Optativa I","credits":0,"hours":68,"difficulty":3,"prereqs":[]}'

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"3.4.218","name":"Desarrollo de Aplicaciones II","credits":0,"hours":168,"difficulty":5,"prereqs":[{"code":"3.4.082"},{"code":"3.4.210"}]}'

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"3.4.086","name":"Evaluación de Proyectos Informáticos","credits":0,"hours":68,"difficulty":4,"prereqs":[{"code":"3.1.049"}]}'

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"3.4.096","name":"Inteligencia Artificial","credits":0,"hours":68,"difficulty":5,"prereqs":[{"code":"3.1.056"}]}'

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"3.4.219","name":"Tecnología y Medio Ambiente","credits":0,"hours":68,"difficulty":3,"prereqs":[]}'

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"PPS06","name":"Práctica Profesional Supervisada","credits":0,"hours":200,"difficulty":4,"prereqs":[]}'

echo "✅ 4° Año, 2° Cuatrimestre: 6 materias creadas (incluye Optativa I y PPS)"

# ============================================================================
# 5° AÑO - 1° CUATRIMESTRE
# ============================================================================
echo "📚 Creando materias - 5° Año, 1° Cuatrimestre..."

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"2","name":"Optativa II","credits":0,"hours":68,"difficulty":3,"prereqs":[]}'

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"3.4.094","name":"Arquitectura de Aplicaciones","credits":0,"hours":68,"difficulty":4,"prereqs":[{"code":"3.4.207"}]}'

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"3.4.220","name":"Tendencias Tecnológicas","credits":0,"hours":68,"difficulty":3,"prereqs":[]}'

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"3.4.100","name":"Proyecto Final de Ingeniería en Informática","credits":0,"hours":300,"difficulty":5,"prereqs":[]}'

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"3.4.098","name":"Calidad de Software","credits":0,"hours":68,"difficulty":4,"prereqs":[{"code":"3.4.214"}]}'

echo "✅ 5° Año, 1° Cuatrimestre: 5 materias creadas"

# ============================================================================
# 5° AÑO - 2° CUATRIMESTRE
# ============================================================================
echo "📚 Creando materias - 5° Año, 2° Cuatrimestre..."

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"3","name":"Optativa III","credits":0,"hours":68,"difficulty":3,"prereqs":[]}'

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"3.4.221","name":"Negocios Tecnológicos","credits":0,"hours":68,"difficulty":3,"prereqs":[]}'

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"3.4.135","name":"Tecnología e Innovación","credits":0,"hours":68,"difficulty":3,"prereqs":[]}'

curl -sS -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"2.3.056","name":"Derecho Informático","credits":0,"hours":68,"difficulty":3,"prereqs":[]}'

echo "✅ 5° Año, 2° Cuatrimestre: 4 materias creadas"
echo "🎓 Título final: Ingeniero en Informática"
echo ""

# ============================================================================
# RESUMEN
# ============================================================================
echo "================================================"
echo "✅ Plan de Estudios cargado exitosamente!"
echo "================================================"
echo ""
echo "📊 Resumen:"
echo "  📚 Total: 46 materias cargadas"
echo "  - 1° Año: 11 materias (5+6)"
echo "  - 2° Año: 10 materias (5+5)"
echo "  - 3° Año: 11 materias (6+5)"
echo "  - 4° Año: 11 materias (5+6)"
echo "  - 5° Año: 9 materias (5+4)"
echo ""
echo "  🎓 Títulos:"
echo "    - Analista en Informática (3° año)"
echo "    - Ingeniero en Informática (5° año)"
echo ""
echo "  📖 Optativas: 3 (códigos 1, 2, 3)"
echo "  💼 PPS: Práctica Profesional Supervisada (código PPS06)"
echo "  🌐 Examen de Inglés (código 2.4.216)"
echo ""
echo "🧪 Prueba los endpoints:"
echo "  curl ${BASE_URL}/courses"
echo "  curl ${BASE_URL}/courses/3.4.069"
echo "  curl '${BASE_URL}/graph/toposort'"
echo "  curl '${BASE_URL}/schedule/greedy?maxHours=20'"
echo ""
echo "🌐 Neo4j Browser: http://localhost:7474"
echo "   Usuario: neo4j"
echo "   Password: grupo123"
echo ""
echo "📝 Consultas útiles en Neo4j:"
echo "   MATCH (c:Course) RETURN c"
echo "   MATCH (a:Course)-[:REQUIRES]->(b:Course) RETURN a.name, b.name"
echo "   MATCH path = (a:Course {code:'3.4.069'})-[:REQUIRES*]->(b:Course) RETURN path"
echo ""

