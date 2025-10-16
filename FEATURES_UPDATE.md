# 📱 Science Learning App — Feature Update Specification (Kotlin + Supabase)

This document outlines the required new features and UI/UX improvements for the **existing Kotlin-based Science Learning App**.  
These instructions are designed for implementation using **GitHub Copilot Agent** and integration with **Supabase** for backend services.

---

## 🧩 Overview

**Goal:**  
Enhance the existing Science Learning App by adding YouTube video lessons, DepEd lesson content, a simplified interface, new settings options, editable profile fields connected to Supabase, and a structured placeholder system for all image assets.

---

## 🎥 1. Video Lessons & DepEd Lessons Screen

- Add a new screen where students can **watch YouTube videos** for the four science topics:
  - *Decantation*
  - *Organ System*
  - *Simple Machines*
  - *Solar System*
- Use the [`android-youtube-player`](https://github.com/PierfrancescoSoffritti/android-youtube-player) library to play videos directly inside the app (not in an external browser).
- Below each video, include:
  - A **short topic summary**
  - A **“Check Understanding”** quiz button
  - A **“View DepEd Lesson”** button showing simplified visual content  
- DepEd Lessons should be **visual and concise**, using short text, icons, or diagrams instead of paragraphs.

---

## 🧠 2. Simplified Interface (Sweller’s Cognitive Load Theory)

- Redesign screens to be **more visual and less text-heavy**.
- Present **one key idea per screen** to reduce cognitive overload.
- Use icons, animations, and visual cues instead of decorative or lengthy text.
- Keep UI layouts clean, minimal, and easy to navigate.

---

## ⚙️ 3. Settings Page

- Add a **Settings** screen with:
  - **Music Volume** and **SFX Volume** sliders
  - **Captions On/Off** toggle
  - **Reset to Default Settings** option  
- Settings should be **saved and loaded** using Supabase.

---

## 👤 4. Profile & Sign-Up Update (Supabase Integration)

- On the **Sign-Up Page**, add input fields for:
  - **Student Name**
  - **Teacher’s Name**
  - **Section**
- Store this data using **Supabase Authentication + Database**.
- On the **Profile Page**, allow users to:
  - View their current information
  - **Edit their Name, Teacher, and Section anytime**
- Updates should sync automatically with Supabase.

---

## 🗂️ 5. Assets & Placeholders

- Use **placeholder images and icons** for all UI visuals (videos, buttons, topics, etc.).
- Organize all assets in a clean folder structure:

```
/res
 ├── drawable/
 │    ├── placeholder_icons/
 │    ├── topic_thumbnails/
 │    └── ui_elements/
 └── raw/
      ├── sound_effects/
      └── music/
```

- All placeholder files must have **clear, consistent names** so they can easily be replaced later.
  - Example filenames:
    - `icon_play_placeholder.svg`
    - `topic_decantation_placeholder.png`
    - `thumb_solarsystem_placeholder.png`

---

## 🔗 6. Supabase Integration Details

Use Supabase for authentication, profiles, and progress tracking.

### Example Tables

- `profiles (id, display_name, teacher_name, section, avatar_url, created_at)`
- `videos (id, topic, title, youtube_id, thumbnail_url, duration_seconds, summary)`
- `deped_lessons (id, topic, short_summary, full_text, resource_url)`
- `video_progress (id, user_id, video_id, watched_seconds, completed, updated_at)`
- `user_settings (id, user_id, music_volume, sfx_volume, captions_enabled, updated_at)`

### RLS Policy Notes
- Each user can **select/update only their own rows**.
- Admin users can view all records (future role setup).
- Example migration file: `supabase/migrations/001_create_tables.sql`

---

## 🧱 7. Implementation Notes

- Follow **Kotlin MVVM architecture** and reuse existing components.
- Each feature should be implemented as a **separate commit or branch**:
  1. `feat/video-lessons`
  2. `feat/deped-lessons`
  3. `feat/settings`
  4. `feat/profile-edit`
  5. `chore/assets`
  6. `chore/sql`

- Example dependency additions:
  ```gradle
  implementation 'com.pierfrancescosoffritti.androidyoutubeplayer:core:11.1.0'
  implementation "io.supabase:supabase-kt:0.3.0"
  ```

---

## ✅ 8. Acceptance Criteria

- [ ] Videos play inside app using YouTube Player (not browser)
- [ ] Video progress saved to Supabase and restored on replay
- [ ] DepEd lessons show short summaries + expandable full text
- [ ] Settings persist and reload correctly
- [ ] Profile info editable and synced with Supabase
- [ ] Placeholders exist and are organized as described
- [ ] RLS policies restrict access to user-owned data only

---

## 🧩 9. Example Code Snippets

**YouTube Player Listener**
```kotlin
youtubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
    override fun onReady(player: YouTubePlayer) {
        player.cueVideo(videoYoutubeId, 0f)
    }
    override fun onStateChange(player: YouTubePlayer, state: PlayerConstants.PlayerState) {
        if (state == PlayerConstants.PlayerState.ENDED) {
            viewModel.markVideoComplete(videoId)
        }
    }
})
```

**Supabase Profile Update (Kotlin pseudocode)**
```kotlin
suspend fun updateProfile(userId: String, name: String, teacher: String, section: String) {
    supabase.from("profiles").update(
        mapOf(
            "display_name" to name,
            "teacher_name" to teacher,
            "section" to section
        )
    ).eq("id", userId)
}
```

---

## 🧪 10. Testing & Verification

- Test video playback, profile editing, and settings persistence.
- Verify Supabase sync using dashboard logs.
- Check that placeholder folders and filenames exist.
- Ensure minimal text and visual focus per screen (Sweller’s principle).

---

### 🧭 Summary

This document serves as the **feature implementation guide** for GitHub Copilot Agent.  
Copilot should follow this structure, create separate commits per feature, and ensure that all data syncs with Supabase.  
Visual placeholders must be implemented in an organized folder hierarchy for easy replacement.

---

**Document version:** October 2025  
**Maintainer:** Red  
**Tech stack:** Kotlin, Supabase, Android Studio, YouTube Player Core
