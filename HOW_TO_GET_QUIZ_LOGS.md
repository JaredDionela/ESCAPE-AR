# GET DETAILED QUIZ ERROR LOGS

## Instructions:

1. **Clear logs first:**
   ```powershell
   adb logcat -c
   ```

2. **Open the app and click on a quiz module** (Decantation, Organ System, or Simple Machines)

3. **Get the detailed logs:**
   ```powershell
   adb logcat -d | Select-String "QuizRepository" | Out-File -FilePath quiz_logs.txt
   notepad quiz_logs.txt
   ```

4. **Look for these key indicators:**

   ### ✅ Success Pattern:
   ```
   QuizRepository: Fetching quiz questions for module: decantation
   QuizRepository: Successfully fetched 5 questions for module: decantation
   QuizRepository: ✅ Question IDs: [...]
   ```

   ### ❌ Empty Result Pattern:
   ```
   QuizRepository: Successfully fetched 0 questions for module: decantation
   QuizRepository: ⚠️ NO QUESTIONS FOUND!
   ```
   **Meaning**: Questions weren't inserted OR RLS policies blocking access

   ### ❌ Error Pattern:
   ```
   QuizRepository: ❌ ERROR fetching quiz questions
   QuizRepository: ❌ Error message: [some error]
   ```
   **Meaning**: Database connection issue or query syntax error

5. **Share the output here so I can help diagnose!**

---

## Meanwhile, Check Database Status

Run this in Supabase SQL Editor:

```sql
-- Quick check: Do questions exist?
SELECT COUNT(*) as total FROM quiz_questions;

-- Check by module
SELECT module_id, COUNT(*) 
FROM quiz_questions 
GROUP BY module_id;

-- Test anonymous access (what app uses)
SET ROLE anon;
SELECT COUNT(*) FROM quiz_questions;
RESET ROLE;
```

**Expected Results:**
- Total: 15 (if SQL ran successfully)
- decantation: 5, organ_system: 5, simple_machines: 5
- Anonymous access: 15

**If you get 0**, it means either:
1. The SQL didn't run successfully
2. RLS policies are still blocking access
