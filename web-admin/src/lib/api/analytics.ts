import { supabase } from '../supabase'

export interface DashboardStats {
  totalUsers: number
  totalLessons: number
  totalQuizQuestions: number
  completedQuizzes: number
  recentActivity: Array<{
    type: string
    message: string
    timestamp: string
  }>
}

export async function getDashboardStats(): Promise<DashboardStats> {
  try {
    console.log('Fetching dashboard stats...');
    
    // Get current teacher ID
    const { data: { user } } = await supabase.auth.getUser()
    const teacherId = user?.id
    
    // Get total users (only teacher's students)
    const { count: usersCount, error: usersError } = await supabase
      .from('profiles')
      .select('*', { count: 'exact', head: true })
      .eq('teacher_id', teacherId)  // Filter by teacher
      .eq('role', 'student')
    
    if (usersError) {
      console.error('Error fetching users count:', usersError);
    }

    // Get total lessons
    const { count: lessonsCount, error: lessonsError } = await supabase
      .from('lessons')
      .select('*', { count: 'exact', head: true })
    
    if (lessonsError) {
      console.error('Error fetching lessons count:', lessonsError);
    }

    // Get total quiz questions
    const { count: quizzesCount, error: quizzesError } = await supabase
      .from('quiz_questions')
      .select('*', { count: 'exact', head: true })
    
    if (quizzesError) {
      console.error('Error fetching quiz questions count:', quizzesError);
    }

    // Get completed quizzes (unique users who completed at least one quiz)
    // Only count teacher's students - first get student IDs
    const { data: teacherStudents } = await supabase
      .from('profiles')
      .select('id')
      .eq('teacher_id', teacherId)
      .eq('role', 'student')
    
    const studentIds = teacherStudents?.map(s => s.id) || []
    
    let completedQuizzes = 0
    if (studentIds.length > 0) {
      const { data: completedUsers, error: completedError } = await supabase
        .from('progress')
        .select('user_id')
        .eq('completed', true)
        .in('user_id', studentIds)
      
      if (completedError) {
        console.error('Error fetching completed quizzes:', completedError);
      }

      // Count unique users
      const uniqueUsers = new Set(completedUsers?.map(u => u.user_id) || [])
      completedQuizzes = uniqueUsers.size
    }

    // Get recent activity
    const recentActivity = await getRecentActivity()

    const stats = {
      totalUsers: usersCount || 0,
      totalLessons: lessonsCount || 0,
      totalQuizQuestions: quizzesCount || 0,
      completedQuizzes,
      recentActivity
    };
    
    console.log('Dashboard stats loaded:', stats);
    return stats;
  } catch (error) {
    console.error('Error fetching dashboard stats:', error)
    return {
      totalUsers: 0,
      totalLessons: 0,
      totalQuizQuestions: 0,
      completedQuizzes: 0,
      recentActivity: []
    }
  }
}

