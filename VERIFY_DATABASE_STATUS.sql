-- ============================================
-- DATABASE VERIFICATION QUERIES
-- Run these in Supabase SQL Editor to diagnose the issue
-- ============================================

-- 1. Check if quiz_results table has the new columns
SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'quiz_results'
ORDER BY ordinal_position;

-- 2. Check if any quiz results exist and what data they contain
SELECT 
    id,
    user_id,
    module,
    score_percentage,
    total_questions,
    correct_answers,
    created_at
FROM quiz_results
ORDER BY created_at DESC
LIMIT 10;

-- 3. Check progress table structure
SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'progress'
ORDER BY ordinal_position;

-- 4. Check if any progress records exist
SELECT 
    user_id,
    module,
    best_score,
    completed,
    created_at,
    updated_at
FROM progress
ORDER BY updated_at DESC
LIMIT 10;

-- 5. Check if progress table has data for specific user (replace 'your-user-id' with actual ID)
-- SELECT 
--     module,
--     best_score,
--     completed,
--     updated_at
-- FROM progress
-- WHERE user_id = 'your-user-id'
-- ORDER BY module;

-- 6. Count total records in each table
SELECT 
    'quiz_results' as table_name, 
    COUNT(*) as record_count 
FROM quiz_results
UNION ALL
SELECT 
    'progress' as table_name, 
    COUNT(*) as record_count 
FROM progress;
