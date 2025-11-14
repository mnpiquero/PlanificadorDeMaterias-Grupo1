#!/bin/bash

# Script para crear relaciones RELATED automáticas entre materias del Plan 1621
# Este script debe ejecutarse DESPUÉS de init-data-ingenieria.sh
# Crea relaciones entre materias relacionadas temáticamente usando el endpoint /relationships/auto

BASE_URL="http://localhost:8080"

echo "🔗 Iniciando creación de relaciones RELATED..."
echo "📋 Plan: Ingeniería en Informática 1621"
echo ""

# Verificar que el servidor esté corriendo
echo "📡 Verificando conectividad..."
if ! curl -s "${BASE_URL}/ping" > /dev/null 2>&1; then
    echo "❌ Error: La aplicación no está corriendo en ${BASE_URL}"
    echo "   Ejecuta: docker-compose up -d"
    exit 1
fi
echo "✅ Servidor conectado"
echo ""

# Contador de relaciones creadas
CREATED=0
FAILED=0

# Función para crear relación con manejo de errores
create_relation() {
    local from=$1
    local to=$2
    local desc=$3
    
    echo "  Creando: $desc"
    response=$(curl -sS -X POST "${BASE_URL}/relationships/auto" \
        -H "Content-Type: application/json" \
        -d "{\"fromCode\":\"$from\",\"toCode\":\"$to\"}" 2>&1)
    
    if echo "$response" | grep -q "similarity"; then
        ((CREATED++))
    else
        ((FAILED++))
        echo "    ⚠️  Falló: $from <-> $to"
    fi
}

echo "================================================"
echo "🔗 Creando relaciones RELATED"
echo "================================================"
echo ""

# ============================================================================
# SECUENCIAS DE PROGRAMACIÓN
# ============================================================================
echo "💻 Secuencia de Programación..."
create_relation "3.4.069" "3.4.071" "Fundamentos de Informática <-> Programación I"
create_relation "3.4.071" "3.4.074" "Programación I <-> Programación II"
create_relation "3.4.074" "3.4.077" "Programación II <-> Programación III"
create_relation "3.4.071" "3.4.208" "Programación I <-> Paradigma OO"
create_relation "3.4.074" "3.4.208" "Programación II <-> Paradigma OO"
create_relation "3.4.208" "3.4.082" "Paradigma OO <-> Aplicaciones Interactivas"
create_relation "3.4.077" "3.4.210" "Programación III <-> Proceso Desarrollo Software"
echo ""

# ============================================================================
# SECUENCIAS DE SISTEMAS DE INFORMACIÓN
# ============================================================================
echo "📊 Secuencia de Sistemas de Información..."
create_relation "3.4.164" "3.4.207" "Sistemas de Información I <-> II"
create_relation "3.4.207" "3.4.214" "Sistemas de Información II <-> Ingeniería de Software"
create_relation "3.4.207" "3.4.089" "Sistemas de Información II <-> Dirección de Proyectos"
create_relation "3.4.207" "3.4.094" "Sistemas de Información II <-> Arquitectura de Aplicaciones"
echo ""

# ============================================================================
# SECUENCIAS DE DESARROLLO DE SOFTWARE
# ============================================================================
echo "🏗️  Secuencia de Desarrollo de Software..."
create_relation "3.4.210" "3.4.214" "Proceso Desarrollo <-> Ingeniería de Software"
create_relation "3.4.210" "3.4.216" "Proceso Desarrollo <-> Desarrollo Aplicaciones I"
create_relation "3.4.216" "3.4.218" "Desarrollo Aplicaciones I <-> II"
create_relation "3.4.214" "3.4.098" "Ingeniería de Software <-> Calidad de Software"
create_relation "3.4.082" "3.4.218" "Aplicaciones Interactivas <-> Desarrollo Aplicaciones II"
echo ""

# ============================================================================
# SECUENCIAS DE DATOS E IA
# ============================================================================
echo "🤖 Secuencia de Datos e Inteligencia Artificial..."
create_relation "3.4.209" "3.4.213" "Ingeniería de Datos I <-> II"
create_relation "3.4.213" "3.4.217" "Ingeniería de Datos II <-> Ciencia de Datos"
create_relation "3.4.217" "3.4.096" "Ciencia de Datos <-> Inteligencia Artificial"
create_relation "3.1.049" "3.4.217" "Probabilidad y Estadística <-> Ciencia de Datos"
create_relation "3.1.056" "3.4.096" "Estadística Avanzada <-> Inteligencia Artificial"
echo ""

# ============================================================================
# SECUENCIAS DE REDES Y TELECOMUNICACIONES
# ============================================================================
echo "🌐 Secuencia de Redes y Telecomunicaciones..."
create_relation "3.4.072" "3.4.075" "Arquitectura de Computadores <-> Sistemas Operativos"
create_relation "3.4.078" "3.4.212" "Fundamentos Telecomunicaciones <-> Teleinformática"
create_relation "3.4.212" "3.4.092" "Teleinformática <-> Seguridad e Integridad"
echo ""

