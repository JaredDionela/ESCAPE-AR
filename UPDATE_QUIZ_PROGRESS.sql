-- ====================================================================
-- Update Quiz Progress for ALL Students
-- Ensures ALL students have attempted ALL 4 modules with varied results
-- ====================================================================

DO $$
DECLARE
  v_student_record RECORD;
  v_modules TEXT[] := ARRAY['decantation', 'organ_system', 'simple_machines', 'solar_system'];
  v_module TEXT;
  v_score INTEGER;
  v_completed BOOLEAN;
  v_attempts INTEGER;
  v_base_date TIMESTAMP;
  v_existing_progress RECORD;
  v_has_existing BOOLEAN;
  j INTEGER;
BEGIN
  RAISE NOTICE '🚀 Starting quiz progress update for ALL students...';
  
  -- Loop through ALL students
  FOR v_student_record IN 
    SELECT id, display_name, email
    FROM profiles 
    WHERE role = 'student'
    ORDER BY id
  LOOP
    RAISE NOTICE 'Processing student: % (ID: %)', v_student_record.display_name, v_student_record.id;
    
    -- Process each module
    FOREACH v_module IN ARRAY v_modules LOOP
      -- Check if student already has progress for this module
      SELECT * INTO v_existing_progress
      FROM progress
      WHERE user_id = v_student_record.id AND module = v_module::module_code;
      
      v_has_existing := FOUND;
      
      -- Skip if already has progress (don't overwrite existing data)
      IF v_has_existing THEN
        RAISE NOTICE '  ⏭️  Skipping module % (already has progress)', v_module;
        CONTINUE;
      END IF;
      
      -- Generate varied but realistic scores (between 35-100)
      -- Distribution: 10% low (35-50), 30% medium (51-69), 40% good (70-85), 20% excellent (86-100)
      CASE 
        WHEN random() < 0.10 THEN
          -- Low performers: 35-50%
          v_score := 35 + floor(random() * 16);
          v_completed := false;
          v_attempts := 3 + floor(random() * 3); -- 3-5 attempts
        WHEN random() < 0.40 THEN
          -- Below passing: 51-69%
          v_score := 51 + floor(random() * 19);
          v_completed := false;
          v_attempts := 2 + floor(random() * 3); -- 2-4 attempts
        WHEN random() < 0.80 THEN
          -- Good performers: 70-85%
          v_score := 70 + floor(random() * 16);
          v_completed := true;
          v_attempts := 1 + floor(random() * 3); -- 1-3 attempts
        ELSE
          -- Excellent performers: 86-100%
          v_score := 86 + floor(random() * 15);
          v_completed := true;
          v_attempts := 1 + floor(random() * 2); -- 1-2 attempts
      END CASE;
      
      -- Random base date (within last 30 days)
      v_base_date := NOW() - (random() * INTERVAL '30 days');
      
      -- Insert progress record
      INSERT INTO progress (
        user_id,
        module,
        best_score,
        completed,
        created_at,
        updated_at
      ) VALUES (
        v_student_record.id,
        v_module::module_code,
        v_score,
        v_completed,
        v_base_date,
        v_base_date + (v_attempts || ' days')::INTERVAL
      )
      ON CONFLICT (user_id, module) 
      DO UPDATE SET
        best_score = EXCLUDED.best_score,
        completed = EXCLUDED.completed,
        updated_at = EXCLUDED.updated_at;
      
      -- Create quiz_results entries for each attempt
      -- Earlier attempts have lower scores, building up to best score
      FOR j IN 1..v_attempts LOOP
        DECLARE
          v_attempt_score INTEGER;
          v_attempt_date TIMESTAMP;
        BEGIN
          -- Calculate attempt score (progressive improvement)
          IF j = v_attempts THEN
            -- Last attempt = best score
            v_attempt_score := v_score;
          ELSIF j = 1 THEN
            -- First attempt: 60-85% of final score
            v_attempt_score := GREATEST(35, floor(v_score * (0.60 + random() * 0.25)));
          ELSE
            -- Middle attempts: gradual improvement
            v_attempt_score := GREATEST(35, floor(v_score * (0.70 + random() * 0.20)));
          END IF;
          
          -- Space out attempts over time
          v_attempt_date := v_base_date + (j * INTERVAL '1 day') + (random() * INTERVAL '12 hours');
          
          INSERT INTO quiz_results (
            id,
            user_id,
            module_id,
            score_percentage,
            total_questions,
            correct_answers,
            is_correct,
            created_at
          ) VALUES (
            gen_random_uuid(),
            v_student_record.id,
            v_module,
            v_attempt_score,
            10, -- Standard 10 questions per quiz
            GREATEST(0, floor(10 * v_attempt_score / 100.0)), -- Calculate correct answers
            v_attempt_score >= 70, -- Mark as correct if score is 70% or higher
            v_attempt_date
          )
          ON CONFLICT (id) DO NOTHING;
        END;
      END LOOP;
      
      RAISE NOTICE '  ✓ Module: % | Score: % | Attempts: % | Completed: %', 
        v_module, v_score, v_attempts, v_completed;
    END LOOP;
    
    RAISE NOTICE '  ✅ Completed all modules for %', v_student_record.display_name;
    RAISE NOTICE '';
  END LOOP;
  
  RAISE NOTICE '🎉 Quiz progress update complete!';
END $$;

-- ====================================================================
-- VERIFICATION QUERIES
-- ====================================================================

-- Summary by module
SELECT 
  '=== MODULE SUMMARY (ALL STUDENTS) ===' as info;

SELECT 
  p.module::TEXT as module,
  COUNT(*) as total_students,
  COUNT(CASE WHEN p.completed THEN 1 END) as students_completed,
  COUNT(CASE WHEN NOT p.completed THEN 1 END) as students_incomplete,
  ROUND(AVG(p.best_score), 1) as avg_score,
  MIN(p.best_score) as min_score,
  MAX(p.best_score) as max_score,
  ROUND(AVG(qr.attempt_count), 1) as avg_attempts
FROM progress p
JOIN profiles pr ON p.user_id = pr.id
LEFT JOIN (
  SELECT user_id, module_id, COUNT(*) as attempt_count
  FROM quiz_results
  GROUP BY user_id, module_id
) qr ON p.user_id = qr.user_id AND p.module::TEXT = qr.module_id
WHERE pr.role = 'student'
GROUP BY p.module
ORDER BY p.module;

-- Top 10 performers (by average score across all modules)
SELECT 
  '=== TOP 10 PERFORMERS (ALL STUDENTS) ===' as info;

SELECT 
  pr.display_name,
  pr.section,
  COUNT(p.module) as modules_attempted,
  COUNT(CASE WHEN p.completed THEN 1 END) as modules_completed,
  ROUND(AVG(p.best_score), 1) as avg_score,
  SUM((SELECT COUNT(*) FROM quiz_results WHERE user_id = pr.id)) as total_attempts
FROM profiles pr
JOIN progress p ON pr.id = p.user_id
WHERE pr.role = 'student'
GROUP BY pr.id, pr.display_name, pr.section
ORDER BY avg_score DESC
LIMIT 10;

-- Bottom 10 performers (students who need help)
SELECT 
  '=== STUDENTS NEEDING SUPPORT (ALL STUDENTS) ===' as info;

SELECT 
  pr.display_name,
  pr.section,
  COUNT(p.module) as modules_attempted,
  COUNT(CASE WHEN p.completed THEN 1 END) as modules_completed,
  ROUND(AVG(p.best_score), 1) as avg_score,
  SUM((SELECT COUNT(*) FROM quiz_results WHERE user_id = pr.id)) as total_attempts
FROM profiles pr
JOIN progress p ON pr.id = p.user_id
WHERE pr.role = 'student'
GROUP BY pr.id, pr.display_name, pr.section
ORDER BY avg_score ASC
LIMIT 10;

-- Progress distribution (score ranges)
SELECT 
  '=== SCORE DISTRIBUTION (ALL STUDENTS) ===' as info;

SELECT 
  score_range,
  count,
  percentage
FROM (
  SELECT 
    CASE 
      WHEN best_score < 50 THEN '❌ Below 50%'
      WHEN best_score < 70 THEN '⚠️  50-69%'
      WHEN best_score < 85 THEN '✅ 70-84%'
      ELSE '🌟 85-100%'
    END as score_range,
    COUNT(*) as count,
    ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER (), 1) as percentage,
    CASE 
      WHEN best_score < 50 THEN 1
      WHEN best_score < 70 THEN 2
      WHEN best_score < 85 THEN 3
      ELSE 4
    END as sort_order
  FROM progress p
  JOIN profiles pr ON p.user_id = pr.id
  WHERE pr.role = 'student'
  GROUP BY 
    CASE 
      WHEN best_score < 50 THEN '❌ Below 50%'
      WHEN best_score < 70 THEN '⚠️  50-69%'
      WHEN best_score < 85 THEN '✅ 70-84%'
      ELSE '🌟 85-100%'
    END,
    CASE 
      WHEN best_score < 50 THEN 1
      WHEN best_score < 70 THEN 2
      WHEN best_score < 85 THEN 3
      ELSE 4
    END
) subquery
ORDER BY sort_order;

