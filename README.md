# KashCake

A modern, open source birthday reminder for Android. Simple, beautiful, and private.

## Features

- **Contact Sync**: Automatically imports birthdays from your Android contacts
- **Smart Reminders**: Get notified 1 day, 3 days, or 1 week before birthdays
- **Age Display**: Shows age (and ordinal like "25th") when birth year is known
- **Month View**: Birthdays organized by month with current month section
- **Quick Actions**: Send birthday wishes via Text or Email
- **Privacy First**: All data stays on your device - no cloud sync, no tracking

## Requirements

- Android 10 (API 29) or higher
- Contacts permission for birthday sync
- Notification permission for reminders (Android 13+)

## Building

```bash
# Clone the repository
git clone https://github.com/onekash-labs/KashCake.git
cd KashCake

# Build debug APK
./gradlew assembleDebug

# Build release APK (requires signing config in local.properties)
./gradlew assembleRelease
```

## Project Structure

```
app/src/main/kotlin/org/onekash/kashcake/
├── data/
│   ├── contacts/     # Contact sync logic
│   ├── db/           # Room database (Birthday entity, DAO)
│   └── preferences/  # DataStore preferences
├── di/               # Hilt dependency injection modules
├── domain/           # Business logic (Repository, Utils)
├── reminder/         # Notification scheduling
├── ui/
│   ├── components/   # Reusable Compose components
│   ├── screens/      # Home and Settings screens
│   └── theme/        # Material 3 theming
└── widget/           # Home screen widget
```

## Tech Stack

- **UI**: Jetpack Compose with Material 3
- **Architecture**: MVVM with Repository pattern
- **DI**: Hilt
- **Database**: Room
- **Preferences**: DataStore
- **Image Loading**: Coil
- **Widget**: Glance

## License

Apache License 2.0 - see [LICENSE](LICENSE) for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
