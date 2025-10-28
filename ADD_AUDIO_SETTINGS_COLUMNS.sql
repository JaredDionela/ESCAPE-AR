-- =====================================================
-- Audio Settings Columns for Supabase Profiles Table
-- =====================================================
-- Run this SQL in your Supabase SQL Editor to add
-- audio settings storage to the profiles table
-- =====================================================

-- Add audio settings columns to profiles table
ALTER TABLE public.profiles
ADD COLUMN IF NOT EXISTS background_music BOOLEAN DEFAULT true,
ADD COLUMN IF NOT EXISTS music_volume REAL DEFAULT 0.5,
ADD COLUMN IF NOT EXISTS sound_effects BOOLEAN DEFAULT true,
ADD COLUMN IF NOT EXISTS effects_volume REAL DEFAULT 0.7,
ADD COLUMN IF NOT EXISTS wifi_only BOOLEAN DEFAULT false;

-- Add comment to document the columns
COMMENT ON COLUMN public.profiles.background_music IS 'Enable/disable background music in the app';
COMMENT ON COLUMN public.profiles.music_volume IS 'Background music volume (0.0-1.0 range)';
COMMENT ON COLUMN public.profiles.sound_effects IS 'Enable/disable sound effects in the app';
COMMENT ON COLUMN public.profiles.effects_volume IS 'Sound effects volume (0.0-1.0 range)';
COMMENT ON COLUMN public.profiles.wifi_only IS 'Download AR modules only on Wi-Fi';

-- Verify the columns were added
SELECT column_name, data_type, column_default 
FROM information_schema.columns 
WHERE table_schema = 'public' 
  AND table_name = 'profiles'
  AND column_name IN ('background_music', 'music_volume', 'sound_effects', 'effects_volume', 'wifi_only');

-- =====================================================
-- Expected Output:
-- =====================================================
-- column_name       | data_type | column_default
-- ------------------+-----------+----------------
-- background_music  | boolean   | true
-- music_volume      | real      | 0.5
-- sound_effects     | boolean   | true
-- effects_volume    | real      | 0.7
-- wifi_only         | boolean   | false
-- =====================================================

-- Optional: Update existing profiles with default values (if needed)
-- UPDATE public.profiles
-- SET 
--   background_music = COALESCE(background_music, true),
--   music_volume = COALESCE(music_volume, 0.5),
--   sound_effects = COALESCE(sound_effects, true),
--   effects_volume = COALESCE(effects_volume, 0.7),
--   wifi_only = COALESCE(wifi_only, false)
-- WHERE 
--   background_music IS NULL 
--   OR music_volume IS NULL 
--   OR sound_effects IS NULL 
--   OR effects_volume IS NULL 
--   OR wifi_only IS NULL;
