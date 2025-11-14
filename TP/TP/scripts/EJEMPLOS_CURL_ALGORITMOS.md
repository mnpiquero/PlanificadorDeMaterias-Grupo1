# Ejemplos cURL para Probar Algoritmos

Este documento contiene ejemplos de comandos cURL para probar todos los algoritmos del proyecto usando los datos reales del Plan de Estudios de Ingeniería en Informática (Plan 1621).

**Base URL**: `http://localhost:8080`

**Requisito previo**: Ejecutar el script `init-data-ingenieria.sh` para cargar los datos.

---

## 📊 Algoritmos de Grafos (`/graph`)

### 1. DFS (Depth-First Search)

Explora todas las materias alcanzables desde una materia inicial siguiendo prerequisitos.

```bash
# DFS desde "Fundamentos de Informática" (primer año)
curl "http://localhost:8080/graph/dfs?from=3.4.069"

# DFS desde "Programación I" (requiere 3.4.069)
curl "http://localhost:8080/graph/dfs?from=3.4.071"

# DFS desde "Programación II" (requiere 3.4.071)
curl "http://localhost:8080/graph/dfs?from=3.4.074"

# DFS desde "Proyecto Final" (último año)
curl "http://localhost:8080/graph/dfs?from=3.4.100"
```

**Respuesta esperada**: Lista de códigos de materias en orden de visita DFS.

---

### 2. BFS por Capas (Breadth-First Search)

Organiza materias por niveles de prerequisitos.

```bash
# BFS desde "Fundamentos de Informática"
curl "http://localhost:8080/graph/bfs-layers?from=3.4.069"

# BFS desde "Programación I"
curl "http://localhost:8080/graph/bfs-layers?from=3.4.071"

# BFS desde "Proyecto Final" (muestra todo el camino hacia atrás)
curl "http://localhost:8080/graph/bfs-layers?from=3.4.100"
```

**Respuesta esperada**: Lista de listas, donde cada lista es una capa/nivel de prerequisitos.

---

### 3. Ordenamiento Topológico (Kahn's Algorithm)

Determina un orden válido de cursada respetando prerequisitos.

```bash
# Orden topológico completo (sin materias aprobadas)
curl "http://localhost:8080/graph/toposort"

# Orden topológico considerando materias aprobadas del 1° año
curl "http://localhost:8080/graph/toposort?approved=3.4.069&approved=3.4.164&approved=2.1.002&approved=3.4.043&approved=3.1.050"

# Orden topológico con 1° y 2° año aprobados
curl "http://localhost:8080/graph/toposort?approved=3.4.069&approved=3.4.164&approved=3.4.071&approved=3.4.074&approved=3.4.207&approved=3.4.075&approved=3.1.052&approved=3.1.053"

# Orden topológico con materias avanzadas aprobadas
curl "http://localhost:8080/graph/toposort?approved=3.4.074&approved=3.4.207&approved=3.4.209"
```

**Respuesta esperada**: Lista de códigos de materias en orden topológico válido.

---

### 4. Detección de Ciclos

Valida que el plan de estudios no tenga dependencias circulares.

```bash
# Verificar si hay ciclos en el grafo
curl "http://localhost:8080/graph/cycles"
```

**Respuesta esperada**:
```json
{"hasCycle": false}
```

**Nota**: Si el resultado es `true`, hay un error en la configuración del plan de estudios.

---

### 5. Camino Más Corto (Dijkstra)

Encuentra el mejor camino entre dos materias según diferentes métricas.

```bash
# Camino más corto por dificultad (default)
# De "Fundamentos de Informática" a "Proyecto Final"
curl "http://localhost:8080/graph/shortest?from=3.4.069&to=3.4.100"

# De "Programación I" a "Desarrollo de Aplicaciones II"
curl "http://localhost:8080/graph/shortest?from=3.4.071&to=3.4.218"

# Camino más corto por horas (minimiza horas totales)
curl "http://localhost:8080/graph/shortest?from=3.4.069&to=3.4.100&metric=hours"

# Camino más corto por créditos (maximiza créditos)
curl "http://localhost:8080/graph/shortest?from=3.4.071&to=3.4.218&metric=credits"

# Camino desde "Álgebra" a "Cálculo II"
curl "http://localhost:8080/graph/shortest?from=3.1.051&to=3.1.054&metric=difficulty"

# Camino desde "Programación I" a "Ingeniería de Software"
curl "http://localhost:8080/graph/shortest?from=3.4.071&to=3.4.214&metric=hours"
```

