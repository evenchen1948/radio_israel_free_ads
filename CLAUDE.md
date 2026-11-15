# Israeli Radio App

Native Android app in Kotlin + Jetpack Compose. Requirements are in SPEC.md. Read it before starting any task.

## Working rules

- Build one step from the SPEC "Build order" at a time, then stop for review
- Commit after each step that works
- Push after every commit (remote `origin`, branch `main`)
- Every change must keep the app compliant with Google Play policies. See PLAY_RELEASE_CHECKLIST.md
- Never commit keystores, signing passwords or other secrets; release signing reads them from the git-ignored `keystore.properties` (or the `RADIO_KEYSTORE_*` environment variables)
- Never invent stream URLs; verify every stream plays before adding it
- UI text in Hebrew via strings.xml, RTL. Code, comments and commits in English
- Do not add dependencies beyond the SPEC without asking first
- Keep all station data in app/src/main/assets/stations.json only
- All playback goes through the Media3 service, never a player owned by a screen

## Commands

- `scripts/run` — build, install and launch the app on the connected device or running emulator (Git Bash; finds JDK 17+ and adb itself)
- `./gradlew assembleDebug` — build a debug APK
- `./gradlew installDebug` — install on a connected device or emulator
- `./gradlew test` — run unit tests
- `./gradlew bundleRelease` — build the release AAB (signed when `keystore.properties` exists or the `RADIO_KEYSTORE_*` environment variables are set, otherwise unsigned)
