import { useState } from 'react'
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Box,
  Alert,
  CircularProgress,
  Typography,
} from '@mui/material'
import { Info as InfoIcon } from '@mui/icons-material'
import { createLesson } from '../../lib/api/lessons'
import { MODULES } from '../../types/database.types'
import type { Lesson } from '../../types/database.types'

interface LessonFormDialogProps {
  open: boolean
  onClose: () => void
  onSuccess: (lesson: Lesson) => void
  editLesson?: Lesson | null
}

// Helper function to extract YouTube Video ID from URL
function extractYouTubeId(input: string): string {
  // If it's already just an ID (11 characters, no special chars), return it
  if (/^[a-zA-Z0-9_-]{11}$/.test(input.trim())) {
    return input.trim()
  }

  // Try to extract from various YouTube URL formats
  const patterns = [
    /(?:youtube\.com\/watch\?v=|youtu\.be\/)([a-zA-Z0-9_-]{11})/,
    /youtube\.com\/embed\/([a-zA-Z0-9_-]{11})/,
    /youtube\.com\/v\/([a-zA-Z0-9_-]{11})/,
  ]

  for (const pattern of patterns) {
    const match = input.match(pattern)
    if (match && match[1]) {
      return match[1]
    }
  }

  // If no pattern matched, return the input as-is
  return input.trim()
}

export function LessonFormDialog({ open, onClose, onSuccess, editLesson }: LessonFormDialogProps) {
  const [formData, setFormData] = useState({
    module_id: editLesson?.module_id || '',
    title: editLesson?.title || '',
    description: editLesson?.description || '',
    youtube_video_id: editLesson?.youtube_video_id || '',
    thumbnail_url: editLesson?.thumbnail_url || '',
    order_index: editLesson?.order_index || 1
  })
  
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const handleChange = (field: string, value: string | number) => {
    setFormData(prev => ({ ...prev, [field]: value }))
  }

  const handleYouTubeIdChange = (value: string) => {
    const extractedId = extractYouTubeId(value)
    setFormData(prev => ({ ...prev, youtube_video_id: extractedId }))
  }

  const handleSubmit = async () => {
    // Validation
    if (!formData.module_id || !formData.title || !formData.youtube_video_id) {
      setError('Please fill in all required fields')
      return
    }

    setLoading(true)
    setError(null)

    try {
      // Don't send duration_minutes - let database use default value
      const lesson = await createLesson(formData as any)
      onSuccess(lesson)
      onClose()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create lesson')
    } finally {
      setLoading(false)
    }
  }

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>
        {editLesson ? 'Edit Lesson' : 'Create New Lesson'}
      </DialogTitle>
      
      <DialogContent>
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 2 }}>
          {error && <Alert severity="error">{error}</Alert>}
          
          <FormControl fullWidth required>
            <InputLabel>Module</InputLabel>
            <Select
              value={formData.module_id}
              onChange={(e) => handleChange('module_id', e.target.value)}
              label="Module"
            >
              {MODULES.map(module => (
                <MenuItem key={module.id} value={module.id}>
                  {module.icon} {module.name}
                </MenuItem>
              ))}
            </Select>
          </FormControl>

          <TextField
            required
            fullWidth
            label="Lesson Title"
            value={formData.title}
            onChange={(e) => handleChange('title', e.target.value)}
            placeholder="e.g., Introduction to Decantation"
          />

          <TextField
            fullWidth
            multiline
            rows={3}
            label="Description"
            value={formData.description}
            onChange={(e) => handleChange('description', e.target.value)}
            placeholder="Describe what students will learn..."
          />

          <Box sx={{ mb: 2 }}>
            <TextField
              required
              fullWidth
              label="YouTube Video ID or URL"
              value={formData.youtube_video_id}
              onChange={(e) => handleYouTubeIdChange(e.target.value)}
              placeholder="Paste full URL or just the video ID"
              helperText={
                <Box component="span">
                  Paste the full YouTube URL or just the video ID
                </Box>
              }
            />
            <Alert severity="info" icon={<InfoIcon />} sx={{ mt: 1 }}>
              <Typography variant="caption" component="div">
                <strong>How to find the YouTube ID:</strong>
              </Typography>
              <Typography variant="caption" component="div" sx={{ mt: 0.5 }}>
                1. Go to the YouTube video you want to use
              </Typography>
              <Typography variant="caption" component="div">
                2. Copy the URL from your browser:
              </Typography>
              <Typography variant="caption" component="div" sx={{ fontFamily: 'monospace', bgcolor: '#f5f5f5', p: 0.5, borderRadius: 1, mt: 0.5 }}>
                https://www.youtube.com/watch?v=<strong style={{ color: '#d32f2f' }}>dQw4w9WgXcQ</strong>
              </Typography>
              <Typography variant="caption" component="div" sx={{ mt: 0.5 }}>
                3. Paste the entire URL here - we'll extract the ID automatically!
              </Typography>
              <Typography variant="caption" component="div" sx={{ mt: 0.5 }}>
                Or just paste the ID part: <strong>dQw4w9WgXcQ</strong>
              </Typography>
            </Alert>
          </Box>

          <TextField
            fullWidth
            label="Thumbnail URL (optional)"
            value={formData.thumbnail_url}
            onChange={(e) => handleChange('thumbnail_url', e.target.value)}
            placeholder="https://..."
          />

          <TextField
            required
            type="number"
            label="Order"
            value={formData.order_index}
            onChange={(e) => handleChange('order_index', parseInt(e.target.value) || 1)}
            fullWidth
            helperText="Display order (1, 2, 3...)"
          />
        </Box>
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose} disabled={loading}>
          Cancel
        </Button>
        <Button 
          onClick={handleSubmit} 
          variant="contained" 
          disabled={loading}
          startIcon={loading ? <CircularProgress size={20} /> : null}
        >
          {loading ? 'Creating...' : editLesson ? 'Update' : 'Create Lesson'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}
