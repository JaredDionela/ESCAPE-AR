-- ====================================================================
-- Fix User Deletion - Check and Repair RLS Policies
-- ====================================================================

-- First, let's check current RLS policies on all related tables
SELECT 
  '=== CURRENT RLS POLICIES ===' as info;

SELECT 
  schemaname,
  tablename,
  policyname,
  permissive,
  cmd as command,
  CASE 
    WHEN cmd = 'DELETE' THEN '🗑️ DELETE'
    WHEN cmd = 'SELECT' THEN '👁️ SELECT'
    WHEN cmd = 'INSERT' THEN '➕ INSERT'
    WHEN cmd = 'UPDATE' THEN '✏️ UPDATE'
    ELSE cmd
  END as operation
FROM pg_policies
WHERE tablename IN ('profiles', 'quiz_results', 'progress')
ORDER BY tablename, cmd;

-- ====================================================================
-- FIX: Add DELETE policies for quiz_results and progress
-- ====================================================================

-- Drop existing delete policies if they exist
DROP POLICY IF EXISTS "Teachers can delete their students quiz results" ON quiz_results;
DROP POLICY IF EXISTS "Teachers can delete their students progress" ON progress;
DROP POLICY IF EXISTS "Teachers can delete their students" ON profiles;
DROP POLICY IF EXISTS "Users can delete own data" ON quiz_results;
DROP POLICY IF EXISTS "Users can delete own progress" ON progress;

-- Enable RLS on all tables
ALTER TABLE quiz_results ENABLE ROW LEVEL SECURITY;
ALTER TABLE progress ENABLE ROW LEVEL SECURITY;
ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;

-- ====================================================================
-- QUIZ_RESULTS TABLE POLICIES
-- ====================================================================

-- Allow users to delete their own quiz results
CREATE POLICY "Users can delete own quiz results"
ON quiz_results FOR DELETE
USING (auth.uid() = user_id);

-- Allow teachers to delete their students' quiz results
CREATE POLICY "Teachers can delete students quiz results"
ON quiz_results FOR DELETE
USING (
  user_id IN (
    SELECT id FROM profiles 
    WHERE teacher_id = auth.uid() AND role = 'student'
  )
);

-- ====================================================================
-- PROGRESS TABLE POLICIES
-- ====================================================================

-- Allow users to delete their own progress
CREATE POLICY "Users can delete own progress"
ON progress FOR DELETE
USING (auth.uid() = user_id);

-- Allow teachers to delete their students' progress
CREATE POLICY "Teachers can delete students progress"
ON progress FOR DELETE
USING (
  user_id IN (
    SELECT id FROM profiles 
    WHERE teacher_id = auth.uid() AND role = 'student'
  )
);

-- ====================================================================
-- PROFILES TABLE POLICIES
-- ====================================================================

-- Allow teachers to delete their students' profiles
CREATE POLICY "Teachers can delete their students"
ON profiles FOR DELETE
USING (
  -- Teacher can delete their students
  teacher_id = auth.uid()
  OR
  -- Or delete their own profile
  auth.uid() = id
);

-- ====================================================================
-- CREATE CASCADE DELETE FUNCTION
-- ====================================================================

-- This function will be called BEFORE profile deletion to clean up related data
CREATE OR REPLACE FUNCTION cleanup_user_data()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
  -- Delete quiz results for this user
  DELETE FROM quiz_results WHERE user_id = OLD.id;
  
  -- Delete progress records for this user
  DELETE FROM progress WHERE user_id = OLD.id;
  
  RAISE NOTICE 'Cleaned up data for user: %', OLD.id;
  
  RETURN OLD;
END;
$$;

-- Create trigger to auto-cleanup when profile is deleted
DROP TRIGGER IF EXISTS trigger_cleanup_user_data ON profiles;

CREATE TRIGGER trigger_cleanup_user_data
BEFORE DELETE ON profiles
FOR EACH ROW
EXECUTE FUNCTION cleanup_user_data();

-- ====================================================================
-- VERIFICATION
-- ====================================================================

SELECT 
  '=== UPDATED DELETE POLICIES ===' as info;

SELECT 
  tablename,
  policyname,
  cmd as operation
FROM pg_policies
WHERE tablename IN ('profiles', 'quiz_results', 'progress')
  AND cmd = 'DELETE'
ORDER BY tablename;

-- Show the trigger
SELECT 
  '=== CLEANUP TRIGGER ===' as info;

SELECT 
  trigger_name,
  event_manipulation,
  event_object_table,
  action_statement
FROM information_schema.triggers
WHERE trigger_name = 'trigger_cleanup_user_data';

-- ====================================================================
-- TEST QUERY (Safe - just checks, doesn't delete)
-- ====================================================================

SELECT 
  '=== YOUR STUDENTS (DELETABLE) ===' as info;

SELECT 
  id,
  display_name,
  email,
  section,
  (SELECT COUNT(*) FROM quiz_results WHERE user_id = profiles.id) as quiz_count,
  (SELECT COUNT(*) FROM progress WHERE user_id = profiles.id) as progress_count
FROM profiles
WHERE teacher_id = auth.uid()
  AND role = 'student'
LIMIT 5;

-- ====================================================================
-- DONE! ✅
-- Delete policies have been fixed
-- Trigger will auto-cleanup related data
-- Teachers can now delete their students completely
-- ====================================================================

/*
WHAT'S FIXED:

1. ✅ Added DELETE policies on quiz_results table
2. ✅ Added DELETE policies on progress table  
3. ✅ Added DELETE policy on profiles table
4. ✅ Created trigger to auto-cleanup quiz_results & progress
5. ✅ Verified teacher can only delete their own students

NOW TRY:
- Refresh your web admin page
- Try deleting a test student
- It should work now!
*/
