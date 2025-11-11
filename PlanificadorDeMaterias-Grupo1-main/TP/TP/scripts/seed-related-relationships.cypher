// Script para crear las relaciones RELATED según el seed proporcionado
// Ejecuta este script en Neo4j Browser (http://localhost:7474)

// Primero, verifica que los cursos existan
MATCH (c:Course)
RETURN c.code AS code, c.name AS name
ORDER BY c.code;

// Crear relaciones RELATED (grafo no dirigido)
// IMPORTANTE: Usamos MERGE con sintaxis no dirigida para asegurar que se creen correctamente

// POO <-> AYP
MATCH (poo:Course {code:'POO'}), (ayp:Course {code:'AYP'})
MERGE (poo)-[r1:RELATED]-(ayp)
SET r1.sim = 1.0
MERGE (ayp)-[r2:RELATED]-(poo)
SET r2.sim = 1.0;

// PROG3 <-> POO
MATCH (p3:Course {code:'PROG3'}), (poo:Course {code:'POO'})
MERGE (p3)-[r1:RELATED]-(poo)
SET r1.sim = 0.9
MERGE (poo)-[r2:RELATED]-(p3)
SET r2.sim = 0.9;

// PROG3 <-> BD
MATCH (p3:Course {code:'PROG3'}), (bd:Course {code:'BD'})
MERGE (p3)-[r1:RELATED]-(bd)
SET r1.sim = 0.6
MERGE (bd)-[r2:RELATED]-(p3)
SET r2.sim = 0.6;

// AYED2 <-> AYP
MATCH (ay2:Course {code:'AYED2'}), (ayp:Course {code:'AYP'})
MERGE (ay2)-[r1:RELATED]-(ayp)
SET r1.sim = 0.8
MERGE (ayp)-[r2:RELATED]-(ay2)
SET r2.sim = 0.8;

// IA <-> AYED2
MATCH (ia:Course {code:'IA'}), (ay2:Course {code:'AYED2'})
MERGE (ia)-[r1:RELATED]-(ay2)
SET r1.sim = 0.7
MERGE (ay2)-[r2:RELATED]-(ia)
SET r2.sim = 0.7;

// SO <-> POO
MATCH (so:Course {code:'SO'}), (poo:Course {code:'POO'})
MERGE (so)-[r1:RELATED]-(poo)
SET r1.sim = 0.5
MERGE (poo)-[r2:RELATED]-(so)
SET r2.sim = 0.5;

// RED <-> SO
MATCH (red:Course {code:'RED'}), (so:Course {code:'SO'})
MERGE (red)-[r1:RELATED]-(so)
SET r1.sim = 0.4
MERGE (so)-[r2:RELATED]-(red)
SET r2.sim = 0.4;

// Verificar que las relaciones se crearon correctamente
MATCH (a:Course)-[r:RELATED]-(b:Course)
RETURN a.code AS from, b.code AS to, r.sim AS similarity
ORDER BY a.code, b.code;

// Contar relaciones
MATCH (a:Course)-[r:RELATED]-(b:Course)
RETURN count(r) AS totalRelationships;

