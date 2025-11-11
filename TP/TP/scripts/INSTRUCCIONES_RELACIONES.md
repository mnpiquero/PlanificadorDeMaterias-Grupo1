# Instrucciones para Crear Relaciones RELATED

## Problema
Las relaciones RELATED no existen en Neo4j, por eso el endpoint `/graph/mst?algo=prim` devuelve una lista vacía.

## Solución: Opción 1 - Usar la API (Recomendado)

### Paso 1: Verificar que la aplicación esté corriendo
```powershell
# Verifica que la aplicación responda
curl http://localhost:8080/ping
```

### Paso 2: Verificar que los cursos existan
```powershell
# Ver todos los cursos
curl http://localhost:8080/courses
```

### Paso 3: Ejecutar el script PowerShell
```powershell
cd "TP\TP\scripts"
.\create-related-via-api.ps1
```

Este script creará las siguientes relaciones RELATED:
- POO <-> AYP (similaridad: 1.0)
- PROG3 <-> POO (similaridad: 0.9)
- PROG3 <-> BD (similaridad: 0.6)
- AYED2 <-> AYP (similaridad: 0.8)
- IA <-> AYED2 (similaridad: 0.7)
- SO <-> POO (similaridad: 0.5)
- RED <-> SO (similaridad: 0.4)

### Paso 4: Verificar que las relaciones se crearon
```powershell
# Ver todas las relaciones RELATED
curl http://localhost:8080/relationships

# Probar el endpoint MST
curl "http://localhost:8080/graph/mst?algo=prim"
```

---

## Solución: Opción 2 - Usar Neo4j Browser directamente

### Paso 1: Abrir Neo4j Browser
Abre http://localhost:7474 en tu navegador

### Paso 2: Ejecutar el script Cypher
Copia y pega el contenido del archivo `fix-seed-cypher.cypher` en Neo4j Browser y ejecútalo.

O ejecuta este script simplificado:

```cypher
// Verificar que los cursos existan
MATCH (c:Course)
WHERE c.code IN ['AYP', 'POO', 'BD', 'PROG3', 'AYED2', 'SO', 'RED', 'IA']
RETURN c.code AS code, c.name AS name
ORDER BY c.code;

// Crear relaciones RELATED
MATCH (poo:Course {code:'POO'}), (ayp:Course {code:'AYP'})
MERGE (poo)-[r:RELATED]-(ayp)
SET r.sim = 1.0;

MATCH (p3:Course {code:'PROG3'}), (poo:Course {code:'POO'})
MERGE (p3)-[r:RELATED]-(poo)
SET r.sim = 0.9;

MATCH (p3:Course {code:'PROG3'}), (bd:Course {code:'BD'})
MERGE (p3)-[r:RELATED]-(bd)
SET r.sim = 0.6;

MATCH (ay2:Course {code:'AYED2'}), (ayp:Course {code:'AYP'})
MERGE (ay2)-[r:RELATED]-(ayp)
SET r.sim = 0.8;

MATCH (ia:Course {code:'IA'}), (ay2:Course {code:'AYED2'})
MERGE (ia)-[r:RELATED]-(ay2)
SET r.sim = 0.7;

MATCH (so:Course {code:'SO'}), (poo:Course {code:'POO'})
MERGE (so)-[r:RELATED]-(poo)
SET r.sim = 0.5;

MATCH (red:Course {code:'RED'}), (so:Course {code:'SO'})
MERGE (red)-[r:RELATED]-(so)
SET r.sim = 0.4;

// Verificar relaciones creadas
MATCH (a:Course)-[r:RELATED]-(b:Course)
RETURN a.code AS from, b.code AS to, r.sim AS similarity
ORDER BY a.code, b.code;
```

### Paso 3: Verificar en Neo4j Browser
```cypher
MATCH (a:Course)-[r:RELATED]-(b:Course)
RETURN count(r) AS totalRelationships;
```

Deberías ver 7 relaciones (o 14 si se crearon bidireccionales, lo cual está bien).

---

## Verificación Final

Después de crear las relaciones, verifica:

1. **En Neo4j Browser:**
   ```cypher
   MATCH (a:Course)-[r:RELATED]-(b:Course)
   RETURN a.code, b.code, r.sim
   ```

2. **Via API:**
   ```powershell
   curl http://localhost:8080/relationships
   ```

3. **Probar el endpoint MST:**
   ```powershell
   curl "http://localhost:8080/graph/mst?algo=prim"
   ```

Deberías obtener una lista con las aristas del MST.

---

## Problemas Comunes

### Error: "Una o ambas materias no existen"
- Verifica que los cursos existan: `curl http://localhost:8080/courses`
- Asegúrate de que los códigos sean exactamente: AYP, POO, BD, PROG3, AYED2, SO, RED, IA

### Error: "La aplicación no está corriendo"
- Inicia la aplicación Spring Boot
- Verifica que esté corriendo en http://localhost:8080

### Las relaciones no aparecen en Neo4j Browser
- Verifica que ejecutaste el script completo
- Asegúrate de que los cursos existan antes de crear las relaciones
- Revisa los mensajes de error en Neo4j Browser

