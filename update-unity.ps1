# Unity AR Game Update Script for ESCAPE AR
# This script automatically updates the Unity game from PROJECTESCAPE export

$ErrorActionPreference = "Stop"

# Paths
$source = "C:\Users\Jared Dionela\AndroidStudioProjects\PROJECTESCAPE\unityLibrary"
$dest = "C:\Users\Jared Dionela\AndroidStudioProjects\ESCAPEAR\unityLibrary"
$projectRoot = "C:\Users\Jared Dionela\AndroidStudioProjects\ESCAPEAR"

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  ESCAPE AR - Unity Game Updater" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Verify source exists
if (-not (Test-Path $source)) {
    Write-Host "❌ Error: Source Unity export not found at: $source" -ForegroundColor Red
    exit 1
}

Write-Host "📂 Source: $source" -ForegroundColor Gray
Write-Host "📂 Destination: $dest`n" -ForegroundColor Gray

# Ask for confirmation
$confirmation = Read-Host "This will replace the current Unity game. Continue? (yes/no)"
if ($confirmation -ne "yes") {
    Write-Host "❌ Update cancelled." -ForegroundColor Yellow
    exit 0
}

try {
    # Step 1: Create backup
    Write-Host "`n[1/6] 💾 Creating backup..." -ForegroundColor Yellow
    $timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
    $backupPath = "C:\Users\Jared Dionela\AndroidStudioProjects\ESCAPEAR_BACKUP_$timestamp"
    
    if (-not (Test-Path $backupPath)) {
        Copy-Item -Recurse $projectRoot $backupPath -ErrorAction Stop
        Write-Host "✅ Backup created: $backupPath" -ForegroundColor Green
    }

    # Step 2: Clean old builds
    Write-Host "`n[2/6] 🧹 Cleaning old builds..." -ForegroundColor Yellow
    Set-Location $projectRoot
    
    & ./gradlew clean 2>&1 | Out-Null
    
    if (Test-Path "$dest\.gradle") {
        Remove-Item -Recurse -Force "$dest\.gradle"
    }
    if (Test-Path "$dest\build") {
        Remove-Item -Recurse -Force "$dest\build"
    }
    if (Test-Path "$dest\.cxx") {
        Remove-Item -Recurse -Force "$dest\.cxx"
    }
    Write-Host "✅ Builds cleaned" -ForegroundColor Green

    # Step 3: Replace Unity Library Assets
    Write-Host "`n[3/6] 🎮 Replacing Unity game assets..." -ForegroundColor Yellow
    
    # Replace src folder
    if (Test-Path "$dest\unityLibrary\src") {
        Remove-Item -Recurse -Force "$dest\unityLibrary\src"
    }
    Copy-Item -Recurse "$source\unityLibrary\src" "$dest\unityLibrary\src"
    Write-Host "  ✓ Game assets updated" -ForegroundColor Green

    # Replace symbols (native libs)
    if (Test-Path "$dest\unityLibrary\symbols") {
        Remove-Item -Recurse -Force "$dest\unityLibrary\symbols"
    }
    if (Test-Path "$source\symbols") {
        Copy-Item -Recurse "$source\symbols" "$dest\unityLibrary\symbols"
        Write-Host "  ✓ Native libraries updated" -ForegroundColor Green
    }

    # Step 4: Update libs if present
    Write-Host "`n[4/6] 📦 Updating AAR libraries..." -ForegroundColor Yellow
    if (Test-Path "$source\libs") {
        if (Test-Path "$dest\unityLibrary\libs") {
            Remove-Item -Recurse -Force "$dest\unityLibrary\libs"
        }
        Copy-Item -Recurse "$source\libs" "$dest\unityLibrary\libs"
        Write-Host "✅ AAR libraries updated" -ForegroundColor Green
    } else {
        Write-Host "ℹ️  No libs folder in source (skipping)" -ForegroundColor Gray
    }

    # Step 5: Update AR manifest
    Write-Host "`n[5/6] 🥽 Updating AR configuration..." -ForegroundColor Yellow
    if (Test-Path "$source\xrmanifest.androidlib") {
        if (Test-Path "$dest\xrmanifest.androidlib") {
            Remove-Item -Recurse -Force "$dest\xrmanifest.androidlib"
        }
        Copy-Item -Recurse "$source\xrmanifest.androidlib" "$dest\xrmanifest.androidlib"
        Write-Host "✅ AR configuration updated" -ForegroundColor Green
    } else {
        Write-Host "ℹ️  No xrmanifest in source (skipping)" -ForegroundColor Gray
    }

    # Step 6: Build APK
    Write-Host "`n[6/6] 🔨 Building new APK..." -ForegroundColor Yellow
    Write-Host "  (This may take a few minutes...)`n" -ForegroundColor Gray
    
    & ./gradlew assembleDebug
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "`n========================================" -ForegroundColor Green
        Write-Host "  ✅ Unity AR Game Updated Successfully!" -ForegroundColor Green
        Write-Host "========================================`n" -ForegroundColor Green
        
        $apkPath = "$projectRoot\app\build\outputs\apk\debug\app-debug.apk"
        if (Test-Path $apkPath) {
            $apkSize = [math]::Round((Get-Item $apkPath).Length / 1MB, 2)
            Write-Host "📱 APK Location: $apkPath" -ForegroundColor Cyan
            Write-Host "📦 APK Size: $apkSize MB`n" -ForegroundColor Cyan
            
            Write-Host "Next steps:" -ForegroundColor Yellow
            Write-Host "1. Install on device: ./gradlew installDebug" -ForegroundColor White
            Write-Host "2. Test AR functionality thoroughly" -ForegroundColor White
            Write-Host "3. Verify quiz and authentication still work" -ForegroundColor White
            Write-Host "4. Upload to Google Drive for phone testing`n" -ForegroundColor White
        }
    } else {
        throw "Build failed with exit code $LASTEXITCODE"
    }

} catch {
    Write-Host "`n❌ Error during update: $_" -ForegroundColor Red
    Write-Host "`n💡 To restore backup, run:" -ForegroundColor Yellow
    Write-Host "   Remove-Item -Recurse -Force '$projectRoot'" -ForegroundColor Gray
    Write-Host "   Move-Item '$backupPath' '$projectRoot'" -ForegroundColor Gray
    exit 1
}

Write-Host ""
Write-Host "Update complete! Test the AR features." -ForegroundColor Cyan
Write-Host ""
