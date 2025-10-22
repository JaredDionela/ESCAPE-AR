# Quick Vercel Deployment Script
# Run this to prepare and deploy your web-admin to Vercel

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "E.S.C.A.P.E. AR - Vercel Deployment" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Navigate to web-admin directory
Set-Location "C:\Users\Jared Dionela\AndroidStudioProjects\ESCAPEAR\web-admin"

Write-Host "Step 1: Testing local build..." -ForegroundColor Yellow
npm run build

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Build failed! Fix errors before deploying." -ForegroundColor Red
    exit 1
}

Write-Host "✅ Build successful!" -ForegroundColor Green
Write-Host ""

Write-Host "Step 2: Checking git status..." -ForegroundColor Yellow
Set-Location ".."
git status

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "NEXT STEPS:" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Commit your changes:" -ForegroundColor White
Write-Host "   git add ." -ForegroundColor Gray
Write-Host "   git commit -m 'Add Vercel deployment configuration'" -ForegroundColor Gray
Write-Host "   git push origin clean-dev" -ForegroundColor Gray
Write-Host ""
Write-Host "2. Deploy to Vercel:" -ForegroundColor White
Write-Host "   Option A: Go to https://vercel.com and import your GitHub repo" -ForegroundColor Gray
Write-Host "   Option B: Run 'vercel' command if you have Vercel CLI" -ForegroundColor Gray
Write-Host ""
Write-Host "3. Add these environment variables in Vercel Dashboard:" -ForegroundColor White
Write-Host "   VITE_SUPABASE_URL=https://iixfznklvvydfqouwzuh.supabase.co" -ForegroundColor Gray
Write-Host "   VITE_SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." -ForegroundColor Gray
Write-Host ""
Write-Host "4. Configure in Vercel:" -ForegroundColor White
Write-Host "   - Root Directory: Select 'web-admin' folder" -ForegroundColor Gray
Write-Host "   - Framework: Vite (auto-detected)" -ForegroundColor Gray
Write-Host "   - Build Command: npm run build" -ForegroundColor Gray
Write-Host "   - Output Directory: dist" -ForegroundColor Gray
Write-Host ""
Write-Host "5. After deployment, update Supabase:" -ForegroundColor White
Write-Host "   - Go to app.supabase.com → Your Project → Authentication" -ForegroundColor Gray
Write-Host "   - Add your Vercel URL to Redirect URLs" -ForegroundColor Gray
Write-Host "   - Example: https://your-project.vercel.app" -ForegroundColor Gray
Write-Host ""
Write-Host "📖 Full guide: web-admin/VERCEL_DEPLOYMENT_GUIDE.md" -ForegroundColor Cyan
Write-Host ""
Write-Host "Ready to deploy? (See guide for detailed steps)" -ForegroundColor Green
