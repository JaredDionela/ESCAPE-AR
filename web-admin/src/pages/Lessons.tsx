import { useState, useEffect } from 'react'
import { Box, Typography, Button, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, IconButton, Chip, CircularProgress, Dialog, DialogTitle, DialogContent } from '@mui/material'
import AddIcon from '@mui/icons-material/Add'
import EditIcon from '@mui/icons-material/Edit'
import DeleteIcon from '@mui/icons-material/Delete'
import AttachFileIcon from '@mui/icons-material/AttachFile'
import { getAllLessons, deleteLesson } from '../lib/api/lessons'
import type { Lesson } from '../types/database.types'
import { MODULES } from '../types/database.types'
import { LessonFormDialog } from '../components/lessons/LessonFormDialog'
import { FileUploader } from '../components/lessons/FileUploader'
import { YouTubePreview } from '../components/lessons/YouTubePreview'

export function Lessons() {
  const [lessons, setLessons] = useState<Lesson[]>([])
  const [loading, setLoading] = useState(true)
  const [formOpen, setFormOpen] = useState(false)
  const [editingLesson, setEditingLesson] = useState<Lesson | null>(null)
  const [filesDialogOpen, setFilesDialogOpen] = useState(false)
  const [selectedLessonId, setSelectedLessonId] = useState<string>('')
  const [previewVideoId, setPreviewVideoId] = useState<string>('')

  useEffect(() => {
    loadLessons()
  }, [])

  const loadLessons = async () => {
    try {
      const data = await getAllLessons()
      setLessons(data)
    } catch (error) {
      console.error('Error loading lessons:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleDelete = async (id: string) => {
    if (!confirm('Are you sure you want to delete this lesson?')) return
    
    try {
      await deleteLesson(id)
      setLessons(lessons.filter(l => l.id !== id))
    } catch (error) {
      console.error('Error deleting lesson:', error)
      alert('Failed to delete lesson')
    }
  }

  const handleCreateSuccess = (newLesson: Lesson) => {
    setLessons([...lessons, newLesson])
    setFormOpen(false)
  }

  const handleEdit = (lesson: Lesson) => {
    setEditingLesson(lesson)
    setFormOpen(true)
  }

  const handleManageFiles = (lesson: Lesson) => {
    setSelectedLessonId(lesson.id)
    setPreviewVideoId(lesson.youtube_video_id)
    setFilesDialogOpen(true)
  }

  const getModuleInfo = (moduleId: string) => {
    return MODULES.find(m => m.id === moduleId) || { name: moduleId, icon: '📚', color: '#666' }
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
          📚 Lessons Management
        </Typography>
        <Button 
          variant="contained" 
          startIcon={<AddIcon />} 
          color="primary"
          onClick={() => {
            setEditingLesson(null)
            setFormOpen(true)
          }}
        >
          New Lesson
        </Button>
      </Box>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow sx={{ backgroundColor: '#f5f5f5' }}>
              <TableCell><strong>Module</strong></TableCell>
              <TableCell><strong>Title</strong></TableCell>
              <TableCell><strong>YouTube Video ID</strong></TableCell>
              <TableCell><strong>Order</strong></TableCell>
              <TableCell><strong>Actions</strong></TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {lessons.map((lesson) => {
              const moduleInfo = getModuleInfo(lesson.module_id)
              return (
                <TableRow key={lesson.id} hover>
                  <TableCell>
                    <Chip 
                      label={`${moduleInfo.icon} ${moduleInfo.name}`}
                      sx={{ backgroundColor: moduleInfo.color, color: 'white' }}
                      size="small"
                    />
                  </TableCell>
                  <TableCell>{lesson.title}</TableCell>
                  <TableCell>
                    <code style={{ fontSize: '0.85em', backgroundColor: '#f0f0f0', padding: '2px 6px', borderRadius: 4 }}>
                      {lesson.youtube_video_id}
                    </code>
                  </TableCell>
                  <TableCell>{lesson.order_index}</TableCell>
                  <TableCell>
                    <IconButton 
                      color="primary" 
                      size="small"
                      onClick={() => handleEdit(lesson)}
                      title="Edit lesson"
                    >
                      <EditIcon />
                    </IconButton>
                    <IconButton 
                      color="secondary" 
                      size="small"
                      onClick={() => handleManageFiles(lesson)}
                      title="Manage files"
                    >
                      <AttachFileIcon />
                    </IconButton>
                    <IconButton 
                      color="error" 
                      size="small" 
                      onClick={() => handleDelete(lesson.id)}
                      title="Delete lesson"
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

      {lessons.length === 0 && (
        <Box sx={{ textAlign: 'center', py: 8 }}>
          <Typography variant="h6" color="text.secondary">
            No lessons found. Create your first lesson!
          </Typography>
        </Box>
      )}

      {/* Create/Edit Lesson Dialog */}
      <LessonFormDialog
        open={formOpen}
        onClose={() => {
          setFormOpen(false)
          setEditingLesson(null)
        }}
        onSuccess={handleCreateSuccess}
        editLesson={editingLesson}
      />

      {/* Files Management Dialog */}
      <Dialog 
        open={filesDialogOpen} 
        onClose={() => setFilesDialogOpen(false)}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>Manage Lesson Content</DialogTitle>
        <DialogContent>
          {/* File Uploader Section */}
          <Box sx={{ mb: 4 }}>
            <Typography variant="h6" gutterBottom sx={{ mb: 2 }}>
              📁 Files & Downloads
            </Typography>
            <FileUploader lessonId={selectedLessonId} />
          </Box>

          {/* Video Preview Section */}
          <Box>
            <Typography variant="h6" gutterBottom sx={{ mb: 2 }}>
              🎥 Video Preview
            </Typography>
            <YouTubePreview videoId={previewVideoId} />
          </Box>
        </DialogContent>
      </Dialog>
    </Box>
  )
}
