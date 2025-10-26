# Planificador de Materias - Grupo 1

Sistema de planificación académica universitaria implementado con Spring Boot y Neo4j. Implementa algoritmos de grafos y técnicas de optimización para resolver problemas de planificación de materias.

## 🚀 Quick Start

### Con Docker (Recomendado)

```bash
# 1. Levantar todo (Neo4j + Aplicación)
docker-compose up -d

# 2. Esperar ~40 segundos y verificar
curl http://localhost:8080/ping

# 3. Cargar datos de prueba
# Windows PowerShell:
.\scripts\init-data.ps1
# Linux/Mac:
chmod +x scripts/init-data.sh && ./scripts/init-data.sh
```

### Sin Docker (Desarrollo Local)

```bash
# 1. Iniciar solo Neo4j en Docker
docker-compose -f docker-compose.dev.yml up -d

# 2. Ejecutar aplicación localmente
./mvnw spring-boot:run

# 3. Verificar
curl http://localhost:8080/ping
```

📘 **[Ver guía completa de Docker](./DOCKER.md)**

## 📚 Documentación

- **[DOCUMENTACION.md](./DOCUMENTACION.md)**: Documentación completa del proyecto
  - Arquitectura detallada
  - Todos los endpoints con ejemplos cURL
  - Algoritmos implementados
  - Configuración avanzada
  
- **[DOCKER.md](./DOCKER.md)**: Guía completa de Docker
  - Configuración de contenedores
  - Comandos útiles
  - Troubleshooting
  - Despliegue

## ✨ Características Principales

### CRUD Completo de Materias
- Crear, leer, actualizar (completo y parcial), eliminar
- Búsqueda avanzada por múltiples criterios
- Validaciones de entrada robustas

### Gestión de Relaciones RELATED
- Crear/eliminar relaciones de similaridad entre materias
- Cálculo automático de similaridad
- Actualización de pesos

### Algoritmos Implementados

**Grafos:**
- DFS/BFS
- Ordenamiento Topológico (Kahn)
- Detección de Ciclos
- Dijkstra (camino más corto)
- MST (Prim y Kruskal)

**Optimización:**
- Greedy
- Programación Dinámica (Knapsack)
- Backtracking
- Branch & Bound

## 📡 Endpoints Principales

### Materias
```bash
GET    /courses                  # Listar todas
GET    /courses/{code}           # Obtener una
POST   /courses                  # Crear
PUT    /courses                  # Crear o actualizar
PATCH  /courses/{code}           # Actualizar parcial
DELETE /courses/{code}           # Eliminar
GET    /courses/search/by-name   # Buscar por nombre
GET    /courses/search/advanced  # Búsqueda avanzada
```

### Relaciones
```bash
GET    /relationships             # Listar todas
GET    /relationships/{code}      # Por materia
POST   /relationships             # Crear
POST   /relationships/auto        # Con similaridad auto
PATCH  /relationships/{from}/{to} # Actualizar
DELETE /relationships/{from}/{to} # Eliminar
```

### Algoritmos
```bash
GET /graph/dfs                 # DFS
GET /graph/bfs-layers          # BFS
GET /graph/toposort            # Orden topológico
GET /graph/cycles              # Detección ciclos
GET /graph/shortest            # Dijkstra
GET /graph/mst                 # MST

GET /schedule/available        # Materias disponibles
GET /schedule/greedy           # Selección greedy
GET /schedule/dp               # Knapsack DP
GET /schedule/backtracking     # Todas las rutas
GET /schedule/bnb              # Branch & Bound
```

## 🛠️ Tecnologías

- Java 17
- Spring Boot 3.5.6
- Spring Data Neo4j
- Neo4j (base de datos de grafos)
- Maven
- Project Reactor (programación reactiva)

## 📝 Ejemplo de Uso

```bash
# 1. Crear materias
curl -X POST http://localhost:8080/courses \
  -H "Content-Type: application/json" \
  -d '{"code":"MAT101","name":"Matemática I","credits":6,"hours":8,"difficulty":4,"prereqs":[]}'

curl -X POST http://localhost:8080/courses \
  -H "Content-Type: application/json" \
  -d '{"code":"FIS101","name":"Física I","credits":6,"hours":6,"difficulty":3,"prereqs":[]}'

# 2. Crear relación de similaridad
curl -X POST http://localhost:8080/relationships \
  -H "Content-Type: application/json" \
  -d '{"fromCode":"MAT101","toCode":"FIS101","similarity":0.8}'

# 3. Obtener orden de cursada
curl "http://localhost:8080/graph/toposort"

# 4. Planificar cuatrimestre (greedy)
curl "http://localhost:8080/schedule/greedy?maxHours=20"

# 5. MST
curl "http://localhost:8080/graph/mst?algo=prim"
```

## 📊 Estructura del Proyecto

```
src/main/java/com/tp/PlanificadorMat/
├── controllers/          # API REST endpoints
│   ├── CourseController.java
│   ├── RelationshipController.java
│   ├── GraphController.java
│   └── ScheduleController.java
├── servicio/            # Lógica de negocio
│   ├── GraphService.java
│   ├── ScheduleService.java
│   └── RelationshipService.java
├── repositorio/         # Acceso a datos
│   └── CourseRepository.java
├── modelo/              # Entidades
│   └── Course.java
└── configuracion/       # Configuración
    └── Neo4jConfig.java
```

## 🔧 Configuración

Editar `src/main/resources/application.properties`:

```properties
spring.application.name=TP
spring.neo4j.uri=bolt://localhost:7687
spring.neo4j.authentication.username=neo4j
spring.neo4j.authentication.password=grupo123
```

## 👥 Equipo

Grupo 1 - Trabajo Práctico  
Algoritmos y Estructuras de Datos

## 📖 Más Información

Para documentación completa, ejemplos detallados y descripción de algoritmos, ver [DOCUMENTACION.md](./DOCUMENTACION.md)

---

**Última actualización**: 2025-10-26