# ============================================================================
# SECUENCIAS DE MATEMÁTICA
# ============================================================================
echo "📐 Secuencia de Matemática..."
create_relation "3.1.050" "3.1.051" "Elementos de Álgebra <-> Álgebra"
create_relation "3.1.053" "3.1.054" "Cálculo I <-> Cálculo II"
create_relation "3.1.051" "3.1.052" "Álgebra <-> Física I"
create_relation "3.1.052" "3.1.055" "Física I <-> Física II"
create_relation "3.1.053" "3.1.049" "Cálculo I <-> Probabilidad y Estadística"
create_relation "3.1.049" "3.1.056" "Probabilidad y Estadística <-> Estadística Avanzada"
create_relation "3.1.024" "3.4.209" "Matemática Discreta <-> Ingeniería de Datos I"
create_relation "3.1.024" "3.4.215" "Matemática Discreta <-> Teoría de la Computación"
create_relation "3.1.054" "3.1.025" "Cálculo II <-> Modelado y Simulación"
echo ""

# ============================================================================
# RELACIONES DE GESTIÓN Y PROYECTOS
# ============================================================================
echo "📈 Área de Gestión y Proyectos..."
create_relation "3.4.089" "3.4.086" "Dirección de Proyectos <-> Evaluación de Proyectos"
create_relation "3.4.211" "3.4.089" "Seminario Integración <-> Dirección de Proyectos"
create_relation "3.4.211" "3.4.214" "Seminario Integración <-> Ingeniería de Software"
echo ""

# ============================================================================
# RELACIONES DE TECNOLOGÍA Y TENDENCIAS
# ============================================================================
echo "🚀 Área de Tecnología y Tendencias..."
create_relation "3.4.220" "3.4.135" "Tendencias Tecnológicas <-> Tecnología e Innovación"
create_relation "3.4.219" "3.4.135" "Tecnología y Medio Ambiente <-> Tecnología e Innovación"
create_relation "3.4.221" "3.4.086" "Negocios Tecnológicos <-> Evaluación de Proyectos"
echo ""

# ============================================================================
# RELACIONES INTERDISCIPLINARIAS
# ============================================================================
echo "🔄 Relaciones Interdisciplinarias..."
create_relation "3.4.164" "3.4.043" "Sistemas de Información I <-> Teoría de Sistemas"
create_relation "3.4.077" "3.4.215" "Programación III <-> Teoría de la Computación"
create_relation "3.4.094" "3.4.075" "Arquitectura de Aplicaciones <-> Sistemas Operativos"
create_relation "3.4.094" "3.4.218" "Arquitectura de Aplicaciones <-> Desarrollo Aplicaciones II"
echo ""

# ============================================================================
# RELACIONES DE MATERIAS AVANZADAS
# ============================================================================
echo "🎓 Materias Avanzadas..."
create_relation "3.4.100" "3.4.218" "Proyecto Final <-> Desarrollo Aplicaciones II"
create_relation "3.4.100" "3.4.214" "Proyecto Final <-> Ingeniería de Software"
create_relation "3.4.100" "PPS06" "Proyecto Final <-> Práctica Profesional"
create_relation "3.4.098" "3.4.216" "Calidad de Software <-> Desarrollo Aplicaciones I"
echo ""

# ============================================================================
# RELACIONES DE OPTATIVAS (entre sí)
# ============================================================================
echo "📚 Optativas..."
create_relation "1" "2" "Optativa I <-> Optativa II"
create_relation "2" "3" "Optativa II <-> Optativa III"
echo ""

# ============================================================================
# RESUMEN Y VERIFICACIÓN
# ============================================================================
echo "================================================"
echo "✅ Proceso completado"
echo "================================================"
echo ""
echo "📊 Resumen:"
echo "  ✅ Relaciones creadas: $CREATED"
echo "  ⚠️  Relaciones fallidas: $FAILED"
echo ""

if [ $CREATED -gt 0 ]; then
    echo "🔍 Verificando relaciones en Neo4j..."
    echo ""
    
    # Obtener todas las relaciones creadas
    echo "📋 Listando relaciones RELATED (primeras 10):"
    curl -sS "${BASE_URL}/relationships" | head -20
    echo ""
    echo ""
    
    echo "🧪 Probando algoritmos MST:"
    echo ""
    echo "  🔹 Prim:"
    prim_result=$(curl -sS "${BASE_URL}/graph/mst?algo=prim")
    prim_count=$(echo "$prim_result" | grep -o '"from"' | wc -l)
    echo "    Aristas en MST: $prim_count"
    
    echo ""
    echo "  🔹 Kruskal:"
    kruskal_result=$(curl -sS "${BASE_URL}/graph/mst?algo=kruskal")
    kruskal_count=$(echo "$kruskal_result" | grep -o '"from"' | wc -l)
    echo "    Aristas en MST: $kruskal_count"
    
    echo ""
    if [ $prim_count -gt 0 ] && [ $kruskal_count -gt 0 ]; then
        echo "✅ ¡Los algoritmos MST funcionan correctamente!"
    else
        echo "⚠️  Los algoritmos MST devuelven resultados vacíos."
        echo "    Esto puede suceder si las materias forman componentes desconexas."
    fi
else
    echo "❌ No se crearon relaciones. Verifica que:"
    echo "   1. Las materias existan (ejecuta init-data-ingenieria.sh primero)"
    echo "   2. El servidor esté corriendo correctamente"
    echo "   3. Neo4j esté conectado"
fi

echo ""
echo "🌐 Para ver las relaciones en Neo4j Browser (http://localhost:7474):"
echo "   MATCH (a:Course)-[r:RELATED]-(b:Course) RETURN a, r, b LIMIT 25"
echo ""
echo "📝 Para probar MST manualmente:"
echo "   curl \"${BASE_URL}/graph/mst?algo=prim\" | jq"
echo "   curl \"${BASE_URL}/graph/mst?algo=kruskal\" | jq"
echo ""

