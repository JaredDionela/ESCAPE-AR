# Quick Login and Profile Test Script
# Run this after logging into the app

Write-Host "================================" -ForegroundColor Cyan
Write-Host "Profile & Question Count Tester" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Step 1: Clearing logs..." -ForegroundColor Yellow
adb logcat -c
Start-Sleep -Seconds 1

Write-Host "Step 2: Starting app..." -ForegroundColor Yellow
adb shell am force-stop com.example.escape_ar
Start-Sleep -Seconds 1
adb shell am start -n com.example.escape_ar/.MainActivity
Start-Sleep -Seconds 5

Write-Host ""
Write-Host "Step 3: Checking login status..." -ForegroundColor Yellow
Write-Host "Looking for user session..." -ForegroundColor Gray

$loginLogs = adb logcat -d | Select-String "UserRepository.*getCurrentUser|Loaded user|No current Supabase" | Select-Object -Last 10

if ($loginLogs -match "No current Supabase user session") {
    Write-Host ""
    Write-Host "❌ USER NOT LOGGED IN!" -ForegroundColor Red
    Write-Host "   Please login to the app first, then run this script again." -ForegroundColor Red
    Write-Host ""
    Write-Host "After logging in, press Enter to continue..." -ForegroundColor Yellow
    Read-Host
    
    # Check again after user confirms login
    Write-Host "Checking login status again..." -ForegroundColor Yellow
    $loginLogs = adb logcat -d | Select-String "UserRepository.*Loaded user" | Select-Object -Last 5
    
    if ($loginLogs) {
        Write-Host "✅ User logged in!" -ForegroundColor Green
        $loginLogs | ForEach-Object { Write-Host $_.Line -ForegroundColor Gray }
    } else {
        Write-Host "❌ Still not logged in. Exiting..." -ForegroundColor Red
        exit
    }
} elseif ($loginLogs -match "Loaded user") {
    Write-Host "✅ User is logged in!" -ForegroundColor Green
    $loginLogs | Where-Object { $_ -match "Loaded user" } | ForEach-Object { Write-Host $_.Line -ForegroundColor Gray }
} else {
    Write-Host "⚠️ Cannot determine login status" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Step 4: Navigate to Profile screen in the app, then press Enter..." -ForegroundColor Yellow
Read-Host

Write-Host ""
Write-Host "Step 5: Checking question counts from database..." -ForegroundColor Yellow
Start-Sleep -Seconds 2

$questionLogs = adb logcat -d | Select-String "Module.*has.*questions in database" | Select-Object -Last 10

if ($questionLogs) {
    Write-Host "✅ Dynamic question counts loaded from database:" -ForegroundColor Green
    $questionLogs | ForEach-Object { Write-Host "   $_" -ForegroundColor Cyan }
} else {
    Write-Host "⚠️ No question count logs found" -ForegroundColor Yellow
    Write-Host "   This might mean:" -ForegroundColor Gray
    Write-Host "   - Profile hasn't loaded yet" -ForegroundColor Gray
    Write-Host "   - User not logged in" -ForegroundColor Gray
    Write-Host "   - Access token missing" -ForegroundColor Gray
}

Write-Host ""
Write-Host "Step 6: Checking progress data..." -ForegroundColor Yellow
$progressLogs = adb logcat -d | Select-String "Module.*completed.*score.*questions=" | Select-Object -Last 10

if ($progressLogs) {
    Write-Host "✅ Module progress data:" -ForegroundColor Green
    $progressLogs | ForEach-Object { Write-Host "   $_" -ForegroundColor Cyan }
} else {
    Write-Host "⚠️ No progress data found" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Step 7: Checking ProfileViewModel state..." -ForegroundColor Yellow
$profileLogs = adb logcat -d | Select-String "ProfileViewModel" | Select-Object -Last 15

if ($profileLogs) {
    Write-Host "✅ ProfileViewModel logs:" -ForegroundColor Green
    $profileLogs | ForEach-Object { Write-Host "   $_" -ForegroundColor Gray }
} else {
    Write-Host "⚠️ No ProfileViewModel logs" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "================================" -ForegroundColor Cyan
Write-Host "Test Complete!" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Summary:" -ForegroundColor Yellow
Write-Host "- Check if question counts are from database (not all 10)" -ForegroundColor White
Write-Host "- Check if module progress shows completion status and scores" -ForegroundColor White
Write-Host "- If still showing zeros, user might not be logged in" -ForegroundColor White
Write-Host ""
Write-Host "To test quiz completion:" -ForegroundColor Yellow
Write-Host "1. Complete a quiz in the app" -ForegroundColor White
Write-Host "2. Return to profile" -ForegroundColor White
Write-Host "3. Check logs for UPSERT successful" -ForegroundColor White
Write-Host "4. Verify profile refreshed with new scores" -ForegroundColor White
