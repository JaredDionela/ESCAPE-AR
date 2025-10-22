import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Box,
  Card,
  CardContent,
  TextField,
  Button,
  Typography,
  Alert,
  CircularProgress,
  InputAdornment,
  IconButton,
  Link as MuiLink
} from '@mui/material'
import {
  Visibility,
  VisibilityOff,
  Email,
  Lock,
  Person
} from '@mui/icons-material'
import { supabase } from '../lib/supabase'
import { Logo } from '../components/Logo'
import { brandColors } from '../theme/theme'

export function RegisterTeacher() {
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [displayName, setDisplayName] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)
    setLoading(true)

    try {
      // Create auth user - the trigger will automatically create the profile with role='teacher'
      const { data: authData, error: authError } = await supabase.auth.signUp({
        email,
        password,
        options: {
          data: {
            display_name: displayName,
            role: 'teacher'
          }
        }
      })

      if (authError) throw authError

      if (!authData.user) {
        throw new Error('User creation failed')
      }

      // Profile is automatically created by the database trigger!
      // No need for manual insert - the trigger extracts role and display_name from metadata

      // Success! Redirect to login
      alert('Teacher account created successfully! Please check your email to verify your account, then log in.')
      navigate('/login')
    } catch (err: any) {
      console.error('Registration error:', err)
      setError(err.message || 'Failed to create teacher account')
    } finally {
      setLoading(false)
    }
  }

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: `linear-gradient(135deg, ${brandColors.primary} 0%, ${brandColors.secondary} 100%)`,
        padding: 2
      }}
    >
      <Card
        sx={{
          maxWidth: 450,
          width: '100%',
          boxShadow: '0 8px 32px rgba(0,0,0,0.1)'
        }}
      >
        <CardContent sx={{ p: 4 }}>
          {/* Header */}
          <Box sx={{ textAlign: 'center', mb: 4 }}>
            <Box sx={{ display: 'flex', justifyContent: 'center', mb: 3 }}>
              <Logo variant="icon" size="large" />
            </Box>
            <Typography variant="h6" color="text.secondary" gutterBottom>
              Teacher Registration
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Create your teacher account to manage students
            </Typography>
          </Box>

          {/* Error Alert */}
          {error && (
            <Alert severity="error" sx={{ mb: 3 }}>
              {error}
            </Alert>
          )}

          {/* Registration Form */}
          <form onSubmit={handleRegister}>
            <TextField
              fullWidth
              label="Full Name"
              type="text"
              value={displayName}
              onChange={(e) => setDisplayName(e.target.value)}
              required
              placeholder="e.g., Ms. Garcia"
              sx={{ mb: 2 }}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <Person color="action" />
                  </InputAdornment>
                ),
              }}
            />

            <TextField
              fullWidth
              label="Email Address"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              autoComplete="email"
              sx={{ mb: 2 }}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <Email color="action" />
                  </InputAdornment>
                ),
              }}
            />

            <TextField
              fullWidth
              label="Password"
              type={showPassword ? 'text' : 'password'}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              autoComplete="new-password"
              inputProps={{ minLength: 6 }}
              helperText="Minimum 6 characters"
              sx={{ mb: 3 }}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <Lock color="action" />
                  </InputAdornment>
                ),
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton
                      onClick={() => setShowPassword(!showPassword)}
                      edge="end"
                    >
                      {showPassword ? <VisibilityOff /> : <Visibility />}
                    </IconButton>
                  </InputAdornment>
                ),
              }}
            />

            <Button
              type="submit"
              variant="contained"
              fullWidth
              size="large"
              disabled={loading}
              sx={{
                py: 1.5,
                background: `linear-gradient(135deg, ${brandColors.primary} 0%, ${brandColors.secondary} 100%)`,
                '&:hover': {
                  background: `linear-gradient(135deg, ${brandColors.primaryDark} 0%, ${brandColors.secondaryDark} 100%)`,
                }
              }}
            >
              {loading ? (
                <CircularProgress size={24} color="inherit" />
              ) : (
                'Register as Teacher'
              )}
            </Button>
          </form>

          {/* Footer Links */}
          <Box sx={{ mt: 3, textAlign: 'center' }}>
            <Typography variant="body2" color="text.secondary">
              Already have an account?{' '}
              <MuiLink 
                component="button"
                type="button"
                onClick={() => navigate('/login')}
                underline="hover"
                sx={{ fontWeight: 'bold' }}
              >
                Sign In
              </MuiLink>
            </Typography>
          </Box>

          {/* Info Box */}
          <Box
            sx={{
              mt: 4,
              p: 2,
              backgroundColor: '#f5f5f5',
              borderRadius: 1,
              border: '1px dashed #ccc'
            }}
          >
            <Typography variant="caption" display="block" fontWeight="bold" gutterBottom>
              📧 Email Verification Required
            </Typography>
            <Typography variant="caption" display="block" color="text.secondary">
              After registration, please check your email to verify your account before signing in.
            </Typography>
          </Box>
        </CardContent>
      </Card>
    </Box>
  )
}
