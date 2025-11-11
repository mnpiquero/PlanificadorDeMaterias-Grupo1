# API y Algoritmos - Planificador de Materias

## 📚 Índice

1. [Endpoints del API](#endpoints-del-api)
   - [Course Controller](#course-controller)
   - [Relationship Controller](#relationship-controller)
   - [Graph Controller](#graph-controller)
   - [Schedule Controller](#schedule-controller)
2. [Algoritmos Implementados](#algoritmos-implementados)
   - [Algoritmos de Grafos](#algoritmos-de-grafos)
   - [Algoritmos de Optimización](#algoritmos-de-optimización)

---

## 📡 Endpoints del API

### Course Controller

Endpoint base: `/courses`

#### `PUT /courses`
**Descripción**: Crear o actualizar una materia (operación upsert).

**Uso**: Permite crear una nueva materia o actualizar una existente. Útil para cargar datos iniciales sin verificar si ya existen.

**Ejemplo**:
```json
PUT /courses
{
  "code": "MAT101",
  "name": "Matemática I",
  "credits": 6,
  "hours": 8,
  "difficulty": 4,
  "prereqs": []
}
```

**Por qué se usa**: La operación upsert es ideal para scripts de inicialización de datos donde no sabemos si las materias ya existen. Simplifica la carga masiva de información.

---

#### `POST /courses`
**Descripción**: Crear una nueva materia (falla si ya existe).

**Uso**: Garantiza que solo se crean materias nuevas. Si la materia con ese código ya existe, retorna error 409 (Conflict).

**Ejemplo**:
```json
POST /courses
{
  "code": "ALG101",
  "name": "Álgebra I",
  "credits": 4,
  "hours": 6,
  "difficulty": 3,
  "prereqs": []
}
```

**Por qué se usa**: Útil cuando queremos evitar sobrescribir datos existentes accidentalmente. Aporta seguridad en operaciones de creación manual.

---

#### `GET /courses`
**Descripción**: Listar todas las materias del sistema.

**Formato**: Retorna un stream Server-Sent Events (SSE).

**Uso**: Obtener el catálogo completo de materias para visualización en el frontend o análisis.

**Ejemplo**:
```
GET /courses
```

**Por qué SSE**: Para grandes volúmenes de datos, SSE permite comenzar a procesar resultados antes de que finalice la consulta completa, mejorando la experiencia del usuario.

---

#### `GET /courses/{code}`
**Descripción**: Obtener una materia específica por su código.

**Uso**: Consultar detalles de una materia individual.

**Ejemplo**:
```
GET /courses/MAT101
```

**Por qué se usa**: Acceso directo y rápido a información de una materia específica, útil para mostrar detalles en el frontend.

---

#### `DELETE /courses/{code}`
**Descripción**: Eliminar una materia del sistema.

**Uso**: Remover materias obsoletas o incorrectas del catálogo.

**Ejemplo**:
```
DELETE /courses/MAT101
```

**Retorno**: 204 No Content si es exitoso, 404 si no existe.

---

#### `PATCH /courses/{code}`
**Descripción**: Actualización parcial de una materia.

**Uso**: Modificar solo algunos campos sin tener que enviar toda la entidad.

**Ejemplo**:
```json
PATCH /courses/MAT101
{
  "difficulty": 5
}
```

**Por qué se usa**: Más eficiente que PUT cuando solo necesitamos cambiar un atributo (ej: ajustar dificultad, actualizar horas).

---

#### `GET /courses/search/by-name?name={nombre}`
**Descripción**: Búsqueda de materias por nombre (coincidencia parcial, case-insensitive).

**Uso**: Buscar materias cuando el usuario solo conoce parte del nombre.

**Ejemplo**:
```
GET /courses/search/by-name?name=Matemática
```

**Por qué se usa**: Búsqueda intuitiva y flexible para usuarios que no conocen los códigos exactos de las materias.

---

#### `GET /courses/search/advanced`
**Descripción**: Búsqueda avanzada con múltiples criterios simultáneos.

**Parámetros**:
- `nameContains`: Texto en el nombre
- `minCredits` / `maxCredits`: Rango de créditos
- `minDifficulty` / `maxDifficulty`: Rango de dificultad (1-5)
- `minHours` / `maxHours`: Rango de horas semanales

**Uso**: Filtrar materias según múltiples condiciones para encontrar opciones que cumplan requisitos específicos.

**Ejemplo**:
```
GET /courses/search/advanced?minCredits=4&maxCredits=6&maxDifficulty=3
```

**Por qué se usa**: Permite encontrar materias que cumplan criterios complejos (ej: "materias de 4-6 créditos, dificultad baja, que no excedan 8 horas").

---

#### `GET /courses/{code}/exists`
**Descripción**: Verificar si una materia existe.

**Uso**: Validación rápida de existencia antes de crear relaciones o realizar otras operaciones.

**Ejemplo**:
```
GET /courses/MAT101/exists
```

**Retorno**: `true` o `false`.

**Por qué se usa**: Evita hacer queries más costosas cuando solo necesitamos saber si algo existe.

---

### Relationship Controller

Endpoint base: `/relationships`

#### `POST /relationships`
**Descripción**: Crear una relación RELATED entre dos materias con similaridad especificada.

**Uso**: Establecer relaciones de similaridad cuando conocemos el valor exacto.

**Ejemplo**:
```json
POST /relationships
{
  "fromCode": "MAT101",
  "toCode": "FIS101",
  "similarity": 0.8
}
```

**Por qué se usa**: Permite definir manualmente relaciones basadas en conocimiento experto o análisis previo.

---

#### `POST /relationships/auto`
**Descripción**: Crear relación RELATED con similaridad calculada automáticamente.

**Uso**: Calcular similaridad basándose en atributos de las materias (créditos, horas, dificultad).

**Ejemplo**:
```json
POST /relationships/auto
{
  "fromCode": "MAT101",
  "toCode": "ALG101"
}
```

**Cálculo de similaridad**: Basado en diferencias normalizadas entre créditos, horas y dificultad.

**Por qué se usa**: Útil cuando queremos relaciones objetivas basadas en métricas cuantificables, sin sesgo subjetivo.

---

#### `GET /relationships`
**Descripción**: Listar todas las relaciones RELATED del sistema.

**Uso**: Obtener una vista completa de las relaciones de similaridad para análisis o visualización.

**Ejemplo**:
```
GET /relationships
```

---

#### `GET /relationships/{code}`
**Descripción**: Obtener todas las materias relacionadas con una materia específica.

**Uso**: Ver qué materias son similares a una materia dada.

**Ejemplo**:
```
GET /relationships/MAT101
```

**Por qué se usa**: Útil para recomendar materias relacionadas o entender conexiones conceptuales.

---

#### `PATCH /relationships/{fromCode}/{toCode}`
**Descripción**: Actualizar el valor de similaridad de una relación existente.

**Uso**: Refinar valores de similaridad después de análisis adicional.

**Ejemplo**:
```json
PATCH /relationships/MAT101/FIS101
{
  "similarity": 0.9
}
```

---

#### `DELETE /relationships/{fromCode}/{toCode}`
**Descripción**: Eliminar una relación RELATED entre dos materias.

**Uso**: Remover relaciones incorrectas o que ya no son relevantes.

**Ejemplo**:
```
DELETE /relationships/MAT101/FIS101
```

---

### Graph Controller

Endpoint base: `/graph`

#### `GET /graph/dfs?from={code}`
**Descripción**: Recorrido en Profundidad (Depth-First Search) desde una materia.

**Algoritmo**: DFS recursivo estándar.

**Complejidad**: O(V + E) donde V = nodos, E = aristas.

**Uso**: Explorar todas las materias alcanzables desde una materia inicial siguiendo prerequisitos.

**Ejemplo**:
```
GET /graph/dfs?from=MAT101
```

**Retorno**: Lista de códigos de materias en orden de visita.

**Por qué DFS**: 
- **Exploración exhaustiva**: Visita todas las materias alcanzables desde el punto de partida.
- **Estructura simple**: Implementación sencilla y eficiente.
- **Ordenamiento natural**: Visita prerequisitos antes que materias que los requieren.
- **Uso en visualización**: Útil para generar grafos o árboles de dependencias.

---

#### `GET /graph/bfs-layers?from={code}`
**Descripción**: Recorrido por Capas (Breadth-First Search) desde una materia.

**Algoritmo**: BFS estándar agrupando por niveles/capas.

**Complejidad**: O(V + E).

**Uso**: Organizar materias por "distancia" de prerequisitos (nivel 0 = la materia inicial, nivel 1 = sus prerequisitos directos, etc.).

**Ejemplo**:
```
GET /graph/bfs-layers?from=MAT101
```

**Retorno**: Lista de listas, cada lista es una capa/nivel.

**Por qué BFS**:
- **Organización por niveles**: Ideal para mostrar materias agrupadas por "profundidad" en el plan de estudios.
- **Camino más corto**: Encontrar la cadena más corta de prerequisitos.
- **Visualización estructurada**: Facilita mostrar el grafo en niveles horizontales.
- **Análisis de distancia**: Útil para entender cuántos prerequisitos separan una materia de otra.

---

#### `GET /graph/toposort?approved={code1}&approved={code2}`
**Descripción**: Ordenamiento Topológico (Algoritmo de Kahn).

**Algoritmo**: Kahn's Algorithm con consideración de materias aprobadas.

**Complejidad**: O(V + E).

**Uso**: Determinar un orden válido de cursada respetando prerequisitos.

**Ejemplo**:
```
GET /graph/toposort?approved=MAT101&approved=FIS101
```

**Características**:
- Considera materias ya aprobadas (no las incluye en el orden).
- Calcula grado de entrada efectivo (indegree) excluyendo prerequisitos aprobados.
- Retorna solo materias cursables.

**Por qué Toposort (Kahn)**:
- **Ordenamiento válido**: Garantiza que todas las materias se puedan cursar en el orden retornado.
- **Respeta prerequisitos**: Imposible cursar una materia antes que sus prerequisitos.
- **Eficiente**: O(V+E) es óptimo para grafos acíclicos.
- **Considera progreso**: Al incluir materias aprobadas, adapta el orden al estado actual del estudiante.
- **Planificación**: Base para generar planes de cursada cuatrimestral.

**Caso de uso**: Un estudiante quiere saber "¿qué materias puedo cursar este cuatrimestre?" - el orden topológico le da todas las opciones en orden lógico.

---

#### `GET /graph/cycles`
**Descripción**: Detección de ciclos en el grafo de prerequisitos.

**Algoritmo**: Query Cypher que detecta ciclos en Neo4j.

**Complejidad**: O(V + E).

**Uso**: Validar que el plan de estudios sea coherente (no tenga ciclos de dependencias circulares).

**Ejemplo**:
```
GET /graph/cycles
```

**Retorno**: `{"hasCycle": true/false}`

**Por qué es importante**:
- **Validación de datos**: Detecta errores en la configuración del plan de estudios.
- **Consistencia**: Un ciclo significa dependencias imposibles (A requiere B, B requiere A).
- **Prevención de errores**: Evita que los algoritmos de planificación fallen.

**Caso de uso**: Antes de generar planes, validar que no existan ciclos que hagan imposible la cursada.

---

#### `GET /graph/shortest?from={code1}&to={code2}&metric={metric}`
**Descripción**: Camino más corto entre dos materias usando Dijkstra.

**Algoritmo**: Dijkstra con pesos configurables.

**Complejidad**: O((V + E) log V) con Priority Queue.

**Parámetros**:
- `from`: Materia origen
- `to`: Materia destino
- `metric`: `difficulty` (default), `hours`, o `credits`

**Uso**: Encontrar el mejor camino entre dos materias según diferentes métricas.

**Ejemplo**:
```
GET /graph/shortest?from=MAT101&to=MAT301&metric=difficulty
```

**Métricas de peso**:
- `difficulty`: Minimiza dificultad total del camino.
- `hours`: Minimiza horas semanales totales.
- `credits`: Maximiza créditos (usando inverso de créditos como peso).

**Por qué Dijkstra**:
- **Camino óptimo**: Garantiza encontrar el camino más corto (según la métrica).
- **Pesos variables**: Permite optimizar por diferentes objetivos (dificultad, tiempo, valor).
- **Algoritmo estándar**: Bien conocido y probado para caminos más cortos.
- **Eficiencia**: O((V+E)log V) es razonable para grafos de tamaño moderado.

**Casos de uso**:
1. "¿Cuál es el camino más fácil de MAT101 a MAT301?" → `metric=difficulty`
2. "¿Cuál es el camino que requiere menos horas?" → `metric=hours`
3. "¿Cómo llegar a MAT301 maximizando créditos?" → `metric=credits`

---

#### `GET /graph/mst?algo={prim|kruskal}`
**Descripción**: Árbol de Expansión Mínima (MST) sobre relaciones RELATED.

**Algoritmos**: Prim o Kruskal (seleccionable).

**Complejidad**: O(E log V).

**Uso**: Encontrar el conjunto mínimo de relaciones que conecten todas las materias relacionadas.

**Ejemplo**:
```
GET /graph/mst?algo=prim
```

**Funcionamiento**:
- Opera sobre el subgrafo no dirigido de relaciones RELATED.
- Peso = 1/similaridad (mayor similaridad = menor peso).
- Retorna el árbol que conecta todas las materias con menor costo total.

**Por qué MST**:
- **Conectividad óptima**: Conecta todas las materias relacionadas con el menor "costo" total.
- **Relaciones clave**: Identifica las relaciones de similaridad más importantes.
- **Agrupación**: Útil para agrupar materias relacionadas conceptualmente.
- **Optimización**: Aplicación clásica de grafos para encontrar estructuras eficientes.

**Por qué Prim vs Kruskal**:
- **Prim**: Mejor para grafos densos. O(E log V) con Fibonacci heap, O(V²) con lista.
- **Kruskal**: Mejor para grafos dispersos. Implementación más simple.
- **Ambos**: Dan el mismo resultado, elegir según características del grafo.

**Caso de uso**: Encontrar el "esqueleto" de materias más similares que forman un grupo coherente.

---

### Schedule Controller

Endpoint base: `/schedule`

#### `GET /schedule/available?approved={code1}&approved={code2}`
**Descripción**: Listar materias disponibles para cursar según materias aprobadas.

**Algoritmo**: Query Cypher con `ALL` que verifica que todos los prerequisitos estén en el conjunto de aprobadas.

**Complejidad**: O(V + E) en el peor caso.

**Uso**: Determinar qué materias puede cursar un estudiante ahora.

**Ejemplo**:
```
GET /schedule/available?approved=MAT101&approved=FIS101
```

**Por qué se usa**:
- **Base para planificación**: Primer paso para todos los algoritmos de planificación.
- **Validación eficiente**: Query Cypher optimizada en Neo4j.
- **Filtrado automático**: Excluye materias con prerequisitos faltantes.

---

#### `GET /schedule/greedy?approved={codes}&value={credits|difficulty|hours}&maxHours={n}`
**Descripción**: Selección greedy de materias para un cuatrimestre.

**Algoritmo**: Algoritmo Greedy estándar.

**Complejidad**: O(n log n) por el ordenamiento, donde n = materias disponibles.

**Estrategias de valor**:
- `credits` (default): Maximiza créditos totales.
- `difficulty`: Minimiza dificultad promedio (selecciona materias más fáciles).
- `hours`: Minimiza horas semanales.

**Uso**: Generar rápidamente un plan de cuatrimestre que optimice un criterio.

**Ejemplo**:
```
GET /schedule/greedy?approved=MAT101&value=credits&maxHours=20
```

**Funcionamiento**:
1. Ordena materias disponibles por valor/horas (ratio).
2. Selecciona materias en orden hasta alcanzar `maxHours`.

**Por qué Greedy**:
- **Rapidez**: O(n log n) es muy rápido, apropiado para tiempo real.
- **Simplicidad**: Fácil de entender e implementar.
- **Buen resultado en práctica**: Aunque no garantiza óptimo global, suele dar buenas soluciones.
- **Heurística efectiva**: Para problemas de selección con restricciones, greedy funciona bien.

**Limitaciones**:
- No garantiza solución óptima (puede quedar espacio sin usar).
- No considera efectos a largo plazo.

**Casos de uso**:
1. "Quiero maximizar créditos este cuatrimestre" → `value=credits`
2. "Quiero materias fáciles" → `value=difficulty`
3. "Tengo poco tiempo disponible" → `value=hours`

---

#### `GET /schedule/dp?approved={codes}&value={credits|difficulty|hours}&maxHours={n}`
**Descripción**: Selección óptima usando Programación Dinámica (Knapsack).

**Algoritmo**: Knapsack 0/1 con Programación Dinámica.

**Complejidad**: O(n × capacidad) donde n = materias, capacidad = maxHours.

**Uso**: Encontrar la selección óptima de materias que maximice un valor respetando límite de horas.

**Ejemplo**:
```
GET /schedule/dp?approved=MAT101&value=credits&maxHours=20
```

**Funcionamiento**:
- Modela como problema de mochila: materias = items, horas = peso, créditos = valor.
- Construye tabla DP para encontrar combinación óptima.
- Reconstruye solución seleccionada.

**Por qué Programación Dinámica (Knapsack)**:
- **Óptimo garantizado**: Encuentra la solución óptima (a diferencia de greedy).
- **Considera todas las combinaciones**: Evalúa todas las posibilidades dentro de la capacidad.
- **Eficiente para este caso**: O(n × maxHours) es manejable si maxHours ≤ 40.
- **Base sólida**: Algoritmo clásico y probado para optimización con restricciones.

**Ventajas sobre Greedy**:
- Solución óptima garantizada.
- Mejor uso del espacio disponible.
- Considera compensaciones (ej: dos materias pequeñas vs una grande).

**Desventajas**:
- Más lento que greedy (pero aún aceptable).
- Más complejo de implementar.

**Caso de uso**: Cuando necesitas la mejor combinación posible, no solo una "buena" solución.

---

#### `GET /schedule/backtracking?from={code1}&to={code2}&maxDepth={n}`
**Descripción**: Encontrar todas las rutas posibles entre dos materias usando Backtracking.

**Algoritmo**: Backtracking recursivo con límite de profundidad.

**Complejidad**: Exponencial en el peor caso, pero acotado por `maxDepth`.

**Uso**: Explorar todas las alternativas de cursada entre dos materias.

**Ejemplo**:
```
GET /schedule/backtracking?from=MAT101&to=MAT301&maxDepth=5
```

**Retorno**: Lista de listas, cada lista es una ruta posible.

**Funcionamiento**:
- Explora recursivamente el grafo.
- Registra cada camino válido encontrado.
- Poda cuando alcanza `maxDepth` o encuentra un ciclo.

**Por qué Backtracking**:
- **Exploración exhaustiva**: Encuentra todas las rutas posibles (dentro del límite de profundidad).
- **Alternativas completas**: Útil cuando hay múltiples caminos y queremos evaluar todos.
- **Flexibilidad**: Fácil de modificar para agregar restricciones adicionales.
- **Análisis comparativo**: Permite comparar diferentes trayectorias académicas.

**Limitaciones**:
- Complejidad exponencial sin límites.
- Requiere `maxDepth` para evitar explosión combinatoria.

**Casos de uso**:
1. "¿Cuántas formas hay de llegar de MAT101 a MAT301?"
2. "Quiero ver todas mis opciones de cursada antes de decidir"
3. Análisis de planes alternativos

**Por qué no BFS**: BFS encuentra el camino más corto, pero no todos los caminos. Backtracking encuentra todas las alternativas.

---

#### `GET /schedule/bnb?approved={codes}&semesters={n}&maxHours={m}`
**Descripción**: Planificación óptima a N cuatrimestres usando Branch & Bound.

**Algoritmo**: Branch & Bound con poda heurística.

**Complejidad**: Exponencial en el peor caso, pero con poda agresiva puede ser manejable.

**Uso**: Generar el mejor plan de cursada para múltiples cuatrimestres.

**Ejemplo**:
```
GET /schedule/bnb?approved=MAT101&semesters=4&maxHours=24
```

**Retorno**: Lista de listas, donde cada lista interna es un cuatrimestre.

**Objetivo**: Maximizar créditos totales acumulados en todos los cuatrimestres.

**Funcionamiento**:
1. Genera ramas de posibles planes por cuatrimestre.
2. Calcula cota superior (upper bound) para cada rama.
3. Poda ramas que no pueden mejorar la mejor solución actual.
4. Explora ramas prometedoras primero.

**Técnicas de poda**:
- **Upper bound**: Estima el máximo créditos posible en cuatrimestres restantes.
- **Lower bound**: Mejor solución encontrada hasta ahora.
- **Heurística**: Ordena materias por ratio créditos/horas para explorar mejores opciones primero.

**Por qué Branch & Bound**:
- **Óptimo global**: Encuentra la mejor solución a largo plazo (no solo por cuatrimestre).
- **Considera efectos temporales**: Optimiza para múltiples períodos simultáneamente.
- **Poda eficiente**: Reduce significativamente el espacio de búsqueda con buenas heurísticas.
- **Flexibilidad**: Puede adaptarse para otros objetivos (ej: minimizar tiempo total de carrera).

**Ventajas sobre Greedy iterativo**:
- Optimiza a largo plazo, no solo localmente.
- Considera que materias disponibles cambian por cuatrimestre.
- Solución globalmente óptima.

**Limitaciones**:
- Puede ser lento para muchos cuatrimestres o muchas materias.
- Complejidad exponencial en el peor caso.

**Caso de uso**: 
- "Quiero el mejor plan para los próximos 4 cuatrimestres que maximice mis créditos totales"
- Planificación estratégica a mediano plazo

**Comparación con otros algoritmos**:

| Algoritmo | Alcance | Óptimo | Complejidad | Uso |
|-----------|---------|--------|-------------|-----|
| Greedy | 1 cuatrimestre | Local | O(n log n) | Rápido, buena heurística |
| DP Knapsack | 1 cuatrimestre | Óptimo | O(n × cap) | Óptimo para un período |
| Backtracking | Ruta A→B | Todas las rutas | Exponencial | Exploración completa |
| Branch & Bound | N cuatrimestres | Óptimo global | Exponencial (con poda) | Planificación estratégica |

---

## 🧮 Resumen de Algoritmos y Casos de Uso

### Algoritmos de Grafos

| Algoritmo | Complejidad | Caso de Uso Principal | Por Qué Este Algoritmo |
|-----------|-------------|------------------------|------------------------|
| **DFS** | O(V + E) | Explorar materias alcanzables | Simple, exhaustivo, natural para árboles |
| **BFS** | O(V + E) | Organizar por niveles | Camino más corto, visualización estructurada |
| **Toposort (Kahn)** | O(V + E) | Orden de cursada válido | Garantiza orden respetando prerequisitos |
| **Detección de Ciclos** | O(V + E) | Validar consistencia del plan | Detecta errores en configuración |
| **Dijkstra** | O((V+E)log V) | Camino óptimo entre materias | Óptimo para caminos más cortos con pesos |
| **MST (Prim/Kruskal)** | O(E log V) | Relaciones mínimas entre materias | Conectividad óptima en grafos no dirigidos |

### Algoritmos de Optimización

| Algoritmo | Complejidad | Caso de Uso Principal | Por Qué Este Algoritmo |
|-----------|-------------|------------------------|------------------------|
| **Greedy** | O(n log n) | Selección rápida 1 cuatrimestre | Rápido, bueno en práctica, fácil de entender |
| **DP Knapsack** | O(n × cap) | Selección óptima 1 cuatrimestre | Óptimo garantizado, eficiente para restricciones |
| **Backtracking** | Exponencial (acotado) | Todas las rutas posibles | Exhaustivo, encuentra todas las alternativas |
| **Branch & Bound** | Exponencial (con poda) | Plan óptimo N cuatrimestres | Óptimo global, considera efectos temporales |

---

## 🎯 Recomendaciones de Uso

### Para Estudiantes

1. **Verificar ciclo de cursada**: Usar `/graph/toposort` para ver el orden recomendado.
2. **Planificar un cuatrimestre**: 
   - Rápido: `/schedule/greedy`
   - Óptimo: `/schedule/dp`
3. **Planificar carrera completa**: `/schedule/bnb` con semesters adecuado.
4. **Explorar alternativas**: `/schedule/backtracking` para ver todas las opciones.

### Para Administradores

1. **Validar plan de estudios**: `/graph/cycles` para detectar inconsistencias.
2. **Analizar estructura**: `/graph/bfs-layers` para entender organización del plan.
3. **Relaciones conceptuales**: `/graph/mst` para identificar grupos de materias similares.

---

## 📊 Flujo de Decisiones

```
¿Necesito planificar?
│
├─ ¿Para cuántos períodos?
│  ├─ 1 cuatrimestre
│  │  ├─ ¿Necesito óptimo?
│  │  │  ├─ SÍ → /schedule/dp (Knapsack)
│  │  │  └─ NO → /schedule/greedy (Rápido)
│  │  │
│  │  └─ ¿Todas las opciones?
│  │     └─ /schedule/backtracking
│  │
│  └─ Múltiples cuatrimestres
│     └─ /schedule/bnb (Branch & Bound)
│
└─ ¿Necesito analizar estructura?
   ├─ Orden de cursada → /graph/toposort
   ├─ Validar consistencia → /graph/cycles
   ├─ Explorar alcanzables → /graph/dfs
   ├─ Organizar por niveles → /graph/bfs-layers
   ├─ Camino óptimo → /graph/shortest
   └─ Relaciones clave → /graph/mst
```

---

## 📝 Notas Técnicas

### Complejidades Asintóticas

- **V**: Número de vértices (materias)
- **E**: Número de aristas (relaciones de prerequisitos)
- **n**: Número de materias disponibles para seleccionar
- **cap**: Capacidad (maxHours, típicamente ≤ 40)

### Consideraciones de Implementación

1. **Reactividad**: Todos los servicios usan Reactor (Mono/Flux) para manejo asíncrono.
2. **Queries Cypher**: Optimizadas en Neo4j para operaciones de grafo.
3. **Estructuras en memoria**: Grafos se construyen en memoria para algoritmos complejos.
4. **Poda en B&B**: Heurísticas para mantener el rendimiento aceptable.

### Límites Prácticos

- **Greedy/DP**: Manejan fácilmente 100+ materias.
- **Backtracking**: Requiere `maxDepth` para evitar explosión combinatoria.
- **Branch & Bound**: Funciona bien para 3-6 cuatrimestres, puede ser lento para 10+.

---

**Última actualización**: 2025-01-27

