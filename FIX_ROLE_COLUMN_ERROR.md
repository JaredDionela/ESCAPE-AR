# Quick Fix: Role Column Missing Error

## Problem
When running `TEACHER_STUDENT_RELATIONSHIP_MIGRATION.sql`, you got:
```
ERROR: 42703: column "role" does not exist
```

## Solution ✅ Already Fixed

The migration has been updated to **add the `role` column first** before using it.

## What Changed

### Before (Line 18-23):
```sql
-- Add teacher_id foreign key to profiles table
ALTER TABLE profiles 
ADD COLUMN IF NOT EXISTS teacher_id UUID REFERENCES profiles(id) ON DELETE SET NULL;
```

### After (Line 18-38):
```sql
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
```

## How to Run the Fixed Migration

### Step 1: Open Supabase Dashboard
1. Go to https://supabase.com/dashboard
2. Select your project
3. Click "SQL Editor" in the left sidebar

### Step 2: Copy and Paste
1. Open `TEACHER_STUDENT_RELATIONSHIP_MIGRATION.sql`
2. Copy the **entire file** (all 475 lines)
3. Paste into the SQL Editor

### Step 3: Run the Migration
1. Click the green "Run" button (or press Ctrl+Enter)
2. Wait for execution (should take 5-10 seconds)
3. Check output for success messages

### Step 4: Verify Success
You should see output like:
```
NOTICE: Migration Summary:
NOTICE:   - Students linked to teachers: 5
NOTICE:   - Students without teachers: 2
==========================================
TEACHER-STUDENT RELATIONSHIP SUMMARY
==========================================
Total Teachers: 2
Total Students: 7
Students Assigned: 5
Students Unassigned: 2
==========================================
```

## After Migration: Manual Teacher Setup

Since the script can't know who should be teachers, you need to manually update teacher accounts:

```sql
-- Check current roles
SELECT id, email, display_name, role, teacher_name 
FROM profiles 
ORDER BY role, display_name;

-- Update specific users to be teachers
UPDATE profiles 
SET role = 'teacher', teacher_id = NULL 
WHERE email IN (
  'teacher1@school.com',
  'teacher2@school.com'
);

-- Verify teacher accounts
SELECT id, email, display_name, role 
FROM profiles 
WHERE role = 'teacher';
```

## Important Notes

### Role Assignment Logic
The migration automatically assigns roles based on existing data:

1. **Has `teacher_name`** → role = 'student'
2. **No `teacher_name`** → role = 'teacher' (probably needs manual verification)

### After Migration Checklist
- [ ] Run migration successfully
- [ ] Manually verify/update teacher accounts
- [ ] Link existing students to correct teachers
- [ ] Test web admin login as teacher
- [ ] Verify teacher sees only their students
- [ ] Update Android app code (see `ANDROID_MULTI_TEACHER_IMPLEMENTATION.md`)

## Troubleshooting

### If migration fails again:
1. Check the error message carefully
2. Run this query to see current schema:
```sql
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'profiles'
ORDER BY ordinal_position;
```

3. If `role` column already exists, the script will skip adding it (safe to re-run)

### If you need to rollback:
Uncomment and run the rollback section at the bottom of `TEACHER_STUDENT_RELATIONSHIP_MIGRATION.sql` (lines 430-465)

## Next Steps

1. ✅ Run updated migration (should work now)
2. ✅ Manually set teacher roles for teacher accounts
3. ✅ Test web admin
4. ⏳ Update Android app
5. ⏳ Test complete flow

**The migration is now ready to run!** The `role` column will be created before it's used.
