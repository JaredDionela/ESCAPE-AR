# FINAL QUIZ DIAGNOSTIC
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "QUIZ DIAGNOSTIC - Complete Test" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "Step 1: Clearing logcat..." -ForegroundColor Yellow
adb logcat -c
Start-Sleep -Seconds 1
Write-Host "Done!`n" -ForegroundColor Green

Write-Host "Step 2: ACTION REQUIRED" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor White
Write-Host "1. Open ESCAPE AR app" -ForegroundColor White
Write-Host "2. Navigate to Quiz" -ForegroundColor White  
Write-Host "3. Click on Decantation module" -ForegroundColor White
Write-Host "4. Wait 3 seconds" -ForegroundColor White
Write-Host "`nPress ENTER when done..." -ForegroundColor Cyan
Read-Host

Write-Host "`nStep 3: Capturing logs..." -ForegroundColor Yellow
Start-Sleep -Seconds 2

# Get all logs
$allLogs = adb logcat -d

# Filter for quiz-related
$quizLogs = $allLogs | Select-String -Pattern "Quiz" -Context 1

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "RESULTS:" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

if ($quizLogs) {
    Write-Host "Found quiz logs:`n" -ForegroundColor Green
    $quizLogs | ForEach-Object { Write-Host $_.Line -ForegroundColor White }
    $quizLogs | Out-File "complete_quiz_logs.txt"
    Write-Host "`nSaved to complete_quiz_logs.txt" -ForegroundColor Green
} else {
    Write-Host "No quiz logs found!" -ForegroundColor Red
    Write-Host "`nShowing last 50 lines of all logs:" -ForegroundColor Yellow
    $allLogs | Select-Object -Last 50 | ForEach-Object { Write-Host $_ -ForegroundColor Gray }
}

Write-Host "`n========================================" -ForegroundColor Cyan
