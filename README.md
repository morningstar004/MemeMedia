# Nothing-style Sound Widget

A 2×2 circular home-screen widget: tap it and it plays a "faah" sound; swipe
up/down and it flips through meme sounds and an "add sound" tile. Styling
uses a matte-black disc, thin grey ring, off-white monospace labels, and a
single red accent dot — matching Nothing OS's dot-matrix look.

## Open it
1. Android Studio → **Open** → select this `NothingSoundWidget` folder.
2. Let Gradle sync (it will download the Android Gradle Plugin + Kotlin
   plugin listed in `build.gradle`).
3. Run the `app` module on a device/emulator (API 26+).
4. Long-press the home screen → Widgets → "Sound Widget" → drag it on.

## Add your actual sound files
The project ships with no audio (I can't attach binary audio in this
environment). Drop your files into `app/src/main/res/raw/` using these exact
names (any of mp3/ogg/wav, just keep the base name):

- `faah.mp3` — plays on the default/first tap
- `bruh.mp3`
- `vine_boom.mp3`
- `airhorn.mp3`

If a file is missing, tapping that tile just does nothing (no crash) — so
you can add sounds incrementally. Users can also add their own on-device via
the widget's "add sound" tile, which opens a tiny picker screen — those are
copied into app storage and don't need rebuilding the app.

## How the pieces fit together
- **`SoundWidgetProvider`** — the widget itself. Builds the `RemoteViews`,
  attaches the `StackView` adapter, and routes taps.
- **`StackRemoteViewsService` / `SoundStackFactory`** — supplies each
  swipeable "page" (faah / meme sounds / add-tile) to the `StackView`.
- **`SoundPlayService`** — a headless service; a tap starts it with a sound
  id, it plays that one sound via `MediaPlayer`, then stops itself. No UI
  ever opens for a normal tap.
- **`SoundRepository`** — the ordered sound list, built-ins + user-added,
  persisted as JSON in `SharedPreferences`.
- **`MainActivity`** — only opens when you tap the "add sound" tile (or
  launch the app icon directly); lets you pick an audio file, name it, and
  remove sounds you've added.

## One real platform limit, worth knowing up front
Home-screen widgets render through `RemoteViews` inside the launcher's
process, not your app's — so a widget can't run arbitrary custom
touch/animation code the way a normal in-app view can. The only RemoteViews
component with a native swipe/flick gesture is `StackView` (the same
mechanism behind stock widgets like Play Books' "recently read" stack),
which is what's used here. Its flip transition is the *system's* built-in
animation — smooth and native-feeling, but not something you can restyle
frame-by-frame from app code. If you want a fully custom swipe animation,
that would need to live inside an app screen (e.g. an overlay/quick-settings
tile) rather than a true home-screen widget.

## Tuning the Nothing look
All colors live in `res/values/colors.xml` — `nothing_black`,
`nothing_grey_ring`, `nothing_white`, `nothing_red`. The dot-matrix icons are
plain vector drawables (`ic_dot_wave.xml`, `ic_dot_meme.xml`,
`ic_dot_add.xml`) built from circles, so they're easy to recolor or resize
without needing image assets.
