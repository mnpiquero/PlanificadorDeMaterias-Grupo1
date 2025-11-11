// Script CORREGIDO del seed original
// Este script corrige los errores del seed original y crea correctamente las relaciones RELATED

// 1. Crear constraint (si no existe)
CREATE CONSTRAINT course_code_unique IF NOT EXISTS
FOR (c:Course) REQUIRE c.code IS UNIQUE;

// 2. Crear los cursos (CORREGIDO: eliminado el espacio después de bd:)
MERGE (ayp:Course {code:'AYP',   name:'Algoritmos y Programación', credits:6, hours:6, difficulty:2})
MERGE (poo:Course {code:'POO',   name:'Programación OO',          credits:6, hours:6, difficulty:3})
MERGE (bd:Course {code:'BD',    name:'Bases de Datos',            credits:6, hours:6, difficulty:3})
MERGE (p3:Course {code:'PROG3', name:'Programación 3',            credits:6, hours:6, difficulty:4})
MERGE (ay2:Course {code:'AYED2', name:'Algoritmos y Estructuras 2',credits:6, hours:6, difficulty:4})
MERGE (so:Course {code:'SO',    name:'Sistemas Operativos',       credits:6, hours:6, difficulty:3})
MERGE (red:Course {code:'RED',   name:'Redes',                     credits:6, hours:4, difficulty:3})
MERGE (ia:Course {code:'IA',    name:'Inteligencia Artificial',   credits:6, hours:6, difficulty:5});

// 3. Crear relaciones REQUIRES (correlativas)
MERGE (poo)-[:REQUIRES]->(ayp)
MERGE (p3)-[:REQUIRES]->(poo)
MERGE (p3)-[:REQUIRES]->(bd)
MERGE (ay2)-[:REQUIRES]->(ayp)
MERGE (so)-[:REQUIRES]->(poo)
MERGE (ia)-[:REQUIRES]->(ay2)
MERGE (ia)-[:REQUIRES]->(bd)
MERGE (red)-[:REQUIRES]->(so);

// 4. Crear relaciones RELATED (para MST) - CORREGIDO: usar sintaxis no dirigida
// IMPORTANTE: Usamos MERGE con sintaxis no dirigida para que Neo4j las encuentre correctamente
MATCH (poo:Course {code:'POO'}), (ayp:Course {code:'AYP'})
MERGE (poo)-[r1:RELATED]-(ayp)
SET r1.sim = 1.0;

MATCH (p3:Course {code:'PROG3'}), (poo:Course {code:'POO'})
MERGE (p3)-[r2:RELATED]-(poo)
SET r2.sim = 0.9;

MATCH (p3:Course {code:'PROG3'}), (bd:Course {code:'BD'})
MERGE (p3)-[r3:RELATED]-(bd)
SET r3.sim = 0.6;

MATCH (ay2:Course {code:'AYED2'}), (ayp:Course {code:'AYP'})
MERGE (ay2)-[r4:RELATED]-(ayp)
SET r4.sim = 0.8;

MATCH (ia:Course {code:'IA'}), (ay2:Course {code:'AYED2'})
MERGE (ia)-[r5:RELATED]-(ay2)
SET r5.sim = 0.7;

MATCH (so:Course {code:'SO'}), (poo:Course {code:'POO'})
MERGE (so)-[r6:RELATED]-(poo)
SET r6.sim = 0.5;

MATCH (red:Course {code:'RED'}), (so:Course {code:'SO'})
MERGE (red)-[r7:RELATED]-(so)
SET r7.sim = 0.4;

// 5. Verificar que todo se creó correctamente
MATCH (c:Course)
RETURN c.code AS code, c.name AS name, c.credits AS credits
ORDER BY c.code;

// 6. Verificar relaciones RELATED
MATCH (a:Course)-[r:RELATED]-(b:Course)
RETURN a.code AS from, b.code AS to, r.sim AS similarity
ORDER BY a.code, b.code;

// 7. Contar relaciones
MATCH (a:Course)-[r:RELATED]-(b:Course)
RETURN count(r) AS totalRelatedRelationships;

