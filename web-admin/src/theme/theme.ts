import { createTheme } from '@mui/material/styles'

// ☀️ **NEW LIGHT THEME - E.S.C.A.P.E. AR (Web Admin)**
// Vibe: Minimal, Airy, Analytical — Fits Teacher/Admin Dashboard
// Perfect for data clarity, neat contrast, and gentle brightness

// ═══════════════════════════════════════════════════════════════
// 🎨 CORE PALETTE - LIGHT THEME (Web Admin)
// ═══════════════════════════════════════════════════════════════

export const brandColors = {
  // Primary - Ocean Blue (Mirrors dark theme's Misty Blue, keeps brand link)
  primary: '#3B82F6',        // Clean, professional blue
  primaryLight: '#60A5FA',   // Lighter for hover
  primaryDark: '#2563EB',    // Darker for active states
  
  // Secondary - Fresh Mint (same as accent, for gradient compatibility)
  secondary: '#14B8A6',      // Fresh, modern teal-mint
  secondaryDark: '#0F766E',  // Darker mint
  
  // Accent - Fresh Mint (Energizing but balanced)
  accent: '#14B8A6',         // Fresh, modern teal-mint
  accentLight: '#2DD4BF',    // Lighter mint
  accentDark: '#0F766E',     // Darker mint
  
  // Semantic Colors (Matches dark theme for consistency)
  success: '#10B981',        // Emerald - Success states
  successLight: '#34D399',   // Lighter emerald
  warning: '#F59E0B',        // Amber - Attention
  warningLight: '#FBBF24',   // Lighter amber
  error: '#F43F5E',          // Rose - Errors
  errorLight: '#FB7185',     // Lighter rose
  
  // Background Colors (Light Mode - Minimal & Airy)
  background: {
    main: '#F9FAFB',         // Off-white, less glare
    paper: '#FFFFFF',        // White - Clean containers
    secondary: '#F3F4F6',    // Light gray for sections
    borders: '#E5E7EB',      // Subtle dividers
  },
  
  // Text Colors (Crisp but not jet-black)
  text: {
    primary: '#111827',      // Charcoal - Main text
    secondary: '#6B7280',    // Slate - Helper text
    muted: '#9CA3AF',        // Light gray - Disabled
    white: '#FFFFFF',        // For colored backgrounds
  },
  
  // Gradient Colors (Optional blue-mint accents)
  gradient: {
    start: '#3B82F6',        // Ocean Blue
    end: '#14B8A6',          // Fresh Mint
  }
}

// Create consistent theme across auth pages and admin panel
export const theme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: brandColors.primary,         // Ocean Blue
      light: brandColors.primaryLight,
      dark: brandColors.primaryDark,
      contrastText: '#ffffff',
    },
    secondary: {
      main: brandColors.accent,          // Fresh Mint
      light: brandColors.accentLight,
      dark: brandColors.accentDark,
      contrastText: '#ffffff',
    },
    info: {
      main: brandColors.accent,          // Fresh Mint
      light: brandColors.accentLight,
      dark: brandColors.accentDark,
      contrastText: '#ffffff',
    },
    success: {
      main: brandColors.success,
      light: brandColors.successLight,
      contrastText: '#ffffff',
    },
    warning: {
      main: brandColors.warning,
      light: brandColors.warningLight,
      contrastText: '#ffffff',
    },
    error: {
      main: brandColors.error,
      light: brandColors.errorLight,
      contrastText: '#ffffff',
    },
    background: {
      default: brandColors.background.main,
      paper: brandColors.background.paper,
    },
    text: {
      primary: brandColors.text.primary,
      secondary: brandColors.text.secondary,
    },
  },
  typography: {
    fontFamily: '"Roboto", "Helvetica", "Arial", sans-serif',
    h4: {
      fontWeight: 700,
    },
    h5: {
      fontWeight: 600,
    },
    h6: {
      fontWeight: 600,
    },
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          textTransform: 'none',
          borderRadius: 8,
          fontWeight: 600,
        },
        contained: {
          boxShadow: '0 4px 6px rgba(102, 126, 234, 0.25)',
          '&:hover': {
            boxShadow: '0 6px 12px rgba(102, 126, 234, 0.35)',
          },
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          borderRadius: 12,
          boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
        },
      },
    },
    MuiAppBar: {
      styleOverrides: {
        root: {
          background: `linear-gradient(135deg, ${brandColors.gradient.start} 0%, ${brandColors.gradient.end} 100%)`,
        },
      },
    },
    MuiDrawer: {
      styleOverrides: {
        paper: {
          borderRight: 'none',
          boxShadow: '2px 0 8px rgba(0,0,0,0.05)',
        },
      },
    },
  },
})

export default theme
