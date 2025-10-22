# 🚨 CRITICAL: Fix "Infinite Recursion" Error

## 🎯 THE PROBLEM

You're getting:
```
infinite recursion detected in policy for relation "profiles"
```

**Root Cause:** RLS policies that query the `profiles` table FROM WITHIN policies protecting the `profiles` table cause infinite recursion.

**The bad policy:**
```sql
-- ❌ THIS CAUSES INFINITE RECURSION!
CREATE POLICY "profiles_teachers_read_students" 
ON public.profiles FOR SELECT
USING (
  EXISTS (
    SELECT 1 FROM public.profiles teacher  -- ❌ Querying profiles FROM profiles policy!
    WHERE teacher.id = auth.uid() 
    AND teacher.role = 'teacher'
  )
);
```

When you try to INSERT a new profile, the policy runs, which queries profiles, which triggers the policy again, which queries profiles... infinite loop! 💥

## ✅ THE SOLUTION

**Run this SQL file in Supabase SQL Editor:**
```
FIX_RLS_NO_RECURSION.sql
```

This script:
1. ✅ Drops ALL existing policies (including the recursive one)
2. ✅ Creates SAFE policies that ONLY use `auth.uid()` - NO subqueries
3. ✅ Re-enables RLS without recursion risk
4. ✅ Verifies all policies are safe

## 🎬 STEP-BY-STEP FIX

### Step 1: Open Supabase Dashboard
1. Go to [Supabase Dashboard](https://supabase.com/dashboard)
2. Select your project
3. Go to **SQL Editor**

### Step 2: Run the Fix
1. Open `FIX_RLS_NO_RECURSION.sql` in VS Code
2. Copy **ENTIRE file** (all lines)
3. Paste into Supabase SQL Editor
4. Click **Run** (or press F5)
5. ✅ Should see: "All RLS policies updated successfully!"

### Step 3: Run the Trigger Fix
1. Open `FIX_AUTO_CREATE_PROFILE_WITH_ROLE.sql` in VS Code
2. Copy **ENTIRE file**
3. Paste into Supabase SQL Editor
4. Click **Run**
5. ✅ Should see: "Trigger created successfully!"

### Step 4: Test Teacher Registration
1. Go to your web admin: `/register-teacher`
2. Fill in:
   - Email: `test.teacher.new@example.com`
   - Password: `Test123!`
   - Display Name: `Test Teacher`
3. Click **Register**
4. ✅ Should succeed without any errors!
5. Try to login with those credentials
6. ✅ Should access admin panel immediately

## 📋 WHAT CHANGED

### BEFORE (Recursive - Broken):
```sql
-- ❌ BAD: Queries profiles table in a profiles policy
CREATE POLICY "profiles_teachers_read_students" 
ON public.profiles FOR SELECT
USING (
  EXISTS (
    SELECT 1 FROM public.profiles teacher  -- ❌ RECURSION!
    WHERE teacher.id = auth.uid() 
    AND teacher.role = 'teacher'
  )
);
```

### AFTER (Non-Recursive - Safe):
```sql
-- ✅ GOOD: Only uses auth.uid(), no subquery, no recursion
CREATE POLICY "profiles_select_own" 
ON public.profiles FOR SELECT 
TO authenticated
USING (auth.uid() = id);  -- ✅ SAFE!

CREATE POLICY "profiles_insert_own" 
ON public.profiles FOR INSERT 
TO authenticated
WITH CHECK (auth.uid() = id);  -- ✅ SAFE!
```

## 🔍 WHY THE OLD FIX DIDN'T WORK

The previous `QUICK_FIX_PROFILES_RLS.sql` removed some recursive policies but **ADDED A NEW RECURSIVE ONE**:
```sql
-- This was in the "fix" but it's ALSO recursive!
CREATE POLICY "profiles_teachers_read_students" 
ON public.profiles FOR SELECT
USING (
  EXISTS (
    SELECT 1 FROM public.profiles teacher  -- ❌ Still recursive!
    ...
```

The new fix (`FIX_RLS_NO_RECURSION.sql`) **completely removes ALL recursive policies** and uses ONLY `auth.uid()` checks.

## 🎯 WHAT ABOUT TEACHER-STUDENT ACCESS?

**Q: Won't teachers need to see student profiles for analytics?**

**A: Yes, but we handle this differently:**

### Option 1: Service Role (Backend Only)
Teachers accessing student data through your backend API can use the service role key (bypasses RLS).

### Option 2: Add Non-Recursive Teacher Check Later
After basic registration works, you can add teacher access using JWT claims:
```sql
-- ✅ SAFE: Uses JWT claims, not a subquery
CREATE POLICY "teachers_see_all_profiles" 
ON public.profiles FOR SELECT
TO authenticated
USING (
  (auth.jwt() ->> 'role')::text = 'teacher'  -- ✅ No recursion!
  OR auth.uid() = id
);
```

But we can add this AFTER we fix the registration issue!

## 💡 KEY RULES TO AVOID RECURSION

**✅ SAFE (No Recursion):**
- `auth.uid() = id`
- `auth.jwt() ->> 'role' = 'teacher'`
- `auth.email() = email`
- Direct column comparisons

**❌ DANGEROUS (Causes Recursion):**
- `EXISTS (SELECT ... FROM profiles ...)` in a profiles policy
- `WHERE id IN (SELECT ... FROM profiles ...)` in a profiles policy
- Any subquery that references the same table

**General Rule:** 
> Never query a table FROM WITHIN a policy protecting that same table!

## 🧪 VERIFY THE FIX

After running the SQL scripts, check that policies are safe:

```sql
-- Run this in Supabase SQL Editor to verify
SELECT 
  tablename,
  policyname,
  cmd,
  qual as using_clause,
  with_check
FROM pg_policies 
WHERE schemaname = 'public' 
  AND tablename = 'profiles';
```

**Expected result:**
- `profiles_select_own` - USING: `(auth.uid() = id)` ✅
- `profiles_insert_own` - WITH CHECK: `(auth.uid() = id)` ✅
- `profiles_update_own` - USING/CHECK: `(auth.uid() = id)` ✅
- `profiles_delete_own` - USING: `(auth.uid() = id)` ✅

None should have `SELECT ... FROM profiles` in them!

## 📞 TROUBLESHOOTING

### Still getting "infinite recursion"?
Run this to see ALL policies:
```sql
SELECT * FROM pg_policies WHERE tablename = 'profiles';
```

If you see ANY policy with `FROM profiles` in the `qual` or `with_check` columns, that's the culprit!

### Getting "permission denied"?
Run this:
```sql
GRANT SELECT, INSERT, UPDATE, DELETE ON public.profiles TO authenticated;
GRANT USAGE ON SCHEMA public TO authenticated;
```

### Trigger still not working?
Make sure you ran BOTH scripts:
1. `FIX_RLS_NO_RECURSION.sql` - Fixes policies
2. `FIX_AUTO_CREATE_PROFILE_WITH_ROLE.sql` - Fixes trigger

## 🎉 SUMMARY

**Order matters! Run these in sequence:**

1. ✅ **FIX_RLS_NO_RECURSION.sql** - Removes recursive policies
2. ✅ **FIX_AUTO_CREATE_PROFILE_WITH_ROLE.sql** - Updates trigger to handle role
3. ✅ **Test registration** - Should work without errors!

After this, teachers can self-register without:
- ❌ Infinite recursion errors
- ❌ Database errors
- ❌ Access denied errors
- ❌ Manual SQL intervention

**Fully automated and scalable!** 🚀
