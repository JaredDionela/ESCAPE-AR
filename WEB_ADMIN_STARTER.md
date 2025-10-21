# 🌐 Web Admin Panel - Quick Start Template

## 📋 When You're Ready to Build the Web Admin

This file contains starter code to help you begin building the web admin panel.

---

## 🚀 Step 1: Create Project

```powershell
# Create React + TypeScript project
npm create vite@latest escape-ar-admin -- --template react-ts

# Navigate to project
cd escape-ar-admin

# Install dependencies
npm install

# Install Supabase
npm install @supabase/supabase-js

# Install UI library (choose one)
npm install @mui/material @mui/icons-material @emotion/react @emotion/styled
# OR
npx shadcn-ui@latest init

# Install other dependencies
npm install react-router-dom @tanstack/react-query recharts react-dropzone
```

---

## 🔐 Step 2: Setup Environment Variables

Create `.env.local`:
```env
VITE_SUPABASE_URL=your_supabase_project_url
VITE_SUPABASE_ANON_KEY=your_supabase_anon_key
```

Get these from your Supabase project settings (same as Android app).

---

## 📁 Step 3: Create Supabase Client

**src/lib/supabase.ts:**
```typescript
import { createClient } from '@supabase/supabase-js'

const supabaseUrl = import.meta.env.VITE_SUPABASE_URL
const supabaseAnonKey = import.meta.env.VITE_SUPABASE_ANON_KEY

if (!supabaseUrl || !supabaseAnonKey) {
  throw new Error('Missing Supabase environment variables')
}

export const supabase = createClient(supabaseUrl, supabaseAnonKey)
```

---

## 🎨 Step 4: Create Basic Layout

**src/components/layout/AdminLayout.tsx:**
```typescript
import { ReactNode } from 'react'
import { Link, Outlet } from 'react-router-dom'

export function AdminLayout() {
  return (
    <div style={{ display: 'flex', minHeight: '100vh' }}>
      {/* Sidebar */}
      <aside style={{ width: '250px', background: '#1a1a1a', color: 'white', padding: '20px' }}>
        <h2>E.S.C.A.P.E. Admin</h2>
        <nav style={{ marginTop: '40px' }}>
          <ul style={{ listStyle: 'none', padding: 0 }}>
            <li style={{ marginBottom: '15px' }}>
              <Link to="/dashboard" style={{ color: 'white', textDecoration: 'none' }}>
                📊 Dashboard
              </Link>
            </li>
            <li style={{ marginBottom: '15px' }}>
              <Link to="/lessons" style={{ color: 'white', textDecoration: 'none' }}>
                📚 Lessons
              </Link>
            </li>
            <li style={{ marginBottom: '15px' }}>
              <Link to="/quiz" style={{ color: 'white', textDecoration: 'none' }}>
                📝 Quizzes
              </Link>
            </li>
            <li style={{ marginBottom: '15px' }}>
              <Link to="/users" style={{ color: 'white', textDecoration: 'none' }}>
                👥 Users
              </Link>
            </li>
            <li style={{ marginBottom: '15px' }}>
              <Link to="/analytics" style={{ color: 'white', textDecoration: 'none' }}>
                📈 Analytics
              </Link>
            </li>
          </ul>
        </nav>
      </aside>

      {/* Main Content */}
      <main style={{ flex: 1, padding: '40px', background: '#f5f5f5' }}>
        <Outlet />
      </main>
    </div>
  )
}
```

---

## 🔄 Step 5: Setup Router

**src/App.tsx:**
```typescript
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AdminLayout } from './components/layout/AdminLayout'
import { Dashboard } from './pages/Dashboard'
import { Lessons } from './pages/Lessons'
import { Login } from './pages/Login'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/" element={<AdminLayout />}>
          <Route index element={<Navigate to="/dashboard" replace />} />
          <Route path="dashboard" element={<Dashboard />} />
          <Route path="lessons" element={<Lessons />} />
          {/* Add more routes */}
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default App
```

---

## 🎯 Step 6: Create Lessons Management Page

