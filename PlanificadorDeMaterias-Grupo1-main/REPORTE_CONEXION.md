# Reporte de Conexión Backend-Frontend
## Planificador de Materias - Análisis Completo

**Fecha**: 5 de Noviembre, 2025  
**Estado**: En Desarrollo - Feature Branch `conexion-backend`

---

## 📋 Resumen Ejecutivo

Este reporte identifica todas las brechas entre el backend (Spring Boot + Neo4j) y el frontend (React + TypeScript) del sistema Planificador de Materias, proporcionando un plan de acción claro para completar la integración y hacer el proyecto completamente funcional.

### Estado Actual
- ✅ **Backend**: API REST completa con 40+ endpoints y 10 algoritmos implementados
- ⚠️ **Frontend**: UI básica implementada pero muchas funcionalidades no conectadas
- ❌ **Integración**: Sin configuración CORS, manejo incompleto de respuestas, resultados no visualizados

---

## 🔴 Problemas Críticos (Bloqueantes)

### 1. Configuración CORS Faltante en Backend
**Problema**: El frontend no puede comunicarse con el backend debido a políticas CORS.

**Impacto**: 🔴 **CRÍTICO** - Impide toda comunicación.

**Solución Requerida**:
```java
// Crear archivo: TP/TP/src/main/java/com/tp/PlanificadorMat/configuracion/CorsConfig.java
@Configuration
public class CorsConfig {
    @Bean
    public WebFluxConfigurer corsConfigurer() {
        return new WebFluxConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                    .allowedOrigins("http://localhost:5173", "http://localhost:3000")
                    .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true);
            }
        };
    }
}
```

**Archivos a Modificar**:
- `TP/TP/src/main/java/com/tp/PlanificadorMat/configuracion/CorsConfig.java` (crear)

---

### 2. Manejo de Server-Sent Events (SSE) en GET /courses
**Problema**: El endpoint `/courses` retorna `TEXT_EVENT_STREAM` pero el frontend hace un request normal.

**Impacto**: 🔴 **CRÍTICO** - El catálogo de materias no carga correctamente.

**Código Actual (Backend)**:
```java
@GetMapping(value={"","/"}, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
Flux<Course> all(){ return repo.findAll(); }
```

**Solución Requerida en Frontend**:
```typescript
// Opción 1: Cambiar el frontend para manejar SSE
export const coursesApi = {
  getAll: async () => {
    const response = await fetch(`${API_URL}/courses`, {
      headers: { 'Accept': 'text/event-stream' }
    });
    // Parsear SSE...
  }
}

// Opción 2 (RECOMENDADA): Agregar endpoint alternativo en backend
@GetMapping(value="/list", produces = MediaType.APPLICATION_JSON_VALUE)
Flux<Course> allList(){ return repo.findAll(); }
```

**Archivos a Modificar**:
- **Opción Recomendada**: `TP/TP/src/main/java/com/tp/PlanificadorMat/controllers/CourseController.java`
- **Alternativa**: `frontend/src/services/api.ts`

---

### 3. Parámetros Array en Requests GET
**Problema**: Los endpoints que reciben `List<String>` no manejan correctamente arrays desde el frontend.

**Ejemplos Afectados**:
- `/graph/toposort?approved=MAT101&approved=FIS101`
- `/schedule/available?approved=MAT101&approved=FIS101`
- `/schedule/greedy?approved=MAT101&approved=FIS101`

**Solución Requerida en Frontend**:
```typescript
// frontend/src/services/api.ts

export const graphApi = {
  topoSort: (approved?: string[]) => {
    const params = new URLSearchParams();
    approved?.forEach(code => params.append('approved', code));
    return api.get<string[]>(`/graph/toposort?${params.toString()}`).then(res => res.data);
  },
};

export const scheduleApi = {
  available: (approved?: string[]) => {
    const params = new URLSearchParams();
    approved?.forEach(code => params.append('approved', code));
    return api.get<Course[]>(`/schedule/available?${params.toString()}`).then(res => res.data);
  },
  
  greedy: (approved: string[] = [], value: 'credits' | 'difficulty' | 'hours' = 'credits', maxHours = 24) => {
    const params = new URLSearchParams();
    approved.forEach(code => params.append('approved', code));
    params.append('value', value);
    params.append('maxHours', maxHours.toString());
    return api.get<Course[]>(`/schedule/greedy?${params.toString()}`).then(res => res.data);
  },
  
  dp: (approved: string[] = [], value: 'credits' | 'difficulty' | 'hours' = 'credits', maxHours = 24) => {
    const params = new URLSearchParams();
    approved.forEach(code => params.append('approved', code));
    params.append('value', value);
    params.append('maxHours', maxHours.toString());
    return api.get<Course[]>(`/schedule/dp?${params.toString()}`).then(res => res.data);
  },
  
  branchAndBound: (approved: string[] = [], semesters = 4, maxHours = 24) => {
    const params = new URLSearchParams();
    approved.forEach(code => params.append('approved', code));
    params.append('semesters', semesters.toString());
    params.append('maxHours', maxHours.toString());
    return api.get<string[][]>(`/schedule/bnb?${params.toString()}`).then(res => res.data);
  },
};
```

