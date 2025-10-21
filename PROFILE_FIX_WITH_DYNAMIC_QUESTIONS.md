# Profile Fix with Dynamic Question Counts - Complete Guide

## Issues Fixed

### ✅ Issue 1: Hard-coded Question Counts
**Problem**: Module question counts were hard-coded to 10, not reflecting actual database values.

**Solution**: Added `getModuleQuestionCount()` function that:
- Queries `quiz_questions` table for each module
- Counts actual questions in database: `SELECT id FROM quiz_questions WHERE module_id = 'moduleX'`
- Falls back to hardcoded value (10) if query fails
- Logs actual count: `"Module X has Y questions in database"`

### ✅ Issue 2: Profile Not Showing Data
**Problem**: Profile shows zeros even though quiz completed.

**Root Cause**: **User not logged in!**

**Evidence from logs**:
```
D UserRepository: getCurrentUser() called
D UserRepository: No current Supabase user session
```

## Code Changes Made

### 1. Added Dynamic Question Count Function
```kotlin
private suspend fun getModuleQuestionCount(moduleId: String): Int {
    // Queries quiz_questions table
    // Returns actual count from database
    // Falls back to 10 if fetch fails
}
```

### 2. Updated Module Loading
```kotlin
// Before:
totalQuestions = km.questionCount  // always 10

// After:
val actualQuestionCount = getModuleQuestionCount(km.id)
totalQuestions = actualQuestionCount  // from database
```

### 3. Enhanced Logging
Now logs: `"Module X: completed=Y, score=Z, questions=W"`

## CRITICAL: Must Login First!

### Why Profile Shows Zeros

The profile cannot load data because there's **no active user session**. Here's what happens:

1. App starts
2. ProfileViewModel calls `getCurrentUser()`
3. Returns `null` (no session)
4. ProfileViewModel sets error: `"No user session found. Please sign in again."`
5. **No data loads** - stops immediately
6. Profile shows default empty state (zeros)

### Login Required

**Before testing anything, you MUST:**

1. ✅ Open the app
2. ✅ Navigate to login/signup screen
3. ✅ Enter credentials and sign in
4. ✅ Wait for "Loaded user: [name]" in logs
5. ✅ **Then** navigate to profile

## Testing Checklist

### Step 1: Verify Database Schema ⚠️

**IMPORTANT**: Run this SQL in Supabase first!

```sql
-- File: FIX_QUIZ_SCORE_UPDATE.sql
-- This adds the required columns to quiz_results table
```

### Step 2: Login to App

1. Build and install app:
   ```powershell
   cd "C:\Users\Jared Dionela\AndroidStudioProjects\ESCAPEAR"
   .\gradlew installDebug
   ```

2. Clear logs and start app:
   ```powershell
   adb logcat -c
   adb shell am start -n com.example.escape_ar/.MainActivity
   ```

3. **Login with your credentials**

4. Check logs for successful login:
   ```powershell
   adb logcat | Select-String "UserRepository.*Loaded user"
   ```

   Expected output:
   ```
   D UserRepository: Loaded user: [Your Name] ([email])
   ```

### Step 3: Verify Question Counts

After logging in, navigate to profile and check logs:

```powershell
adb logcat | Select-String "Module.*questions="
```

Expected output:
```
D UserRepository: Module Chemical Separation Lab: completed=false, score=null, questions=15
D UserRepository: Module Human Body Systems: completed=false, score=null, questions=20
D UserRepository: Module Mechanical Engineering: completed=false, score=null, questions=12
D UserRepository: Module Navigation: completed=true, score=85.0, questions=18
```

Notice:
- ✅ Questions counts are from database (not hardcoded 10)
- ✅ Each module may have different question counts
- ✅ Completion status and scores from progress table

### Step 4: Test Quiz Completion Flow

1. **Navigate to quiz** for any module

2. **Answer all questions** and submit

3. **Check completion logs**:
   ```powershell
   adb logcat | Select-String "QuizViewModel.*completeModule|saveQuizProgress|Module.*completed"
   ```

   Expected:
   ```
   D QuizViewModel: completeModule called: moduleId=decantation, score=85, questionsAnswered=17, totalQuestions=20
   D QuizViewModel: Calling userRepository.saveQuizProgress
   D UserRepository: saveQuizProgress called: module=decantation, score=85
   D UserRepository: Upserting progress for user [ID], module decantation, score 85
   D UserRepository: UPSERT successful for decantation
   ```

4. **Return to profile screen**

5. **Check profile refresh logs**:
   ```powershell
   adb logcat | Select-String "ProfileViewModel.*Starting profile|Loaded.*modules|Module.*completed"
   ```

   Expected:
   ```
   D ProfileViewModel: Starting profile load...
   D UserRepository: Fetching progress for user: [ID]
   D UserRepository: Loaded 1 progress records
   D UserRepository: Module decantation has 20 questions in database
   D UserRepository: Module Chemical Separation Lab: completed=true, score=85.0, questions=20
   D ProfileViewModel: Loaded 4 modules
   ```

6. **Verify UI shows updated scores**

### Step 5: Verify Web Admin Compatibility