**Parámetros**:
- `from`: Materia origen
- `to`: Materia destino
- `metric`: `difficulty` (default), `hours`, o `credits`
- `direction`: `prereqs` (default) o `dependents`

**Respuesta esperada**: Lista de códigos de materias que forman el camino más corto.

---

### 6. Árbol de Expansión Mínima (MST)

Encuentra el conjunto mínimo de relaciones RELATED que conecten todas las materias relacionadas.

**⚠️ IMPORTANTE**: Requiere que existan relaciones RELATED en Neo4j. Si no las hay, el resultado será vacío.

#### Crear Relaciones RELATED Automáticamente

**Opción 1: Usar el script automatizado (RECOMENDADO)**

```bash
# En Linux/Mac
./scripts/create-related-ingenieria.sh

# En Windows PowerShell
.\scripts\create-related-ingenieria.ps1
```

Este script crea automáticamente ~57 relaciones RELATED entre materias relacionadas temáticamente:
- Secuencias de Programación (7 relaciones)
- Sistemas de Información (4 relaciones)
- Desarrollo de Software (5 relaciones)
- Datos e IA (5 relaciones)
- Redes y Telecomunicaciones (3 relaciones)
- Matemática (9 relaciones)
- Gestión y Proyectos (3 relaciones)
- Tecnología y Tendencias (3 relaciones)
- Interdisciplinarias (4 relaciones)
- Materias Avanzadas (4 relaciones)
- Optativas (2 relaciones)

**Opción 2: Crear relaciones manualmente**

```bash
# Crear relación RELATED entre dos materias con similaridad específica
curl -X POST "http://localhost:8080/relationships" \
  -H "Content-Type: application/json" \
  -d '{"fromCode":"3.4.071","toCode":"3.4.074","similarity":0.8}'

# Crear relación RELATED con similaridad automática (calculada por la API)
curl -X POST "http://localhost:8080/relationships/auto" \
  -H "Content-Type: application/json" \
  -d '{"fromCode":"3.4.071","toCode":"3.4.074"}'
```

La similaridad automática se calcula basándose en:
- Diferencia en créditos
- Diferencia en horas semanales
- Diferencia en dificultad

#### Ejecutar MST

```bash
# MST usando algoritmo de Prim (default)
curl "http://localhost:8080/graph/mst?algo=prim"

# MST usando algoritmo de Kruskal
curl "http://localhost:8080/graph/mst?algo=kruskal"

# 🆕 MST con Prim desde un nodo específico (explora área temática)
curl "http://localhost:8080/graph/mst?algo=prim&startNode=3.4.071"  # Desde Programación I
curl "http://localhost:8080/graph/mst?algo=prim&startNode=3.1.053"  # Desde Cálculo I
```

**Respuesta esperada**: Lista de aristas con formato:
```json
[
  {"from": "3.4.071", "to": "3.4.074", "weight": 107},
  {"from": "3.4.074", "to": "3.4.077", "weight": 100}
]
```

**Nota sobre pesos**: El peso en el MST es `peso = round((1/similaridad) * 100)`. Mayor similaridad → menor peso → mayor prioridad en el MST.

#### 🆕 MST Forest (Detectar Áreas Temáticas)

Encuentra MST para cada componente conexa, identificando automáticamente las áreas del plan de estudios:

```bash
# Obtener todas las componentes temáticas
curl "http://localhost:8080/graph/mst-forest" | jq

# Contar áreas temáticas
curl -s "http://localhost:8080/graph/mst-forest" | jq '. | length'

# Ver resumen de componentes
curl -s "http://localhost:8080/graph/mst-forest" | jq '[.[] | {
  area: .componentId,
  materias: (.nodes | length),
  aristas: (.edges | length)
}]'

# Ver la componente más grande
curl -s "http://localhost:8080/graph/mst-forest" | jq 'max_by(.nodes | length)'
```

**Respuesta esperada**: Lista de componentes, cada una representa un área temática.

#### 🆕 Estadísticas de Conectividad

```bash
# Ver estadísticas del grafo RELATED
curl "http://localhost:8080/graph/connectivity-stats" | jq
```

**Respuesta esperada**:
```json
{
  "totalNodes": 52,
  "connectedNodes": 47,
  "disconnectedNodes": 5,
  "disconnectedNodesList": ["2.1.002", "3.2.178", "3.3.121", "2.3.056", "2.4.216"],
  "totalComponents": 6,
  "coveragePercentage": 90.38
}
```

