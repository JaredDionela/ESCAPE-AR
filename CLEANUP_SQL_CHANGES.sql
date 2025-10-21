-- ============================================
-- CLEANUP SQL SCRIPT
-- Removes unused features and simplifies schema
-- Run in Supabase SQL Editor
-- ============================================

-- ============================================
-- 1. DROP USER_SETTINGS TABLE (if exists)
-- ============================================
-- Remove user_settings feature entirely
-- Note: This section is safe to skip if table doesn't exist

-- Check if table exists first, then drop
DO $$
BEGIN
    -- Drop the table if it exists
    IF EXISTS (
        SELECT 1 FROM information_schema.tables 
        WHERE table_name = 'user_settings'
    ) THEN
        DROP TABLE user_settings CASCADE;
        RAISE NOTICE 'user_settings table dropped successfully';
    ELSE
        RAISE NOTICE 'user_settings table does not exist - skipping';
    END IF;
END $$;

-- Comment on profiles table
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables 
        WHERE table_name = 'profiles'
    ) THEN
        COMMENT ON TABLE profiles IS 'User profiles with teacher_name and section persisted';
    END IF;
END $$;

-- ============================================
-- 2. VERIFY PROFILES TABLE HAS REQUIRED COLUMNS
-- ============================================
-- Ensure teacher_name and section exist in profiles

DO $$
BEGIN
    -- Add teacher_name if not exists
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'profiles' AND column_name = 'teacher_name'
    ) THEN
        ALTER TABLE profiles ADD COLUMN teacher_name TEXT;
    END IF;
    
    -- Add section if not exists
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'profiles' AND column_name = 'section'
    ) THEN
        ALTER TABLE profiles ADD COLUMN section TEXT;
    END IF;
END $$;

-- ============================================
-- 3. FIX RLS POLICIES FOR PROFILE UPDATES
-- ============================================
-- Ensure users can update their own profiles

-- Drop existing policies if any
DROP POLICY IF EXISTS "Users can view own profile" ON profiles;
DROP POLICY IF EXISTS "Users can update own profile" ON profiles;
DROP POLICY IF EXISTS "Users can insert own profile" ON profiles;

-- Allow users to view their own profile
CREATE POLICY "Users can view own profile"
    ON profiles FOR SELECT
    USING (auth.uid() = id);

-- Allow users to update their own profile
CREATE POLICY "Users can update own profile"
    ON profiles FOR UPDATE
    USING (auth.uid() = id)
    WITH CHECK (auth.uid() = id);

-- Allow users to insert their own profile
CREATE POLICY "Users can insert own profile"
    ON profiles FOR INSERT
    WITH CHECK (auth.uid() = id);

-- ============================================
-- 4. SIMPLIFY LESSON_PROGRESS (Optional)
-- ============================================
-- Since we're removing lesson tracking from web-admin,
-- we can keep the table but it won't be displayed

-- Keep lesson_progress table for future use
-- No changes needed

-- ============================================
-- 5. ENSURE PROGRESS TABLE IS OPTIMIZED
-- ============================================
-- Main table for quiz tracking

-- Verify progress table structure
DO $$
BEGIN
    -- Ensure best_score exists
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'progress' AND column_name = 'best_score'
    ) THEN
        ALTER TABLE progress ADD COLUMN best_score NUMERIC(5,2) DEFAULT 0;
    END IF;
    
    -- Ensure completed exists
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'progress' AND column_name = 'completed'
    ) THEN
        ALTER TABLE progress ADD COLUMN completed BOOLEAN DEFAULT false;
    END IF;
END $$;

-- Add index for faster queries
CREATE INDEX IF NOT EXISTS idx_progress_user_module ON progress(user_id, module);
CREATE INDEX IF NOT EXISTS idx_progress_completed ON progress(completed);

-- ============================================
-- 6. ENSURE QUIZ_RESULTS TABLE IS OPTIMIZED
-- ============================================
-- For tracking quiz attempts

-- Verify quiz_results has required columns
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'quiz_results' AND column_name = 'score_percentage'
    ) THEN
        ALTER TABLE quiz_results ADD COLUMN score_percentage NUMERIC(5,2);
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'quiz_results' AND column_name = 'total_questions'
    ) THEN
        ALTER TABLE quiz_results ADD COLUMN total_questions INTEGER;
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'quiz_results' AND column_name = 'correct_answers'
    ) THEN
        ALTER TABLE quiz_results ADD COLUMN correct_answers INTEGER;
    END IF;
END $$;

-- Add indexes for performance
CREATE INDEX IF NOT EXISTS idx_quiz_results_user ON quiz_results(user_id);
CREATE INDEX IF NOT EXISTS idx_quiz_results_module ON quiz_results(module_id);
CREATE INDEX IF NOT EXISTS idx_quiz_results_created ON quiz_results(created_at DESC);

-- ============================================
-- 7. VERIFICATION QUERIES
-- ============================================

-- Check what tables exist
SELECT 
    'Existing Tables' as check_type,
    table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
    AND table_type = 'BASE TABLE'
    AND table_name IN ('profiles', 'progress', 'quiz_results', 'lesson_progress', 'user_settings')
ORDER BY table_name;

-- Check profiles columns
SELECT 
    'Profiles Columns' as check_type,
    column_name,
    data_type
FROM information_schema.columns
WHERE table_name = 'profiles'
ORDER BY ordinal_position;

-- Check progress table
SELECT 
    'Progress Table Stats' as check_type,
    COUNT(*) as total_records,
    COUNT(DISTINCT user_id) as unique_users,
    COUNT(*) FILTER (WHERE completed = true) as completed_quizzes
FROM progress;

-- Check quiz_results table
SELECT 
    'Quiz Results Stats' as check_type,
    COUNT(*) as total_attempts,
    COUNT(DISTINCT user_id) as unique_users,
    AVG(score_percentage) as avg_score
FROM quiz_results
WHERE score_percentage IS NOT NULL;

-- ============================================
-- 8. SUCCESS MESSAGE
-- ============================================

SELECT 
    '✅ CLEANUP COMPLETE' as status,
    'user_settings table dropped' as action_1,
    'Profile RLS policies fixed' as action_2,
    'teacher_name and section verified in profiles' as action_3,
    'Indexes created for performance' as action_4,
    'Schema optimized for simplified UI' as action_5;
