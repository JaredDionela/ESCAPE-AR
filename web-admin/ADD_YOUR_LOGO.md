# 🎯 How to Add Your Project E.S.C.A.P.E Logo

## Step 1: Save Your Logo Image

1. **Save the logo image** you showed me (the one with the spiral maze icon and "PROJECT E.S.C.A.P.E" text)
2. **Save it as**: `logo.png` (or `logo.svg` if you have vector format)
3. **Place it in**: `web-admin/public/logo.png`

Your file structure should look like:
```
web-admin/
├── public/
│   └── logo.png    ← Your logo goes here
└── src/
    └── ...
```

---

## Step 2: Update the Logo Component

Open: `web-admin/src/components/Logo.tsx`

### Change 1: Uncomment logoImage section (around line 33-44)

**Find this:**
```tsx
  // When you have your logo.png, place it in /web-admin/public/logo.png
  // Then uncomment this section and comment out the icon section below
  /*
  const logoImage = (
    <Box
      component="img"
      src="/logo.png"
      alt="E.S.C.A.P.E. AR Logo"
      sx={{
        height: iconSize,
        width: 'auto',
        objectFit: 'contain',
      }}
    />
  )
  */
```

**Change to:**
```tsx
  // Your actual logo image
  const logoImage = (
    <Box
      component="img"
      src="/logo.png"
      alt="E.S.C.A.P.E. AR Logo"
      sx={{
        height: iconSize,
        width: 'auto',
        objectFit: 'contain',
      }}
    />
  )
```

### Change 2: Comment out logoIcon section (around line 47-55)

**Find this:**
```tsx
  // Temporary placeholder using Material Icon
  // Replace this with logoImage once you add your logo.png
  const logoIcon = showIcon && (
    <School 
      sx={{ 
        fontSize: iconSize, 
        color: iconColor,
        filter: color === 'white' ? 'none' : 'drop-shadow(0 2px 4px rgba(102, 126, 234, 0.3))',
      }} 
    />
  )
```

**Change to:**
```tsx
  // Temporary placeholder using Material Icon
  // Replace this with logoImage once you add your logo.png
  /*
  const logoIcon = showIcon && (
    <School 
      sx={{ 
        fontSize: iconSize, 
        color: iconColor,
        filter: color === 'white' ? 'none' : 'drop-shadow(0 2px 4px rgba(102, 126, 234, 0.3))',
      }} 
    />
  )
  */
```

### Change 3: Update icon variant return (around line 72)

**Find this:**
```tsx
  if (variant === 'icon') {
    return <Box sx={{ display: 'flex', alignItems: 'center' }}>{logoIcon}</Box>
  }
```

**Change to:**
```tsx
  if (variant === 'icon') {
    return <Box sx={{ display: 'flex', alignItems: 'center' }}>{logoImage}</Box>
  }
```

### Change 4: Update full variant return (around line 87-96)

**Find this:**
```tsx
  // Full variant - icon + text
  return (
    <Box 
      sx={{ 
        display: 'flex', 
        alignItems: 'center', 
        gap: size === 'small' ? 1 : 2 
      }}
    >
      {logoIcon}
      {logoText}
    </Box>
  )
```

**Change to:**
```tsx
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
```

---

## Step 3: Optional - Show Only Logo (No Text)

Since your logo image already includes the "PROJECT E.S.C.A.P.E" text, you might want to show **only the logo image** without the additional "E.S.C.A.P.E. AR" text.

### Option A: Keep Both (Logo + Text)
Leave the pages as they are. The logo icon will show, then "E.S.C.A.P.E. AR" text below it.

### Option B: Show Only Logo (Recommended for your logo)
Update the Login and Register pages to use `variant="icon"` which shows only the logo image.

**They already use this**, so you're good! ✅

---

## Step 4: Adjust Logo Size (If Needed)

Your logo has the text built-in, so you might want to make it wider. Update the Logo component:

**Find this (around line 41):**
```tsx
sx={{
  height: iconSize,
  width: 'auto',
  objectFit: 'contain',
}}
```

**Change to (for wider logo):**
```tsx
sx={{
  height: iconSize,
  width: iconSize * 3, // Makes it 3x wider (good for horizontal logos)
  objectFit: 'contain',
}}
```

Or for flexible width:
```tsx
sx={{
  maxHeight: iconSize,
  maxWidth: iconSize * 4, // Allows logo to be up to 4x wider
  width: 'auto',
  height: 'auto',
  objectFit: 'contain',
}}
```

---

## Step 5: Test It!

1. **Restart the dev server** (if needed):
   ```bash
   cd web-admin
   npm run dev
   ```

2. **Open**: http://localhost:3004/

3. **You should see your logo** with the spiral maze and "PROJECT E.S.C.A.P.E" text!

---

## Quick Checklist

- [ ] Save your logo as `web-admin/public/logo.png`
- [ ] Edit `web-admin/src/components/Logo.tsx`
- [ ] Uncomment `logoImage` section
- [ ] Comment out `logoIcon` section
- [ ] Replace `logoIcon` with `logoImage` (2 places)
- [ ] Optional: Adjust width for horizontal logo
- [ ] Save files
- [ ] Refresh browser (Ctrl+F5)
- [ ] See your logo! 🎉

---

## 🎨 Color Tip

Your logo appears to be gray/white. If you need it to change colors based on the background:

1. Save your logo as **SVG** format (if possible)
2. Or create separate versions:
   - `logo-light.png` (for light backgrounds)
   - `logo-dark.png` (for dark backgrounds)

Then update the Logo component to switch based on the `color` prop.

---

**Need help?** Let me know and I can make these changes for you!
