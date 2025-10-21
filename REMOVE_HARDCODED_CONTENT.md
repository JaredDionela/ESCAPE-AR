# 🗑️ REMOVING HARDCODED CONTENT FROM ANDROID APP

## ✅ What Was Fixed

### 1. SQL Syntax Error Fixed
- **File:** `supabase/migrations/004_create_quiz_tables.sql`
- **Error:** `'s` → Fixed to `''s` (SQL escape for apostrophe)
- **Line 131:** Changed `Earth\'s` to `Earth''s`

### 2. Created Proper Database Models
- **File:** `app/src/main/java/com/example/escape_ar/data/model/Models.kt`
- **Added:** `QuizQuestion` data class (matches Supabase schema)
- **Added:** `QuizResult` data class (for storing student answers)
- **Fields match:** `module_id`, `question_text`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`

### 3. Updated QuizRepository
- **File:** `app/src/main/java/com/example/escape_ar/data/repository/QuizRepository.kt`
- **Removed:** All hardcoded mock data
- **Added:** Real Supabase queries:
  - `getQuizQuestionsByModule()` - Fetch questions from database
  - `getAllQuizQuestions()` - Fetch all questions
  - `submitQuizAnswer()` - Save student answers
  - `getQuizResultsByUserAndModule()` - Get student results
  - `calculateModuleScore()` - Calculate quiz performance

---

## 🔄 What Still Needs To Be Done

### Files to Delete:
```
❌ app/src/main/java/com/example/escape_ar/ui/screens/QuizQuestions.kt
```
This entire file contains hardcoded quiz data and is no longer needed!

### Files to Update:

#### 1. **QuizScreen.kt** - Remove hardcoded questions
**Current Problem:**
```kotlin
// Lines 49-87: Hardcoded module data with questions
val modules = remember {
    listOf(
        QuizModuleData(
            questions = getDecantationQuestions()  // ❌ Hardcoded!
        ),
        QuizModuleData(
            questions = getOrganSystemQuestions()  // ❌ Hardcoded!
        ),
        // ... etc
    )
}
```

**Solution:**
- Fetch questions from `QuizRepository.getQuizQuestionsByModule(moduleId)`
- Use `LaunchedEffect` to load questions when module is selected
- Show loading state while fetching
- Handle empty state (no questions in database)

#### 2. **QuizViewModel** - Add quiz loading logic
**Need to add:**
```kotlin
// In QuizViewModel
private val quizRepository = QuizRepository()

fun loadQuizQuestions(moduleId: String) {
    viewModelScope.launch {
        _isLoading.value = true
        val result = quizRepository.getQuizQuestionsByModule(moduleId)
        result.onSuccess { questions ->
            // Convert to UI model if needed
            _questions.value = questions
        }.onFailure { error ->
            // Handle error
        }
        _isLoading.value = false
    }
}
```

---

## 📋 STEP-BY-STEP MIGRATION GUIDE

### Step 1: Run the SQL Migration ✅ (DO THIS FIRST!)

1. Go to **Supabase Dashboard** → **SQL Editor**
2. Copy ALL content from: `supabase/migrations/004_create_quiz_tables.sql`
3. Paste and click **Run**
4. Verify tables created:
```sql
SELECT COUNT(*) FROM quiz_questions;
-- Should return 24 (sample questions)
```

### Step 2: Delete Hardcoded Quiz File

Delete this file:
```
app/src/main/java/com/example/escape_ar/ui/screens/QuizQuestions.kt
```

Or rename it to `QuizQuestions.kt.backup` if you want to keep it temporarily.

### Step 3: Create Quiz Data Adapter

Create new file: `app/src/main/java/com/example/escape_ar/ui/screens/QuizDataAdapter.kt`

```kotlin
package com.example.escape_ar.ui.screens

import com.example.escape_ar.data.model.QuizQuestion as DbQuizQuestion

/**
 * UI model for quiz questions (what the screen uses)
 */
data class UiQuizQuestion(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctAnswer: Int,
    val explanation: String = ""
)

/**
 * Convert database QuizQuestion to UI QuizQuestion
 */
fun DbQuizQuestion.toUiModel(): UiQuizQuestion {
    return UiQuizQuestion(
        id = this.id,
        question = this.questionText,
        options = this.getOptions(),
        correctAnswer = this.getCorrectAnswerIndex(),
        explanation = "" // Can add explanation field to database later
    )
}

/**
 * Convert list of database questions to UI questions
 */
fun List<DbQuizQuestion>.toUiModels(): List<UiQuizQuestion> {
    return this.map { it.toUiModel() }
}
```

### Step 4: Update QuizScreen.kt

Find and replace the hardcoded modules section:

**REMOVE Lines 49-87** (the entire `modules = remember { listOf(...) }` block)

**ADD** at the top of QuizScreen function:
```kotlin
val quizRepository = remember { QuizRepository() }
var questions by remember { mutableStateOf<List<UiQuizQuestion>>(emptyList()) }
var isLoadingQuestions by remember { mutableStateOf(false) }
var loadError by remember { mutableStateOf<String?>(null) }

