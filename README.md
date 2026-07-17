# MemeMedia

MemeMedia is an Android home-screen soundboard with a compact, Nothing OS-inspired 2×2 widget. Flip through bundled meme sounds, play a sound without opening the app, and add or remove personal audio files from the companion screen.

## Features

- 2×2 home-screen widget with a matte-black, dot-matrix-inspired design.
- Swipeable `StackView` deck for the built-in sounds and the **Add sound** tile.
- Background playback through `SoundPlayService`; a normal widget tap does not open an activity.
- Built-in sounds: `faah`, `bruh`, `vine boom`, and `airhorn`.
- Import, name, preview, and remove custom audio files. Imported files are copied to the app's private storage.
- Graceful handling of a missing bundled resource or corrupt saved sound list.

## Requirements

- Android Studio with JDK 17.
- Android device or emulator running Android 8.0 (API 26) or later.
- Android SDK 34 installed for building.

## Quick start

1. Open this `MemeMedia` folder in Android Studio and allow Gradle to sync.
2. Select the `app` run configuration and run it on an API 26+ device or emulator.
3. Long-press the launcher home screen, choose **Widgets**, then drag **Sound Widget** onto the home screen.
4. Tap the current tile to play it. Swipe the widget to move through the sounds.
5. On the **add sound** tile, tap to open the manager, then use the add button to choose an audio file and give it a name.

## Bundled sounds

Built-in audio lives in [`app/src/main/res/raw`](app/src/main/res/raw). Android resource names must use lowercase letters, numbers, and underscores. To replace a bundled sound, keep one of these base names and use a supported audio format such as MP3, OGG, or WAV:

- `faah` — first/default widget sound
- `bruh`
- `vine_boom`
- `airhorn`

Missing bundled files are ignored safely, so the app remains usable while sounds are being replaced.

## Project structure

- `SoundWidgetProvider` configures and refreshes the widget and routes tile taps.
- `StackRemoteViewsService` supplies the swipeable widget tiles.
- `SoundPlayService` creates and releases `MediaPlayer` instances for playback.
- `SoundRepository` stores the default and user-added sound entries in `SharedPreferences`.
- `MainActivity` provides the custom-sound manager.

## Build from the command line

On Windows:

```powershell
.\gradlew.bat assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`. The project uses Android Gradle Plugin 8.4.1, Kotlin 1.9.24, Gradle 8.6, and Java 17.

## Widget limitation

Android home-screen widgets use `RemoteViews`, so their flip animation is supplied by the launcher. The `StackView` supports native swipe navigation, but its animation cannot be customized frame by frame like an in-app view.

## License

This project is released under the [MIT License](LICENSE).
