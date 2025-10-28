# ✅ SIMPLIFIED VERCEL DEPLOYMENT - No Root Directory Needed!

## Problem Solved
Can't edit root directory in Vercel? **No problem!** Use Vercel CLI instead.

## ⚡ Quick Deploy (5 Minutes)

### Step 1: Install Vercel CLI
Open PowerShell and run:
```powershell
npm install -g vercel
```

### Step 2: Login to Vercel
```powershell
vercel login
```
This will open your browser. Sign in with GitHub.

### Step 3: Navigate to web-admin
```powershell
cd "C:\Users\Jared Dionela\AndroidStudioProjects\ESCAPEAR\web-admin"
```

### Step 4: Deploy!
```powershell
vercel --prod
```

That's it! Your site will be live in 2-3 minutes! 🎉

### Step 5: Add Environment Variables (First Time Only)
If prompted or if login fails after deployment:

```powershell
vercel env add VITE_SUPABASE_URL production
```
Paste: `https://iixfznklvvydfqouwzuh.supabase.co`

```powershell
vercel env add VITE_SUPABASE_ANON_KEY production
```
Paste: `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImlpeGZ6bmtsdnZ5ZGZxb3V3enVoIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTQ3NTI3MDUsImV4cCI6MjA3MDMyODcwNX0.wPA_dHaBNzxW_TSIhcizYGSiSISMTnbii0O0q7pYZFI`

Then redeploy:
```powershell
vercel --prod
```

### Step 6: Update Supabase
After deployment, you'll get a URL like: `https://escape-ar-admin.vercel.app`

Add it to Supabase:
1. Go to https://app.supabase.com
2. Your Project → Authentication → URL Configuration
3. Add to Redirect URLs:
   - `https://your-url.vercel.app`
   - `https://your-url.vercel.app/auth/callback`

## 🤖 Automated Script

Or just run this script that does everything:
```powershell
.\deploy-vercel-cli.ps1
```

## 🔄 Future Deployments

Whenever you make changes:
```powershell
cd "C:\Users\Jared Dionela\AndroidStudioProjects\ESCAPEAR\web-admin"
vercel --prod
```

## ✅ Advantages of Vercel CLI

- ✅ No need to change root directory
- ✅ Deploy directly from web-admin folder
- ✅ Faster than using dashboard
- ✅ Works with any repository structure
- ✅ One command to deploy: `vercel --prod`

## 📝 Commands Reference

| Command | Description |
|---------|-------------|
| `vercel login` | Login to Vercel |
| `vercel` | Deploy to preview |
| `vercel --prod` | Deploy to production |
| `vercel ls` | List deployments |
| `vercel logs` | View logs |
| `vercel env ls` | List environment variables |
| `vercel env add NAME` | Add environment variable |
| `vercel alias` | Set custom domain |

## 🆘 Troubleshooting

### "Command not found: vercel"
Install Vercel CLI:
```powershell
npm install -g vercel
```

### "Not authorized"
Login first:
```powershell
vercel login
```

### "Build failed"
Test build locally:
```powershell
npm run build
```

### Login doesn't work after deployment
Add environment variables:
```powershell
vercel env add VITE_SUPABASE_URL production
vercel env add VITE_SUPABASE_ANON_KEY production
vercel --prod
```

## 🎯 Summary

**Instead of using Vercel Dashboard** (which requires root directory selection):
- ✅ Use Vercel CLI
- ✅ Deploy from web-admin folder directly
- ✅ One command: `vercel --prod`

**That's it!** No complicated setup, no repository restructuring needed! 🚀
