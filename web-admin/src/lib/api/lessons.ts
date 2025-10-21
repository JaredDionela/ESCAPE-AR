import { supabase } from '../supabase'
import type { Lesson, LessonFile } from '../../types/database.types'

// Fetch all lessons
export async function getAllLessons() {
  const { data, error } = await supabase
    .from('lessons')
    .select('*')
    .order('module_id')
    .order('order_index')
  
  if (error) throw error
  return data as Lesson[]
}

// Fetch lessons by module
export async function getLessonsByModule(moduleId: string) {
  const { data, error } = await supabase
    .from('lessons')
    .select('*')
    .eq('module_id', moduleId)
    .order('order_index')
  
  if (error) throw error
  return data as Lesson[]
}

// Get single lesson
export async function getLesson(id: string) {
  const { data, error } = await supabase
    .from('lessons')
    .select('*')
    .eq('id', id)
    .single()
  
  if (error) throw error
  return data as Lesson
}

// Create new lesson
export async function createLesson(lesson: Omit<Lesson, 'id' | 'created_at' | 'updated_at'>) {
  try {
    console.log('Creating lesson with data:', lesson)
    
    // Ensure all required fields are present
    const lessonData = {
      module_id: lesson.module_id,
      title: lesson.title,
      description: lesson.description || '',
      youtube_video_id: lesson.youtube_video_id,
      thumbnail_url: lesson.thumbnail_url || null,
      order_index: lesson.order_index || 1,
      duration_minutes: lesson.duration_minutes || 0
    }
    
    const { data, error } = await supabase
      .from('lessons')
      .insert([lessonData])
      .select()
      .single()
    
    if (error) {
      console.error('Supabase error creating lesson:', error)
      throw new Error(`Failed to create lesson: ${error.message}`)
    }
    
    console.log('Lesson created successfully:', data)
    return data as Lesson
  } catch (err) {
    console.error('Error in createLesson:', err)
    throw err
  }
}

// Update lesson
export async function updateLesson(id: string, updates: Partial<Lesson>) {
  const { data, error } = await supabase
    .from('lessons')
    .update(updates)
    .eq('id', id)
    .select()
    .single()
  
  if (error) throw error
  return data as Lesson
}

// Delete lesson
export async function deleteLesson(id: string) {
  const { error } = await supabase
    .from('lessons')
    .delete()
    .eq('id', id)
  
  if (error) throw error
}

// Get lesson files
export async function getLessonFiles(lessonId: string) {
  const { data, error } = await supabase
    .from('lesson_files')
    .select('*')
    .eq('lesson_id', lessonId)
  
  if (error) throw error
  return data as LessonFile[]
}

// Upload file to Supabase Storage
export async function uploadLessonFile(file: File, lessonId: string) {
  try {
    console.log('Uploading file:', file.name, 'for lesson:', lessonId)
    
    const fileExt = file.name.split('.').pop()
    const fileName = `${lessonId}/${Date.now()}.${fileExt}`
    
    // First, ensure the bucket exists and is accessible
    const { error: uploadError } = await supabase.storage
      .from('lesson-files')
      .upload(fileName, file, {
        cacheControl: '3600',
        upsert: false
      })
    
    if (uploadError) {
      console.error('Storage upload error:', uploadError)
      throw new Error(`Failed to upload file: ${uploadError.message}`)
    }
    
    console.log('File uploaded to storage successfully')
    
    // Get public URL
    const { data: { publicUrl } } = supabase.storage
      .from('lesson-files')
      .getPublicUrl(fileName)
    
    console.log('Public URL:', publicUrl)
    
    // Create database entry
    const { data, error } = await supabase
      .from('lesson_files')
      .insert([{
        lesson_id: lessonId,
        file_name: file.name,
        file_url: publicUrl,
        file_type: file.type,
        file_size: file.size
      }])
      .select()
      .single()
    
    if (error) {
      console.error('Database error creating file record:', error)
      // Try to clean up the uploaded file
      await supabase.storage.from('lesson-files').remove([fileName])
      throw new Error(`Failed to save file record: ${error.message}`)
    }
    
    console.log('File record created:', data)
    return data as LessonFile
  } catch (err) {
    console.error('Error in uploadLessonFile:', err)
    throw err
  }
}

// Delete lesson file
export async function deleteLessonFile(id: string, fileUrl: string) {
  // Extract file path from URL
  const urlParts = fileUrl.split('/')
  const fileName = urlParts.slice(-2).join('/')
  
  // Delete from storage
  const { error: storageError } = await supabase.storage
    .from('lesson-files')
    .remove([fileName])
  
  if (storageError) console.error('Storage deletion error:', storageError)
  
  // Delete from database
  const { error } = await supabase
    .from('lesson_files')
    .delete()
    .eq('id', id)
  
  if (error) throw error
}
