-- Quiz Display Diagnostic Script
-- Run this in Supabase SQL Editor to diagnose quiz issues

-- 1. Check if quiz_questions table exists and has data
SELECT 
    COUNT(*) as total_questions,
    COUNT(DISTINCT module_id) as unique_modules
FROM quiz_questions;

-- 2. Show all module IDs and question counts
SELECT 
    module_id,
    COUNT(*) as question_count,
    MIN(order_index) as min_order,
    MAX(order_index) as max_order
FROM quiz_questions
GROUP BY module_id
ORDER BY module_id;

-- 3. Show sample questions from each module
SELECT 
    module_id,
    id,
    question_text,
    correct_answer,
    order_index,
    created_at
FROM quiz_questions
ORDER BY module_id, order_index
LIMIT 20;

-- 4. Check for module ID mismatches
-- Expected module IDs: 'decantation', 'organ_system', 'simple_machines'
SELECT 
    module_id,
    CASE 
        WHEN module_id IN ('decantation', 'organ_system', 'simple_machines') 
        THEN '✅ CORRECT'
        ELSE '❌ INCORRECT - Update this module_id!'
    END as status,
    COUNT(*) as question_count
FROM quiz_questions
GROUP BY module_id;

-- 5. Check RLS policies on quiz_questions table
SELECT 
    schemaname,
    tablename,
    policyname,
    permissive,
    roles,
    cmd,
    qual
FROM pg_policies
WHERE tablename = 'quiz_questions';

-- 6. Test if anon role can read quiz_questions
-- This simulates what the Android app does
SET ROLE anon;
SELECT COUNT(*) as accessible_questions FROM quiz_questions;
RESET ROLE;

-- 7. Find questions without required fields
SELECT 
    id,
    module_id,
    question_text,
    CASE WHEN option_a IS NULL OR option_a = '' THEN '❌' ELSE '✅' END as has_option_a,
    CASE WHEN option_b IS NULL OR option_b = '' THEN '❌' ELSE '✅' END as has_option_b,
    CASE WHEN option_c IS NULL OR option_c = '' THEN '❌' ELSE '✅' END as has_option_c,
    CASE WHEN option_d IS NULL OR option_d = '' THEN '❌' ELSE '✅' END as has_option_d,
    correct_answer
FROM quiz_questions
WHERE 
    option_a IS NULL OR option_a = '' OR
    option_b IS NULL OR option_b = '' OR
    option_c IS NULL OR option_c = '' OR
    option_d IS NULL OR option_d = '' OR
    correct_answer NOT IN ('A', 'B', 'C', 'D')
ORDER BY module_id, order_index;

-- 8. Show quiz results to verify submissions are working
SELECT 
    qr.module_id,
    COUNT(*) as total_submissions,
    SUM(CASE WHEN qr.is_correct THEN 1 ELSE 0 END) as correct_answers,
    ROUND(AVG(CASE WHEN qr.is_correct THEN 1.0 ELSE 0.0 END) * 100, 2) as accuracy_percentage
FROM quiz_results qr
GROUP BY qr.module_id
ORDER BY qr.module_id;

-- Quick Fix: Update module IDs if needed
-- UNCOMMENT AND MODIFY THESE LINES IF YOUR MODULE IDs ARE WRONG:

-- UPDATE quiz_questions SET module_id = 'decantation' WHERE module_id = 'Module1';
-- UPDATE quiz_questions SET module_id = 'organ_system' WHERE module_id = 'Module2';
-- UPDATE quiz_questions SET module_id = 'simple_machines' WHERE module_id = 'Module3';

-- Verify the update:
-- SELECT module_id, COUNT(*) FROM quiz_questions GROUP BY module_id;