async function getRecentActivity() {
  const activities: Array<{ type: string; message: string; timestamp: string }> = []

  try {
    // Get current teacher's ID
    const { data: { user } } = await supabase.auth.getUser()
    const teacherId = user?.id

    if (!teacherId) {
      return []
    }

    // Get teacher's students
    const { data: teacherStudents } = await supabase
      .from('profiles')
      .select('id')
      .eq('teacher_id', teacherId)
      .eq('role', 'student')

    const studentIds = teacherStudents?.map(s => s.id) || []

    if (studentIds.length === 0) {
      return []
    }

    // Recent users (new students for this teacher)
    const { data: recentUsers } = await supabase
      .from('profiles')
      .select('display_name, created_at')
      .eq('teacher_id', teacherId)
      .eq('role', 'student')
      .order('created_at', { ascending: false })
      .limit(3)

    if (recentUsers) {
      recentUsers.forEach(user => {
        activities.push({
          type: 'user',
          message: `New user: ${user.display_name}`,
          timestamp: user.created_at
        })
      })
    }

    // Note: Lesson completion tracking removed - video progress handled by YouTube
    // Recent quiz completions instead (only for teacher's students)
    const { data: recentQuizzes } = await supabase
      .from('quiz_results')
      .select(`
        created_at,
        score_percentage,
        module_id,
        user_id,
        profiles!inner(display_name)
      `)
      .in('user_id', studentIds)
      .not('score_percentage', 'is', null) // Only summary records
      .order('created_at', { ascending: false })
      .limit(5)

    if (recentQuizzes) {
      recentQuizzes.forEach((quiz: any) => {
        const moduleNames: Record<string, string> = {
          'decantation': 'Decantation',
          'organ_system': 'Organ System',
          'simple_machines': 'Simple Machines',
          'solar_system': 'Solar System'
        }
        const moduleName = moduleNames[quiz.module_id] || quiz.module_id
        activities.push({
          type: 'quiz',
          message: `${quiz.profiles.display_name} scored ${Math.round(quiz.score_percentage)}% on ${moduleName} quiz`,
          timestamp: quiz.created_at
        })
      })
    }

    // Sort by timestamp
    activities.sort((a, b) => 
      new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime()
    )

    return activities.slice(0, 10)
  } catch (error) {
    console.error('Error fetching recent activity:', error)
    return []
  }
}

export interface ModuleProgress {
  module_id: string
  module_name: string
  total_lessons: number
  completed_lessons: number
  completion_rate: number
}

export async function getModuleProgress(): Promise<ModuleProgress[]> {
  try {
    console.log('Fetching module progress...');
    
    // Get current teacher's ID
    const { data: { user } } = await supabase.auth.getUser()
    const teacherId = user?.id

    if (!teacherId) {
      return []
    }

    // Get teacher's students
    const { data: teacherStudents } = await supabase
      .from('profiles')
      .select('id')
      .eq('teacher_id', teacherId)
      .eq('role', 'student')

    const studentIds = teacherStudents?.map(s => s.id) || []

    if (studentIds.length === 0) {
      return []
    }

    const modules = ['decantation', 'organ_system', 'simple_machines', 'solar_system']
    const moduleNames: Record<string, string> = {
      'decantation': 'Decantation',
      'organ_system': 'Organ System',
      'simple_machines': 'Simple Machines',
      'solar_system': 'Solar System'
    }

    const progress: ModuleProgress[] = []

    for (const moduleId of modules) {
      // Count total lessons in this module
      const { count: totalLessons, error: countError } = await supabase
        .from('lessons')
        .select('*', { count: 'exact', head: true })
        .eq('module_id', moduleId)
      
      if (countError) {
        console.error(`Error counting lessons for module ${moduleId}:`, countError);
      }

      // Note: lesson_progress table removed - using completed modules from progress table instead
      // Count unique users (from teacher's students) who have completed this module
      const { count: completedUsers, error: progressError } = await supabase
        .from('progress')
        .select('*', { count: 'exact', head: true })
        .eq('module', moduleId)
        .eq('completed', true)
        .in('user_id', studentIds)
      
      if (progressError) {
        console.error(`Error counting completed users for module ${moduleId}:`, progressError);
      }

      // Calculate completion rate based on teacher's students who completed the module
      const totalUsers = studentIds.length

      progress.push({
        module_id: moduleId,
        module_name: moduleNames[moduleId] || moduleId,
        total_lessons: totalLessons || 0,
        completed_lessons: completedUsers || 0, // Repurposed: now means users who completed module
        completion_rate: totalUsers > 0 
          ? ((completedUsers || 0) / totalUsers) * 100 
          : 0
      })
    }

    console.log('Module progress loaded:', progress);
    return progress
  } catch (error) {
    console.error('Error fetching module progress:', error)
    return []
  }
}

