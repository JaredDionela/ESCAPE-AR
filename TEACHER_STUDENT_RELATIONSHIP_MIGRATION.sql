-- =============================================
-- TEACHER-STUDENT RELATIONSHIP IMPLEMENTATION
-- Migration Script for Multi-Teacher Support
-- Date: October 22, 2025
-- =============================================

-- PURPOSE:
-- This migration adds teacher-student relationships to enable:
-- 1. Multiple teachers managing their own students
-- 2. Data isolation between teachers
-- 3. Clear ownership and responsibility tracking
-- 4. Simplified student assignment during signup

-- =============================================
-- STEP 1: ADD ROLE AND TEACHER_ID COLUMNS
-- =============================================

-- Add role column if it doesn't exist (distinguishes teachers from students)
ALTER TABLE profiles 
ADD COLUMN IF NOT EXISTS role TEXT DEFAULT 'student' CHECK (role IN ('student', 'teacher'));

-- Set default role for existing users (assume students unless they have no teacher_name)
UPDATE profiles 
SET role = 'student' 
WHERE role IS NULL AND teacher_name IS NOT NULL;

-- Mark users without teacher_name as potential teachers (you can update manually)
UPDATE profiles 
SET role = 'teacher' 
WHERE role IS NULL AND (teacher_name IS NULL OR teacher_name = '');

-- Add teacher_id foreign key to profiles table
ALTER TABLE profiles 
ADD COLUMN IF NOT EXISTS teacher_id UUID REFERENCES profiles(id) ON DELETE SET NULL;

-- Add comments for documentation
COMMENT ON COLUMN profiles.role IS 'User role: student or teacher. Determines data access permissions.';
COMMENT ON COLUMN profiles.teacher_id IS 'Foreign key linking student to their teacher. NULL for teacher accounts and unassigned students.';

-- =============================================
-- STEP 2: ADD INDEXES FOR PERFORMANCE
-- =============================================

-- Index for fast teacher → students queries
CREATE INDEX IF NOT EXISTS idx_profiles_teacher_id 
ON profiles(teacher_id);

-- Index for role filtering
CREATE INDEX IF NOT EXISTS idx_profiles_role 
ON profiles(role);

-- Composite index for common queries
CREATE INDEX IF NOT EXISTS idx_profiles_teacher_role 
ON profiles(teacher_id, role) 
WHERE role = 'student';

-- =============================================
-- STEP 3: MIGRATE EXISTING DATA (OPTIONAL)
-- =============================================

-- Link existing students to teachers based on teacher_name field
-- This is safe to run multiple times (idempotent)
UPDATE profiles p1
SET teacher_id = (
  SELECT p2.id 
  FROM profiles p2 
  WHERE p2.role = 'teacher' 
  AND LOWER(p2.display_name) = LOWER(p1.teacher_name)
  LIMIT 1
)
WHERE p1.role = 'student' 
AND p1.teacher_name IS NOT NULL
AND p1.teacher_name != ''
AND p1.teacher_id IS NULL;

-- Log migration results
DO $$
DECLARE
  linked_count INTEGER;
  unlinked_count INTEGER;
BEGIN
  SELECT COUNT(*) INTO linked_count 
  FROM profiles 
  WHERE role = 'student' AND teacher_id IS NOT NULL;
  
  SELECT COUNT(*) INTO unlinked_count 
  FROM profiles 
  WHERE role = 'student' AND teacher_id IS NULL;
  
  RAISE NOTICE 'Migration Summary:';
  RAISE NOTICE '  - Students linked to teachers: %', linked_count;
  RAISE NOTICE '  - Students without teachers: %', unlinked_count;
END $$;

-- =============================================
-- STEP 4: UPDATE ROW LEVEL SECURITY (RLS) POLICIES
-- =============================================

-- Drop existing policies that might conflict
DROP POLICY IF EXISTS "Teachers can view their students" ON profiles;
DROP POLICY IF EXISTS "Teachers can update their students" ON profiles;
DROP POLICY IF EXISTS "Teachers can delete their students" ON profiles;
DROP POLICY IF EXISTS "Students can view their own profile" ON profiles;
DROP POLICY IF EXISTS "Users can view own profile" ON profiles;
DROP POLICY IF EXISTS "Teachers can view profiles" ON profiles;

