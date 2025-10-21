import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { ThemeProvider, createTheme, CssBaseline } from '@mui/material'
import { AdminLayout } from './components/layout/AdminLayout'
import { Dashboard } from './pages/Dashboard'
import { Lessons } from './pages/Lessons'
import QuizModules from './pages/QuizModules'
import QuizModulePage from './pages/QuizModulePage'
import { Users } from './pages/Users'
import { Analytics } from './pages/Analytics'
import { Settings } from './pages/Settings'

const queryClient = new QueryClient()

const theme = createTheme({
  palette: {
    primary: {
      main: '#2196F3',
    },
    secondary: {
      main: '#FF9800',
    },
  },
  typography: {
    fontFamily: '"Roboto", "Helvetica", "Arial", sans-serif',
  },
})

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <BrowserRouter>
          <Routes>
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
            <Route path="/dashboard" element={<AdminLayout><Dashboard /></AdminLayout>} />
            <Route path="/lessons" element={<AdminLayout><Lessons /></AdminLayout>} />
            <Route path="/quiz" element={<AdminLayout><QuizModules /></AdminLayout>} />
            <Route path="/quiz/:moduleId" element={<AdminLayout><QuizModulePage /></AdminLayout>} />
            <Route path="/users" element={<AdminLayout><Users /></AdminLayout>} />
            <Route path="/analytics" element={<AdminLayout><Analytics /></AdminLayout>} />
            <Route path="/settings" element={<AdminLayout><Settings /></AdminLayout>} />
          </Routes>
        </BrowserRouter>
      </ThemeProvider>
    </QueryClientProvider>
  )
}

export default App
