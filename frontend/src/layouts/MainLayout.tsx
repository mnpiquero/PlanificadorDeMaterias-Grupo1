import { Outlet, Link, useLocation } from 'react-router-dom';
import { 
  LayoutDashboard, 
  BookOpen, 
  Network, 
  Calendar, 
  GraduationCap
} from 'lucide-react';
import { useApprovedCourses } from '../store/approvedCourses';

interface NavItem {
  path: string;
  icon: React.ReactNode;
  label: string;
}

const navItems: NavItem[] = [
  { path: '/', icon: <LayoutDashboard />, label: 'Dashboard' },
  { path: '/catalog', icon: <BookOpen />, label: 'Catálogo' },
  { path: '/graph', icon: <Network />, label: 'Correlativas' },
  { path: '/planner', icon: <Calendar />, label: 'Planificador' },
  { path: '/algorithms', icon: <GraduationCap />, label: 'Algoritmos' },
];

export default function MainLayout() {
  const location = useLocation();
  const { approvedCodes } = useApprovedCourses();

  const isActive = (path: string) => {
    if (path === '/') {
      return location.pathname === '/';
    }
    return location.pathname.startsWith(path);
  };

  return (
    <div className="min-h-screen bg-uade-light">
      {/* Header */}
      <header className="bg-white border-b-2 border-uade-primary shadow-sm">
        <div className="max-w-7xl mx-auto px-6 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-4">
              <div className="flex items-center gap-3">
                <div className="w-12 h-12 bg-uade-primary rounded-xl flex items-center justify-center">
                  <GraduationCap className="w-7 h-7 text-white" />
                </div>
                <div>
                  <h1 className="text-2xl font-bold text-uade-primary">UADE</h1>
                  <p className="text-sm text-gray-600 -mt-1">Planificador de Materias</p>
                </div>
              </div>
            </div>
            
            {/* Approved courses badge */}
            <div className="flex items-center gap-4">
              <div className="flex items-center gap-2 px-4 py-2 bg-uade-light rounded-xl">
                <div className="flex -space-x-2">
                  {approvedCodes.slice(0, 3).map((code, idx) => (
                    <div
                      key={idx}
                      className="w-8 h-8 bg-uade-primary rounded-full flex items-center justify-center border-2 border-white"
                    >
                      <span className="text-xs font-bold text-white">{code}</span>
                    </div>
                  ))}
                </div>
                {approvedCodes.length > 3 && (
                  <span className="text-sm font-semibold text-gray-700">
                    +{approvedCodes.length - 3}
                  </span>
                )}
                <span className="text-sm text-gray-600">
                  {approvedCodes.length === 0 && 'Sin materias aprobadas'}
                </span>
              </div>
            </div>
          </div>
        </div>
      </header>

      <div className="flex">
        {/* Sidebar */}
        <aside className="w-64 bg-white border-r border-gray-200 min-h-[calc(100vh-88px)] sticky top-[88px]">
          <nav className="p-4">
            <ul className="space-y-2">
              {navItems.map((item) => (
                <li key={item.path}>
                  <Link
                    to={item.path}
                    className={`flex items-center gap-3 px-4 py-3 rounded-xl font-medium transition-colors duration-200 ${
                      isActive(item.path)
                        ? 'bg-uade-primary text-white shadow-md'
                        : 'text-gray-700 hover:bg-uade-light'
                    }`}
                  >
                    <span className="w-5 h-5">{item.icon}</span>
                    <span>{item.label}</span>
                  </Link>
                </li>
              ))}
            </ul>
          </nav>
        </aside>

        {/* Main Content */}
        <main className="flex-1 p-8">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