-- Policy 1: Users can view their own profile
CREATE POLICY "Users can view own profile"
  ON profiles FOR SELECT
  USING (auth.uid() = id);

-- Policy 2: Teachers can view their assigned students
CREATE POLICY "Teachers can view their students"
  ON profiles FOR SELECT
  USING (
    -- Current user is a teacher
    EXISTS (
      SELECT 1 FROM profiles 
      WHERE id = auth.uid() AND role = 'teacher'
    )
    AND (
      -- And viewing their own students
      teacher_id = auth.uid()
      -- Or viewing other teachers (for collaboration)
      OR role = 'teacher'
    )
  );

-- Policy 3: Teachers can update their assigned students
CREATE POLICY "Teachers can update their students"
  ON profiles FOR UPDATE
  USING (
    EXISTS (
      SELECT 1 FROM profiles 
      WHERE id = auth.uid() AND role = 'teacher'
    )
    AND (
      teacher_id = auth.uid()  -- Only their students
      OR id = auth.uid()        -- Or their own profile
    )
  );

-- Policy 4: Teachers can delete their assigned students
CREATE POLICY "Teachers can delete their students"
  ON profiles FOR DELETE
  USING (
    EXISTS (
      SELECT 1 FROM profiles 
      WHERE id = auth.uid() AND role = 'teacher'
    )
    AND teacher_id = auth.uid()  -- Only their students
  );

-- Policy 5: Teachers can insert new students (register functionality)
DROP POLICY IF EXISTS "Teachers can create students" ON profiles;
CREATE POLICY "Teachers can create students"
  ON profiles FOR INSERT
  WITH CHECK (
    -- Must be a teacher
    EXISTS (
      SELECT 1 FROM profiles 
      WHERE id = auth.uid() AND role = 'teacher'
    )
    AND (
      -- Creating a student linked to themselves
      (role = 'student' AND teacher_id = auth.uid())
      -- Or creating another teacher (admin feature)
      OR role = 'teacher'
    )
  );

-- Policy 6: Students can view their teacher's profile
DROP POLICY IF EXISTS "Students can view their teacher" ON profiles;
CREATE POLICY "Students can view their teacher"
  ON profiles FOR SELECT
  USING (
    EXISTS (
      SELECT 1 FROM profiles 
      WHERE id = auth.uid() AND role = 'student' AND teacher_id = profiles.id
    )
  );

-- =============================================
-- STEP 5: UPDATE RELATED TABLES RLS
-- =============================================

-- Update quiz_results policies to respect teacher-student boundaries
DROP POLICY IF EXISTS "Teachers can view their students quiz results" ON quiz_results;
CREATE POLICY "Teachers can view their students quiz results"
  ON quiz_results FOR SELECT
  USING (
    user_id IN (
      SELECT id FROM profiles 
      WHERE teacher_id = auth.uid()
    )
  );

-- Update progress policies
DROP POLICY IF EXISTS "Teachers can view their students progress" ON progress;
CREATE POLICY "Teachers can view their students progress"
  ON progress FOR SELECT
  USING (
    user_id IN (
      SELECT id FROM profiles 
      WHERE teacher_id = auth.uid()
    )
  );

-- =============================================
-- STEP 6: CREATE HELPER VIEWS
-- =============================================

-- Drop existing view if it exists
DROP VIEW IF EXISTS teacher_students_view;

-- Create view for easy teacher-student queries with stats
CREATE VIEW teacher_students_view AS
SELECT 
  s.id as student_id,
  s.email as student_email,
  s.display_name as student_name,
  s.section as student_section,
  s.avatar_url as student_avatar,
  s.created_at as enrolled_at,
  t.id as teacher_id,
  t.email as teacher_email,
  t.display_name as teacher_name,
  -- Aggregate statistics
  COUNT(DISTINCT p.module) FILTER (WHERE p.completed = true) as completed_modules,
  ROUND(AVG(p.best_score) FILTER (WHERE p.completed = true), 2) as average_score,
  COUNT(DISTINCT qr.id) FILTER (WHERE qr.score_percentage IS NOT NULL) as total_quizzes_taken,
  ROUND(AVG(qr.score_percentage) FILTER (WHERE qr.score_percentage IS NOT NULL), 2) as average_quiz_score