**Archivos a Modificar**:
- `frontend/src/services/api.ts`

---

## 🟠 Problemas Importantes (Alta Prioridad)

### 4. Dashboard: No Calcula Materias Disponibles con el Backend
**Problema**: El cálculo de materias disponibles se hace en el frontend, pero existe `/schedule/available`.

**Código Actual**:
```typescript
const available = courses.filter(c => {
  if (!c.prereqs || c.prereqs.length === 0) return true;
  return c.prereqs.every(p => approvedCodes.includes(p.code));
}).length;
```

**Solución Requerida**:
```typescript
// frontend/src/pages/Dashboard.tsx
const { data: availableCourses = [], isLoading: loadingAvailable } = useQuery({
  queryKey: ['available-courses', approvedCodes],
  queryFn: () => scheduleApi.available(Array.from(approvedCodes)),
  enabled: courses.length > 0,
});

const stats = {
  total: courses.length,
  approved: approvedCodes.length,
  remaining: courses.length - approvedCodes.length,
  available: availableCourses.length,
};
```

**Archivos a Modificar**:
- `frontend/src/pages/Dashboard.tsx`

---

### 5. Página Algorithms: Solo Muestra Toasts, No Visualiza Resultados
**Problema**: Los algoritmos se ejecutan pero los resultados no se muestran al usuario.

**Funcionalidades Faltantes**:
- ❌ Visualización de orden topológico
- ❌ Visualización de caminos más cortos
- ❌ Visualización de MST
- ❌ Visualización de DFS/BFS
- ❌ Resultados de backtracking

**Solución Requerida**: Crear componentes de visualización para cada algoritmo.

**Ejemplo para Toposort**:
```typescript
// frontend/src/pages/Algorithms.tsx
const [topoResult, setTopoResult] = useState<string[]>([]);

const handleTopoSort = async () => {
  try {
    const result = await graphApi.topoSort(Array.from(approvedCodes));
    setTopoResult(result);
    toast.success(`Orden calculado con ${result.length} materias`);
  } catch (error) {
    toast.error('Error al calcular ordenamiento');
  }
};

// Agregar sección de resultados en el JSX
{topoResult.length > 0 && (
  <Card>
    <CardHeader title="Ordenamiento Topológico" />
    <div className="space-y-2">
      {topoResult.map((code, idx) => (
        <div key={code} className="flex items-center gap-3">
          <Badge variant="primary">{idx + 1}</Badge>
          <span className="font-medium">{code}</span>
        </div>
      ))}
    </div>
  </Card>
)}
```

**Archivos a Modificar**:
- `frontend/src/pages/Algorithms.tsx`

---

### 6. Página Graph: No Usa Algoritmos del Backend para Layout
**Problema**: El grafo usa un layout muy básico y no aprovecha DFS/BFS del backend.

**Solución Requerida**: Usar BFS layers del backend para posicionar nodos por niveles.

```typescript
// frontend/src/pages/Graph.tsx
const [layoutAlgorithm, setLayoutAlgorithm] = useState<'manual' | 'bfs'>('manual');
const [bfsRoot, setBfsRoot] = useState<string>('');

const { data: bfsLayers, refetch: refetchBfs } = useQuery({
  queryKey: ['bfs-layers', bfsRoot],
  queryFn: () => graphApi.bfsLayers(bfsRoot),
  enabled: layoutAlgorithm === 'bfs' && !!bfsRoot,
});

// Usar bfsLayers para posicionar nodos
useEffect(() => {
  if (bfsLayers && layoutAlgorithm === 'bfs') {
    const layerHeight = 200;
    const nodeSpacing = 150;
    
    bfsLayers.forEach((layer, layerIdx) => {
      layer.forEach((nodeId, nodeIdx) => {
        const node = nodesMap.get(nodeId);
        if (node) {
          node.position = {
            x: nodeIdx * nodeSpacing,
            y: layerIdx * layerHeight,
          };
        }
      });
    });
  }
}, [bfsLayers, layoutAlgorithm]);
```

