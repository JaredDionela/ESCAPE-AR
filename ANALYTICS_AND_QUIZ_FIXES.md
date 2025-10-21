# Analytics Dashboard & Quiz Fixes

## Issues Fixed

### 1. ✅ Quiz Crash Fix (Android App)
**Problem:** App crashed when starting a quiz with error:
```
java.lang.IndexOutOfBoundsException: Empty list doesn't contain element at index 0
```

**Root Cause:** The quiz questions list was initialized as `emptyList()` but never loaded from the database. When users clicked "Start Quiz", the app tried to access `questions[0]` on an empty list.

**Solution:**
- Added `LaunchedEffect` in `QuizScreen.kt` to automatically load questions from database when a module is selected
- Added loading state with `CircularProgressIndicator` while fetching questions
- Added error handling with user-friendly error messages
- Questions are now properly loaded using `QuizViewModel.getQuizQuestionsByModule()`

**Files Modified:**
- `app/src/main/java/com/example/escape_ar/ui/screens/QuizScreen.kt`

**Code Changes:**
```kotlin
// Added state variables
var isLoadingQuestions by remember { mutableStateOf(false) }
var loadError by remember { mutableStateOf<String?>(null) }

// Added LaunchedEffect to load questions
LaunchedEffect(selectedModule?.id) {
    selectedModule?.let { module ->
        if (module.questions.isEmpty()) {
            isLoadingQuestions = true
            loadError = null
            
            val result = quizViewModel.getQuizQuestionsByModule(module.id)
            result.onSuccess { questions ->
                if (questions.isNotEmpty()) {
                    selectedModule = module.copy(
                        questions = questions,
                        totalQuestions = questions.size
                    )
                } else {
                    loadError = "No questions available for this module yet"
                }
                isLoadingQuestions = false
            }.onFailure { error ->
                loadError = error.message ?: "Failed to load questions"
                isLoadingQuestions = false
            }
        }
    }
}

// Added loading and error UI states
if (isLoadingQuestions) {
    // Show loading spinner
} else if (loadError != null) {
    // Show error message with back button
} else if (!showResults) {
    // Show quiz questions (existing code)
}
```

---

### 2. ✅ Quiz Score Tracking Fix (Admin Dashboard)
**Problem:** Quiz average scores were calculated incorrectly. The code was trying to access `score` and `total_questions` fields that don't exist in the `quiz_results` table.

**Root Cause:** Database schema mismatch. The `quiz_results` table stores individual question answers with `is_correct` boolean, not aggregate scores.

**Actual Schema:**
```sql
CREATE TABLE quiz_results (
    id UUID PRIMARY KEY,
    user_id UUID,
    question_id UUID,
    module_id TEXT,
    selected_answer TEXT, -- "A", "B", "C", or "D"
    is_correct BOOLEAN,
    created_at TIMESTAMP
);
```

**Solution:**
- Fixed quiz score calculation to count `is_correct` answers
- Now correctly calculates: `(correct_answers / total_answers) * 100`

**Files Modified:**
- `web-admin/src/pages/Analytics.tsx`

**Before:**
```typescript
const { data: quizResults } = await supabase
  .from('quiz_results')
  .select('score, total_questions');

let totalScore = 0;
let totalQuestions = 0;
if (quizResults) {
  quizResults.forEach((result: any) => {
    totalScore += result.score;
    totalQuestions += result.total_questions;
  });
}
const quizAverageScore = totalQuestions > 0 
  ? Math.round((totalScore / totalQuestions) * 100) 
  : 0;
```

**After:**
```typescript
const { data: quizResults } = await supabase
  .from('quiz_results')
  .select('is_correct');

let correctAnswers = 0;
let totalAnswers = 0;
if (quizResults) {
  totalAnswers = quizResults.length;
  correctAnswers = quizResults.filter((result: any) => result.is_correct).length;
}
const quizAverageScore = totalAnswers > 0 
  ? Math.round((correctAnswers / totalAnswers) * 100) 
  : 0;
```

---

### 3. ✅ User Tracking Fix (Admin Dashboard)
**Problem:** Active users count was only looking at lesson_progress table, missing users who only took quizzes.

**Solution:**
- Now tracks users from both `lesson_progress` AND `quiz_results` tables
- Uses Set to get unique user IDs from both sources
- Provides accurate count of users active in last 7 days

**Files Modified:**
- `web-admin/src/pages/Analytics.tsx`

**Before:**
```typescript
const { count: activeUsers } = await supabase
  .from('lesson_progress')
  .select('user_id', { count: 'exact', head: true })
  .gte('updated_at', sevenDaysAgo.toISOString());
```

**After:**
```typescript
const { data: lessonUsers } = await supabase
  .from('lesson_progress')
  .select('user_id')
  .gte('updated_at', sevenDaysAgo.toISOString());

const { data: quizUsers } = await supabase
  .from('quiz_results')
  .select('user_id')
  .gte('created_at', sevenDaysAgo.toISOString());

const activeUserIds = new Set([
  ...(lessonUsers?.map((u: any) => u.user_id) || []),
  ...(quizUsers?.map((u: any) => u.user_id) || []),
]);
const activeUsers = activeUserIds.size;
```

---

### 4. ✅ Top Performers Fix (Admin Dashboard)
**Problem:** Top performers calculation was using non-existent `score` and `total_questions` fields.

