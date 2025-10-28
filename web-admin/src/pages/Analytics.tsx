import { useState, useEffect } from 'react';
import {
  Box,
  Paper,
  Typography,
  Grid,
  Card,
  CardContent,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
  CircularProgress,
  Button,
} from '@mui/material';
import {
  Quiz as QuizIcon,
  CheckCircle as CheckCircleIcon,
} from '@mui/icons-material';
import { supabase } from '../lib/supabase';

interface AnalyticsData {
  userGrowth: number;
  lessonCompletionRate: number;
  quizAverageScore: number;
  activeUsers: number;
  topPerformers: Array<{
    name: string;
    score: number;
    completed: number;
  }>;
  moduleProgress: Array<{
    module: string;
    completed: number;
    total: number;
  }>;
}

export function Analytics() {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [data, setData] = useState<AnalyticsData>({
    userGrowth: 0,
    lessonCompletionRate: 0,
    quizAverageScore: 0,
    activeUsers: 0,
    topPerformers: [],
    moduleProgress: [],
  });

  useEffect(() => {
    loadAnalytics();
  }, []);

  const loadAnalytics = async () => {
    try {
      setError(null);
      
      // Get user growth (users created in last 30 days)
      const thirtyDaysAgo = new Date();
      thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);

      const { count: newUsers, error: userError } = await supabase
        .from('profiles')
        .select('*', { count: 'exact', head: true })
        .gte('created_at', thirtyDaysAgo.toISOString());
      
      if (userError) {
        console.error('Error fetching user growth:', userError);
      }

      // Get lesson completion rate - properly track from database
      const { data: allLessonProgress, error: allProgressError } = await supabase
        .from('lesson_progress')
        .select('lesson_id, completed, user_id');
      
      if (allProgressError) {
        console.error('Error fetching lesson progress:', allProgressError);
      }

      // Count unique lesson completions (unique user-lesson pairs)
      const completedSet = new Set();
      const totalSet = new Set();
      
      allLessonProgress?.forEach((progress: any) => {
        const key = `${progress.user_id}_${progress.lesson_id}`;
        totalSet.add(key);
        if (progress.completed) {
          completedSet.add(key);
        }
      });

      const lessonCompletionRate = totalSet.size > 0
        ? Math.round((completedSet.size / totalSet.size) * 100)
        : 0;

      // Get quiz average score using NEW summary fields
      const { data: quizResults, error: quizError } = await supabase
        .from('quiz_results')
        .select('score_percentage, total_questions, correct_answers')
        .not('score_percentage', 'is', null); // Only get summary records
      
      if (quizError) {
        console.error('Error fetching quiz results:', quizError);
      }

      let totalScore = 0;
      let quizCount = 0;
      if (quizResults && quizResults.length > 0) {
        quizCount = quizResults.length;
        totalScore = quizResults.reduce((sum: number, result: any) => 
          sum + (result.score_percentage || 0), 0);
      }
      const quizAverageScore = quizCount > 0 ? Math.round(totalScore / quizCount) : 0;

      // Get active users (users with activity in last 7 days)
      const sevenDaysAgo = new Date();
      sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7);

      // Get unique users from both lesson progress and quiz results
      const { data: lessonUsers, error: lessonUsersError } = await supabase
        .from('lesson_progress')
        .select('user_id')
        .gte('updated_at', sevenDaysAgo.toISOString());
      
      if (lessonUsersError) {
        console.error('Error fetching lesson users:', lessonUsersError);
      }

      const { data: quizUsers, error: quizUsersError } = await supabase
        .from('quiz_results')
        .select('user_id')
        .not('score_percentage', 'is', null) // Only summary records
        .gte('created_at', sevenDaysAgo.toISOString());
      
      if (quizUsersError) {
        console.error('Error fetching quiz users:', quizUsersError);
      }

      const activeUserIds = new Set([
        ...(lessonUsers?.map((u: any) => u.user_id) || []),
        ...(quizUsers?.map((u: any) => u.user_id) || []),
      ]);
      const activeUsers = activeUserIds.size;

      // Get current teacher's students for filtering
      const { data: { user } } = await supabase.auth.getUser();
      const teacherId = user?.id;
      
      let studentIds: string[] = [];
      if (teacherId) {
        const { data: teacherStudents } = await supabase
          .from('profiles')
          .select('id')
          .eq('teacher_id', teacherId)
          .eq('role', 'student');
        
        studentIds = teacherStudents?.map(s => s.id) || [];
      }

      // Get top performers - calculate final grade across ALL 4 modules
      // FIXED: Students must be ranked by average of all 4 modules, not just completed ones
      const { data: allProgressData, error: performersError } = await supabase
        .from('progress')
        .select(`
          user_id,
          module,
          best_score,
          profiles!inner(display_name)
        `)
        .in('user_id', studentIds.length > 0 ? studentIds : ['00000000-0000-0000-0000-000000000000']);
      
      if (performersError) {
        console.error('Error fetching top performers:', performersError);
      }

      // Calculate final grade for each user across ALL 4 modules
      const modules = ['decantation', 'organ_system', 'simple_machines', 'solar_system'];
      const userFinalGrades = new Map<string, { 
        name: string; 
        moduleScores: Map<string, number>; 
      }>();
      
      allProgressData?.forEach((record: any) => {
        const userId = record.user_id;
        const userName = record.profiles?.display_name || 'Anonymous';
        const module = record.module;
        const score = record.best_score || 0;
        
        if (!userFinalGrades.has(userId)) {
          userFinalGrades.set(userId, { 
            name: userName, 
            moduleScores: new Map() 
          });
        }
        
        const user = userFinalGrades.get(userId)!;
        // Only keep the best score for each module
        const currentScore = user.moduleScores.get(module) || 0;
        user.moduleScores.set(module, Math.max(currentScore, score));
      });

      // Calculate final grade = average of all 4 modules (0 if not attempted)
      const topPerformers = Array.from(userFinalGrades.entries())
        .map(([_userId, user]) => {
          const moduleScores = modules.map(module => user.moduleScores.get(module) || 0);
          const finalGrade = moduleScores.reduce((sum, score) => sum + score, 0) / 4;
          const modulesCompleted = Array.from(user.moduleScores.keys()).length;
          
          return {
            name: user.name,
            score: Math.round(finalGrade),
            completed: modulesCompleted
          };
        })
        .sort((a, b) => {
          // Sort by final grade (average of all 4 modules)
          if (b.score !== a.score) return b.score - a.score;
          // If same grade, prefer more modules completed
          return b.completed - a.completed;
        })
        .slice(0, 10); // Show top 10 instead of top 5

      // Get module progress - track quiz completions from progress table
      const moduleList = [
        { id: 'decantation', name: 'Decantation' },
        { id: 'organ_system', name: 'Organ System' },
        { id: 'simple_machines', name: 'Simple Machines' },
        { id: 'solar_system', name: 'Solar System' },
      ];

      const moduleProgress = await Promise.all(
        moduleList.map(async (module) => {
          // Get total unique users who attempted this module
          const { data: totalAttempts, error: totalError } = await supabase
            .from('progress')
            .select('user_id')
            .eq('module', module.id);
          
          if (totalError) {
            console.error(`Error fetching attempts for ${module.id}:`, totalError);
          }

          // Get users who completed this module
          const { data: completedAttempts, error: completedError } = await supabase
            .from('progress')
            .select('user_id')
            .eq('module', module.id)
            .eq('completed', true);
          
          if (completedError) {
            console.error(`Error fetching completions for ${module.id}:`, completedError);
          }

          const total = totalAttempts?.length || 0;
          const completed = completedAttempts?.length || 0;

          return {
            module: module.name,
            completed,
            total,
          };
        })
      );

      setData({
        userGrowth: newUsers || 0,
        lessonCompletionRate,
        quizAverageScore,
        activeUsers,
        topPerformers,
        moduleProgress,
      });
    } catch (error) {
      console.error('Error loading analytics:', error);
      const errorMessage = error instanceof Error ? error.message : 'Unknown error occurred';
      setError(`Failed to load analytics: ${errorMessage}`);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 400 }}>
        <CircularProgress />
      </Box>
    );
  }
  
  if (error) {
    return (
      <Box sx={{ p: 3 }}>
        <Typography variant="h4" fontWeight="bold" gutterBottom>
          📊 Analytics Dashboard
        </Typography>
        <Paper sx={{ p: 3, bgcolor: '#ffebee' }}>
          <Typography color="error" variant="h6" gutterBottom>
            Error Loading Analytics
          </Typography>
          <Typography color="error">
            {error}
          </Typography>
          <Button 
            variant="contained" 
            onClick={loadAnalytics} 
            sx={{ mt: 2 }}
          >
            Retry
          </Button>
        </Paper>
      </Box>
    );
  }

  const statCards = [
    {
      title: 'Quiz Average Score',
      value: `${data.quizAverageScore}%`,
      icon: <QuizIcon />,
      color: '#9C27B0',
    },
  ];

  return (
    <Box>
      <Typography variant="h4" fontWeight="bold" gutterBottom>
        📊 Analytics
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
        Monitor user engagement and performance metrics
      </Typography>

      {/* Stats Cards */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        {statCards.map((stat, index) => (
          <Grid item xs={12} sm={6} md={3} key={index}>
            <Card sx={{ height: '100%' }}>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
                  <Box
                    sx={{
                      p: 1.5,
                      borderRadius: 2,
                      bgcolor: `${stat.color}20`,
                      color: stat.color,
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                    }}
                  >
                    {stat.icon}
                  </Box>
                  <Typography variant="h4" fontWeight="bold">
                    {stat.value}
                  </Typography>
                </Box>
                <Typography variant="body2" color="text.secondary">
                  {stat.title}
                </Typography>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      <Grid container spacing={3}>
        {/* Module Performance Overview */}
        <Grid item xs={12}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" fontWeight="bold" gutterBottom>
              📚 Module Performance Overview
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
              Quiz completion and performance statistics by module
            </Typography>
            <TableContainer>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell><strong>Module</strong></TableCell>
                    <TableCell align="center"><strong>Completions</strong></TableCell>
                    <TableCell align="center"><strong>Total Students</strong></TableCell>
                    <TableCell align="center"><strong>Completion Rate</strong></TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {data.moduleProgress.length > 0 ? (
                    data.moduleProgress.map((module, index) => {
                      const completionRate = module.total > 0 
                        ? Math.round((module.completed / module.total) * 100) 
                        : 0;
                      
                      return (
                        <TableRow key={index} hover>
                          <TableCell>{module.module}</TableCell>
                          <TableCell align="center">
                            <Chip 
                              label={module.completed}
                              size="small"
                              color="primary"
                              variant="outlined"
                            />
                          </TableCell>
                          <TableCell align="center">{module.total}</TableCell>
                          <TableCell align="center">
                            <Chip 
                              label={`${completionRate}%`}
                              size="small"
                              color={
                                completionRate >= 75 ? 'success' : 
                                completionRate >= 50 ? 'primary' : 
                                'warning'
                              }
                            />
                          </TableCell>
                        </TableRow>
                      );
                    })
                  ) : (
                    <TableRow>
                      <TableCell colSpan={4} align="center">
                        <Typography variant="body2" color="text.secondary">
                          No module data available yet
                        </Typography>
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          </Paper>
        </Grid>

        {/* Ranked User Final Grades */}
        <Grid item xs={12}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" fontWeight="bold" gutterBottom>
              🏆 Ranked User Final Grades
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
              Students ranked by their average quiz performance across all modules
            </Typography>
            <TableContainer sx={{ mt: 2 }}>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell><strong>Rank</strong></TableCell>
                    <TableCell><strong>Student</strong></TableCell>
                    <TableCell align="center"><strong>Final Grade</strong></TableCell>
                    <TableCell align="center"><strong>Modules Completed</strong></TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {data.topPerformers.length > 0 ? (
                    data.topPerformers.map((performer, index) => (
                      <TableRow key={index} hover>
                        <TableCell>
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, fontWeight: 'bold' }}>
                            {index === 0 && '🥇'}
                            {index === 1 && '🥈'}
                            {index === 2 && '🥉'}
                            {index > 2 && `${index + 1}.`}
                          </Box>
                        </TableCell>
                        <TableCell>
                          {performer.name}
                        </TableCell>
                        <TableCell align="center">
                          <Chip 
                            label={`${performer.score}%`} 
                            color={performer.score >= 90 ? 'success' : performer.score >= 75 ? 'primary' : 'warning'}
                            size="small" 
                          />
                        </TableCell>
                        <TableCell align="center">
                          <Chip
                            icon={<CheckCircleIcon />}
                            label={`${performer.completed} / 4`}
                            size="small"
                            variant="outlined"
                          />
                        </TableCell>
                      </TableRow>
                    ))
                  ) : (
                    <TableRow>
                      <TableCell colSpan={4} align="center">
                        <Typography variant="body2" color="text.secondary">
                          No quiz data available yet
                        </Typography>
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
}
