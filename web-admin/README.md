# E.S.C.A.P.E. AR - Web Admin Panel

Web admin panel for managing lessons, quizzes, users, and analytics for the E.S.C.A.P.E. AR learning platform.

## 🚀 Quick Start

### 1. Install Dependencies

```powershell
cd web-admin
npm install
```

### 2. Configure Environment Variables

Edit `.env.local` and add your Supabase credentials:

```env
VITE_SUPABASE_URL=https://your-project.supabase.co
VITE_SUPABASE_ANON_KEY=your-anon-key
```

Get these from your Supabase project settings (same as Android app).

### 3. Run Development Server

```powershell
npm run dev
```

The admin panel will open at `http://localhost:3000`

## 📁 Project Structure

```
web-admin/
├── src/
│   ├── components/
│   │   ├── layout/         # Layout components (Sidebar, Header)
│   │   ├── lessons/        # Lesson management components
│   │   └── auth/           # Authentication components
│   ├── lib/
│   │   ├── supabase.ts     # Supabase client
│   │   └── api/            # API functions
│   ├── pages/              # Page components
│   ├── types/              # TypeScript types
│   ├── App.tsx             # Main app component
│   └── main.tsx            # Entry point
├── .env.local              # Environment variables
└── package.json
```

## 🔧 Available Scripts

- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run preview` - Preview production build
- `npm run lint` - Run ESLint

## ✨ Features

- ✅ Dashboard with statistics
- ✅ Lessons management (view, create, edit, delete)
- 🔄 Quiz management (coming soon)
- 🔄 User management (coming soon)
- 🔄 Analytics dashboard (coming soon)
- 🔄 File uploads (coming soon)

## 📚 Tech Stack

- **React 18** + **TypeScript**
- **Vite** - Fast build tool
- **Material-UI** - UI components
- **React Router** - Routing
- **TanStack Query** - Data fetching
- **Supabase** - Backend

## 🌐 Deployment

When ready to deploy:

```powershell
npm run build
```

Deploy the `dist/` folder to:
- **Vercel** (recommended)
- **Netlify**
- Any static hosting service

## 🔗 Related

- Android App: `../app/`
- Database Migrations: `../supabase/migrations/`
- Documentation: `../LESSONS_AND_WEB_ADMIN_GUIDE.md`