FROM profiles s
JOIN profiles t ON s.teacher_id = t.id
LEFT JOIN progress p ON s.id = p.user_id
LEFT JOIN quiz_results qr ON s.id = qr.user_id AND qr.score_percentage IS NOT NULL
WHERE s.role = 'student' AND t.role = 'teacher'
GROUP BY s.id, s.email, s.display_name, s.section, s.avatar_url, s.created_at, 
         t.id, t.email, t.display_name;

-- Add comment to view
COMMENT ON VIEW teacher_students_view IS 'Convenient view showing teacher-student relationships with performance metrics';

-- =============================================
-- STEP 7: CREATE HELPER FUNCTIONS
-- =============================================

-- Function to get all students for a teacher
CREATE OR REPLACE FUNCTION get_teacher_students(teacher_uuid UUID)
RETURNS TABLE (
  student_id UUID,
  student_email TEXT,
  student_name TEXT,
  section TEXT,
  completed_modules BIGINT,
  average_score NUMERIC
) AS $$
BEGIN
  RETURN QUERY
  SELECT 
    p.id,
    p.email,
    p.display_name,
    p.section,
    COUNT(DISTINCT pr.module) FILTER (WHERE pr.completed = true),
    ROUND(AVG(pr.best_score) FILTER (WHERE pr.completed = true), 2)
  FROM profiles p
  LEFT JOIN progress pr ON p.id = pr.user_id
  WHERE p.teacher_id = teacher_uuid
  AND p.role = 'student'
  GROUP BY p.id, p.email, p.display_name, p.section
  ORDER BY p.display_name;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Function to get student count per teacher
CREATE OR REPLACE FUNCTION get_teacher_student_count(teacher_uuid UUID)
RETURNS INTEGER AS $$
DECLARE
  student_count INTEGER;
BEGIN
  SELECT COUNT(*) INTO student_count
  FROM profiles
  WHERE teacher_id = teacher_uuid
  AND role = 'student';
  
  RETURN student_count;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Function to reassign students to a different teacher
CREATE OR REPLACE FUNCTION reassign_students(
  old_teacher_uuid UUID,
  new_teacher_uuid UUID
)
RETURNS INTEGER AS $$
DECLARE
  affected_rows INTEGER;
BEGIN
  -- Verify new teacher exists and is a teacher
  IF NOT EXISTS (SELECT 1 FROM profiles WHERE id = new_teacher_uuid AND role = 'teacher') THEN
    RAISE EXCEPTION 'New teacher UUID does not exist or is not a teacher';
  END IF;
  
  -- Reassign students
  UPDATE profiles
  SET teacher_id = new_teacher_uuid,
      teacher_name = (SELECT display_name FROM profiles WHERE id = new_teacher_uuid)
  WHERE teacher_id = old_teacher_uuid
  AND role = 'student';
  
  GET DIAGNOSTICS affected_rows = ROW_COUNT;
  
  RETURN affected_rows;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- =============================================
-- STEP 8: CREATE TRIGGERS
-- =============================================

-- Trigger to auto-update teacher_name when teacher_id is set/changed
CREATE OR REPLACE FUNCTION sync_teacher_name()
RETURNS TRIGGER AS $$
BEGIN
  IF NEW.teacher_id IS NOT NULL THEN
    NEW.teacher_name := (
      SELECT display_name 
      FROM profiles 
      WHERE id = NEW.teacher_id
    );
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Drop trigger if exists
DROP TRIGGER IF EXISTS trigger_sync_teacher_name ON profiles;

-- Create trigger
CREATE TRIGGER trigger_sync_teacher_name
  BEFORE INSERT OR UPDATE OF teacher_id ON profiles
  FOR EACH ROW
  WHEN (NEW.role = 'student')
  EXECUTE FUNCTION sync_teacher_name();

-- =============================================
-- STEP 9: VERIFICATION QUERIES
-- =============================================

-- Summary report
DO $$
DECLARE
  total_teachers INTEGER;
  total_students INTEGER;
  assigned_students INTEGER;
  unassigned_students INTEGER;
