import { useState, useCallback } from 'react'
import { useDropzone } from 'react-dropzone'
import {
  Box,
  Typography,
  List,
  ListItem,
  ListItemText,
  ListItemSecondaryAction,
  IconButton,
  Paper,
  LinearProgress,
  Alert,
  Chip
} from '@mui/material'
import CloudUploadIcon from '@mui/icons-material/CloudUpload'
import DeleteIcon from '@mui/icons-material/Delete'
import InsertDriveFileIcon from '@mui/icons-material/InsertDriveFile'
import { uploadLessonFile, deleteLessonFile, getLessonFiles } from '../../lib/api/lessons'
import type { LessonFile } from '../../types/database.types'
import { useEffect } from 'react'

interface FileUploaderProps {
  lessonId: string
}

export function FileUploader({ lessonId }: FileUploaderProps) {
  const [files, setFiles] = useState<LessonFile[]>([])
  const [uploading, setUploading] = useState(false)
  const [uploadProgress, setUploadProgress] = useState(0)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    loadFiles()
  }, [lessonId])

  const loadFiles = async () => {
    try {
      const loadedFiles = await getLessonFiles(lessonId)
      setFiles(loadedFiles)
    } catch (err) {
      console.error('Error loading files:', err)
    }
  }

  const onDrop = useCallback(async (acceptedFiles: File[]) => {
    setUploading(true)
    setError(null)
    setUploadProgress(0)

    try {
      for (let i = 0; i < acceptedFiles.length; i++) {
        const file = acceptedFiles[i]
        
        // Check file size (max 10MB)
        if (file.size > 10 * 1024 * 1024) {
          setError(`File ${file.name} is too large. Maximum size is 10MB.`)
          continue
        }

        const uploadedFile = await uploadLessonFile(file, lessonId)
        setFiles(prev => [...prev, uploadedFile])
        setUploadProgress(((i + 1) / acceptedFiles.length) * 100)
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Upload failed')
    } finally {
      setUploading(false)
      setUploadProgress(0)
    }
  }, [lessonId])

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: {
      'application/pdf': ['.pdf'],
      'application/vnd.ms-powerpoint': ['.ppt'],
      'application/vnd.openxmlformats-officedocument.presentationml.presentation': ['.pptx'],
      'application/msword': ['.doc'],
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document': ['.docx'],
      'image/*': ['.png', '.jpg', '.jpeg', '.gif']
    }
  })

  const handleDelete = async (file: LessonFile) => {
    if (!confirm(`Delete ${file.file_name}?`)) return

    try {
      await deleteLessonFile(file.id, file.file_url)
      setFiles(prev => prev.filter(f => f.id !== file.id))
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Delete failed')
    }
  }

  const formatFileSize = (bytes: number) => {
    if (bytes < 1024) return bytes + ' B'
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
  }

  return (
    <Box>
      <Typography variant="h6" gutterBottom>
        📎 Lesson Files
      </Typography>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      {/* Dropzone */}
      <Paper
        {...getRootProps()}
        sx={{
          p: 3,
          mb: 2,
          border: '2px dashed',
          borderColor: isDragActive ? 'primary.main' : 'grey.300',
          backgroundColor: isDragActive ? 'action.hover' : 'background.paper',
          cursor: 'pointer',
          textAlign: 'center',
          transition: 'all 0.2s'
        }}
      >
        <input {...getInputProps()} />
        <CloudUploadIcon sx={{ fontSize: 48, color: 'text.secondary', mb: 1 }} />
        <Typography variant="body1" gutterBottom>
          {isDragActive ? 'Drop files here...' : 'Drag & drop files or click to browse'}
        </Typography>
        <Typography variant="caption" color="text.secondary">
          Supported: PDF, PPTX, DOCX, Images (max 10MB each)
        </Typography>
      </Paper>

      {/* Upload Progress */}
      {uploading && (
        <Box sx={{ mb: 2 }}>
          <LinearProgress variant="determinate" value={uploadProgress} />
          <Typography variant="caption" sx={{ mt: 0.5, display: 'block' }}>
            Uploading... {Math.round(uploadProgress)}%
          </Typography>
        </Box>
      )}

      {/* Files List */}
      {files.length > 0 ? (
        <List>
          {files.map(file => (
            <ListItem key={file.id} divider>
              <InsertDriveFileIcon sx={{ mr: 2, color: 'primary.main' }} />
              <ListItemText
                primary={file.file_name}
                secondary={
                  <Box sx={{ display: 'flex', gap: 1, mt: 0.5 }}>
                    <Chip label={file.file_type} size="small" />
                    <Chip label={formatFileSize(file.file_size)} size="small" />
                  </Box>
                }
              />
              <ListItemSecondaryAction>
                <IconButton edge="end" onClick={() => handleDelete(file)} color="error">
                  <DeleteIcon />
                </IconButton>
              </ListItemSecondaryAction>
            </ListItem>
          ))}
        </List>
      ) : (
        <Typography variant="body2" color="text.secondary" sx={{ textAlign: 'center', py: 2 }}>
          No files uploaded yet
        </Typography>
      )}
    </Box>
  )
}
