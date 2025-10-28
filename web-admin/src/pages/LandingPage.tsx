import { useNavigate } from 'react-router-dom'
import { 
  Box, 
  Container, 
  Typography, 
  Button, 
  Paper,
  Grid,
  Stack
} from '@mui/material'
import DownloadIcon from '@mui/icons-material/Download'
import LoginIcon from '@mui/icons-material/Login'
import SchoolIcon from '@mui/icons-material/School'
import ScienceIcon from '@mui/icons-material/Science'

export function LandingPage() {
  const navigate = useNavigate()

  const handleDownloadAPK = () => {
    // TODO: Replace with actual APK download URL
    window.open('https://github.com/JaredDionela/ESCAPE-AR/releases/latest', '_blank')
  }

  return (
    <Box
      sx={{
        minHeight: '100vh',
        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        position: 'relative',
        display: 'flex',
        flexDirection: 'column'
      }}
    >
      {/* Admin Sign In Button (Top Right Corner) */}
      <Box sx={{ position: 'absolute', top: 20, right: 20 }}>
        <Button
          variant="contained"
          startIcon={<LoginIcon />}
          onClick={() => navigate('/login')}
          sx={{
            bgcolor: 'white',
            color: '#667eea',
            '&:hover': {
              bgcolor: '#f5f5f5'
            }
          }}
        >
          Admin Sign In
        </Button>
      </Box>

      {/* Main Content */}
      <Container maxWidth="lg" sx={{ flex: 1, display: 'flex', alignItems: 'center', py: 8 }}>
        <Grid container spacing={4} alignItems="center">
          {/* Left Side - App Info */}
          <Grid item xs={12} md={6}>
            <Stack spacing={3}>
              {/* Logo/Icon */}
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                
                <Typography
                  variant="h2"
                  fontWeight="bold"
                  sx={{ color: 'white' }}
                >
                  
                </Typography>
              </Box>

              {/* Description */}
              <Typography
                variant="h5"
                sx={{ color: 'rgba(255, 255, 255, 0.95)', lineHeight: 1.6 }}
              >
                Educational Science Platform with Augmented Reality
              </Typography>

              <Typography
                variant="body1"
                sx={{ color: 'rgba(255, 255, 255, 0.85)', lineHeight: 1.8 }}
              >
                Learn science concepts through interactive AR experiences. 
                Explore modules on decantation, organ systems, simple machines, 
                and the solar system with immersive 3D visualizations and quizzes.
              </Typography>

              {/* Features */}
              <Stack spacing={2} sx={{ mt: 2 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                  <SchoolIcon sx={{ color: 'white' }} />
                  <Typography sx={{ color: 'white' }}>
                    Interactive video lessons & quizzes
                  </Typography>
                </Box>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                  <ScienceIcon sx={{ color: 'white' }} />
                  <Typography sx={{ color: 'white' }}>
                    Augmented Reality 3D models
                  </Typography>
                </Box>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                  <SchoolIcon sx={{ color: 'white' }} />
                  <Typography sx={{ color: 'white' }}>
                    Track progress & performance
                  </Typography>
                </Box>
              </Stack>

              {/* Download Button */}
              <Box sx={{ mt: 4 }}>
                <Button
                  variant="contained"
                  size="large"
                  startIcon={<DownloadIcon />}
                  onClick={handleDownloadAPK}
                  sx={{
                    bgcolor: '#4CAF50',
                    color: 'white',
                    py: 2,
                    px: 4,
                    fontSize: '1.1rem',
                    fontWeight: 'bold',
                    '&:hover': {
                      bgcolor: '#45a049'
                    }
                  }}
                >
                  Download APK
                </Button>
                <Typography
                  variant="caption"
                  sx={{ display: 'block', color: 'rgba(255, 255, 255, 0.7)', mt: 1 }}
                >
                  Version 1.0 • Android 8.0 or higher
                </Typography>
              </Box>
            </Stack>
          </Grid>

          {/* Right Side - Screenshot/Preview */}
          <Grid item xs={12} md={6}>
            <Paper
              elevation={8}
              sx={{
                p: 4,
                bgcolor: '#292929ff',
                borderRadius: 4,
                textAlign: 'center'
              }}
            >
              <Box
                sx={{
                  width: '100%',
                  height: 400,
                  bgcolor: '#292929ff',
                  borderRadius: 2,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  flexDirection: 'column',
                  gap: 2
                }}
              >
                <img 
                  src="/logo-avatar.png" 
                  alt="Project E.S.C.A.P.E Mobile App" 
                  style={{ width: 200, height: 200, objectFit: 'contain' }}
                />
                <Typography variant="h6" color='#e9e7e7ff'>
                  Project E.S.C.A.P.E
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Interactive AR Science Learning
                </Typography>
              </Box>

              <Typography variant="body1" sx={{ mt: 3, color: 'text.secondary' }}>
                Available for Android devices
              </Typography>
            </Paper>
          </Grid>
        </Grid>
      </Container>

      {/* Footer */}
      <Box
        sx={{
          bgcolor: 'rgba(0, 0, 0, 0.2)',
          py: 3,
          textAlign: 'center'
        }}
      >
        <Typography variant="body2" sx={{ color: 'rgba(255, 255, 255, 0.8)' }}>
          © 2025 Project E.S.C.A.P.E. All rights reserved.
        </Typography>
        <Typography variant="caption" sx={{ color: 'rgba(255, 255, 255, 0.6)', display: 'block', mt: 1 }}>
          Built for science education
        </Typography>
      </Box>
    </Box>
  )
}
