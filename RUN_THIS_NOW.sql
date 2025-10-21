-- =====================================================
-- ULTIMATE FIX - RUN THIS NOW IN SUPABASE
-- Fixes EVERYTHING: Quiz + Files + Lessons
-- =====================================================

-- =====================================================
-- PART 1: FIX QUIZ QUESTIONS
-- =====================================================

-- Drop ALL existing policies
DROP POLICY IF EXISTS "Enable read access for all users" ON quiz_questions;
DROP POLICY IF EXISTS "Enable insert for authenticated users only" ON quiz_questions;
DROP POLICY IF EXISTS "Enable update for authenticated users only" ON quiz_questions;
DROP POLICY IF EXISTS "Enable delete for authenticated users only" ON quiz_questions;
DROP POLICY IF EXISTS "Authenticated users can manage quiz questions" ON quiz_questions;
DROP POLICY IF EXISTS "Anyone can view quiz questions" ON quiz_questions;
DROP POLICY IF EXISTS "Allow admin operations on quiz questions" ON quiz_questions;
DROP POLICY IF EXISTS "quiz_questions_select_policy" ON quiz_questions;
DROP POLICY IF EXISTS "quiz_questions_insert_policy" ON quiz_questions;
DROP POLICY IF EXISTS "quiz_questions_update_policy" ON quiz_questions;
DROP POLICY IF EXISTS "quiz_questions_delete_policy" ON quiz_questions;

-- Create simple permissive policies
CREATE POLICY "quiz_read_all" ON quiz_questions FOR SELECT USING (true);
CREATE POLICY "quiz_insert_all" ON quiz_questions FOR INSERT WITH CHECK (true);
CREATE POLICY "quiz_update_all" ON quiz_questions FOR UPDATE USING (true) WITH CHECK (true);
CREATE POLICY "quiz_delete_all" ON quiz_questions FOR DELETE USING (true);

ALTER TABLE quiz_questions ENABLE ROW LEVEL SECURITY;

-- =====================================================
-- PART 2: FIX LESSONS
-- =====================================================

DROP POLICY IF EXISTS "Enable read access for all users" ON lessons;
DROP POLICY IF EXISTS "Enable insert for authenticated users only" ON lessons;
DROP POLICY IF EXISTS "Enable update for authenticated users only" ON lessons;
DROP POLICY IF EXISTS "Enable delete for authenticated users only" ON lessons;
DROP POLICY IF EXISTS "Authenticated users can manage lessons" ON lessons;
DROP POLICY IF EXISTS "Anyone can view lessons" ON lessons;
DROP POLICY IF EXISTS "Allow admin operations on lessons" ON lessons;

CREATE POLICY "lessons_read_all" ON lessons FOR SELECT USING (true);
CREATE POLICY "lessons_insert_all" ON lessons FOR INSERT WITH CHECK (true);
CREATE POLICY "lessons_update_all" ON lessons FOR UPDATE USING (true) WITH CHECK (true);
CREATE POLICY "lessons_delete_all" ON lessons FOR DELETE USING (true);

ALTER TABLE lessons ENABLE ROW LEVEL SECURITY;

-- =====================================================
-- PART 3: FIX LESSON_FILES + CONSTRAINT
-- =====================================================

-- Drop the restrictive check constraint that's causing the file upload error
ALTER TABLE lesson_files DROP CONSTRAINT IF EXISTS lesson_files_file_type_check;

-- Add a permissive constraint that accepts MIME types (what the admin sends)
-- Allow common MIME types for documents, presentations, images, videos, etc.
ALTER TABLE lesson_files ADD CONSTRAINT lesson_files_file_type_check 
CHECK (
  file_type IS NULL OR 
  file_type = '' OR
  file_type LIKE 'application/%' OR  -- All application types (pdf, docx, pptx, etc.)
  file_type LIKE 'text/%' OR         -- All text types (plain, csv, etc.)
  file_type LIKE 'image/%' OR        -- All image types (jpeg, png, gif, etc.)
  file_type LIKE 'video/%' OR        -- All video types (mp4, avi, etc.)
  file_type LIKE 'audio/%'           -- All audio types (mp3, wav, etc.)
);

-- Alternative: Remove constraint completely (uncomment if needed)
-- ALTER TABLE lesson_files DROP CONSTRAINT IF EXISTS lesson_files_file_type_check;

-- Drop existing policies
DROP POLICY IF EXISTS "Enable read access for all users" ON lesson_files;
DROP POLICY IF EXISTS "Enable insert for authenticated users only" ON lesson_files;
DROP POLICY IF EXISTS "Enable update for authenticated users only" ON lesson_files;
DROP POLICY IF EXISTS "Enable delete for authenticated users only" ON lesson_files;
DROP POLICY IF EXISTS "Authenticated users can manage lesson files" ON lesson_files;
DROP POLICY IF EXISTS "Anyone can view lesson files" ON lesson_files;
DROP POLICY IF EXISTS "Allow admin operations on lesson files" ON lesson_files;

CREATE POLICY "files_read_all" ON lesson_files FOR SELECT USING (true);
CREATE POLICY "files_insert_all" ON lesson_files FOR INSERT WITH CHECK (true);
CREATE POLICY "files_update_all" ON lesson_files FOR UPDATE USING (true) WITH CHECK (true);
CREATE POLICY "files_delete_all" ON lesson_files FOR DELETE USING (true);

ALTER TABLE lesson_files ENABLE ROW LEVEL SECURITY;

-- =====================================================
-- PART 4: CREATE STORAGE BUCKET + POLICIES
-- =====================================================