**Solution:**
- Fixed to count `is_correct` answers from quiz_results
- Improved sorting: primary by quiz score, secondary by completed lessons
- Filter out users with no quiz activity
- Show top 5 performers

**Files Modified:**
- `web-admin/src/pages/Analytics.tsx`

**Changes:**
```typescript
const topPerformers = topPerformersData?.map((user: any) => {
  const correctAnswers = user.quiz_results.filter((r: any) => r.is_correct).length;
  const totalAnswers = user.quiz_results.length;
  const score = totalAnswers > 0 
    ? Math.round((correctAnswers / totalAnswers) * 100) 
    : 0;
  const completed = user.lesson_progress.filter((lp: any) => lp.completed).length;

  return {
    name: user.display_name || 'Anonymous',
    score,
    completed,
    totalAnswers,
  };
})
  .filter((user: any) => user.totalAnswers > 0) // Only users with quiz activity
  .sort((a: any, b: any) => {
    if (b.score !== a.score) return b.score - a.score;
    return b.completed - a.completed;
  })
  .slice(0, 5)
  .map(({ name, score, completed }) => ({ name, score, completed })) || [];
```

---

### 5. ✅ Module Progress Fix (Admin Dashboard)
**Problem:** Module progress was counting ALL completed lessons for each module, not filtering by module_id.

**Solution:**
- First get lesson IDs for each specific module
- Then count completed lesson_progress entries only for those lesson IDs
- Provides accurate per-module completion statistics

**Files Modified:**
- `web-admin/src/pages/Analytics.tsx`

**Before:**
```typescript
const { count: total } = await supabase
  .from('lessons')
  .select('*', { count: 'exact', head: true })
  .eq('module_id', module.id);

const { count: completed } = await supabase
  .from('lesson_progress')
  .select('*', { count: 'exact', head: true })
  .eq('completed', true); // ❌ Not filtered by module!
```

**After:**
```typescript
const { count: total } = await supabase
  .from('lessons')
  .select('*', { count: 'exact', head: true })
  .eq('module_id', module.id);

// Get lesson IDs for this module
const { data: lessonIds } = await supabase
  .from('lessons')
  .select('id')
  .eq('module_id', module.id);

const lessonIdsArray = lessonIds?.map((l: any) => l.id) || [];

let completedCount = 0;
if (lessonIdsArray.length > 0) {
  const { count: completed } = await supabase
    .from('lesson_progress')
    .select('*', { count: 'exact', head: true })
    .eq('completed', true)
    .in('lesson_id', lessonIdsArray); // ✅ Filtered by module's lessons
  completedCount = completed || 0;
}
```

---

## User Stats Already Working ✅

The user statistics in the User Management page (`getUserStats` in `users.ts`) were already correctly implemented:

```typescript
// Already correct implementation
const completedLessons = lessonProgress?.filter(l => l.completed).length || 0
const totalQuizzes = quizResults?.length || 0
const correctAnswers = quizResults?.filter(q => q.is_correct).length || 0
const quizAccuracy = totalQuizzes > 0 ? (correctAnswers / totalQuizzes) * 100 : 0
```

---

## Testing Steps

### Android App (Quiz Loading)
1. ✅ Open the app and navigate to Quizzes
2. ✅ Select any module (Decantation, Organ System, etc.)
3. ✅ Should see "Loading questions..." spinner
4. ✅ If questions exist in database: Quiz starts normally
5. ✅ If no questions: Shows error message "No questions available for this module yet"
6. ✅ Users can go back to module selection from error screen

### Admin Dashboard (Analytics)
1. ✅ Open web-admin (http://localhost:3003)
2. ✅ Navigate to Analytics page
3. ✅ Verify quiz average score shows correct percentage
4. ✅ Verify active users count includes quiz takers
5. ✅ Verify top performers shows users with quiz scores
6. ✅ Verify module progress bars show correct per-module completion

### Admin Dashboard (User Management)
1. ✅ Navigate to Users page
2. ✅ Click "View Details" on any user
3. ✅ Verify quiz accuracy percentage is calculated correctly
4. ✅ Verify completed lessons count is accurate

---

## Next Steps for Full Functionality

### For Lessons to Show Content:
1. **Admin needs to add lessons via web-admin**
   - Go to Lessons page
   - Click "Add Lesson" for each module
   - Fill in title, description, YouTube video ID
   - Save lesson

2. **Database must have lessons**
   - Check Supabase dashboard
   - Verify `lessons` table has entries for each module_id

### For Quizzes to Show Questions:
1. **Admin needs to add quiz questions via web-admin**
   - Go to Quiz page (module-based)
   - Select a module (Decantation, Organ System, etc.)
   - Click "Add Question"
   - Fill in question text and 4 options (A, B, C, D)
   - Mark correct answer
   - Save question

2. **Database must have quiz questions**
   - Check Supabase dashboard
   - Verify `quiz_questions` table has entries for each module_id

---

## Summary

All analytics tracking and quiz loading are now working correctly:

✅ **Android App:** Quizzes load from database with proper error handling  
✅ **Analytics:** Quiz scores calculated using `is_correct` field  
✅ **Analytics:** User tracking includes both lesson and quiz activity  
✅ **Analytics:** Top performers sorted by quiz score + lesson completion  
✅ **Analytics:** Module progress properly filtered by module_id  
✅ **User Stats:** Already working correctly with `is_correct` field  

**The app is now ready for admins to add content!**
