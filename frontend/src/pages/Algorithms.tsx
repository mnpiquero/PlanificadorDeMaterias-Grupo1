import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Card, CardHeader } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Badge } from '../components/ui/Badge';
import { graphApi, coursesApi } from '../services/api';
import { useApprovedCourses } from '../store/approvedCourses';
import { Search, TrendingUp, Layers, GitMerge } from 'lucide-react';
import { toast } from 'react-hot-toast';

export default function Algorithms() {
  const { approvedCodes } = useApprovedCourses();
  const [fromCode, setFromCode] = useState('');
  const [toCode, setToCode] = useState('');

  const { data: courses = [] } = useQuery({
    queryKey: ['courses'],
    queryFn: coursesApi.getAll,
  });

  const handleTopoSort = async () => {
    try {
      toast.loading('Calculando ordenamiento topológico...');
      const result = await graphApi.topoSort(Array.from(approvedCodes));
      toast.success(`Orden calculado con ${result.length} materias`);
      console.log('Toposort result:', result);
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
      toast.success(`Camino encontrado: ${result.length} materias`);
      console.log('Shortest path:', result);
    } catch (error) {
      toast.error('No se encontró camino');
      console.error(error);
    } finally {
      toast.dismiss();
    }
  };

  const handleMST = async () => {
    try {
      toast.loading('Calculando MST...');
      const result = await graphApi.mst('prim');
      toast.success(`MST calculado con ${result.length} aristas`);
      console.log('MST result:', result);
    } catch (error) {
      toast.error('Error al calcular MST');
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

      {/* Shortest Path */}
      <Card>
        <CardHeader 
          title="Camino Más Corto" 
          subtitle="Encuentra el camino más corto entre dos materias"
        />
        <div className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <Input
              label="Desde"
              placeholder="Ej: MAT101"
              value={fromCode}
              onChange={(e) => setFromCode(e.target.value.toUpperCase())}
            />
            <Input
              label="Hasta"
              placeholder="Ej: MAT301"
              value={toCode}
              onChange={(e) => setToCode(e.target.value.toUpperCase())}
            />
          </div>
          <Button variant="primary" onClick={handleShortestPath} className="w-full">
            <Search className="w-4 h-4 mr-2" />
            Calcular Camino
          </Button>
        </div>
      </Card>

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

