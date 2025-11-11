# Planificador de Materias - Documentación del Proyecto

> **✨ ACTUALIZACIÓN**: Se ha implementado el CRUD completo y la gestión de relaciones RELATED. Ver [novedades](#-novedades-implementadas).

## 📋 Índice
1. [Novedades Implementadas](#-novedades-implementadas)
2. [Objetivos del Proyecto](#objetivos-del-proyecto)
3. [Estructura del Proyecto](#estructura-del-proyecto)
4. [Tecnologías Utilizadas](#tecnologías-utilizadas)
5. [Arquitectura y Funcionamiento](#arquitectura-y-funcionamiento)
6. [Algoritmos Implementados](#algoritmos-implementados)
7. [Configuración y Requisitos](#configuración-y-requisitos)
8. [Guía de Uso - Endpoints y cURLs](#guía-de-uso---endpoints-y-curls)
9. [Qué Falta por Implementar](#qué-falta-por-implementar)

---

## ✨ Novedades Implementadas

### CRUD Completo para Materias
El sistema ahora cuenta con operaciones CRUD completas:

- ✅ **POST** `/courses` - Crear nueva materia (falla si existe)
- ✅ **GET** `/courses` - Listar todas las materias
- ✅ **GET** `/courses/{code}` - Obtener materia por código
- ✅ **PUT** `/courses` - Crear o actualizar materia (upsert)
- ✅ **PATCH** `/courses/{code}` - Actualización parcial
- ✅ **DELETE** `/courses/{code}` - Eliminar materia
- ✅ **GET** `/courses/search/by-name` - Buscar por nombre
- ✅ **GET** `/courses/search/advanced` - Búsqueda avanzada multi-criterio
- ✅ **GET** `/courses/{code}/exists` - Verificar existencia

**Validaciones implementadas:**
- Código y nombre obligatorios
- Créditos: 0-12
- Horas semanales: 0-40
- Dificultad: 1-5
- Manejo de errores HTTP apropiado (400, 404, 409)

### Gestión Completa de Relaciones RELATED
Nuevo controller `RelationshipController` para gestionar relaciones de similaridad:

- ✅ **POST** `/relationships` - Crear relación RELATED
- ✅ **POST** `/relationships/auto` - Crear con similaridad automática
- ✅ **GET** `/relationships` - Listar todas las relaciones
- ✅ **GET** `/relationships/{code}` - Obtener relaciones de una materia
- ✅ **PATCH** `/relationships/{from}/{to}` - Actualizar similaridad
- ✅ **DELETE** `/relationships/{from}/{to}` - Eliminar relación

**Características destacadas:**
- Cálculo automático de similaridad basado en atributos (créditos, horas, dificultad)
- Validación de similaridad en rango [0.0, 1.0]
- Prevención de auto-relaciones
- Verificación de existencia de materias antes de crear relaciones

### Nuevos Componentes
- `RelationshipService`: Lógica de negocio para relaciones
- `RelationshipController`: Endpoints REST para relaciones
- `RelatedRelationshipDTO`: DTO para crear/actualizar relaciones
- `CoursePatchDTO`: DTO para actualizaciones parciales
- `CourseSearchCriteria`: Criterios de búsqueda avanzada

---

## 🎯 Objetivos del Proyecto

El **Planificador de Materias** es una aplicación backend desarrollada como trabajo práctico para demostrar la implementación de diversos algoritmos de teoría de grafos y técnicas de optimización aplicados a un problema real: **la planificación académica universitaria**.

### Objetivos principales:
- **Modelar el plan de estudios** como un grafo dirigido donde:
  - Los **nodos** representan materias/cursos
  - Las **aristas** representan relaciones de prerequisitos (REQUIRES)
- **Implementar algoritmos clásicos de grafos**:
  - DFS (Depth-First Search)
  - BFS (Breadth-First Search)
  - Ordenamiento Topológico (Algoritmo de Kahn)
  - Detección de Ciclos
  - Camino Más Corto (Dijkstra)
  - Árbol de Expansión Mínima - MST (Prim y Kruskal)
- **Implementar técnicas de optimización**:
  - Algoritmos Greedy (selección de materias)
  - Programación Dinámica (Knapsack)
  - Backtracking (búsqueda de rutas)
  - Branch & Bound (planificación óptima)
- **Proporcionar una API REST** para consultar y manipular el grafo de materias

---

## 🏗️ Estructura del Proyecto

```
TP/
├── src/
│   ├── main/
│   │   ├── java/com/tp/PlanificadorMat/
│   │   │   ├── configuracion/
│   │   │   │   └── Neo4jConfig.java          # Configuración de Neo4j
│   │   │   ├── controllers/
│   │   │   │   ├── CourseController.java     # CRUD de materias
│   │   │   │   ├── GraphController.java      # Endpoints de algoritmos de grafos
│   │   │   │   ├── ScheduleController.java   # Endpoints de planificación
│   │   │   │   ├── Ping.java                 # Health check
│   │   │   │   └── MstEdgeDTO.java           # DTO para aristas MST
│   │   │   ├── modelo/
│   │   │   │   └── Course.java               # Entidad de materia
│   │   │   ├── repositorio/
│   │   │   │   └── CourseRepository.java     # Repositorio Neo4j
│   │   │   ├── servicio/
│   │   │   │   ├── GraphService.java         # Lógica de algoritmos de grafos
│   │   │   │   └── ScheduleService.java      # Lógica de planificación
│   │   │   └── TpApplication.java            # Punto de entrada
│   │   └── resources/
│   │       └── application.properties        # Configuración de la aplicación
│   └── test/
│       └── java/com/tp/PlanificadorMat/
│           └── TpApplicationTests.java       # Tests (básicos)
├── pom.xml                                    # Configuración Maven
├── mvnw                                       # Maven Wrapper (Linux/Mac)
└── mvnw.cmd                                   # Maven Wrapper (Windows)
```

### Organización por capas:

1. **Capa de Modelo** (`modelo/`): Define la entidad `Course` con sus atributos y relaciones
2. **Capa de Repositorio** (`repositorio/`): Maneja la persistencia en Neo4j con queries personalizadas
3. **Capa de Servicio** (`servicio/`): Contiene la lógica de negocio y los algoritmos
4. **Capa de Controlador** (`controllers/`): Expone la API REST
5. **Configuración** (`configuracion/`): Configuración de beans y conexiones

---

## 🛠️ Tecnologías Utilizadas

- **Java 17**: Lenguaje de programación
- **Spring Boot 3.5.6**: Framework principal
  - Spring Web: Para la API REST
  - Spring Data Neo4j: Para integración con Neo4j
  - Spring DevTools: Para desarrollo ágil
- **Neo4j**: Base de datos orientada a grafos
  - URI: `bolt://localhost:7687`
  - Credenciales: `neo4j` / `grupo123`
- **Maven**: Gestor de dependencias y construcción
- **Reactor**: Programación reactiva (Mono/Flux)

---

## 🔧 Arquitectura y Funcionamiento

### Modelo de Datos

#### Entidad Course (Nodo en Neo4j)
```java
@Node("Course")
public class Course {
    @Id
    private String code;           // Código único (ej: "MAT101")
    private String name;            // Nombre de la materia
    private Integer credits;        // Créditos que otorga (ej: 4)
    private Integer hours;          // Horas semanales (ej: 6)
    private Integer difficulty;     // Dificultad (1-5)
    
    @Relationship(type = "REQUIRES", direction = OUTGOING)
    private Set<Course> prereqs;    // Materias prerequisito
}
```

#### Relaciones en Neo4j
- **REQUIRES**: Relación dirigida que indica prerequisitos
  - `(A)-[:REQUIRES]->(B)` significa "A requiere B como prerequisito"
- **RELATED**: Relación no dirigida con peso `sim` (similaridad) usada para MST
  - `(A)-[:RELATED {sim: 0.8}]-(B)` indica relación de similaridad

### Flujo de Funcionamiento

1. **Cliente** realiza petición HTTP a un endpoint
2. **Controller** recibe la petición y valida parámetros
3. **Service** ejecuta la lógica de negocio:
   - Consulta datos desde **Repository**
   - Construye estructuras de datos en memoria (grafos, mapas)
   - Ejecuta algoritmos
4. **Repository** interactúa con **Neo4j** mediante queries Cypher
5. **Service** retorna resultado (Mono/Flux reactivo)
6. **Controller** serializa respuesta a JSON
7. **Cliente** recibe respuesta

---

## 🧮 Algoritmos Implementados

### 1. Algoritmos de Grafos (`GraphService`)

#### DFS (Depth-First Search)
- **Complejidad**: O(V + E)
- **Descripción**: Recorre el grafo en profundidad desde un nodo inicial
- **Uso**: Explorar todas las materias alcanzables desde una materia dada

#### BFS (Breadth-First Search)
- **Complejidad**: O(V + E)
- **Descripción**: Recorre el grafo por capas/niveles
- **Uso**: Ver materias organizadas por "distancia" de prerequisitos

#### Ordenamiento Topológico (Kahn)
- **Complejidad**: O(V + E)
- **Descripción**: Ordena las materias respetando prerequisitos
- **Características**:
  - Considera materias ya aprobadas
  - Calcula grado de entrada (indegree) efectivo
  - Retorna orden de cursada posible

#### Detección de Ciclos
- **Complejidad**: O(V + E)
- **Descripción**: Detecta si hay ciclos en el grafo de prerequisitos
- **Uso**: Validar que el plan de estudios sea coherente

#### Dijkstra (Camino Más Corto)
- **Complejidad**: O((V + E) log V)
- **Descripción**: Encuentra el camino más corto entre dos materias
- **Métricas de peso**:
  - `difficulty`: Por dificultad de las materias
  - `hours`: Por horas semanales
  - `credits`: Por créditos (invertido)

#### MST (Minimum Spanning Tree)
- **Complejidad**: O(E log V)
- **Algoritmos**: Prim y Kruskal
- **Descripción**: Construye árbol de expansión mínima sobre relaciones RELATED
- **Uso**: Encontrar conjunto óptimo de materias relacionadas

### 2. Algoritmos de Planificación (`ScheduleService`)

#### Materias Disponibles
- **Complejidad**: O(V + E)
- **Descripción**: Encuentra materias cursables dado un conjunto de aprobadas
- **Query Cypher**: Usa `ALL` para verificar prerequisitos

#### Greedy (Selección Voraz)
- **Complejidad**: O(n log n)
- **Descripción**: Selecciona materias para un cuatrimestre maximizando valor
- **Estrategias**:
  - Por créditos (maximizar)
  - Por dificultad (minimizar)
  - Por horas (minimizar)
- **Restricción**: Límite de horas semanales (maxHours)

#### DP Knapsack (Programación Dinámica)
- **Complejidad**: O(n × capacidad)
- **Descripción**: Selección óptima de materias (problema de la mochila)
- **Optimiza**: Valor (créditos/dificultad/horas)
- **Restricción**: Horas totales
- **Ventaja**: Solución óptima garantizada (vs Greedy)

#### Backtracking
- **Complejidad**: Exponencial (con poda)
- **Descripción**: Encuentra TODAS las rutas posibles entre dos materias
- **Parámetro**: `maxDepth` para limitar profundidad de búsqueda
- **Uso**: Explorar alternativas de cursada

#### Branch & Bound
- **Complejidad**: Exponencial (con poda agresiva)
- **Descripción**: Planificación óptima a N cuatrimestres
- **Objetivo**: Maximizar créditos totales
- **Técnicas**:
  - Bound estimation (cota superior)
  - Poda por límite inferior (lower bound)
  - Exploración de ramas heurística

---

## ⚙️ Configuración y Requisitos

### Opción 1: Docker (Recomendado) 🐳

**Requisitos:**
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) instalado
- Docker Compose (incluido en Docker Desktop)

**Quick Start:**
```bash
# Levantar todo (Neo4j + Aplicación)
docker-compose up -d

# Esperar ~40 segundos
curl http://localhost:8080/ping

# Cargar datos de prueba
# Windows:
.\scripts\init-data.ps1
# Linux/Mac:
./scripts/init-data.sh
```

**📘 [Ver guía completa de Docker](./DOCKER.md)** con troubleshooting, comandos útiles y configuración avanzada.

---

### Opción 2: Instalación Local

**Requisitos previos:**

1. **Java 17** o superior
2. **Maven 3.6+** (o usar el wrapper incluido)
3. **Neo4j 4.x/5.x** en ejecución
   - Puerto: 7687 (bolt)
   - Usuario: `neo4j`
   - Contraseña: `grupo123`

#### Instalación de Neo4j

**Opción A: Neo4j Desktop**
1. Descargar desde [https://neo4j.com/download/](https://neo4j.com/download/)
2. Crear una base de datos local
3. Configurar contraseña `grupo123`
4. Iniciar la base de datos

**Opción B: Docker (solo Neo4j)**
```bash
# Usando docker-compose.dev.yml
docker-compose -f docker-compose.dev.yml up -d

# O con comando directo
docker run \
  --name neo4j-planificador \
  -p 7474:7474 -p 7687:7687 \
  -e NEO4J_AUTH=neo4j/grupo123 \
  neo4j:5.13.0
```

### Configuración del Proyecto

Editar `src/main/resources/application.properties`:
```properties
spring.application.name=TP
spring.neo4j.uri=bolt://localhost:7687
spring.neo4j.authentication.username=neo4j
spring.neo4j.authentication.password=grupo123
```

### Ejecutar el Proyecto

#### Con Docker (Recomendado):
```bash
# Todo en contenedores
docker-compose up -d

# Ver logs
docker-compose logs -f
```

#### Desarrollo Local (sin Docker para la app):

**Windows:**
```bash
.\mvnw.cmd spring-boot:run
```

**Linux/Mac:**
```bash
./mvnw spring-boot:run
```

**Con Maven instalado:**
```bash
mvn spring-boot:run
```

La aplicación se iniciará en `http://localhost:8080`

---

### 🐳 Comandos Docker Útiles

```bash
# Ver estado de servicios
docker-compose ps

# Ver logs en tiempo real
docker-compose logs -f app

# Detener servicios
docker-compose stop

# Detener y eliminar (mantiene datos)
docker-compose down

# Eliminar todo incluyendo datos
docker-compose down -v

# Reconstruir aplicación
docker-compose build app
docker-compose up -d --build

# Acceder a Neo4j Browser
# http://localhost:7474
# Usuario: neo4j / Password: grupo123
```

📘 **[Ver DOCKER.md](./DOCKER.md)** para guía completa

---

## 📡 Guía de Uso - Endpoints y cURLs

### Health Check

#### Ping
```bash
curl http://localhost:8080/ping
```
**Respuesta**: `pong`

---

### 1. Course Controller - CRUD de Materias

#### Crear/Actualizar Materia (Upsert)
```bash
curl -X PUT http://localhost:8080/courses \
  -H "Content-Type: application/json" \
  -d '{
    "code": "MAT101",
    "name": "Matemática I",
    "credits": 6,
    "hours": 8,
    "difficulty": 4,
    "prereqs": []
  }'
```

#### Crear Materia con Prerequisitos
```bash
curl -X PUT http://localhost:8080/courses \
  -H "Content-Type: application/json" \
  -d '{
    "code": "MAT201",
    "name": "Matemática II",
    "credits": 6,
    "hours": 8,
    "difficulty": 5,
    "prereqs": [
      {"code": "MAT101", "name": "Matemática I"}
    ]
  }'
```

#### Obtener Todas las Materias
```bash
curl http://localhost:8080/courses
```
**Nota**: Retorna un stream de eventos (Server-Sent Events)

#### Obtener Materia por Código
```bash
curl http://localhost:8080/courses/MAT101
```

#### Crear Nueva Materia (POST - Falla si existe)
```bash
curl -X POST http://localhost:8080/courses \
  -H "Content-Type: application/json" \
  -d '{
    "code": "ALG101",
    "name": "Álgebra I",
    "credits": 4,
    "hours": 6,
    "difficulty": 3,
    "prereqs": []
  }'
```

**Respuesta de error si existe**:
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Ya existe una materia con código: ALG101"
}
```

#### Eliminar Materia
```bash
curl -X DELETE http://localhost:8080/courses/MAT101
```

**Respuesta exitosa**: `204 No Content`

#### Actualización Parcial (PATCH)
```bash
# Actualizar solo la dificultad
curl -X PATCH http://localhost:8080/courses/MAT101 \
  -H "Content-Type: application/json" \
  -d '{
    "difficulty": 5
  }'

# Actualizar nombre y créditos
curl -X PATCH http://localhost:8080/courses/MAT101 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Matemática I - Actualizado",
    "credits": 8
  }'
```

#### Búsqueda por Nombre
```bash
# Buscar materias que contengan "Matemática"
curl "http://localhost:8080/courses/search/by-name?name=Matemática"
```

**Ejemplo de respuesta**:
```json
[
  {
    "code": "MAT101",
    "name": "Matemática I",
    "credits": 6,
    "hours": 8,
    "difficulty": 4,
    "prereqs": []
  },
  {
    "code": "MAT201",
    "name": "Matemática II",
    "credits": 6,
    "hours": 8,
    "difficulty": 5,
    "prereqs": []
  }
]
```

#### Búsqueda Avanzada con Múltiples Criterios
```bash
# Buscar materias con 4-6 créditos y dificultad 1-3
curl "http://localhost:8080/courses/search/advanced?minCredits=4&maxCredits=6&minDifficulty=1&maxDifficulty=3"

# Buscar materias de máximo 6 horas semanales que contengan "Física"
curl "http://localhost:8080/courses/search/advanced?nameContains=Física&maxHours=6"

# Búsqueda completa con todos los filtros
curl "http://localhost:8080/courses/search/advanced?nameContains=Mat&minCredits=4&maxCredits=8&minDifficulty=2&maxDifficulty=5&minHours=4&maxHours=10"
```

**Parámetros disponibles**:
- `nameContains`: Texto que debe contener el nombre (case insensitive)
- `minCredits` / `maxCredits`: Rango de créditos
- `minDifficulty` / `maxDifficulty`: Rango de dificultad (1-5)
- `minHours` / `maxHours`: Rango de horas semanales

#### Verificar si Existe una Materia
```bash
curl http://localhost:8080/courses/MAT101/exists
```

**Respuesta**: `true` o `false`

---

### 2. Relationship Controller - Gestión de Relaciones RELATED

#### Crear Relación RELATED entre Materias
```bash
# Con similaridad especificada
curl -X POST http://localhost:8080/relationships \
  -H "Content-Type: application/json" \
  -d '{
    "fromCode": "MAT101",
    "toCode": "FIS101",
    "similarity": 0.8
  }'

# Sin similaridad (usa 0.5 por defecto)
curl -X POST http://localhost:8080/relationships \
  -H "Content-Type: application/json" \
  -d '{
    "fromCode": "MAT201",
    "toCode": "FIS201"
  }'
```

**Respuesta**:
```json
{
  "message": "Relación creada exitosamente",
  "from": "MAT101",
  "to": "FIS101",
  "similarity": "0.8"
}
```

#### Crear Relación con Similaridad Automática
```bash
# El sistema calcula la similaridad basándose en créditos, horas y dificultad
curl -X POST http://localhost:8080/relationships/auto \
  -H "Content-Type: application/json" \
  -d '{
    "fromCode": "MAT101",
    "toCode": "ALG101"
  }'
```

**Respuesta**:
```json
{
  "message": "Relación creada con similaridad calculada automáticamente",
  "from": "MAT101",
  "to": "ALG101",
  "similarity": "0.75"
}
```

#### Actualizar Similaridad de una Relación
```bash
curl -X PATCH http://localhost:8080/relationships/MAT101/FIS101 \
  -H "Content-Type: application/json" \
  -d '{
    "similarity": 0.9
  }'
```

**Respuesta**:
```json
{
  "message": "Similaridad actualizada exitosamente",
  "from": "MAT101",
  "to": "FIS101",
  "similarity": "0.9"
}
```

#### Eliminar Relación RELATED
```bash
curl -X DELETE http://localhost:8080/relationships/MAT101/FIS101
```

**Respuesta**:
```json
{
  "message": "Relación eliminada exitosamente",
  "from": "MAT101",
  "to": "FIS101"
}
```

#### Obtener Materias Relacionadas con una Materia
```bash
curl http://localhost:8080/relationships/MAT101
```

**Ejemplo de respuesta**:
```json
[
  {
    "relatedCourse": "FIS101",
    "similarity": 0.8
  },
  {
    "relatedCourse": "ALG101",
    "similarity": 0.75
  }
]
```

#### Listar Todas las Relaciones RELATED
```bash
curl http://localhost:8080/relationships
```

**Ejemplo de respuesta**:
```json
[
  {
    "from": "MAT101",
    "to": "FIS101",
    "similarity": 0.8
  },
  {
    "from": "MAT201",
    "to": "FIS201",
    "similarity": 0.85
  },
  {
    "from": "MAT101",
    "to": "ALG101",
    "similarity": 0.75
  }
]
```

---

### 3. Graph Controller - Algoritmos de Grafos

#### DFS (Recorrido en Profundidad)
```bash
# Desde una materia específica
curl "http://localhost:8080/graph/dfs?from=MAT101"
```

**Ejemplo de respuesta**:
```json
["MAT101", "FIS101", "ALG101"]
```

#### BFS (Recorrido por Capas)
```bash
curl "http://localhost:8080/graph/bfs-layers?from=MAT101"
```

**Ejemplo de respuesta**:
```json
[
  ["MAT101"],
  ["FIS101", "ALG101"],
  ["MAT201", "FIS201"]
]
```

#### Ordenamiento Topológico
```bash
# Sin materias aprobadas
curl "http://localhost:8080/graph/toposort"

# Con materias aprobadas
curl "http://localhost:8080/graph/toposort?approved=MAT101&approved=FIS101"
```

**Ejemplo de respuesta**:
```json
["ALG101", "MAT201", "FIS201", "MAT301"]
```

#### Detección de Ciclos
```bash
curl http://localhost:8080/graph/cycles
```

**Ejemplo de respuesta**:
```json
{"hasCycle": false}
```

#### Dijkstra (Camino Más Corto)
```bash
# Por dificultad (default)
curl "http://localhost:8080/graph/shortest?from=MAT101&to=MAT301"

# Por horas
curl "http://localhost:8080/graph/shortest?from=MAT101&to=MAT301&metric=hours"

# Por créditos
curl "http://localhost:8080/graph/shortest?from=MAT101&to=MAT301&metric=credits"
```

**Ejemplo de respuesta**:
```json
["MAT101", "MAT201", "MAT301"]
```

#### MST (Árbol de Expansión Mínima)
```bash
# Algoritmo de Prim (default)
curl "http://localhost:8080/graph/mst"

# Algoritmo de Kruskal
curl "http://localhost:8080/graph/mst?algo=kruskal"
```

**Ejemplo de respuesta**:
```json
[
  {"from": "MAT101", "to": "FIS101", "weight": 1.25},
  {"from": "FIS101", "to": "MAT201", "weight": 1.33},
  {"from": "MAT201", "to": "ALG201", "weight": 1.11}
]
```

**Nota**: Se requieren relaciones RELATED en Neo4j:
```cypher
MATCH (a:Course {code: 'MAT101'}), (b:Course {code: 'FIS101'})
CREATE (a)-[:RELATED {sim: 0.8}]-(b)
```

---

### 4. Schedule Controller - Algoritmos de Planificación

#### Materias Disponibles
```bash
# Sin materias aprobadas
curl "http://localhost:8080/schedule/available"

# Con materias aprobadas
curl "http://localhost:8080/schedule/available?approved=MAT101&approved=FIS101"
```

**Ejemplo de respuesta**:
```json
[
  {
    "code": "MAT201",
    "name": "Matemática II",
    "credits": 6,
    "hours": 8,
    "difficulty": 5,
    "prereqs": []
  },
  {
    "code": "FIS201",
    "name": "Física II",
    "credits": 6,
    "hours": 6,
    "difficulty": 4,
    "prereqs": []
  }
]
```

#### Greedy (Selección Voraz)
```bash
# Maximizar créditos (default)
curl "http://localhost:8080/schedule/greedy?approved=MAT101&maxHours=20"

# Minimizar dificultad
curl "http://localhost:8080/schedule/greedy?approved=MAT101&value=difficulty&maxHours=20"

# Minimizar horas
curl "http://localhost:8080/schedule/greedy?approved=MAT101&value=hours&maxHours=24"
```

**Parámetros**:
- `approved`: Materias ya aprobadas (opcional)
- `value`: `credits` (default) | `difficulty` | `hours`
- `maxHours`: Límite de horas semanales (default: 24)

#### DP Knapsack (Programación Dinámica)
```bash
# Maximizar créditos con límite de horas
curl "http://localhost:8080/schedule/dp?approved=MAT101&value=credits&maxHours=20"

# Minimizar dificultad
curl "http://localhost:8080/schedule/dp?value=difficulty&maxHours=24"
```

**Ejemplo de respuesta**:
```json
[
  {
    "code": "MAT201",
    "name": "Matemática II",
    "credits": 6,
    "hours": 8,
    "difficulty": 5,
    "prereqs": []
  },
  {
    "code": "ALG201",
    "name": "Álgebra II",
    "credits": 4,
    "hours": 6,
    "difficulty": 3,
    "prereqs": []
  }
]
```

#### Backtracking (Todas las Rutas)
```bash
# Encontrar rutas de MAT101 a MAT301
curl "http://localhost:8080/schedule/backtracking?from=MAT101&to=MAT301"

# Con límite de profundidad
curl "http://localhost:8080/schedule/backtracking?from=MAT101&to=MAT301&maxDepth=5"
```

**Ejemplo de respuesta**:
```json
[
  ["MAT101", "MAT201", "MAT301"],
  ["MAT101", "ALG101", "ALG201", "MAT301"]
]
```

#### Branch & Bound (Planificación Óptima)
```bash
# Plan a 4 cuatrimestres con 24hs/semana
curl "http://localhost:8080/schedule/bnb?semesters=4&maxHours=24"

# Con materias aprobadas
curl "http://localhost:8080/schedule/bnb?approved=MAT101&approved=FIS101&semesters=3&maxHours=20"
```

**Parámetros**:
- `approved`: Materias ya aprobadas (opcional)
- `semesters`: Número de cuatrimestres a planificar (default: 4)
- `maxHours`: Horas máximas por cuatrimestre (default: 24)

**Ejemplo de respuesta**:
```json
[
  ["MAT101", "FIS101", "ALG101"],
  ["MAT201", "FIS201"],
  ["ALG201", "PRO201"],
  ["MAT301"]
]
```

---

### Ejemplo Completo: Cargar Datos de Prueba

```bash
# 1. Crear materias base
curl -X PUT http://localhost:8080/courses -H "Content-Type: application/json" -d '{"code":"MAT101","name":"Matemática I","credits":6,"hours":8,"difficulty":4,"prereqs":[]}'
curl -X PUT http://localhost:8080/courses -H "Content-Type: application/json" -d '{"code":"FIS101","name":"Física I","credits":6,"hours":6,"difficulty":3,"prereqs":[]}'
curl -X PUT http://localhost:8080/courses -H "Content-Type: application/json" -d '{"code":"ALG101","name":"Álgebra I","credits":4,"hours":6,"difficulty":3,"prereqs":[]}'

# 2. Crear materias con prerequisitos
curl -X PUT http://localhost:8080/courses -H "Content-Type: application/json" -d '{"code":"MAT201","name":"Matemática II","credits":6,"hours":8,"difficulty":5,"prereqs":[{"code":"MAT101"}]}'
curl -X PUT http://localhost:8080/courses -H "Content-Type: application/json" -d '{"code":"FIS201","name":"Física II","credits":6,"hours":6,"difficulty":4,"prereqs":[{"code":"FIS101"},{"code":"MAT101"}]}'
curl -X PUT http://localhost:8080/courses -H "Content-Type: application/json" -d '{"code":"ALG201","name":"Álgebra II","credits":4,"hours":6,"difficulty":4,"prereqs":[{"code":"ALG101"}]}'

# 3. Crear relaciones RELATED (similaridad)
curl -X POST http://localhost:8080/relationships -H "Content-Type: application/json" -d '{"fromCode":"MAT101","toCode":"FIS101","similarity":0.8}'
curl -X POST http://localhost:8080/relationships -H "Content-Type: application/json" -d '{"fromCode":"MAT101","toCode":"ALG101","similarity":0.75}'
curl -X POST http://localhost:8080/relationships/auto -H "Content-Type: application/json" -d '{"fromCode":"MAT201","toCode":"FIS201"}'

# 4. Probar búsquedas
curl "http://localhost:8080/courses/search/by-name?name=Matemática"
curl "http://localhost:8080/courses/search/advanced?minCredits=4&maxCredits=6"

# 5. Probar relaciones
curl "http://localhost:8080/relationships"
curl "http://localhost:8080/relationships/MAT101"

# 6. Probar algoritmos
curl "http://localhost:8080/graph/toposort"
curl "http://localhost:8080/schedule/greedy?maxHours=20"
curl "http://localhost:8080/schedule/dp?maxHours=20"
curl "http://localhost:8080/graph/mst?algo=prim"
```

---

## 🚧 Qué Falta por Implementar

### 1. Funcionalidades Básicas Faltantes

#### CRUD Completo
- ✅ **DELETE** de materias - **IMPLEMENTADO**
- ✅ Búsqueda avanzada de materias (por nombre, rango de créditos, etc.) - **IMPLEMENTADO**
- ✅ Actualización parcial (PATCH) - **IMPLEMENTADO**
- ✅ Validaciones de entrada robustas - **IMPLEMENTADO**
  - Validación de código y nombre obligatorios
  - Validación de rangos (créditos: 0-12, horas: 0-40, dificultad: 1-5)
  - Manejo de errores con mensajes descriptivos
- ✅ POST para crear materias que falla si ya existe - **IMPLEMENTADO**
- ✅ Endpoint para verificar existencia de materias - **IMPLEMENTADO**

#### Gestión de Relaciones RELATED
- ✅ Endpoints para crear/eliminar relaciones RELATED - **IMPLEMENTADO**
- ✅ Cálculo automático de similaridad entre materias - **IMPLEMENTADO**
  - Algoritmo basado en créditos, horas y dificultad
  - Endpoint `/relationships/auto` para cálculo automático
- ✅ CRUD completo de relaciones (crear, leer, actualizar, eliminar) - **IMPLEMENTADO**
- ✅ Listar relaciones por materia y globalmente - **IMPLEMENTADO**
- ❌ Mantenimiento automático de relaciones al eliminar nodos
- ❌ Validación de ciclos al crear relaciones

### 2. Mejoras en Algoritmos

#### GraphService
- ❌ **Floyd-Warshall**: Caminos más cortos entre todos los pares
- ❌ **Bellman-Ford**: Soportar pesos negativos
- ❌ **Strongly Connected Components**: Detectar componentes fuertemente conectadas
- ❌ **Articulación Points**: Materias críticas
- ❌ Mejora en MST: soportar grafos desconectados

#### ScheduleService
- ❌ **A\* Search**: Búsqueda heurística mejorada
- ❌ **Genetic Algorithms**: Optimización de planes de estudio
- ❌ **Simulated Annealing**: Alternativa a B&B
- ❌ Considerar **múltiples objetivos** (multi-objective optimization)
  - Ejemplo: minimizar dificultad Y maximizar créditos simultáneamente

### 3. Validaciones y Restricciones

- ❌ Validar que no se creen ciclos al agregar prerequisitos
- ❌ Validar rangos de valores (credits, hours, difficulty)
- ❌ Validar que prerequisitos existan antes de crear relaciones
- ❌ Limitar máximo de prerequisitos por materia
- ❌ Validar códigos de materia únicos

### 4. Persistencia y Gestión de Datos

- ❌ **Migrations/Scripts de inicialización** para Neo4j
- ❌ Carga masiva de datos desde CSV/JSON
- ❌ Export de planes a diferentes formatos
- ❌ Backup y restore de la base de datos
- ❌ Versionado de planes de estudio

### 5. Testing

- ❌ **Unit Tests** completos para servicios
- ❌ **Integration Tests** para endpoints
- ❌ Tests de algoritmos con casos edge
- ❌ Tests de performance con grafos grandes
- ❌ Coverage mínimo del 80%
- ❌ Tests de carga/estrés

### 6. Documentación y Observabilidad

- ❌ **Swagger/OpenAPI**: Documentación interactiva de la API
- ❌ **Spring Actuator**: Métricas y health checks avanzados
- ❌ **Logging estructurado**: Con niveles apropiados
- ❌ **Monitoring**: Integración con Prometheus/Grafana
- ❌ Documentación de arquitectura (diagramas C4, etc.)
- ❌ Ejemplos de uso más completos

### 7. Seguridad

- ❌ **Autenticación**: Spring Security con JWT
- ❌ **Autorización**: Roles (estudiante, admin, etc.)
- ❌ **Rate Limiting**: Prevenir abuso de endpoints
- ❌ **CORS**: Configuración adecuada
- ❌ Sanitización de inputs
- ❌ Protección contra inyecciones Cypher

### 8. Performance

- ❌ **Caché**: Redis para resultados frecuentes
- ❌ **Paginación**: Para listados grandes
- ❌ **Índices**: En Neo4j para queries frecuentes
- ❌ **Optimización de queries**: Cypher más eficiente
- ❌ Connection pooling configurado
- ❌ Timeouts configurados

### 9. Frontend

- ❌ **Web UI**: Interfaz para visualizar el grafo
- ❌ Visualización interactiva de planes
- ❌ Editor de prerequisitos drag-and-drop
- ❌ Dashboard con estadísticas
- ❌ Comparación de estrategias de planificación

### 10. Características Avanzadas

- ❌ **Simulación**: "Qué pasa si apruebo X materia"
- ❌ **Recomendaciones**: ML para sugerir mejores planes
- ❌ **Análisis de tendencias**: Materias más cursadas juntas
- ❌ **Optimización multi-criterio**: Pareto frontier
- ❌ **Horarios**: Integrar slots horarios y colisiones
- ❌ **Profesores**: Considerar disponibilidad y ratings
- ❌ **Cupos**: Límites de inscripción

### 11. Infraestructura

- ✅ **Docker Compose**: Para ambiente completo - **IMPLEMENTADO**
  - `docker-compose.yml`: Configuración completa (Neo4j + App)
  - `docker-compose.dev.yml`: Solo Neo4j para desarrollo
  - `Dockerfile`: Multi-stage build optimizado
  - Scripts de inicialización de datos (Windows y Linux)
  - Guía completa en DOCKER.md
- ❌ **CI/CD**: GitHub Actions / Jenkins
- ❌ **Kubernetes**: Deployment en producción
- ✅ **Variables de entorno**: Gestión con perfiles - **PARCIAL**
  - Archivo `.env.example` incluido
  - Variables en docker-compose
- ✅ Configuración para diferentes ambientes (dev, prod) - **IMPLEMENTADO**

### 12. Manejo de Errores

- ❌ **Exception Handlers** globales
- ❌ Respuestas de error estandarizadas
- ❌ Códigos de error específicos
- ❌ Mensajes de error informativos
- ❌ Logging de errores con stack trace

---

## 📚 Referencias y Recursos

### Algoritmos
- **DFS/BFS**: [Introduction to Algorithms - CLRS](https://mitpress.mit.edu/books/introduction-algorithms-third-edition)
- **Dijkstra**: [Wikipedia - Dijkstra's Algorithm](https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm)
- **MST**: [Wikipedia - Minimum Spanning Tree](https://en.wikipedia.org/wiki/Minimum_spanning_tree)
- **Branch & Bound**: [GeeksforGeeks - Branch and Bound](https://www.geeksforgeeks.org/branch-and-bound-algorithm/)

### Tecnologías
- **Spring Boot**: [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- **Neo4j**: [Neo4j Documentation](https://neo4j.com/docs/)
- **Spring Data Neo4j**: [Spring Data Neo4j Reference](https://docs.spring.io/spring-data/neo4j/reference/)
- **Reactor**: [Project Reactor Documentation](https://projectreactor.io/docs)

---

## 👥 Autores

**Grupo 1** - Planificador de Materias  
Trabajo Práctico - Algoritmos y Estructuras de Datos

---

## 📄 Licencia

Este proyecto es un trabajo práctico académico.

---

## 🔗 Links Útiles

- **Neo4j Browser**: http://localhost:7474
- **API Base URL**: http://localhost:8080
- **Health Check**: http://localhost:8080/ping

---

**Última actualización**: 2025-10-26

