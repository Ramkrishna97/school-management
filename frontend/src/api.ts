import axios from 'axios'
import { create } from 'zustand'
import { createJSONStorage, persist } from 'zustand/middleware'

export type Role = 'SUPER_ADMIN' | 'PRINCIPAL' | 'TEACHER' | 'ACCOUNTANT' |
  'LIBRARIAN' | 'STORE_MANAGER' | 'STUDENT' | 'PARENT'

export interface Session {
  accessToken: string
  username: string
  role: Role
  teacherId: number | null
}

interface SessionState {
  session: Session | null
  setSession: (session: Session) => void
  clear: () => void
}

export const useSession = create<SessionState>()(persist(
  (set) => ({
    session: null,
    setSession: (session) => set({ session }),
    clear: () => set({ session: null }),
  }),
  { name: 'school-session', storage: createJSONStorage(() => sessionStorage) },
))

export const client = axios.create({ baseURL: '/api', timeout: 10000 })
client.interceptors.request.use((config) => {
  const token = useSession.getState().session?.accessToken
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})
client.interceptors.response.use((response) => response, (error) => {
  if (error.response?.status === 401 && !error.config?.url?.includes('/auth/login')) {
    useSession.getState().clear()
  }
  return Promise.reject(error)
})

export interface Teacher {
  id: number
  name: string
  department: string
  qualification: string
  classTeacherOf: string | null
  subjectCodes: string[]
  casualLeaveBalance: number
  sickLeaveBalance: number
  earnedLeaveBalance: number
}

export interface TeacherLeave {
  id: number
  teacherId: number
  startDate: string
  endDate: string
  type: 'CASUAL' | 'SICK' | 'EARNED'
  status: 'PENDING' | 'APPROVED' | 'REJECTED'
  reason: string
}

export interface TimetableEntry {
  id: number
  dayOfWeek: string
  periodNumber: number
  className: string
  section: string
  subjectCode: string
  teacherId: number
}

export interface Allocation {
  timetableEntryId: number
  periodNumber: number
  className: string
  section: string
  subjectCode: string
  absentTeacherId: number
  substituteTeacherId: number | null
  status: string
}

export interface DailyPeriod {
  date: string
  periodNumber: number
  className: string
  section: string
  subjectCode: string | null
  scheduledTeacherId: number | null
  effectiveTeacherId: number | null
  status: 'FREE' | 'REGULAR' | 'SUBSTITUTED' | 'UNFILLED'
}

export interface SubstitutionNotice {
  id: number
  substitutionId: number
  teacherId: number
  message: string
  createdAt: string
  status: 'ACTIVE' | 'CANCELLED'
}

export interface InventoryItem {
  id: number
  sku: string
  name: string
  category: string
  quantity: number
  minimumQuantity: number
  reorderQuantity: number
  status: string
  needsReorder: boolean
}

export interface PurchaseRequisition {
  id: number
  itemId: number
  quantity: number
  status: string
  createdAt: string
}

export interface VendorOrder {
  id: number
  requisitionId: number
  vendorName: string
  quantity: number
  status: string
}

export function errorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    return error.response?.data?.message ?? error.message
  }
  return error instanceof Error ? error.message : 'Request failed'
}
