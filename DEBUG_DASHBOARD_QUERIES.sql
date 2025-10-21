-- DEBUG QUERIES FOR DASHBOARD/ANALYTICS ISSUE
-- Run these in Supabase SQL Editor to see what data exists

-- 1. Check what's in the progress table
SELECT 
    'Progress Table Data' as check_type,
    user_id,
    module,
    best_score,
    completed,
    created_at
FROM progress
ORDER BY created_at DESC
LIMIT 10;

-- 2. Check what's in the quiz_results table
SELECT 
    'Quiz Results Data' as check_type,
    user_id,
    module_id,
    score_percentage,
    total_questions,
    correct_answers,
    created_at
FROM quiz_results
WHERE score_percentage IS NOT NULL
ORDER BY created_at DESC
LIMIT 10;

-- 3. Check profiles table
SELECT 
    'Profiles Data' as check_type,
    id,
    full_name,
    display_name,
    email,
    created_at
FROM profiles
LIMIT 10;

-- 4. Count completions by module
SELECT 
    'Completions by Module' as check_type,
    module,
    COUNT(*) as total_records,
    COUNT(*) FILTER (WHERE completed = true) as completed_count,
    COUNT(DISTINCT user_id) as unique_users,
    ROUND(AVG(best_score), 2) as avg_score
FROM progress
GROUP BY module
ORDER BY module;

-- 5. Check if profiles.full_name exists and has data
SELECT 
    'Profile Names Check' as check_type,
    COUNT(*) as total_profiles,
    COUNT(full_name) as profiles_with_full_name,
    COUNT(display_name) as profiles_with_display_name
FROM profiles;

-- 6. Test the join used in getDashboardMetrics
SELECT 
    'Top Performers Join Test' as check_type,
    p.user_id,
    p.best_score,
    p.completed,
    profiles.full_name,
    profiles.display_name
FROM progress p
INNER JOIN profiles ON p.user_id = profiles.id
WHERE p.completed = true
LIMIT 10;

-- 7. Recent quiz results with profile join
SELECT 
    'Recent Completions Join Test' as check_type,
    qr.user_id,
    qr.module_id,
    qr.score_percentage,
    qr.created_at,
    profiles.full_name,
    profiles.display_name
FROM quiz_results qr
INNER JOIN profiles ON qr.user_id = profiles.id
WHERE qr.score_percentage IS NOT NULL
ORDER BY qr.created_at DESC
LIMIT 5;

-- 8. Check module names in use
SELECT DISTINCT
    'Module Names in Progress' as check_type,
    module
FROM progress
WHERE module IS NOT NULL
ORDER BY module;

-- 9. Check total possible completions calculation
SELECT 
    'Completion Stats' as check_type,
    (SELECT COUNT(*) FROM profiles) as total_users,
    (SELECT COUNT(*) FROM progress WHERE completed = true) as total_completions,
    (SELECT COUNT(*) FROM profiles) * 4 as total_possible_completions,
    ROUND(
        (SELECT COUNT(*) FROM progress WHERE completed = true)::numeric / 
        ((SELECT COUNT(*) FROM profiles) * 4) * 100, 
        2
    ) as completion_percentage;

-- 10. Debug: Show ALL progress records
SELECT 
    'ALL Progress Records' as check_type,
    user_id,
    module,
    best_score,
    completed,
    attempts,
    created_at,
    updated_at
FROM progress
ORDER BY updated_at DESC;