export interface DashboardMetrics {
  quizCompletionRate: number
  totalCompletions: number
  totalPossible: number
  averageScore: number
  studentsNeedingHelp: number
  topPerformer: {
    name: string
    score: number
    modulesCompleted: number
  } | null
  recentCompletions: Array<{
    studentName: string
    module: string
    score: number
    createdAt: string
  }>
}

export async function getDashboardMetrics(): Promise<DashboardMetrics> {
  try {
    console.log('Fetching dashboard metrics...')

    // Get current teacher's ID
    const { data: { user } } = await supabase.auth.getUser()
    const teacherId = user?.id

    if (!teacherId) {
      return {
        quizCompletionRate: 0,
        totalCompletions: 0,
        totalPossible: 0,
        averageScore: 0,
        studentsNeedingHelp: 0,
        topPerformer: null,
        recentCompletions: []
      }
    }

    // Get teacher's students
    const { data: teacherStudents } = await supabase
      .from('profiles')
      .select('id')
      .eq('teacher_id', teacherId)
      .eq('role', 'student')

    const studentIds = teacherStudents?.map(s => s.id) || []

    if (studentIds.length === 0) {
      return {
        quizCompletionRate: 0,
        totalCompletions: 0,
        totalPossible: 0,
        averageScore: 0,
        studentsNeedingHelp: 0,
        topPerformer: null,
        recentCompletions: []
      }
    }

    // 1. Get total users and calculate possible completions
    const totalUsers = studentIds.length
    const totalModules = 4 // decantation, organ_system, simple_machines, solar_system
    const totalPossible = totalUsers * totalModules

    console.log('Total users:', totalUsers, 'Total possible completions:', totalPossible)

    // 2. Get completion rate from progress table (only for teacher's students)
    const { count: totalCompletions, error: completionsError } = await supabase
      .from('progress')
      .select('*', { count: 'exact', head: true })
      .eq('completed', true)
      .in('user_id', studentIds)
    
    if (completionsError) {
      console.error('Error counting completions:', completionsError)
    }

    console.log('Total completions from progress table:', totalCompletions)

    const quizCompletionRate = totalPossible > 0 
      ? Math.round(((totalCompletions || 0) / totalPossible) * 100) 
      : 0
    
    console.log('Quiz completion rate:', quizCompletionRate + '%')

    // 3. Get average score across all completed quizzes (only for teacher's students)
    const { data: scores, error: scoresError } = await supabase
      .from('progress')
      .select('best_score')
      .eq('completed', true)
      .in('user_id', studentIds)
    
    if (scoresError) {
      console.error('Error fetching scores:', scoresError)
    }

    const averageScore = scores && scores.length > 0
      ? Math.round(scores.reduce((sum, s) => sum + (s.best_score || 0), 0) / scores.length)
      : 0

    // 4. Get students needing help (best score < 50% in any module, only teacher's students)
    const { data: strugglingStudents, error: strugglingError } = await supabase
      .from('progress')
      .select('user_id')
      .eq('completed', true)
      .lt('best_score', 50)
      .in('user_id', studentIds)
    
    if (strugglingError) {
      console.error('Error fetching struggling students:', strugglingError)
    }

    const uniqueStrugglingStudents = new Set(strugglingStudents?.map(s => s.user_id) || [])
    const studentsNeedingHelp = uniqueStrugglingStudents.size

    // 5. Get top performer (only from teacher's students)
    const { data: topPerformers, error: topError } = await supabase
      .from('progress')
      .select(`
        user_id,
        best_score,
        profiles!inner(full_name)
      `)
      .eq('completed', true)
      .in('user_id', studentIds)
    
    if (topError) {
      console.error('Error fetching top performers:', topError)
    }

    console.log('Top performers raw data:', topPerformers)

    // Calculate average score per user
    const userScores = new Map<string, { totalScore: number; count: number; name: string }>()
    topPerformers?.forEach((record: any) => {
      const userId = record.user_id
      const name = record.profiles?.full_name || 'Unknown'
      console.log(`Processing user ${userId}: name=${name}, score=${record.best_score}`)
      if (!userScores.has(userId)) {
        userScores.set(userId, { totalScore: 0, count: 0, name })
      }
      const userScore = userScores.get(userId)!
      userScore.totalScore += record.best_score || 0
      userScore.count += 1
    })

    console.log('User scores map:', Array.from(userScores.entries()))

    const topPerformerData = Array.from(userScores.entries())
      .map(([userId, data]) => ({
        userId,
        name: data.name,
        score: Math.round(data.totalScore / data.count),
        modulesCompleted: data.count
      }))
      .sort((a, b) => b.score - a.score)[0] || null

    console.log('Top performer final:', topPerformerData)

    // 6. Get recent completions (last 5, only for teacher's students)
    const { data: recentData, error: recentError } = await supabase
      .from('quiz_results')
      .select(`
        user_id,
        module_id,
        score_percentage,
        created_at,
        profiles!inner(full_name)
      `)
      .not('score_percentage', 'is', null)
      .in('user_id', studentIds)
      .order('created_at', { ascending: false })
      .limit(5)
    
    if (recentError) {
      console.error('Error fetching recent completions:', recentError)
    }

    console.log('Recent completions raw data:', recentData)

    const moduleNames: Record<string, string> = {
      'decantation': 'Decantation',
      'filtration': 'Filtration',
      'evaporation': 'Evaporation',
      'distillation': 'Distillation',
      'organ_system': 'Organ System',
      'simple_machines': 'Simple Machines',
      'solar_system': 'Solar System'
    }

    const recentCompletions = recentData?.map((record: any) => ({
      studentName: record.profiles?.full_name || 'Unknown',
      module: moduleNames[record.module_id] || record.module_id,
      score: Math.round(record.score_percentage || 0),
      createdAt: record.created_at
    })) || []

    const metrics = {
      quizCompletionRate,
      totalCompletions: totalCompletions || 0,
      totalPossible,
      averageScore,
      studentsNeedingHelp,
      topPerformer: topPerformerData,
      recentCompletions
    }

    console.log('Dashboard metrics loaded:', metrics)
    return metrics
  } catch (error) {
    console.error('Error fetching dashboard metrics:', error)
    return {
      quizCompletionRate: 0,
      totalCompletions: 0,
      totalPossible: 0,
      averageScore: 0,
      studentsNeedingHelp: 0,
      topPerformer: null,
      recentCompletions: []
    }
  }
}

