import { useState, useEffect } from 'react'
import { 
  Box, 
  Typography, 
  Paper, 
  Table, 
  TableBody, 
  TableCell, 
  TableContainer, 
  TableHead, 
  TableRow, 
  IconButton, 
  Chip,
  CircularProgress,
  Avatar,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Grid,
  Card,
  CardContent
} from '@mui/material'
import VisibilityIcon from '@mui/icons-material/Visibility'
import EditIcon from '@mui/icons-material/Edit'
import DeleteIcon from '@mui/icons-material/Delete'
import QuizIcon from '@mui/icons-material/Quiz'
import PercentIcon from '@mui/icons-material/Percent'
import { getAllUsers, deleteUser, getUserStats } from '../lib/api/users'
import type { Profile } from '../types/database.types'

export function Users() {
  const [users, setUsers] = useState<Profile[]>([])
  const [loading, setLoading] = useState(true)
  const [detailsOpen, setDetailsOpen] = useState(false)
  const [selectedUser, setSelectedUser] = useState<Profile | null>(null)
  const [userStats, setUserStats] = useState<{
    completedLessons: number
    totalQuizzes: number
    quizAccuracy: number
    moduleBreakdown: Array<{
      moduleId: string
      moduleName: string
      completed: boolean
      bestScore: number
      totalAttempts: number
      latestScore: number | null
      totalQuestions: number | null
      correctAnswers: number | null
    }>
  } | null>(null)

  useEffect(() => {
    loadUsers()
  }, [])

  const loadUsers = async () => {
    try {
      const data = await getAllUsers()
      setUsers(data)
    } catch (error) {
      console.error('Error loading users:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleDelete = async (id: string, displayName: string) => {
    if (!confirm(`Are you sure you want to delete user "${displayName}"?`)) return
    
    try {
      await deleteUser(id)
      setUsers(users.filter(u => u.id !== id))
    } catch (error) {
      console.error('Error deleting user:', error)
      alert('Failed to delete user')
    }
  }

  const handleViewDetails = async (user: Profile) => {
    setSelectedUser(user)
    setDetailsOpen(true)
    
    try {
      const stats = await getUserStats(user.id)
      setUserStats(stats)
    } catch (error) {
      console.error('Error loading user stats:', error)
    }
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    })
  }

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 400 }}>
        <CircularProgress />
      </Box>
    )
  }

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" fontWeight="bold">
          👥 User Management
        </Typography>
        <Chip label={`${users.length} Total Users`} color="primary" />
      </Box>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow sx={{ backgroundColor: '#f5f5f5' }}>
              <TableCell><strong>User</strong></TableCell>
              <TableCell><strong>Teacher</strong></TableCell>
              <TableCell><strong>Section</strong></TableCell>
              <TableCell><strong>Joined</strong></TableCell>
              <TableCell><strong>Actions</strong></TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {users.map((user) => (
              <TableRow key={user.id} hover>
                <TableCell>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <Avatar 
                      src={user.avatar_url || undefined}
                      sx={{ width: 32, height: 32 }}
                    >
                      {user.display_name.charAt(0)}
                    </Avatar>
                    <Typography>{user.display_name}</Typography>
                  </Box>
                </TableCell>
                <TableCell>{user.teacher_name || '—'}</TableCell>
                <TableCell>{user.section || '—'}</TableCell>
                <TableCell>{formatDate(user.created_at)}</TableCell>
                <TableCell>
                  <IconButton 
                    color="primary" 
                    size="small"
                    onClick={() => handleViewDetails(user)}
                    title="View details"
                  >
                    <VisibilityIcon />
                  </IconButton>
                  <IconButton 
                    color="secondary" 
                    size="small"
                    title="Edit user"
                  >
                    <EditIcon />
                  </IconButton>
                  <IconButton 
                    color="error" 
                    size="small"
                    onClick={() => handleDelete(user.id, user.display_name)}
                    title="Delete user"
                  >
                    <DeleteIcon />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      {users.length === 0 && (
        <Box sx={{ textAlign: 'center', py: 8 }}>
          <Typography variant="h6" color="text.secondary">
            No users found
          </Typography>
        </Box>
      )}

      {/* User Details Dialog */}
      <Dialog 
        open={detailsOpen} 
        onClose={() => {
          setDetailsOpen(false)
          setSelectedUser(null)
          setUserStats(null)
        }}
        maxWidth="sm"
        fullWidth
      >
        {selectedUser && (
          <>
            <DialogTitle>
              User Details
            </DialogTitle>
            <DialogContent>
              <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', mb: 3 }}>
                <Avatar 
                  src={selectedUser.avatar_url || undefined}
                  sx={{ width: 80, height: 80, mb: 2 }}
                >
                  {selectedUser.display_name.charAt(0)}
                </Avatar>
                <Typography variant="h6">{selectedUser.display_name}</Typography>
                <Typography variant="body2" color="text.secondary">
                  ID: {selectedUser.id}
                </Typography>
              </Box>

              <Grid container spacing={2} sx={{ mb: 3 }}>
                <Grid item xs={12}>
                  <Paper sx={{ p: 2 }}>
                    <Typography variant="subtitle2" color="text.secondary">Teacher</Typography>
                    <Typography variant="body1">{selectedUser.teacher_name || 'Not set'}</Typography>
                  </Paper>
                </Grid>
                <Grid item xs={12}>
                  <Paper sx={{ p: 2 }}>
                    <Typography variant="subtitle2" color="text.secondary">Section</Typography>
                    <Typography variant="body1">{selectedUser.section || 'Not set'}</Typography>
                  </Paper>
                </Grid>
                <Grid item xs={6}>
                  <Paper sx={{ p: 2 }}>
                    <Typography variant="subtitle2" color="text.secondary">Joined</Typography>
                    <Typography variant="body2">{formatDate(selectedUser.created_at)}</Typography>
                  </Paper>
                </Grid>
                <Grid item xs={6}>
                  <Paper sx={{ p: 2 }}>
                    <Typography variant="subtitle2" color="text.secondary">Updated</Typography>
                    <Typography variant="body2">{formatDate(selectedUser.updated_at)}</Typography>
                  </Paper>
                </Grid>
              </Grid>

              {userStats ? (
                <>
                  <Typography variant="h6" gutterBottom>
                    Quiz Performance
                  </Typography>
                  <Grid container spacing={2} sx={{ mb: 3 }}>
                    <Grid item xs={6}>
                      <Card sx={{ backgroundColor: '#FF9800', color: 'white' }}>
                        <CardContent>
                          <QuizIcon sx={{ fontSize: 32, mb: 1 }} />
                          <Typography variant="h4">{userStats.totalQuizzes}</Typography>
                          <Typography variant="caption">Total Quizzes</Typography>
                        </CardContent>
                      </Card>
                    </Grid>
                    <Grid item xs={6}>
                      <Card sx={{ backgroundColor: '#2196F3', color: 'white' }}>
                        <CardContent>
                          <PercentIcon sx={{ fontSize: 32, mb: 1 }} />
                          <Typography variant="h4">{userStats.quizAccuracy}%</Typography>
                          <Typography variant="caption">Quiz Accuracy</Typography>
                        </CardContent>
                      </Card>
                    </Grid>
                  </Grid>

                  {/* Module Breakdown */}
                  <Typography variant="h6" gutterBottom sx={{ mt: 2 }}>
                    Module Progress
                  </Typography>
                  <TableContainer component={Paper} sx={{ mt: 2 }}>
                    <Table size="small">
                      <TableHead>
                        <TableRow sx={{ backgroundColor: '#f5f5f5' }}>
                          <TableCell><strong>Module</strong></TableCell>
                          <TableCell align="center"><strong>Latest Quiz</strong></TableCell>
                          <TableCell align="center"><strong>Attempts</strong></TableCell>
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {userStats.moduleBreakdown && userStats.moduleBreakdown.length > 0 ? (
                          userStats.moduleBreakdown.map((module) => (
                            <TableRow key={module.moduleId}>
                              <TableCell>{module.moduleName}</TableCell>
                              <TableCell align="center">
                                {module.latestScore !== null ? (
                                  <Box>
                                    <Typography variant="body2" fontWeight="bold">
                                      {Math.round(module.latestScore)}%
                                    </Typography>
                                    <Typography variant="caption" color="text.secondary">
                                      ({module.correctAnswers}/{module.totalQuestions})
                                    </Typography>
                                  </Box>
                                ) : (
                                  <Typography variant="body2" color="text.secondary">—</Typography>
                                )}
                              </TableCell>
                              <TableCell align="center">
                                <Chip 
                                  label={module.totalAttempts}
                                  size="small"
                                  variant="outlined"
                                />
                              </TableCell>
                            </TableRow>
                          ))
                        ) : (
                          <TableRow>
                            <TableCell colSpan={3} align="center">
                              <Typography variant="body2" color="text.secondary">
                                No quiz data available
                              </Typography>
                            </TableCell>
                          </TableRow>
                        )}
                      </TableBody>
                    </Table>
                  </TableContainer>
                </>
              ) : (
                <Box sx={{ textAlign: 'center', py: 2 }}>
                  <CircularProgress size={24} />
                  <Typography variant="caption" sx={{ display: 'block', mt: 1 }}>
                    Loading statistics...
                  </Typography>
                </Box>
              )}
            </DialogContent>
            <DialogActions>
              <Button onClick={() => setDetailsOpen(false)}>Close</Button>
            </DialogActions>
          </>
        )}
      </Dialog>
    </Box>
  )
}
