-- ====================================================================
-- PROGRESS TRACKING VERIFICATION SCRIPT
-- ====================================================================
-- This script verifies that quiz progress is being tracked correctly
-- and that module names are consistent across the system.

-- Step 1: Check the progress table structure
SELECT 
    'Progress Table Structure' as check_name,
    column_name,
    data_type,
    is_nullable
FROM information_schema.columns
WHERE table_name = 'progress'
ORDER BY ordinal_position;

-- Step 2: Check current progress data
SELECT 
    'Current Progress Data' as check_name,
    p.user_id,
    pr.display_name as student_name,
    p.module,
    p.best_score,
    p.completed,
    p.created_at,
    p.updated_at
FROM progress p
LEFT JOIN profiles pr ON p.user_id = pr.id
ORDER BY p.user_id, p.module;

-- Step 3: Check quiz_results table for recent quiz completions
SELECT 
    'Recent Quiz Results' as check_name,
    qr.user_id,
    pr.display_name as student_name,
    qr.module_id,
    qr.score_percentage,
    qr.correct_answers,
    qr.total_questions,
    qr.created_at
FROM quiz_results qr
LEFT JOIN profiles pr ON qr.user_id = pr.id
WHERE qr.score_percentage IS NOT NULL  -- Only summary records
ORDER BY qr.created_at DESC
LIMIT 20;

-- Step 4: Verify module names in quiz_questions table
SELECT 
    'Module Names in Questions' as check_name,
    module_id,
    COUNT(*) as question_count
FROM quiz_questions
GROUP BY module_id
ORDER BY module_id;

-- Step 5: Check for orphaned progress records (users not in profiles)
SELECT 
    'Orphaned Progress Records' as check_name,
    p.*
FROM progress p
LEFT JOIN profiles pr ON p.user_id = pr.id
WHERE pr.id IS NULL;

-- Step 6: Module completion summary
SELECT 
    'Module Completion Summary' as check_name,
    module,
    COUNT(DISTINCT user_id) as total_students,
    COUNT(DISTINCT CASE WHEN completed = true THEN user_id END) as completed_students,
    ROUND(AVG(best_score), 2) as avg_score,
    MAX(best_score) as max_score
FROM progress
GROUP BY module
ORDER BY module;

-- Step 7: Check RLS policies on progress table
SELECT 
    'Progress Table RLS Policies' as check_name,
    schemaname,
    tablename,
    policyname,
    permissive,
    roles,
    cmd,
    qual,
    with_check
FROM pg_policies
WHERE tablename = 'progress';

-- Step 8: Test query that web analytics uses (Module Performance Overview)
SELECT 
    'Web Analytics Test Query' as check_name,
    module as module_id,
    COUNT(*) as total_attempts,
    COUNT(CASE WHEN completed = true THEN 1 END) as completed_count,
    ROUND(AVG(CASE WHEN completed = true THEN best_score END), 2) as avg_completed_score
FROM progress
GROUP BY module
ORDER BY module;

-- Step 9: Check data types to diagnose type mismatch issues
SELECT 
    'Progress Table Column Types' as check_name,
    column_name,
    data_type,
    udt_name
FROM information_schema.columns
WHERE table_name = 'progress'
ORDER BY ordinal_position;

SELECT 
    'Quiz Results Table Column Types' as check_name,
    column_name,
    data_type,
    udt_name
FROM information_schema.columns
WHERE table_name = 'quiz_results'
ORDER BY ordinal_position;

-- Step 10: Verify expected module IDs exist in progress table
-- Note: Casting both sides to text to handle module_code enum type
SELECT 
    'Expected vs Actual Modules' as check_name,
    expected.module_id as expected_module,
    CASE WHEN p.module IS NOT NULL THEN 'EXISTS' ELSE 'MISSING' END as status,
    COALESCE(COUNT(p.user_id), 0) as student_count
FROM (
    SELECT 'decantation' as module_id
    UNION ALL SELECT 'organ_system'
    UNION ALL SELECT 'simple_machines'
    UNION ALL SELECT 'solar_system'
) expected
LEFT JOIN progress p ON expected.module_id = p.module::text
GROUP BY expected.module_id, p.module
ORDER BY expected.module_id;