**Archivos a Modificar**:
- `frontend/src/pages/Graph.tsx`

---

### 7. Página Planner: BnB Muestra Solo Primer Cuatrimestre
**Problema**: Branch & Bound retorna un plan completo de múltiples cuatrimestres pero solo se muestra el primero.

**Código Actual**:
```typescript
case 'bnb':
  const bnbResult = await scheduleApi.branchAndBound(approvedList, 4, maxHours);
  // Solo usa el primer cuatrimestre
  const firstSemester = bnbResult[0] || [];
```

**Solución Requerida**: Crear vista de múltiples cuatrimestres.

```typescript
// frontend/src/pages/Planner.tsx
const [multiSemesterResults, setMultiSemesterResults] = useState<Course[][]>([]);
const [viewMode, setViewMode] = useState<'single' | 'multi'>('single');

// En el handler de BnB
case 'bnb':
  const bnbResult = await scheduleApi.branchAndBound(approvedList, semesters, maxHours);
  const allSemesters = bnbResult.map(semester => 
    semester.map(code => courses.find(c => c.code === code)).filter(Boolean) as Course[]
  );
  setMultiSemesterResults(allSemesters);
  setViewMode('multi');
  break;

// JSX para mostrar múltiples cuatrimestres
{viewMode === 'multi' && multiSemesterResults.length > 0 && (
  <div className="space-y-6">
    {multiSemesterResults.map((semester, idx) => (
      <Card key={idx}>
        <CardHeader title={`Cuatrimestre ${idx + 1}`} />
        {/* Mostrar materias del cuatrimestre */}
      </Card>
    ))}
  </div>
)}
```

**Archivos a Modificar**:
- `frontend/src/pages/Planner.tsx`

---

### 8. Falta Página de Gestión de Relaciones (RELATED)
**Problema**: El backend tiene endpoints completos para relaciones RELATED pero no hay UI.

**Funcionalidades Requeridas**:
- Ver todas las relaciones RELATED
- Crear nueva relación con similaridad manual
- Crear relación con similaridad automática
- Editar similaridad de relación existente
- Eliminar relación
- Ver materias relacionadas a una materia específica

**Solución Requerida**: Crear nueva página.

