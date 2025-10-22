package com.example.escape_ar.ui.theme

import androidx.compose.ui.graphics.Color

// � **NEW DARK THEME - E.S.C.A.P.E. AR (Student App)**
// Vibe: Calm, Focused, Futuristic — Like a Learning Space in Low Light
// Perfect for late-night study sessions, AR glows, and gentle contrast

// ═══════════════════════════════════════════════════════════════
// 🎨 CORE PALETTE - DARK THEME (Student App)
// ═══════════════════════════════════════════════════════════════

// Primary - Misty Blue (Replaces purple; gives calm, techy confidence)
val MistyBlue = Color(0xFF6BA6FF)           // Main brand - calm, confident blue
val MistyBlueLight = Color(0xFF8FC4FF)      // Lighter for hover states
val MistyBlueDark = Color(0xFF4A8EE8)       // Darker for active states

// Accent - Electric Mint (Glows beautifully on dark backgrounds)
val ElectricMint = Color(0xFF5EF1C3)        // AR glow effect, highlights
val ElectricMintLight = Color(0xFF88F5D5)   // Lighter mint
val ElectricMintDark = Color(0xFF3DD9AD)    // Darker mint

// Background & Surfaces - Navy-Tinted Darks (Not Pure Black)
val CharcoalBlue = Color(0xFF0E1726)        // Deep background, easy on eyes
val DeepSlate = Color(0xFF1B2735)           // Card/surface background, adds depth
val ElevatedSlate = Color(0xFF2A3F54)       // Elevated surfaces
val BorderSlate = Color(0xFF334155)         // Borders, dividers

// Text Colors - Soft Contrast
val OffWhite = Color(0xFFE2E8F0)            // Main text, not harsh white
val CoolGray = Color(0xFF94A3B8)            // Secondary info, muted text
val DimGray = Color(0xFF64748B)             // Disabled or tertiary text

// Semantic Colors - Blend with Blue Tones
val SoftEmerald = Color(0xFF34D399)         // Success, correct answers
val GoldenGlow = Color(0xFFFACC15)          // Warnings, attention
val CoralRed = Color(0xFFFB7185)            // Errors, friendly alert

// ═══════════════════════════════════════════════════════════════
// 🎨 LEGACY COMPATIBILITY MAPPING
// Maps old color names to new theme for backwards compatibility
// ═══════════════════════════════════════════════════════════════

// Primary Colors (Purple → Blue Migration)
val SoftPurple = MistyBlue                  // Old purple → New blue
val SoftPurpleLight = MistyBlueLight
val SoftPurpleDark = MistyBlueDark
val BrandIndigo = MistyBlue                 // Old indigo → New blue
val BrandIndigoDark = MistyBlueDark
val PurpleHaze = MistyBlue
val MysteriousPurple = MistyBlue
val MysteriousViolet = MistyBlueLight

// Accent Colors (Teal → Mint Migration)
val SoftTeal = ElectricMint                 // Old teal → New mint
val SoftTealLight = ElectricMintLight
val SoftTealDark = ElectricMintDark
val NeonCyan = ElectricMint                 // Old cyan → New mint
val LightCyan = ElectricMintLight
val MysteriousCyan = ElectricMint

// Background & Surfaces
val DarkNavy = CharcoalBlue                 // Old navy → New charcoal blue
val DeepSpace = CharcoalBlue
val DarkSlate = DeepSlate                   // Kept same name
val DarkGray = ElevatedSlate
val CharcoalGrey = DeepSlate
val DarkGrey = DeepSlate
val LabyrinthDark = CharcoalBlue
val LabyrinthDeep = Color(0xFF0A1219)       // Even deeper variant
val LabyrinthMid = DeepSlate
val LabyrinthLight = ElevatedSlate

// Text Colors
val TextWhite = OffWhite                    // Not pure white
val TextLight = OffWhite
val TextMuted = CoolGray
val TextDisabled = DimGray
val MetallicSilver = CoolGray
val WhiteSmoke = OffWhite
val TextPrimary = OffWhite
val TextSecondary = CoolGray

// Semantic Colors
val EmeraldGreen = SoftEmerald              // Success
val EmeraldGreenLight = Color(0xFF6EE7B7)
val GlowGreen = SoftEmerald
val SuccessGreen = SoftEmerald

val AmberWarm = GoldenGlow                  // Warning
val AmberWarmLight = Color(0xFFFDE047)
val AmberAlert = GoldenGlow
val WarningAmber = GoldenGlow

val RoseSoft = CoralRed                     // Error
val RoseSoftLight = Color(0xFFFDA4AF)
val CrimsonRed = CoralRed
val ErrorRed = CoralRed

// Blue Variants
val ElectricBlue = MistyBlue
val MysteriousBlue = MistyBlueDark

// Gradient Colors (Blue-Mint)
val NeonGradientStart = MistyBlue           // Start with misty blue
val NeonGradientEnd = ElectricMint          // End with electric mint
val DarkGradientStart = CharcoalBlue
val DarkGradientEnd = DeepSlate

// Default Material Colors (for compatibility)
val Purple40 = MistyBlue                    // Maps to blue now
val PurpleGrey40 = Color(0xFF5B6B7A)        // Cool gray variant
val Pink40 = CoralRed                       // Maps to coral