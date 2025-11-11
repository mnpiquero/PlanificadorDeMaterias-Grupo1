# 🐳 Guía de Docker para Planificador de Materias

Esta guía te ayudará a ejecutar todo el proyecto usando Docker, sin necesidad de instalar Neo4j o Java localmente.

## 📋 Requisitos Previos

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) instalado
- Docker Compose (incluido en Docker Desktop)

## 🚀 Quick Start

### Opción 1: Todo en Docker (Recomendado)

Ejecuta tanto Neo4j como la aplicación en contenedores:

```bash
# Construir y levantar todos los servicios
docker-compose up -d

# Ver logs en tiempo real
docker-compose logs -f

# Esperar a que todo esté listo (30-40 segundos)
# Verificar que está funcionando
curl http://localhost:8080/ping
```

**¡Listo!** La aplicación estará disponible en:
- API REST: http://localhost:8080
- Neo4j Browser: http://localhost:7474

### Opción 2: Solo Neo4j en Docker (Para Desarrollo)

Si prefieres ejecutar la aplicación localmente con hot-reload pero Neo4j en Docker:

```bash
# Levantar solo Neo4j
docker-compose -f docker-compose.dev.yml up -d

# En otra terminal, ejecutar la aplicación localmente
./mvnw spring-boot:run
```

## 📦 Archivos de Configuración

### `docker-compose.yml`
Configuración completa con Neo4j y la aplicación Spring Boot.

**Servicios:**
- `neo4j`: Base de datos de grafos
- `app`: Aplicación Spring Boot

**Puertos expuestos:**
- `7474`: Neo4j Browser (interfaz web)
- `7687`: Neo4j Bolt (protocolo de conexión)
- `8080`: API REST de la aplicación

### `docker-compose.dev.yml`
Solo Neo4j para desarrollo local.

### `Dockerfile`
Imagen multi-stage para la aplicación:
1. **Build stage**: Compila el proyecto con Maven
2. **Runtime stage**: Imagen ligera con JRE para ejecutar

## 🛠️ Comandos Útiles

### Iniciar servicios
```bash
# Iniciar en background
docker-compose up -d

# Iniciar y ver logs
docker-compose up

# Iniciar solo un servicio
docker-compose up -d neo4j
```

### Ver logs
```bash
# Todos los servicios
docker-compose logs -f

# Solo un servicio
docker-compose logs -f app
docker-compose logs -f neo4j
```

### Detener servicios
```bash
# Detener contenedores (mantiene volúmenes)
docker-compose stop

# Detener y eliminar contenedores
docker-compose down

# Detener y eliminar TODO (incluyendo volúmenes)
docker-compose down -v
```

### Reconstruir la aplicación
```bash
# Si hiciste cambios en el código
docker-compose build app

# Reconstruir sin caché
docker-compose build --no-cache app

# Reconstruir y levantar
docker-compose up -d --build
```

### Estado de servicios
```bash
# Ver servicios corriendo
docker-compose ps

# Ver uso de recursos
docker stats
```

### Acceder a contenedores
```bash
# Shell en el contenedor de la app
docker-compose exec app sh

# Shell en Neo4j
docker-compose exec neo4j bash

# Ejecutar comando en contenedor
docker-compose exec app java -version
```

## 📊 Cargar Datos de Prueba

Una vez que los servicios estén corriendo:

### Windows (PowerShell):
```powershell
.\scripts\init-data.ps1
```

### Linux/Mac:
```bash
chmod +x scripts/init-data.sh
./scripts/init-data.sh
```

O manualmente:
```bash
# Esperar a que la app esté lista
curl http://localhost:8080/ping

# Crear materias
curl -X PUT http://localhost:8080/courses \
  -H "Content-Type: application/json" \
  -d '{"code":"MAT101","name":"Matemática I","credits":6,"hours":8,"difficulty":4,"prereqs":[]}'

# Ver materias
curl http://localhost:8080/courses
```

## 🔍 Acceso a Neo4j Browser

1. Abrir http://localhost:7474 en el navegador
2. Credenciales:
   - **Usuario**: `neo4j`
   - **Password**: `grupo123`
3. Ejecutar queries Cypher:

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

## 🔧 Configuración Avanzada

### Variables de Entorno

Crear un archivo `.env` en la raíz del proyecto:

