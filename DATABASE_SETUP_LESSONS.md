# 📋 Database Setup - Copy and Paste into Supabase SQL Editor

## ⚠️ IMPORTANT: Run this in your Supabase SQL Editor

Go to: https://app.supabase.com → Your Project → SQL Editor → New Query

---

## 🗄️ Create Lessons Tables and Sample Data

```sql
-- =====================================================
-- LESSONS FEATURE - DATABASE SCHEMA
-- =====================================================

-- Enable UUID extension (if not already enabled)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =====================================================
-- 1. LESSONS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS lessons (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    module_id TEXT NOT NULL CHECK (module_id IN ('decantation', 'organ_system', 'simple_machines', 'solar_system')),
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    youtube_video_id TEXT NOT NULL,
    thumbnail_url TEXT,
    duration_minutes INTEGER NOT NULL DEFAULT 0,
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Indexes for faster queries
CREATE INDEX IF NOT EXISTS idx_lessons_module_id ON lessons(module_id);
CREATE INDEX IF NOT EXISTS idx_lessons_order ON lessons(module_id, order_index);

-- Row Level Security
ALTER TABLE lessons ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Anyone can view lessons" ON lessons;
CREATE POLICY "Anyone can view lessons"
    ON lessons FOR SELECT
    USING (true);

DROP POLICY IF EXISTS "Authenticated users can manage lessons" ON lessons;
CREATE POLICY "Authenticated users can manage lessons"
    ON lessons FOR ALL
    USING (auth.role() = 'authenticated');

-- =====================================================
-- 2. LESSON FILES TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS lesson_files (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    lesson_id UUID NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    file_name TEXT NOT NULL,
    file_url TEXT NOT NULL,
    file_type TEXT NOT NULL CHECK (file_type IN ('pdf', 'pptx', 'docx', 'image', 'other')),
    file_size BIGINT NOT NULL DEFAULT 0,
    uploaded_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_lesson_files_lesson_id ON lesson_files(lesson_id);

ALTER TABLE lesson_files ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Anyone can view lesson files" ON lesson_files;
CREATE POLICY "Anyone can view lesson files"
    ON lesson_files FOR SELECT
    USING (true);

DROP POLICY IF EXISTS "Authenticated users can manage lesson files" ON lesson_files;
CREATE POLICY "Authenticated users can manage lesson files"
    ON lesson_files FOR ALL
    USING (auth.role() = 'authenticated');

-- =====================================================
-- 3. LESSON PROGRESS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS lesson_progress (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    lesson_id UUID NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    completed BOOLEAN DEFAULT false,
    video_progress FLOAT DEFAULT 0.0 CHECK (video_progress >= 0.0 AND video_progress <= 1.0),
    last_watched_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(user_id, lesson_id)
);

CREATE INDEX IF NOT EXISTS idx_lesson_progress_user ON lesson_progress(user_id);
CREATE INDEX IF NOT EXISTS idx_lesson_progress_lesson ON lesson_progress(lesson_id);
CREATE INDEX IF NOT EXISTS idx_lesson_progress_user_lesson ON lesson_progress(user_id, lesson_id);

ALTER TABLE lesson_progress ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Users can view own lesson progress" ON lesson_progress;
CREATE POLICY "Users can view own lesson progress"
    ON lesson_progress FOR SELECT
    USING (auth.uid() = user_id);

DROP POLICY IF EXISTS "Users can insert own lesson progress" ON lesson_progress;
CREATE POLICY "Users can insert own lesson progress"
    ON lesson_progress FOR INSERT
    WITH CHECK (auth.uid() = user_id);

DROP POLICY IF EXISTS "Users can update own lesson progress" ON lesson_progress;
CREATE POLICY "Users can update own lesson progress"
    ON lesson_progress FOR UPDATE
    USING (auth.uid() = user_id);

-- =====================================================
-- 4. INSERT SAMPLE LESSONS
-- =====================================================

-- Decantation Module
INSERT INTO lessons (module_id, title, description, youtube_video_id, duration_minutes, order_index)
VALUES 
    ('decantation', 'Introduction to Decantation', 'Learn the basics of decantation and when to use this separation technique in chemistry.', 'dQw4w9WgXcQ', 15, 1),
    ('decantation', 'Decantation Laboratory Techniques', 'Master proper laboratory techniques for decanting liquids safely and effectively.', 'dQw4w9WgXcQ', 20, 2),
    ('decantation', 'Real-World Applications', 'Discover how decantation is used in everyday life and industrial processes.', 'dQw4w9WgXcQ', 18, 3)
ON CONFLICT DO NOTHING;

-- Organ System Module
INSERT INTO lessons (module_id, title, description, youtube_video_id, duration_minutes, order_index)
VALUES 
    ('organ_system', 'The Human Circulatory System', 'Understanding how blood flows through the body and the role of the heart.', 'dQw4w9WgXcQ', 25, 1),
    ('organ_system', 'The Respiratory System', 'How we breathe and exchange gases essential for life.', 'dQw4w9WgXcQ', 20, 2),
    ('organ_system', 'The Digestive System', 'Journey of food through the digestive tract and nutrient absorption.', 'dQw4w9WgXcQ', 22, 3),
    ('organ_system', 'The Nervous System', 'How your brain and nerves control your entire body.', 'dQw4w9WgXcQ', 24, 4)
ON CONFLICT DO NOTHING;

-- Simple Machines Module
INSERT INTO lessons (module_id, title, description, youtube_video_id, duration_minutes, order_index)
VALUES 
    ('simple_machines', 'Introduction to Simple Machines', 'Learn about the six types of simple machines and how they make work easier.', 'dQw4w9WgXcQ', 16, 1),
    ('simple_machines', 'Levers and Pulleys', 'Understanding mechanical advantage with levers and pulley systems.', 'dQw4w9WgXcQ', 18, 2),
    ('simple_machines', 'Inclined Planes and Wedges', 'How ramps and wedges reduce the force needed to move objects.', 'dQw4w9WgXcQ', 17, 3),
    ('simple_machines', 'Wheel and Axle, Screws', 'Exploring rotational simple machines in everyday objects.', 'dQw4w9WgXcQ', 19, 4)
ON CONFLICT DO NOTHING;

-- Solar System Module
INSERT INTO lessons (module_id, title, description, youtube_video_id, duration_minutes, order_index)
VALUES 
    ('solar_system', 'Overview of Our Solar System', 'Journey through the eight planets and their unique characteristics.', 'dQw4w9WgXcQ', 30, 1),
    ('solar_system', 'The Inner Planets', 'Exploring Mercury, Venus, Earth, and Mars - the rocky planets.', 'dQw4w9WgXcQ', 22, 2),
    ('solar_system', 'The Gas Giants', 'Discover Jupiter and Saturn - the largest planets in our system.', 'dQw4w9WgXcQ', 24, 3),
    ('solar_system', 'The Ice Giants and Beyond', 'Uranus, Neptune, and the mysterious Kuiper Belt.', 'dQw4w9WgXcQ', 26, 4)
ON CONFLICT DO NOTHING;

-- =====================================================
-- 5. CREATE ANALYTICS VIEWS
-- =====================================================

-- Lesson statistics for admin dashboard
CREATE OR REPLACE VIEW lesson_statistics AS
SELECT 
    l.id,
    l.module_id,
    l.title,
    COUNT(DISTINCT lp.user_id) as total_students,
    COUNT(DISTINCT CASE WHEN lp.completed THEN lp.user_id END) as completed_students,
    ROUND(AVG(lp.video_progress)::numeric, 2) as avg_progress,
    COUNT(lf.id) as file_count
FROM lessons l
LEFT JOIN lesson_progress lp ON l.id = lp.lesson_id
LEFT JOIN lesson_files lf ON l.id = lf.lesson_id
GROUP BY l.id, l.module_id, l.title;

-- User progress overview
CREATE OR REPLACE VIEW user_lesson_progress AS
SELECT 
    p.display_name as student_name,
    l.module_id,
    l.title as lesson_title,
    lp.completed,
    lp.video_progress,
    lp.last_watched_at,
    lp.completed_at
FROM lesson_progress lp
JOIN profiles p ON lp.user_id = p.id
JOIN lessons l ON lp.lesson_id = l.id
ORDER BY p.display_name, l.module_id, l.order_index;

-- =====================================================
-- 6. UPDATE TRIGGERS
-- =====================================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

DROP TRIGGER IF EXISTS update_lessons_updated_at ON lessons;
CREATE TRIGGER update_lessons_updated_at
    BEFORE UPDATE ON lessons
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_lesson_progress_updated_at ON lesson_progress;
CREATE TRIGGER update_lesson_progress_updated_at
    BEFORE UPDATE ON lesson_progress
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- ✅ DONE! Tables, sample data, and views created.
-- =====================================================
```

