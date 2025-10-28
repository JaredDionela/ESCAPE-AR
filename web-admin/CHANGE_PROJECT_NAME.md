# Change Vercel Project Name to "projectescape-admin"

## Steps:

1. **Go to Project Settings**
   - Visit: https://vercel.com/reds-projects-ebb298b0/web-admin/settings

2. **Scroll down to "Project Name" section**

3. **Change the name:**
   - Current name: `web-admin`
   - New name: `projectescape-admin`
   - Click "Save"

4. **Your new URL will be:**
   - Production: `https://projectescape-admin.vercel.app`
   - Or: `https://projectescape-admin-[unique-id].vercel.app`

## Note:
- The URL will automatically update after you save the new project name
- All existing deployments will continue to work
- The project settings and environment variables will be preserved

---

## Also Fix Environment Variables (if site is still blank):

While you're in the settings, also fix the environment variables:

1. **Go to Environment Variables:**
   - Visit: https://vercel.com/reds-projects-ebb298b0/web-admin/settings/environment-variables

2. **Delete existing variables** (they might be empty):
   - Delete `VITE_SUPABASE_URL`
   - Delete `VITE_SUPABASE_ANON_KEY`

3. **Add them again:**

   **Variable 1:**
   - Name: `VITE_SUPABASE_URL`
   - Value: `https://iixfznklvvydfqouwzuh.supabase.co`
   - Environment: ✓ Production
   - Click "Add"

   **Variable 2:**
   - Name: `VITE_SUPABASE_ANON_KEY`
   - Value: `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImlpeGZ6bmtsdnZ5ZGZxb3V3enVoIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTQ3NTI3MDUsImV4cCI6MjA3MDMyODcwNX0.wPA_dHaBNzxW_TSIhcizYGSiSISMTnbii0O0q7pYZFI`
   - Environment: ✓ Production
   - Click "Add"

4. **Vercel will automatically redeploy** with the correct environment variables

---

## After Everything is Set Up:

Don't forget to update Supabase redirect URLs:

1. Go to: https://app.supabase.com/project/iixfznklvvydfqouwzuh/auth/url-configuration
2. Add the new URL: `https://projectescape-admin.vercel.app/**`
3. Click "Save"
