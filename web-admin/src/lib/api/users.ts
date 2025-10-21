import { supabase } from '../supabase'
import type { Profile } from '../../types/database.types'

// Fetch all users
export async function getAllUsers() {
  const { data, error } = await supabase
    .from('profiles')
    .select('*')
    .order('created_at', { ascending: false })
  
  if (error) throw error
  return data as Profile[]
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
  const { data, error } = await supabase
    .from('profiles')
    .update(updates)
    .eq('id', id)
    .select()
    .single()
  
  if (error) throw error
  return data as Profile
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
    // Get lesson progress
    const { data: lessonProgress, error: lessonError } = await supabase
      .from('lesson_progress')
      .select('*')
      .eq('user_id', userId)
    
    if (lessonError) throw lessonError

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

    const completedLessons = lessonProgress?.filter(l => l.completed).length || 0
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
export async function getUserLessonProgress(userId: string) {
  try {
    const { data, error } = await supabase
      .from('lesson_progress')
      .select(`
        *,
        lessons!inner(
          title,
          module_id,
          order_index
        )
      `)
      .eq('user_id', userId)
      .order('updated_at', { ascending: false });
    
    if (error) throw error;
    
    return data || [];
  } catch (error) {
    console.error('Error fetching user lesson progress:', error);
    return [];
  }
}
