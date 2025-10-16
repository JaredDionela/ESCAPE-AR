# E.S.C.A.P.E. AR
**Enhanced Science Comprehension through Augmented and Playful Education**

An Android educational application designed to teach science concepts through interactive quizzes and planned AR experiences.

> **Note**: This repository contains the Android app without Unity integration files (excluded due to GitHub file size limits). The app is fully functional for quiz-based learning.

## ✨ Features

### Current Features
- 🎓 **4 Science Modules**: Decantation, Organ Systems, Simple Machines, Solar System
- 📝 **Interactive Quizzes** with progress tracking and scoring
- 👤 **User Authentication** with profile management (teacher name, section)
- 📊 **Analytics Dashboard** to track learning progress
- ⚙️ **Settings** with analytics preferences
- 🎨 **Modern UI** following Cognitive Load Theory - simplified and educational

### Planned Features
- 🔮 **AR Experiences** with Kylon, your interactive science guide (requires Unity integration)
- 📱 **Augmented Reality** 3D visualizations of scientific concepts

## 🛠 Tech Stack

- **Android**: Kotlin, Jetpack Compose
- **Backend**: Supabase (PostgreSQL + Authentication)
- **Architecture**: MVVM with Repository pattern
- **UI**: Material Design 3
- **Async**: Coroutines & Flow
- **AR**: Unity with ARCore (not included in this repository)

## 📱 Screenshots

- **Simplified Lobby**: Learning Progress, AR Experience card, Science Quizzes
- **Module Selection**: View all 4 science modules with descriptions
- **Quiz Interface**: Interactive questions with immediate feedback
- **Profile Management**: Edit teacher name, section, display name

## 🚀 Setup

### Prerequisites
- Android Studio Hedgehog or newer
- Android SDK 24+ (target SDK 34)
- Supabase account

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/JaredDionela/ESCAPE-AR.git
   cd ESCAPE-AR
   git checkout clean-dev
   ```

2. **Configure Supabase credentials**
   
   Create or edit `local.properties` in the project root:
   ```properties
   SUPABASE_URL=your_supabase_project_url
   SUPABASE_ANON_KEY=your_supabase_anon_key
   ```

3. **Set up the database**
   
   Run these SQL files in your Supabase SQL editor (in order):
   - `complete_schema_final.sql` - Complete database schema
   - `fresh_database_setup.sql` - Initial data setup

4. **Build and run**
   ```bash
   ./gradlew assembleDebug
   ./gradlew installDebug
   ```
   
   Or open in Android Studio and click Run ▶️

## 📁 Project Structure

```
app/src/main/java/com/example/escape_ar/
├── data/
│   ├── model/          # Data models (SupabaseModels, Models)
│   ├── repository/     # Data layer (UserRepository, QuizRepository, SettingsRepository)
│   ├── SessionManager  # Session and token management
│   └── SupabaseConfig  # Supabase client configuration
├── ui/
│   ├── screens/        # Compose UI screens
│   └── theme/          # Material Design 3 theme
├── viewmodel/          # ViewModels (AuthViewModel, ProfileViewModel, QuizViewModel)
├── navigation/         # Navigation setup
└── MainActivity        # App entry point
```

## 🗄 Database Schema

### Tables
- `profiles` - Extended user profiles (teacher name, section, avatar)
- `analytics_settings` - User analytics preferences
- `user_module_progress` - Quiz progress and scores
- `modules` - Learning modules (Decantation, Organ Systems, etc.)
- `questions` - Quiz questions for each module
- `user_responses` - User answers and timestamps

See `complete_schema_final.sql` for full schema details.

## 🎯 Key Changes (Latest Update)

### UI Simplification
- ✅ Removed mission storyline narrative
- ✅ Changed to educational terminology (Sign In/Up, Start Quiz)
- ✅ Redesigned lobby with 3 clear cards
- ✅ Single "View Modules" button for cleaner navigation
- ✅ Added AR experience description with Kylon science guide

### Bug Fixes
- ✅ Fixed teacher name/section not displaying after signup
- ✅ Fixed navigation crash when clicking "View Modules"
- ✅ Fixed profile creation with email field validation

## 🤝 Contributing

This is an educational project. Contributions are welcome!

## 📄 License

Educational project - see project guidelines.

## 🔗 Links

- **Repository**: [ESCAPE-AR on GitHub](https://github.com/JaredDionela/ESCAPE-AR)
- **Branch**: `clean-dev` (without Unity files)
- **Issues**: [Report bugs or request features](https://github.com/JaredDionela/ESCAPE-AR/issues)

---

**Note on Unity Integration**: Due to GitHub's file size limitations, Unity AR integration files are not included in this repository. The app currently functions as a quiz-based learning platform. For AR features, Unity must be integrated separately.
