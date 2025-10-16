-- ====================================================================
-- ESCAPE AR - Complete Production-Ready Supabase Schema
-- Run this in your Supabase SQL editor for a clean setup
-- ====================================================================

-- Extensions
create extension if not exists "uuid-ossp";
create extension if not exists "pgcrypto";

-- Module enum with your actual module names (MATCHES APP CODE)
create type public.module_code as enum (
  'decantation',
  'organ_system',           -- Singular form (app updated to match)
  'simple_machines',
  'solar_system'
);

-- Profiles table (replaces user_profiles, matches your UserRepository)
create table public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  email text not null,
  full_name text,           -- This is the "agent code name" your app expects
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

-- Progress table (replaces quiz_scores, normalized per user+module)
create table public.progress (
  id bigserial primary key,
  user_id uuid not null references public.profiles(id) on delete cascade,
  module public.module_code not null,
  best_score int not null default 0 check (best_score >= 0 and best_score <= 100),
  completed boolean not null default false,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  constraint progress_user_module_unique unique (user_id, module)
);

-- Updated_at trigger function
create or replace function public.set_updated_at()
returns trigger language plpgsql as $$
begin
  new.updated_at = now();
  return new;
end; $$;

-- Apply updated_at triggers
create trigger trg_profiles_updated_at
before update on public.profiles
for each row execute function public.set_updated_at();

create trigger trg_progress_updated_at  
before update on public.progress
for each row execute function public.set_updated_at();

-- Auto-create profile when user signs up
create or replace function public.handle_new_user()
returns trigger language plpgsql security definer set search_path=public as $$
begin
  insert into public.profiles (id, email, full_name)
  values (new.id, new.email, coalesce(new.raw_user_meta_data->>'full_name', split_part(new.email,'@',1)))
  on conflict (id) do nothing;
  return new;
end; $$;

create trigger on_auth_user_created
after insert on auth.users  
for each row execute function public.handle_new_user();

-- Indexes for performance
create unique index if not exists profiles_email_unique on public.profiles (lower(email));
create index if not exists idx_progress_user_id on public.progress (user_id);
create index if not exists idx_progress_user_module on public.progress (user_id, module);

-- Row Level Security
alter table public.profiles enable row level security;
alter table public.progress enable row level security;

-- Profiles policies
create policy "Profiles Select Own" on public.profiles
for select using (auth.uid() = id);

create policy "Profiles Insert Self" on public.profiles  
for insert with check (auth.uid() = id);

create policy "Profiles Update Own" on public.profiles
for update using (auth.uid() = id) with check (auth.uid() = id);

create policy "Profiles Delete Own" on public.profiles
for delete using (auth.uid() = id);

-- Progress policies  
create policy "Progress Select Own" on public.progress
for select using (auth.uid() = user_id);

create policy "Progress Insert Own" on public.progress
for insert with check (auth.uid() = user_id);

create policy "Progress Update Own" on public.progress
for update using (auth.uid() = user_id) with check (auth.uid() = user_id);

create policy "Progress Delete Own" on public.progress  
for delete using (auth.uid() = user_id);

-- Analytics view for dashboard
create or replace view public.aggregate_user_progress as
select
  p.id as user_id,
  coalesce(p.full_name, split_part(p.email, '@', 1)) as display_name,
  count(pr.*) filter (where pr.completed) as modules_completed,
  count(pr.*) as modules_tracked,
  case when count(pr.*) = 0 then 0
       else round((count(pr.*) filter (where pr.completed)::numeric / count(pr.*)) * 100)::int 
  end as completion_percent,
  coalesce(avg(pr.best_score) filter (where pr.completed), 0)::int as average_score,
  max(pr.updated_at) as last_activity
from public.profiles p
left join public.progress pr on pr.user_id = p.id  
group by p.id, p.full_name, p.email;

-- Sample data insertion pattern for your app:
-- insert into public.progress (user_id, module, best_score, completed)
-- values (auth.uid(), 'decantation', 85, true)
-- on conflict (user_id, module) do update
-- set best_score = greatest(excluded.best_score, public.progress.best_score),
--     completed = public.progress.completed or excluded.completed,
--     updated_at = now();
