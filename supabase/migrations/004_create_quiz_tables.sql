-- =====================================================
-- Quiz Feature - Database Schema
-- Migration: 004_create_quiz_tables.sql
-- Run this in Supabase SQL Editor!
-- =====================================================

-- =====================================================
-- 1. QUIZ QUESTIONS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS quiz_questions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    module_id TEXT NOT NULL CHECK (module_id IN ('decantation', 'organ_system', 'simple_machines', 'solar_system')),
    question_text TEXT NOT NULL,
    option_a TEXT NOT NULL,
    option_b TEXT NOT NULL,
    option_c TEXT NOT NULL,
    option_d TEXT NOT NULL,
    correct_answer TEXT NOT NULL CHECK (correct_answer IN ('A', 'B', 'C', 'D')),
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Index for faster queries
CREATE INDEX IF NOT EXISTS idx_quiz_questions_module ON quiz_questions(module_id);
CREATE INDEX IF NOT EXISTS idx_quiz_questions_order ON quiz_questions(module_id, order_index);

-- RLS Policies for quiz_questions
ALTER TABLE quiz_questions ENABLE ROW LEVEL SECURITY;

-- Anyone can view quiz questions (students need to see them!)
DROP POLICY IF EXISTS "Anyone can view quiz questions" ON quiz_questions;
CREATE POLICY "Anyone can view quiz questions"
    ON quiz_questions FOR SELECT
    USING (true);

-- Authenticated users can manage quiz questions
DROP POLICY IF EXISTS "Authenticated users can manage quiz questions" ON quiz_questions;
CREATE POLICY "Authenticated users can manage quiz questions"
    ON quiz_questions FOR ALL
    USING (auth.role() = 'authenticated');

