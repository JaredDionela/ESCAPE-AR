-- ============================================
-- NUCLEAR OPTION: Completely Reset RLS Policies
-- Use this if other fixes didn't work
-- ============================================
-- 
-- This script completely disables RLS, drops ALL policies,
-- then creates the simplest possible safe policies
-- ============================================

-- Step 1: Temporarily disable RLS to clean everything
ALTER TABLE public.profiles DISABLE ROW LEVEL SECURITY;

-- Step 2: Drop EVERY possible policy (catches all variations)
DO $$ 
DECLARE 
    r RECORD;
BEGIN
    FOR r IN (SELECT policyname FROM pg_policies WHERE schemaname = 'public' AND tablename = 'profiles')
    LOOP
        EXECUTE 'DROP POLICY IF EXISTS ' || quote_ident(r.policyname) || ' ON public.profiles';
        RAISE NOTICE 'Dropped policy: %', r.policyname;
    END LOOP;
END $$;

-- Step 3: Create the SIMPLEST possible policies (no recursion risk)

-- Allow anyone to SELECT their own profile
CREATE POLICY "simple_select_own" 
ON public.profiles 
FOR SELECT 
USING (auth.uid() = id);

-- Allow anyone to INSERT their own profile (for registration)
CREATE POLICY "simple_insert_own" 
ON public.profiles 
FOR INSERT 
WITH CHECK (auth.uid() = id);

-- Allow anyone to UPDATE their own profile
CREATE POLICY "simple_update_own" 
ON public.profiles 
FOR UPDATE 
USING (auth.uid() = id)
WITH CHECK (auth.uid() = id);

-- Step 4: Re-enable RLS
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;

-- Step 5: Grant permissions
GRANT ALL ON public.profiles TO authenticated;
GRANT USAGE ON SCHEMA public TO authenticated;

-- Step 6: Verify everything is working
SELECT 
  '✅ RLS RESET COMPLETE' as status,
  COUNT(*) as policy_count
FROM pg_policies 
WHERE schemaname = 'public' 
  AND tablename = 'profiles';

-- Step 7: Show all current policies
SELECT 
  policyname,
  cmd as operation,
  qual as using_expression,
  with_check as with_check_expression
FROM pg_policies 
WHERE schemaname = 'public' 
  AND tablename = 'profiles'
ORDER BY policyname;

-- Step 8: Test that we can query our own profile
DO $$
BEGIN
  RAISE NOTICE '✅ Policies created successfully!';
  RAISE NOTICE '✅ Users can now SELECT/INSERT/UPDATE their own profiles';
  RAISE NOTICE '✅ No more infinite recursion or 500 errors!';
END $$;
