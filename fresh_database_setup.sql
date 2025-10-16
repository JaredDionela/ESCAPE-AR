-- ====================================================================
-- ESCAPE AR - Fresh Database Setup
-- Copy and paste this entire code into your Supabase SQL editor
-- ====================================================================

-- Clean slate: Drop everything first (if exists)
DROP VIEW IF EXISTS public.aggregate_user_progress CASCADE;
DROP TABLE IF EXISTS public.progress CASCADE;
DROP TABLE IF EXISTS public.profiles CASCADE;
DROP TYPE IF EXISTS public.module_code CASCADE;
DROP FUNCTION IF EXISTS public.set_updated_at() CASCADE;
DROP FUNCTION IF EXISTS public.handle_new_user() CASCADE;

-- Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Module enum (matches your app code exactly)
CREATE TYPE public.module_code AS ENUM (
  'decantation',
  'organ_system',
  'simple_machines',
  'solar_system'
);

-- Profiles table
CREATE TABLE public.profiles (
  id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
  email TEXT NOT NULL,
  full_name TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Progress table
CREATE TABLE public.progress (
  id BIGSERIAL PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
  module public.module_code NOT NULL,
  best_score INTEGER NOT NULL DEFAULT 0 CHECK (best_score >= 0 AND best_score <= 100),
  completed BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT progress_user_module_unique UNIQUE (user_id, module)
);

-- Updated_at trigger function
CREATE OR REPLACE FUNCTION public.set_updated_at()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END; $$;

-- Apply updated_at triggers
CREATE TRIGGER trg_profiles_updated_at
  BEFORE UPDATE ON public.profiles
  FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_progress_updated_at
  BEFORE UPDATE ON public.progress
  FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

-- Auto-create profile when user signs up
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER LANGUAGE plpgsql SECURITY DEFINER SET search_path = public AS $$
BEGIN
  INSERT INTO public.profiles (id, email, full_name)
  VALUES (
    NEW.id, 
    NEW.email, 
    COALESCE(NEW.raw_user_meta_data->>'full_name', SPLIT_PART(NEW.email, '@', 1))
  )
  ON CONFLICT (id) DO NOTHING;
  RETURN NEW;
END; $$;

CREATE TRIGGER on_auth_user_created
  AFTER INSERT ON auth.users
  FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- Indexes for performance
CREATE UNIQUE INDEX profiles_email_unique ON public.profiles (LOWER(email));
CREATE INDEX idx_progress_user_id ON public.progress (user_id);
CREATE INDEX idx_progress_user_module ON public.progress (user_id, module);

-- Row Level Security
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.progress ENABLE ROW LEVEL SECURITY;

-- Profiles policies
CREATE POLICY "Profiles Select Own" ON public.profiles
  FOR SELECT USING (auth.uid() = id);

CREATE POLICY "Profiles Insert Self" ON public.profiles
  FOR INSERT WITH CHECK (auth.uid() = id);

CREATE POLICY "Profiles Update Own" ON public.profiles
  FOR UPDATE USING (auth.uid() = id) WITH CHECK (auth.uid() = id);

CREATE POLICY "Profiles Delete Own" ON public.profiles
  FOR DELETE USING (auth.uid() = id);

-- Progress policies
CREATE POLICY "Progress Select Own" ON public.progress
  FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Progress Insert Own" ON public.progress
  FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Progress Update Own" ON public.progress
  FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Progress Delete Own" ON public.progress
  FOR DELETE USING (auth.uid() = user_id);

-- Analytics view for dashboard
CREATE VIEW public.aggregate_user_progress AS
SELECT
  p.id AS user_id,
  COALESCE(p.full_name, SPLIT_PART(p.email, '@', 1)) AS display_name,
  COUNT(pr.*) FILTER (WHERE pr.completed) AS modules_completed,
  COUNT(pr.*) AS modules_tracked,
  CASE 
    WHEN COUNT(pr.*) = 0 THEN 0
    ELSE ROUND((COUNT(pr.*) FILTER (WHERE pr.completed)::NUMERIC / COUNT(pr.*)) * 100)::INTEGER
  END AS completion_percent,
  COALESCE(AVG(pr.best_score) FILTER (WHERE pr.completed), 0)::INTEGER AS average_score,
  MAX(pr.updated_at) AS last_activity
FROM public.profiles p
LEFT JOIN public.progress pr ON pr.user_id = p.id
GROUP BY p.id, p.full_name, p.email;

-- Test the setup with some sample data (optional)
/*
-- Uncomment these lines to insert test data:

INSERT INTO auth.users (id, email, raw_user_meta_data, email_confirmed_at, created_at, updated_at)
VALUES (
  '12345678-1234-1234-1234-123456789012',
  'test@example.com',
  '{"full_name": "Test Student"}',
  NOW(),
  NOW(),
  NOW()
) ON CONFLICT (id) DO NOTHING;

INSERT INTO public.progress (user_id, module, best_score, completed)
VALUES 
  ('12345678-1234-1234-1234-123456789012', 'decantation', 85, true),
  ('12345678-1234-1234-1234-123456789012', 'organ_system', 92, true)
ON CONFLICT (user_id, module) DO UPDATE SET
  best_score = GREATEST(excluded.best_score, public.progress.best_score),
  completed = public.progress.completed OR excluded.completed,
  updated_at = NOW();
*/
