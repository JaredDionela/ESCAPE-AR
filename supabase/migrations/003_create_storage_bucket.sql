-- =====================================================
-- SUPABASE STORAGE SETUP FOR WEB ADMIN
-- Run this in your Supabase SQL Editor
-- =====================================================

-- 1. Create storage bucket for lesson files
INSERT INTO storage.buckets (id, name, public)
VALUES ('lesson-files', 'lesson-files', true)
ON CONFLICT (id) DO NOTHING;

-- 2. Set up storage policies for lesson files

-- Allow public read access (so students can download)
CREATE POLICY "Public can view lesson files"
ON storage.objects FOR SELECT
USING (bucket_id = 'lesson-files');

-- Allow authenticated users to upload files
CREATE POLICY "Authenticated users can upload lesson files"
ON storage.objects FOR INSERT
WITH CHECK (bucket_id = 'lesson-files' AND auth.role() = 'authenticated');

-- Allow authenticated users to update files
CREATE POLICY "Authenticated users can update lesson files"
ON storage.objects FOR UPDATE
USING (bucket_id = 'lesson-files' AND auth.role() = 'authenticated');

-- Allow authenticated users to delete files
CREATE POLICY "Authenticated users can delete lesson files"
ON storage.objects FOR DELETE
USING (bucket_id = 'lesson-files' AND auth.role() = 'authenticated');

-- 3. Verify bucket was created
SELECT * FROM storage.buckets WHERE id = 'lesson-files';

-- Expected output:
-- id            | name          | owner | public | ...
-- lesson-files  | lesson-files  | NULL  | true   | ...

-- =====================================================
-- DONE! Your web admin can now upload files.
-- =====================================================

-- Optional: View all storage policies
SELECT * FROM pg_policies WHERE tablename = 'objects';
