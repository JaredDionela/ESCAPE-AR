# Complete Logo.tsx File - Ready to Use

**File:** `web-admin/src/components/Logo.tsx`

**Instructions:**
1. Save your logo as `web-admin/public/logo.png`
2. Replace the entire content of `Logo.tsx` with the code below
3. Refresh your browser

---

```tsx
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
  
  // Size configurations
  const sizes = {
    small: { icon: 32, text: 'h6' as const },
    medium: { icon: 48, text: 'h5' as const },
    large: { icon: 64, text: 'h4' as const },
  }

  const iconSize = sizes[size].icon
  const textVariant = sizes[size].text
  
  // Color configurations
  const textColor = color === 'white' ? '#ffffff' : brandColors.text.primary

  // Your actual logo image
  const logoImage = showIcon && (
    <Box
      component="img"
      src="/logo.png"
      alt="E.S.C.A.P.E. AR Logo"
      sx={{
        maxHeight: iconSize,
        maxWidth: iconSize * 4, // Allows horizontal logos to be wider
        width: 'auto',
        height: 'auto',
        objectFit: 'contain',
      }}
    />
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
```

---

## What Changed?

✅ Removed the School icon placeholder  
✅ Using your logo image from `/logo.png`  
✅ Made logo width flexible (up to 4x height) for horizontal logos  
✅ Simplified the code  
✅ Ready to use immediately

---

## Your Logo Will Appear:

1. **Login Page**: Large horizontal logo
2. **Registration Page**: Large horizontal logo
3. **Admin Sidebar**: Small horizontal logo + text

Since your logo already includes "PROJECT E.S.C.A.P.E" text, it will look perfect!

---

## Alternative: Logo Only (No Extra Text)

If you want to show **ONLY your logo** without the "E.S.C.A.P.E. AR" text below it, update these pages:

### Login.tsx (around line 75-80)
**Current:**
```tsx
<Box sx={{ display: 'flex', justifyContent: 'center', mb: 2 }}>
  <Logo variant="icon" size="large" />
</Box>
<Typography variant="h4" fontWeight="bold" gutterBottom>
  E.S.C.A.P.E. AR
</Typography>
```

**Change to:**
```tsx
<Box sx={{ display: 'flex', justifyContent: 'center', mb: 3 }}>
  <Logo variant="icon" size="large" />
</Box>
{/* Text removed - logo already has it */}
```

### RegisterTeacher.tsx (same change)
Do the same thing in the registration page.

---

**Just add your logo file and you're done!** 🚀
