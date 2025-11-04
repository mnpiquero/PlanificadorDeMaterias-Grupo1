# Ejemplos de Uso - Plan de Estudios

Una vez que hayas cargado el plan de estudios usando `init-data-ingenieria.sh` o `init-data-ingenieria.ps1`, puedes probar estos ejemplos.

## 📡 API REST - Ejemplos con curl

### 1. Ver todas las materias

```bash
curl http://localhost:8080/courses | jq
```

### 2. Ver una materia específica

```bash
# Fundamentos de Informática
curl http://localhost:8080/courses/3.4.069 | jq

# Programación I
curl http://localhost:8080/courses/3.4.071 | jq

# Inteligencia Artificial
curl http://localhost:8080/courses/3.4.096 | jq
```

### 3. Buscar materias por nombre

```bash
# Buscar todas las materias con "Programación" en el nombre
curl "http://localhost:8080/courses/search?name=Programación" | jq

# Buscar materias de Física
curl "http://localhost:8080/courses/search?name=Física" | jq
```

### 4. Ver prerequisitos de una materia

```bash
# Ver qué materias requiere Programación III
curl http://localhost:8080/courses/3.4.077 | jq '.prereqs'

# Ver prerequisitos de Seminario de Integración Profesional (tiene 3)
curl http://localhost:8080/courses/3.4.211 | jq '.prereqs'
```

### 5. Orden topológico (orden sugerido de cursada)

```bash
# Ver el orden completo de todas las materias
curl "http://localhost:8080/graph/toposort" | jq

# Contar cuántas materias hay
curl "http://localhost:8080/graph/toposort" | jq '. | length'
```

### 6. Planificación con el algoritmo Greedy

```bash
# Planificar con máximo 20 horas por cuatrimestre
curl "http://localhost:8080/schedule/greedy?maxHours=20" | jq

# Planificar con máximo 15 horas (más conservador)
curl "http://localhost:8080/schedule/greedy?maxHours=15" | jq

# Planificar con máximo 25 horas (más agresivo)
curl "http://localhost:8080/schedule/greedy?maxHours=25" | jq
```

### 7. Calcular Árbol de Expansión Mínimo (MST)

```bash
# MST usando Prim
curl "http://localhost:8080/graph/mst/prim" | jq

# MST usando Kruskal
curl "http://localhost:8080/graph/mst/kruskal" | jq
```

---

## 🌐 Neo4j Browser - Consultas Cypher

Abre Neo4j Browser en http://localhost:7474 (usuario: `neo4j`, password: `grupo123`) y prueba estas consultas:

### 1. Ver todas las materias

```cypher
MATCH (c:Course)
RETURN c
LIMIT 50
```

### 2. Ver el grafo de correlativas completo

```cypher
MATCH (a:Course)-[:REQUIRES]->(b:Course)
RETURN a, b
```

### 3. Materias de 1° año (sin correlativas)

```cypher
MATCH (c:Course)
WHERE NOT (c)-[:REQUIRES]->()
RETURN c.code as Código, c.name as Materia, c.hours as Horas
ORDER BY c.code
```

### 4. Cadena de correlativas desde Fundamentos de Informática

```cypher
MATCH path = (start:Course {code: '3.4.069'})-[:REQUIRES*]->(end:Course)
RETURN path
```

### 5. ¿Qué materias requieren Programación I?

```cypher
MATCH (prereq:Course {code: '3.4.071'})<-[:REQUIRES]-(course:Course)
RETURN course.code as Código, course.name as Materia
ORDER BY course.code
```

### 6. Materias con más de 100 horas

```cypher
MATCH (c:Course)
WHERE c.hours > 100
RETURN c.code as Código, c.name as Materia, c.hours as Horas
ORDER BY c.hours DESC
```

### 7. Materias más difíciles (dificultad 5)

```cypher
MATCH (c:Course)
WHERE c.difficulty = 5
RETURN c.code as Código, c.name as Materia, c.hours as Horas
ORDER BY c.code
```

### 8. Camino más corto entre dos materias

```cypher
MATCH path = shortestPath(
  (start:Course {code: '3.4.069'})-[:REQUIRES*]->(end:Course {code: '3.4.096'})
)
RETURN path
```

### 9. Materias sin prerequisitos (materias de entrada)

