# E.S.C.A.P.E. AR - Quick Vercel CLI Deployment
# This script helps you deploy web-admin to Vercel using CLI

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "E.S.C.A.P.E. AR - Vercel CLI Deployment" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if Vercel CLI is installed
Write-Host "Checking Vercel CLI..." -ForegroundColor Yellow
$vercelInstalled = Get-Command vercel -ErrorAction SilentlyContinue

if (-not $vercelInstalled) {
    Write-Host "❌ Vercel CLI not found" -ForegroundColor Red
    Write-Host ""
    Write-Host "Installing Vercel CLI..." -ForegroundColor Yellow
    npm install -g vercel
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Failed to install Vercel CLI" -ForegroundColor Red
        Write-Host "Please run manually: npm install -g vercel" -ForegroundColor Yellow
        exit 1
    }
    Write-Host "✅ Vercel CLI installed!" -ForegroundColor Green
} else {
    Write-Host "✅ Vercel CLI already installed" -ForegroundColor Green
}

Write-Host ""

# Navigate to web-admin
Write-Host "Navigating to web-admin folder..." -ForegroundColor Yellow
Set-Location "C:\Users\Jared Dionela\AndroidStudioProjects\ESCAPEAR\web-admin"

# Test build
Write-Host "Testing build..." -ForegroundColor Yellow
npm run build

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Build failed! Fix errors before deploying." -ForegroundColor Red
    exit 1
}

Write-Host "✅ Build successful!" -ForegroundColor Green
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "READY TO DEPLOY!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Next steps:" -ForegroundColor White
Write-Host ""
Write-Host "1. Login to Vercel (if not already):" -ForegroundColor Yellow
Write-Host "   vercel login" -ForegroundColor Gray
Write-Host ""
Write-Host "2. Deploy to Vercel:" -ForegroundColor Yellow
Write-Host "   vercel" -ForegroundColor Gray
Write-Host ""
Write-Host "3. Add environment variables (first time only):" -ForegroundColor Yellow
Write-Host "   vercel env add VITE_SUPABASE_URL production" -ForegroundColor Gray
Write-Host "   vercel env add VITE_SUPABASE_ANON_KEY production" -ForegroundColor Gray
Write-Host ""
Write-Host "4. Deploy to production:" -ForegroundColor Yellow
Write-Host "   vercel --prod" -ForegroundColor Gray
Write-Host ""

Write-Host "Or run this all-in-one command:" -ForegroundColor Cyan
Write-Host "vercel --prod" -ForegroundColor White
Write-Host ""

$answer = Read-Host "Do you want to deploy now? (y/n)"

if ($answer -eq 'y' -or $answer -eq 'Y') {
    Write-Host ""
    Write-Host "Deploying to Vercel..." -ForegroundColor Yellow
    vercel --prod
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Green
        Write-Host "✅ DEPLOYMENT SUCCESSFUL!" -ForegroundColor Green
        Write-Host "========================================" -ForegroundColor Green
        Write-Host ""
        Write-Host "IMPORTANT: Update Supabase redirect URLs" -ForegroundColor Yellow
        Write-Host "1. Go to https://app.supabase.com" -ForegroundColor White
        Write-Host "2. Your Project → Authentication → URL Configuration" -ForegroundColor White
        Write-Host "3. Add your Vercel URL to Redirect URLs" -ForegroundColor White
        Write-Host ""
    } else {
        Write-Host ""
        Write-Host "❌ Deployment failed" -ForegroundColor Red
        Write-Host "Check the error messages above" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "Common issues:" -ForegroundColor Yellow
        Write-Host "- Not logged in: Run 'vercel login'" -ForegroundColor Gray
        Write-Host "- Environment variables missing: Add them first" -ForegroundColor Gray
        Write-Host "- Build errors: Check npm run build output" -ForegroundColor Gray
    }
} else {
    Write-Host ""
    Write-Host "Deployment cancelled." -ForegroundColor Yellow
    Write-Host "Run 'vercel --prod' manually when ready." -ForegroundColor White
}
