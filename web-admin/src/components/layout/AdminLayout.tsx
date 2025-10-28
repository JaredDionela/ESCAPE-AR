import { ReactNode, useEffect } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { Box, Drawer, AppBar, Toolbar, List, Typography, Divider, ListItem, ListItemButton, ListItemIcon, ListItemText, IconButton, Avatar } from '@mui/material'
import DashboardIcon from '@mui/icons-material/Dashboard'
import SchoolIcon from '@mui/icons-material/School'
import QuizIcon from '@mui/icons-material/Quiz'
import PeopleIcon from '@mui/icons-material/People'
import BarChartIcon from '@mui/icons-material/BarChart'
import SettingsIcon from '@mui/icons-material/Settings'
import LogoutIcon from '@mui/icons-material/Logout'
import MenuIcon from '@mui/icons-material/Menu'
import PersonIcon from '@mui/icons-material/Person'
import { useState } from 'react'
import { useAuth } from '../../contexts/AuthContext'
import { Logo } from '../Logo'
import { brandColors } from '../../theme/theme'
import { supabase } from '../../lib/supabase'

const drawerWidth = 260

interface AdminLayoutProps {
  children: ReactNode
}

const menuItems = [
  { text: 'Dashboard', icon: <DashboardIcon />, path: '/admin/dashboard' },
  { text: 'Lessons', icon: <SchoolIcon />, path: '/admin/lessons' },
  { text: 'Quizzes', icon: <QuizIcon />, path: '/admin/quiz' },
  { text: 'Users', icon: <PeopleIcon />, path: '/admin/users' },
  { text: 'Analytics', icon: <BarChartIcon />, path: '/admin/analytics' },
  { text: 'Settings', icon: <SettingsIcon />, path: '/admin/settings' }
]

export function AdminLayout({ children }: AdminLayoutProps) {
  const location = useLocation()
  const navigate = useNavigate()
  const { user, signOut } = useAuth()
  const [mobileOpen, setMobileOpen] = useState(false)
  const [profileName, setProfileName] = useState<string>('Teacher')
  const [profileEmail, setProfileEmail] = useState<string>('')

  // Fetch teacher profile info
  useEffect(() => {
    const fetchProfile = async () => {
      if (!user) return
      
      const { data, error } = await supabase
        .from('profiles')
        .select('display_name, email')
        .eq('id', user.id)
        .single()
      
      if (data && !error) {
        setProfileName(data.display_name || 'Teacher')
        setProfileEmail(data.email || user.email || '')
      }
    }
    
    fetchProfile()
  }, [user])

  const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen)
  }

  const handleLogout = async () => {
    await signOut()
    navigate('/login')
  }

  const drawer = (
    <div>
      <Toolbar 
        sx={{ 
          background: '#3B82F6',
          color: 'white',
          minHeight: 70,
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          padding: 2
        }}
      >
        <Box sx={{ 
          backgroundColor: '#3B82F6',
          borderRadius: 2,
          padding: 1.5,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          width: '100%'
        }}>
          <Logo variant="icon" size="medium" color="white" />
        </Box>
      </Toolbar>
      <Divider />
      <List>
        {menuItems.map((item) => (
          <ListItem key={item.text} disablePadding>
            <ListItemButton
              component={Link}
              to={item.path}
              selected={location.pathname === item.path}
              sx={{
                '&.Mui-selected': {
                  backgroundColor: `rgba(102, 126, 234, 0.12)`,
                  borderLeft: `4px solid ${brandColors.primary}`,
                  '& .MuiListItemIcon-root': {
                    color: brandColors.primary,
                  },
                  '& .MuiListItemText-primary': {
                    color: brandColors.primary,
                    fontWeight: 600,
                  }
                },
                '&:hover': {
                  backgroundColor: 'rgba(102, 126, 234, 0.08)',
                }
              }}
            >
              <ListItemIcon sx={{ color: location.pathname === item.path ? brandColors.primary : 'inherit' }}>
                {item.icon}
              </ListItemIcon>
              <ListItemText primary={item.text} />
            </ListItemButton>
          </ListItem>
        ))}
      </List>
      <Divider />
      <List>
        <ListItem disablePadding>
          <ListItemButton onClick={handleLogout}>
            <ListItemIcon>
              <LogoutIcon />
            </ListItemIcon>
            <ListItemText primary="Logout" />
          </ListItemButton>
        </ListItem>
      </List>
    </div>
  )

  return (
    <Box sx={{ display: 'flex' }}>
      <AppBar
        position="fixed"
        sx={{
          width: { sm: `calc(100% - ${drawerWidth}px)` },
          ml: { sm: `${drawerWidth}px` },
          background: `linear-gradient(135deg, ${brandColors.primary} 0%, ${brandColors.secondary} 100%)`,
          boxShadow: '0 2px 8px rgba(102, 126, 234, 0.2)'
        }}
      >
        <Toolbar>
          <IconButton
            color="inherit"
            edge="start"
            onClick={handleDrawerToggle}
            sx={{ mr: 2, display: { sm: 'none' } }}
          >
            <MenuIcon />
          </IconButton>
          <Typography variant="h6" noWrap component="div" sx={{ fontWeight: 600, flexGrow: 1 }}>
            Admin Panel
          </Typography>
          
          {/* Teacher Profile Info */}
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <Box sx={{ textAlign: 'right', display: { xs: 'none', sm: 'block' } }}>
              <Typography variant="body2" sx={{ fontWeight: 600, lineHeight: 1.2 }}>
                {profileName}
              </Typography>
              <Typography variant="caption" sx={{ opacity: 0.8 }}>
                {profileEmail}
              </Typography>
            </Box>
            <Avatar 
              sx={{ 
                bgcolor: 'rgba(255,255,255,0.2)', 
                width: 40, 
                height: 40,
                border: '2px solid rgba(255,255,255,0.3)'
              }}
            >
              <PersonIcon />
            </Avatar>
          </Box>
        </Toolbar>
      </AppBar>
      
      <Box
        component="nav"
        sx={{ width: { sm: drawerWidth }, flexShrink: { sm: 0 } }}
      >
        {/* Mobile drawer */}
        <Drawer
          variant="temporary"
          open={mobileOpen}
          onClose={handleDrawerToggle}
          ModalProps={{ keepMounted: true }}
          sx={{
            display: { xs: 'block', sm: 'none' },
            '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth }
          }}
        >
          {drawer}
        </Drawer>
        
        {/* Desktop drawer */}
        <Drawer
          variant="permanent"
          sx={{
            display: { xs: 'none', sm: 'block' },
            '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth }
          }}
          open
        >
          {drawer}
        </Drawer>
      </Box>
      
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          p: 3,
          width: { sm: `calc(100% - ${drawerWidth}px)` },
          backgroundColor: '#f5f5f5',
          minHeight: '100vh'
        }}
      >
        <Toolbar />
        {children}
      </Box>
    </Box>
  )
}
