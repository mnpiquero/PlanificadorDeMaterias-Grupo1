import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Card } from '../components/ui/Card';
import { Input } from '../components/ui/Input';
import { Badge } from '../components/ui/Badge';
import { Button } from '../components/ui/Button';
import { coursesApi } from '../services/api';
import { useApprovedCourses } from '../store/approvedCourses';
import { Search, CheckCircle2, Circle, Filter } from 'lucide-react';
import type { Course } from '../types';

export default function Catalog() {
  const [searchTerm, setSearchTerm] = useState('');
  const [showFilters, setShowFilters] = useState(false);
  const { approvedCodes, toggle } = useApprovedCourses();

  const { data: courses = [], isLoading } = useQuery({
    queryKey: ['courses'],
    queryFn: coursesApi.getAll,
  });

  const filteredCourses = courses.filter(course =>
    course.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    course.code.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const getDifficultyColor = (difficulty?: number) => {
    if (!difficulty) return 'secondary';
    if (difficulty <= 2) return 'success';
    if (difficulty <= 3) return 'info';
    if (difficulty <= 4) return 'warning';
    return 'danger';
  };

  const getDifficultyLabel = (difficulty?: number) => {
    if (!difficulty) return 'N/A';
    const levels = ['', 'Muy Fácil', 'Fácil', 'Media', 'Difícil', 'Muy Difícil'];
    return levels[difficulty];
  };

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-3xl font-bold text-gray-900 mb-2">Catálogo de Materias</h2>
        <p className="text-gray-600">
          Explora todas las materias disponibles del plan de estudios
        </p>
      </div>

      {/* Search & Filters */}
      <Card>
        <div className="flex flex-col md:flex-row gap-4">
          <div className="flex-1 relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 w-5 h-5" />
            <Input
              type="text"
              placeholder="Buscar por nombre o código..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="pl-10"
            />
          </div>
          <Button
            variant="secondary"
            onClick={() => setShowFilters(!showFilters)}
          >
            <Filter className="w-4 h-4 mr-2" />
            Filtros
          </Button>
        </div>
      </Card>

      {/* Course Grid */}
      {isLoading ? (
        <div className="flex justify-center items-center py-20">
          <div className="text-gray-500">Cargando materias...</div>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredCourses.map((course) => (
            <CourseCard
              key={course.code}
              course={course}
              isApproved={approvedCodes.includes(course.code)}
              onToggleApproved={() => toggle(course.code)}
              getDifficultyColor={getDifficultyColor}
              getDifficultyLabel={getDifficultyLabel}
            />
          ))}
          {filteredCourses.length === 0 && (
            <div className="col-span-full text-center py-20 text-gray-500">
              No se encontraron materias
            </div>
          )}
        </div>
      )}
    </div>
  );
}

interface CourseCardProps {
  course: Course;
  isApproved: boolean;
  onToggleApproved: () => void;
  getDifficultyColor: (difficulty?: number) => string;
  getDifficultyLabel: (difficulty?: number) => string;
}

function CourseCard({ course, isApproved, onToggleApproved, getDifficultyColor, getDifficultyLabel }: CourseCardProps) {
  return (
    <Card className="hover:shadow-md transition-shadow duration-200">
      <div className="flex items-start justify-between mb-4">
        <div className="flex-1">
          <div className="flex items-center gap-2 mb-1">
            <h3 className="text-lg font-bold text-gray-900">{course.name}</h3>
          </div>
          <Badge variant="primary" size="sm">{course.code}</Badge>
        </div>
        <button
          onClick={onToggleApproved}
          className="ml-2 p-1 hover:bg-gray-100 rounded-lg transition-colors"
        >
          {isApproved ? (
            <CheckCircle2 className="w-6 h-6 text-green-600" />
          ) : (
            <Circle className="w-6 h-6 text-gray-300" />
          )}
        </button>
      </div>

      <div className="space-y-2 mb-4">
        {course.credits && (
          <div className="flex items-center justify-between text-sm">
            <span className="text-gray-600">Créditos:</span>
            <span className="font-semibold">{course.credits}</span>
          </div>
        )}
        {course.hours && (
          <div className="flex items-center justify-between text-sm">
            <span className="text-gray-600">Horas semanales:</span>
            <span className="font-semibold">{course.hours}h</span>
          </div>
        )}
        {course.difficulty && (
          <div className="flex items-center justify-between text-sm">
            <span className="text-gray-600">Dificultad:</span>
            <Badge variant={getDifficultyColor(course.difficulty) as any}>
              {getDifficultyLabel(course.difficulty)}
            </Badge>
          </div>
        )}
      </div>

      {course.prereqs && course.prereqs.length > 0 && (
        <div className="pt-4 border-t border-gray-200">
          <p className="text-xs font-semibold text-gray-600 mb-2">Prerequisitos:</p>
          <div className="flex flex-wrap gap-1">
            {course.prereqs.map((prereq) => (
              <Badge key={prereq.code} variant="secondary" size="sm">
                {prereq.code}
              </Badge>
            ))}
          </div>
        </div>
      )}
    </Card>
  );
}