**src/pages/Lessons.tsx:**
```typescript
import { useEffect, useState } from 'react'
import { supabase } from '../lib/supabase'

interface Lesson {
  id: string
  module_id: string
  title: string
  description: string
  youtube_video_id: string
  duration_minutes: number
}

export function Lessons() {
  const [lessons, setLessons] = useState<Lesson[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetchLessons()
  }, [])

  async function fetchLessons() {
    try {
      const { data, error } = await supabase
        .from('lessons')
        .select('*')
        .order('module_id')
        .order('order_index')

      if (error) throw error
      setLessons(data || [])
    } catch (error) {
      console.error('Error fetching lessons:', error)
    } finally {
      setLoading(false)
    }
  }

  async function deleteLesson(id: string) {
    if (!confirm('Delete this lesson?')) return

    try {
      const { error } = await supabase
        .from('lessons')
        .delete()
        .eq('id', id)

      if (error) throw error
      fetchLessons() // Refresh list
    } catch (error) {
      console.error('Error deleting lesson:', error)
    }
  }

  if (loading) return <div>Loading...</div>

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '30px' }}>
        <h1>Lessons Management</h1>
        <button
          style={{
            padding: '10px 20px',
            background: '#00bcd4',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer'
          }}
          onClick={() => alert('Create lesson form - TODO')}
        >
          + New Lesson
        </button>
      </div>

      <div style={{ background: 'white', borderRadius: '8px', padding: '20px' }}>
        {lessons.length === 0 ? (
          <p>No lessons yet. Create your first lesson!</p>
        ) : (
          lessons.map(lesson => (
            <div
              key={lesson.id}
              style={{
                padding: '20px',
                borderBottom: '1px solid #eee',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center'
              }}
            >
              <div>
                <h3 style={{ margin: '0 0 8px 0' }}>{lesson.title}</h3>
                <p style={{ margin: '0', color: '#666' }}>{lesson.description}</p>
                <small style={{ color: '#999' }}>
                  📺 {lesson.youtube_video_id} • ⏱️ {lesson.duration_minutes} min • 
                  🏷️ {lesson.module_id}
                </small>
              </div>
              <div>
                <button
                  style={{
                    padding: '8px 16px',
                    marginRight: '8px',
                    background: '#4caf50',
                    color: 'white',
                    border: 'none',
                    borderRadius: '4px',
                    cursor: 'pointer'
                  }}
                  onClick={() => alert('Edit lesson - TODO')}
                >
                  Edit
                </button>
                <button
                  style={{
                    padding: '8px 16px',
                    background: '#f44336',
                    color: 'white',
                    border: 'none',
                    borderRadius: '4px',
                    cursor: 'pointer'
                  }}
                  onClick={() => deleteLesson(lesson.id)}
                >
                  Delete
                </button>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  )
}
```

---

## 📊 Step 7: Create Dashboard Page

**src/pages/Dashboard.tsx:**
```typescript
import { useEffect, useState } from 'react'
import { supabase } from '../lib/supabase'

export function Dashboard() {
  const [stats, setStats] = useState({
    totalUsers: 0,
    totalLessons: 0,
    totalQuizzes: 0,
    totalProgress: 0
  })

  useEffect(() => {
    fetchStats()
  }, [])

  async function fetchStats() {
    try {
      // Count users
      const { count: users } = await supabase
        .from('profiles')
        .select('*', { count: 'exact', head: true })

      // Count lessons
      const { count: lessons } = await supabase
        .from('lessons')
        .select('*', { count: 'exact', head: true })

      // Count quiz questions
      const { count: quizzes } = await supabase
        .from('quiz_questions')
        .select('*', { count: 'exact', head: true })

      // Count progress records
      const { count: progress } = await supabase
        .from('lesson_progress')
        .select('*', { count: 'exact', head: true })

      setStats({
        totalUsers: users || 0,
        totalLessons: lessons || 0,
        totalQuizzes: quizzes || 0,
        totalProgress: progress || 0
      })
    } catch (error) {
      console.error('Error fetching stats:', error)
    }
  }

  return (
    <div>
      <h1>Dashboard</h1>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '20px', marginTop: '30px' }}>
        <StatCard title="Total Users" value={stats.totalUsers} icon="👥" color="#2196f3" />
        <StatCard title="Total Lessons" value={stats.totalLessons} icon="📚" color="#4caf50" />
        <StatCard title="Quiz Questions" value={stats.totalQuizzes} icon="📝" color="#ff9800" />
        <StatCard title="Progress Records" value={stats.totalProgress} icon="📊" color="#9c27b0" />
      </div>
    </div>
  )
}

function StatCard({ title, value, icon, color }: { title: string; value: number; icon: string; color: string }) {
  return (
    <div style={{
      background: 'white',
      padding: '30px',
      borderRadius: '8px',
      boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
      borderTop: `4px solid ${color}`
    }}>
      <div style={{ fontSize: '32px', marginBottom: '10px' }}>{icon}</div>
      <div style={{ fontSize: '32px', fontWeight: 'bold', color, marginBottom: '8px' }}>{value}</div>
      <div style={{ color: '#666' }}>{title}</div>
    </div>
  )
}
```

