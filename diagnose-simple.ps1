# QUIZ DIAGNOSTIC - Simple Version
Write-Host "QUIZ DIAGNOSTIC TOOL" -ForegroundColor Cyan
Write-Host "========================================"

Write-Host "`nStep 1: Clearing old logs..." -ForegroundColor Yellow
adb logcat -c
Write-Host "Logs cleared!" -ForegroundColor Green

Write-Host "`nStep 2: Now click a quiz module in the app..." -ForegroundColor Yellow
Write-Host "Press ENTER after clicking..." -ForegroundColor Cyan
Read-Host

Write-Host "`nStep 3: Capturing logs..." -ForegroundColor Yellow
$logs = adb logcat -d | Select-String "QuizRepository"

if ($logs) {
    Write-Host "`nQUIZ LOGS:" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    $logs | Out-String | Write-Host
    $logs | Out-File -FilePath "quiz_logs.txt"
    Write-Host "Logs saved to quiz_logs.txt" -ForegroundColor Green
} else {
    Write-Host "No logs found - make sure you clicked a quiz!" -ForegroundColor Yellow
}