```typescript
// frontend/src/pages/Relationships.tsx (CREAR)
import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { relationshipsApi, coursesApi } from '../services/api';
import { Card, CardHeader } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Badge } from '../components/ui/Badge';
import { toast } from 'react-hot-toast';

export default function Relationships() {
  const [fromCode, setFromCode] = useState('');
  const [toCode, setToCode] = useState('');
  const [similarity, setSimilarity] = useState(0.5);
  const [autoMode, setAutoMode] = useState(true);
  
  const queryClient = useQueryClient();
  
  const { data: relationships = [], isLoading } = useQuery({
    queryKey: ['relationships'],
    queryFn: relationshipsApi.getAll,
  });
  
  const createMutation = useMutation({
    mutationFn: autoMode 
      ? () => relationshipsApi.createAuto({ fromCode, toCode })
      : () => relationshipsApi.create({ fromCode, toCode, similarity }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['relationships'] });
      toast.success('Relación creada');
      setFromCode('');
      setToCode('');
    },
    onError: () => toast.error('Error al crear relación'),
  });
  
  const deleteMutation = useMutation({
    mutationFn: (data: { from: string; to: string }) => 
      relationshipsApi.delete(data.from, data.to),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['relationships'] });
      toast.success('Relación eliminada');
    },
    onError: () => toast.error('Error al eliminar relación'),
  });
  
  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-3xl font-bold text-gray-900 mb-2">
          Relaciones RELATED
        </h2>
        <p className="text-gray-600">
          Gestiona relaciones de similaridad entre materias
        </p>
      </div>
      
      {/* Form para crear relación */}
      <Card>
        <CardHeader title="Crear Nueva Relación" />
        <div className="space-y-4">
          <div className="flex items-center gap-4">
            <label className="flex items-center gap-2">
              <input
                type="radio"
                checked={autoMode}
                onChange={() => setAutoMode(true)}
              />
              <span>Cálculo Automático</span>
            </label>
            <label className="flex items-center gap-2">
              <input
                type="radio"
                checked={!autoMode}
                onChange={() => setAutoMode(false)}
              />
              <span>Similaridad Manual</span>
            </label>
          </div>
          
          <div className="grid grid-cols-2 gap-4">
            <Input
              label="Desde"
              placeholder="Código materia"
              value={fromCode}
              onChange={(e) => setFromCode(e.target.value.toUpperCase())}
            />
            <Input
              label="Hasta"
              placeholder="Código materia"
              value={toCode}
              onChange={(e) => setToCode(e.target.value.toUpperCase())}
            />
          </div>
          
          {!autoMode && (
            <Input
              label="Similaridad (0-1)"
              type="number"
              min="0"
              max="1"
              step="0.1"
              value={similarity}
              onChange={(e) => setSimilarity(parseFloat(e.target.value))}
            />
          )}
          
          <Button
            variant="primary"
            onClick={() => createMutation.mutate()}
            disabled={!fromCode || !toCode}
            className="w-full"
          >
            Crear Relación
          </Button>
        </div>
      </Card>
      
      {/* Lista de relaciones */}
      <Card>
        <CardHeader 
          title="Relaciones Existentes" 
          subtitle={`${relationships.length} relaciones`}
        />
        <div className="space-y-2">
          {isLoading ? (
            <p className="text-gray-500">Cargando...</p>
          ) : relationships.length === 0 ? (
            <p className="text-gray-500">No hay relaciones creadas</p>
          ) : (
            relationships.map((rel: any, idx: number) => (
              <div
                key={idx}
                className="flex items-center justify-between p-4 bg-gray-50 rounded-xl"
              >
                <div className="flex items-center gap-4">
                  <Badge variant="primary">{rel.from}</Badge>
                  <span className="text-gray-400">→</span>
                  <Badge variant="primary">{rel.to}</Badge>
                  <span className="text-sm text-gray-600">
                    Similaridad: {(rel.similarity || 0).toFixed(2)}
                  </span>
                </div>
                <Button
                  variant="danger"
                  size="sm"
                  onClick={() => deleteMutation.mutate({ from: rel.from, to: rel.to })}
                >
                  Eliminar
                </Button>
              </div>
            ))
          )}
        </div>
      </Card>
    </div>
  );
}
```

**Archivos a Crear**:
- `frontend/src/pages/Relationships.tsx`

**Archivos a Modificar**:
- `frontend/src/App.tsx` (agregar ruta)

---

### 9. Falta CRUD de Materias en el Catálogo
**Problema**: El catálogo solo muestra materias, no permite crear/editar/eliminar.

**Funcionalidades Faltantes**:
- ❌ Crear nueva materia
- ❌ Editar materia existente
- ❌ Eliminar materia
- ❌ Agregar/quitar prerequisitos

**Solución Requerida**: Agregar modales/formularios en la página Catalog.

```typescript
// frontend/src/pages/Catalog.tsx

// Agregar estados
const [showCreateModal, setShowCreateModal] = useState(false);
const [editingCourse, setEditingCourse] = useState<Course | null>(null);

// Mutation para crear
const createMutation = useMutation({
  mutationFn: (course: Course) => coursesApi.create(course),
  onSuccess: () => {
    queryClient.invalidateQueries({ queryKey: ['courses'] });
    toast.success('Materia creada');
    setShowCreateModal(false);
  },
  onError: () => toast.error('Error al crear materia'),
});

// Mutation para eliminar
const deleteMutation = useMutation({
  mutationFn: (code: string) => coursesApi.delete(code),
  onSuccess: () => {
    queryClient.invalidateQueries({ queryKey: ['courses'] });
    toast.success('Materia eliminada');
  },
  onError: () => toast.error('Error al eliminar materia'),
});

// Agregar botón "Nueva Materia" en el header
// Agregar modal con formulario
// Agregar botones editar/eliminar en cada CourseCard
```

**Archivos a Modificar**:
- `frontend/src/pages/Catalog.tsx`

**Componentes a Crear**:
- `frontend/src/components/CourseFormModal.tsx`
- `frontend/src/components/DeleteConfirmModal.tsx`

---

### 10. Falta Endpoint en API para Agregar Prerequisitos
**Problema**: El backend tiene `POST /courses/{code}/prereqs` pero no está en api.ts del frontend.

