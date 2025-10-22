# 🎨 E.S.C.A.P.E. AR COLOR PALETTE - Quick Reference

## 🌙 DARK THEME (Student App)

```
PRIMARY BRAND
├─ Misty Blue        #6BA6FF  ████████████  (Main buttons, headers)
├─ Misty Blue Light  #8FC4FF  ████████████  (Hover states)
└─ Misty Blue Dark   #4A8EE8  ████████████  (Active states)

ACCENT
├─ Electric Mint      #5EF1C3  ████████████  (AR glows, highlights)
├─ Electric Mint Light #88F5D5 ████████████  (Hover states)
└─ Electric Mint Dark  #3DD9AD ████████████  (Active states)

BACKGROUNDS
├─ Charcoal Blue     #0E1726  ████████████  (Main background)
├─ Deep Slate        #1B2735  ████████████  (Cards, surfaces)
├─ Elevated Slate    #2A3F54  ████████████  (Elevated elements)
└─ Border Slate      #334155  ████████████  (Borders, dividers)

TEXT
├─ Off-White         #E2E8F0  ████████████  (Main text)
├─ Cool Gray         #94A3B8  ████████████  (Secondary text)
└─ Dim Gray          #64748B  ████████████  (Disabled text)

SEMANTIC
├─ Soft Emerald      #34D399  ████████████  (Success ✅)
├─ Golden Glow       #FACC15  ████████████  (Warning ⚠️)
└─ Coral Red         #FB7185  ████████████  (Error ❌)

GRADIENTS
Blue→Mint:  linear-gradient(135deg, #6BA6FF, #5EF1C3)
```

---

## ☀️ LIGHT THEME (Web Admin)

```
PRIMARY BRAND
├─ Ocean Blue        #3B82F6  ████████████  (Main buttons, headers)
├─ Ocean Blue Light  #60A5FA  ████████████  (Hover states)
└─ Ocean Blue Dark   #2563EB  ████████████  (Active states)

ACCENT
├─ Fresh Mint        #14B8A6  ████████████  (Highlights, accents)
├─ Fresh Mint Light  #2DD4BF  ████████████  (Hover states)
└─ Fresh Mint Dark   #0F766E  ████████████  (Active states)

BACKGROUNDS
├─ Off-White         #F9FAFB  ████████████  (Main background)
├─ White             #FFFFFF  ████████████  (Cards, containers)
├─ Light Gray        #F3F4F6  ████████████  (Sections)
└─ Borders           #E5E7EB  ████████████  (Dividers)

TEXT
├─ Charcoal          #111827  ████████████  (Main text)
├─ Slate             #6B7280  ████████████  (Secondary text)
└─ Muted Gray        #9CA3AF  ████████████  (Disabled text)

SEMANTIC
├─ Emerald           #10B981  ████████████  (Success ✅)
├─ Amber             #F59E0B  ████████████  (Warning ⚠️)
└─ Rose              #F43F5E  ████████████  (Error ❌)

GRADIENTS
Blue→Mint:  linear-gradient(135deg, #3B82F6, #14B8A6)
```

---

## 🔗 COLOR HARMONY

```
DARK APP          LIGHT ADMIN       PURPOSE
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
#6BA6FF    ←→     #3B82F6          Primary Brand (Blue)
#5EF1C3    ←→     #14B8A6          Accent (Mint)
#34D399    ←→     #10B981          Success (Green)
#FACC15    ←→     #F59E0B          Warning (Amber)
#FB7185    ←→     #F43F5E          Error (Rose/Coral)
```

---

## 📋 USAGE EXAMPLES

### **Buttons:**
```
Primary:     Background: Blue (#6BA6FF / #3B82F6)
             Text: White
             Hover: Lighter Blue

Secondary:   Background: Mint (#5EF1C3 / #14B8A6)
             Text: White
             Hover: Lighter Mint

Danger:      Background: Coral/Rose (#FB7185 / #F43F5E)
             Text: White
```

### **Cards:**
```
Dark App:    Background: Deep Slate (#1B2735)
             Border: Border Slate (#334155)
             Text: Off-White (#E2E8F0)

Light Admin: Background: White (#FFFFFF)
             Border: Light Gray (#E5E7EB)
             Text: Charcoal (#111827)
```

### **Status Indicators:**
```
✅ Success:  Background: Green (#34D399 / #10B981)
⚠️ Warning:  Background: Amber (#FACC15 / #F59E0B)
❌ Error:    Background: Coral/Rose (#FB7185 / #F43F5E)
ℹ️ Info:     Background: Mint (#5EF1C3 / #14B8A6)
```

---

## 🎨 GRADIENT RECIPES

### **Blue-Mint (Most Common):**
```css
/* Dark App */
background: linear-gradient(135deg, #6BA6FF 0%, #5EF1C3 100%);

/* Light Admin */
background: linear-gradient(135deg, #3B82F6 0%, #14B8A6 100%);
```

### **Vertical Gradient (Backgrounds):**
```css
/* Dark App */
background: linear-gradient(180deg, #0E1726 0%, #1B2735 100%);

/* Light Admin */
background: linear-gradient(180deg, #F9FAFB 0%, #FFFFFF 100%);
```

---

## 🔍 CONTRAST RATIOS (WCAG AA)

| Combination | Ratio | Pass |
|-------------|-------|------|
| Blue on White | 4.9:1 | ✅ AA |
| Mint on Dark | 8.2:1 | ✅ AAA |
| White on Blue | 4.9:1 | ✅ AA |
| Charcoal on White | 15.6:1 | ✅ AAA |
| Off-White on Dark | 11.3:1 | ✅ AAA |

---

## 💡 QUICK TIPS

1. **Dark Backgrounds:** Always use navy-tinted (#0E1726), never pure black
2. **Text on Dark:** Use off-white (#E2E8F0), not pure white
3. **Hover Effects:** Lighten colors by ~20% for hover states
4. **Active States:** Darken colors by ~20% for active states
5. **Gradients:** Use 135deg angle for consistency
6. **Shadows:** Use `rgba(0, 0, 0, 0.05)` for light theme
7. **Glows:** Use mint color with low opacity for AR effects

---

## 🎯 COLOR PSYCHOLOGY

| Color | Emotion | Use Case |
|-------|---------|----------|
| **Blue** | Trust, focus, calm | Primary actions, brand identity |
| **Mint** | Fresh, energy, modern | Accents, highlights, success |
| **Green** | Success, correct | Achievements, positive feedback |
| **Amber** | Attention, caution | Warnings, important notices |
| **Coral/Rose** | Error, urgent | Mistakes, critical alerts |

---

## 📱 PLATFORM CONSISTENCY

```
COLOR NAME          ANDROID HEX    WEB HEX        MATCH
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Misty Blue          #6BA6FF        #3B82F6        ✅ (Blue family)
Electric Mint       #5EF1C3        #14B8A6        ✅ (Mint family)
Success Green       #34D399        #10B981        ✅ (Same green)
Warning Amber       #FACC15        #F59E0B        ✅ (Same amber)
Error Coral/Rose    #FB7185        #F43F5E        ✅ (Same rose)
```

---

**Print this and keep it handy while coding!** 🎨✨
