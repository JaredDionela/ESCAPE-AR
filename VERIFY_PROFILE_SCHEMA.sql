-- ============================================
-- ANDROID PROFILE FIX - DATABASE VERIFICATION
-- Run these queries to verify and test the new schema
-- ============================================

-- ========================================
-- STEP 1: VERIFY SCHEMA
-- ========================================

-- Check all columns in profiles table
SELECT 
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_name = 'profiles'
ORDER BY ordinal_position;

-- Expected columns:
-- id, email, full_name, display_name, role, teacher_id, section, created_at, updated_at
-- (teacher_name might still exist for backwards compatibility)

-- ========================================
-- STEP 2: CHECK EXISTING DATA
-- ========================================

-- View all teachers
SELECT 
    id,
    email,
    display_name,
    role
FROM profiles
WHERE role = 'teacher';

-- View all students with their teachers
SELECT 
    s.id as student_id,
    s.email as student_email,
    s.display_name as student_name,
    s.teacher_id,
    s.section,
    t.display_name as teacher_name,
    CASE 
        WHEN s.teacher_id IS NULL THEN '❌ No teacher assigned'
        ELSE '✅ Teacher assigned'
    END as status
FROM profiles s
LEFT JOIN profiles t ON s.teacher_id = t.id
WHERE s.role = 'student'
ORDER BY s.created_at DESC;

-- ========================================
-- STEP 3: TEST THE JOIN QUERY (What App Uses)
-- ========================================

-- This is how the Android app will fetch profile with teacher info
-- Using Postgrest embedded resources syntax
SELECT 
    id,
    email,
    display_name,
    role,
    teacher_id,
    section
FROM profiles
WHERE id = 'YOUR-STUDENT-UUID-HERE';

-- Then in a separate query, fetch teacher info:
SELECT display_name
FROM profiles
WHERE id = 'TEACHER-UUID-FROM-ABOVE';

-- ========================================
-- STEP 4: MIGRATE OLD DATA (If Needed)
-- ========================================

-- If you have students with teacher_name but no teacher_id, run this migration:
UPDATE profiles p1
SET teacher_id = (
    SELECT p2.id 
    FROM profiles p2 
    WHERE LOWER(p2.display_name) = LOWER(p1.teacher_name)
      AND p2.role = 'teacher'
    LIMIT 1
)
WHERE p1.role = 'student' 
  AND p1.teacher_name IS NOT NULL
  AND p1.teacher_id IS NULL;

-- Verify migration worked:
SELECT 
    display_name as student,
    teacher_name as old_teacher_name,
    teacher_id as new_teacher_id,
    CASE 
        WHEN teacher_id IS NOT NULL THEN '✅ Migrated'
        ELSE '❌ Migration failed'
    END as migration_status
FROM profiles
WHERE role = 'student'
  AND teacher_name IS NOT NULL;

-- ========================================
-- STEP 5: TEST QUERIES FOR ANDROID APP
-- ========================================

-- Query 1: Get student profile with teacher info
-- (Android app uses this when loading profile screen)
WITH student_profile AS (
    SELECT 
        s.id,
        s.email,
        s.display_name,
        s.role,
        s.teacher_id,
        s.section,
        t.display_name as teacher_name
    FROM profiles s
    LEFT JOIN profiles t ON s.teacher_id = t.id
    WHERE s.id = 'YOUR-STUDENT-UUID'
)
SELECT * FROM student_profile;

-- Query 2: Get all teachers (for dropdown in signup/edit)
SELECT 
    id,
    display_name,
    email
FROM profiles
WHERE role = 'teacher'
ORDER BY display_name ASC;

-- ========================================
-- STEP 6: INSERT TEST DATA (Optional)
-- ========================================

-- Create a test teacher (if needed)
INSERT INTO profiles (id, email, display_name, role, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'test.teacher@example.com',
    'Test Teacher',
    'teacher',
    NOW(),
    NOW()
)
RETURNING id, display_name;

-- Create a test student linked to teacher
-- (Replace TEACHER-UUID with actual teacher ID from above)
INSERT INTO profiles (id, email, display_name, role, teacher_id, section, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'test.student@example.com',
    'Test Student',
    'student',
    'TEACHER-UUID-HERE',
    'Section A',
    NOW(),
    NOW()
)
RETURNING id, display_name, teacher_id;

-- ========================================
-- STEP 7: CLEANUP (Optional)
-- ========================================

-- After verifying the migration works, you can drop the old teacher_name column
-- WARNING: This is irreversible! Only do this after thorough testing.
-- ALTER TABLE profiles DROP COLUMN IF EXISTS teacher_name;

-- ========================================
-- STEP 8: VERIFY RLS POLICIES
-- ========================================

-- Check that students can see their teacher's names
SELECT 
    policyname,
    cmd,
    qual as using_clause
FROM pg_policies
WHERE tablename = 'profiles'
ORDER BY policyname;

-- Should include:
-- anyone_can_see_teachers - FOR SELECT - USING (role = 'teacher')
-- simple_select_own - FOR SELECT - USING (auth.uid() = id)

-- ========================================
-- SUMMARY
-- ========================================

SELECT 
    '✅ VERIFICATION COMPLETE' as status,
    COUNT(CASE WHEN role = 'teacher' THEN 1 END) as teacher_count,
    COUNT(CASE WHEN role = 'student' THEN 1 END) as student_count,
    COUNT(CASE WHEN role = 'student' AND teacher_id IS NOT NULL THEN 1 END) as students_with_teacher,
    COUNT(CASE WHEN role = 'student' AND teacher_id IS NULL THEN 1 END) as students_without_teacher
FROM profiles;
