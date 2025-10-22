# 🎨 E.S.C.A.P.E. AR Color Theme Reference

## Quick Color Guide for Development

### **Primary Colors (Use These Most)**

```
Soft Purple:   #8B7FD8  ████████  Main buttons, primary accents
Indigo:        #667eea  ████████  Secondary elements, gradients  
Soft Teal:     #4FD1C5  ████████  Modern accents, highlights
```

### **Semantic Colors (Use for Meaning)**

```
Emerald Green: #10B981  ████████  ✓ Success, correct answers
Amber:         #F59E0B  ████████  ⚠ Warnings, attention
Soft Rose:     #F43F5E  ████████  ✗ Errors, incorrect answers
```

### **Backgrounds (Dark Theme)**

```
Dark Navy:     #1A202C  ████████  Main background
Dark Slate:    #2D3748  ████████  Cards, elevated surfaces
Dark Gray:     #4A5568  ████████  Borders, dividers
```

### **Text Colors**

```
White:         #F7FAFC  ████████  Primary text
Light Gray:    #E2E8F0  ████████  Secondary text
Muted Gray:    #A0AEC0  ████████  Tertiary/muted text
```

---

## Usage Examples

### **Buttons:**
- Primary: Soft Purple (`#8B7FD8`)
- Secondary: Indigo (`#667eea`)
- Accent: Soft Teal (`#4FD1C5`)

### **Alerts:**
- Success: Emerald Green (`#10B981`)
- Warning: Amber (`#F59E0B`)
- Error: Soft Rose (`#F43F5E`)

### **Gradients:**
```css
/* Auth Pages */
background: linear-gradient(135deg, #8B7FD8 0%, #667eea 100%);

/* Full Screen */
background: linear-gradient(180deg, #8B7FD8 0%, #667eea 50%, #1A202C 100%);

/* Subtle Cards */
background: linear-gradient(135deg, #2D3748 0%, #4A5568 100%);
```

### **Progress/Loading:**
- Bar Color: Soft Teal (`#4FD1C5`)
- Track Color: Dark Slate (`#2D3748`)

### **Quiz States:**
- Correct Answer: Emerald Green (`#10B981`)
- Wrong Answer: Soft Rose (`#F43F5E`)
- Selected: Soft Purple (`#8B7FD8`)

---

## Android Kotlin Color Names

```kotlin
// Primary
SoftPurple           // #8B7FD8
SoftPurpleLight      // #A396E0
SoftPurpleDark       // #6B5FB8
BrandIndigo          // #667eea
BrandIndigoDark      // #5568d3

// Accent
SoftTeal             // #4FD1C5
SoftTealLight        // #81E6D9
SoftTealDark         // #38B2AC

// Semantic
EmeraldGreen         // #10B981
EmeraldGreenLight    // #34D399
AmberWarm            // #F59E0B
AmberWarmLight       // #FBBF24
RoseSoft             // #F43F5E
RoseSoftLight        // #FB7185

// Backgrounds
DarkNavy             // #1A202C
DarkSlate            // #2D3748
DarkGray             // #4A5568

// Text
TextWhite            // #F7FAFC
TextLight            // #E2E8F0
TextMuted            // #A0AEC0
TextDisabled         // #718096

// Legacy (mapped to new colors)
NeonCyan             → SoftTeal
PurpleHaze           → SoftPurple
ElectricBlue         → BrandIndigo
DeepSpace            → DarkNavy
MetallicSilver       → TextMuted
GlowGreen            → EmeraldGreen
CrimsonRed           → RoseSoft
AmberAlert           → AmberWarm
```

---

## Web TypeScript/React Color References

```typescript
// Import
import { brandColors } from '../theme/theme'

// Primary
brandColors.primary           // #8B7FD8
brandColors.primaryLight      // #A396E0
brandColors.primaryDark       // #6B5FB8

// Secondary
brandColors.secondary         // #667eea
brandColors.secondaryDark     // #5568d3

// Accent
brandColors.accent            // #4FD1C5
brandColors.accentLight       // #81E6D9
brandColors.accentDark        // #38B2AC

// Semantic
brandColors.success           // #10B981
brandColors.successLight      // #34D399
brandColors.warning           // #F59E0B
brandColors.warningLight      // #FBBF24
brandColors.error             // #F43F5E
brandColors.errorLight        // #FB7185

// Backgrounds
brandColors.background.main       // #F7FAFC
brandColors.background.paper      // #FFFFFF
brandColors.background.dark       // #1A202C
brandColors.background.secondary  // #2D3748

// Text
brandColors.text.primary      // #1A202C (light mode) / #F7FAFC (dark mode)
brandColors.text.secondary    // #4A5568
brandColors.text.light        // #718096
brandColors.text.white        // #F7FAFC
```

---

## CSS Variables (Optional - For Future Use)

```css
:root {
  /* Primary */
  --color-soft-purple: #8B7FD8;
  --color-indigo: #667eea;
  --color-soft-teal: #4FD1C5;
  
  /* Semantic */
  --color-emerald: #10B981;
  --color-amber: #F59E0B;
  --color-rose: #F43F5E;
  
  /* Backgrounds */
  --bg-navy: #1A202C;
  --bg-slate: #2D3748;
  --bg-gray: #4A5568;
  
  /* Text */
  --text-white: #F7FAFC;
  --text-light: #E2E8F0;
  --text-muted: #A0AEC0;
}
```

---

## Color Psychology & Usage

### **Soft Purple (#8B7FD8)**
- **Feeling**: Calming, creative, focused
- **Use for**: Main brand, primary buttons, important actions
- **Students**: Promotes concentration and imagination

### **Indigo (#667eea)**
- **Feeling**: Professional, trustworthy, stable
- **Use for**: Secondary elements, gradients, backgrounds
- **Students**: Builds confidence in the platform

### **Soft Teal (#4FD1C5)**
- **Feeling**: Modern, energetic, refreshing
- **Use for**: Accents, highlights, interactive elements
- **Students**: Keeps energy up without being harsh

### **Emerald Green (#10B981)**
- **Feeling**: Success, achievement, growth
- **Use for**: Correct answers, achievements, progress
- **Students**: Positive reinforcement, motivation

### **Amber (#F59E0B)**
- **Feeling**: Attention, caution, warmth
- **Use for**: Warnings, important notices
- **Students**: Friendly warning, not alarming

### **Soft Rose (#F43F5E)**
- **Feeling**: Error, mistake, need correction
- **Use for**: Wrong answers, validation errors
- **Students**: Gentle correction, not harsh

---

## Accessibility Notes

✅ **All color combinations meet WCAG AA standards** for contrast
✅ **Dark theme reduces eye strain** for extended study sessions
✅ **Color-blind friendly** - doesn't rely on red/green distinction
✅ **Semantic meaning** reinforced with icons and text, not just color

---

## Design Tips

1. **Use Purple for Primary Actions**: Sign in, submit, save
2. **Use Teal for Accents**: Links, highlights, active states
3. **Use Green Sparingly**: Only for success/completion
4. **Use Rose Sparingly**: Only for errors/mistakes
5. **Use Amber for Warnings**: Time limits, important notices
6. **Dark Backgrounds**: Main app uses navy, not pure black
7. **Text Hierarchy**: White > Light Gray > Muted Gray

---

**Questions?** Refer to:
- `web-admin/src/theme/theme.ts`
- `app/src/main/java/.../ui/theme/Color.kt`
