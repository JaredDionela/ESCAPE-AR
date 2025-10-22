# Module Completion Rules - Clarification ✅

## ✅ Current System Status: CORRECTLY IMPLEMENTED

The system **already correctly implements** the rule that only scores of 70% or above complete a module.

## Module Completion Rules

### What "Completed" Means:
- ✅ **Score >= 70%**: Module is COMPLETED (marked as complete in database and UI)
- ❌ **Score < 70%**: Module is NOT completed (attempt recorded but module remains incomplete)

### Database Behavior:
When a student takes a quiz, the `progress` table is updated:

| Score Result | Database Field | UI Display |
|--------------|----------------|------------|
| Score >= 70% | `completed = true` | ✅ Shows checkmark, "Passed", green color |
| Score < 70%  | `completed = false` | ❌ Shows as incomplete, "Failed", red color |
| No attempt   | No record | ⚪ Shows as not started |

## Code Implementation

### 1. Android App - Save Quiz Progress
**File**: `app/src/main/java/com/example/escape_ar/data/repository/UserRepository.kt` (Line 673)

```kotlin
// Mark as completed only if best score is 70% or higher (passing grade)
val isCompleted = finalBestScore >= 70
```

**This ensures**:
- ✅ Only passing scores (70%+) set `completed = true` in database
- ❌ Failing scores (< 70%) set `completed = false` in database
- 🔄 If a student improves from failing to passing, `completed` updates to `true`

### 2. Android App - Profile Screen
**File**: `app/src/main/java/com/example/escape_ar/ui/screens/ProfileScreen.kt` (Lines 704-719)

```kotlin
val isPassed = module.score >= 70 // Passing grade is 70%
if (isPassed) {
    Text(text = "Passed", color = GlowGreen)
} else if (module.score > 0) {
    Text(text = "Failed", color = CrimsonRed)
}
```

**Visual Indicators**:
- ✅ Green checkmark icon if score >= 70%
- ❌ Gray/silver icon if score < 70% or not attempted
- ✅ "Passed" label (green) if score >= 70%
- ❌ "Failed" label (red) if 0% < score < 70%
- 🟢 Green progress bar if score >= 70%
- 🔴 Red progress bar if 0% < score < 70%
- 🔵 Blue progress bar if not attempted

### 3. Quiz Result Screen
**File**: `app/src/main/java/com/example/escape_ar/ui/screens/QuizComponents.kt` (Lines 354, 410, 435)

```kotlin
val isPassed = percentage >= 70

text = if (isPassed) "Module Complete!" else "Try Again"
ScoreItem("Status", if (isPassed) "PASSED" else "FAILED")
```

**Messages**:
- ✅ Score >= 70%: "Module Complete!" + "PASSED" + green colors
- ❌ Score < 70%: "Try Again" + "FAILED" + red colors

### 4. Web Admin - Analytics
**File**: `web-admin/src/pages/Analytics.tsx` (Lines 215-220)

```typescript
// Get users who completed this module (70%+ only)
const { data: completedAttempts } = await supabase
  .from('progress')
  .select('user_id')
  .eq('module', module.id)
  .eq('completed', true);  // ✅ Only counts 70%+ scores
```

**Module Progress Chart**:
- Shows "Completed" count = students with score >= 70%
- Shows "Total" count = all students who attempted (any score)
- Completion Rate = Completed / Total

### 5. Web Admin - User Details
**File**: `web-admin/src/lib/api/users.ts` (Line 171)

```typescript
completed: progress?.completed || false,
```

**User Stats Display**:
- Module shows as "completed" only if database `completed = true`
- This means only scores >= 70% show as completed

## Analytics & Reporting

### What Teachers See:

#### Module Performance Overview (Analytics Page)
- **Total Attempts**: All quiz attempts regardless of score
- **Completed**: Only students who scored >= 70%
- **Completion Rate**: (Completed / Total) × 100%

Example:
```
Decantation Module
├─ Total Attempts: 10 students
├─ Completed: 7 students (70%+ score)
└─ Completion Rate: 70%
```

#### Top Performers (Analytics Page)
- **Shows**: ALL students with quiz attempts
- **Sorted by**: Best score (highest to lowest)
- **Includes**: Both passing and failing scores

