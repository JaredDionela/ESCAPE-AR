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
    
    // Get total users
    const { count: usersCount, error: usersError } = await supabase
      .from('profiles')
      .select('*', { count: 'exact', head: true })
    
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
    const { data: completedUsers, error: completedError } = await supabase
      .from('progress')
      .select('user_id')
      .eq('completed', true)
    
    if (completedError) {
      console.error('Error fetching completed quizzes:', completedError);
    }

    // Count unique users
    const uniqueUsers = new Set(completedUsers?.map(u => u.user_id) || [])
    const completedQuizzes = uniqueUsers.size

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
    // Recent users
    const { data: recentUsers } = await supabase
      .from('profiles')
      .select('display_name, created_at')
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

    // Recent lesson completions
    const { data: recentCompletions } = await supabase
      .from('lesson_progress')
      .select(`
        completed_at,
        profiles!inner(display_name),
        lessons!inner(title)
      `)
      .eq('completed', true)
      .order('completed_at', { ascending: false })
      .limit(5)

    if (recentCompletions) {
      recentCompletions.forEach((completion: any) => {
        activities.push({
          type: 'lesson',
          message: `${completion.profiles.display_name} completed "${completion.lessons.title}"`,
          timestamp: completion.completed_at
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
    
    const { error: lessonsError } = await supabase
      .from('lessons')
      .select('module_id')

    if (lessonsError) {
      console.error('Error fetching lessons for module progress:', lessonsError);
      throw lessonsError;
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
      const { count: totalLessons, error: countError } = await supabase
        .from('lessons')
        .select('*', { count: 'exact', head: true })
        .eq('module_id', moduleId)
      
      if (countError) {
        console.error(`Error counting lessons for module ${moduleId}:`, countError);
      }

      const { data: lessonData, error: lessonIdError } = await supabase
        .from('lessons')
        .select('id')
        .eq('module_id', moduleId)
      
      if (lessonIdError) {
        console.error(`Error fetching lesson IDs for module ${moduleId}:`, lessonIdError);
      }

      const lessonIds = lessonData?.map(l => l.id) || []
      
      let completedLessons = 0
      if (lessonIds.length > 0) {
        const { count, error: progressError } = await supabase
          .from('lesson_progress')
          .select('*', { count: 'exact', head: true })
          .eq('completed', true)
          .in('lesson_id', lessonIds)
        
        if (progressError) {
          console.error(`Error counting completed lessons for module ${moduleId}:`, progressError);
        }
        completedLessons = count || 0
      }

      progress.push({
        module_id: moduleId,
        module_name: moduleNames[moduleId] || moduleId,
        total_lessons: totalLessons || 0,
        completed_lessons: completedLessons,
        completion_rate: totalLessons ? ((completedLessons || 0) / totalLessons) * 100 : 0
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

    // 1. Get total users and calculate possible completions
    const { count: totalUsers, error: usersError } = await supabase
      .from('profiles')
      .select('*', { count: 'exact', head: true })
    
    if (usersError) {
      console.error('Error counting users:', usersError)
    }

    const totalModules = 4 // decantation, organ_system, simple_machines, solar_system
    const totalPossible = (totalUsers || 0) * totalModules

    console.log('Total users:', totalUsers, 'Total possible completions:', totalPossible)

    // 2. Get completion rate from progress table
    const { count: totalCompletions, error: completionsError } = await supabase
      .from('progress')
      .select('*', { count: 'exact', head: true })
      .eq('completed', true)
    
    if (completionsError) {
      console.error('Error counting completions:', completionsError)
    }

    console.log('Total completions from progress table:', totalCompletions)

    const quizCompletionRate = totalPossible > 0 
      ? Math.round(((totalCompletions || 0) / totalPossible) * 100) 
      : 0
    
    console.log('Quiz completion rate:', quizCompletionRate + '%')

    // 3. Get average score across all completed quizzes
    const { data: scores, error: scoresError } = await supabase
      .from('progress')
      .select('best_score')
      .eq('completed', true)
    
    if (scoresError) {
      console.error('Error fetching scores:', scoresError)
    }

    const averageScore = scores && scores.length > 0
      ? Math.round(scores.reduce((sum, s) => sum + (s.best_score || 0), 0) / scores.length)
      : 0

    // 4. Get students needing help (best score < 50% in any module)
    const { data: strugglingStudents, error: strugglingError } = await supabase
      .from('progress')
      .select('user_id')
      .eq('completed', true)
      .lt('best_score', 50)
    
    if (strugglingError) {
      console.error('Error fetching struggling students:', strugglingError)
    }

    const uniqueStrugglingStudents = new Set(strugglingStudents?.map(s => s.user_id) || [])
    const studentsNeedingHelp = uniqueStrugglingStudents.size

    // 5. Get top performer
    const { data: topPerformers, error: topError } = await supabase
      .from('progress')
      .select(`
        user_id,
        best_score,
        profiles!inner(full_name)
      `)
      .eq('completed', true)
    
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

    // 6. Get recent completions (last 5)
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

    const { data: moduleData, error: moduleError } = await supabase
      .from('progress')
      .select('module, best_score, completed')
      .eq('completed', true)
    
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
    
    const modules = ['decantation', 'organ_system', 'simple_machines', 'solar_system']
    const moduleNames: Record<string, string> = {
      'decantation': 'Decantation',
      'organ_system': 'Organ System',
      'simple_machines': 'Simple Machines',
      'solar_system': 'Solar System'
    }
    
    const analytics: QuizAnalytics[] = []
    
    for (const moduleId of modules) {
      // Get progress data (shows unique users who completed this module)
      const { data: progressData, error: progressError } = await supabase
        .from('progress')
        .select('user_id, best_score, completed')
        .eq('module', moduleId)
        .eq('completed', true) // Only completed quizzes
      
      if (progressError) {
        console.error(`Error fetching progress for ${moduleId}:`, progressError)
        continue
      }
      
      // Get all quiz attempts for average questions calculation
      const { data: quizData, error: quizError } = await supabase
        .from('quiz_results')
        .select('total_questions, score_percentage')
        .eq('module_id', moduleId)
        .not('score_percentage', 'is', null)
      
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