**Interpretación**:
- `totalNodes`: Total de materias en el plan
- `connectedNodes`: Materias con relaciones RELATED
- `disconnectedNodes`: Materias sin relaciones (candidatas para agregar)
- `coveragePercentage`: % de cobertura del grafo RELATED

📖 **Para más ejemplos detallados**, ver [EJEMPLOS_MST_MEJORADO.md](./EJEMPLOS_MST_MEJORADO.md)

---

## 📅 Algoritmos de Planificación (`/schedule`)

### 7. Materias Disponibles

Lista las materias que un estudiante puede cursar según sus materias aprobadas.

```bash
# Sin materias aprobadas (materias sin prerequisitos)
curl "http://localhost:8080/schedule/available"

# Con materias del 1° año aprobadas
curl "http://localhost:8080/schedule/available?approved=3.4.069&approved=3.4.164&approved=2.1.002&approved=3.4.043&approved=3.1.050"

# Con 1° y 2° año aprobados
curl "http://localhost:8080/schedule/available?approved=3.4.069&approved=3.4.164&approved=3.4.071&approved=3.4.074&approved=3.4.207&approved=3.4.075&approved=3.1.052&approved=3.1.053"

# Con materias específicas aprobadas (ej: solo Programación I y II)
curl "http://localhost:8080/schedule/available?approved=3.4.071&approved=3.4.074"
```

**Respuesta esperada**: Lista de objetos `Course` con todas las materias disponibles para cursar.

---

### 8. Selección Greedy

Selección rápida de materias para un cuatrimestre optimizando un criterio.

```bash
# Maximizar créditos (default) con límite de 20 horas/semana
curl "http://localhost:8080/schedule/greedy?approved=3.4.069&approved=3.4.164&maxHours=20"

# Maximizar créditos con materias del 1° año aprobadas
curl "http://localhost:8080/schedule/greedy?approved=3.4.069&approved=3.4.164&approved=3.4.071&approved=3.1.050&value=credits&maxHours=24"

# Minimizar dificultad (selecciona materias más fáciles)
curl "http://localhost:8080/schedule/greedy?approved=3.4.071&approved=3.4.074&value=difficulty&maxHours=20"

# Minimizar horas semanales
curl "http://localhost:8080/schedule/greedy?approved=3.4.074&approved=3.4.207&value=hours&maxHours=30"

# Con múltiples materias aprobadas del 2° año
curl "http://localhost:8080/schedule/greedy?approved=3.4.074&approved=3.4.207&approved=3.4.075&approved=3.1.052&approved=3.1.053&value=credits&maxHours=25"
```

**Parámetros**:
- `approved`: Lista de materias aprobadas (opcional)
- `value`: `credits` (default), `difficulty`, o `hours`
- `maxHours`: Límite de horas semanales (default: 24)

**Respuesta esperada**: Lista de objetos `Course` seleccionadas por el algoritmo greedy.

---

### 9. Programación Dinámica (Knapsack)

Selección óptima de materias para un cuatrimestre usando DP.

```bash
# Maximizar créditos con límite de 20 horas (solución óptima)
curl "http://localhost:8080/schedule/dp?approved=3.4.069&approved=3.4.164&value=credits&maxHours=20"

# Maximizar créditos con materias del 1° año aprobadas
curl "http://localhost:8080/schedule/dp?approved=3.4.069&approved=3.4.164&approved=3.4.071&approved=3.1.050&value=credits&maxHours=24"

# Minimizar dificultad (solución óptima)
curl "http://localhost:8080/schedule/dp?approved=3.4.071&approved=3.4.074&value=difficulty&maxHours=20"

# Minimizar horas semanales
curl "http://localhost:8080/schedule/dp?approved=3.4.074&approved=3.4.207&value=hours&maxHours=30"

# Con materias avanzadas aprobadas
curl "http://localhost:8080/schedule/dp?approved=3.4.074&approved=3.4.207&approved=3.4.209&approved=3.4.210&value=credits&maxHours=25"
```

**Parámetros**:
- `approved`: Lista de materias aprobadas (opcional)
- `value`: `credits` (default), `difficulty`, o `hours`
- `maxHours`: Límite de horas semanales (default: 24)

**Respuesta esperada**: Lista de objetos `Course` con la selección óptima.