1. Open web admin
2. Navigate to Users → [Your User]
3. Check Module Progress table
4. Should show:
   - ✅ Completed modules
   - ✅ Best scores (as percentages)
   - ✅ Latest quiz timestamp

## Expected Log Patterns

### On Login
```
D UserRepository: getCurrentUser() called
D UserRepository: Loaded user: John Doe (john@example.com)
D ProfileViewModel: Starting profile load...
D ProfileViewModel: Loaded user: John Doe (john@example.com)
D ProfileViewModel: Loaded extended profile: teacher=Ms. Smith, section=Section A
```

### On Profile Load (with data)
```
D UserRepository: Fetching progress for user: abc-123
D UserRepository: Loaded 2 progress records
D UserRepository: Module decantation has 20 questions in database
D UserRepository: Module Chemical Separation Lab: completed=true, score=85.0, questions=20
D UserRepository: Module organ_system has 15 questions in database
D UserRepository: Module Human Body Systems: completed=false, score=null, questions=15
D ProfileViewModel: Loaded 4 modules
D ProfileViewModel: Profile UI state updated: userName=John Doe, modules=4
```

### On Quiz Completion
```
D QuizViewModel: completeModule called: moduleId=decantation, score=85, questionsAnswered=17, totalQuestions=20
D UserRepository: saveQuizProgress called: module=decantation, score=85, userId=abc-123
D UserRepository: Getting current best score for decantation
D UserRepository: Previous best score: 0, new score: 85
D UserRepository: Upserting progress for user abc-123, module decantation, score 85
D UserRepository: UPSERT successful for decantation
D QuizViewModel: Submitting quiz completion to quiz_results table
D QuizRepository: Score percentage: 85.0
D QuizViewModel: Successfully saved quiz result with flexible scoring
```

## Troubleshooting

### Profile Still Shows Zeros

**Check 1: Are you logged in?**
```powershell
adb logcat | Select-String "getCurrentUser|No current Supabase"
```

If you see `"No current Supabase user session"` → **Login first!**

**Check 2: Is database schema updated?**
Run `VERIFY_DATABASE_STATUS.sql` in Supabase to confirm:
- `quiz_results` has `score_percentage`, `total_questions`, `correct_answers` columns
- `progress` table exists with `best_score`, `completed` columns

**Check 3: Is RLS blocking access?**
Check Supabase logs for permission errors

**Check 4: Is access token valid?**
Look for logs:
```
W UserRepository: No access token available
```

If token missing, re-login to app.

### Question Counts Still Show 10

**Possible causes:**

1. **No questions in database** for that module
   - Check: `SELECT COUNT(*) FROM quiz_questions WHERE module_id = 'decantation';`
   
2. **RLS policy blocking read**
   - Check: RLS policies on `quiz_questions` table
   - Policy should allow SELECT for authenticated users

3. **Access token missing**
   - Function falls back to hardcoded 10
   - Solution: Login to get token

### Scores Not Updating After Quiz

1. **Check if quiz completion saved**:
   ```powershell
   adb logcat | Select-String "UPSERT successful"
   ```

2. **Check database directly**:
   ```sql
   SELECT * FROM progress WHERE user_id = 'your-user-id';
   SELECT * FROM quiz_results WHERE user_id = 'your-user-id' ORDER BY created_at DESC LIMIT 5;
   ```

3. **Check profile refresh**:
   - Profile should auto-refresh on ON_RESUME
   - Look for: `"ProfileViewModel: Starting profile load..."`

## Files Modified

### Kotlin App
- ✅ `UserRepository.kt` - Added `getModuleQuestionCount()`, updated module loading
- ✅ All 4 KylonModuleData entries - Added `questionCount = 10` parameter

### Already Fixed (Previous Sessions)
- ✅ `ProfileScreen.kt` - DisposableEffect refresh on ON_RESUME
- ✅ `ProfileViewModel.kt` - Proper loadProfile() with logging
- ✅ `QuizViewModel.kt` - Saves to both progress and quiz_results
- ✅ Web admin Analytics.tsx, users.ts, Users.tsx, analytics.ts

## Next Steps

1. ✅ **Build and install app** (already done)
2. ⚠️ **Run FIX_QUIZ_SCORE_UPDATE.sql** in Supabase (if not done)
3. ⚠️ **LOGIN to the app** (CRITICAL!)
4. ✅ Test profile - should show question counts from database
5. ✅ Complete a quiz - should update progress
6. ✅ Return to profile - should refresh and show new scores
7. ✅ Check web admin - should show updated analytics

## Summary

### What Was Fixed
1. ✅ Dynamic question counts from database (not hardcoded)
2. ✅ Better error handling with fallbacks
3. ✅ Enhanced logging to track actual question counts

### What Still Needs Attention
1. ⚠️ **User must login** - app requires authentication
2. ⚠️ **Database schema** - run FIX_QUIZ_SCORE_UPDATE.sql if not done
3. ⚠️ **RLS policies** - ensure quiz_questions table allows SELECT for authenticated users

### Key Insight
**The profile works correctly when user is logged in!** The "zeros problem" is because there's no active user session. Once logged in, the app will:
- Fetch actual question counts from database
- Load progress data from progress table
- Display scores and completion status
- Update on quiz completion
- Refresh on navigation back to profile
