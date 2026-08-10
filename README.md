# min-note

A minimal offline note app.

## Android

The Android project lives in `android/`.

Current Android scope:
- create notes
- edit notes
- delete notes
- search notes by title
- local SQLite storage only

App package:
- `com.convoy.noteandroid`

Build debug APK:
```bash
export JAVA_HOME=/home/user/Downloads/toolchains/jdk-17.0.18+8
export ANDROID_SDK_ROOT=/home/user/Downloads/android-sdk
export GRADLE_USER_HOME=/tmp/gradle-user-home
export PATH="$JAVA_HOME/bin:$ANDROID_SDK_ROOT/platform-tools:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$PATH"
cd android
./gradlew :app:assembleDebug
```

## Web

The web version lives in `web/` and uses Bun with plain HTML, CSS, and JavaScript.

Run locally:
```bash
cd web
bun run dev
```
