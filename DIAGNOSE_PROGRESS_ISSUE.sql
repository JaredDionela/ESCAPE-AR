-- ====================================================================
-- DIAGNOSE PROGRESS TRACKING ISSUE
-- ====================================================================
-- This script helps diagnose why progress table is empty

-- Step 1: Check if progress table exists and is accessible
SELECT 
    'Progress Table Exists' as check_name,
    table_name,
    table_type
FROM information_schema.tables
WHERE table_name = 'progress';

-- Step 2: Count total rows in progress table
SELECT 
    'Total Progress Rows' as check_name,
    COUNT(*) as total_rows
FROM progress;

-- Step 3: Check if there are any students (profiles) in the system
SELECT 
    'Total Students' as check_name,
    COUNT(*) as total_students,
    COUNT(CASE WHEN role = 'student' THEN 1 END) as students_only
FROM profiles;

-- Step 4: Check recent students
SELECT 
    'Recent Students' as check_name,
    id,
    display_name,
    email,
    role,
    created_at
FROM profiles
WHERE role = 'student'
ORDER BY created_at DESC
LIMIT 5;

-- Step 5: Check if quiz_results table has any data
SELECT 
    'Quiz Results Count' as check_name,
    COUNT(*) as total_results,
    COUNT(CASE WHEN score_percentage IS NOT NULL THEN 1 END) as summary_results,
    COUNT(CASE WHEN question_id IS NOT NULL THEN 1 END) as question_results
FROM quiz_results;

-- Step 6: Check recent quiz_results
SELECT 
    'Recent Quiz Results' as check_name,
    qr.id,
    qr.user_id,
    pr.display_name,
    qr.module_id,
    qr.score_percentage,
    qr.created_at
FROM quiz_results qr
LEFT JOIN profiles pr ON qr.user_id = pr.id
WHERE qr.score_percentage IS NOT NULL
ORDER BY qr.created_at DESC
LIMIT 10;

-- Step 7: Check the module_code enum type values
SELECT 
    'Module Code Enum Values' as check_name,
    enumlabel as allowed_module_code
FROM pg_enum
WHERE enumtypid = (
    SELECT oid 
    FROM pg_type 
    WHERE typname = 'module_code'
)
ORDER BY enumsortorder;

-- Step 8: Check progress table constraints
SELECT 
    'Progress Table Constraints' as check_name,
    conname as constraint_name,
    contype as constraint_type,
    pg_get_constraintdef(oid) as definition
FROM pg_constraint
WHERE conrelid = 'progress'::regclass;

-- Step 9: Check if RLS is blocking inserts (check policies)
SELECT 
    'Progress RLS Status' as check_name,
    schemaname,
    tablename,
    rowsecurity as rls_enabled
FROM pg_tables
WHERE tablename = 'progress';

-- Step 10: Try to manually insert a test record (will show if there are permission issues)
-- Note: This will fail if module_code enum doesn't have the right values
-- Uncomment the lines below to test:
-- INSERT INTO progress (user_id, module, best_score, completed)
-- SELECT 
--     id,
--     'decantation'::module_code,
--     75.0,
--     true
-- FROM profiles
-- WHERE role = 'student'
-- LIMIT 1
-- RETURNING *;