```env
NEO4J_USER=neo4j
NEO4J_PASSWORD=tu_password_seguro
NEO4J_URI=bolt://neo4j:7687
APP_PORT=8080
NEO4J_HTTP_PORT=7474
NEO4J_BOLT_PORT=7687
```

Docker Compose lo leerá automáticamente.

### Cambiar Puertos

Si tienes conflictos de puertos, edita `docker-compose.yml`:

```yaml
services:
  neo4j:
    ports:
      - "7475:7474"  # Cambiar puerto del browser
      - "7688:7687"  # Cambiar puerto Bolt
  
  app:
    ports:
      - "8081:8080"  # Cambiar puerto de la API
```

### Ajustar Memoria de Neo4j

En `docker-compose.yml`, sección `neo4j` > `environment`:

```yaml
- NEO4J_dbms_memory_pagecache_size=1G
- NEO4J_dbms_memory_heap_initial__size=1G
- NEO4J_dbms_memory_heap_max__size=2G
```

## 📁 Volúmenes y Persistencia

Los datos se guardan en volúmenes Docker:

```bash
# Listar volúmenes
docker volume ls | grep tp

# Inspeccionar volumen
docker volume inspect tp_neo4j_data

# Backup de datos
docker run --rm \
  -v tp_neo4j_data:/data \
  -v $(pwd):/backup \
  alpine tar czf /backup/neo4j-backup.tar.gz -C /data .

# Restaurar datos
docker run --rm \
  -v tp_neo4j_data:/data \
  -v $(pwd):/backup \
  alpine sh -c "cd /data && tar xzf /backup/neo4j-backup.tar.gz"
```

## 🐛 Troubleshooting

### La aplicación no conecta con Neo4j

```bash
# Verificar que Neo4j esté corriendo
docker-compose ps neo4j

# Ver logs de Neo4j
docker-compose logs neo4j

# Verificar health check
docker inspect planificador-neo4j | grep Health -A 10

# Esperar más tiempo (Neo4j tarda ~40s en iniciar)
docker-compose up -d
sleep 45
curl http://localhost:8080/ping
```

### Puerto ya en uso

```bash
# Ver qué proceso usa el puerto
# Windows:
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac:
lsof -i :8080
kill -9 <PID>

# O cambiar puerto en docker-compose.yml
```

### Reconstruir desde cero

```bash
# Eliminar todo y empezar de nuevo
docker-compose down -v
docker system prune -a --volumes
docker-compose up -d --build
```

### Ver logs detallados de la aplicación

```bash
# Logs de Spring Boot
docker-compose logs -f app

# Entrar al contenedor y ver archivos
docker-compose exec app sh
```

### Neo4j no inicia (memoria insuficiente)

Reducir memoria en `docker-compose.yml`:

```yaml
- NEO4J_dbms_memory_pagecache_size=256M
- NEO4J_dbms_memory_heap_initial__size=256M
- NEO4J_dbms_memory_heap_max__size=512M
```

## 🧪 Testing con Docker

```bash
# Ejecutar tests dentro del contenedor
docker-compose run --rm app mvn test

# O construir con tests
docker-compose build --build-arg SKIP_TESTS=false app
```

## 🚀 Despliegue en Producción

### Consideraciones:
1. Cambiar contraseñas por defecto
2. Usar variables de entorno seguras
3. Configurar límites de recursos
4. Habilitar HTTPS
5. Configurar backups automáticos

```yaml
# Ejemplo: Límites de recursos
services:
  app:
    deploy:
      resources:
        limits:
          cpus: '1'
          memory: 1G
        reservations:
          memory: 512M
```

## 📚 Recursos Adicionales

- [Docker Documentation](https://docs.docker.com/)
- [Neo4j Docker Guide](https://neo4j.com/docs/operations-manual/current/docker/)
- [Spring Boot Docker Guide](https://spring.io/guides/gs/spring-boot-docker/)

## 🆘 Ayuda

Si tienes problemas:

1. Revisar logs: `docker-compose logs -f`
2. Verificar estado: `docker-compose ps`
3. Reiniciar servicios: `docker-compose restart`
4. Limpiar y reconstruir: `docker-compose down -v && docker-compose up -d --build`

---

**Última actualización**: 2025-10-26

