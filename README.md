# ESCAPE AR

An Android AR application for interactive educational quizzes with Unity integration.

## Features

- AR-based learning modules
- Quiz system with progress tracking
- User authentication and profile management
- Real-time analytics and scoring
- Unity-powered AR experiences

## Tech Stack

- **Android**: Kotlin, Jetpack Compose
- **Backend**: Supabase (PostgreSQL + Auth)
- **AR**: Unity with ARCore
- **Architecture**: MVVM with Repository pattern

## Setup

1. Clone the repository
2. Configure `local.properties` with your Supabase credentials:
   ```
   SUPABASE_URL=your_supabase_url
   SUPABASE_ANON_KEY=your_supabase_anon_key
   ```
3. Build and run the project

## Database Schema

Essential SQL setup files:
- `complete_schema_final.sql` - Complete database schema
- `fresh_database_setup.sql` - Fresh installation setup

## Build

```bash
./gradlew assembleDebug installDebug
```
