import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { ThemeProvider, CssBaseline } from '@mui/material'
import { AuthProvider } from './contexts/AuthContext'
import { ProtectedRoute } from './components/ProtectedRoute'
import { LandingPage } from './pages/LandingPage'
import { Login } from './pages/Login'
import { RegisterTeacher } from './pages/RegisterTeacher'
import { AdminLayout } from './components/layout/AdminLayout'
import { Dashboard } from './pages/Dashboard'
import { Lessons } from './pages/Lessons'
import QuizModules from './pages/QuizModules'
import QuizModulePage from './pages/QuizModulePage'
import { Users } from './pages/Users'
import { Analytics } from './pages/Analytics'
import { Settings } from './pages/Settings'
import { theme } from './theme/theme'

const queryClient = new QueryClient()

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <BrowserRouter>
          <AuthProvider>
            <Routes>
              {/* Public Routes */}
              <Route path="/" element={<LandingPage />} />
              <Route path="/login" element={<Login />} />
              <Route path="/register" element={<RegisterTeacher />} />
              
              {/* Protected Admin Routes */}
              <Route path="/dashboard" element={<Navigate to="/admin/dashboard" replace />} />
              <Route 
                path="/admin/dashboard" 
                element={
                  <ProtectedRoute>
                    <AdminLayout><Dashboard /></AdminLayout>
                  </ProtectedRoute>
                } 
              />
              <Route 
                path="/admin/lessons" 
                element={
                  <ProtectedRoute>
                    <AdminLayout><Lessons /></AdminLayout>
                  </ProtectedRoute>
                } 
              />
              <Route 
                path="/admin/quiz" 
                element={
                  <ProtectedRoute>
                    <AdminLayout><QuizModules /></AdminLayout>
                  </ProtectedRoute>
                } 
              />
              <Route 
                path="/admin/quiz/:moduleId" 
                element={
                  <ProtectedRoute>
                    <AdminLayout><QuizModulePage /></AdminLayout>
                  </ProtectedRoute>
                } 
              />
              <Route 
                path="/admin/users" 
                element={
                  <ProtectedRoute>
                    <AdminLayout><Users /></AdminLayout>
                  </ProtectedRoute>
                } 
              />
              <Route 
                path="/admin/analytics" 
                element={
                  <ProtectedRoute>
                    <AdminLayout><Analytics /></AdminLayout>
                  </ProtectedRoute>
                } 
              />
              <Route 
                path="/admin/settings" 
                element={
                  <ProtectedRoute>
                    <AdminLayout><Settings /></AdminLayout>
                  </ProtectedRoute>
                } 
              />
              
              {/* Legacy routes - redirect to new admin paths */}
              <Route path="/lessons" element={<Navigate to="/admin/lessons" replace />} />
              <Route path="/quiz" element={<Navigate to="/admin/quiz" replace />} />
              <Route path="/quiz/:moduleId" element={<Navigate to="/admin/quiz/:moduleId" replace />} />
              <Route path="/users" element={<Navigate to="/admin/users" replace />} />
              <Route path="/analytics" element={<Navigate to="/admin/analytics" replace />} />
              <Route path="/settings" element={<Navigate to="/admin/settings" replace />} />
            </Routes>
          </AuthProvider>
        </BrowserRouter>
      </ThemeProvider>
    </QueryClientProvider>
  )
}

export default App
