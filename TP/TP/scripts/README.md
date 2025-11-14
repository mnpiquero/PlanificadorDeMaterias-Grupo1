# Scripts de Inicialización

Scripts para cargar datos de prueba y el plan de estudios de Ingeniería en Informática en el sistema.

## 📋 Prerequisitos

- La aplicación debe estar corriendo en http://localhost:8080
- Neo4j debe estar accesible

## 🚀 Uso

### Opción 1: Datos de Prueba (9 materias básicas)

#### Windows (PowerShell)

```powershell
.\init-data.ps1
```

#### Linux / Mac

```bash
chmod +x init-data.sh
./init-data.sh
```

### Opción 2: Plan Completo de Ingeniería en Informática (46 materias)

**✨ Nuevo:** Los scripts ahora usan un enfoque de **dos fases** para evitar nodos con propiedades NULL.

#### Paso 1: Cargar materias y prerequisitos

##### Windows (PowerShell)

```powershell
.\init-data-ingenieria.ps1
```

##### Linux / Mac

```bash
chmod +x init-data-ingenieria.sh
./init-data-ingenieria.sh
```

El script ejecuta:
- **Fase 1**: Crea los 46 cursos con propiedades completas (sin relaciones)
- **Fase 2**: Agrega las relaciones REQUIRES entre cursos

#### Paso 2: Crear relaciones RELATED (necesario para MST)

**⚠️ IMPORTANTE**: Los algoritmos MST (Prim y Kruskal) requieren relaciones RELATED. Ejecuta este script después del paso 1.

##### Windows (PowerShell)

```powershell
.\create-related-ingenieria.ps1
```

##### Linux / Mac

```bash
chmod +x create-related-ingenieria.sh
./create-related-ingenieria.sh
```

Este script crea automáticamente **~57 relaciones RELATED** entre materias relacionadas temáticamente usando el endpoint `/relationships/auto` que calcula la similaridad basándose en créditos, horas y dificultad.

## 📊 Datos que Carga

### Script `init-data` - Datos de Prueba

El script de prueba crea:

#### Materias Base (4)
- MAT101 - Matemática I
- FIS101 - Física I
- ALG101 - Álgebra I
- PRO101 - Programación I

#### Materias con Prerequisitos (5)
- MAT201 - Matemática II (requiere MAT101)
- FIS201 - Física II (requiere FIS101, MAT101)
- ALG201 - Álgebra II (requiere ALG101)
- PRO201 - Programación II (requiere PRO101)
- MAT301 - Matemática III (requiere MAT201)

#### Relaciones RELATED (5)
- MAT101 ↔ FIS101 (similaridad: 0.8)
- MAT101 ↔ ALG101 (similaridad: 0.75)
- ALG101 ↔ PRO101 (similaridad: 0.6)
- MAT201 ↔ FIS201 (similaridad automática)
- PRO101 ↔ PRO201 (similaridad automática)

### Script `init-data-ingenieria` - Plan Completo UADE

El script de Ingeniería en Informática carga el Plan 1621 (Año 2021) con:

#### 📚 Total: 46 Materias

**1° Año** (11 materias)
- 1C: Fundamentos de Informática, Sistemas de Información I, Pensamiento Crítico y Comunicación, Teoría de Sistemas, Elementos de Álgebra y Geometría
- 2C: Programación I, Sistemas de Representación, Fundamentos de Química, Arquitectura de Computadores, Matemática Discreta, Álgebra

**2° Año** (10 materias)
- 1C: Programación II, Sistemas de Información II, Sistemas Operativos, Física I, Cálculo I
- 2C: Programación III, Paradigma Orientado a Objetos, Fundamentos de Telecomunicaciones, Ingeniería de Datos I, Cálculo II

**3° Año** (11 materias)
- 1C: Proceso de Desarrollo de Software, Seminario de Integración Profesional, Teleinformática y Redes, Ingeniería de Datos II, Probabilidad y Estadística, Examen de Inglés
- 🎓 **Título intermedio: Analista en Informática**
- 2C: Aplicaciones Interactivas, Ingeniería de Software, Física II, Teoría de la Computación, Estadística Avanzada

**4° Año** (11 materias)
- 1C: Desarrollo de Aplicaciones I, Dirección de Proyectos Informáticos, Ciencia de Datos, Seguridad e Integridad de la Información, Modelado y Simulación
- 2C: Optativa I, Desarrollo de Aplicaciones II, Evaluación de Proyectos Informáticos, Inteligencia Artificial, Tecnología y Medio Ambiente, Práctica Profesional Supervisada (PPS)

**5° Año** (9 materias)
- 1C: Optativa II, Arquitectura de Aplicaciones, Tendencias Tecnológicas, Proyecto Final de Ingeniería en Informática, Calidad de Software
- 2C: Optativa III, Negocios Tecnológicos, Tecnología e Innovación, Derecho Informático
- 🎓 **Título final: Ingeniero en Informática**

## ✅ Verificación

Después de ejecutar los scripts, puedes verificar:

```bash
# Ver materias
curl http://localhost:8080/courses

# Ver relaciones RELATED
curl http://localhost:8080/relationships

# Ver orden topológico
curl "http://localhost:8080/graph/toposort"

# Probar MST con Prim
curl "http://localhost:8080/graph/mst?algo=prim" | jq

# Probar MST con Kruskal
curl "http://localhost:8080/graph/mst?algo=kruskal" | jq

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

## ⚠️ Solución de Problemas

### Materias con Propiedades NULL

Si después de ejecutar el script ves materias con `name`, `credits`, `hours` o `difficulty` en `null`:

1. **Limpia la base de datos** en Neo4j Browser (http://localhost:7474):
   ```cypher
   MATCH (n) DETACH DELETE n
   ```

2. **Ejecuta el script nuevamente** (ya está corregido para evitar race conditions)

3. **Verifica los datos**:
   ```cypher
   MATCH (c:Course) RETURN c
   ```

📖 **Para más detalles**, consulta [SOLUCION_PROBLEMA_NULL.md](./SOLUCION_PROBLEMA_NULL.md)

### La Aplicación no Responde

Si el script falla con error de conexión:

```bash
# Verifica que los servicios estén corriendo
docker-compose ps

# Si no están corriendo, inícianos
docker-compose up -d

# Espera 10-15 segundos y verifica
curl http://localhost:8080/ping
```

## 🔧 Personalización

Puedes modificar los scripts para cargar tus propios datos. Los scripts están comentados y son fáciles de adaptar.

