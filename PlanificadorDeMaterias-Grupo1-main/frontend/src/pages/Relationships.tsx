import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { relationshipsApi, coursesApi } from '../services/api';
import { Card, CardHeader } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Badge } from '../components/ui/Badge';
import { toast } from 'react-hot-toast';
import { Link2, Trash2, Edit } from 'lucide-react';

export default function Relationships() {
  const [fromCode, setFromCode] = useState('');
  const [toCode, setToCode] = useState('');
  const [similarity, setSimilarity] = useState(0.5);
  const [autoMode, setAutoMode] = useState(true);
  const [editingRelation, setEditingRelation] = useState<{from: string; to: string} | null>(null);
  const [newSimilarity, setNewSimilarity] = useState(0.5);
  
  const queryClient = useQueryClient();
  
  const { data: relationships = [], isLoading } = useQuery({
    queryKey: ['relationships'],
    queryFn: relationshipsApi.getAll,
  });

  const { data: courses = [] } = useQuery({
    queryKey: ['courses'],
    queryFn: coursesApi.getAll,
  });
  
  const createMutation = useMutation({
    mutationFn: () => autoMode 
      ? relationshipsApi.createAuto({ fromCode, toCode })
      : relationshipsApi.create({ fromCode, toCode, similarity }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['relationships'] });
      toast.success('Relación creada exitosamente');
      setFromCode('');
      setToCode('');
      setSimilarity(0.5);
    },
    onError: (error: any) => {
      toast.error(error?.response?.data?.message || 'Error al crear relación');
    },
  });
  
  const updateMutation = useMutation({
    mutationFn: (data: { from: string; to: string; similarity: number }) => 
      relationshipsApi.update(data.from, data.to, data.similarity),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['relationships'] });
      toast.success('Relación actualizada');
      setEditingRelation(null);
    },
    onError: () => toast.error('Error al actualizar relación'),
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

  const handleCreate = () => {
    if (!fromCode || !toCode) {
      toast.error('Completa todos los campos');
      return;
    }
    if (fromCode === toCode) {
      toast.error('Las materias deben ser diferentes');
      return;
    }
    createMutation.mutate();
  };

  const handleUpdate = (from: string, to: string) => {
    updateMutation.mutate({ from, to, similarity: newSimilarity });
  };

  const getCourseName = (code: string) => {
    const course = courses.find(c => c.code === code);
    return course?.name || code;
  };
  
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
        <CardHeader 
          title="Crear Nueva Relación" 
          subtitle="Define relaciones de similaridad entre materias"
        />
        <div className="space-y-4">
          <div className="flex items-center gap-4 p-3 bg-gray-50 rounded-xl">
            <label className="flex items-center gap-2 cursor-pointer">
              <input
                type="radio"
                checked={autoMode}
                onChange={() => setAutoMode(true)}
                className="w-4 h-4 text-uade-primary"
              />
              <span className="text-sm font-medium">Cálculo Automático</span>
            </label>
            <label className="flex items-center gap-2 cursor-pointer">
              <input
                type="radio"
                checked={!autoMode}
                onChange={() => setAutoMode(false)}
                className="w-4 h-4 text-uade-primary"
              />
              <span className="text-sm font-medium">Similaridad Manual</span>
            </label>
          </div>
          
          <div className="grid grid-cols-2 gap-4">
            <Input
              label="Desde"
              placeholder="Código de materia (ej: MAT101)"
              value={fromCode}
              onChange={(e) => setFromCode(e.target.value.toUpperCase())}
            />
            <Input
              label="Hasta"
              placeholder="Código de materia (ej: FIS101)"
              value={toCode}
              onChange={(e) => setToCode(e.target.value.toUpperCase())}
            />
          </div>
          
          {!autoMode && (
            <div>
              <Input
                label={`Similaridad: ${similarity.toFixed(2)}`}
                type="range"
                min="0"
                max="1"
                step="0.01"
                value={similarity}
                onChange={(e) => setSimilarity(parseFloat(e.target.value))}
              />
              <p className="text-xs text-gray-500 mt-1">
                0 = Sin relación, 1 = Muy relacionadas
              </p>
            </div>
          )}

          {autoMode && (
            <div className="p-3 bg-blue-50 rounded-xl">
              <p className="text-sm text-blue-800">
                <strong>Modo Automático:</strong> La similaridad se calculará basándose en 
                créditos, horas y dificultad de las materias.
              </p>
            </div>
          )}
          
          <Button
            variant="primary"
            onClick={handleCreate}
            disabled={!fromCode || !toCode || createMutation.isPending}
            className="w-full"
          >
            <Link2 className="w-4 h-4 mr-2" />
            {createMutation.isPending ? 'Creando...' : 'Crear Relación'}
          </Button>
        </div>
      </Card>
      
      {/* Lista de relaciones */}
      <Card>
        <CardHeader 
          title="Relaciones Existentes" 
          subtitle={`${relationships.length} relaciones en el sistema`}
        />
        
        {isLoading ? (
          <div className="text-center py-8 text-gray-500">Cargando relaciones...</div>
        ) : relationships.length === 0 ? (
          <div className="text-center py-8">
            <Link2 className="w-16 h-16 text-gray-300 mx-auto mb-3" />
            <p className="text-gray-500">No hay relaciones creadas</p>
            <p className="text-sm text-gray-400 mt-1">
              Crea tu primera relación usando el formulario de arriba
            </p>
          </div>
        ) : (
          <div className="space-y-2">
            {relationships.map((rel: any, idx: number) => (
              <div
                key={idx}
                className="flex items-center justify-between p-4 bg-gray-50 rounded-xl hover:bg-gray-100 transition-colors"
              >
                <div className="flex-1">
                  <div className="flex items-center gap-3 mb-2">
                    <Badge variant="primary">{rel.from || rel.fromCode}</Badge>
                    <span className="text-gray-400 font-bold">↔</span>
                    <Badge variant="primary">{rel.to || rel.toCode}</Badge>
                  </div>
                  <div className="text-sm text-gray-600 space-y-1">
                    <p>
                      <strong>{getCourseName(rel.from || rel.fromCode)}</strong> ↔{' '}
                      <strong>{getCourseName(rel.to || rel.toCode)}</strong>
                    </p>
                    {editingRelation?.from === (rel.from || rel.fromCode) && 
                     editingRelation?.to === (rel.to || rel.toCode) ? (
                      <div className="flex items-center gap-2 mt-2">
                        <Input
                          type="range"
                          min="0"
                          max="1"
                          step="0.01"
                          value={newSimilarity}
                          onChange={(e) => setNewSimilarity(parseFloat(e.target.value))}
                          className="flex-1"
                        />
                        <span className="text-sm font-medium">{newSimilarity.toFixed(2)}</span>
                        <Button
                          variant="primary"
                          size="sm"
                          onClick={() => handleUpdate(rel.from || rel.fromCode, rel.to || rel.toCode)}
                        >
                          Guardar
                        </Button>
                        <Button
                          variant="secondary"
                          size="sm"
                          onClick={() => setEditingRelation(null)}
                        >
                          Cancelar
                        </Button>
                      </div>
                    ) : (
                      <p className="text-gray-700">
                        Similaridad: <strong>{((rel.similarity || 0) * 100).toFixed(0)}%</strong>
                      </p>
                    )}
                  </div>
                </div>
                
                <div className="flex items-center gap-2">
                  {!editingRelation && (
                    <>
                      <Button
                        variant="secondary"
                        size="sm"
                        onClick={() => {
                          setEditingRelation({ from: rel.from || rel.fromCode, to: rel.to || rel.toCode });
                          setNewSimilarity(rel.similarity || 0.5);
                        }}
                      >
                        <Edit className="w-4 h-4" />
                      </Button>
                      <Button
                        variant="danger"
                        size="sm"
                        onClick={() => {
                          if (window.confirm('¿Eliminar esta relación?')) {
                            deleteMutation.mutate({ 
                              from: rel.from || rel.fromCode, 
                              to: rel.to || rel.toCode 
                            });
                          }
                        }}
                      >
                        <Trash2 className="w-4 h-4" />
                      </Button>
                    </>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </Card>

      {/* Info sobre relaciones RELATED */}
      <Card>
        <CardHeader title="Información sobre Relaciones RELATED" />
        <div className="space-y-3 text-sm text-gray-600">
          <p>
            Las relaciones RELATED indican similaridad conceptual entre materias.
            Son útiles para:
          </p>
          <ul className="list-disc list-inside space-y-1 ml-3">
            <li>Encontrar materias relacionadas temáticamente</li>
            <li>Calcular el Árbol de Expansión Mínima (MST)</li>
            <li>Recomendar materias similares a estudiantes</li>
            <li>Agrupar materias por áreas de conocimiento</li>
          </ul>
          <p className="pt-2 border-t border-gray-200">
            <strong>Nota:</strong> Las relaciones RELATED son diferentes de los prerequisitos.
            Los prerequisitos son obligatorios, las relaciones RELATED son informativas.
          </p>
        </div>
      </Card>
    </div>
  );
}

