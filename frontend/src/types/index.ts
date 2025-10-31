export interface Course {
  code: string;
  name: string;
  credits?: number;
  hours?: number;
  difficulty?: number;
  prereqs?: Course[];
}

export interface RelatedRelationship {
  fromCode: string;
  toCode: string;
  similarity?: number;
}

export interface SearchCriteria {
  nameContains?: string;
  minCredits?: number;
  maxCredits?: number;
  minDifficulty?: number;
  maxDifficulty?: number;
  minHours?: number;
  maxHours?: number;
}

export interface GraphResult {
  nodes: string[];
}

export interface ScheduleResult {
  courses: Course[];
  totalCredits?: number;
  totalHours?: number;
}

export interface MSTEdge {
  u: string;
  v: string;
  w: number;
}

export interface ApprovedCoursesStore {
  approvedCodes: Set<string>;
  toggle: (code: string) => void;
  add: (code: string) => void;
  remove: (code: string) => void;
  clear: () => void;
  has: (code: string) => boolean;
}

export type ScheduleAlgorithm = 'greedy' | 'dp' | 'bnb';
export type ScheduleValue = 'credits' | 'difficulty' | 'hours';