---

## ✅ Verification Queries

After running the above SQL, verify everything is working:

```sql
-- Check if tables exist
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_name IN ('lessons', 'lesson_files', 'lesson_progress');

-- Count lessons per module
SELECT module_id, COUNT(*) as lesson_count 
FROM lessons 
GROUP BY module_id;

-- View lesson statistics
SELECT * FROM lesson_statistics;

-- Check sample lessons
SELECT module_id, title, youtube_video_id, duration_minutes 
FROM lessons 
ORDER BY module_id, order_index;
```

---

## 🎬 Replace Sample YouTube Video IDs

The sample data uses placeholder video ID `dQw4w9WgXcQ`. Replace with real educational videos:

```sql
-- Example: Update a specific lesson with a real YouTube video
UPDATE lessons 
SET 
    youtube_video_id = 'YOUR_REAL_VIDEO_ID',
    thumbnail_url = 'https://img.youtube.com/vi/YOUR_REAL_VIDEO_ID/maxresdefault.jpg'
WHERE 
    title = 'Introduction to Decantation';

-- Bulk update all decantation lessons (example)
UPDATE lessons 
SET youtube_video_id = 'REAL_VIDEO_ID_1'
WHERE module_id = 'decantation' AND order_index = 1;

UPDATE lessons 
SET youtube_video_id = 'REAL_VIDEO_ID_2'
WHERE module_id = 'decantation' AND order_index = 2;
```

---

## 📁 Setup Supabase Storage for Files

1. Go to **Storage** in Supabase Dashboard
2. Click **New Bucket**
3. Name: `lesson-files`
4. Make it **Public**
5. Click **Create bucket**

### Set Storage Policies:

```sql
-- Allow public read access to lesson files
CREATE POLICY "Public read access for lesson files"
ON storage.objects FOR SELECT
USING (bucket_id = 'lesson-files');

-- Allow authenticated users to upload files
CREATE POLICY "Authenticated users can upload lesson files"
ON storage.objects FOR INSERT
WITH CHECK (
    bucket_id = 'lesson-files' 
    AND auth.role() = 'authenticated'
);
```

---

## 🎉 All Done!

Your database is now ready for the Lessons feature! 🚀

Next steps:
1. Build and run your Android app
2. Test the lessons feature
3. Upload real YouTube video IDs
4. Upload files to Supabase Storage
5. Start building the web admin panel!
