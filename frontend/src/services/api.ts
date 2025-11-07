import axios from 'axios';
import type { Course, RelatedRelationship, SearchCriteria, MSTEdge } from '../types';

// Obtener URL del API desde variables de entorno
const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

// Validar que la URL esté configurada en producción
if (import.meta.env.PROD && !import.meta.env.VITE_API_URL) {
  console.warn('VITE_API_URL no está configurada. Usando URL por defecto.');
}

const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Courses API
export const coursesApi = {
  // Usar /list para obtener JSON en lugar de SSE
  getAll: () => api.get<Course[]>('/courses/list').then(res => res.data),
  
  getByCode: (code: string) => api.get<Course>(`/courses/${code}`).then(res => res.data),
  
  create: (course: Course) => api.post<Course>('/courses', course).then(res => res.data),
  
  update: (course: Course) => api.put<Course>('/courses', course).then(res => res.data),
  
  patch: (code: string, updates: Partial<Course>) => 
    api.patch<Course>(`/courses/${code}`, updates).then(res => res.data),
  
  delete: (code: string) => api.delete(`/courses/${code}`),
  
  searchByName: (name: string) => 
    api.get<Course[]>('/courses/search/by-name', { params: { name } }).then(res => res.data),
  
  advancedSearch: (criteria: SearchCriteria) => 
    api.get<Course[]>('/courses/search/advanced', { params: criteria }).then(res => res.data),
  
  exists: (code: string) => 
    api.get<boolean>(`/courses/${code}/exists`).then(res => res.data),
  
  addPrereqs: (code: string, prereqCodes: string[]) => 
    api.post<Course>(`/courses/${code}/prereqs`, prereqCodes).then(res => res.data),
};

// Relationships API
export const relationshipsApi = {
  getAll: () => api.get<any[]>('/relationships').then(res => res.data),
  
  getByCourse: (code: string) => 
    api.get<any[]>(`/relationships/${code}`).then(res => res.data),
  
  create: (relationship: RelatedRelationship) => 
    api.post<any>('/relationships', relationship).then(res => res.data),
  
  createAuto: (relationship: Omit<RelatedRelationship, 'similarity'>) => 
    api.post<any>('/relationships/auto', relationship).then(res => res.data),
  
  update: (fromCode: string, toCode: string, similarity: number) => 
    api.patch<any>(`/relationships/${fromCode}/${toCode}`, { similarity }).then(res => res.data),
  
  delete: (fromCode: string, toCode: string) => 
    api.delete(`/relationships/${fromCode}/${toCode}`).then(res => res.data),
};

// Graph API
export const graphApi = {
  dfs: (from: string) => 
    api.get<string[]>(`/graph/dfs`, { params: { from } }).then(res => res.data),
  
  bfsLayers: (from: string) => 
    api.get<string[][]>(`/graph/bfs-layers`, { params: { from } }).then(res => res.data),
  
  topoSort: (approved?: string[]) => {
    const params = new URLSearchParams();
    approved?.forEach(code => params.append('approved', code));
    return api.get<string[]>(`/graph/toposort?${params.toString()}`).then(res => res.data);
  },
  
  hasCycles: () => 
    api.get<{ hasCycle: boolean }>('/graph/cycles').then(res => res.data),
  
  shortestPath: (from: string, to: string, metric?: string) => 
    api.get<string[]>('/graph/shortest', { params: { from, to, metric } }).then(res => res.data),
  
  mst: (algo: 'prim' | 'kruskal' = 'prim') => 
    api.get<MSTEdge[]>('/graph/mst', { params: { algo } }).then(res => res.data),
};

// Schedule API
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
  
  backtracking: (from: string, to: string, maxDepth = 10) => 
    api.get<string[][]>('/schedule/backtracking', { params: { from, to, maxDepth } }).then(res => res.data),
  
  branchAndBound: (approved: string[] = [], semesters = 4, maxHours = 24) => {
    const params = new URLSearchParams();
    approved.forEach(code => params.append('approved', code));
    params.append('semesters', semesters.toString());
    params.append('maxHours', maxHours.toString());
    return api.get<string[][]>(`/schedule/bnb?${params.toString()}`).then(res => res.data);
  },
};

export default api;

