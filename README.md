# note-android

A minimal offline Android note app.

Current scope:
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
./gradlew :app:assembleDebug
```


## Security

The app opens behind a login screen using the `user` account with initial password `12345678`. Application data is stored in a SQLCipher-encrypted SQLite database and is only opened after successful authentication. Change the initial credential before distributing the app.
