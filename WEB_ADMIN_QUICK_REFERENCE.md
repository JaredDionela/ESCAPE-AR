# Web Admin - Quick Reference

## 🚀 What Changed

| Component | Before | After |
|-----------|--------|-------|
| **Dashboard: Total Views** | Tracked lesson_progress count | ❌ Removed |
| **Dashboard: Total Quizzes** | Count of questions | ✅ "Quiz Questions" - total questions created |
| **Dashboard: New Stat** | N/A | ✅ "Quiz Completions" - unique users who completed |
| **Analytics: Lesson Completion** | Total lessons vs completed | ✅ User-lesson pairs completion rate |
| **Analytics: Module Progress** | Lesson completions | ✅ Quiz completions from progress table |
| **Analytics: Top Performers** | Complex join with mixed data | ✅ Simple query from progress table |
| **User Details** | Already functional | ✅ No changes needed |

## 📊 Data Sources

| Metric | Source Table | Query |
|--------|-------------|-------|
| Total Users | `profiles` | `COUNT(*)` |
| Total Lessons | `lessons` | `COUNT(*)` |
| Quiz Questions | `quiz_questions` | `COUNT(*)` |
| Quiz Completions | `progress` | `COUNT(DISTINCT user_id) WHERE completed=true` |
| Lesson Completion % | `lesson_progress` | Completed pairs / Total pairs |
| Module Progress | `progress` | Users per module WHERE completed=true |
| Top Performers | `progress` | AVG(best_score) GROUP BY user_id |
| User Best Scores | `progress` | best_score per module |
| User Quiz Attempts | `quiz_results` | COUNT(*) WHERE score_percentage IS NOT NULL |

## 🎯 Key Tables

### progress (Most Important!)
```
user_id | module | best_score | completed | updated_at
--------|--------|------------|-----------|------------
abc123  | decant | 85         | true      | 2025-10-18
abc123  | organ  | 92         | true      | 2025-10-17
```
**Purpose:** Tracks which users completed which module quizzes and their best scores

### quiz_results
```
user_id | module_id | score_percentage | total_questions | correct_answers | created_at
--------|-----------|-----------------|-----------------|-----------------|------------
abc123  | decant    | 85              | 20              | 17              | 2025-10-18
```
**Purpose:** Records each quiz attempt with detailed results

### lesson_progress
```
user_id | lesson_id | completed | completed_at
--------|-----------|-----------|-------------
abc123  | lesson1   | true      | 2025-10-18
```
**Purpose:** Tracks lesson completions

## ✅ Quick Testing

### 1. Check Dashboard
```bash
cd web-admin && npm run dev
# Visit http://localhost:3004/dashboard
```
Expected: 4 stat cards with numbers from database

### 2. Check Analytics
```bash
# Visit http://localhost:3004/analytics
```
Expected:
- Lesson completion % (not 0%)
- Module progress bars (showing students)
- Top 5 performers ranked by score

### 3. Check User Details
```bash
# Visit http://localhost:3004/users → Click any user
```
Expected:
- Module breakdown table
- Status, Best Score, Latest Quiz, Attempts

### 4. Verify with SQL
```sql
-- Quick verification
SELECT 
  'Total Users' as metric,
  COUNT(*) as value 
FROM profiles
UNION ALL
SELECT 'Quiz Completions', COUNT(DISTINCT user_id) FROM progress WHERE completed=true
UNION ALL
SELECT 'Total Lessons', COUNT(*) FROM lessons;
```

## 🐛 Troubleshooting

| Issue | Cause | Fix |
|-------|-------|-----|
| All stats show 0 | No data in database | Add test data or complete quizzes in app |
| Module progress 0/0 | No progress records | Complete quizzes in Android app |
| Top performers empty | No completed quizzes | Complete quizzes with `completed=true` |
| User details blank | User has no activity | Have user complete lessons/quizzes |
| TypeScript errors | Missing imports | Check Dashboard.tsx imports |

## 📝 Files Changed

1. `web-admin/src/lib/api/analytics.ts` - Interface & queries
2. `web-admin/src/pages/Dashboard.tsx` - Stat cards
3. `web-admin/src/pages/Analytics.tsx` - All sections
4. `web-admin/src/lib/api/users.ts` - Added helper function

## 🎉 Result

✅ No hard-coded values
✅ All data from database
✅ Real-time updates
✅ Accurate analytics
✅ Clean, maintainable code

**Everything works!** 🚀
