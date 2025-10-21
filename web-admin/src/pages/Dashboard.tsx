import { useState, useEffect } from 'react'
import { 
  Box, 
  Grid, 
  Typography, 
  Card, 
  CardContent, 
  CircularProgress,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Chip
} from '@mui/material'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import EmojiEventsIcon from '@mui/icons-material/EmojiEvents'
import WarningIcon from '@mui/icons-material/Warning'
import TrendingUpIcon from '@mui/icons-material/TrendingUp'
import { getDashboardMetrics } from '../lib/api/analytics'
import type { DashboardMetrics } from '../lib/api/analytics'

export function Dashboard() {
  const [metrics, setMetrics] = useState<DashboardMetrics | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadMetrics()
  }, [])

  const loadMetrics = async () => {
    try {
      console.log('Dashboard: Starting to load metrics...')
      const data = await getDashboardMetrics()
      console.log('Dashboard: Metrics loaded successfully:', data)
      setMetrics(data)
    } catch (error) {
      console.error('Dashboard: Error loading metrics:', error)
    } finally {
      setLoading(false)
    }
  }

  const formatTimeAgo = (timestamp: string): string => {
    const date = new Date(timestamp)
    const seconds = Math.floor((new Date().getTime() - date.getTime()) / 1000)
    
    const intervals = {
      year: 31536000,
      month: 2592000,
      week: 604800,
      day: 86400,
      hour: 3600,
      minute: 60
    }
    
    for (const [unit, secondsInUnit] of Object.entries(intervals)) {
      const interval = Math.floor(seconds / secondsInUnit)
      if (interval >= 1) {
        return `${interval} ${unit}${interval === 1 ? '' : 's'} ago`
      }
    }
    
    return 'Just now'
  }

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 400 }}>
        <CircularProgress />
      </Box>
    )
  }

  const statCards = [
    { 
      title: 'Quiz Completion', 
      value: `${metrics?.quizCompletionRate || 0}%`,
      subtitle: `${metrics?.totalCompletions || 0} / ${metrics?.totalPossible || 0}`,
      icon: <CheckCircleIcon sx={{ fontSize: 40 }} />, 
      color: '#4CAF50',
      status: (metrics?.quizCompletionRate || 0) >= 70 ? 'Good progress!' : 'Needs improvement'
    },
    { 
      title: 'Average Score', 
      value: `${metrics?.averageScore || 0}%`,
      subtitle: (metrics?.averageScore || 0) >= 75 ? 'Above target' : 'Below target',
      icon: <TrendingUpIcon sx={{ fontSize: 40 }} />, 
      color: '#2196F3',
      status: 'Across all modules'
    },
    { 
      title: 'Students Needing Help', 
      value: metrics?.studentsNeedingHelp || 0,
      subtitle: 'Score < 50%',
      icon: <WarningIcon sx={{ fontSize: 40 }} />, 
      color: '#FF9800',
      status: (metrics?.studentsNeedingHelp || 0) > 0 ? 'Requires attention' : 'All doing well!'
    },
    { 
      title: 'Top Performer', 
      value: metrics?.topPerformer?.name || 'N/A',
      subtitle: metrics?.topPerformer 
        ? `${metrics.topPerformer.score}% avg across ${metrics.topPerformer.modulesCompleted} modules`
        : 'No data yet',
      icon: <EmojiEventsIcon sx={{ fontSize: 40 }} />, 
      color: '#FFC107',
      status: '⭐ Excellent work!'
    }
  ]

  return (
    <Box>
      <Typography variant="h4" gutterBottom fontWeight="bold">
        📊 Dashboard
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
        Quick overview of student progress and engagement
      </Typography>

      {/* Key Metrics Cards */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        {statCards.map((stat) => (
          <Grid item xs={12} sm={6} md={3} key={stat.title}>
            <Card sx={{ height: '100%', backgroundColor: stat.color, color: 'white' }}>
              <CardContent>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 2 }}>
                  <Box sx={{ flex: 1 }}>
                    <Typography variant="body2" sx={{ opacity: 0.9, mb: 1 }}>
                      {stat.title}
                    </Typography>
                    <Typography variant="h4" fontWeight="bold" sx={{ mb: 0.5 }}>
                      {stat.value}
                    </Typography>
                    <Typography variant="caption" sx={{ opacity: 0.8, display: 'block' }}>
                      {stat.subtitle}
                    </Typography>
                  </Box>
                  <Box sx={{ opacity: 0.7 }}>
                    {stat.icon}
                  </Box>
                </Box>
                <Typography variant="caption" sx={{ opacity: 0.9, fontStyle: 'italic' }}>
                  {stat.status}
                </Typography>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      {/* Recent Quiz Completions */}
      <Paper sx={{ p: 3 }}>
        <Typography variant="h6" gutterBottom fontWeight="bold">
          📋 Recent Quiz Completions
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
          Last 5 quiz submissions
        </Typography>

        {metrics?.recentCompletions && metrics.recentCompletions.length > 0 ? (
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell><strong>Student</strong></TableCell>
                  <TableCell><strong>Module</strong></TableCell>
                  <TableCell align="center"><strong>Score</strong></TableCell>
                  <TableCell align="right"><strong>Time</strong></TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {metrics.recentCompletions.map((completion, index) => (
                  <TableRow key={index} hover>
                    <TableCell>{completion.studentName}</TableCell>
                    <TableCell>{completion.module}</TableCell>
                    <TableCell align="center">
                      <Chip 
                        label={`${completion.score}%`}
                        size="small"
                        color={
                          completion.score >= 90 ? 'success' : 
                          completion.score >= 75 ? 'primary' : 
                          completion.score >= 50 ? 'warning' : 
                          'error'
                        }
                      />
                    </TableCell>
                    <TableCell align="right" sx={{ color: 'text.secondary' }}>
                      {formatTimeAgo(completion.createdAt)}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        ) : (
          <Typography color="text.secondary" sx={{ py: 3, textAlign: 'center' }}>
            No recent quiz completions yet
          </Typography>
        )}
      </Paper>
    </Box>
  )
}