// Load questions when module is selected
LaunchedEffect(selectedModule) {
    selectedModule?.let { module ->
        isLoadingQuestions = true
        loadError = null
        
        val result = quizRepository.getQuizQuestionsByModule(module.id)
        result.onSuccess { dbQuestions ->
            questions = dbQuestions.toUiModels()
        }.onFailure { error ->
            loadError = "Failed to load questions: ${error.message}"
        }
        
        isLoadingQuestions = false
    }
}
```

**REPLACE** the modules list with:
```kotlin
val modules = remember {
    listOf(
        QuizModuleData(
            id = "decantation",
            name = "Decantation",
            description = "Learn how to separate mixtures",
            icon = Icons.Default.Science,
            color = NeonCyan,
            totalQuestions = 0, // Will be updated from database
            questions = emptyList() // No longer hardcoded!
        ),
        QuizModuleData(
            id = "organ_system",
            name = "Organ Systems",
            description = "Learn about human organ systems",
            icon = Icons.Default.Favorite,
            color = CrimsonRed,
            totalQuestions = 0,
            questions = emptyList()
        ),
        QuizModuleData(
            id = "simple_machines",
            name = "Simple Machines",
            description = "Explore simple machines",
            icon = Icons.Default.Settings,
            color = AmberAlert,
            totalQuestions = 0,
            questions = emptyList()
        ),
        QuizModuleData(
            id = "solar_system",
            name = "Solar System",
            description = "Journey through space",
            icon = Icons.Default.Brightness7,
            color = PurpleHaze,
            totalQuestions = 0,
            questions = emptyList()
        )
    )
}
```

### Step 5: Update QuizModuleData

Find the `QuizModuleData` class and change:
```kotlin
data class QuizModuleData(
    val id: String,
    val name: String,
    val description: String,
    val icon: ImageVector,
    val color: androidx.compose.ui.graphics.Color,
    val totalQuestions: Int,
    val questions: List<UiQuizQuestion> = emptyList() // Changed type!
)
```

### Step 6: Rename UI QuizQuestion

In QuizScreen.kt, find the old `data class QuizQuestion` and rename it to `UiQuizQuestion` to avoid conflicts:

```kotlin
// OLD (around line 352):
data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctAnswer: Int,
    val explanation: String
)

// DELETE THIS - Use the one from QuizDataAdapter.kt instead!
```

---

## 🎯 TESTING CHECKLIST

After making all changes:

### 1. Build the App
```powershell
cd "C:\Users\Jared Dionela\AndroidStudioProjects\ESCAPEAR"
.\gradlew assembleDebug
```

### 2. Install on Device
```powershell
.\gradlew installDebug
```

### 3. Test Quiz Flow
1. Open Android app
2. Go to Quizzes
3. Select "Decantation" module
4. Should see questions loaded from database
5. Answer questions
6. Check that results are saved

### 4. Verify Database
In Supabase SQL Editor:
```sql
-- Check questions exist
SELECT module_id, COUNT(*) as count 
FROM quiz_questions 
GROUP BY module_id;

-- Check student answers being saved
SELECT * FROM quiz_results 
WHERE user_id = 'YOUR_USER_ID' 
ORDER BY created_at DESC 
LIMIT 10;
```

---

## 🚨 IMPORTANT NOTES

### Before Deleting Hardcoded File:
1. ✅ Run SQL migration first (creates quiz_questions table)
2. ✅ Test that questions load from database
3. ✅ Backup the hardcoded file just in case

### Database Must Have Data:
- The SQL migration includes 24 sample questions
- If you deleted them, students will see empty quizzes
- Use web admin to add questions before deleting hardcoded data

### For Other Hardcoded Content:
Similar pattern applies to:
- **Lesson videos** - Should come from `lessons` table (already done!)
- **Lesson files** - Should come from `lesson_files` table (already done!)
- **User progress** - Should come from `lesson_progress` and `quiz_results` tables

---

## 📝 SUMMARY

### What's Done:
- ✅ Fixed SQL syntax error
- ✅ Created database models (QuizQuestion, QuizResult)
- ✅ Updated QuizRepository with real queries
- ✅ SQL migration ready with sample data

### What You Need To Do:
1. ⏳ Run `004_create_quiz_tables.sql` in Supabase
2. ⏳ Delete `QuizQuestions.kt` (hardcoded data file)
3. ⏳ Create `QuizDataAdapter.kt` (converter)
4. ⏳ Update `QuizScreen.kt` (use repository instead of hardcoded)
5. ⏳ Test quiz flow in app
6. ⏳ Verify answers saved to database

### Result:
- ❌ No more hardcoded quiz questions in app
- ✅ All questions come from Supabase database
- ✅ Admin can manage via web admin panel
- ✅ Students see latest content automatically
- ✅ Real-time sync between admin and app

---

## 🔗 Related Files

- **SQL Migration:** `supabase/migrations/004_create_quiz_tables.sql`
- **Data Models:** `app/src/main/java/com/example/escape_ar/data/model/Models.kt`
- **Repository:** `app/src/main/java/com/example/escape_ar/data/repository/QuizRepository.kt`
- **UI Screen:** `app/src/main/java/com/example/escape_ar/ui/screens/QuizScreen.kt`
- **Hardcoded (DELETE):** `app/src/main/java/com/example/escape_ar/ui/screens/QuizQuestions.kt`

---

## 💡 Future Improvements

After basic migration works:
1. Add question explanations field to database
2. Add question difficulty levels
3. Add question images/diagrams
4. Add timed quizzes
5. Add quiz retry limits
6. Add detailed analytics per question

**Start by running the SQL, then follow the steps above! 🚀**
