-- =====================================================
-- Make duration_minutes optional in lessons table
-- Migration: 006_make_duration_optional.sql
-- =====================================================

-- Make duration_minutes column nullable and default to NULL instead of 0
ALTER TABLE lessons 
ALTER COLUMN duration_minutes DROP NOT NULL,
ALTER COLUMN duration_minutes SET DEFAULT NULL;

-- Update existing rows with 0 to NULL for cleaner data (optional)
UPDATE lessons 
SET duration_minutes = NULL 
WHERE duration_minutes = 0;
