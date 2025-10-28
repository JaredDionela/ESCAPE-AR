# Fix Vercel Environment Variables
Write-Host "Removing existing environment variables..." -ForegroundColor Yellow

# Remove existing variables
vercel env rm VITE_SUPABASE_URL production --yes 2>&1 | Out-Null
vercel env rm VITE_SUPABASE_ANON_KEY production --yes 2>&1 | Out-Null

Write-Host "Adding environment variables..." -ForegroundColor Green

# Add VITE_SUPABASE_URL
$url = "https://iixfznklvvydfqouwzuh.supabase.co"
Write-Host "Setting VITE_SUPABASE_URL..." -ForegroundColor Cyan
echo $url | vercel env add VITE_SUPABASE_URL production

# Add VITE_SUPABASE_ANON_KEY
$key = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImlpeGZ6bmtsdnZ5ZGZxb3V3enVoIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTQ3NTI3MDUsImV4cCI6MjA3MDMyODcwNX0.wPA_dHaBNzxW_TSIhcizYGSiSISMTnbii0O0q7pYZFI"
Write-Host "Setting VITE_SUPABASE_ANON_KEY..." -ForegroundColor Cyan
echo $key | vercel env add VITE_SUPABASE_ANON_KEY production

Write-Host "`nEnvironment variables updated!" -ForegroundColor Green
Write-Host "Now deploying to production..." -ForegroundColor Yellow

# Redeploy
vercel --prod --yes

Write-Host "`nDeployment complete!" -ForegroundColor Green
