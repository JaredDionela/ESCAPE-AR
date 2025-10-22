// Database types matching your Supabase schema

export interface Lesson {
  id: string
  module_id: string
  title: string
  description: string
  youtube_video_id: string
  thumbnail_url?: string
  duration_minutes?: number  // Made optional since we removed this field from the UI
  order_index: number
  created_at: string
  updated_at: string
}

export interface LessonFile {
  id: string
  lesson_id: string
  file_name: string
  file_url: string
  file_type: string
  file_size: number
  uploaded_at: string
}

export interface LessonProgress {
  id: string
  user_id: string
  lesson_id: string
  completed: boolean
  video_progress: number
  last_watched_at?: string
  completed_at?: string
}

export interface Profile {
  id: string
  email: string
  display_name: string
  role: 'student' | 'teacher'
  teacher_id?: string | null       // NEW: Link to teacher account
  teacher_name?: string | null
  section?: string | null
  avatar_url?: string | null
  created_at: string
  updated_at: string
}

// Extended interfaces for type safety
export interface StudentProfile extends Profile {
  role: 'student'
  teacher_id: string  // Required for students
  teacher?: TeacherProfile  // Populated when joined
}

export interface TeacherProfile extends Profile {
  role: 'teacher'
  teacher_id: null  // Always null for teachers
  students?: StudentProfile[]  // Populated when needed
  student_count?: number  // Computed field
}

export interface QuizQuestion {
  id: string
  module_id: string
  question_text: string
  option_a: string
  option_b: string
  option_c: string
  option_d: string
  correct_answer: string // 'A', 'B', 'C', or 'D'
  order_index: number
  created_at?: string
  updated_at?: string
}

export interface QuizResult {
  id: string
  user_id: string
  question_id: string
  module_id: string
  selected_answer: string // 'A', 'B', 'C', or 'D'
  is_correct: boolean
  created_at: string
}

export type ModuleId = 'decantation' | 'organ_system' | 'simple_machines' | 'solar_system'

export const MODULES = [
  { id: 'decantation', name: 'Decantation', icon: '🧪', color: '#4CAF50' },
  { id: 'organ_system', name: 'Organ System', icon: '❤️', color: '#E91E63' },
  { id: 'simple_machines', name: 'Simple Machines', icon: '⚙️', color: '#FF9800' },
  { id: 'solar_system', name: 'Solar System', icon: '🌍', color: '#2196F3' }
] as const