export interface ModulePerformance {
  module: string
  averageScore: number
  completions: number
}

export async function getModulePerformance(): Promise<ModulePerformance[]> {
  try {
    console.log('Fetching module performance...')

    // Get current teacher's ID
    const { data: { user } } = await supabase.auth.getUser()
    const teacherId = user?.id

    if (!teacherId) {
      return []
    }

    // Get teacher's students
    const { data: teacherStudents } = await supabase
      .from('profiles')
      .select('id')
      .eq('teacher_id', teacherId)
      .eq('role', 'student')

    const studentIds = teacherStudents?.map(s => s.id) || []

    if (studentIds.length === 0) {
      return []
    }

    const { data: moduleData, error: moduleError } = await supabase
      .from('progress')
      .select('module, best_score, completed')
      .eq('completed', true)
      .in('user_id', studentIds)
    
    if (moduleError) {
      console.error('Error fetching module performance:', moduleError)
      return []
    }

    const moduleNames: Record<string, string> = {
      'decantation': 'Decantation',
      'filtration': 'Filtration',
      'evaporation': 'Evaporation',
      'distillation': 'Distillation',
      'organ_system': 'Organ System',
      'simple_machines': 'Simple Machines',
      'solar_system': 'Solar System'
    }

    const moduleStats = new Map<string, { totalScore: number; count: number }>()
    moduleData?.forEach(record => {
      const moduleName = moduleNames[record.module] || record.module
      if (!moduleStats.has(moduleName)) {
        moduleStats.set(moduleName, { totalScore: 0, count: 0 })
      }
      const stats = moduleStats.get(moduleName)!
      stats.totalScore += record.best_score || 0
      stats.count += 1
    })

    const modulePerformance = Array.from(moduleStats.entries())
      .map(([module, stats]) => ({
        module,
        averageScore: Math.round(stats.totalScore / stats.count),
        completions: stats.count
      }))
      .sort((a, b) => b.averageScore - a.averageScore)

    console.log('Module performance loaded:', modulePerformance)
    return modulePerformance
  } catch (error) {
    console.error('Error fetching module performance:', error)
    return []
  }
}

