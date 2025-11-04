#!/bin/bash

# Script para cargar datos de prueba en el sistema
# Ejecutar después de que la aplicación esté corriendo

BASE_URL="http://localhost:8080"

echo "🚀 Iniciando carga de datos de prueba..."
echo ""

# Verificar que el servidor esté corriendo
echo "📡 Verificando conectividad..."
if ! curl -s "${BASE_URL}/ping" > /dev/null; then
    echo "❌ Error: La aplicación no está corriendo en ${BASE_URL}"
    echo "   Ejecuta: docker-compose up -d"
    exit 1
fi
echo "✅ Servidor conectado"
echo ""

# 1. Crear materias base
echo "📚 Creando materias base..."
curl -s -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"MAT101","name":"Matemática I","credits":6,"hours":8,"difficulty":4,"prereqs":[]}' > /dev/null

curl -s -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"FIS101","name":"Física I","credits":6,"hours":6,"difficulty":3,"prereqs":[]}' > /dev/null

curl -s -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"ALG101","name":"Álgebra I","credits":4,"hours":6,"difficulty":3,"prereqs":[]}' > /dev/null

curl -s -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"PRO101","name":"Programación I","credits":6,"hours":8,"difficulty":3,"prereqs":[]}' > /dev/null

echo "✅ Materias base creadas (MAT101, FIS101, ALG101, PRO101)"
echo ""

# 2. Crear materias con prerequisitos
echo "📚 Creando materias con prerequisitos..."
curl -s -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"MAT201","name":"Matemática II","credits":6,"hours":8,"difficulty":5,"prereqs":[{"code":"MAT101"}]}' > /dev/null

curl -s -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"FIS201","name":"Física II","credits":6,"hours":6,"difficulty":4,"prereqs":[{"code":"FIS101"},{"code":"MAT101"}]}' > /dev/null

curl -s -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"ALG201","name":"Álgebra II","credits":4,"hours":6,"difficulty":4,"prereqs":[{"code":"ALG101"}]}' > /dev/null

curl -s -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"PRO201","name":"Programación II","credits":6,"hours":8,"difficulty":4,"prereqs":[{"code":"PRO101"}]}' > /dev/null

curl -s -X PUT "${BASE_URL}/courses" \
  -H "Content-Type: application/json" \
  -d '{"code":"MAT301","name":"Matemática III","credits":6,"hours":8,"difficulty":5,"prereqs":[{"code":"MAT201"}]}' > /dev/null

echo "✅ Materias con prerequisitos creadas (MAT201, FIS201, ALG201, PRO201, MAT301)"
echo ""

# 3. Crear relaciones RELATED
echo "🔗 Creando relaciones de similaridad..."
curl -s -X POST "${BASE_URL}/relationships" \
  -H "Content-Type: application/json" \
  -d '{"fromCode":"MAT101","toCode":"FIS101","similarity":0.8}' > /dev/null

curl -s -X POST "${BASE_URL}/relationships" \
  -H "Content-Type: application/json" \
  -d '{"fromCode":"MAT101","toCode":"ALG101","similarity":0.75}' > /dev/null

curl -s -X POST "${BASE_URL}/relationships/auto" \
  -H "Content-Type: application/json" \
  -d '{"fromCode":"MAT201","toCode":"FIS201"}' > /dev/null

curl -s -X POST "${BASE_URL}/relationships/auto" \
  -H "Content-Type: application/json" \
  -d '{"fromCode":"PRO101","toCode":"PRO201"}' > /dev/null

curl -s -X POST "${BASE_URL}/relationships" \
  -H "Content-Type: application/json" \
  -d '{"fromCode":"ALG101","toCode":"PRO101","similarity":0.6}' > /dev/null

echo "✅ Relaciones RELATED creadas"
echo ""

# Resumen
echo "================================================"
echo "✅ Datos de prueba cargados exitosamente!"
echo "================================================"
echo ""
echo "📊 Resumen:"
echo "  - 9 materias creadas"
echo "  - 5 relaciones de similaridad"
echo ""
echo "🧪 Prueba los endpoints:"
echo "  curl ${BASE_URL}/courses"
echo "  curl ${BASE_URL}/relationships"
echo "  curl '${BASE_URL}/graph/toposort'"
echo "  curl '${BASE_URL}/schedule/greedy?maxHours=20'"
echo ""
echo "🌐 Neo4j Browser: http://localhost:7474"
echo "   Usuario: neo4j"
echo "   Password: grupo123"
echo ""