-- Sample individual student progress (first 20 entries)
SELECT 
  '=== SAMPLE STUDENT PROGRESS (ALL STUDENTS) ===' as info;

SELECT 
  pr.display_name,
  pr.section,
  p.module::TEXT as module,
  p.best_score,
  p.completed,
  (SELECT COUNT(*) FROM quiz_results WHERE user_id = pr.id AND module_id = p.module::TEXT) as attempts,
  p.updated_at::DATE as last_attempt
FROM profiles pr
JOIN progress p ON pr.id = p.user_id
WHERE pr.role = 'student'
ORDER BY pr.display_name, p.module
LIMIT 20;

-- Total quiz attempts across all students
SELECT 
  '=== TOTAL QUIZ ACTIVITY (ALL STUDENTS) ===' as info;

SELECT 
  COUNT(DISTINCT pr.id) as total_students,
  COUNT(p.id) as total_progress_records,
  COUNT(qr.id) as total_quiz_attempts,
  ROUND(AVG(attempt_count), 1) as avg_attempts_per_student
FROM profiles pr
LEFT JOIN progress p ON pr.id = p.user_id
LEFT JOIN quiz_results qr ON pr.id = qr.user_id
LEFT JOIN (
  SELECT user_id, COUNT(*) as attempt_count
  FROM quiz_results
  WHERE user_id IN (SELECT id FROM profiles WHERE role = 'student')
  GROUP BY user_id
) ac ON pr.id = ac.user_id
WHERE pr.role = 'student';

-- ====================================================================
-- DONE! ✅
-- All students now have complete quiz progress with varied results
-- Students who already had progress on certain modules were skipped
-- Only missing modules were filled in for existing students
-- ====================================================================