**Solución Requerida**:
```typescript
// frontend/src/services/api.ts

export const coursesApi = {
  // ... métodos existentes
  
  addPrereqs: (code: string, prereqCodes: string[]) => 
    api.post<Course>(`/courses/${code}/prereqs`, prereqCodes).then(res => res.data),
};
```

**Archivos a Modificar**:
- `frontend/src/services/api.ts`

---

## 🟡 Mejoras Recomendadas (Media Prioridad)

### 11. Manejo de Errores Inconsistente
**Problema**: Algunos componentes manejan errores, otros no. No hay componente global de error.

**Solución Requerida**:
```typescript
// frontend/src/components/ErrorBoundary.tsx (CREAR)
import { Component, ReactNode } from 'react';

interface Props {
  children: ReactNode;
}

interface State {
  hasError: boolean;
  error?: Error;
}

export class ErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen flex items-center justify-center bg-gray-50">
          <div className="text-center">
            <h1 className="text-4xl font-bold text-gray-900 mb-4">
              Algo salió mal
            </h1>
            <p className="text-gray-600 mb-6">
              {this.state.error?.message || 'Error desconocido'}
            </p>
            <button
              onClick={() => window.location.reload()}
              className="px-6 py-3 bg-uade-primary text-white rounded-xl"
            >
              Recargar página
            </button>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}
```

**Archivos a Crear**:
- `frontend/src/components/ErrorBoundary.tsx`

**Archivos a Modificar**:
- `frontend/src/App.tsx` (envolver con ErrorBoundary)

---

### 12. Loading States Inconsistentes
**Problema**: Algunos componentes muestran loading, otros no. No hay componente reutilizable.

**Solución Requerida**:
```typescript
// frontend/src/components/LoadingSpinner.tsx (CREAR)
export function LoadingSpinner({ size = 'md' }: { size?: 'sm' | 'md' | 'lg' }) {
  const sizeClasses = {
    sm: 'w-4 h-4',
    md: 'w-8 h-8',
    lg: 'w-12 h-12',
  };
  
  return (
    <div className="flex justify-center items-center py-8">
      <div
        className={`${sizeClasses[size]} border-4 border-gray-200 border-t-uade-primary rounded-full animate-spin`}
      ></div>
    </div>
  );
}

// frontend/src/components/LoadingPage.tsx (CREAR)
export function LoadingPage({ message = 'Cargando...' }: { message?: string }) {
  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-gray-50">
      <LoadingSpinner size="lg" />
      <p className="mt-4 text-gray-600">{message}</p>
    </div>
  );
}
```

**Archivos a Crear**:
- `frontend/src/components/LoadingSpinner.tsx`
- `frontend/src/components/LoadingPage.tsx`

---

### 13. Búsqueda Avanzada No Implementada en Frontend
**Problema**: El backend tiene `/courses/search/advanced` pero el frontend no lo usa.

**Solución Requerida**: Agregar formulario de búsqueda avanzada en Catalog.

```typescript
// frontend/src/pages/Catalog.tsx

const [advancedSearch, setAdvancedSearch] = useState<SearchCriteria>({});

const { data: searchResults, refetch: searchAdvanced } = useQuery({
  queryKey: ['advanced-search', advancedSearch],
  queryFn: () => coursesApi.advancedSearch(advancedSearch),
  enabled: false, // Solo ejecutar manualmente
});

// Agregar formulario de búsqueda avanzada cuando showFilters es true
{showFilters && (
  <Card>
    <div className="grid grid-cols-2 gap-4">
      <Input
        label="Créditos mínimos"
        type="number"
        value={advancedSearch.minCredits || ''}
        onChange={(e) => setAdvancedSearch({
          ...advancedSearch,
          minCredits: parseInt(e.target.value) || undefined,
        })}
      />
      {/* Más campos... */}
    </div>
    <Button onClick={() => searchAdvanced()}>Buscar</Button>
  </Card>
)}
```

**Archivos a Modificar**:
- `frontend/src/pages/Catalog.tsx`

---

### 14. No Hay Persistencia de Materias Aprobadas en Backend
**Problema**: Las materias aprobadas solo se guardan en localStorage del navegador.

**Impacto**: Los datos se pierden si el usuario cambia de dispositivo o navegador.

**Solución Requerida en Backend**: Crear endpoint para guardar/recuperar estado del estudiante.

