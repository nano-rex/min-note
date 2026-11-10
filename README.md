# min-note

A minimal offline note app for Android and the browser. No account, no sync, no network:
notes stay on the device.

## Features

- Create, edit and delete notes (title + body)
- Search notes by title
- Find and replace inside a note (**Search and replace**)
- Android: local SQLite storage (package `com.convoy.noteandroid`)
- Web: plain HTML/CSS/JavaScript, notes kept in the browser's `localStorage`

## Build and run

### Android (`android/`)

Requirements: JDK 17 and the Android SDK with platform 34 and build-tools 34
(`sdkmanager "platforms;android-34" "build-tools;34.0.0"`). Android Studio is not needed;
the Gradle 8.2 wrapper is included.

```bash
git clone https://github.com/nano-rex/min-note.git
cd min-note/android
export ANDROID_HOME=$HOME/Android/Sdk      # path to your Android SDK
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Web (`web/`)

Requires [Bun](https://bun.sh). No dependencies to install.

```bash
cd min-note/web
bun run dev          # auto-reload while editing; or: bun run start
```

Then open <http://localhost:4173> (set `PORT=…` to change it).

## Example

1. Tap **New Note**, enter the title `Groceries` and the body:

   ```
   milk
   eggs
   bread
   ```

   then **Save** — the list shows "Note created".
2. Type `groc` in **Search notes** to find it again.
3. Open it → **Search and replace**: find `milk`, replace with `oat milk` → **Replace**.
4. **Delete** removes the note.
