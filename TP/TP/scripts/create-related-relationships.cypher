// Script CORREGIDO para crear relaciones RELATED
// Ejecuta este script en Neo4j Browser (http://localhost:7474)
// 
// IMPORTANTE: Este script asume que los cursos ya existen con los códigos:
// AYP, POO, BD, PROG3, AYED2, SO, RED, IA

// Verificar que los cursos existan primero
MATCH (c:Course)
WHERE c.code IN ['AYP', 'POO', 'BD', 'PROG3', 'AYED2', 'SO', 'RED', 'IA']
RETURN c.code AS code, c.name AS name
ORDER BY c.code;

// Si no ves los 8 cursos, ejecuta primero el seed de cursos

// Crear relaciones RELATED (una sola dirección es suficiente, Neo4j las encuentra en ambas direcciones)
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

// Verificar las relaciones creadas
MATCH (a:Course)-[r:RELATED]-(b:Course)
RETURN a.code AS from, b.code AS to, r.sim AS similarity
ORDER BY a.code, b.code;