```java
// TP/TP/src/main/java/com/tp/PlanificadorMat/modelo/Student.java (CREAR)
@Node
public class Student {
    @Id
    private String id;
    private String name;
    
    @Relationship(type = "APPROVED", direction = Relationship.Direction.OUTGOING)
    private Set<Course> approvedCourses;
    
    // Getters, setters, constructors
}

// TP/TP/src/main/java/com/tp/PlanificadorMat/controllers/StudentController.java (CREAR)
@RestController
@RequestMapping("/students")
public class StudentController {
    
    @PostMapping("/{id}/approve/{courseCode}")
    public Mono<Student> approveCourse(@PathVariable String id, @PathVariable String courseCode) {
        // Implementación
    }
    
    @DeleteMapping("/{id}/approve/{courseCode}")
    public Mono<Student> removeApprovedCourse(@PathVariable String id, @PathVariable String courseCode) {
        // Implementación
    }
    
    @GetMapping("/{id}/approved")
    public Flux<Course> getApprovedCourses(@PathVariable String id) {
        // Implementación
    }
}
```

**Archivos a Crear**:
- `TP/TP/src/main/java/com/tp/PlanificadorMat/modelo/Student.java`
- `TP/TP/src/main/java/com/tp/PlanificadorMat/repositorio/StudentRepository.java`
- `TP/TP/src/main/java/com/tp/PlanificadorMat/controllers/StudentController.java`

**Archivos a Modificar**:
- `frontend/src/store/approvedCourses.ts` (sincronizar con backend)
- `frontend/src/services/api.ts` (agregar studentApi)

---

### 15. Variables de Entorno No Configuradas
**Problema**: La URL del API está hardcoded con fallback.

**Solución Requerida**:

```bash
# frontend/.env.development (CREAR)
VITE_API_URL=http://localhost:8080

# frontend/.env.production (CREAR)
VITE_API_URL=https://api-planificador.tudominio.com
```

```typescript
// frontend/src/services/api.ts
const API_URL = import.meta.env.VITE_API_URL;

if (!API_URL) {
  throw new Error('VITE_API_URL no está configurada');
}
```

**Archivos a Crear**:
- `frontend/.env.development`
- `frontend/.env.production`
- `frontend/.env.example`

**Archivos a Modificar**:
- `frontend/src/services/api.ts`

---

### 16. Visualización de Resultados de Backtracking
**Problema**: El endpoint `/schedule/backtracking` existe pero no se usa en el frontend.

**Solución Requerida**: Agregar sección en la página Algorithms o Planner.

```typescript
// frontend/src/pages/Algorithms.tsx o Planner.tsx

const [backtrackingResults, setBacktrackingResults] = useState<string[][]>([]);
const [btFrom, setBtFrom] = useState('');
const [btTo, setBtTo] = useState('');
const [maxDepth, setMaxDepth] = useState(10);

const handleBacktracking = async () => {
  try {
    toast.loading('Calculando rutas...');
    const result = await scheduleApi.backtracking(btFrom, btTo, maxDepth);
    setBacktrackingResults(result);
    toast.success(`Se encontraron ${result.length} rutas posibles`);
  } catch (error) {
    toast.error('Error al calcular rutas');
  } finally {
    toast.dismiss();
  }
};

// JSX
<Card>
  <CardHeader title="Backtracking - Todas las Rutas Posibles" />
  <div className="space-y-4">
    <div className="grid grid-cols-3 gap-4">
      <Input label="Desde" value={btFrom} onChange={(e) => setBtFrom(e.target.value.toUpperCase())} />
      <Input label="Hasta" value={btTo} onChange={(e) => setBtTo(e.target.value.toUpperCase())} />
      <Input label="Profundidad máxima" type="number" value={maxDepth} onChange={(e) => setMaxDepth(parseInt(e.target.value))} />
    </div>
    <Button variant="primary" onClick={handleBacktracking} className="w-full">
      Encontrar Todas las Rutas
    </Button>
    
    {backtrackingResults.length > 0 && (
      <div className="space-y-3">
        <h4 className="font-semibold">Rutas Encontradas ({backtrackingResults.length})</h4>
        {backtrackingResults.map((path, idx) => (
          <div key={idx} className="flex items-center gap-2 p-3 bg-gray-50 rounded-lg">
            <Badge variant="secondary">Ruta {idx + 1}</Badge>
            <div className="flex items-center gap-2 flex-wrap">
              {path.map((code, i) => (
                <>
                  <Badge key={code} variant="primary">{code}</Badge>
                  {i < path.length - 1 && <span className="text-gray-400">→</span>}
                </>
              ))}
            </div>
          </div>
        ))}
      </div>
    )}
  </div>
</Card>
```

