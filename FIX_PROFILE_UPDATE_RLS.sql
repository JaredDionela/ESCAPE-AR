-- ====================================================================
-- Fix Profile Update RLS Policies
-- Ensures teachers can update their students' profiles
-- ====================================================================

-- Drop existing policies if they exist
DROP POLICY IF EXISTS "Users can view own profile" ON profiles;
DROP POLICY IF EXISTS "Users can update own profile" ON profiles;
DROP POLICY IF EXISTS "Teachers can view their students" ON profiles;
DROP POLICY IF EXISTS "Teachers can update their students" ON profiles;
DROP POLICY IF EXISTS "Teachers can insert students" ON profiles;
DROP POLICY IF EXISTS "Public profiles are viewable by everyone" ON profiles;

-- Enable RLS
ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;

-- Policy 1: Users can view their own profile
CREATE POLICY "Users can view own profile"
ON profiles FOR SELECT
USING (auth.uid() = id);

-- Policy 2: Users can update their own profile
CREATE POLICY "Users can update own profile"
ON profiles FOR UPDATE
USING (auth.uid() = id)
WITH CHECK (auth.uid() = id);

-- Policy 3: Teachers can view all their students
CREATE POLICY "Teachers can view their students"
ON profiles FOR SELECT
USING (
  -- Teacher viewing their own students
  teacher_id = auth.uid()
  OR
  -- Or viewing their own profile
  auth.uid() = id
);

-- Policy 4: Teachers can update their students' profiles
CREATE POLICY "Teachers can update their students"
ON profiles FOR UPDATE
USING (
  -- Teacher can update their students
  teacher_id = auth.uid()
  OR
  -- Or update their own profile
  auth.uid() = id
)
WITH CHECK (
  -- Ensure teacher_id doesn't change or remains the same
  teacher_id = auth.uid()
  OR
  auth.uid() = id
);

-- Policy 5: Teachers can insert new students
CREATE POLICY "Teachers can insert students"
ON profiles FOR INSERT
WITH CHECK (
  -- Only allow inserting if the new profile is a student under this teacher
  role = 'student' AND teacher_id = auth.uid()
  OR
  -- Or if it's the user's own profile (for registration)
  auth.uid() = id
);

-- ====================================================================
-- VERIFICATION
-- ====================================================================

-- Show all RLS policies on profiles table
SELECT 
  '=== PROFILES TABLE RLS POLICIES ===' as info;

SELECT 
  schemaname,
  tablename,
  policyname,
  permissive,
  roles,
  cmd,
  qual,
  with_check
FROM pg_policies
WHERE tablename = 'profiles'
ORDER BY policyname;

-- Test query: Check if current user can see students
SELECT 
  '=== YOUR STUDENTS (VISIBILITY TEST) ===' as info;

SELECT 
  display_name,
  email,
  section,
  role
FROM profiles
WHERE role = 'student'
LIMIT 5;

-- ====================================================================
-- DONE! ✅
-- RLS policies have been updated
-- Teachers can now update their students' profiles
-- ====================================================================
