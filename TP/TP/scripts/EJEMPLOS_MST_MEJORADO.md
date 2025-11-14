# Ejemplos de MST Mejorado

Este documento muestra cómo usar las mejoras implementadas en los algoritmos MST (Prim y Kruskal).

## 🆕 Nuevas Funcionalidades

### 1. **Prim con Nodo Inicial Personalizado**
Ahora puedes elegir desde qué materia comenzar el algoritmo de Prim.

### 2. **MST Forest (Bosque)**
Encuentra MST para cada componente conexa, identificando áreas temáticas automáticamente.

### 3. **Estadísticas de Conectividad**
Obtén información sobre la estructura del grafo RELATED.

---

## 📖 Ejemplos de Uso

### MST Tradicional (Comportamiento Existente)

```bash
# Prim desde primer nodo alfabético (comportamiento por defecto)
curl "http://localhost:8080/graph/mst?algo=prim"

# Kruskal (sin cambios)
curl "http://localhost:8080/graph/mst?algo=kruskal"
```

---

### 🆕 MST con Nodo Inicial Personalizado

Ahora puedes especificar desde qué materia comenzar el algoritmo de Prim para explorar diferentes áreas temáticas:

#### Ejemplo 1: Explorar área de Programación

```bash
# MST desde "Programación I"
curl "http://localhost:8080/graph/mst?algo=prim&startNode=3.4.071"
```

**Resultado esperado:** Árbol que conecta materias de programación y desarrollo.

#### Ejemplo 2: Explorar área de Matemática

```bash
# MST desde "Cálculo I"
curl "http://localhost:8080/graph/mst?algo=prim&startNode=3.1.053"
```

**Resultado esperado:** Árbol que conecta materias de matemática y física.

#### Ejemplo 3: Explorar área de Datos/IA

```bash
# MST desde "Ingeniería de Datos I"
curl "http://localhost:8080/graph/mst?algo=prim&startNode=3.4.209"
```

**Resultado esperado:** Árbol que conecta materias de datos, estadística e IA.

#### Ejemplo 4: Explorar área de Redes

```bash
# MST desde "Sistemas Operativos"
curl "http://localhost:8080/graph/mst?algo=prim&startNode=3.4.075"
```

**Resultado esperado:** Árbol que conecta materias de redes, SO y telecomunicaciones.

---

### 🆕 MST Forest (Bosque de Árboles)

Este endpoint encuentra **todas las componentes conexas** y retorna un MST para cada una, identificando automáticamente las áreas temáticas del plan de estudios.

```bash
# Obtener MST Forest
curl "http://localhost:8080/graph/mst-forest" | jq
```

**Respuesta esperada:**

```json
[
  {
    "componentId": "component_1",
    "nodes": [
      "3.4.069", "3.4.071", "3.4.074", "3.4.077", "3.4.208", "3.4.210",
      "3.4.214", "3.4.216", "3.4.218", "3.4.082", "3.4.098"
    ],
    "edges": [
      {"from": "3.4.069", "to": "3.4.071", "weight": 100},
      {"from": "3.4.071", "to": "3.4.074", "weight": 107},
      ...
    ],
    "totalWeight": 1234
  },
  {
    "componentId": "component_2",
    "nodes": [
      "3.1.050", "3.1.051", "3.1.052", "3.1.053", "3.1.054", "3.1.049", "3.1.056"
    ],
    "edges": [
      {"from": "3.1.050", "to": "3.1.051", "weight": 100},
      {"from": "3.1.053", "to": "3.1.054", "weight": 107},
      ...
    ],
    "totalWeight": 567
  },
  {
    "componentId": "component_3",
    "nodes": [
      "3.4.209", "3.4.213", "3.4.217", "3.4.096", "3.1.024"
    ],
    "edges": [
      {"from": "3.4.209", "to": "3.4.213", "weight": 100},
      ...
    ],
    "totalWeight": 345
  }
]
```

#### Interpretación del Forest

Cada componente representa un **área temática** del plan de estudios:

- **Component 1**: Programación y Desarrollo de Software
- **Component 2**: Matemática y Física
- **Component 3**: Datos e Inteligencia Artificial
- **Component 4**: Redes y Telecomunicaciones
- Etc.

#### Contar componentes

```bash
# Cantidad de áreas temáticas identificadas
curl -s "http://localhost:8080/graph/mst-forest" | jq '. | length'
```

#### Ver solo la componente más grande

```bash
# Componente con más materias (área principal)
curl -s "http://localhost:8080/graph/mst-forest" | jq 'max_by(.nodes | length)'
```

#### Ver resumen de todas las componentes

```bash
# Resumen: ID, cantidad de materias, peso total
curl -s "http://localhost:8080/graph/mst-forest" | jq '[.[] | {
  id: .componentId,
  materias: (.nodes | length),
  aristas: (.edges | length),
  peso_total: .totalWeight
}]'
```

---

### 🆕 Estadísticas de Conectividad

Este endpoint proporciona información sobre la estructura del grafo RELATED.

```bash
# Obtener estadísticas
curl "http://localhost:8080/graph/connectivity-stats" | jq
```

**Respuesta esperada:**

