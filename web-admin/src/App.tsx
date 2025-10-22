import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { ThemeProvider, CssBaseline } from '@mui/material'
import { AuthProvider } from './contexts/AuthContext'
import { ProtectedRoute } from './components/ProtectedRoute'
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
              <Route path="/login" element={<Login />} />
              <Route path="/register" element={<RegisterTeacher />} />
              
              {/* Protected Routes */}
              <Route path="/" element={<Navigate to="/dashboard" replace />} />
              <Route 
                path="/dashboard" 
                element={
                  <ProtectedRoute>
                    <AdminLayout><Dashboard /></AdminLayout>
                  </ProtectedRoute>
                } 
              />
              <Route 
                path="/lessons" 
                element={
                  <ProtectedRoute>
                    <AdminLayout><Lessons /></AdminLayout>
                  </ProtectedRoute>
                } 
              />
              <Route 
                path="/quiz" 
                element={
                  <ProtectedRoute>
                    <AdminLayout><QuizModules /></AdminLayout>
                  </ProtectedRoute>
                } 
              />
              <Route 
                path="/quiz/:moduleId" 
                element={
                  <ProtectedRoute>
                    <AdminLayout><QuizModulePage /></AdminLayout>
                  </ProtectedRoute>
                } 
              />
              <Route 
                path="/users" 
                element={
                  <ProtectedRoute>
                    <AdminLayout><Users /></AdminLayout>
                  </ProtectedRoute>
                } 
              />
              <Route 
                path="/analytics" 
                element={
                  <ProtectedRoute>
                    <AdminLayout><Analytics /></AdminLayout>
                  </ProtectedRoute>
                } 
              />
              <Route 
                path="/settings" 
                element={
                  <ProtectedRoute>
                    <AdminLayout><Settings /></AdminLayout>
                  </ProtectedRoute>
                } 
              />
            </Routes>
          </AuthProvider>
        </BrowserRouter>
      </ThemeProvider>
    </QueryClientProvider>
  )
}

export default App
