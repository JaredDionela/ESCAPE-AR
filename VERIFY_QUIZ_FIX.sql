-- ====================================================================
-- QUICK VERIFICATION: Check if quiz_results table is fixed
-- Run this AFTER running FIX_QUIZ_SCORE_UPDATE.sql
-- ====================================================================

-- Check 1: Verify all required columns exist
SELECT 
    CASE 
        WHEN COUNT(*) = 10 THEN '✅ All columns exist!'
        ELSE '❌ Missing columns! Expected 10, found ' || COUNT(*)::text
    END AS column_check,
    COUNT(*) as total_columns
FROM information_schema.columns
WHERE table_name = 'quiz_results';

-- Check 2: List all columns with their properties
SELECT 
    column_name,
    data_type,
    CASE WHEN is_nullable = 'YES' THEN '✅ Nullable' ELSE '❌ NOT NULL' END as nullable_status
FROM information_schema.columns
WHERE table_name = 'quiz_results'
ORDER BY ordinal_position;

-- Check 3: Verify the critical new columns
SELECT 
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'quiz_results' AND column_name = 'score_percentage'
        ) THEN '✅ score_percentage exists'
        ELSE '❌ score_percentage MISSING'
    END AS score_percentage_check,
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'quiz_results' AND column_name = 'total_questions'
        ) THEN '✅ total_questions exists'
        ELSE '❌ total_questions MISSING'
    END AS total_questions_check,
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'quiz_results' AND column_name = 'correct_answers'
        ) THEN '✅ correct_answers exists'
        ELSE '❌ correct_answers MISSING'
    END AS correct_answers_check;

-- Check 4: Verify nullable columns
SELECT 
    CASE 
        WHEN (SELECT is_nullable FROM information_schema.columns 
              WHERE table_name = 'quiz_results' AND column_name = 'question_id') = 'YES'
        THEN '✅ question_id is nullable'
        ELSE '❌ question_id should be nullable'
    END AS question_id_check,
    CASE 
        WHEN (SELECT is_nullable FROM information_schema.columns 
              WHERE table_name = 'quiz_results' AND column_name = 'selected_answer') = 'YES'
        THEN '✅ selected_answer is nullable'
        ELSE '❌ selected_answer should be nullable'
    END AS selected_answer_check;

-- Check 5: Show recent quiz results (if any)
SELECT 
    '=== Recent Quiz Results ===' as section,
    COUNT(*) as total_results,
    COUNT(*) FILTER (WHERE score_percentage IS NOT NULL) as with_percentage,
    COUNT(*) FILTER (WHERE total_questions IS NOT NULL) as with_total,
    COUNT(*) FILTER (WHERE correct_answers IS NOT NULL) as with_correct
FROM quiz_results;

-- Check 6: Show sample data (most recent 3 results)
SELECT 
    module_id,
    score_percentage,
    total_questions,
    correct_answers,
    CASE 
        WHEN score_percentage IS NOT NULL THEN '✅'
        ELSE '❌'
    END as has_score,
    created_at
FROM quiz_results
ORDER BY created_at DESC
LIMIT 3;

-- ====================================================================
-- INTERPRETATION GUIDE:
-- ====================================================================
-- ✅ All checks show green checkmarks = Database is ready!
-- ❌ Any red X marks = Run FIX_QUIZ_SCORE_UPDATE.sql again
-- 
-- Expected results:
-- - Total columns: 10
-- - question_id: NULLABLE ✅
-- - selected_answer: NULLABLE ✅
-- - score_percentage: EXISTS ✅
-- - total_questions: EXISTS ✅
-- - correct_answers: EXISTS ✅
-- ====================================================================
