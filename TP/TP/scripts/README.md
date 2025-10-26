# Scripts de Inicialización

Scripts para cargar datos de prueba en el sistema.

## 📋 Prerequisitos

- La aplicación debe estar corriendo en http://localhost:8080
- Neo4j debe estar accesible

## 🚀 Uso

### Windows (PowerShell)

```powershell
.\init-data.ps1
```

### Linux / Mac

```bash
chmod +x init-data.sh
./init-data.sh
```

## 📊 Datos que Carga

El script crea automáticamente:

### Materias Base (4)
- MAT101 - Matemática I
- FIS101 - Física I
- ALG101 - Álgebra I
- PRO101 - Programación I

### Materias con Prerequisitos (5)
- MAT201 - Matemática II (requiere MAT101)
- FIS201 - Física II (requiere FIS101, MAT101)
- ALG201 - Álgebra II (requiere ALG101)
- PRO201 - Programación II (requiere PRO101)
- MAT301 - Matemática III (requiere MAT201)

### Relaciones RELATED (5)
- MAT101 ↔ FIS101 (similaridad: 0.8)
- MAT101 ↔ ALG101 (similaridad: 0.75)
- ALG101 ↔ PRO101 (similaridad: 0.6)
- MAT201 ↔ FIS201 (similaridad automática)
- PRO101 ↔ PRO201 (similaridad automática)

## ✅ Verificación

Después de ejecutar el script, puedes verificar:

```bash
# Ver materias
curl http://localhost:8080/courses

# Ver relaciones
curl http://localhost:8080/relationships

# Ver orden topológico
curl "http://localhost:8080/graph/toposort"

# Planificación greedy
curl "http://localhost:8080/schedule/greedy?maxHours=20"
```

## 🌐 Neo4j Browser

También puedes verificar en Neo4j Browser (http://localhost:7474):

```cypher
// Ver todas las materias
MATCH (c:Course) RETURN c

// Ver prerequisitos
MATCH (a:Course)-[:REQUIRES]->(b:Course) 
RETURN a.code, a.name, b.code, b.name

// Ver relaciones RELATED
MATCH (a:Course)-[r:RELATED]-(b:Course) 
RETURN a.code, b.code, r.sim
```

## 🔧 Personalización

Puedes modificar los scripts para cargar tus propios datos. Los scripts están comentados y son fáciles de adaptar.

