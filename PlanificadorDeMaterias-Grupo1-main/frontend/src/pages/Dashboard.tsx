import { useQuery } from '@tanstack/react-query';
import { Card, CardHeader } from '../components/ui/Card';
import { coursesApi, scheduleApi } from '../services/api';
import { useApprovedCourses } from '../store/approvedCourses';
import { LayoutDashboard, BookOpen, CheckCircle2, TrendingUp, Users } from 'lucide-react';
import { Badge } from '../components/ui/Badge';

export default function Dashboard() {
  const { approvedCodes } = useApprovedCourses();
  const { data: courses = [], isLoading } = useQuery({
    queryKey: ['courses'],
    queryFn: coursesApi.getAll,
  });

  // Usar el backend para calcular materias disponibles
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

  const statCards = [
    {
      title: 'Total Materias',
      value: stats.total,
      icon: <BookOpen className="w-6 h-6" />,
      color: 'text-blue-600',
      bgColor: 'bg-blue-100',
    },
    {
      title: 'Aprobadas',
      value: stats.approved,
      icon: <CheckCircle2 className="w-6 h-6" />,
      color: 'text-green-600',
      bgColor: 'bg-green-100',
    },
    {
      title: 'Pendientes',
      value: stats.remaining,
      icon: <TrendingUp className="w-6 h-6" />,
      color: 'text-orange-600',
      bgColor: 'bg-orange-100',
    },
    {
      title: 'Disponibles',
      value: stats.available,
      icon: <Users className="w-6 h-6" />,
      color: 'text-purple-600',
      bgColor: 'bg-purple-100',
    },
  ];

  return (
    <div className="space-y-8">
      <div>
        <h2 className="text-3xl font-bold text-gray-900 mb-2">Dashboard</h2>
        <p className="text-gray-600">
          Visión general de tu progreso académico
        </p>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {statCards.map((stat, idx) => (
          <Card key={idx}>
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600 mb-1">{stat.title}</p>
                <p className="text-3xl font-bold text-gray-900">
                  {(isLoading || loadingAvailable) ? '...' : stat.value}
                </p>
              </div>
              <div className={`${stat.bgColor} ${stat.color} p-3 rounded-xl`}>
                {stat.icon}
              </div>
            </div>
          </Card>
        ))}
      </div>

      {/* Quick Actions */}
      <Card>
        <CardHeader title="Acciones Rápidas" />
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <QuickActionCard
            title="Catálogo de Materias"
            description="Explora todas las materias disponibles"
            href="/catalog"
            icon={<BookOpen className="w-6 h-6" />}
          />
          <QuickActionCard
            title="Ver Correlativas"
            description="Visualiza el grafo de prerequisitos"
            href="/graph"
            icon={<LayoutDashboard className="w-6 h-6" />}
          />
          <QuickActionCard
            title="Planificar Cuatrimestre"
            description="Genera tu plan de cursada"
            href="/planner"
            icon={<TrendingUp className="w-6 h-6" />}
          />
        </div>
      </Card>

      {/* Recent Approved */}
      {approvedCodes.length > 0 && (
        <Card>
          <CardHeader 
            title="Últimas Aprobadas" 
            subtitle={`${approvedCodes.length} materia${approvedCodes.length !== 1 ? 's' : ''} aprobada${approvedCodes.length !== 1 ? 's' : ''}`}
          />
          <div className="flex flex-wrap gap-3">
            {approvedCodes.map(code => (
              <Badge key={code} variant="success">
                {code}
              </Badge>
            ))}
          </div>
        </Card>
      )}
    </div>
  );
}

function QuickActionCard({ title, description, href, icon }: any) {
  return (
    <a
      href={href}
      className="flex flex-col items-center justify-center p-6 border-2 border-dashed border-gray-300 rounded-xl hover:border-uade-primary hover:bg-uade-light transition-all duration-200 group"
    >
      <div className="mb-4 text-uade-primary group-hover:scale-110 transition-transform duration-200">
        {icon}
      </div>
      <h4 className="font-semibold text-gray-900 mb-1">{title}</h4>
      <p className="text-sm text-center text-gray-600">{description}</p>
    </a>
  );
}

