# 🚨 URGENT FIX - TWO ISSUES

## Problem 1: "Bucket not found" ❌
## Problem 2: "No quiz questions" ❌

Both need to be fixed in Supabase Dashboard!

---

## 🎯 SOLUTION - Do These 3 Things

### ✅ STEP 1: Run SQL Fix (5 minutes)

1. **Open Supabase SQL Editor:**
   https://app.supabase.com/project/iixfznklvvydfqouwzuh/sql/new

2. **Open file:** `FIX_EVERYTHING.sql` (I just created it)

3. **Copy ALL 200+ lines** from that file

4. **Paste** into Supabase SQL Editor

5. **Click "Run"** (green button)

6. **Wait for success** - you should see:
   - ✅ Quiz Questions Created
   - ✅ Anonymous User Can Read
   - ✅ Active Policies

---

### ✅ STEP 2: Create Storage Bucket (2 minutes)

1. **Open Supabase Storage:**
   https://app.supabase.com/project/iixfznklvvydfqouwzuh/storage/buckets

2. **Click "New bucket"** button

3. **Fill in:**
   - **Name:** `lesson-files` (EXACTLY this name!)
   - **Public bucket:** ✅ Check this box (make it public)
   - **File size limit:** Leave default or set to 50MB
   - **Allowed MIME types:** Leave empty (allow all)

4. **Click "Create bucket"**

5. **Go to bucket policies:**
   - Click on the `lesson-files` bucket
   - Click "Policies" tab
   - Click "New policy"
   - Click "For full customization" 
   - **Policy name:** `Allow public uploads`
   - **Policy definition:** Choose "All operations" OR add these:

```sql
-- Allow public to read files
CREATE POLICY "Public Access"
ON storage.objects FOR SELECT
USING (bucket_id = 'lesson-files');

-- Allow anyone to upload files
CREATE POLICY "Allow uploads"
ON storage.objects FOR INSERT
WITH CHECK (bucket_id = 'lesson-files');

-- Allow anyone to delete files (for admin)
CREATE POLICY "Allow deletes"
ON storage.objects FOR DELETE
USING (bucket_id = 'lesson-files');
```

6. **Click "Save"**

---

### ✅ STEP 3: Clear App and Test (2 minutes)

```powershell
# Clear app cache
adb shell pm clear com.example.escape_ar

# Reinstall app
cd "c:\Users\Jared Dionela\AndroidStudioProjects\ESCAPEAR"
.\gradlew installDebug
```

**Now test:**
1. Open admin panel: http://localhost:3004
2. Try uploading a file - should work! ✅
3. Open Android app
4. Click Quiz - questions should appear! ✅

---

## 🎯 Quick Storage Bucket Setup (Alternative)

If you prefer SQL for storage bucket policies, run this in SQL Editor:

```sql
-- Create storage policies for lesson-files bucket
-- (Bucket must exist first!)

insert into storage.buckets (id, name, public)
values ('lesson-files', 'lesson-files', true)
on conflict (id) do nothing;

-- Allow public read
CREATE POLICY "Public can read lesson files"
ON storage.objects FOR SELECT
USING (bucket_id = 'lesson-files');

-- Allow public insert
CREATE POLICY "Public can upload lesson files"  
ON storage.objects FOR INSERT
WITH CHECK (bucket_id = 'lesson-files');

-- Allow public delete
CREATE POLICY "Public can delete lesson files"
ON storage.objects FOR DELETE
USING (bucket_id = 'lesson-files');

-- Allow public update
CREATE POLICY "Public can update lesson files"
ON storage.objects FOR UPDATE
USING (bucket_id = 'lesson-files');
```

---

## 📋 Complete Checklist

### Database (SQL):
- [ ] Opened Supabase SQL Editor
- [ ] Ran `FIX_EVERYTHING.sql`
- [ ] Saw success messages
- [ ] Verified quiz questions created

### Storage:
- [ ] Opened Supabase Storage
- [ ] Created `lesson-files` bucket
- [ ] Made bucket public
- [ ] Added storage policies
- [ ] Bucket shows in storage list

### App:
- [ ] Cleared app cache
- [ ] Reinstalled app
- [ ] Tested file upload in admin panel - works!
- [ ] Tested quiz in app - works!

---

## 🆘 If Still Having Issues

### For "Bucket not found":
1. Check bucket exists: https://app.supabase.com/project/iixfznklvvydfqouwzuh/storage/buckets
2. Verify name is exactly: `lesson-files` (no spaces, lowercase)
3. Check bucket is public
4. Verify storage policies exist

### For "No quiz questions":
1. Verify SQL ran successfully
2. Check questions exist:
   ```sql
   SELECT module_id, COUNT(*) FROM quiz_questions GROUP BY module_id;
   ```
3. Should show 5 questions per module
4. Check app logs:
   ```powershell
   adb logcat -d | Select-String "QuizRepository" | Select-Object -Last 20
   ```

---

## 🎉 Expected Results

**After completing all steps:**

✅ Admin panel file uploads work
✅ Quiz questions appear in app  
✅ Can answer quiz questions
✅ Can submit quiz and see results
✅ Files can be downloaded in app

---

**DO NOW:**
1. Run `FIX_EVERYTHING.sql` in Supabase
2. Create `lesson-files` storage bucket
3. Clear app and test!