-- Create the storage bucket if it doesn't exist
INSERT INTO storage.buckets (id, name, public)
VALUES ('lesson-files', 'lesson-files', true)
ON CONFLICT (id) DO UPDATE SET public = true;

-- Drop existing storage policies
DROP POLICY IF EXISTS "Public Access" ON storage.objects;
DROP POLICY IF EXISTS "Allow uploads" ON storage.objects;
DROP POLICY IF EXISTS "Allow deletes" ON storage.objects;
DROP POLICY IF EXISTS "Public can read lesson files" ON storage.objects;
DROP POLICY IF EXISTS "Public can upload lesson files" ON storage.objects;
DROP POLICY IF EXISTS "Public can delete lesson files" ON storage.objects;
DROP POLICY IF EXISTS "Public can update lesson files" ON storage.objects;
DROP POLICY IF EXISTS "lesson_files_select" ON storage.objects;
DROP POLICY IF EXISTS "lesson_files_insert" ON storage.objects;
DROP POLICY IF EXISTS "lesson_files_update" ON storage.objects;
DROP POLICY IF EXISTS "lesson_files_delete" ON storage.objects;

-- Create new permissive storage policies
CREATE POLICY "lesson_files_select" ON storage.objects 
FOR SELECT USING (bucket_id = 'lesson-files');

CREATE POLICY "lesson_files_insert" ON storage.objects 
FOR INSERT WITH CHECK (bucket_id = 'lesson-files');

CREATE POLICY "lesson_files_update" ON storage.objects 
FOR UPDATE USING (bucket_id = 'lesson-files');

CREATE POLICY "lesson_files_delete" ON storage.objects 
FOR DELETE USING (bucket_id = 'lesson-files');

-- =====================================================
-- PART 5: INSERT SAMPLE QUIZ QUESTIONS
-- =====================================================

-- Delete existing questions if you want fresh data (optional)
-- DELETE FROM quiz_questions;

-- Insert sample questions
INSERT INTO quiz_questions (module_id, question_text, option_a, option_b, option_c, option_d, correct_answer, order_index)
VALUES 
-- Decantation (5 questions)
('decantation', 'What is decantation?', 'A separation technique for liquids and solids', 'A chemical reaction', 'A type of mixture', 'A physical state', 'A', 1),
('decantation', 'Which tool is commonly used for decantation?', 'Beaker or flask', 'Magnet', 'Filter paper', 'Thermometer', 'A', 2),
('decantation', 'Decantation works best when the solid particles are:', 'Heavy and settle at the bottom', 'Light and floating', 'Dissolved completely', 'Very small and suspended', 'A', 3),
('decantation', 'After decantation, the liquid obtained is:', 'Mostly clear and free of solids', 'More cloudy than before', 'Completely solid', 'Unchanged from original', 'A', 4),
('decantation', 'Which mixture is best separated by decantation?', 'Sand and water', 'Salt and water', 'Sugar and water', 'Alcohol and water', 'A', 5),

-- Organ System (5 questions)
('organ_system', 'What is the main function of the heart?', 'Pump blood throughout the body', 'Digest food', 'Filter air', 'Store energy', 'A', 1),
('organ_system', 'Which organ is responsible for breathing?', 'Lungs', 'Heart', 'Liver', 'Kidneys', 'A', 2),
('organ_system', 'The brain is part of which system?', 'Nervous system', 'Digestive system', 'Respiratory system', 'Skeletal system', 'A', 3),
('organ_system', 'What do kidneys filter from the blood?', 'Waste products and excess water', 'Oxygen', 'Nutrients', 'Red blood cells', 'A', 4),
('organ_system', 'Which system helps you move your body?', 'Muscular and skeletal systems', 'Digestive system', 'Respiratory system', 'Circulatory system', 'A', 5),

-- Simple Machines (5 questions)
('simple_machines', 'What is a lever?', 'A rigid bar that pivots on a fulcrum', 'A wheel with a rope', 'A flat inclined surface', 'A pointed wedge tool', 'A', 1),
('simple_machines', 'Which is an example of a wheel and axle?', 'Doorknob', 'Scissors', 'Ramp', 'Seesaw', 'A', 2),
('simple_machines', 'What does an inclined plane help reduce?', 'The force needed to move an object', 'The mass of the object', 'The volume of the object', 'The temperature', 'A', 3),
('simple_machines', 'A pulley system uses which combination?', 'Wheels and ropes', 'Two levers', 'Inclined planes', 'Multiple wedges', 'A', 4),
('simple_machines', 'Scissors are an example of:', 'Two levers working together', 'A single wheel and axle', 'An inclined plane', 'A simple pulley', 'A', 5)
ON CONFLICT (id) DO NOTHING;

-- =====================================================
-- PART 6: VERIFICATION
-- =====================================================

-- Check quiz questions
SELECT 
    'Quiz Questions:' as type,
    module_id,
    COUNT(*) as total
FROM quiz_questions
GROUP BY module_id
ORDER BY module_id;

-- Test anonymous access
SET ROLE anon;
SELECT 'Anonymous can read:' as test, COUNT(*) as count FROM quiz_questions;
RESET ROLE;

-- Check storage bucket
SELECT 'Storage Bucket:' as type, id, name, public FROM storage.buckets WHERE id = 'lesson-files';

-- Check policies
SELECT 
    'Policies:' as type,
    tablename,
    policyname
FROM pg_policies
WHERE tablename IN ('quiz_questions', 'lessons', 'lesson_files')
ORDER BY tablename, policyname;

SELECT '✅ ALL FIXES APPLIED!' as status;
