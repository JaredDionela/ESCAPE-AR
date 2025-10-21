-- =====================================================
-- CRITICAL: Test Anonymous Access
-- =====================================================
-- This tests if the app can actually read quiz questions
-- The app uses the 'anon' role (anonymous/unauthenticated)
-- =====================================================

-- Test 1: Can anon role read quiz_questions?
SET ROLE anon;
SELECT 
    'Test 1: Anonymous read access' as test,
    COUNT(*) as accessible_questions
FROM quiz_questions;
RESET ROLE;

-- Test 2: Can anon role filter by module?
SET ROLE anon;
SELECT 
    'Test 2: Filter by decantation' as test,
    COUNT(*) as decantation_questions
FROM quiz_questions
WHERE module_id = 'decantation';
RESET ROLE;

-- Test 3: What policies exist?
SELECT 
    'Test 3: Current policies' as test,
    policyname,
    cmd as operation,
    qual as using_expression,
    with_check
FROM pg_policies 
WHERE tablename = 'quiz_questions'
ORDER BY policyname;

-- Test 4: Is RLS enabled?
SELECT 
    'Test 4: RLS status' as test,
    tablename,
    rowsecurity as rls_enabled
FROM pg_tables
WHERE schemaname = 'public' 
AND tablename = 'quiz_questions';

-- =====================================================
-- EXPECTED RESULTS
-- =====================================================
-- Test 1: Should return 38 (or your total question count)
-- Test 2: Should return count of decantation questions (probably 5-8)
-- Test 3: Should show policies like 'quiz_read_all'
-- Test 4: Should show rls_enabled = true
--
-- IF Test 1 or Test 2 returns 0:
--   Problem: RLS policies blocking anonymous access
--   Fix: Run COMPLETE_FIX_ALL_ISSUES.sql again
--
-- IF Test 1 and Test 2 return numbers > 0:
--   Problem: App is not connecting properly or has different issue
--   Fix: Check app logs with diagnose-simple.ps1
-- =====================================================
