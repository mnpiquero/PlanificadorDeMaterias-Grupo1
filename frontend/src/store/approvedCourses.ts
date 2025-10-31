import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';

interface ApprovedCoursesState {
  approvedCodes: string[];
  toggle: (code: string) => void;
  add: (code: string) => void;
  remove: (code: string) => void;
  clear: () => void;
  has: (code: string) => boolean;
  getSet: () => Set<string>;
}

export const useApprovedCourses = create<ApprovedCoursesState>()(
  persist(
    (set, get) => ({
      approvedCodes: [],
      
      toggle: (code: string) => {
        const current = get().approvedCodes;
        const updated = current.includes(code)
          ? current.filter(c => c !== code)
          : [...current, code];
        set({ approvedCodes: updated });
      },
      
      add: (code: string) => {
        const current = get().approvedCodes;
        if (!current.includes(code)) {
          set({ approvedCodes: [...current, code] });
        }
      },
      
      remove: (code: string) => {
        set({ approvedCodes: get().approvedCodes.filter(c => c !== code) });
      },
      
      clear: () => set({ approvedCodes: [] }),
      
      has: (code: string) => get().approvedCodes.includes(code),
      
      getSet: () => new Set(get().approvedCodes),
    }),
    {
      name: 'approved-courses-storage',
      storage: createJSONStorage(() => localStorage),
    }
  )
);