```cypher
MATCH (c:Course)
WHERE NOT (c)-[:REQUIRES]->()
RETURN c.code as Código, c.name as Materia, c.hours as Horas
ORDER BY c.code
```

### 10. Contar correlativas por materia

```cypher
MATCH (c:Course)
OPTIONAL MATCH (c)-[:REQUIRES]->(prereq:Course)
WITH c, count(prereq) as numPrereqs
RETURN c.code as Código, c.name as Materia, numPrereqs as Correlativas
ORDER BY numPrereqs DESC
```

### 11. Materias que son prerequisito de muchas otras

```cypher
MATCH (prereq:Course)<-[:REQUIRES]-(course:Course)
WITH prereq, count(course) as numCourses
WHERE numCourses > 0
RETURN prereq.code as Código, prereq.name as Materia, numCourses as "Es prereq de"
ORDER BY numCourses DESC
```

### 12. Profundidad de cada materia en el grafo

```cypher
MATCH path = (c:Course)-[:REQUIRES*0..]->(end:Course)
WHERE NOT (end)-[:REQUIRES]->()
WITH c, max(length(path)) as depth
RETURN c.code as Código, c.name as Materia, depth as Profundidad
ORDER BY depth DESC, c.code
```

### 13. Todas las materias de Programación

```cypher
MATCH (c:Course)
WHERE c.name CONTAINS 'Programación'
RETURN c.code as Código, c.name as Materia, c.hours as Horas
ORDER BY c.code
```

### 14. Visualizar camino desde materias base hasta Proyecto Final

```cypher
MATCH path = (start:Course)-[:REQUIRES*]->(end:Course {code: '3.4.100'})
WHERE NOT (start)-[:REQUIRES]->()
RETURN path
LIMIT 10
```

### 15. Estadísticas generales

```cypher
MATCH (c:Course)
WITH count(c) as totalCursos,
     sum(c.hours) as totalHoras,
     avg(c.hours) as promedioHoras,
     max(c.hours) as maxHoras,
     min(c.hours) as minHoras
RETURN 
  totalCursos as "Total Materias",
  totalHoras as "Total Horas",
  round(promedioHoras) as "Promedio Horas",
  maxHoras as "Máximo Horas",
  minHoras as "Mínimo Horas"
```

---

## 🎯 Casos de Uso Prácticos

### Caso 1: Estudiante que quiere saber qué puede cursar

Si ya aprobaste ciertas materias, puedes simular qué materias podés cursar:

```cypher
// Materias aprobadas: Fundamentos de Informática, Elementos de Álgebra
MATCH (available:Course)
WHERE NOT EXISTS {
  MATCH (available)-[:REQUIRES]->(prereq:Course)
  WHERE NOT prereq.code IN ['3.4.069', '3.1.050']
}
AND NOT available.code IN ['3.4.069', '3.1.050']
RETURN available.code as Código, available.name as "Podés Cursar"
ORDER BY available.code
```

### Caso 2: Planificar el primer año completo

```bash
# Ver todas las materias sin correlativas (1° cuatrimestre)
curl "http://localhost:8080/courses/search?name=" | jq '[.[] | select(.prereqs | length == 0)]'
```

### Caso 3: Ver la carga horaria por cuatrimestre estimada

```bash
# Planificación con 20 horas por cuatrimestre
curl "http://localhost:8080/schedule/greedy?maxHours=20" | jq '.[] | {semester: .semester, courses: .courses | length, totalHours: ([.courses[].hours] | add)}'
```

---

## 🔧 Debugging y Mantenimiento

### Verificar que se cargaron todas las materias

```bash
curl http://localhost:8080/courses | jq 'length'
# Debería devolver: 46
```

### Verificar materias con correlativas

```cypher
MATCH ()-[r:REQUIRES]->()
RETURN count(r) as TotalCorrelativas
```

### Limpiar la base de datos (¡CUIDADO!)

```cypher
// Borrar todas las materias y relaciones
MATCH (n:Course)
DETACH DELETE n
```

Luego puedes volver a ejecutar el script de inicialización.

---

## 📚 Documentación Adicional

- Ver el plan completo: `PLAN_INGENIERIA_1621.md`
- Instrucciones de los scripts: `README.md`
- Documentación de la API: Ver `API_Y_ALGORITMOS.md` en el directorio raíz del proyecto