This allows teachers to:
- ✅ Identify high performers (top scores)
- ✅ Identify struggling students (low scores)
- ✅ See complete picture of class performance

#### Individual Student View (Users Page)
- **Module Breakdown**: Shows all 4 modules
- **Latest Quiz Score**: Most recent attempt (passing or failing)
- **Total Attempts**: Number of times student took the quiz
- **Status**: Based on `completed` field (true = passed, false = failed/not attempted)

## Student Experience

### Scenario 1: First Attempt - Failed
1. Student takes quiz, scores 40%
2. ❌ Database: `completed = false`, `best_score = 40`
3. ❌ Profile: Shows "Failed" (red), red progress bar
4. ❌ Module NOT completed
5. 🔄 Student can retake quiz

### Scenario 2: Retake - Passed
1. Student retakes quiz, scores 80%
2. ✅ Database: `completed = true`, `best_score = 80`
3. ✅ Profile: Shows "Passed" (green), green progress bar, checkmark
4. ✅ Module IS completed
5. 🏆 Student can retake to improve score further

### Scenario 3: Multiple Attempts
1. Attempt 1: 30% → `completed = false`, `best_score = 30`
2. Attempt 2: 50% → `completed = false`, `best_score = 50`
3. Attempt 3: 75% → `completed = true`, `best_score = 75` ✅
4. Attempt 4: 65% → `completed = true`, `best_score = 75` (keeps best)

**Rule**: Once completed (70%+), stays completed even if later score is lower

## Database Schema

```sql
CREATE TABLE progress (
  user_id UUID REFERENCES profiles(id),
  module module_code NOT NULL,  -- 'decantation', 'organ_system', etc.
  best_score INTEGER DEFAULT 0, -- 0-100
  completed BOOLEAN DEFAULT false, -- true only if best_score >= 70
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW(),
  PRIMARY KEY (user_id, module)
);
```

## Verification Checklist

To verify the system is working correctly:

### ✅ Android App
- [ ] Take quiz with score < 70%
  - [ ] Shows "Try Again" and "FAILED"
  - [ ] Profile shows "Failed" (red)
  - [ ] No checkmark icon

- [ ] Take quiz with score >= 70%
  - [ ] Shows "Module Complete!" and "PASSED"
  - [ ] Profile shows "Passed" (green)
  - [ ] Green checkmark icon

### ✅ Web Admin
- [ ] Check Analytics page
  - [ ] "Completed" count only includes 70%+ scores
  - [ ] "Total Attempts" includes all scores

- [ ] Check User Details
  - [ ] Failed attempts (< 70%) show in quiz history
  - [ ] Module shows as completed only if score >= 70%

### ✅ Database
```sql
-- Check a student's progress
SELECT user_id, module, best_score, completed
FROM progress
WHERE user_id = 'your-student-uuid';

-- Should see:
-- best_score >= 70 → completed = true
-- best_score < 70 → completed = false
```

## Common Questions

### Q: Can a student complete a module with 50%?
**A**: ❌ No, 70% is the minimum passing score.

### Q: If a student scores 80% then 60% on a retake, are they still completed?
**A**: ✅ Yes, `best_score = 80` and `completed = true` are preserved.

### Q: Do failed attempts show in the admin panel?
**A**: ✅ Yes, teachers can see ALL attempts including failed ones in:
- Analytics "Top Performers" section (sorted by score)
- User Details "Module Breakdown" (latest score and attempt count)

### Q: What if a student never reaches 70%?
**A**: The module remains incomplete (`completed = false`), but their attempts and best score are tracked so teachers can identify students who need help.

---

## Summary

✅ **Module completion requires 70% or higher score**  
✅ **Android app correctly sets completed = true only for 70%+**  
✅ **Profile screen correctly shows "Passed" only for 70%+**  
✅ **Web admin correctly counts completions as 70%+ only**  
✅ **Teachers can see ALL attempts including failures**  
✅ **Students can retake quizzes to improve**  

**Date**: October 23, 2025  
**Status**: CORRECTLY IMPLEMENTED ✅
