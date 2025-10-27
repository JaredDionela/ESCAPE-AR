-- ====================================================================
-- Set Sections for Test Students
-- Assigns VI SSC and VI Jose Rizal sections to test students
-- ====================================================================

DO $$
DECLARE
  v_student_record RECORD;
  v_section TEXT;
  v_counter INTEGER := 0;
BEGIN
  RAISE NOTICE '🏫 Setting sections for test students...';
  
  -- Loop through all test students
  FOR v_student_record IN 
    SELECT id, display_name, email
    FROM profiles 
    WHERE email LIKE '%@student.escapear.edu' 
      AND role = 'student'
    ORDER BY id
  LOOP
    v_counter := v_counter + 1;
    
    -- Alternate between sections (odd numbers = VI SSC, even numbers = VI Jose Rizal)
    IF v_counter % 2 = 1 THEN
      v_section := 'VI SSC';
    ELSE
      v_section := 'VI Jose Rizal';
    END IF;
    
    -- Update the student's section
    UPDATE profiles
    SET section = v_section
    WHERE id = v_student_record.id;
    
    RAISE NOTICE 'Updated: % → %', v_student_record.display_name, v_section;
  END LOOP;
  
  RAISE NOTICE '✅ Section assignment complete!';
  RAISE NOTICE 'Total students updated: %', v_counter;
END $$;

-- ====================================================================
-- VERIFICATION
-- ====================================================================

SELECT 
  '=== SECTION DISTRIBUTION ===' as info;

SELECT 
  section,
  COUNT(*) as student_count
FROM profiles
WHERE email LIKE '%@student.escapear.edu' 
  AND role = 'student'
GROUP BY section
ORDER BY section;

-- Show sample students with sections
SELECT 
  '=== SAMPLE STUDENTS WITH SECTIONS ===' as info;

SELECT 
  display_name,
  email,
  section
FROM profiles
WHERE email LIKE '%@student.escapear.edu' 
  AND role = 'student'
ORDER BY section, display_name
LIMIT 20;

-- ====================================================================
-- DONE! ✅
-- All test students now have sections assigned
-- ====================================================================
