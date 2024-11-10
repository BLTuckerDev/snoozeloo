# Snoozeloo - Modern Android Alarm App

Snoozeloo is a modern Android alarm application developed as part of the [Mobile Dev Campus Monthly Challenge](https://pl-coding.com/campus/) by Philipp Lackner for November of 2024. It demonstrates modern Android development practices and architecture while providing a seamless alarm management experience.

## Features

- 🕒 Intuitive alarm creation and management
- 🔄 Support for repeating alarms with customizable days
- 💤 Smart snooze functionality
- 🎵 Customizable alarm sounds and volume
- 📱 Material Design 3 UI with Jetpack Compose
- 🌙 Bedtime recommendations for optimal sleep
- ⚡ Reliable alarm scheduling that persists through device restarts

## Technical Highlights

This project showcases modern Android development practices and technologies:

### Architecture & Design Patterns
- Clean Architecture principles
- MVVM pattern with UI States
- Repository pattern for data management
- Single Activity architecture

### Android Jetpack
- **Compose**: Modern declarative UI
- **Room**: Local database for alarm storage
- **Hilt**: Dependency injection
- **WorkManager**: Background task scheduling
- **DataStore**: Preferences storage
- **Navigation**: Single Activity navigation

### Other Technologies & Libraries
- **Kotlin**: 100% Kotlin codebase with coroutines and flows
- **Material Design 3**: Modern and consistent UI/UX
- **Version Catalog**: Dependency management

## Implementation Details

### Alarm Scheduling
- Uses Android's `AlarmManager` for precise scheduling
- Handles device reboots gracefully
- Implements efficient repeat scheduling algorithms
- Smart handling of one-time vs recurring alarms

### Data Persistence
- Room database for alarm storage
- Efficient BitSet encoding for repeat days
- Type converters for complex data types

### Background Processing
- WorkManager for reliable background tasks
- Boot receiver for alarm rescheduling
- Broadcast receivers for alarm triggers

## Building The Project

1. Clone the repository
```bash
git clone https://github.com/BLTuckerDev/Snoozeloo.git
```

2. Open the project in Android Studio (latest version recommended)

3. Build and run the project

## Requirements
- Minimum SDK: 27 (Android 8.1)
- Target SDK: 34 (Android 14)
- Kotlin 1.9.25

## Credits

This project was developed as part of the Mobile Dev Campus Monthly Challenge by [Philipp Lackner](https://pl-coding.com/campus/). The challenge provided an opportunity to demonstrate Android development expertise while building a practical, user-focused application.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.