**Nota**: Este algoritmo garantiza la solución óptima, pero es más lento que greedy.

---

### 10. Backtracking (Todas las Rutas)

Encuentra todas las rutas posibles entre dos materias.

```bash
# Todas las rutas de "Programación I" a "Desarrollo de Aplicaciones II"
curl "http://localhost:8080/schedule/backtracking?from=3.4.071&to=3.4.218"

# Todas las rutas de "Fundamentos de Informática" a "Desarrollo de Aplicaciones II"
curl "http://localhost:8080/schedule/backtracking?from=3.4.069&to=3.4.218"

# Todas las rutas de "Álgebra" a "Cálculo II" con límite de profundidad
curl "http://localhost:8080/schedule/backtracking?from=3.1.051&to=3.1.054&maxDepth=5"

# Todas las rutas de "Programación I" a "Ingeniería de Software"
curl "http://localhost:8080/schedule/backtracking?from=3.4.071&to=3.4.214&maxDepth=8"

# Todas las rutas de "Sistemas de Información I" a "Ingeniería de Software"
curl "http://localhost:8080/schedule/backtracking?from=3.4.164&to=3.4.214&maxDepth=6"
```

**Parámetros**:
- `from`: Materia origen
- `to`: Materia destino
- `maxDepth`: Límite de profundidad para evitar explosión combinatoria (default: 10)

**Respuesta esperada**: Lista de listas, donde cada lista interna es una ruta posible:
```json
[
  ["3.4.071", "3.4.074", "3.4.077", "3.4.210", "3.4.216", "3.4.218"],
  ["3.4.071", "3.4.208", "3.4.210", "3.4.216", "3.4.218"]
]
```

**Nota**: Este algoritmo puede ser lento para rutas muy largas. Usa `maxDepth` para limitar la búsqueda.

---

### 11. Branch & Bound (Planificación Óptima)

Genera el mejor plan de cursada para múltiples cuatrimestres.

```bash
# Plan óptimo para 4 cuatrimestres con 24 horas/semana
curl "http://localhost:8080/schedule/bnb?semesters=4&maxHours=24"

# Plan óptimo con materias del 1° año aprobadas
curl "http://localhost:8080/schedule/bnb?approved=3.4.069&approved=3.4.164&approved=3.4.071&approved=3.1.050&semesters=4&maxHours=24"

# Plan óptimo para 6 cuatrimestres (3 años)
curl "http://localhost:8080/schedule/bnb?approved=3.4.069&approved=3.4.164&approved=3.4.071&semesters=6&maxHours=25"

# Plan óptimo con materias del 2° año aprobadas
curl "http://localhost:8080/schedule/bnb?approved=3.4.074&approved=3.4.207&approved=3.4.075&approved=3.1.052&approved=3.1.053&semesters=3&maxHours=24"

# Plan óptimo para 2 cuatrimestres (1 año)
curl "http://localhost:8080/schedule/bnb?approved=3.4.071&approved=3.4.074&semesters=2&maxHours=20"
```

**Parámetros**:
- `approved`: Lista de materias aprobadas (opcional)
- `semesters`: Número de cuatrimestres a planificar (default: 4)
- `maxHours`: Límite de horas semanales por cuatrimestre (default: 24)

**Respuesta esperada**: Lista de listas, donde cada lista interna es un cuatrimestre:
```json
[
  ["3.4.077", "3.4.208", "3.4.209", "3.1.054"],
  ["3.4.210", "3.4.211", "3.4.212", "3.4.213"],
  ["3.4.082", "3.4.214", "3.1.055"],
  ["3.4.216", "3.4.089", "3.4.217"]
]
```

**Nota**: Este algoritmo puede ser lento para muchos cuatrimestres o muchas materias. Funciona mejor para 3-6 cuatrimestres.

---

## 🔍 Ejemplos de Casos de Uso Reales

### Caso 1: Estudiante de 1° Año

```bash
# 1. Ver qué materias puede cursar ahora
curl "http://localhost:8080/schedule/available"

# 2. Ver el orden recomendado de cursada
curl "http://localhost:8080/graph/toposort"

# 3. Planificar el próximo cuatrimestre (maximizar créditos)
curl "http://localhost:8080/schedule/greedy?maxHours=20&value=credits"
```

### Caso 2: Estudiante de 2° Año (con 1° año aprobado)

