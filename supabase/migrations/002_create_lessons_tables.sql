-- =====================================================
-- Lessons Feature - Database Schema
-- Migration: 002_create_lessons_tables.sql
-- =====================================================

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

-- Index for faster queries
CREATE INDEX IF NOT EXISTS idx_lessons_module_id ON lessons(module_id);
CREATE INDEX IF NOT EXISTS idx_lessons_order ON lessons(module_id, order_index);

-- RLS Policies for lessons (public read, admin write)
ALTER TABLE lessons ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Anyone can view lessons" ON lessons;
CREATE POLICY "Anyone can view lessons"
    ON lessons FOR SELECT
    USING (true);

-- Only authenticated users with admin role can insert/update/delete
-- For now, we'll allow authenticated users to manage (update this later with roles)
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

-- Index for faster queries
CREATE INDEX IF NOT EXISTS idx_lesson_files_lesson_id ON lesson_files(lesson_id);

-- RLS Policies for lesson_files
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

-- Index for faster queries
CREATE INDEX IF NOT EXISTS idx_lesson_progress_user ON lesson_progress(user_id);
CREATE INDEX IF NOT EXISTS idx_lesson_progress_lesson ON lesson_progress(lesson_id);
CREATE INDEX IF NOT EXISTS idx_lesson_progress_user_lesson ON lesson_progress(user_id, lesson_id);

-- RLS Policies for lesson_progress
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
-- 4. SAMPLE DATA (Optional - for testing)
-- =====================================================
-- Insert sample lessons for Decantation module
INSERT INTO lessons (module_id, title, description, youtube_video_id, duration_minutes, order_index)
VALUES 
    ('decantation', 'Introduction to Decantation', 'Learn the basics of decantation and when to use it', 'dQw4w9WgXcQ', 15, 1),
    ('decantation', 'Decantation Techniques', 'Master different decantation techniques', 'dQw4w9WgXcQ', 20, 2)
ON CONFLICT DO NOTHING;

-- Insert sample lessons for Organ System module
INSERT INTO lessons (module_id, title, description, youtube_video_id, duration_minutes, order_index)
VALUES 
    ('organ_system', 'The Circulatory System', 'Understanding how blood flows through the body', 'dQw4w9WgXcQ', 25, 1),
    ('organ_system', 'The Respiratory System', 'How we breathe and exchange gases', 'dQw4w9WgXcQ', 20, 2)
ON CONFLICT DO NOTHING;

-- Insert sample lessons for Simple Machines module
INSERT INTO lessons (module_id, title, description, youtube_video_id, duration_minutes, order_index)
VALUES 
    ('simple_machines', 'Levers and Pulleys', 'Learn about mechanical advantage', 'dQw4w9WgXcQ', 18, 1),
    ('simple_machines', 'Inclined Planes and Wedges', 'Understanding force and distance', 'dQw4w9WgXcQ', 16, 2)
ON CONFLICT DO NOTHING;

-- Insert sample lessons for Solar System module
INSERT INTO lessons (module_id, title, description, youtube_video_id, duration_minutes, order_index)
VALUES 
    ('solar_system', 'Our Solar System Overview', 'Journey through the planets', 'dQw4w9WgXcQ', 30, 1),
    ('solar_system', 'The Inner Planets', 'Mercury, Venus, Earth, and Mars', 'dQw4w9WgXcQ', 22, 2)
ON CONFLICT DO NOTHING;

-- =====================================================
-- 5. FUNCTIONS AND TRIGGERS
-- =====================================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Trigger for lessons table
DROP TRIGGER IF EXISTS update_lessons_updated_at ON lessons;
CREATE TRIGGER update_lessons_updated_at
    BEFORE UPDATE ON lessons
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Trigger for lesson_progress table
DROP TRIGGER IF EXISTS update_lesson_progress_updated_at ON lesson_progress;
CREATE TRIGGER update_lesson_progress_updated_at
    BEFORE UPDATE ON lesson_progress
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- 6. ADMIN VIEWS (For web admin panel)
-- =====================================================

-- View to see lesson statistics
CREATE OR REPLACE VIEW lesson_statistics AS
SELECT 
    l.id,
    l.module_id,
    l.title,
    COUNT(DISTINCT lp.user_id) as total_students,
    COUNT(DISTINCT CASE WHEN lp.completed THEN lp.user_id END) as completed_students,
    AVG(lp.video_progress) as avg_progress,
    COUNT(lf.id) as file_count
FROM lessons l
LEFT JOIN lesson_progress lp ON l.id = lp.lesson_id
LEFT JOIN lesson_files lf ON l.id = lf.lesson_id
GROUP BY l.id, l.module_id, l.title;

-- View to see user progress across all lessons
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
-- NOTES FOR WEB ADMIN PANEL:
-- =====================================================
-- 1. Use Supabase Storage for file uploads
-- 2. Generate signed URLs for file downloads
-- 3. Use the lesson_statistics view for analytics
-- 4. Implement role-based access control for admin users
-- 5. Consider adding a separate 'admins' table with roles
-- =====================================================
