# Deploy web-admin as Standalone Repository to Vercel

## Problem
Cannot select root directory in Vercel when importing from main ESCAPE-AR repository.

## Solution: Create a separate deployment branch

### Step 1: Create a deployment branch with only web-admin content

```powershell
# Navigate to your project
cd "C:\Users\Jared Dionela\AndroidStudioProjects\ESCAPEAR"

# Create a new branch for deployment
git checkout --orphan vercel-deploy

# Remove all files from staging
git rm -rf .

# Copy only web-admin files to root
Copy-Item -Path "web-admin\*" -Destination "." -Recurse -Force

# Add and commit
git add .
git commit -m "Deploy: web-admin only for Vercel"

# Push to GitHub
git push origin vercel-deploy
```

### Step 2: Deploy on Vercel

1. Go to https://vercel.com
2. Sign in with GitHub
3. Click "Add New..." → "Project"
4. Select "ESCAPE-AR" repository
5. **Select branch**: Choose `vercel-deploy` (not clean-dev)
6. Root directory will be automatically correct (root of vercel-deploy branch)
7. Add environment variables:
   ```
   VITE_SUPABASE_URL=https://iixfznklvvydfqouwzuh.supabase.co
   VITE_SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImlpeGZ6bmtsdnZ5ZGZxb3V3enVoIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTQ3NTI3MDUsImV4cCI6MjA3MDMyODcwNX0.wPA_dHaBNzxW_TSIhcizYGSiSISMTnbii0O0q7pYZFI
   ```
8. Click "Deploy"

### Step 3: Keep deployment branch updated

Whenever you make changes to web-admin:

```powershell
# Make changes in clean-dev branch as usual
git checkout clean-dev
# ... make your changes ...
git add .
git commit -m "Your changes"
git push origin clean-dev

# Update deployment branch
git checkout vercel-deploy
git rm -rf .
git checkout clean-dev -- web-admin
Copy-Item -Path "web-admin\*" -Destination "." -Recurse -Force
git add .
git commit -m "Update deployment from clean-dev"
git push origin vercel-deploy
```

Vercel will automatically redeploy when you push to `vercel-deploy` branch.

---

## Alternative: Use Vercel CLI (Easier!)

This is simpler and doesn't require branch management:

### Step 1: Install Vercel CLI
```powershell
npm install -g vercel
```

### Step 2: Login
```powershell
vercel login
```

### Step 3: Deploy from web-admin folder
```powershell
cd "C:\Users\Jared Dionela\AndroidStudioProjects\ESCAPEAR\web-admin"
vercel
```

Follow the prompts:
- Set up and deploy? **Yes**
- Which scope? (Select your account)
- Link to existing project? **No**
- Project name? **escape-ar-admin**
- In which directory is your code? **./** (current)
- Override settings? **No**

### Step 4: Add environment variables
```powershell
vercel env add VITE_SUPABASE_URL production
# Paste: https://iixfznklvvydfqouwzuh.supabase.co

vercel env add VITE_SUPABASE_ANON_KEY production
# Paste: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Step 5: Deploy to production
```powershell
vercel --prod
```

### Step 6: Future deployments (just run from web-admin folder)
```powershell
cd "C:\Users\Jared Dionela\AndroidStudioProjects\ESCAPEAR\web-admin"
vercel --prod
```

---

## Recommendation: Use Vercel CLI (Option 2)

The Vercel CLI approach is:
- ✅ Simpler - no branch management
- ✅ Faster - deploy directly from web-admin folder
- ✅ Flexible - deploy anytime with one command
- ✅ Works regardless of repository structure

Just run `vercel --prod` from the web-admin folder whenever you want to deploy!
