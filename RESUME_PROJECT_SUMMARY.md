# Meme Media — Resume Project Summary

## Project at a glance

**Meme Media** is a native Android soundboard app with a Nothing OS-inspired home-screen widget. Users can swipe through built-in and custom meme sounds and play a sound directly from the widget, without opening the app. A companion screen lets users import, preview, and remove their own audio.

## Resume-ready entry

**Meme Media | Android Soundboard & Home-Screen Widget**  
*Kotlin, Android SDK, Android App Widgets, Gradle*

- Built a native Android soundboard featuring a swipeable 2×2 home-screen widget and a custom sound-management screen.
- Implemented widget-to-app interactions with `AppWidgetProvider`, `RemoteViews`, `StackView`, and `PendingIntent` templates, including direct playback from the home screen.
- Developed foreground-service audio playback with `MediaPlayer`, media audio attributes, playback notifications, and completion/error cleanup.
- Added custom audio import using Android’s content picker; copied selected files into app-private storage and managed sound metadata with `SharedPreferences` and JSON.
- Designed a Nothing OS-inspired, dot-matrix visual style using Android XML layouts and drawable resources.

## What I learned and implemented

- **Android app components:** Connected an Activity, AppWidgetProvider, RemoteViewsService, and foreground Service to support distinct app and widget workflows.
- **Widget constraints and interactions:** Used `RemoteViews`-compatible layouts, a `StackView` data factory, fill-in intents, and a pending-intent template for swipeable, tappable widget tiles.
- **Background media playback:** Managed `MediaPlayer` setup and release, media audio attributes, foreground-service notifications, and playback completion/error handling.
- **User-selected files and app storage:** Opened the system audio picker, read content through a `Uri`, copied audio into internal storage, and connected saved file paths to playback.
- **Local persistence:** Serialized sound entries as JSON in `SharedPreferences` and refreshed widget data when users add or remove a sound.
- **Android resource and UI work:** Built XML-based screens and widget tiles, used bundled raw audio and custom drawable/font resources, and applied a consistent visual theme.
- **Platform compatibility:** Configured a minimum Android API level, Java/Kotlin 17 compilation, and foreground media playback declarations in the manifest.

## Features and strengths to highlight

- Quick access to four bundled sounds and user-imported audio from the home screen.
- Custom sounds can be named, previewed, and removed; imported audio is stored privately within the app.
- The widget’s add-sound tile opens the manager, while sound tiles start playback without launching the main screen.
- Demonstrates end-to-end Android development across UI, widgets, services, media, storage, and app configuration.

## Applications and development tools

- **IDE/workflow:** Android Studio (the project README documents Android Studio as the development workflow).
- **Language:** Kotlin 1.9.24.
- **Android platform:** Android SDK, compile/target SDK 34, minimum SDK 26.
- **Build system:** Gradle 8.6 wrapper and Android Gradle Plugin 8.4.1.
- **Java toolchain:** JDK 17.
- **UI and platform APIs:** Android XML layouts/resources, Android App Widgets, `RemoteViews`, `MediaPlayer`, AndroidX Core KTX, AppCompat, Activity KTX, and Material Components.

## Build

From the `MemeMedia` project directory on Windows:

```powershell
.\gradlew.bat assembleDebug
```
