import { useMemo, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Card, CardHeader } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { coursesApi } from '../services/api';
import { useApprovedCourses } from '../store/approvedCourses';
import ReactFlow, { 
  Controls, 
  Background, 
  MiniMap,
  MarkerType
} from 'reactflow';
import type { Node, Edge } from 'reactflow';
import 'reactflow/dist/style.css';
import { RefreshCw } from 'lucide-react';
import type { Course } from '../types';

export default function Graph() {
  const { approvedCodes } = useApprovedCourses();
  const [selectedCourse, setSelectedCourse] = useState<string | null>(null);
  
  const { data: courses = [], isLoading, refetch } = useQuery({
    queryKey: ['courses'],
    queryFn: coursesApi.getAll,
  });

  // Convert courses to nodes and edges
  const { nodes, edges } = useMemo(() => {
    const nodesMap: Map<string, Node> = new Map();
    const edgesList: Edge[] = [];

    courses.forEach((course) => {
      const isApproved = approvedCodes.includes(course.code);
      const isSelected = selectedCourse === course.code;

      nodesMap.set(course.code, {
        id: course.code,
        data: { 
          label: (
            <div className={`p-2 ${isSelected ? 'bg-uade-primary text-white' : ''} rounded-lg`}>
              <div className="font-bold text-sm">{course.code}</div>
              <div className={`text-xs ${isSelected ? 'text-white/90' : 'text-gray-600'}`}>
                {course.name}
              </div>
            </div>
          ),
          course,
        },
        position: { x: 0, y: 0 },
        style: {
          background: isApproved ? '#10b981' : isSelected ? '#002B80' : '#fff',
          border: isSelected ? '3px solid #0040BF' : '2px solid #e5e7eb',
          borderRadius: '12px',
          padding: '0',
          minWidth: '120px',
          color: isApproved || isSelected ? 'white' : '#1f2937',
        },
      });

      // Add edges for prerequisites
      if (course.prereqs) {
        course.prereqs.forEach((prereq) => {
          edgesList.push({
            id: `e${prereq.code}-${course.code}`,
            source: prereq.code,
            target: course.code,
            type: 'smoothstep',
            animated: isSelected && (prereq.code === selectedCourse || course.code === selectedCourse),
            style: { strokeWidth: 2 },
            markerEnd: {
              type: MarkerType.ArrowClosed,
            },
          });
        });
      }
    });

    // Simple layout: arrange in layers
    const nodesArray = Array.from(nodesMap.values());
    const layerWidth = 300;
    const layerHeight = 150;

    // Simple force-directed-like layout
    nodesArray.forEach((node, idx) => {
      const depth = calculateDepth(node.id, nodesMap, edgesList);
      node.position = {
        x: depth * layerWidth + (idx % 3) * 100,
        y: (idx % 5) * layerHeight + (depth % 2) * 50,
      };
    });

    return {
      nodes: nodesArray,
      edges: edgesList,
    };
  }, [courses, approvedCodes, selectedCourse]);

  const handleNodeClick = (_event: any, node: Node) => {
    setSelectedCourse(node.id === selectedCourse ? null : node.id);
  };

  if (isLoading) {
    return (
      <div className="flex justify-center items-center py-20">
        <div className="text-gray-500">Cargando grafo...</div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-3xl font-bold text-gray-900 mb-2">Grafo de Correlativas</h2>
          <p className="text-gray-600">
            Visualiza las relaciones de prerequisitos entre materias
          </p>
        </div>
        <Button variant="secondary" onClick={() => refetch()}>
          <RefreshCw className="w-4 h-4 mr-2" />
          Actualizar
        </Button>
      </div>

      {/* Legend */}
      <Card>
        <div className="flex items-center gap-6 flex-wrap">
          <div className="flex items-center gap-2">
            <div className="w-6 h-6 bg-green-500 rounded-lg"></div>
            <span className="text-sm text-gray-700">Aprobada</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-6 h-6 bg-white border-2 border-gray-300 rounded-lg"></div>
            <span className="text-sm text-gray-700">Pendiente</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-6 h-6 bg-uade-primary rounded-lg"></div>
            <span className="text-sm text-gray-700">Seleccionada</span>
          </div>
        </div>
      </Card>

      {/* Flow */}
      <Card className="p-0 h-[600px]">
        <ReactFlow
          nodes={nodes}
          edges={edges}
          onNodeClick={handleNodeClick}
          fitView
          className="bg-uade-light"
        >
          <Controls />
          <MiniMap />
          <Background gap={20} size={1} />
        </ReactFlow>
      </Card>

      {/* Selected Course Details */}
      {selectedCourse && (
        <Card>
          <CardHeader 
            title="Detalle de Materia" 
          />
          <SelectedCourseDetails courseCode={selectedCourse} courses={courses} />
        </Card>
      )}
    </div>
  );
}

function SelectedCourseDetails({ courseCode, courses }: { courseCode: string; courses: Course[] }) {
  const course = courses.find(c => c.code === courseCode);
  if (!course) return null;

  return (
    <div className="space-y-4">
      <div>
        <h3 className="text-xl font-bold text-gray-900">{course.name}</h3>
        <p className="text-sm text-gray-600">{course.code}</p>
      </div>
      
      <div className="grid grid-cols-3 gap-4">
        {course.credits && (
          <div>
            <p className="text-sm text-gray-600">Créditos</p>
            <p className="text-2xl font-bold text-gray-900">{course.credits}</p>
          </div>
        )}
        {course.hours && (
          <div>
            <p className="text-sm text-gray-600">Horas</p>
            <p className="text-2xl font-bold text-gray-900">{course.hours}</p>
          </div>
        )}
        {course.difficulty && (
          <div>
            <p className="text-sm text-gray-600">Dificultad</p>
            <p className="text-2xl font-bold text-gray-900">{course.difficulty}/5</p>
          </div>
        )}
      </div>
    </div>
  );
}

// Helper to calculate node depth in the graph
function calculateDepth(nodeId: string, _nodesMap: Map<string, Node>, edges: Edge[]): number {
  let maxDepth = 0;
  
  function dfs(nodeId: string, visited: Set<string>, depth: number) {
    if (visited.has(nodeId)) return;
    visited.add(nodeId);
    maxDepth = Math.max(maxDepth, depth);
    
    const outgoingEdges = edges.filter(e => e.source === nodeId);
    outgoingEdges.forEach(edge => {
      dfs(edge.target, visited, depth + 1);
    });
  }
  
  dfs(nodeId, new Set(), 0);
  return maxDepth;
}

