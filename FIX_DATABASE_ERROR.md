# 🚨 URGENT: Fix "Database error saving new user"

## 🎯 THE PROBLEM

You're getting **"Database error saving new user"** because the trigger is trying to insert into a column that might not exist, OR the trigger is still using the old code that doesn't handle the `role` field properly.

## ✅ SOLUTION - RUN THESE IN ORDER

### Step 1: Check Current Database State (DIAGNOSTIC)

**Run this in Supabase SQL Editor:**
```sql
-- Copy/paste entire CHECK_CURRENT_TRIGGER.sql file
```

This will show you:
- ✅ Does the trigger exist?
- ✅ Does profiles have `role` and `display_name` columns?
- ✅ What's the current trigger code?

### Step 2: Fix the Trigger (THE FIX)

**Run this in Supabase SQL Editor:**
```sql
-- Copy/paste entire FIX_AUTO_CREATE_PROFILE_WITH_ROLE.sql file
```

This will:
- Drop the old broken trigger
- Create NEW trigger that properly handles role/display_name
- Handle both old schema (if columns missing) and new schema (if columns exist)
- Show success message when done

### Step 3: Test Teacher Registration

1. Go to `/register-teacher` in your web admin
2. Fill in:
   - Email: `newteacher@test.com`
   - Password: `Test123!`
   - Display Name: `Test Teacher`
3. Click **Register**
4. ✅ Should succeed without "Database error"
5. Try to login
6. ✅ Should access admin panel (not "Access Denied")

## 🔍 WHAT IF IT STILL FAILS?

### Error: "column 'role' does not exist"
**This means you need to run the migration first:**

```sql
-- Add missing columns to profiles table
ALTER TABLE profiles 
ADD COLUMN IF NOT EXISTS role TEXT DEFAULT 'student' CHECK (role IN ('student', 'teacher'));

ALTER TABLE profiles 
ADD COLUMN IF NOT EXISTS display_name TEXT;

ALTER TABLE profiles 
ADD COLUMN IF NOT EXISTS teacher_id UUID REFERENCES profiles(id) ON DELETE SET NULL;

-- Now run FIX_AUTO_CREATE_PROFILE_WITH_ROLE.sql again
```

### Error: "relation 'profiles' does not exist"
**This means you need to run `fresh_database_setup.sql` first to create tables.**

### Error: "infinite recursion detected"
**This means RLS policies are wrong. Run:**
```sql
-- Copy/paste QUICK_FIX_PROFILES_RLS.sql
```

## 📋 QUICK REFERENCE

**Files to run in order:**

1. **CHECK_CURRENT_TRIGGER.sql** - Diagnostic (see what's wrong)
2. **FIX_AUTO_CREATE_PROFILE_WITH_ROLE.sql** - Fix the trigger
3. Test registration - Should work now!

## 🎯 EXPECTED BEHAVIOR AFTER FIX

### BEFORE (Broken):
```
User signs up → Trigger creates profile with NO role → 
RegisterTeacher.tsx tries manual insert → Fails → 
"Database error saving new user" ❌
```

### AFTER (Fixed):
```
User signs up → Trigger extracts role from metadata → 
Profile created WITH role='teacher' → 
Login works → Admin panel access ✅
```

## 💡 WHY THE ERROR HAPPENED

The trigger function `handle_new_user()` was trying to INSERT into columns that either:
1. Don't exist yet (need migration), OR
2. Exist but trigger code is outdated (doesn't extract role from metadata)

The new trigger has a **fallback mechanism**:
- Tries to insert with role/display_name first
- If columns don't exist, falls back to basic insert (id, email, full_name)
- This makes it work regardless of schema state

## 📞 STILL STUCK?

Run the diagnostic script and share the output:
```sql
-- Run CHECK_CURRENT_TRIGGER.sql
-- Copy output here
```

Then we can see exactly what's in your database and fix accordingly.
