-- =====================================================
-- Science Learning App - Database Schema
-- Migration: 001_create_tables.sql
-- =====================================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =====================================================
-- 1. PROFILES TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    display_name TEXT NOT NULL,
    teacher_name TEXT,
    section TEXT,
    avatar_url TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- RLS Policies for profiles
ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Users can view own profile" ON profiles;
CREATE POLICY "Users can view own profile"
    ON profiles FOR SELECT
    USING (auth.uid() = id);

DROP POLICY IF EXISTS "Users can update own profile" ON profiles;
CREATE POLICY "Users can update own profile"
    ON profiles FOR UPDATE
    USING (auth.uid() = id);

DROP POLICY IF EXISTS "Users can insert own profile" ON profiles;
CREATE POLICY "Users can insert own profile"
    ON profiles FOR INSERT
    WITH CHECK (auth.uid() = id);

-- =====================================================
-- 2. VIDEOS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS videos (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    topic TEXT NOT NULL CHECK (topic IN ('decantation', 'organ_system', 'simple_machines', 'solar_system')),
    title TEXT NOT NULL,
    youtube_id TEXT NOT NULL,
    thumbnail_url TEXT,
    duration_seconds INTEGER,
    summary TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- RLS Policies for videos (public read)
ALTER TABLE videos ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Anyone can view videos" ON videos;
CREATE POLICY "Anyone can view videos"
    ON videos FOR SELECT
    USING (true);

-- =====================================================
-- 3. DEPED_LESSONS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS deped_lessons (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    topic TEXT NOT NULL CHECK (topic IN ('decantation', 'organ_system', 'simple_machines', 'solar_system')),
    short_summary TEXT NOT NULL,
    full_text TEXT,
    resource_url TEXT,
    visual_elements JSONB, -- Store visual content metadata
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- RLS Policies for deped_lessons (public read)
ALTER TABLE deped_lessons ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Anyone can view deped lessons" ON deped_lessons;
CREATE POLICY "Anyone can view deped lessons"
    ON deped_lessons FOR SELECT
    USING (true);

-- =====================================================
-- 4. VIDEO_PROGRESS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS video_progress (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    video_id UUID NOT NULL REFERENCES videos(id) ON DELETE CASCADE,
    watched_seconds INTEGER DEFAULT 0,
    completed BOOLEAN DEFAULT false,
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(user_id, video_id)
);

-- RLS Policies for video_progress
ALTER TABLE video_progress ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Users can view own video progress" ON video_progress;
CREATE POLICY "Users can view own video progress"
    ON video_progress FOR SELECT
    USING (auth.uid() = user_id);

DROP POLICY IF EXISTS "Users can insert own video progress" ON video_progress;
CREATE POLICY "Users can insert own video progress"
    ON video_progress FOR INSERT
    WITH CHECK (auth.uid() = user_id);

DROP POLICY IF EXISTS "Users can update own video progress" ON video_progress;
CREATE POLICY "Users can update own video progress"
    ON video_progress FOR UPDATE
    USING (auth.uid() = user_id);

-- =====================================================
-- 5. USER_SETTINGS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS user_settings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL UNIQUE REFERENCES auth.users(id) ON DELETE CASCADE,
    music_volume DECIMAL(3,2) DEFAULT 0.70 CHECK (music_volume BETWEEN 0 AND 1),
    sfx_volume DECIMAL(3,2) DEFAULT 0.70 CHECK (sfx_volume BETWEEN 0 AND 1),
    captions_enabled BOOLEAN DEFAULT true,
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- RLS Policies for user_settings
ALTER TABLE user_settings ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Users can view own settings" ON user_settings;
CREATE POLICY "Users can view own settings"
    ON user_settings FOR SELECT
    USING (auth.uid() = user_id);

DROP POLICY IF EXISTS "Users can insert own settings" ON user_settings;
CREATE POLICY "Users can insert own settings"
    ON user_settings FOR INSERT
    WITH CHECK (auth.uid() = user_id);

DROP POLICY IF EXISTS "Users can update own settings" ON user_settings;
CREATE POLICY "Users can update own settings"
    ON user_settings FOR UPDATE
    USING (auth.uid() = user_id);

-- =====================================================
-- 6. TRIGGERS FOR UPDATED_AT
-- =====================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS update_profiles_updated_at ON profiles;
CREATE TRIGGER update_profiles_updated_at
    BEFORE UPDATE ON profiles
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_deped_lessons_updated_at ON deped_lessons;
CREATE TRIGGER update_deped_lessons_updated_at
    BEFORE UPDATE ON deped_lessons
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_video_progress_updated_at ON video_progress;
CREATE TRIGGER update_video_progress_updated_at
    BEFORE UPDATE ON video_progress
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_user_settings_updated_at ON user_settings;
CREATE TRIGGER update_user_settings_updated_at
    BEFORE UPDATE ON user_settings
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- 7. SAMPLE DATA (Optional - for development)
-- =====================================================
-- Insert sample videos for each topic
INSERT INTO videos (topic, title, youtube_id, summary, duration_seconds) VALUES
    ('decantation', 'Introduction to Decantation', 'dQw4w9WgXcQ', 'Learn the basics of separating mixtures using decantation technique.', 300),
    ('organ_system', 'Human Organ Systems Overview', 'dQw4w9WgXcQ', 'Understanding how different organ systems work together in the human body.', 420),
    ('simple_machines', 'Six Types of Simple Machines', 'dQw4w9WgXcQ', 'Explore levers, pulleys, wedges, screws, inclined planes, and wheel-and-axle.', 480),
    ('solar_system', 'Tour of Our Solar System', 'dQw4w9WgXcQ', 'Journey through the planets and celestial bodies of our solar system.', 600)
ON CONFLICT DO NOTHING;

-- Insert sample DepEd lessons
INSERT INTO deped_lessons (topic, short_summary, full_text) VALUES
    ('decantation', 'Separate liquids by pouring carefully', 'Decantation is a method to separate mixtures by carefully pouring the liquid away from the sediment.'),
    ('organ_system', 'Body systems work together for life', 'The human body has 11 major organ systems including circulatory, respiratory, digestive, and nervous systems.'),
    ('simple_machines', 'Tools that make work easier', 'Simple machines include levers, pulleys, wedges, screws, inclined planes, and wheel-and-axle mechanisms.'),
    ('solar_system', 'Eight planets orbit our Sun', 'The solar system consists of the Sun, eight planets, their moons, and other celestial objects.')
ON CONFLICT DO NOTHING;

-- =====================================================
-- 8. INDEXES FOR PERFORMANCE
-- =====================================================
CREATE INDEX IF NOT EXISTS idx_video_progress_user_id ON video_progress(user_id);
CREATE INDEX IF NOT EXISTS idx_video_progress_video_id ON video_progress(video_id);
CREATE INDEX IF NOT EXISTS idx_videos_topic ON videos(topic);
CREATE INDEX IF NOT EXISTS idx_deped_lessons_topic ON deped_lessons(topic);

-- =====================================================
-- END OF MIGRATION
-- =====================================================
