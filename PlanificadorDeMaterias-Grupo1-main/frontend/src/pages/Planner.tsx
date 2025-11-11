import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Card, CardHeader } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { scheduleApi, coursesApi } from '../services/api';
import { useApprovedCourses } from '../store/approvedCourses';
import { Calendar, Clock, BookOpen, TrendingUp } from 'lucide-react';
import { toast } from 'react-hot-toast';
import type { Course } from '../types';

export default function Planner() {
  const { approvedCodes } = useApprovedCourses();
  const [algorithm, setAlgorithm] = useState<'greedy' | 'dp' | 'bnb'>('greedy');
  const [value, setValue] = useState<'credits' | 'difficulty' | 'hours'>('credits');
  const [maxHours, setMaxHours] = useState(24);
  const [semesters, setSemesters] = useState(4);
  const [results, setResults] = useState<Course[]>([]);
  const [multiSemesterResults, setMultiSemesterResults] = useState<Course[][]>([]);

  const { data: courses = [] } = useQuery({
    queryKey: ['courses'],
    queryFn: coursesApi.getAll,
  });

  const handleGeneratePlan = async () => {
    try {
      toast.loading('Generando plan de cursada...');
      
      const approvedList = Array.from(approvedCodes);
      
      if (algorithm === 'bnb') {
        // Branch & Bound retorna múltiples cuatrimestres
        const bnbResult = await scheduleApi.branchAndBound(approvedList, semesters, maxHours);
        const allSemesters = bnbResult.map(semester => 
          semester.map(code => courses.find(c => c.code === code)).filter(Boolean) as Course[]
        );
        setMultiSemesterResults(allSemesters);
        setResults([]); // Limpiar results single
        
        const totalCourses = allSemesters.reduce((sum, sem) => sum + sem.length, 0);
        toast.success(`Plan generado: ${totalCourses} materias en ${allSemesters.length} cuatrimestres`);
      } else {
        // Greedy y DP retornan un solo cuatrimestre
        let data: Course[];
        
        switch (algorithm) {
          case 'greedy':
            data = await scheduleApi.greedy(approvedList, value, maxHours);
            break;
          case 'dp':
            data = await scheduleApi.dp(approvedList, value, maxHours);
            break;
          default:
            data = [];
        }
        
        setResults(data);
        setMultiSemesterResults([]); // Limpiar multi results
        toast.success(`Plan generado con ${data.length} materias`);
      }
    } catch (error) {
      toast.error('Error al generar el plan');
      console.error(error);
    } finally {
      toast.dismiss();
    }
  };

  const totalCredits = results.reduce((sum, c) => sum + (c.credits || 0), 0);
  const totalHours = results.reduce((sum, c) => sum + (c.hours || 0), 0);

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-3xl font-bold text-gray-900 mb-2">Planificador de Cursada</h2>
        <p className="text-gray-600">
          Genera planes de cursada optimizados usando algoritmos avanzados
        </p>
      </div>

      {/* Configuration */}
      <Card>
        <CardHeader title="Configuración" />
        <div className="space-y-6">
          {/* Algorithm Selection */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-3">
              Algoritmo
            </label>
            <div className="grid grid-cols-3 gap-3">
              {['greedy', 'dp', 'bnb'].map((alg) => (
                <button
                  key={alg}
                  onClick={() => setAlgorithm(alg as any)}
                  className={`px-4 py-3 rounded-xl border-2 transition-all duration-200 text-left ${
                    algorithm === alg
                      ? 'border-uade-primary bg-uade-primary/5'
                      : 'border-gray-300 hover:border-uade-primary/50'
                  }`}
                >
                  <div className="font-semibold text-gray-900 capitalize mb-1">
                    {alg === 'greedy' ? 'Greedy' : alg === 'dp' ? 'DP Knapsack' : 'Branch & Bound'}
                  </div>
                  <div className="text-sm text-gray-600">
                    {alg === 'greedy' && 'Selección voraz por valor'}
                    {alg === 'dp' && 'Programación dinámica'}
                    {alg === 'bnb' && 'Optimización completa'}
                  </div>
                </button>
              ))}
            </div>
          </div>

          {/* Value Selection */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-3">
              Criterio de Optimización
            </label>
            <div className="grid grid-cols-3 gap-3">
              {['credits', 'difficulty', 'hours'].map((val) => (
                <button
                  key={val}
                  onClick={() => setValue(val as any)}
                  className={`px-4 py-3 rounded-xl border-2 transition-all duration-200 text-left ${
                    value === val
                      ? 'border-uade-primary bg-uade-primary/5'
                      : 'border-gray-300 hover:border-uade-primary/50'
                  }`}
                >
                  <div className="font-semibold text-gray-900 capitalize">
                    {val === 'credits' ? 'Créditos' : val === 'difficulty' ? 'Dificultad' : 'Horas'}
                  </div>
                </button>
              ))}
            </div>
          </div>

          {/* Max Hours */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Máximo de Horas Semanales
            </label>
            <input
              type="number"
              value={maxHours}
              onChange={(e) => setMaxHours(Number(e.target.value))}
              min="1"
              max="40"
              className="w-full px-4 py-2 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-uade-primary"
            />
          </div>

          {/* Semesters (solo para BnB) */}
          {algorithm === 'bnb' && (
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Cantidad de Cuatrimestres
              </label>
              <input
                type="number"
                value={semesters}
                onChange={(e) => setSemesters(Number(e.target.value))}
                min="1"
                max="10"
                className="w-full px-4 py-2 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-uade-primary"
              />
              <p className="text-xs text-gray-500 mt-1">
                Branch & Bound optimizará para {semesters} cuatrimestres
              </p>
            </div>
          )}

          <Button variant="primary" onClick={handleGeneratePlan} className="w-full">
            Generar Plan
          </Button>
        </div>
      </Card>

      {/* Results */}
      {results.length > 0 && (
        <Card>
          <CardHeader title="Plan Generado" />
          <div className="space-y-6">
            {/* Summary */}
            <div className="grid grid-cols-3 gap-4">
              <div className="flex items-center gap-3 p-4 bg-blue-50 rounded-xl">
                <BookOpen className="w-8 h-8 text-blue-600" />
                <div>
                  <p className="text-sm text-gray-600">Materias</p>
                  <p className="text-2xl font-bold text-gray-900">{results.length}</p>
                </div>
              </div>
              <div className="flex items-center gap-3 p-4 bg-green-50 rounded-xl">
                <TrendingUp className="w-8 h-8 text-green-600" />
                <div>
                  <p className="text-sm text-gray-600">Créditos</p>
                  <p className="text-2xl font-bold text-gray-900">{totalCredits}</p>
                </div>
              </div>
              <div className="flex items-center gap-3 p-4 bg-purple-50 rounded-xl">
                <Clock className="w-8 h-8 text-purple-600" />
                <div>
                  <p className="text-sm text-gray-600">Horas/sem</p>
                  <p className="text-2xl font-bold text-gray-900">{totalHours}</p>
                </div>
              </div>
            </div>

            {/* Course List */}
            <div className="space-y-3">
              <h4 className="font-semibold text-gray-900">Materias del Plan</h4>
              <div className="space-y-2">
                {results.map((course) => (
                  <div
                    key={course.code}
                    className="flex items-center justify-between p-4 bg-gray-50 rounded-xl hover:bg-gray-100 transition-colors"
                  >
                    <div className="flex items-center gap-4">
                      <Badge variant="primary">{course.code}</Badge>
                      <div>
                        <p className="font-semibold text-gray-900">{course.name}</p>
                        <div className="flex items-center gap-4 mt-1 text-sm text-gray-600">
                          {course.credits && <span>{course.credits} créditos</span>}
                          {course.hours && <span>{course.hours}h semanales</span>}
                          {course.difficulty && <span>Dificultad: {course.difficulty}/5</span>}
                        </div>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </Card>
      )}

      {/* Multi Semester Results (BnB) */}
      {multiSemesterResults.length > 0 && (
        <div className="space-y-6">
          <h3 className="text-2xl font-bold text-gray-900">
            Plan de {multiSemesterResults.length} Cuatrimestres
          </h3>
          {multiSemesterResults.map((semester, idx) => {
            const semCredits = semester.reduce((sum, c) => sum + (c.credits || 0), 0);
            const semHours = semester.reduce((sum, c) => sum + (c.hours || 0), 0);
            
            return (
              <Card key={idx}>
                <CardHeader 
                  title={`Cuatrimestre ${idx + 1}`} 
                  subtitle={`${semester.length} materias - ${semCredits} créditos - ${semHours}h semanales`}
                />
                <div className="space-y-2">
                  {semester.map((course) => (
                    <div
                      key={course.code}
                      className="flex items-center justify-between p-4 bg-gray-50 rounded-xl hover:bg-gray-100 transition-colors"
                    >
                      <div className="flex items-center gap-4">
                        <Badge variant="primary">{course.code}</Badge>
                        <div>
                          <p className="font-semibold text-gray-900">{course.name}</p>
                          <div className="flex items-center gap-4 mt-1 text-sm text-gray-600">
                            {course.credits && <span>{course.credits} créditos</span>}
                            {course.hours && <span>{course.hours}h semanales</span>}
                            {course.difficulty && <span>Dificultad: {course.difficulty}/5</span>}
                          </div>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </Card>
            );
          })}

          {/* Summary de todos los cuatrimestres */}
          <Card>
            <CardHeader title="Resumen Total" />
            <div className="grid grid-cols-3 gap-4">
              <div className="text-center p-4 bg-blue-50 rounded-xl">
                <p className="text-sm text-gray-600 mb-1">Total Materias</p>
                <p className="text-3xl font-bold text-gray-900">
                  {multiSemesterResults.reduce((sum, sem) => sum + sem.length, 0)}
                </p>
              </div>
              <div className="text-center p-4 bg-green-50 rounded-xl">
                <p className="text-sm text-gray-600 mb-1">Total Créditos</p>
                <p className="text-3xl font-bold text-gray-900">
                  {multiSemesterResults.reduce((sum, sem) => 
                    sum + sem.reduce((s, c) => s + (c.credits || 0), 0), 0
                  )}
                </p>
              </div>
              <div className="text-center p-4 bg-purple-50 rounded-xl">
                <p className="text-sm text-gray-600 mb-1">Promedio Horas/Cuatr.</p>
                <p className="text-3xl font-bold text-gray-900">
                  {(multiSemesterResults.reduce((sum, sem) => 
                    sum + sem.reduce((s, c) => s + (c.hours || 0), 0), 0
                  ) / multiSemesterResults.length).toFixed(1)}h
                </p>
              </div>
            </div>
          </Card>
        </div>
      )}

      {/* No approved courses message */}
      {approvedCodes.length === 0 && (
        <Card>
          <div className="text-center py-8">
            <Calendar className="w-16 h-16 text-gray-400 mx-auto mb-4" />
            <h3 className="text-lg font-semibold text-gray-900 mb-2">
              No hay materias aprobadas
            </h3>
            <p className="text-gray-600 mb-4">
              Marca algunas materias como aprobadas en el Catálogo para generar un plan
            </p>
          </div>
        </Card>
      )}
    </div>
  );
}

