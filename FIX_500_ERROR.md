# 🚨 URGENT: Fix 500 Internal Server Error on Login

## 🎯 THE PROBLEM

You're getting:
```
GET https://...supabase.co/rest/v1/profiles?select=role&id=eq.588b6c62... 500 (Internal Server Error)
```

**This means:**
1. ✅ Registration worked! (User created with ID: 588b6c62-baed-45cd-8b99-7fc16fb82fe9)
2. ❌ Login failed because the SELECT query on profiles returns 500 error
3. ❌ RLS policies are still broken in your database

**Root Cause:** The RLS policies in your Supabase database haven't been updated yet, or they're still causing issues.

## ✅ THE FIX (Run This SQL!)

### Option 1: Nuclear Fix (Recommended - Most Reliable)

**Run this in Supabase SQL Editor:**
```sql
-- Copy entire NUCLEAR_FIX_RLS.sql file
```

This script:
- Disables RLS temporarily
- Drops ALL existing policies (including broken ones)
- Creates simple, guaranteed-safe policies
- Re-enables RLS
- Shows you what policies exist

### Option 2: If Nuclear Fix Doesn't Work

Run this simpler version directly in Supabase SQL Editor:

```sql
-- 1. Disable RLS temporarily
ALTER TABLE public.profiles DISABLE ROW LEVEL SECURITY;

-- 2. Drop all policies manually
DROP POLICY IF EXISTS "simple_select_own" ON public.profiles;
DROP POLICY IF EXISTS "simple_insert_own" ON public.profiles;
DROP POLICY IF EXISTS "simple_update_own" ON public.profiles;
DROP POLICY IF EXISTS "profiles_select_own" ON public.profiles;
DROP POLICY IF EXISTS "profiles_insert_own" ON public.profiles;
DROP POLICY IF EXISTS "profiles_update_own" ON public.profiles;
DROP POLICY IF EXISTS "profiles_delete_own" ON public.profiles;
DROP POLICY IF EXISTS "profiles_teachers_read_students" ON public.profiles;
DROP POLICY IF EXISTS "Profiles Select Own" ON public.profiles;
DROP POLICY IF EXISTS "Profiles Insert Self" ON public.profiles;

-- 3. Create simple policies (NO recursion)
CREATE POLICY "simple_select_own" 
ON public.profiles FOR SELECT 
USING (auth.uid() = id);

CREATE POLICY "simple_insert_own" 
ON public.profiles FOR INSERT 
WITH CHECK (auth.uid() = id);

CREATE POLICY "simple_update_own" 
ON public.profiles FOR UPDATE 
USING (auth.uid() = id)
WITH CHECK (auth.uid() = id);

-- 4. Re-enable RLS
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;

-- 5. Grant permissions
GRANT ALL ON public.profiles TO authenticated;
```

## 🎬 STEP-BY-STEP

1. **Open Supabase Dashboard** → SQL Editor
2. **Copy the SQL above** (or entire `NUCLEAR_FIX_RLS.sql` file)
3. **Paste and Run**
4. **Should see:** "RLS RESET COMPLETE" and policy count
5. **Try logging in again**
6. ✅ Should work now!

## 🔍 WHY IS THIS HAPPENING?

The 500 error means the database is having an internal error when trying to execute the SELECT query. This happens when:

1. **RLS policies are recursive** (querying profiles from within profiles policy)
2. **RLS policies are malformed** (syntax errors)
3. **RLS policies conflict** (multiple policies with contradictory rules)

The nuclear fix completely wipes everything and starts fresh with the simplest possible policies.

## 🧪 VERIFY THE FIX

After running the SQL, check your policies:

```sql
-- Run this in Supabase SQL Editor
SELECT 
  policyname,
  cmd,
  qual as using_clause,
  with_check
FROM pg_policies 
WHERE tablename = 'profiles';
```

**You should see:**
- `simple_select_own` - FOR SELECT - USING: (auth.uid() = id)
- `simple_insert_own` - FOR INSERT - CHECK: (auth.uid() = id)
- `simple_update_own` - FOR UPDATE - USING/CHECK: (auth.uid() = id)

**You should NOT see:**
- Any policy with `SELECT ... FROM profiles` in it
- Any policy with recursive queries
- More than 3-4 policies total

## 🎯 TEST AGAIN

After running the fix:

1. Go to `/login` in your web admin
2. Enter the credentials you just registered with:
   - Email: (the one you used)
   - Password: (the one you used)
3. Click **Sign In**
4. ✅ Should work without 500 error!
5. ✅ Should see admin panel!

## 📞 STILL GETTING 500?

If you still get a 500 error after running the SQL:

### Check 1: Verify policies exist
```sql
SELECT COUNT(*) FROM pg_policies WHERE tablename = 'profiles';
```
Should return at least 3.

### Check 2: Verify RLS is enabled
```sql
SELECT relrowsecurity FROM pg_class WHERE relname = 'profiles';
```
Should return `true`.

### Check 3: Test the query directly
```sql
-- Replace with your user ID
SELECT role FROM profiles WHERE id = '588b6c62-baed-45cd-8b99-7fc16fb82fe9';
```
Should return `teacher`.

### Check 4: Look at Supabase logs
In Supabase Dashboard → Logs → check for error details.

## 💡 WHAT IF I NEED TO START OVER?

If nothing works, you can completely reset the profiles table:

```sql
-- WARNING: This deletes all data!
DROP TABLE IF EXISTS public.profiles CASCADE;

-- Then run fresh_database_setup.sql to recreate everything
```

But try the nuclear RLS fix first before doing this!

## 🎉 EXPECTED OUTCOME

After fixing RLS policies:
- ✅ Registration works (already working!)
- ✅ Login works (will work after this fix!)
- ✅ Admin panel access (will work after this fix!)
- ✅ No 500 errors
- ✅ No infinite recursion
- ✅ No manual SQL intervention needed

**Run the nuclear fix now and try logging in again!** 🚀
