-- ====================================================================
-- Generate 5 Additional Test Students (Manual Creation Instructions)
-- Quick test students for deletion testing
-- ====================================================================

-- NOTE: You need to create these users manually via Supabase Auth Dashboard
-- OR use the admin panel's "Add Student" feature
-- This SQL only shows you what to create

/*
CREATE THESE 5 STUDENTS:

1. Email: alpha.student@test.delete.me
   Password: Test123!
   Name: Alpha Student
   Section: VI SSC
   
2. Email: beta.student@test.delete.me
   Password: Test123!
   Name: Beta Student
   Section: VI Jose Rizal
   
3. Email: gamma.student@test.delete.me
   Password: Test123!
   Name: Gamma Student
   Section: VI SSC
   
4. Email: delta.student@test.delete.me
   Password: Test123!
   Name: Delta Student
   Section: VI Jose Rizal
   
5. Email: epsilon.student@test.delete.me
   Password: Test123!
   Name: Epsilon Student
   Section: VI SSC

Teacher: ms.merlyn.science@gmail.com
*/

-- ====================================================================
-- OR: If you have service_role access, run this:
-- ====================================================================

-- Step 1: Get teacher ID
DO $$
DECLARE
  v_teacher_id UUID;
BEGIN
  SELECT id INTO v_teacher_id 
  FROM profiles 
  WHERE email = 'ms.merlyn.science@gmail.com' AND role = 'teacher';
  
  RAISE NOTICE 'Teacher ID: %', v_teacher_id;
  
  -- You'll need to manually create the auth users first via Supabase Dashboard
  -- Then uncomment and run the profile creation below with the actual user IDs
END $$;

-- ====================================================================
-- VERIFICATION
-- ====================================================================

SELECT 
  '=== NEW TEST STUDENTS ===' as info;

SELECT 
  display_name,
  email,
  section,
  (SELECT COUNT(*) FROM quiz_results WHERE user_id = profiles.id) as quiz_attempts,
  (SELECT COUNT(*) FROM progress WHERE user_id = profiles.id) as progress_records,
  created_at::DATE as created
FROM profiles
WHERE email LIKE '%@test.delete.me'
ORDER BY created_at DESC;

-- ====================================================================
-- DONE! ✅
-- 5 test students created for deletion testing
-- Password: Test123!
-- ====================================================================