```bash
# Materias aprobadas: 1° año completo
APPROVED="approved=3.4.069&approved=3.4.164&approved=2.1.002&approved=3.4.043&approved=3.1.050&approved=3.4.071&approved=3.3.121&approved=3.2.178&approved=3.4.072&approved=3.1.024&approved=3.1.051"

# Ver materias disponibles
curl "http://localhost:8080/schedule/available?${APPROVED}"

# Plan óptimo para 2 cuatrimestres
curl "http://localhost:8080/schedule/bnb?${APPROVED}&semesters=2&maxHours=24"

# Selección greedy para este cuatrimestre
curl "http://localhost:8080/schedule/greedy?${APPROVED}&value=credits&maxHours=20"
```

### Caso 3: Estudiante Avanzado (planificando últimos años)

```bash
# Materias aprobadas hasta 3° año
APPROVED="approved=3.4.074&approved=3.4.207&approved=3.4.075&approved=3.1.052&approved=3.1.053&approved=3.4.077&approved=3.4.208&approved=3.4.209&approved=3.1.054&approved=3.4.210&approved=3.4.211&approved=3.4.212&approved=3.4.213"

# Ver todas las rutas hacia "Desarrollo de Aplicaciones II"
curl "http://localhost:8080/schedule/backtracking?from=3.4.210&to=3.4.218&maxDepth=8"

# Plan óptimo para los últimos 4 cuatrimestres
curl "http://localhost:8080/schedule/bnb?${APPROVED}&semesters=4&maxHours=24"

# Camino más fácil hacia "Proyecto Final"
curl "http://localhost:8080/graph/shortest?from=3.4.210&to=3.4.100&metric=difficulty"
```

### Caso 4: Validación del Plan de Estudios

```bash
# 1. Verificar que no haya ciclos
curl "http://localhost:8080/graph/cycles"

# 2. Ver estructura completa desde una materia inicial
curl "http://localhost:8080/graph/bfs-layers?from=3.4.069"

# 3. Ver orden topológico completo
curl "http://localhost:8080/graph/toposort"
```

---

## 📝 Notas Importantes

1. **Base URL**: Todos los ejemplos asumen que el servidor está corriendo en `http://localhost:8080`

2. **Datos Requeridos**: Asegúrate de haber ejecutado `init-data-ingenieria.sh` antes de probar los algoritmos.

3. **MST (Prim/Kruskal)**: Requiere relaciones RELATED en Neo4j. **Ejecuta `create-related-ingenieria.sh` después de cargar los datos** para crear automáticamente ~57 relaciones RELATED entre materias relacionadas. Sin estas relaciones, MST devolverá una lista vacía.

4. **Parámetros Múltiples**: Para pasar múltiples valores al mismo parámetro (ej: `approved`), repite el parámetro:
   ```bash
   ?approved=3.4.069&approved=3.4.164&approved=3.4.071
   ```

5. **Formato de Respuesta**: La mayoría de endpoints retornan JSON. Puedes usar `jq` para formatear:
   ```bash
   curl "http://localhost:8080/graph/toposort" | jq
   ```

6. **Rendimiento**:
   - **Greedy/DP**: Muy rápidos (milisegundos)
   - **Backtracking**: Puede ser lento para rutas largas (usa `maxDepth`)
   - **Branch & Bound**: Puede ser lento para muchos cuatrimestres (3-6 es óptimo)

---

## 🧪 Verificación Rápida

Para verificar que todo funciona correctamente:

```bash
# 1. Verificar que el servidor está corriendo
curl "http://localhost:8080/ping"

# 2. Listar todas las materias
curl "http://localhost:8080/courses" | head -20

# 3. Verificar una materia específica
curl "http://localhost:8080/courses/3.4.071"

# 4. Probar un algoritmo simple
curl "http://localhost:8080/graph/cycles"

# 5. Probar orden topológico
curl "http://localhost:8080/graph/toposort" | jq '.[:10]'  # Primeras 10 materias

# 6. Crear relaciones RELATED (necesario para MST)
./scripts/create-related-ingenieria.sh  # En Linux/Mac
# O: .\scripts\create-related-ingenieria.ps1  # En Windows

# 7. Probar MST con Prim
curl "http://localhost:8080/graph/mst?algo=prim" | jq

# 8. Probar MST con Kruskal
curl "http://localhost:8080/graph/mst?algo=kruskal" | jq
```

---

**Última actualización**: 2025-01-27
**Plan de Estudios**: Ingeniería en Informática - Plan 1621 (Año 2021)

