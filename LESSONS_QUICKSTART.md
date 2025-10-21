# 🚀 Quick Start: Lessons Feature

## ✅ What's Been Added to Your Android App

### New Files Created:
1. **LessonsScreen.kt** - Main lessons interface
2. **Lesson.kt** - Data models for lessons, files, and progress
3. **LessonRepository.kt** - Database operations
4. **002_create_lessons_tables.sql** - Database migration

### Modified Files:
1. **StudentMainScreen.kt** - Added "Video Lessons" card
2. **MainActivity.kt** - Added lessons navigation route

---

## 📝 Step-by-Step Setup

### 1️⃣ Run Database Migration

Go to your Supabase Dashboard:
1. Open **SQL Editor**
2. Copy contents from `supabase/migrations/002_create_lessons_tables.sql`
3. Execute the SQL
4. Verify tables created: `lessons`, `lesson_files`, `lesson_progress`

### 2️⃣ Build and Test

```powershell
# Clean build
./gradlew clean

# Build the app
./gradlew build

# Or run directly from Android Studio
```

### 3️⃣ Test the Feature

1. **Launch the app**
2. **Login as a student**
3. **On main screen, find the "Video Lessons" card**
4. **Click any module button** (Decantation, Organs, Machines, Solar)
5. **See the lessons screen**

---

## 📱 How It Works

### Student Flow:
```
StudentMainScreen 
  → Click "Decantation" button
  → LessonsScreen(moduleId="decantation")
  → Shows lessons from database
  → Click video → Opens YouTube app
  → Click files → Downloads/opens file
```

### Data Flow:
```
LessonsScreen 
  → LessonRepository.getLessonsWithProgress()
  → Supabase query to 'lessons' table
  → Returns Lesson + LessonProgress
  → Display in UI with progress bars
```

---

## 🎬 Adding Real YouTube Videos

Replace sample video IDs in the database:

```sql
-- Update with real YouTube video IDs
UPDATE lessons 
SET youtube_video_id = 'REAL_VIDEO_ID_HERE',
    thumbnail_url = 'https://img.youtube.com/vi/REAL_VIDEO_ID_HERE/maxresdefault.jpg'
WHERE title = 'Introduction to Decantation';
```

To get YouTube video ID:
- Video URL: `https://www.youtube.com/watch?v=dQw4w9WgXcQ`
- Video ID: `dQw4w9WgXcQ` (everything after `v=`)

---

## 📁 Uploading Files

### Via Supabase Dashboard:
1. Go to **Storage** in Supabase Dashboard
2. Create bucket: `lesson-files` (make it public)
3. Upload files (PDF, PPTX, images)
4. Copy public URL
5. Insert into database:

```sql
INSERT INTO lesson_files (lesson_id, file_name, file_url, file_type, file_size)
VALUES (
  'LESSON_UUID_HERE',
  'Decantation Guide.pdf',
  'https://your-project.supabase.co/storage/v1/object/public/lesson-files/file.pdf',
  'pdf',
  1024000 -- size in bytes
);
```

### Programmatically (Web Admin):
```typescript
// Upload file to Supabase Storage
const { data, error } = await supabase.storage
  .from('lesson-files')
  .upload(`${lessonId}/${file.name}`, file)

// Get public URL
const { data: { publicUrl } } = supabase.storage
  .from('lesson-files')
  .getPublicUrl(data.path)

// Save to database
await supabase.from('lesson_files').insert({
  lesson_id: lessonId,
  file_name: file.name,
  file_url: publicUrl,
  file_type: file.type.split('/')[1],
  file_size: file.size
})
```

---

## 🔍 Testing Checklist

- [ ] Database migration executed successfully
- [ ] App builds without errors
- [ ] "Video Lessons" card appears on student main screen
- [ ] All 4 module buttons are visible
- [ ] Clicking a module opens LessonsScreen
- [ ] Lessons load from database
- [ ] Clicking video opens YouTube
- [ ] Files section expands/collapses
- [ ] Progress bars show correctly

---

## 🐛 Troubleshooting

### Issue: "No lessons available yet"
**Solution:** Make sure you ran the database migration and sample data is inserted.

### Issue: App crashes on navigation
**Solution:** Check that `LessonsScreen` import is added to MainActivity:
```kotlin
import com.example.escape_ar.ui.screens.LessonsScreen
```

### Issue: Video doesn't open
**Solution:** Ensure YouTube app is installed or browser is available on device.

### Issue: Files don't download
**Solution:** 
1. Check file URLs are accessible
2. Verify storage bucket is public
3. Grant storage permissions in AndroidManifest.xml

---

## 🌐 Web Admin Panel (Next Phase)

See `LESSONS_AND_WEB_ADMIN_GUIDE.md` for complete setup instructions.

**Quick Summary:**
- Separate workspace recommended
- Tech Stack: React + TypeScript + Vite + Supabase
- Features: CRUD lessons, upload files, edit quizzes, view analytics
- Deployment: Vercel or Netlify

---

## 📊 Analytics Available

Query lesson statistics:
```sql
SELECT * FROM lesson_statistics;
```

View user progress:
```sql
SELECT * FROM user_lesson_progress 
WHERE student_name = 'John Doe';
```

---

## 🎉 You're All Set!

Your Android app now has:
- ✅ Video lessons feature
- ✅ Downloadable files support
- ✅ Progress tracking
- ✅ Module-based organization
- ✅ YouTube integration

Next: Build the web admin panel to manage all this content! 🚀
