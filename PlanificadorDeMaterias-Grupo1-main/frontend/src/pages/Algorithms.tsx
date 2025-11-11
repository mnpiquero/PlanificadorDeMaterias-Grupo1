import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Card, CardHeader } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Badge } from '../components/ui/Badge';
import { graphApi, coursesApi, scheduleApi } from '../services/api';
import { useApprovedCourses } from '../store/approvedCourses';
import { Search, TrendingUp, Layers, GitMerge, Route, ArrowRight } from 'lucide-react';
import { toast } from 'react-hot-toast';
import type { MSTEdge } from '../types';

export default function Algorithms() {
  const { approvedCodes } = useApprovedCourses();
  const [fromCode, setFromCode] = useState('');
  const [toCode, setToCode] = useState('');
  
  // Estados para resultados
  const [topoResult, setTopoResult] = useState<string[]>([]);
  const [shortestPathResult, setShortestPathResult] = useState<string[]>([]);
  const [mstResult, setMstResult] = useState<MSTEdge[]>([]);
  const [dfsResult, setDfsResult] = useState<string[]>([]);
  const [bfsResult, setBfsResult] = useState<string[][]>([]);
  const [backtrackingResult, setBacktrackingResult] = useState<string[][]>([]);
  
  const [dfsFrom, setDfsFrom] = useState('');
  const [bfsFrom, setBfsFrom] = useState('');
  const [btMaxDepth, setBtMaxDepth] = useState(10);

  const { data: courses = [] } = useQuery({
    queryKey: ['courses'],
    queryFn: coursesApi.getAll,
  });

  const handleTopoSort = async () => {
    try {
      toast.loading('Calculando ordenamiento topológico...');
      const result = await graphApi.topoSort(Array.from(approvedCodes));
      setTopoResult(result);
      toast.success(`Orden calculado con ${result.length} materias`);
    } catch (error) {
      toast.error('Error al calcular ordenamiento');
      console.error(error);
    } finally {
      toast.dismiss();
    }
  };

  const handleCheckCycles = async () => {
    try {
      toast.loading('Verificando ciclos...');
      const result = await graphApi.hasCycles();
      if (result.hasCycle) {
        toast.error('Se detectaron ciclos en el grafo');
      } else {
        toast.success('No se detectaron ciclos');
      }
    } catch (error) {
      toast.error('Error al verificar ciclos');
      console.error(error);
    } finally {
      toast.dismiss();
    }
  };

  const handleShortestPath = async () => {
    if (!fromCode || !toCode) {
      toast.error('Ingresa ambas materias');
      return;
    }
    try {
      toast.loading('Calculando camino más corto...');
      const result = await graphApi.shortestPath(fromCode, toCode);
      setShortestPathResult(result);
      toast.success(`Camino encontrado: ${result.length} materias`);
    } catch (error) {
      toast.error('No se encontró camino');
      console.error(error);
      setShortestPathResult([]);
    } finally {
      toast.dismiss();
    }
  };

  const handleMST = async () => {
    try {
      toast.loading('Calculando MST...');
      const result = await graphApi.mst('prim');
      setMstResult(result);
      toast.success(`MST calculado con ${result.length} aristas`);
    } catch (error) {
      toast.error('Error al calcular MST');
      console.error(error);
    } finally {
      toast.dismiss();
    }
  };

  const handleDFS = async () => {
    if (!dfsFrom) {
      toast.error('Ingresa una materia de inicio');
      return;
    }
    try {
      toast.loading('Ejecutando DFS...');
      const result = await graphApi.dfs(dfsFrom);
      setDfsResult(result);
      toast.success(`DFS completado: ${result.length} materias visitadas`);
    } catch (error) {
      toast.error('Error al ejecutar DFS');
      console.error(error);
    } finally {
      toast.dismiss();
    }
  };

  const handleBFS = async () => {
    if (!bfsFrom) {
      toast.error('Ingresa una materia de inicio');
      return;
    }
    try {
      toast.loading('Ejecutando BFS...');
      const result = await graphApi.bfsLayers(bfsFrom);
      setBfsResult(result);
      toast.success(`BFS completado: ${result.length} niveles encontrados`);
    } catch (error) {
      toast.error('Error al ejecutar BFS');
      console.error(error);
    } finally {
      toast.dismiss();
    }
  };

  const handleBacktracking = async () => {
    if (!fromCode || !toCode) {
      toast.error('Ingresa ambas materias');
      return;
    }
    try {
      toast.loading('Buscando todas las rutas...');
      const result = await scheduleApi.backtracking(fromCode, toCode, btMaxDepth);
      setBacktrackingResult(result);
      toast.success(`Se encontraron ${result.length} rutas posibles`);
    } catch (error) {
      toast.error('Error al buscar rutas');
      console.error(error);
    } finally {
      toast.dismiss();
    }
  };

  const algorithmCards = [
    {
      title: 'Ordenamiento Topológico',
      description: 'Orden recomendado de cursada respetando correlativas',
      icon: <Layers className="w-6 h-6" />,
      handler: handleTopoSort,
      variant: 'primary' as const,
    },
    {
      title: 'Detección de Ciclos',
      description: 'Verifica si hay ciclos en las dependencias',
      icon: <GitMerge className="w-6 h-6" />,
      handler: handleCheckCycles,
      variant: 'secondary' as const,
    },
    {
      title: 'MST (Árbol de Expansión)',
      description: 'Calcula el árbol de expansión mínima sobre relaciones RELATED',
      icon: <TrendingUp className="w-6 h-6" />,
      handler: handleMST,
      variant: 'primary' as const,
    },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-3xl font-bold text-gray-900 mb-2">Algoritmos de Grafos</h2>
        <p className="text-gray-600">
          Ejecuta algoritmos avanzados sobre el grafo de correlativas
        </p>
      </div>

      {/* Quick Algorithms */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {algorithmCards.map((algo, idx) => (
          <Card key={idx} className="hover:shadow-md transition-shadow">
            <div className="flex items-start gap-4 mb-4">
              <div className="p-3 bg-uade-light rounded-xl text-uade-primary">
                {algo.icon}
              </div>
              <div className="flex-1">
                <h3 className="font-bold text-gray-900 mb-1">{algo.title}</h3>
                <p className="text-sm text-gray-600">{algo.description}</p>
              </div>
            </div>
            <Button
              variant={algo.variant}
              onClick={algo.handler}
              className="w-full"
            >
              Ejecutar
            </Button>
          </Card>
        ))}
      </div>

      {/* Shortest Path & Backtracking */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <Card>
          <CardHeader title="Camino Más Corto (Dijkstra)" />
          <div className="space-y-4">
            <div className="grid grid-cols-2 gap-2">
              <Input
                placeholder="Desde"
                value={fromCode}
                onChange={(e) => setFromCode(e.target.value.toUpperCase())}
              />
              <Input
                placeholder="Hasta"
                value={toCode}
                onChange={(e) => setToCode(e.target.value.toUpperCase())}
              />
            </div>
            <Button variant="primary" onClick={handleShortestPath} className="w-full" size="sm">
              <Search className="w-4 h-4 mr-2" />
              Calcular
            </Button>
          </div>
        </Card>

        <Card>
          <CardHeader title="Todas las Rutas (Backtracking)" />
          <div className="space-y-4">
            <div className="grid grid-cols-3 gap-2">
              <Input
                placeholder="Desde"
                value={fromCode}
                onChange={(e) => setFromCode(e.target.value.toUpperCase())}
              />
              <Input
                placeholder="Hasta"
                value={toCode}
                onChange={(e) => setToCode(e.target.value.toUpperCase())}
              />
              <Input
                type="number"
                placeholder="Prof."
                value={btMaxDepth}
                onChange={(e) => setBtMaxDepth(parseInt(e.target.value) || 10)}
              />
            </div>
            <Button variant="primary" onClick={handleBacktracking} className="w-full" size="sm">
              <Route className="w-4 h-4 mr-2" />
              Buscar
            </Button>
          </div>
        </Card>
      </div>

      {/* DFS y BFS */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <Card>
          <CardHeader title="DFS (Recorrido en Profundidad)" />
          <div className="space-y-4">
            <Input
              placeholder="Materia inicial"
              value={dfsFrom}
              onChange={(e) => setDfsFrom(e.target.value.toUpperCase())}
            />
            <Button variant="secondary" onClick={handleDFS} className="w-full" size="sm">
              Ejecutar DFS
            </Button>
          </div>
        </Card>

        <Card>
          <CardHeader title="BFS (Recorrido por Niveles)" />
          <div className="space-y-4">
            <Input
              placeholder="Materia inicial"
              value={bfsFrom}
              onChange={(e) => setBfsFrom(e.target.value.toUpperCase())}
            />
            <Button variant="secondary" onClick={handleBFS} className="w-full" size="sm">
              Ejecutar BFS
            </Button>
          </div>
        </Card>
      </div>

      {/* Toposort Result */}
      {topoResult.length > 0 && (
        <Card>
          <CardHeader 
            title="Resultado: Ordenamiento Topológico" 
            subtitle={`${topoResult.length} materias en orden recomendado`}
          />
          <div className="space-y-2">
            {topoResult.map((code, idx) => (
              <div key={code} className="flex items-center gap-3 p-2 bg-gray-50 rounded-lg">
                <Badge variant="secondary" size="sm">{idx + 1}</Badge>
                <Badge variant="primary">{code}</Badge>
                <span className="text-sm text-gray-600">
                  {courses.find(c => c.code === code)?.name || code}
                </span>
              </div>
            ))}
          </div>
        </Card>
      )}

      {/* Shortest Path Result */}
      {shortestPathResult.length > 0 && (
        <Card>
          <CardHeader 
            title="Resultado: Camino Más Corto" 
            subtitle={`${shortestPathResult.length} materias en el camino`}
          />
          <div className="flex items-center gap-2 flex-wrap">
            {shortestPathResult.map((code, idx) => (
              <div key={code} className="flex items-center gap-2">
                <Badge variant="primary">{code}</Badge>
                {idx < shortestPathResult.length - 1 && (
                  <ArrowRight className="w-4 h-4 text-gray-400" />
                )}
              </div>
            ))}
          </div>
        </Card>
      )}

      {/* MST Result */}
      {mstResult.length > 0 && (
        <Card>
          <CardHeader 
            title="Resultado: Árbol de Expansión Mínima (MST)" 
            subtitle={`${mstResult.length} aristas en el MST`}
          />
          <div className="space-y-2">
            {mstResult.map((edge, idx) => (
              <div key={idx} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                <div className="flex items-center gap-2">
                  <Badge variant="primary">{edge.u}</Badge>
                  <span className="text-gray-400">↔</span>
                  <Badge variant="primary">{edge.v}</Badge>
                </div>
                <span className="text-sm text-gray-600">Peso: {edge.w.toFixed(3)}</span>
              </div>
            ))}
          </div>
        </Card>
      )}

      {/* DFS Result */}
      {dfsResult.length > 0 && (
        <Card>
          <CardHeader 
            title="Resultado: DFS" 
            subtitle={`${dfsResult.length} materias visitadas`}
          />
          <div className="flex flex-wrap gap-2">
            {dfsResult.map((code, idx) => (
              <div key={code} className="flex items-center gap-1">
                <Badge variant="secondary" size="sm">{idx + 1}</Badge>
                <Badge variant="primary">{code}</Badge>
              </div>
            ))}
          </div>
        </Card>
      )}

      {/* BFS Result */}
      {bfsResult.length > 0 && (
        <Card>
          <CardHeader 
            title="Resultado: BFS por Niveles" 
            subtitle={`${bfsResult.length} niveles encontrados`}
          />
          <div className="space-y-4">
            {bfsResult.map((layer, idx) => (
              <div key={idx} className="space-y-2">
                <h4 className="font-semibold text-sm text-gray-700">Nivel {idx}</h4>
                <div className="flex flex-wrap gap-2">
                  {layer.map(code => (
                    <Badge key={code} variant="primary">{code}</Badge>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </Card>
      )}

      {/* Backtracking Result */}
      {backtrackingResult.length > 0 && (
        <Card>
          <CardHeader 
            title="Resultado: Todas las Rutas (Backtracking)" 
            subtitle={`${backtrackingResult.length} rutas encontradas`}
          />
          <div className="space-y-3">
            {backtrackingResult.map((path, idx) => (
              <div key={idx} className="flex items-center gap-2 p-3 bg-gray-50 rounded-lg flex-wrap">
                <Badge variant="secondary" size="sm">Ruta {idx + 1}</Badge>
                <div className="flex items-center gap-2 flex-wrap">
                  {path.map((code, i) => (
                    <div key={i} className="flex items-center gap-2">
                      <Badge variant="primary">{code}</Badge>
                      {i < path.length - 1 && <ArrowRight className="w-3 h-3 text-gray-400" />}
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </Card>
      )}

      {/* Current Approved Courses */}
      <Card>
        <CardHeader title="Materias Aprobadas" />
        <div className="flex flex-wrap gap-2">
          {approvedCodes.length === 0 ? (
            <p className="text-gray-600">No hay materias aprobadas</p>
          ) : (
            approvedCodes.map(code => (
              <Badge key={code} variant="success">{code}</Badge>
            ))
          )}
        </div>
      </Card>

      {/* Available Courses */}
      <Card>
        <CardHeader title="Materias Disponibles" />
        <AvailableCoursesList courses={courses} approvedCodes={approvedCodes} />
      </Card>
    </div>
  );
}

function AvailableCoursesList({ courses, approvedCodes }: { courses: any[], approvedCodes: string[] }) {
  const available = courses.filter(c => {
    if (!c.prereqs || c.prereqs.length === 0) return true;
    return c.prereqs.every((p: any) => approvedCodes.includes(p.code));
  });

  return (
    <div className="space-y-2">
      {available.length === 0 ? (
        <p className="text-gray-600">No hay materias disponibles</p>
      ) : (
        available.map(course => (
          <div
            key={course.code}
            className="flex items-center justify-between p-3 bg-gray-50 rounded-lg hover:bg-gray-100"
          >
            <div>
              <Badge variant="primary" size="sm">{course.code}</Badge>
              <span className="font-medium text-gray-900">{course.name}</span>
            </div>
            {course.credits && (
              <span className="text-sm text-gray-600">{course.credits} créditos</span>
            )}
          </div>
        ))
      )}
    </div>
  );
}

