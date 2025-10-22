# 🔧 FIX: Android App Profile & Progress Issues

## 🎯 **PROBLEMS IDENTIFIED:**

1. ❌ **Profile screen doesn't show teacher name** after signup
2. ❌ **Section field doesn't display** 
3. ❌ **Edit profile button doesn't work**
4. ❌ **App uses OLD schema** (`teacher_name` text field)
5. ❌ **Database uses NEW schema** (`teacher_id` UUID foreign key)

## 🗄️ **SCHEMA MISMATCH:**

### **OLD Schema (What App Expects):**
```sql
CREATE TABLE profiles (
  id UUID,
  email TEXT,
  display_name TEXT,
  teacher_name TEXT,  -- ❌ Text field with teacher's name
  section TEXT
);
```

### **NEW Schema (What Database Has):**
```sql
CREATE TABLE profiles (
  id UUID,
  email TEXT,
  display_name TEXT,
  role TEXT,          -- ✅ 'student' or 'teacher'
  teacher_id UUID,    -- ✅ Foreign key to profiles(id)
  teacher_name TEXT,  -- ⚠️ Deprecated (might still exist)
  section TEXT
);
```

## ✅ **SOLUTION:**

Update Android app to use the new schema:

### **1. Signup Flow:**
- **Before:** Stores teacher's name as text in `teacher_name`
- **After:** Stores teacher's ID in `teacher_id`

### **2. Profile Display:**
- **Before:** Shows `teacher_name` directly from student's profile
- **After:** JOIN with teacher's profile to get `display_name`

### **3. Edit Profile:**
- **Before:** Updates `teacher_name` text field
- **After:** Updates `teacher_id` foreign key

---

## 🔨 **FILES TO UPDATE:**

### **1. SupabaseModels.kt**
Update `Profile` and `ExtendedProfile` models to use `teacher_id`

### **2. UserRepository.kt**
- Update `registerUser()` to save `teacher_id`
- Update `getExtendedProfile()` to JOIN and fetch teacher's name
- Update `updateProfile()` to update `teacher_id`

### **3. AuthScreen.kt**
- Update signup to pass `teacher_id` instead of `teacher_name`

### **4. ProfileScreen.kt**
- Update edit dialog to select teacher by ID (not name)

---

## 📋 **DATABASE PREPARATION:**

Before updating the app, ensure database has correct schema:

```sql
-- Verify columns exist
SELECT column_name, data_type 
FROM information_schema.columns
WHERE table_name = 'profiles' 
  AND column_name IN ('teacher_id', 'role', 'display_name', 'section');

-- Should show:
-- teacher_id | uuid
-- role | text  
-- display_name | text
-- section | text
```

If `teacher_id` doesn't exist, run migration first:
```sql
-- See TEACHER_STUDENT_RELATIONSHIP_MIGRATION.sql
ALTER TABLE profiles 
ADD COLUMN IF NOT EXISTS teacher_id UUID REFERENCES profiles(id) ON DELETE SET NULL;
```

---

## 🧪 **TESTING PLAN:**

### **Test 1: New Student Signup**
1. Register new student in app
2. Select teacher from dropdown
3. Complete signup
4. ✅ Student's `teacher_id` should be set in database
5. ✅ Profile screen should show teacher's display_name

### **Test 2: Existing Student Migration**
If you have students with `teacher_name` but no `teacher_id`:
```sql
-- Migration script to convert teacher_name to teacher_id
UPDATE profiles p1
SET teacher_id = (
  SELECT p2.id 
  FROM profiles p2 
  WHERE LOWER(p2.display_name) = LOWER(p1.teacher_name)
    AND p2.role = 'teacher'
  LIMIT 1
)
WHERE p1.role = 'student' 
  AND p1.teacher_name IS NOT NULL
  AND p1.teacher_id IS NULL;
```

### **Test 3: Profile Display**
1. Open profile screen
2. ✅ Should show: Student Name
3. ✅ Should show: Teacher's Name (from JOIN)
4. ✅ Should show: Section
5. ✅ Should show: Module progress

### **Test 4: Edit Profile**
1. Click "Edit Profile"
2. Change teacher selection
3. Save
4. ✅ `teacher_id` updated in database
5. ✅ Teacher name updates in UI

---

## 🎯 **IMPLEMENTATION CHECKLIST:**

- [ ] Update `SupabaseModels.kt` - Add `teacherId` field
- [ ] Update `UserRepository.kt` - Use `teacher_id` in all queries
- [ ] Update `AuthScreen.kt` - Pass `teacher_id` during signup
- [ ] Update `ProfileScreen.kt` - Display teacher name from JOIN
- [ ] Update edit profile dialog - Select teacher by ID
- [ ] Test new signup flow
- [ ] Test profile display
- [ ] Test edit profile
- [ ] Test migration for existing users

---

## 🚀 **BENEFITS OF NEW SCHEMA:**

1. **Data Integrity:** Foreign key ensures teacher exists
2. **Scalability:** Can have multiple teachers with same name
3. **Analytics:** Easy to query students by teacher ID
4. **Updates:** If teacher changes name, students auto-update
5. **Deletion:** Can cascade or set NULL when teacher deleted

---

**Next Step:** Update Android app code to use new schema! 🎉
