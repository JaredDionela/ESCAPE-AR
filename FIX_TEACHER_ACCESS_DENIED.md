# 🔧 FIX: "Access denied. Only teachers can access the admin panel."

## Problem
Teacher account was created but can't login with error: **"Access denied. Only teachers can access the admin panel."**

This happens because:
1. ✅ Auth user was created (you can login)
2. ❌ Profile wasn't created OR role wasn't set to 'teacher'

---

## ✅ **SOLUTION: Run SQL in Supabase**

### **Step 1: Check if Profile Exists**

Go to Supabase SQL Editor and run:

```sql
-- Check if profile exists and what role it has
SELECT id, email, display_name, role, created_at
FROM public.profiles
WHERE email = 'ms.merlyn.science@gmail.com';
```

### **Step 2a: If Profile Exists But Role is Wrong**

If you see a profile but `role` is `NULL` or not `'teacher'`, run:

```sql
-- Update the role to teacher
UPDATE public.profiles
SET 
  role = 'teacher',
  updated_at = NOW()
WHERE email = 'ms.merlyn.science@gmail.com';

-- Verify it worked
SELECT id, email, display_name, role 
FROM public.profiles
WHERE email = 'ms.merlyn.science@gmail.com';
```

### **Step 2b: If Profile Doesn't Exist At All**

If no profile was returned, create it:

```sql
-- Create the profile with teacher role
INSERT INTO public.profiles (id, email, display_name, role, teacher_id, created_at, updated_at)
SELECT 
  id,
  email,
  'Ms. Merlyn',
  'teacher',
  NULL,
  NOW(),
  NOW()
FROM auth.users
WHERE email = 'ms.merlyn.science@gmail.com'
ON CONFLICT (id) DO UPDATE 
SET role = 'teacher',
    display_name = 'Ms. Merlyn',
    updated_at = NOW();

-- Verify it worked
SELECT id, email, display_name, role 
FROM public.profiles
WHERE email = 'ms.merlyn.science@gmail.com';
```

### **Step 3: Try Login Again**

1. Go back to login page
2. Enter: `ms.merlyn.science@gmail.com`
3. Enter password
4. Click "Sign In"
5. ✅ Should work now!

---

## 🔍 **Why This Happened**

The RLS (Row Level Security) infinite recursion we fixed earlier prevented the profile from being created during registration. The auth user was created, but the profile insert failed silently.

Now that RLS is fixed, future registrations will work properly. This is a one-time fix for accounts created before the RLS fix.

---

## 📋 **Complete Fix Script**

Here's an all-in-one script that checks and fixes everything:

```sql
-- ============================================
-- COMPLETE FIX: Set Teacher Role
-- ============================================

-- 1. Check current state
SELECT 
  'Current Profile:' as info,
  p.id, 
  p.email, 
  p.display_name, 
  p.role,
  u.email_confirmed_at
FROM public.profiles p
LEFT JOIN auth.users u ON u.id = p.id
WHERE p.email = 'ms.merlyn.science@gmail.com'
UNION ALL
SELECT 
  'Auth User:' as info,
  u.id,
  u.email,
  NULL as display_name,
  NULL as role,
  u.email_confirmed_at
FROM auth.users u
WHERE u.email = 'ms.merlyn.science@gmail.com'
AND NOT EXISTS (
  SELECT 1 FROM public.profiles p WHERE p.id = u.id
);

-- 2. Create or update profile with teacher role
INSERT INTO public.profiles (id, email, display_name, role, teacher_id, created_at, updated_at)
SELECT 
  id,
  email,
  'Ms. Merlyn' as display_name,
  'teacher' as role,
  NULL as teacher_id,
  NOW() as created_at,
  NOW() as updated_at
FROM auth.users
WHERE email = 'ms.merlyn.science@gmail.com'
ON CONFLICT (id) DO UPDATE 
SET 
  role = 'teacher',
  display_name = COALESCE(profiles.display_name, 'Ms. Merlyn'),
  updated_at = NOW();

-- 3. Verify final state (should show role = 'teacher')
SELECT 
  p.id, 
  p.email, 
  p.display_name, 
  p.role,
  u.email_confirmed_at,
  CASE 
    WHEN p.role = 'teacher' THEN '✅ Ready to login!'
    ELSE '❌ Still needs fixing'
  END as status
FROM public.profiles p
INNER JOIN auth.users u ON u.id = p.id
WHERE p.email = 'ms.merlyn.science@gmail.com';
```

---

## 🧪 **Test Login**

After running the SQL:

1. **Refresh the login page** (Ctrl+F5)
2. **Enter credentials:**
   - Email: `ms.merlyn.science@gmail.com`
   - Password: Your password
3. **Click "Sign In"**
4. ✅ **Should redirect to Dashboard!**

---

## 🛡️ **Preventing This in the Future**

The RLS policies are now fixed (`QUICK_FIX_PROFILES_RLS.sql`), so:
- ✅ New teacher registrations will work correctly
- ✅ Profile will be created with proper role
- ✅ No more "Access denied" errors for valid teachers

---

## ❓ **If Still Not Working**

### Check Email Verification:
Some setups require email verification first.

```sql
-- Check if email is confirmed
SELECT id, email, email_confirmed_at, created_at
FROM auth.users
WHERE email = 'ms.merlyn.science@gmail.com';
```

If `email_confirmed_at` is NULL:
1. Check your email for verification link
2. Or manually confirm in Supabase:

```sql
UPDATE auth.users
SET email_confirmed_at = NOW()
WHERE email = 'ms.merlyn.science@gmail.com';
```

### Clear Browser Cache:
```
Ctrl + Shift + Delete → Clear cache
```

### Check Browser Console:
```
F12 → Console tab
Look for errors
```

---

## ✅ **Expected Result**

After running the fix:

```sql
SELECT id, email, display_name, role 
FROM public.profiles
WHERE email = 'ms.merlyn.science@gmail.com';
```

Should return:
```
id: [some-uuid]
email: ms.merlyn.science@gmail.com
display_name: Ms. Merlyn
role: teacher  ← Must be 'teacher'!
```

Then login will work! 🎉

---

**Status:** Run the SQL fix above, then try logging in again!