---

## 🔐 Step 8: Create Login Page

**src/pages/Login.tsx:**
```typescript
import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { supabase } from '../lib/supabase'

export function Login() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const navigate = useNavigate()

  async function handleLogin(e: React.FormEvent) {
    e.preventDefault()
    setLoading(true)
    setError('')

    try {
      const { data, error } = await supabase.auth.signInWithPassword({
        email,
        password
      })

      if (error) throw error

      // Check if user is admin (you'll need to implement this check)
      // For now, just navigate to dashboard
      navigate('/dashboard')
    } catch (error: any) {
      setError(error.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      minHeight: '100vh',
      background: '#f5f5f5'
    }}>
      <div style={{
        background: 'white',
        padding: '40px',
        borderRadius: '8px',
        boxShadow: '0 4px 6px rgba(0,0,0,0.1)',
        width: '400px'
      }}>
        <h1 style={{ textAlign: 'center', marginBottom: '30px' }}>E.S.C.A.P.E. Admin</h1>
        
        {error && (
          <div style={{
            padding: '10px',
            background: '#ffebee',
            color: '#c62828',
            borderRadius: '4px',
            marginBottom: '20px'
          }}>
            {error}
          </div>
        )}

        <form onSubmit={handleLogin}>
          <div style={{ marginBottom: '20px' }}>
            <label style={{ display: 'block', marginBottom: '8px' }}>Email</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              style={{
                width: '100%',
                padding: '10px',
                border: '1px solid #ddd',
                borderRadius: '4px'
              }}
            />
          </div>

          <div style={{ marginBottom: '20px' }}>
            <label style={{ display: 'block', marginBottom: '8px' }}>Password</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              style={{
                width: '100%',
                padding: '10px',
                border: '1px solid #ddd',
                borderRadius: '4px'
              }}
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            style={{
              width: '100%',
              padding: '12px',
              background: '#00bcd4',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer',
              fontSize: '16px'
            }}
          >
            {loading ? 'Logging in...' : 'Login'}
          </button>
        </form>
      </div>
    </div>
  )
}
```

---

## 🚀 Step 9: Run the Development Server

```powershell
npm run dev
```

Open http://localhost:5173 in your browser.

---

## 📦 Step 10: Deploy to Vercel

```powershell
# Install Vercel CLI
npm i -g vercel

# Login to Vercel
vercel login

# Deploy
vercel

# Add environment variables in Vercel dashboard:
# VITE_SUPABASE_URL
# VITE_SUPABASE_ANON_KEY
```

---

## 🎯 Next Steps

1. **Improve the UI** - Add Material-UI or Shadcn components
2. **Add File Upload** - Implement file upload to Supabase Storage
3. **Create Lesson Form** - Build a form to create/edit lessons
4. **Add Quiz Maker** - Build quiz editor interface
5. **Implement Analytics** - Add charts and graphs
6. **Add User Management** - CRUD operations for users
7. **Improve Auth** - Add role-based access control

---

## 📚 Resources

- **React Router**: https://reactrouter.com
- **Supabase Docs**: https://supabase.com/docs
- **Material-UI**: https://mui.com
- **React Query**: https://tanstack.com/query
- **Recharts**: https://recharts.org

---

## 💡 Tips

1. Start simple - Get CRUD working first
2. Use TypeScript - Catch errors early
3. Implement auth early - Secure from day 1
4. Test with real data - Use actual lessons
5. Make it mobile responsive - Admins might use tablets

---

## 🎉 You're Ready!

This gives you a solid starting point. Follow the **LESSONS_AND_WEB_ADMIN_GUIDE.md** for more detailed implementation guidance.

Good luck building your web admin panel! 🚀
