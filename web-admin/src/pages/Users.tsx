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
  CardContent,
  TextField,
  Tooltip,
  Alert
} from '@mui/material'
import VisibilityIcon from '@mui/icons-material/Visibility'
import EditIcon from '@mui/icons-material/Edit'
import DeleteIcon from '@mui/icons-material/Delete'
import QuizIcon from '@mui/icons-material/Quiz'
import PercentIcon from '@mui/icons-material/Percent'
import InfoIcon from '@mui/icons-material/Info'
import { getAllUsers, deleteUser, getUserStats, updateUser, getUsersFinalGrades } from '../lib/api/users'
import type { Profile } from '../types/database.types'

export function Users() {
  const [users, setUsers] = useState<Profile[]>([])
  const [loading, setLoading] = useState(true)
  const [detailsOpen, setDetailsOpen] = useState(false)
  const [editOpen, setEditOpen] = useState(false)
  const [selectedUser, setSelectedUser] = useState<Profile | null>(null)
  const [finalGrades, setFinalGrades] = useState<Map<string, number>>(new Map())
  const [editFormData, setEditFormData] = useState({
    display_name: '',
    section: ''
  })
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
      
      // Load final grades for all users
      if (data.length > 0) {
        const userIds = data.map(u => u.id)
        const grades = await getUsersFinalGrades(userIds)
        setFinalGrades(grades)
      }
    } catch (error) {
      console.error('Error loading users:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleDelete = async (id: string, displayName: string) => {
    const confirmMessage = `⚠️ WARNING: Delete "${displayName}"?\n\n` +
      `This will permanently delete:\n` +
      `• Student profile\n` +
      `• All quiz results\n` +
      `• All progress records\n` +
      `• Authentication account\n\n` +
      `This action CANNOT be undone!\n\n` +
      `Type the student's name to confirm deletion.`
    
    const userInput = prompt(confirmMessage)
    
    if (!userInput || userInput.trim() !== displayName) {
      if (userInput !== null) {
        alert('❌ Deletion cancelled: Name did not match.')
      }
      return
    }
    
    try {
      await deleteUser(id)
      setUsers(users.filter(u => u.id !== id))
      alert(`✅ Successfully deleted "${displayName}" and all associated data.`)
    } catch (error: any) {
      console.error('Error deleting user:', error)
      alert(`❌ Failed to delete user: ${error?.message || 'Unknown error'}`)
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

  const handleEditUser = (user: Profile) => {
    setSelectedUser(user)
    setEditFormData({
      display_name: user.display_name || '',
      section: user.section || ''
    })
    setEditOpen(true)
  }

  const handleUpdateUser = async () => {
    if (!selectedUser) return
    
    try {
      const updates = {
        display_name: editFormData.display_name,
        section: editFormData.section || null
      }
      
      console.log('Updating user:', selectedUser.id, updates)
      
      await updateUser(selectedUser.id, updates)
      
      // Reload users to reflect changes
      await loadUsers()
      setEditOpen(false)
      setSelectedUser(null)
      alert('✅ User updated successfully!')
    } catch (error: any) {
      console.error('Error updating user:', error)
      const errorMessage = error?.message || 'Failed to update user'
      alert(`❌ Update failed: ${errorMessage}`)
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
              <TableCell align="center"><strong>Final Grade</strong></TableCell>
              <TableCell><strong>Actions</strong></TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {users.map((user) => {
              const finalGrade = finalGrades.get(user.id) || 0
              return (
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
                  <TableCell align="center">
                    <Chip 
                      label={`${finalGrade}%`}
                      size="small"
                      color={
                        finalGrade >= 90 ? 'success' : 
                        finalGrade >= 75 ? 'primary' : 
                        finalGrade >= 50 ? 'warning' : 
                        'error'
                      }
                      sx={{ fontWeight: 'bold', minWidth: 60 }}
                    />
                  </TableCell>
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
                      onClick={() => handleEditUser(user)}
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
              )
            })}
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
                                    <Typography 
                                      variant="body2" 
                                      fontWeight="bold"
                                      color={module.latestScore >= 70 ? 'success.main' : 'error.main'}
                                    >
                                      {Math.round(module.latestScore)}%
                                    </Typography>
                                    <Typography 
                                      variant="caption" 
                                      sx={{ 
                                        color: module.latestScore >= 70 ? 'success.light' : 'error.light',
                                        fontWeight: 500
                                      }}
                                    >
                                      {module.latestScore >= 70 ? '✓ Passed' : '✗ Failed'}
                                    </Typography>
                                    <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }}>
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

      {/* Edit User Dialog */}
      <Dialog 
        open={editOpen} 
        onClose={() => {
          setEditOpen(false)
          setSelectedUser(null)
        }}
        maxWidth="sm"
        fullWidth
      >
        {selectedUser && (
          <>
            <DialogTitle>Edit User Profile</DialogTitle>
            <DialogContent>
              <Alert severity="info" sx={{ mb: 3, mt: 2 }}>
                <Typography variant="body2">
                  <strong>Note:</strong> Email and role cannot be changed for security reasons.
                </Typography>
              </Alert>

              {/* Read-only Email Field */}
              <TextField
                label="Email Address"
                value={selectedUser.email}
                fullWidth
                disabled
                sx={{ mb: 2 }}
                InputProps={{
                  endAdornment: (
                    <Tooltip title="Email cannot be changed after account creation">
                      <InfoIcon color="disabled" fontSize="small" />
                    </Tooltip>
                  )
                }}
              />

              {/* Read-only Role Field */}
              <Box sx={{ mb: 3 }}>
                <Typography variant="caption" color="text.secondary" display="block" gutterBottom>
                  Role (cannot be changed)
                </Typography>
                <Chip 
                  label={selectedUser.role?.toUpperCase() || 'STUDENT'} 
                  color="primary"
                  variant="outlined"
                />
              </Box>

              {/* Editable Fields */}
              <TextField
                label="Display Name"
                value={editFormData.display_name}
                onChange={(e) => setEditFormData({ ...editFormData, display_name: e.target.value })}
                fullWidth
                required
                sx={{ mb: 2 }}
                helperText="This name will be displayed throughout the app"
              />

              <TextField
                label="Section"
                value={editFormData.section}
                onChange={(e) => setEditFormData({ ...editFormData, section: e.target.value })}
                fullWidth
                placeholder="e.g., VI SSC, VI Jose Rizal"
                helperText="Student's class section"
              />
            </DialogContent>
            <DialogActions>
              <Button onClick={() => setEditOpen(false)}>Cancel</Button>
              <Button 
                onClick={handleUpdateUser} 
                variant="contained"
                disabled={!editFormData.display_name.trim()}
              >
                Save Changes
              </Button>
            </DialogActions>
          </>
        )}
      </Dialog>
    </Box>
  )
}
