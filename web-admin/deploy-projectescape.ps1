# Deploy to Vercel with project name "projectescape"
# Make sure you have Vercel CLI installed: npm install -g vercel

Write-Host "Deploying to Vercel with project name: projectescape" -ForegroundColor Green

# Navigate to web-admin directory
Set-Location $PSScriptRoot

# Deploy to Vercel
vercel --prod --name projectescape --yes

Write-Host "`nDeployment complete!" -ForegroundColor Green
Write-Host "Your site should be available at: https://projectescape.vercel.app" -ForegroundColor Cyan
