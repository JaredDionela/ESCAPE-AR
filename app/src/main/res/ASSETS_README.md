# 🎨 Assets Folder Structure

This document describes the organized asset structure for the Science Learning App.

## 📁 Folder Organization

### `/res/drawable/placeholder_icons/`
Icons for UI elements, buttons, and actions.

**Expected Files:**
- `icon_play_placeholder.xml` - Play button icon
- `icon_pause_placeholder.xml` - Pause button icon
- `icon_quiz_placeholder.xml` - Quiz/test icon
- `icon_lesson_placeholder.xml` - Lesson/book icon
- `icon_settings_placeholder.xml` - Settings gear icon
- `icon_profile_placeholder.xml` - User profile icon
- `icon_video_placeholder.xml` - Video camera icon
- `icon_completed_placeholder.xml` - Checkmark/complete icon

### `/res/drawable/topic_thumbnails/`
Thumbnail images for each science topic.

**Expected Files:**
- `topic_decantation_placeholder.png` - Decantation topic thumbnail
- `topic_organ_system_placeholder.png` - Organ System topic thumbnail
- `topic_simple_machines_placeholder.png` - Simple Machines topic thumbnail
- `topic_solar_system_placeholder.png` - Solar System topic thumbnail

### `/res/drawable/ui_elements/`
General UI graphics and backgrounds.

**Expected Files:**
- `bg_gradient_placeholder.xml` - Background gradient
- `button_primary_placeholder.xml` - Primary button shape
- `button_secondary_placeholder.xml` - Secondary button shape
- `card_background_placeholder.xml` - Card background
- `progress_bar_placeholder.xml` - Custom progress bar
- `divider_placeholder.xml` - Section divider

### `/res/raw/sound_effects/`
Sound effects for UI interactions.

**Expected Files:**
- `sfx_button_click.mp3` - Button click sound
- `sfx_quiz_correct.mp3` - Correct answer sound
- `sfx_quiz_wrong.mp3` - Wrong answer sound
- `sfx_level_complete.mp3` - Level completion sound
- `sfx_achievement.mp3` - Achievement unlocked sound

### `/res/raw/music/`
Background music tracks.

**Expected Files:**
- `music_menu.mp3` - Menu background music
- `music_quiz.mp3` - Quiz/test background music
- `music_video.mp3` - Video lesson background music (soft)

## 🔄 Replacement Guidelines

All placeholder files follow a naming convention for easy replacement:

1. **Icons**: Use vector drawables (.xml) when possible for scalability
2. **Images**: Use PNG or WebP format, optimize for mobile
3. **Audio**: Use MP3 format, compress for smaller file size

### Naming Convention
```
{category}_{name}_placeholder.{extension}
```

Examples:
- `icon_play_placeholder.xml`
- `topic_decantation_placeholder.png`
- `sfx_button_click.mp3`

## 📝 Notes

- Replace placeholder files with actual assets before production release
- Maintain the same filenames to avoid code changes
- Optimize all assets for mobile devices
- Keep total asset size under 10MB for faster app download

## ✅ Status

- [x] Folder structure created
- [ ] Placeholder icons added
- [ ] Topic thumbnails added
- [ ] UI elements added
- [ ] Sound effects added
- [ ] Background music added

---

**Last Updated:** October 2025
