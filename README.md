# HabitQuest

A beautiful and intuitive habit tracking app built with Jetpack Compose. Track your daily habits, monitor your progress with visual indicators, and build better routines. The app features a GitHub-like contribution graph, weekly calendar view, customizable habit categories with different colors, and a clean Material Design interface with dark/light theme support.

## ✨ Features

### Core Features
- [x] Track daily habits with progress indicators
- [x] Weekly calendar view with completion status
- [x] GitHub-like contribution graph (182 days / 6 months view)
- [x] Customizable habit categories and colors
- [x] Dark/Light theme support
- [x] Settings with various customization options
- [x] Multiple completions per day support
- [x] Habit description and notes
- [x] Random data generator for screenshots/demos

### Habit Management
- [x] Create, edit, and delete habits
- [x] Set target count for habits (1+ completions per day)
- [x] Habit frequency options (Daily, Weekly, Monthly, Custom)
- [x] Category-based organization
- [x] Habit templates/presets (common habits like exercise, meditation, reading)
- [x] Archive functionality for inactive habits (keep history but hide from main view)
- [x] Habit reordering by drag and drop (priority sorting)
- [x] Search and filter UI for habits (by name, category)

### Progress & Analytics
- [x] Display streaks prominently on habit cards (current streak and longest streak badges)
- [x] Analytics dashboard screen with charts (completion rates, best days)
- [x] Habit detail screen showing full statistics and completion history
- [ ] Yearly/monthly overview screens with calendar heatmap
- [x] Habit chains visualization (visual chain showing consecutive days)
- [ ] Weekly/monthly reports with insights (best performing habits, areas to improve)
- [x] Habit streaks tracking (backend implemented)
- [x] Completion statistics and progress tracking

### Notifications & Reminders
- [x] Notification system for habit reminders (WorkManager + NotificationManager, with quick-complete action)
- [x] Reminder time setting with per-day-of-week scheduling
- [x] Midnight streak reset (AlarmManager) that survives reboot

### User Experience
- [x] Add ability to edit/add notes when completing habits (tap completion to edit)
- [x] Undo completion feature (tap the completed checkmark again to undo)
- [x] Quick action: complete all habits for today
- [x] Quick add common habits via Habit templates/presets
- [x] Onboarding tips/tutorial for first-time users
- [x] Custom icon picker for habits (emoji-based, shown on habit cards)
- [x] Streak freeze feature (allow skipping days without breaking streak)

### Achievements & Motivation
- [x] Achievement badges/milestones system (7/30/100/365-day streaks, 10/50/100/500 completions)
- [x] Motivational quotes or encouraging messages when completing habits

### Data & Backup
- [x] Backup and restore functionality (export/import JSON)
- [ ] Data export options (share progress as image, export to CSV for Excel)
- [ ] Progress photos/attachments to habit completions

### Sharing & Social
- [x] Habit sharing (share progress graphs as images to social media)
- [x] Random data generator for creating demo screenshots

### Widgets & Customization
- [x] Home screen widget showing today habits and completion status (3-day and monthly variants, per-widget habit picker)
- [ ] Dark mode scheduling (auto-switch based on time of day)

## 🛠 Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: Room Database
- **Dependency Injection**: Manual DI (can be upgraded to Hilt/Koin)

## 🚀 Getting Started

1. Clone the repository
   ```bash
   git clone https://github.com/yourusername/HabitQuest.git
   ```

2. Open in Android Studio
   - Make sure you have Android Studio Hedgehog or later
   - Ensure Kotlin plugin is installed

3. Build and run on your device
   - Connect an Android device or start an emulator
   - Click Run or press `Shift+F10`

## 📱 Screenshots

*Add screenshots of your app here*

## 🎯 Roadmap

See the [Features](#-features) section above for a complete list of planned features. Priority items include:
1. Data export options (CSV export for Excel)
2. Yearly/monthly overview screens with calendar heatmap
3. Weekly/monthly reports with insights
4. Progress photos/attachments to habit completions
5. Dark mode scheduling (auto-switch based on time of day)

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📝 License

*Add your license here*

## 👨‍💻 Developer

Built by [Mohammad Faizan](https://mohammadfaizan.com)

---

**Note**: Features marked with [x] are implemented, while [ ] indicates planned features. This is an active project with ongoing development.