**Archivos a Modificar**:
- `frontend/src/pages/Algorithms.tsx` o `frontend/src/pages/Planner.tsx`

---

### 17. Visualización de DFS y BFS
**Problema**: Los endpoints existen pero no se usan.

**Solución Requerida**: Agregar en la página Algorithms.

```typescript
// Similar al backtracking, agregar secciones para:
// - DFS: mostrar orden de visita
// - BFS: mostrar capas/niveles
```

**Archivos a Modificar**:
- `frontend/src/pages/Algorithms.tsx`

---

## 🟢 Mejoras Opcionales (Baja Prioridad)

### 18. Testing
**Problema**: No hay tests de integración ni unitarios.

**Solución Recomendada**:
- Backend: Tests con JUnit y MockMvc
- Frontend: Tests con Vitest y React Testing Library

---

### 19. Documentación de API
**Problema**: No hay Swagger/OpenAPI configurado.

**Solución Recomendada**: Agregar SpringDoc OpenAPI.

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-webflux-ui</artifactId>
    <version>1.7.0</version>
</dependency>
```

---

### 20. Modo Oscuro en Frontend
**Problema**: No hay tema oscuro.

**Solución Recomendada**: Implementar con Tailwind dark mode.

---

### 21. Internacionalización (i18n)
**Problema**: Todo está en español hardcoded.

**Solución Recomendada**: Usar react-i18next.

---

### 22. Exportar Resultados
**Problema**: No se pueden exportar planes de cursada o resultados de algoritmos.

**Solución Recomendada**: Agregar botones de exportación a PDF/CSV.

---

## 📊 Resumen de Prioridades

### 🔴 Crítico (Debe Hacerse Ya)
1. Configurar CORS en backend
2. Solucionar manejo de SSE en `/courses`
3. Corregir parámetros array en requests GET

**Tiempo estimado**: 2-3 horas

---

### 🟠 Alta Prioridad (Esta Semana)
4. Dashboard: calcular disponibles con backend
5. Algorithms: visualizar todos los resultados
6. Graph: usar algoritmos del backend para layout
7. Planner: mostrar múltiples cuatrimestres en BnB
8. Crear página de gestión de Relationships
9. Agregar CRUD de materias en Catalog
10. Agregar endpoint de prereqs en api.ts

**Tiempo estimado**: 15-20 horas

---

### 🟡 Media Prioridad (Próximas 2 Semanas)
11. Error boundary y manejo de errores consistente
12. Loading states consistentes
13. Implementar búsqueda avanzada en frontend
14. Persistencia de materias aprobadas en backend
15. Configurar variables de entorno
16. Visualización de backtracking
17. Visualización de DFS/BFS

**Tiempo estimado**: 10-15 horas

---

### 🟢 Baja Prioridad (Futuro)
18. Testing
19. Documentación API (Swagger)
20. Modo oscuro
21. i18n
22. Exportar resultados

**Tiempo estimado**: 20+ horas

---

## 🛠️ Plan de Acción Recomendado

### Fase 1: Conexión Básica (Día 1)
1. ✅ Configurar CORS
2. ✅ Solucionar SSE o agregar endpoint `/courses/list`
3. ✅ Corregir parámetros array

**Resultado**: Frontend puede comunicarse con backend sin errores.

---

### Fase 2: Visualizaciones Esenciales (Días 2-3)
4. ✅ Dashboard con datos reales
5. ✅ Algorithms con resultados visualizados
6. ✅ Planner con múltiples cuatrimestres

**Resultado**: Todas las páginas existentes funcionan completamente.

---

### Fase 3: Funcionalidades Faltantes (Días 4-5)
7. ✅ Página de Relationships
8. ✅ CRUD de materias
9. ✅ Búsqueda avanzada
10. ✅ Backtracking/DFS/BFS en UI

**Resultado**: Todas las capacidades del backend son accesibles desde el frontend.

---

### Fase 4: Robustez (Días 6-7)
11. ✅ Error handling consistente
12. ✅ Loading states
13. ✅ Variables de entorno
14. ✅ Persistencia en backend

**Resultado**: Aplicación robusta y lista para producción.

---

### Fase 5: Mejoras (Opcional)
15. Testing
16. Documentación
17. Features adicionales

**Resultado**: Aplicación profesional y mantenible.

---

## 📁 Archivos por Crear

### Backend
1. `TP/TP/src/main/java/com/tp/PlanificadorMat/configuracion/CorsConfig.java`
2. `TP/TP/src/main/java/com/tp/PlanificadorMat/modelo/Student.java`
3. `TP/TP/src/main/java/com/tp/PlanificadorMat/repositorio/StudentRepository.java`
4. `TP/TP/src/main/java/com/tp/PlanificadorMat/controllers/StudentController.java`

### Frontend
1. `frontend/src/pages/Relationships.tsx`
2. `frontend/src/components/CourseFormModal.tsx`
3. `frontend/src/components/DeleteConfirmModal.tsx`
4. `frontend/src/components/ErrorBoundary.tsx`
5. `frontend/src/components/LoadingSpinner.tsx`
6. `frontend/src/components/LoadingPage.tsx`
7. `frontend/.env.development`
8. `frontend/.env.production`
9. `frontend/.env.example`

### Documentación
1. `TESTING.md`
2. `DEPLOYMENT.md`
3. `CONTRIBUTING.md`

---

## 📝 Archivos a Modificar

### Backend
1. `TP/TP/src/main/java/com/tp/PlanificadorMat/controllers/CourseController.java` (agregar `/list` endpoint)

### Frontend
1. `frontend/src/services/api.ts` (corregir parámetros array, agregar endpoints faltantes)
2. `frontend/src/pages/Dashboard.tsx` (usar `/schedule/available`)
3. `frontend/src/pages/Catalog.tsx` (CRUD y búsqueda avanzada)
4. `frontend/src/pages/Graph.tsx` (usar BFS para layout)
5. `frontend/src/pages/Planner.tsx` (mostrar múltiples cuatrimestres)
6. `frontend/src/pages/Algorithms.tsx` (visualizar todos los resultados)
7. `frontend/src/App.tsx` (agregar ruta de Relationships, ErrorBoundary)
8. `frontend/src/store/approvedCourses.ts` (opcional: sincronizar con backend)

---

## 🎯 Métricas de Éxito

### Funcionalidad
- ✅ Todas las páginas cargan sin errores de red
- ✅ Todos los endpoints del backend son accesibles desde el frontend
- ✅ Todos los algoritmos muestran sus resultados
- ✅ CRUD completo de materias y relaciones

### Usabilidad
- ✅ Loading states en todas las operaciones asíncronas
- ✅ Mensajes de error claros y útiles
- ✅ Feedback visual de todas las acciones
- ✅ UI responsiva en móvil, tablet y desktop

### Rendimiento
- ✅ Tiempo de carga inicial < 3 segundos
- ✅ Operaciones de algoritmos < 5 segundos
- ✅ Sin memory leaks
- ✅ Imágenes y assets optimizados

### Calidad
- ✅ Sin errores en consola del navegador
- ✅ Sin errores en logs del backend
- ✅ TypeScript sin errores (strict mode)
- ✅ Linting sin warnings

---

## 🚀 Comandos para Desarrollo

### Backend
```bash
cd TP/TP
./mvnw spring-boot:run
# API disponible en http://localhost:8080
```

### Frontend
```bash
cd frontend
npm install
npm run dev
# UI disponible en http://localhost:5173
```

### Base de Datos
```bash
cd TP/TP
docker-compose up -d
# Neo4j disponible en http://localhost:7474
```

### Scripts de Datos
```bash
cd TP/TP/scripts
./init-data-ingenieria.sh  # Cargar datos de Ingeniería
```

---

## 📚 Referencias

- [API Documentation](./TP/TP/API_Y_ALGORITMOS.md)
- [Scripts Examples](./TP/TP/scripts/EJEMPLOS_USO.md)
- [Docker Setup](./TP/TP/DOCKER.md)
- [Documentation](./TP/TP/DOCUMENTACION.md)

---

## 👥 Contacto y Soporte

Para preguntas o problemas durante la implementación:
- Revisar la documentación del API
- Consultar ejemplos de uso en `/scripts/EJEMPLOS_USO.md`
- Verificar logs del backend y consola del frontend

---

**Última actualización**: 5 de Noviembre, 2025  
**Versión del documento**: 1.0  
**Estado del proyecto**: En desarrollo activo - Branch `feature/conexion-backend`

