# Unity AR Game Update Script for ESCAPE AR
# Automatically updates Unity game from PROJECTESCAPE export

param([switch]$SkipConfirmation)

$ErrorActionPreference = "Stop"

# Paths
$source = "C:\Users\Jared Dionela\AndroidStudioProjects\PROJECTESCAPE\unityLibrary"
$dest = "C:\Users\Jared Dionela\AndroidStudioProjects\ESCAPEAR\unityLibrary"
$projectRoot = "C:\Users\Jared Dionela\AndroidStudioProjects\ESCAPEAR"

Write-Host ""
Write-Host "========================================"
Write-Host "  ESCAPE AR - Unity Game Updater"
Write-Host "========================================"
Write-Host ""

# Verify source exists
if (-not (Test-Path $source)) {
    Write-Host "ERROR: Source Unity export not found at: $source" -ForegroundColor Red
    exit 1
}

Write-Host "Source: $source"
Write-Host "Destination: $dest"
Write-Host ""

# Ask for confirmation
if (-not $SkipConfirmation) {
    $confirmation = Read-Host "This will replace the current Unity game. Continue? (yes/no)"
    if ($confirmation -ne "yes") {
        Write-Host "Update cancelled." -ForegroundColor Yellow
        exit 0
    }
}

try {
    # Step 1: Create backup
    Write-Host ""
    Write-Host "[1/6] Creating backup..." -ForegroundColor Yellow
    $timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
    $backupPath = "C:\Users\Jared Dionela\AndroidStudioProjects\ESCAPEAR_BACKUP_$timestamp"
    
    if (-not (Test-Path $backupPath)) {
        Copy-Item -Recurse $projectRoot $backupPath -ErrorAction Stop
        Write-Host "Backup created: $backupPath" -ForegroundColor Green
    }

    # Step 2: Clean old builds
    Write-Host ""
    Write-Host "[2/6] Cleaning old builds..." -ForegroundColor Yellow
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
    Write-Host "Builds cleaned" -ForegroundColor Green

    # Step 3: Replace Unity Library Assets
    Write-Host ""
    Write-Host "[3/6] Replacing Unity game assets..." -ForegroundColor Yellow
    
    # Replace src folder
    if (Test-Path "$dest\unityLibrary\src") {
        Remove-Item -Recurse -Force "$dest\unityLibrary\src"
    }
    Copy-Item -Recurse "$source\unityLibrary\src" "$dest\unityLibrary\src"
    Write-Host "  Game assets updated" -ForegroundColor Green

    # Replace symbols (native libs)
    if (Test-Path "$dest\unityLibrary\symbols") {
        Remove-Item -Recurse -Force "$dest\unityLibrary\symbols"
    }
    if (Test-Path "$source\symbols") {
        Copy-Item -Recurse "$source\symbols" "$dest\unityLibrary\symbols"
        Write-Host "  Native libraries updated" -ForegroundColor Green
    }

    # Step 4: Update libs if present
    Write-Host ""
    Write-Host "[4/6] Updating AAR libraries..." -ForegroundColor Yellow
    if (Test-Path "$source\libs") {
        if (Test-Path "$dest\unityLibrary\libs") {
            Remove-Item -Recurse -Force "$dest\unityLibrary\libs"
        }
        Copy-Item -Recurse "$source\libs" "$dest\unityLibrary\libs"
        Write-Host "AAR libraries updated" -ForegroundColor Green
    } else {
        Write-Host "No libs folder in source (skipping)" -ForegroundColor Gray
    }

    # Step 5: Update AR manifest
    Write-Host ""
    Write-Host "[5/6] Updating AR configuration..." -ForegroundColor Yellow
    if (Test-Path "$source\xrmanifest.androidlib") {
        if (Test-Path "$dest\xrmanifest.androidlib") {
            Remove-Item -Recurse -Force "$dest\xrmanifest.androidlib"
        }
        Copy-Item -Recurse "$source\xrmanifest.androidlib" "$dest\xrmanifest.androidlib"
        Write-Host "AR configuration updated" -ForegroundColor Green
    } else {
        Write-Host "No xrmanifest in source (skipping)" -ForegroundColor Gray
    }

    # Step 6: Build APK
    Write-Host ""
    Write-Host "[6/6] Building new APK..." -ForegroundColor Yellow
    Write-Host "  (This may take a few minutes...)"
    Write-Host ""
    
    & ./gradlew assembleDebug
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "========================================"
        Write-Host "  Unity AR Game Updated Successfully!"
        Write-Host "========================================" -ForegroundColor Green
        Write-Host ""
        
        $apkPath = "$projectRoot\app\build\outputs\apk\debug\app-debug.apk"
        if (Test-Path $apkPath) {
            $apkSize = [math]::Round((Get-Item $apkPath).Length / 1MB, 2)
            Write-Host "APK Location: $apkPath" -ForegroundColor Cyan
            Write-Host "APK Size: $apkSize MB" -ForegroundColor Cyan
            Write-Host ""
            
            Write-Host "Next steps:" -ForegroundColor Yellow
            Write-Host "1. Install on device: ./gradlew installDebug"
            Write-Host "2. Test AR functionality thoroughly"
            Write-Host "3. Verify quiz and authentication still work"
            Write-Host "4. Upload to Google Drive for phone testing"
            Write-Host ""
        }
    } else {
        throw "Build failed with exit code $LASTEXITCODE"
    }

} catch {
    Write-Host ""
    Write-Host "ERROR during update: $_" -ForegroundColor Red
    Write-Host ""
    Write-Host "To restore backup, run:" -ForegroundColor Yellow
    Write-Host "  Remove-Item -Recurse -Force '$projectRoot'"
    Write-Host "  Move-Item '$backupPath' '$projectRoot'"
    Write-Host ""
    exit 1
}

Write-Host ""
Write-Host "Update complete! Test the AR features." -ForegroundColor Cyan
Write-Host ""