export interface QuizAnalytics {
  module_id: string
  module_name: string
  total_attempts: number
  average_score: number
  average_questions: number
  pass_rate: number
}

export async function getQuizAnalytics(): Promise<QuizAnalytics[]> {
  try {
    console.log('Fetching quiz analytics with unique user completions...')
    
    // Get current teacher's ID
    const { data: { user } } = await supabase.auth.getUser()
    const teacherId = user?.id

    if (!teacherId) {
      return []
    }

    // Get teacher's students
    const { data: teacherStudents } = await supabase
      .from('profiles')
      .select('id')
      .eq('teacher_id', teacherId)
      .eq('role', 'student')

    const studentIds = teacherStudents?.map(s => s.id) || []

    if (studentIds.length === 0) {
      return []
    }
    
    const modules = ['decantation', 'organ_system', 'simple_machines', 'solar_system']
    const moduleNames: Record<string, string> = {
      'decantation': 'Decantation',
      'organ_system': 'Organ System',
      'simple_machines': 'Simple Machines',
      'solar_system': 'Solar System'
    }
    
    const analytics: QuizAnalytics[] = []
    
    for (const moduleId of modules) {
      // Get progress data (shows unique users who completed this module, only teacher's students)
      const { data: progressData, error: progressError } = await supabase
        .from('progress')
        .select('user_id, best_score, completed')
        .eq('module', moduleId)
        .eq('completed', true) // Only completed quizzes
        .in('user_id', studentIds)
      
      if (progressError) {
        console.error(`Error fetching progress for ${moduleId}:`, progressError)
        continue
      }
      
      // Get all quiz attempts for average questions calculation (only for teacher's students)
      const { data: quizData, error: quizError } = await supabase
        .from('quiz_results')
        .select('total_questions, score_percentage')
        .eq('module_id', moduleId)
        .not('score_percentage', 'is', null)
        .in('user_id', studentIds)
      
      if (quizError) {
        console.error(`Error fetching quiz results for ${moduleId}:`, quizError)
      }
      
      const uniqueCompletions = progressData?.length || 0
      let averageScore = 0
      let averageQuestions = 0
      let passCount = 0
      
      if (uniqueCompletions > 0) {
        // Calculate average score from progress (best scores)
        const totalScore = progressData.reduce((sum, p) => sum + (p.best_score || 0), 0)
        averageScore = totalScore / uniqueCompletions
        passCount = progressData.filter(p => (p.best_score || 0) >= 50).length
        
        // Calculate average questions from quiz results
        if (quizData && quizData.length > 0) {
          const totalQuestions = quizData.reduce((sum, q) => sum + (q.total_questions || 0), 0)
          averageQuestions = totalQuestions / quizData.length
        }
      }
      
      analytics.push({
        module_id: moduleId,
        module_name: moduleNames[moduleId] || moduleId,
        total_attempts: uniqueCompletions, // Changed: unique users, not total attempts
        average_score: Math.round(averageScore * 10) / 10,
        average_questions: Math.round(averageQuestions * 10) / 10,
        pass_rate: uniqueCompletions > 0 ? Math.round((passCount / uniqueCompletions) * 100) : 0
      })
    }
    
    console.log('Quiz analytics loaded:', analytics)
    return analytics
  } catch (error) {
    console.error('Error fetching quiz analytics:', error)
    return []
  }
}