```json
{
  "totalNodes": 46,
  "connectedNodes": 43,
  "disconnectedNodes": 3,
  "disconnectedNodesList": [
    "2.1.002",
    "3.2.178",
    "3.3.121"
  ],
  "totalComponents": 4,
  "coveragePercentage": 93.48
}
```

#### Interpretación

- **totalNodes**: Total de materias en el plan
- **connectedNodes**: Materias con al menos una relación RELATED
- **disconnectedNodes**: Materias sin relaciones (aisladas)
- **disconnectedNodesList**: Códigos de materias aisladas
- **totalComponents**: Cantidad de componentes conexas + nodos aislados
- **coveragePercentage**: % de materias conectadas

---

## 🎯 Casos de Uso Prácticos

### Caso 1: Identificar Áreas de Especialización

```bash
# 1. Obtener todas las componentes temáticas
curl -s "http://localhost:8080/graph/mst-forest" | jq '[.[] | {
  componente: .componentId,
  cantidad_materias: (.nodes | length),
  materias: .nodes
}]' > areas_tematicas.json

# 2. Ver el área más grande (probablemente programación/desarrollo)
curl -s "http://localhost:8080/graph/mst-forest" | jq 'max_by(.nodes | length) | .nodes'
```

### Caso 2: Detectar Materias Aisladas

```bash
# Materias que necesitan más relaciones RELATED
curl -s "http://localhost:8080/graph/connectivity-stats" | jq '.disconnectedNodesList[]'
```

**Acción recomendada:** Crear relaciones RELATED para estas materias usando `/relationships/auto`.

### Caso 3: Explorar Secuencias de Aprendizaje

```bash
# Comenzar desde una materia específica y ver conexiones similares
curl -s "http://localhost:8080/graph/mst?algo=prim&startNode=3.4.071" | jq

# Esto muestra el "camino de similitud" desde esa materia
```

### Caso 4: Comparar Áreas por Coherencia

```bash
# Áreas más coherentes (menor peso total = mayor similitud promedio)
curl -s "http://localhost:8080/graph/mst-forest" | jq '[.[] | {
  area: .componentId,
  coherencia: (.totalWeight / ((.edges | length) + 1))
}] | sort_by(.coherencia)'
```

---

## 📊 Análisis del Plan de Estudios

### Verificar Cobertura

```bash
# ¿Qué % del plan tiene relaciones de similitud?
curl -s "http://localhost:8080/graph/connectivity-stats" | jq '.coveragePercentage'
```

**Meta recomendada:** >90% de cobertura

### Detectar Componentes Pequeñas

```bash
# Componentes con pocas materias (candidatas para fusión/revisión)
curl -s "http://localhost:8080/graph/mst-forest" | jq '[.[] | select((.nodes | length) < 5)]'
```

### Visualizar Estructura

```bash
# Exportar estructura completa para visualización
curl -s "http://localhost:8080/graph/mst-forest" > mst_forest.json
curl -s "http://localhost:8080/graph/connectivity-stats" > stats.json
```

---

## 🔧 Troubleshooting

### Prim devuelve pocas aristas

**Problema:** Prim desde el nodo por defecto encuentra solo 2-3 aristas.

**Solución:** Especifica un `startNode` que esté en una componente más grande:

```bash
# Malo (nodo aislado o componente pequeña)
curl "http://localhost:8080/graph/mst?algo=prim"

# Bueno (nodo en componente grande)
curl "http://localhost:8080/graph/mst?algo=prim&startNode=3.4.071"
```

### startNode no existe

**Problema:** El nodo especificado no tiene relaciones RELATED.

**Respuesta del sistema:** Log de advertencia y uso del primer nodo alfabético.

**Verificar nodos válidos:**

```bash
# Ver qué materias tienen relaciones RELATED
curl "http://localhost:8080/relationships" | jq '[.[] | .from] | unique'
```

### Diferencia entre Prim y Forest

**Prim (`/graph/mst`):**
- Encuentra **UNA** componente conexa
- Empieza desde un nodo específico o el primero alfabéticamente
- Útil para explorar un área temática

**Forest (`/graph/mst-forest`):**
- Encuentra **TODAS** las componentes conexas
- Identifica automáticamente todas las áreas temáticas
- Útil para análisis global del plan

---

## 📝 Notas Técnicas

### Complejidad

- **Prim**: O(V² + E) por componente
- **Forest**: O(V² + E) total (ejecuta Prim por cada componente)
- **Stats**: O(V + E)

### Compatibilidad

✅ Los endpoints existentes siguen funcionando sin cambios
✅ Backward compatible al 100%
✅ `startNode` es opcional

### Limitaciones Conocidas

1. **Prim solo encuentra una componente:** Por diseño. Usa Forest para encontrar todas.
2. **Stats aproxima cantidad de componentes:** Para exactitud absoluta, cuenta las componentes del Forest.
3. **Grafo debe tener relaciones RELATED:** Sin relaciones, todos los endpoints devuelven datos vacíos o mínimos.

---

## 🎓 Interpretación Académica

### MST con startNode
"¿Qué materias están relacionadas temáticamente con X?"

### MST Forest
"¿Cuáles son las áreas de especialización del plan de estudios?"

### Connectivity Stats
"¿Qué tan cohesivo es el plan? ¿Hay materias aisladas?"

---

**Última actualización:** 2025-01-14  
**Compatible con:** Plan 1621 - Ingeniería en Informática

