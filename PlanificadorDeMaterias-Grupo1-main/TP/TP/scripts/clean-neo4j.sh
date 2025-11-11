#!/bin/bash

# Script para limpiar completamente la base de datos Neo4j
# Ejecutar ANTES de cargar el plan de estudios

BASE_URL="http://localhost:8080"

echo "🧹 Limpiando base de datos Neo4j..."
echo ""

# Verificar conectividad
if ! curl -s "${BASE_URL}/ping" > /dev/null 2>&1; then
    echo "❌ Error: La aplicación no está corriendo en ${BASE_URL}"
    echo "   Ejecuta: docker-compose up -d"
    exit 1
fi

echo "📡 Conectado al servidor"
echo ""

# Obtener todos los códigos de cursos
echo "🔍 Obteniendo lista de cursos..."
COURSES=$(curl -s "${BASE_URL}/courses" | grep -o '"code":"[^"]*"' | cut -d'"' -f4)

if [ -z "$COURSES" ]; then
    echo "✅ Base de datos ya está vacía"
    exit 0
fi

COUNT=$(echo "$COURSES" | wc -l | tr -d ' ')
echo "📚 Encontrados $COUNT cursos para eliminar"
echo ""

# Eliminar cada curso
DELETED=0
for CODE in $COURSES; do
    echo "🗑️  Eliminando: $CODE"
    RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "${BASE_URL}/courses/${CODE}")
    if [ "$RESPONSE" = "204" ]; then
        ((DELETED++))
    else
        echo "   ⚠️  Error eliminando $CODE (HTTP $RESPONSE)"
    fi
done

echo ""
echo "================================================"
echo "✅ Limpieza completada"
echo "================================================"
echo "📊 Estadísticas:"
echo "   - Cursos eliminados: $DELETED"
echo ""
echo "🔄 Ahora puedes ejecutar el script de carga:"
echo "   ./init-data-ingenieria.sh"
echo ""

