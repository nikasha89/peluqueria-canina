# Peluquería Canina - Android Native App

## Project Overview
Native Android app for a dog grooming business management system.

## Tech Stack
- **Language:** Kotlin
- **Architecture:** MVVM with ViewModel and LiveData
- **Database:** Room (SQLite)
- **UI:** Material Design 3, RecyclerView, Navigation Component
- **Auth:** Google Sign-In
- **APIs:** Google Calendar, Google Drive

## Project Structure
```
androidNative/
├── app/
│   ├── src/main/
│   │   ├── java/com/peluqueriacanina/app/
│   │   │   ├── data/           # Room DB, entities, DAOs
│   │   │   ├── ui/             # Fragments, adapters
│   │   │   ├── viewmodel/      # ViewModels
│   │   │   └── MainActivity.kt
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   ├── navigation/
│   │   │   └── values/
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
└── settings.gradle.kts
```

## Build Instructions
```bash
./gradlew assembleDebug
```

## Features
- Appointment scheduling with calendar view
- Client and dog management
- Service catalog with pricing
- Google Calendar sync
- Google Drive backup