BEGIN
  SELECT COUNT(*) INTO total_teachers FROM profiles WHERE role = 'teacher';
  SELECT COUNT(*) INTO total_students FROM profiles WHERE role = 'student';
  SELECT COUNT(*) INTO assigned_students FROM profiles WHERE role = 'student' AND teacher_id IS NOT NULL;
  SELECT COUNT(*) INTO unassigned_students FROM profiles WHERE role = 'student' AND teacher_id IS NULL;
  
  RAISE NOTICE '==========================================';
  RAISE NOTICE 'TEACHER-STUDENT RELATIONSHIP SUMMARY';
  RAISE NOTICE '==========================================';
  RAISE NOTICE 'Total Teachers: %', total_teachers;
  RAISE NOTICE 'Total Students: %', total_students;
  RAISE NOTICE 'Students Assigned: %', assigned_students;
  RAISE NOTICE 'Students Unassigned: %', unassigned_students;
  RAISE NOTICE '==========================================';
END $$;

-- Show teacher accounts
SELECT 
  '=== TEACHER ACCOUNTS ===' as info,
  id,
  email,
  display_name,
  (SELECT COUNT(*) FROM profiles p WHERE p.teacher_id = profiles.id) as student_count
FROM profiles 
WHERE role = 'teacher'
ORDER BY display_name;

-- Show students with their teachers
SELECT 
  '=== STUDENTS WITH TEACHERS ===' as info,
  s.email as student_email,
  s.display_name as student_name,
  s.section,
  t.display_name as teacher_name,
  t.email as teacher_email
FROM profiles s
LEFT JOIN profiles t ON s.teacher_id = t.id
WHERE s.role = 'student'
ORDER BY t.display_name, s.display_name
LIMIT 20;

-- Show students without teachers (need assignment)
SELECT 
  '=== STUDENTS WITHOUT TEACHERS (ACTION NEEDED) ===' as info,
  id,
  email,
  display_name,
  section,
  teacher_name as old_teacher_name_text
FROM profiles
WHERE role = 'student'
AND teacher_id IS NULL;

-- Show teacher load distribution
SELECT 
  '=== TEACHER LOAD DISTRIBUTION ===' as info,
  t.display_name as teacher_name,
  t.email as teacher_email,
  COUNT(s.id) as student_count,
  COUNT(DISTINCT s.section) as sections_taught
FROM profiles t
LEFT JOIN profiles s ON t.id = s.teacher_id
WHERE t.role = 'teacher'
GROUP BY t.id, t.display_name, t.email
ORDER BY student_count DESC;

-- =============================================
-- STEP 10: ROLLBACK SCRIPT (COMMENTED OUT)
-- =============================================

-- Uncomment this section if you need to rollback the migration

/*
-- Drop triggers
DROP TRIGGER IF EXISTS trigger_sync_teacher_name ON profiles;
DROP FUNCTION IF EXISTS sync_teacher_name();

-- Drop functions
DROP FUNCTION IF EXISTS get_teacher_students(UUID);
DROP FUNCTION IF EXISTS get_teacher_student_count(UUID);
DROP FUNCTION IF EXISTS reassign_students(UUID, UUID);

-- Drop views
DROP VIEW IF EXISTS teacher_students_view;

-- Drop policies
DROP POLICY IF EXISTS "Users can view own profile" ON profiles;
DROP POLICY IF EXISTS "Teachers can view their students" ON profiles;
DROP POLICY IF EXISTS "Teachers can update their students" ON profiles;
DROP POLICY IF EXISTS "Teachers can delete their students" ON profiles;
DROP POLICY IF EXISTS "Teachers can create students" ON profiles;
DROP POLICY IF EXISTS "Students can view their teacher" ON profiles;
DROP POLICY IF EXISTS "Teachers can view their students quiz results" ON quiz_results;
DROP POLICY IF EXISTS "Teachers can view their students progress" ON progress;

-- Drop indexes
DROP INDEX IF EXISTS idx_profiles_teacher_id;
DROP INDEX IF EXISTS idx_profiles_role;
DROP INDEX IF EXISTS idx_profiles_teacher_role;

-- Remove column
ALTER TABLE profiles DROP COLUMN IF EXISTS teacher_id;

RAISE NOTICE 'Migration rolled back successfully';
*/

-- =============================================
-- MIGRATION COMPLETE
-- =============================================

SELECT '✅ MIGRATION COMPLETED SUCCESSFULLY' as status;
SELECT 'Teacher-student relationships are now active.' as message;
SELECT 'Next: Update application code to use teacher_id field.' as next_step;
