# ========================================
# AUTOMATED QUIZ DIAGNOSTIC
# ========================================

Write-Host "🔍 QUIZ DIAGNOSTIC TOOL" -ForegroundColor Cyan
Write-Host "========================================"

# Step 1: Clear logs
Write-Host "`n📋 Step 1: Clearing old logs..." -ForegroundColor Yellow
adb logcat -c
Write-Host "✅ Logs cleared!" -ForegroundColor Green

# Step 2: Wait for user
Write-Host "`n👆 Step 2: Now do this:" -ForegroundColor Yellow
Write-Host "   1. Open ESCAPE AR app on your device" -ForegroundColor White
Write-Host "   2. Navigate to Quiz section" -ForegroundColor White
Write-Host "   3. Click on ANY quiz module (Decantation, Organ System, or Simple Machines)" -ForegroundColor White
Write-Host "   4. Wait to see if questions load or if it shows 'No questions available'" -ForegroundColor White
Write-Host "`nPress ENTER after you've clicked a quiz module..." -ForegroundColor Cyan
Read-Host

# Step 3: Capture logs
Write-Host "`n📊 Step 3: Capturing logs..." -ForegroundColor Yellow
$logs = adb logcat -d | Select-String "QuizRepository|quiz_questions"

if ($logs) {
    Write-Host "✅ Found quiz-related logs!" -ForegroundColor Green
    Write-Host "`n========================================" -ForegroundColor Cyan
    Write-Host "QUIZ LOGS:" -ForegroundColor Cyan
    Write-Host "========================================`n" -ForegroundColor Cyan
    
    $logs | ForEach-Object {
        $line = $_.Line
        if ($line -match "ERROR|❌") {
            Write-Host $line -ForegroundColor Red
        } elseif ($line -match "SUCCESS|✅") {
            Write-Host $line -ForegroundColor Green
        } elseif ($line -match "WARNING|⚠️") {
            Write-Host $line -ForegroundColor Yellow
        } else {
            Write-Host $line -ForegroundColor White
        }
    }
    
    Write-Host "`n========================================" -ForegroundColor Cyan
    
    # Save to file
    $logs | Out-File -FilePath "quiz_diagnostic_logs.txt"
    Write-Host "`n💾 Full logs saved to: quiz_diagnostic_logs.txt" -ForegroundColor Green
    
    # Analysis
    Write-Host "`n🔎 QUICK ANALYSIS:" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    
    $fetchCount = ($logs | Select-String "Successfully fetched (\d+) questions").Matches.Groups[1].Value
    if ($fetchCount) {
        if ($fetchCount -eq "0") {
            Write-Host "❌ PROBLEM: 0 questions fetched from database" -ForegroundColor Red
            Write-Host "`nPossible causes:" -ForegroundColor Yellow
            Write-Host "  1. Quiz questions not inserted in database" -ForegroundColor White
            Write-Host "  2. RLS policies blocking access" -ForegroundColor White
            Write-Host "  3. Module ID mismatch" -ForegroundColor White
            Write-Host "`nNext steps:" -ForegroundColor Yellow
            Write-Host "  1. Run CHECK_QUIZ_STATUS.sql in Supabase" -ForegroundColor White
            Write-Host "  2. Verify COMPLETE_FIX_ALL_ISSUES.sql ran successfully" -ForegroundColor White
        } else {
            Write-Host "✅ SUCCESS: $fetchCount questions fetched!" -ForegroundColor Green
        }
    }
    
    $errorCount = ($logs | Select-String "ERROR|❌").Count
    if ($errorCount -gt 0) {
        Write-Host "❌ Found $errorCount error(s) - check logs above" -ForegroundColor Red
    }
    
} else {
    Write-Host "No quiz logs found!" -ForegroundColor Yellow
    Write-Host "`nThis could mean:" -ForegroundColor White
    Write-Host "  - You did not click a quiz module yet" -ForegroundColor White
    Write-Host "  - App is not running" -ForegroundColor White
    Write-Host "  - Device not connected" -ForegroundColor White
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "📋 NEXT: Share the logs above!" -ForegroundColor Cyan
Write-Host "========================================"
