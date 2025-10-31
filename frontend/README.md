# Planificador de Materias UADE - Frontend

Frontend profesional para el Planificador de Materias Universitarias con diseño institucional UADE.

## 🚀 Inicio Rápido

### Prerrequisitos

- Node.js 18+ instalado
- Backend corriendo en `http://localhost:8080`

### Instalación

```bash
# Instalar dependencias (si aún no lo hiciste)
npm install

# Iniciar servidor de desarrollo
npm run dev
```

La aplicación estará disponible en: **http://localhost:5173**

### Compilar para producción

```bash
npm run build
```

## 🏗️ Stack Tecnológico

- **React 19** + **TypeScript** + **Vite**
- **TailwindCSS** - Estilos con identidad UADE
- **React Router DOM** - Navegación
- **React Query** - Estado remoto y cache
- **Axios** - Cliente HTTP
- **React Flow** - Visualización de grafos
- **Zustand** - Estado local
- **React Hot Toast** - Notificaciones
- **Lucide React** - Iconos

## 📁 Estructura del Proyecto

```
src/
├── components/        # Componentes reutilizables
│   └── ui/           # Botones, Cards, Badges, Inputs
├── pages/            # Páginas principales
│   ├── Dashboard.tsx
│   ├── Catalog.tsx
│   ├── Graph.tsx
│   ├── Planner.tsx
│   └── Algorithms.tsx
├── layouts/          # Layouts
│   └── MainLayout.tsx
├── services/         # API calls
│   └── api.ts
├── store/            # Zustand stores
│   └── approvedCourses.ts
├── types/            # TypeScript types
│   └── index.ts
└── App.tsx           # Router y configuración
```

## 🎨 Diseño UADE

### Paleta de Colores

- **Azul UADE primario**: `#002B80`
- **Azul secundario**: `#0040BF`
- **Gris claro**: `#F5F6FA`
- **Tipografía**: Montserrat, Poppins

### Componentes de UI

Todos los componentes de UI siguen el sistema de diseño UADE:

- Bordes redondeados (`rounded-xl`)
- Sombras suaves
- Transiciones suaves
- Alto contraste
- Responsive

## 🔌 Integración con Backend

El frontend consume los siguientes endpoints del backend:

- `GET /courses` - Listar materias
- `GET /courses/{code}` - Obtener materia
- `POST /courses` - Crear materia
- `GET /graph/toposort` - Ordenamiento topológico
- `GET /graph/cycles` - Detección de ciclos
- `GET /graph/shortest` - Camino más corto
- `GET /graph/mst` - Árbol de expansión
- `GET /schedule/greedy` - Plan greedy
- `GET /schedule/dp` - Plan DP
- `GET /schedule/bnb` - Plan Branch & Bound

## 📄 Páginas

### Dashboard
Vista general con estadísticas, progreso académico y accesos rápidos.

### Catálogo
Búsqueda y visualización de todas las materias con filtros y marcado de aprobadas.

### Correlativas (Graph)
Visualización interactiva del grafo de prerequisitos usando React Flow.

### Planificador
Generación de planes de cursada optimizados usando algoritmos:
- Greedy
- Programación Dinámica (Knapsack)
- Branch & Bound

### Algoritmos
Ejecución de algoritmos de grafos:
- Toposort
- Detección de ciclos
- MST
- Camino más corto

## 🧪 Scripts Disponibles

```bash
npm run dev      # Servidor de desarrollo
npm run build    # Compilar para producción
npm run preview  # Preview de build
npm run lint     # Linting
```

## 🔧 Variables de Entorno

Crear archivo `.env` en la raíz del proyecto:

```env
VITE_API_URL=http://localhost:8080
```

## 📝 Notas

- Las materias aprobadas se guardan en localStorage
- React Query cachea las respuestas por 5 minutos
- El grafo usa un layout automático básico (mejorable)
- Todos los componentes son responsive

## 🤝 Integración Completa

Para ver el sistema completo funcionando:

1. **Backend**: Seguir instrucciones en `TP/TP/README.md`
2. **Frontend**: `npm run dev` desde esta carpeta
3. Acceder a http://localhost:5173

---

**Diseñado con ❤️ para UADE**
