-- ====================================================================
-- Diagnose User Deletion Issues
-- Run this to see what's blocking deletion
-- ====================================================================

-- Check 1: Your teacher account
SELECT 
  '=== YOUR TEACHER ACCOUNT ===' as info;

SELECT 
  id,
  email,
  display_name,
  role,
  created_at
FROM profiles
WHERE id = auth.uid();

-- Check 2: Your students
SELECT 
  '=== YOUR STUDENTS ===' as info;

SELECT 
  id,
  display_name,
  email,
  section,
  teacher_id,
  (SELECT COUNT(*) FROM quiz_results WHERE user_id = profiles.id) as quiz_results_count,
  (SELECT COUNT(*) FROM progress WHERE user_id = profiles.id) as progress_count
FROM profiles
WHERE teacher_id = auth.uid()
  AND role = 'student'
ORDER BY display_name
LIMIT 10;

-- Check 3: DELETE policies on profiles
SELECT 
  '=== PROFILES DELETE POLICIES ===' as info;

SELECT 
  policyname,
  cmd,
  qual as using_clause,
  with_check
FROM pg_policies
WHERE tablename = 'profiles'
  AND cmd = 'DELETE';

-- Check 4: DELETE policies on quiz_results
SELECT 
  '=== QUIZ_RESULTS DELETE POLICIES ===' as info;

SELECT 
  policyname,
  cmd,
  qual as using_clause
FROM pg_policies
WHERE tablename = 'quiz_results'
  AND cmd = 'DELETE';

-- Check 5: DELETE policies on progress
SELECT 
  '=== PROGRESS DELETE POLICIES ===' as info;

SELECT 
  policyname,
  cmd,
  qual as using_clause
FROM pg_policies
WHERE tablename = 'progress'
  AND cmd = 'DELETE';

-- Check 6: Cleanup trigger
SELECT 
  '=== CLEANUP TRIGGER ===' as info;

SELECT 
  trigger_name,
  event_manipulation,
  event_object_table,
  action_timing,
  action_statement
FROM information_schema.triggers
WHERE trigger_name LIKE '%cleanup%'
  OR trigger_name LIKE '%delete%';

-- Check 7: RPC function for deletion
SELECT 
  '=== DELETE RPC FUNCTION ===' as info;

SELECT 
  proname as function_name,
  prosecdef as security_definer,
  provolatile
FROM pg_proc
WHERE proname LIKE '%delete%user%';

-- ====================================================================
-- RESULTS INTERPRETATION
-- ====================================================================

/*
WHAT TO LOOK FOR:

1. YOUR TEACHER ACCOUNT:
   - Should show your email and role='teacher'
   - If empty, you're not logged in correctly

2. YOUR STUDENTS:
   - Should show students with teacher_id matching your ID
   - If empty, no students assigned to you

3. DELETE POLICIES:
   - Should see at least ONE policy for each table
   - profiles: "Teachers can delete their students"
   - quiz_results: "Teachers can delete students quiz results"
   - progress: "Teachers can delete students progress"
   
   If missing → Run FIX_DELETE_USER_RLS.sql

4. CLEANUP TRIGGER:
   - Should see "trigger_cleanup_user_data" on profiles table
   - If missing → Run FIX_DELETE_USER_RLS.sql

5. RPC FUNCTION:
   - "delete_user_completely" if you want complete auth deletion
   - Optional - direct deletion works without it

NEXT STEPS:
- If policies are missing → Run FIX_DELETE_USER_RLS.sql
- If you have policies → Check browser console for errors
- If RPC function missing → Optional, run SETUP_DELETE_USER_FUNCTION.sql
*/
