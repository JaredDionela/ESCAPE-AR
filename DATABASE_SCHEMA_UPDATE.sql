-- ============================================================================
-- DATABASE SCHEMA UPDATE: Remove Lesson Progress Tracking
-- ============================================================================
-- Purpose: Remove lesson_progress table and update CASCADE delete policies
-- Date: 2025-10-21
-- Reason: Simplify data model - YouTube handles video playback history
-- ============================================================================

-- STEP 1: Drop lesson_progress table if it exists
-- This will remove all video progress tracking data
-- YouTube's embedded player maintains its own playback history
DROP TABLE IF EXISTS lesson_progress CASCADE;

-- STEP 2: Verify CASCADE delete policies on user-related tables
-- Ensure that deleting a user removes all associated data

-- Check and update quiz_results foreign key
ALTER TABLE quiz_results
DROP CONSTRAINT IF EXISTS quiz_results_user_id_fkey;

ALTER TABLE quiz_results
ADD CONSTRAINT quiz_results_user_id_fkey
FOREIGN KEY (user_id) REFERENCES profiles(id)
ON DELETE CASCADE;

-- Check and update progress foreign key
ALTER TABLE progress
DROP CONSTRAINT IF EXISTS progress_user_id_fkey;

ALTER TABLE progress
ADD CONSTRAINT progress_user_id_fkey
FOREIGN KEY (user_id) REFERENCES profiles(id)
ON DELETE CASCADE;

-- STEP 3: Verify email uniqueness constraint for re-registration support
-- This allows deleted user emails to be reused
ALTER TABLE profiles
DROP CONSTRAINT IF EXISTS profiles_email_key;

ALTER TABLE profiles
ADD CONSTRAINT profiles_email_key UNIQUE (email);

-- ============================================================================
-- VERIFICATION QUERIES
-- ============================================================================
-- Run these to verify the changes were applied correctly

-- 1. Confirm lesson_progress table is dropped
SELECT EXISTS (
    SELECT FROM information_schema.tables 
    WHERE table_schema = 'public' 
    AND table_name = 'lesson_progress'
) AS lesson_progress_exists;
-- Expected: FALSE

-- 2. Check CASCADE delete policies
SELECT
    tc.table_name,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name,
    rc.delete_rule
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
    AND tc.table_schema = kcu.table_schema
JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
    AND ccu.table_schema = tc.table_schema
JOIN information_schema.referential_constraints AS rc
    ON tc.constraint_name = rc.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY'
    AND ccu.table_name = 'profiles'
    AND tc.table_name IN ('quiz_results', 'progress');
-- Expected: All should show delete_rule = 'CASCADE'

-- 3. Verify email uniqueness constraint
SELECT conname, contype
FROM pg_constraint
WHERE conrelid = 'profiles'::regclass
    AND contype = 'u'
    AND conname LIKE '%email%';
-- Expected: profiles_email_key | u

-- ============================================================================
-- TEST SCENARIO: User Deletion with CASCADE
-- ============================================================================
-- WARNING: This will delete test data. Only run in development/testing!

-- Create a test user
-- INSERT INTO profiles (id, email, display_name, role) 
-- VALUES (gen_random_uuid(), 'test.delete@example.com', 'Test User', 'student');

-- Add test progress data
-- INSERT INTO progress (id, user_id, module, best_score, completed)
-- SELECT gen_random_uuid(), id, 'decantation', 85, true
-- FROM profiles WHERE email = 'test.delete@example.com';

-- Add test quiz results
-- INSERT INTO quiz_results (id, user_id, question_id, selected_answer, is_correct)
-- SELECT gen_random_uuid(), p.id, q.id, 'A', true
-- FROM profiles p, quiz_questions q
-- WHERE p.email = 'test.delete@example.com'
-- LIMIT 1;

-- Verify data exists
-- SELECT 
--     (SELECT COUNT(*) FROM progress WHERE user_id = (SELECT id FROM profiles WHERE email = 'test.delete@example.com')) as progress_count,
--     (SELECT COUNT(*) FROM quiz_results WHERE user_id = (SELECT id FROM profiles WHERE email = 'test.delete@example.com')) as quiz_results_count;

-- Delete the user (should CASCADE delete all related data)
-- DELETE FROM profiles WHERE email = 'test.delete@example.com';

-- Verify all related data was deleted
-- SELECT 
--     (SELECT COUNT(*) FROM profiles WHERE email = 'test.delete@example.com') as user_count,
--     (SELECT COUNT(*) FROM progress WHERE user_id IN (SELECT id FROM profiles WHERE email = 'test.delete@example.com')) as progress_count,
--     (SELECT COUNT(*) FROM quiz_results WHERE user_id IN (SELECT id FROM profiles WHERE email = 'test.delete@example.com')) as quiz_results_count;
-- Expected: All counts = 0

-- Verify email is available for re-registration
-- INSERT INTO profiles (id, email, display_name, role) 
-- VALUES (gen_random_uuid(), 'test.delete@example.com', 'Test User Reregistered', 'student');
-- Expected: SUCCESS (no unique constraint violation)

-- Cleanup
-- DELETE FROM profiles WHERE email = 'test.delete@example.com';

-- ============================================================================
-- ROLLBACK SCRIPT (if needed)
-- ============================================================================
-- To restore lesson_progress table, uncomment and run:

/*
CREATE TABLE IF NOT EXISTS lesson_progress (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    lesson_id UUID NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    completed BOOLEAN DEFAULT FALSE,
    progress_percentage INTEGER CHECK (progress_percentage >= 0 AND progress_percentage <= 100),
    last_watched TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(user_id, lesson_id)
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_lesson_progress_user_id ON lesson_progress(user_id);
CREATE INDEX IF NOT EXISTS idx_lesson_progress_lesson_id ON lesson_progress(lesson_id);

-- Enable RLS (if using Row Level Security)
ALTER TABLE lesson_progress ENABLE ROW LEVEL SECURITY;

-- Add RLS policies (adjust as needed)
CREATE POLICY "Users can view their own lesson progress"
ON lesson_progress FOR SELECT
USING (auth.uid() = user_id);

CREATE POLICY "Users can insert their own lesson progress"
ON lesson_progress FOR INSERT
WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update their own lesson progress"
ON lesson_progress FOR UPDATE
USING (auth.uid() = user_id);
*/

-- ============================================================================
-- NOTES FOR DEVELOPERS
-- ============================================================================
-- 1. After running this migration, update application code to remove:
--    - lesson_progress table references
--    - Video progress tracking API endpoints
--    - Progress percentage calculations for lessons
--
-- 2. YouTube embedded player handles video playback history natively
--
-- 3. User deletion now:
--    - Removes user profile
--    - CASCADE deletes all quiz_results
--    - CASCADE deletes all progress records
--    - Releases email for re-registration
--
-- 4. Admin panel should be updated to:
--    - Add "Register New User" functionality
--    - Update "Edit User" to modify display_name, teacher_name, section
--    - Show clear warning on user deletion about data erasure
--    - Note that email and role cannot be changed after creation
--
-- 5. For analytics/reporting that previously used lesson_progress:
--    - Focus on quiz completion rates instead
--    - Use progress table for module completion status
--    - Lesson engagement can be tracked via YouTube Analytics API (optional)
-- ============================================================================
