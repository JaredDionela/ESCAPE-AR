import { supabase } from '../supabase'
import type { Profile } from '../../types/database.types'

// Fetch all users (filtered by teacher for students)
export async function getAllUsers() {
  // Get current logged-in user
  const { data: { user } } = await supabase.auth.getUser()
  
  if (!user) throw new Error('Not authenticated')
  
  // Get current user's profile to check role
  const { data: currentProfile } = await supabase
    .from('profiles')
    .select('role')
    .eq('id', user.id)
    .single()
  
  // If teacher, only show their students
  if (currentProfile?.role === 'teacher') {
    const { data, error } = await supabase
      .from('profiles')
      .select('*')
      .eq('teacher_id', user.id)  // Only their students
      .eq('role', 'student')
      .order('created_at', { ascending: false })
    
    if (error) throw error
    return data as Profile[]
  }
  
  // If not teacher (shouldn't happen with auth), return empty
  return []
}

// Get single user
export async function getUser(id: string) {
  const { data, error } = await supabase
    .from('profiles')
    .select('*')
    .eq('id', id)
    .single()
  
  if (error) throw error
  return data as Profile
}

// Update user profile
export async function updateUser(id: string, updates: Partial<Profile>) {
  const { data, error} = await supabase
    .from('profiles')
    .update(updates)
    .eq('id', id)
    .select()
    .single()
  
  if (error) throw error
  return data as Profile
}

// Register new student (linked to current teacher)
export async function registerStudent(studentData: {
  email: string
  password: string
  display_name: string
  section?: string
}) {
  // Get current teacher
  const { data: { user } } = await supabase.auth.getUser()
  if (!user) throw new Error('Not authenticated')
  
  // Verify user is a teacher
  const { data: teacherProfile } = await supabase
    .from('profiles')
    .select('role, display_name')
    .eq('id', user.id)
    .single()
    
  if (teacherProfile?.role !== 'teacher') {
    throw new Error('Only teachers can register students')
  }
  
  // Create auth user
  const { data: authData, error: authError } = await supabase.auth.admin.createUser({
    email: studentData.email,
    password: studentData.password,
    email_confirm: true,  // Auto-confirm for admin-created accounts
    user_metadata: {
      display_name: studentData.display_name,
      role: 'student'
    }
  })
  
  if (authError) throw authError
  if (!authData.user) throw new Error('Failed to create user')
  
  // Create profile linked to current teacher
  const { error: profileError } = await supabase
    .from('profiles')
    .insert({
      id: authData.user.id,
      email: studentData.email,
      display_name: studentData.display_name,
      role: 'student',
      teacher_id: user.id,  // Link to current teacher
      teacher_name: teacherProfile.display_name,  // Will be synced by trigger
      section: studentData.section || null
    })
  
  if (profileError) throw profileError
  
  return authData.user
}

// Delete user
export async function deleteUser(id: string) {
  const { error } = await supabase
    .from('profiles')
    .delete()
    .eq('id', id)
  
  if (error) throw error
}

// Get user statistics with detailed module breakdown
export async function getUserStats(userId: string) {
  try {
    // Get quiz results using NEW summary fields
    const { data: quizResults, error: quizError } = await supabase
      .from('quiz_results')
      .select('module_id, score_percentage, total_questions, correct_answers')
      .eq('user_id', userId)
      .not('score_percentage', 'is', null) // Only summary records
    
    if (quizError) throw quizError

    // Get progress data (best scores per module)
    const { data: progressData, error: progressError } = await supabase
      .from('progress')
      .select('module, best_score, completed')
      .eq('user_id', userId)
    
    if (progressError) throw progressError

    // Note: lesson_progress table has been removed - video tracking handled by YouTube
    const completedLessons = 0 // No longer tracked
    const totalQuizzes = quizResults?.length || 0
    
    // Calculate overall accuracy from summary records
    let totalScore = 0
    if (quizResults && quizResults.length > 0) {
      totalScore = quizResults.reduce((sum, r) => sum + (r.score_percentage || 0), 0)
    }
    const quizAccuracy = totalQuizzes > 0 ? Math.round(totalScore / totalQuizzes) : 0

    // Build detailed module breakdown
    const modules = ['decantation', 'organ_system', 'simple_machines', 'solar_system']
    const moduleNames: Record<string, string> = {
      'decantation': 'Decantation',
      'organ_system': 'Organ System',
      'simple_machines': 'Simple Machines',
      'solar_system': 'Solar System'
    }

    const moduleBreakdown = modules.map(moduleId => {
      const progress = progressData?.find(p => p.module === moduleId)
      const quizAttempts = quizResults?.filter(q => q.module_id === moduleId) || []
      
      // Get the latest quiz attempt for this module
      const latestQuiz = quizAttempts.length > 0 ? quizAttempts[quizAttempts.length - 1] : null
      
      return {
        moduleId,
        moduleName: moduleNames[moduleId] || moduleId,
        completed: progress?.completed || false,
        bestScore: progress?.best_score || 0,
        totalAttempts: quizAttempts.length,
        latestScore: latestQuiz?.score_percentage || null,
        totalQuestions: latestQuiz?.total_questions || null,
        correctAnswers: latestQuiz?.correct_answers || null
      }
    })

    return {
      completedLessons,
      totalQuizzes,
      quizAccuracy,
      moduleBreakdown
    }
  } catch (error) {
    console.error('Error fetching user stats:', error)
    return {
      completedLessons: 0,
      totalQuizzes: 0,
      quizAccuracy: 0,
      moduleBreakdown: []
    }
  }
}

// Get detailed lesson progress for a user
// NOTE: lesson_progress table removed - video tracking handled by YouTube
export async function getUserLessonProgress(_userId: string) {
  // Return empty array since lesson progress is no longer tracked in database
  // YouTube embedded player maintains playback history
  console.info('Lesson progress tracking is handled by YouTube, not database');
  return [];
}