-- =====================================================
-- 2. QUIZ RESULTS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS quiz_results (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    question_id UUID NOT NULL REFERENCES quiz_questions(id) ON DELETE CASCADE,
    module_id TEXT NOT NULL CHECK (module_id IN ('decantation', 'organ_system', 'simple_machines', 'solar_system')),
    selected_answer TEXT NOT NULL CHECK (selected_answer IN ('A', 'B', 'C', 'D')),
    is_correct BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Index for faster queries
CREATE INDEX IF NOT EXISTS idx_quiz_results_user ON quiz_results(user_id);
CREATE INDEX IF NOT EXISTS idx_quiz_results_question ON quiz_results(question_id);
CREATE INDEX IF NOT EXISTS idx_quiz_results_module ON quiz_results(module_id);
CREATE INDEX IF NOT EXISTS idx_quiz_results_user_module ON quiz_results(user_id, module_id);

-- RLS Policies for quiz_results
ALTER TABLE quiz_results ENABLE ROW LEVEL SECURITY;

-- Users can view own quiz results
DROP POLICY IF EXISTS "Users can view own quiz results" ON quiz_results;
CREATE POLICY "Users can view own quiz results"
    ON quiz_results FOR SELECT
    USING (auth.uid() = user_id);

-- Users can insert own quiz results
DROP POLICY IF EXISTS "Users can insert own quiz results" ON quiz_results;
CREATE POLICY "Users can insert own quiz results"
    ON quiz_results FOR INSERT
    WITH CHECK (auth.uid() = user_id);

-- =====================================================
-- 3. TRIGGERS FOR UPDATED_AT
-- =====================================================
DROP TRIGGER IF EXISTS update_quiz_questions_updated_at ON quiz_questions;
CREATE TRIGGER update_quiz_questions_updated_at
    BEFORE UPDATE ON quiz_questions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- 4. SAMPLE QUIZ QUESTIONS (For Testing)
-- =====================================================

-- DECANTATION MODULE
INSERT INTO quiz_questions (module_id, question_text, option_a, option_b, option_c, option_d, correct_answer, order_index)
VALUES 
    ('decantation', 'What is decantation?', 'A method of mixing liquids together', 'A method of separating liquids by carefully pouring', 'A method of heating liquids', 'A method of freezing liquids', 'B', 1),
    ('decantation', 'When is decantation most commonly used?', 'To mix oil and water', 'To separate a solid settled at the bottom from a liquid', 'To combine two liquids', 'To dissolve solids in liquids', 'B', 2),
    ('decantation', 'What is the purpose of letting a mixture stand before decanting?', 'To heat the mixture', 'To allow the heavier particles to settle at the bottom', 'To mix it thoroughly', 'To cool it down', 'B', 3),
    ('decantation', 'Which of the following can be separated using decantation?', 'Salt dissolved in water', 'Sand and water', 'Sugar and water', 'Air and water', 'B', 4),
    ('decantation', 'What happens to the sediment during decantation?', 'It evaporates', 'It dissolves', 'It remains at the bottom of the container', 'It floats to the top', 'C', 5)
ON CONFLICT DO NOTHING;

-- ORGAN SYSTEM MODULE
INSERT INTO quiz_questions (module_id, question_text, option_a, option_b, option_c, option_d, correct_answer, order_index)
VALUES 
    ('organ_system', 'Which organ is responsible for pumping blood throughout the body?', 'Liver', 'Lungs', 'Heart', 'Brain', 'C', 1),
    ('organ_system', 'What is the main function of the respiratory system?', 'To digest food', 'To circulate blood', 'To exchange oxygen and carbon dioxide', 'To produce hormones', 'C', 2),
    ('organ_system', 'Which system helps break down food into nutrients?', 'Circulatory system', 'Digestive system', 'Nervous system', 'Skeletal system', 'B', 3),
    ('organ_system', 'What organ filters waste from the blood?', 'Stomach', 'Heart', 'Kidneys', 'Lungs', 'C', 4),
    ('organ_system', 'The brain is part of which organ system?', 'Digestive system', 'Respiratory system', 'Nervous system', 'Muscular system', 'C', 5),
    ('organ_system', 'Which organ system provides structure and support to the body?', 'Muscular system', 'Skeletal system', 'Circulatory system', 'Digestive system', 'B', 6)
ON CONFLICT DO NOTHING;

-- SIMPLE MACHINES MODULE
INSERT INTO quiz_questions (module_id, question_text, option_a, option_b, option_c, option_d, correct_answer, order_index)
VALUES 
    ('simple_machines', 'A lever is an example of which type of machine?', 'Complex machine', 'Simple machine', 'Electronic device', 'Chemical tool', 'B', 1),
    ('simple_machines', 'Which of the following is a simple machine?', 'Computer', 'Smartphone', 'Pulley', 'Car engine', 'C', 2),
    ('simple_machines', 'What is the main purpose of simple machines?', 'To make work more complicated', 'To make work easier by changing force or direction', 'To generate electricity', 'To create heat', 'B', 3),
    ('simple_machines', 'A ramp is an example of which simple machine?', 'Lever', 'Pulley', 'Inclined plane', 'Wheel and axle', 'C', 4),
    ('simple_machines', 'Which simple machine would you use to lift a heavy flag?', 'Wedge', 'Screw', 'Pulley', 'Lever', 'C', 5),
    ('simple_machines', 'How many types of simple machines are there?', 'Four', 'Five', 'Six', 'Seven', 'C', 6)
ON CONFLICT DO NOTHING;

-- SOLAR SYSTEM MODULE
INSERT INTO quiz_questions (module_id, question_text, option_a, option_b, option_c, option_d, correct_answer, order_index)
VALUES 
    ('solar_system', 'How many planets are in our solar system?', '7', '8', '9', '10', 'B', 1),
    ('solar_system', 'Which planet is closest to the Sun?', 'Venus', 'Earth', 'Mercury', 'Mars', 'C', 2),
    ('solar_system', 'Which is the largest planet in our solar system?', 'Saturn', 'Earth', 'Jupiter', 'Neptune', 'C', 3),
    ('solar_system', 'What is at the center of our solar system?', 'Earth', 'The Moon', 'The Sun', 'Jupiter', 'C', 4),
    ('solar_system', 'Which planet is known as the Red Planet?', 'Venus', 'Mars', 'Mercury', 'Jupiter', 'B', 5),
    ('solar_system', 'Which planet has the most visible rings?', 'Jupiter', 'Uranus', 'Saturn', 'Neptune', 'C', 6),
    ('solar_system', 'What is Earth''s natural satellite called?', 'The Sun', 'The Moon', 'A star', 'An asteroid', 'B', 7)
ON CONFLICT DO NOTHING;

-- =====================================================
-- 5. VERIFICATION QUERY
-- =====================================================
-- Run this to verify the tables were created successfully:
-- SELECT table_name FROM information_schema.tables 
-- WHERE table_schema = 'public' AND table_name IN ('quiz_questions', 'quiz_results');

-- Count quiz questions per module:
-- SELECT module_id, COUNT(*) as question_count 
-- FROM quiz_questions 
-- GROUP BY module_id 
-- ORDER BY module_id;

-- =====================================================
-- END OF MIGRATION
-- =====================================================
