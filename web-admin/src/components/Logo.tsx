import { Box, Typography } from '@mui/material'
import { brandColors } from '../theme/theme'

interface LogoProps {
  variant?: 'icon' | 'full' | 'text'
  size?: 'small' | 'medium' | 'large'
  showIcon?: boolean
  color?: 'primary' | 'white'
}

export function Logo({ 
  variant = 'full', 
  size = 'medium',
  showIcon = true,
  color = 'primary'
}: LogoProps) {
  
  // Size configurations - INCREASED for better visibility
  const sizes = {
    small: { icon: 120, text: 'h6' as const },   // Increased from 100
    medium: { icon: 200, text: 'h5' as const }, // Increased from 150 to fill sidebar better
    large: { icon: 280, text: 'h4' as const },  // Increased from 240
  }

  const iconSize = sizes[size].icon
  const textVariant = sizes[size].text
  
  // Color configurations
  const textColor = color === 'white' ? '#ffffff' : brandColors.text.primary

  // Your actual logo image - Add your logo.png to /web-admin/public/logo.png
  const logoImage = showIcon && (
    <Box
      sx={{
        position: 'relative',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        width: '100%',
        // Clean look for sidebar - no background box
        ...(variant === 'icon' && {
          padding: 0,
          backgroundColor: 'transparent',
          borderRadius: 0,
          boxShadow: 'none',
        }),
        // Boxed look for auth pages
        ...(variant === 'full' && {
          padding: 2, // Reduced from 3 to minimize space
          backgroundColor: color === 'white' 
            ? 'rgba(255, 255, 255, 0.1)' // Subtle white background for dark mode
            : 'rgba(102, 126, 234, 0.08)', // Light purple background
          borderRadius: 2,
          boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
        }),
      }}
    >
      <Box
        component="img"
        src="/logo.png"
        alt="E.S.C.A.P.E. AR Logo"
        sx={{
          maxHeight: iconSize, 
          maxWidth: '90%', 
          width: 'auto',
          height: 'auto',
          objectFit: 'contain',
          // Add contrast and visibility adjustments
          filter: color === 'white' 
            ? 'brightness(1.2)' // Brighten slightly for dark backgrounds
            : 'brightness(0.3) contrast(1.5)', // Darken and enhance contrast for light backgrounds
          opacity: 1,
        }}
      />
    </Box>
  )

  const logoText = (
    <Typography 
      variant={textVariant} 
      component="div"
      sx={{ 
        fontWeight: 'bold',
        color: textColor,
        letterSpacing: '0.5px',
      }}
    >
      E.S.C.A.P.E. AR
    </Typography>
  )

  if (variant === 'icon') {
    return <Box sx={{ display: 'flex', alignItems: 'center' }}>{logoImage}</Box>
  }

  if (variant === 'text') {
    return <Box sx={{ display: 'flex', alignItems: 'center' }}>{logoText}</Box>
  }

  // Full variant - icon + text
  return (
    <Box 
      sx={{ 
        display: 'flex', 
        alignItems: 'center', 
        gap: size === 'small' ? 1 : 2 
      }}
    >
      {logoImage}
      {logoText}
    </Box>
  )
}
