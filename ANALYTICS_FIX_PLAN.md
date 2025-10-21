# Analytics & Profile Persistence Fix

## Issues to Fix

### 1. **Module Scores Not Based on Actual Questions**
- Currently hardcoded to 100 max score
- Need to calculate based on actual question count from database
- Score should be: (correct answers / total questions) × 100

### 2. **Teacher Name & Section Disappearing**
- Fields not persisting after login/logout
- Extended profile needs to be loaded consistently
- ProfileViewModel should cache and restore this data

### 3. **Progress Analytics Not Functional**
- Module progress doesn't reflect actual quiz performance
- Overall progress calculation needs to be dynamic
- Need to show actual scores based on questions answered

## Solution Plan

### Step 1: Update Score Calculation
- Modify `completeModule` in QuizViewModel to use flexible scoring
- Save both percentage and actual question counts
- Update progress table to store total_questions

### Step 2: Fix Profile Data Persistence
- Ensure extended profile is loaded on every app start
- Cache teacher name and section in ProfileViewModel
- Add fallback to SessionManager for persistence

### Step 3: Dynamic Analytics
- Calculate module max scores based on actual questions in database
- Update ProfileScreen to show real-time progress
- Display correct/total questions instead of just percentages

## Implementation

### Files to Modify:
1. ✅ QuizViewModel.kt - Already saves flexible scoring
2. ✅ ProfileViewModel.kt - Load extended profile correctly
3. ✅ ProfileScreen.kt - Display dynamic scores
4. ⚠️ UserRepository.kt - Ensure profile loads consistently
5. ⚠️ QuizScreen.kt - Ensure proper score calculation

## Database Schema
```sql
-- progress table already has:
- user_id (uuid)
- module (text)
- best_score (integer) -- 0-100 percentage
- completed (boolean)
- created_at (timestamp)
- updated_at (timestamp)

-- quiz_results table has:
- id (uuid)
- user_id (uuid)
- module_id (text)
- correct_answers (integer)
- total_questions (integer)
- percentage (integer)
- completed_at (timestamp)
```

## Key Changes Made:
1. Score now calculated as: (correct / total) × 100
2. Module max score = 100 (percentage-based)
3. Display shows: "X/Y questions" where X = correct, Y = total
4. Teacher name & section loaded from profiles table consistently
