# Google Play release checklist

Audit date: 14 November 2026. Requirements were checked against the official Google Play and
Android developer pages on that date (links at the end), not from memory. Play rules change:
re-check the linked pages before each release.

## 1. Verified and fixed in the code

### Target API level

- **Requirement:** since 31 August 2026, new apps and updates must target Android 16 (API 36) or higher.
- **Status: meets it.** `targetSdk` and `compileSdk` are 37 (Android 17); `minSdk` is 26.

### Permissions

- **Status: only the five requested permissions remain.** The release manifest declares `INTERNET`,
  `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK`, `POST_NOTIFICATIONS` and `WAKE_LOCK`.
- **Fixed:** Media3 added `ACCESS_NETWORK_STATE`; it is now removed in the manifest. The app was then
  played through start, network loss and recovery with no security exceptions.
- **Checked on 15 November 2026:** the merged release manifest has no
  `com.google.android.gms.permission.AD_ID`, and the release dependencies are only AndroidX, Kotlin,
  kotlinx and Guava: no ads or analytics SDK.
- **Kept on purpose:** `io.github.evenchen1948.radioil.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`. This is not an
  Android permission. AndroidX defines it inside the app, at signature level, to protect the app's own
  broadcast receivers. Users never see it and it gives access to nothing.
- `WAKE_LOCK` is needed: it keeps the CPU and Wi-Fi awake while streaming with the screen off.

### Foreground service

- **Status: correct.** `PlaybackService` is declared with `foregroundServiceType="mediaPlayback"`.
  It is a Media3 `MediaLibraryService`, which starts the foreground service when the user starts
  playback and stops it when playback ends, as the Android 17 background-audio guidance recommends.
- Verified on the emulator: service in the foreground while playing, playback continues in the
  background and with the screen off, pauses for a phone call and resumes after it.

### Notification permission

- **Status: correct.** Requested at runtime on Android 13+ at first launch.
- Verified: with the permission denied, the app still plays, pauses and resumes.

### Release build

- **Fixed: R8 enabled.** `isMinifyEnabled` and `isShrinkResources` are on for release. The release APK
  is about 2 MB, against 15 MB for debug, and was tested on the emulator (see Stability).
- **Not debuggable:** confirmed on the installed release build.
- **No cleartext traffic.** `usesCleartextTraffic="false"`, plus a network security config that
  forbids cleartext for every domain (Android plans to deprecate the attribute). Every stream URL is HTTPS.
- **No hardcoded secrets.** The code and the full git history were scanned; nothing found.
- **Signed.** `./gradlew bundleRelease` signs the AAB with the upload key. The keystore location,
  alias and passwords come from `keystore.properties` in the project root (`storeFile`,
  `storePassword`, `keyAlias`, `keyPassword`); the `RADIO_KEYSTORE_FILE`, `RADIO_KEYSTORE_PASSWORD`,
  `RADIO_KEY_ALIAS` and `RADIO_KEY_PASSWORD` environment variables are the fallback. With neither it
  builds an unsigned AAB. The upload keystore is `upload-keystore.jks` in the project root, alias
  `upload`. Both files are git-ignored: back them up somewhere safe (section 2).
- **Fixed: backup rules.** Backup and device transfer are switched off explicitly for Android 12+.

### 64-bit and 16 KB page size

- **Requirement:** apps targeting Android 15+ must support 16 KB pages on 64-bit devices.
- **Status: passes.** The only native library is AndroidX's `libandroidx.graphics.path.so`, shipped for
  `arm64-v8a`, `x86_64`, `armeabi-v7a` and `x86`, so 64-bit is covered.
- Verified on the release APK and AAB: every library has 16 KB (`0x4000`) LOAD alignment, its RELRO
  layout is safe on 16 KB pages, and `zipalign -c -P 16` reports "Verification successful".
- Not done: a run on a real 16 KB device or emulator image.

### Edge-to-edge and predictive back

- **Edge-to-edge: correct.** Mandatory when targeting Android 16+. The app draws behind the system
  bars and pads its content with the system insets. Checked in portrait and landscape.
- **Predictive back: correct, with one fix.** `enableOnBackInvokedCallback` is set explicitly.
  **Fixed:** back now returns from a selected category to the full list, and only then leaves the app.
  It uses the AndroidX back API, which supports the predictive back gesture.
- **Large screens:** apps targeting API 37 can no longer restrict orientation or resizing on screens of
  600dp and wider. The app sets no such restriction and its layout adapts. Not tested on a tablet.

### Accessibility

- **Content descriptions:** the play/pause button is labelled; cards and chips expose their text and
  selected state; section titles are marked as headings; status changes are announced.
- **Touch targets:** chips and "show all" are 48dp high, the play button is 52dp, cards are larger.
- **Contrast, fixed:** blue text on the light-blue tile was 4.2:1, below the 4.5:1 minimum. It now uses
  a darker blue (6.0:1). All other text pairs measure 4.8:1 or higher.
- **Font scaling:** all text sizes are in `sp`.
- Not done: a manual pass with TalkBack switched on. See section 2.

### Stability

Tested with the R8 release build on an Android 15 emulator. No crashes, no ANRs, no security exceptions.

| Scenario | Result |
|---|---|
| Screen rotation while playing | Keeps playing, state kept |
| App sent to background and reopened | Keeps playing, screen shows the right station |
| Pause, then play, including after a 60-second pause | Resumes |
| Network lost for 45 seconds while playing | Buffers, then resumes by itself when the network returns |
| Starting a station with no network | Shows an error; retry reconnects once the network is back |
| Notification permission denied | Playback unaffected |

- **Fixed:** resuming an HLS station after a pause could fail with "behind live window". The player
  now rejoins the live broadcast automatically.
- Closing the app from recents (a swipe or "close all") now stops the radio and ends the playback
  service. Confirmed on a Galaxy A56 running Android 17.
- Not tested: Android 17's background-audio restrictions on a real device. See section 2.

### Version, application ID and icon

- `versionCode` is 1 and `versionName` is now `1.0.0`.
- **Adaptive icon:** a rabbit wearing headphones on near black (`#0A0A0C`), with a monochrome layer
  for themed icons and legacy mipmaps. The layers are bitmaps generated from `design/img.png`, which
  is also the 512x512 icon to upload to the Play Console listing.
- `applicationId` and `namespace` are `io.github.evenchen1948.radioil`, as registered in Play
  Console. The ID is permanent once published.
- The app name is "רדיו ישראל".

### Privacy policy link

- **Added.** A "מדיניות הפרטיות" link at the end of the home screen opens
  https://evenchen1948.github.io/arnav/privacy.html in the browser. Not yet tapped on a device.

### Lint

- Android lint on the release variant: 0 errors. The remaining warnings are informational (a newer
  Gradle exists; the service is exported, which a media service must be).

## 2. What you must do yourself

### Blocking, before the first upload

1. **Application ID: done.** It is `io.github.evenchen1948.radioil`.
2. **Back up the upload keystore.** Copy `upload-keystore.jks` and `keystore.properties` from the
   project root to a safe place outside this folder. They are not in git, so a lost disk loses them,
   and replacing a lost upload key needs a request to Google Play support. The signed bundle is
   `app/build/outputs/bundle/release/app-release.aab`.
3. **Privacy policy: linked in the app.** Enter the same URL in Play Console (App content).
4. **Check your rights to the content.** The app streams third-party stations and shows their names
   (and later their logos). Play's intellectual property policy makes you responsible for having
   permission. Review each broadcaster's terms; some forbid embedding their streams in other apps.
5. **Test on a real phone running Android 17** (your Galaxy): start a station, lock the screen, pause
   and resume from the notification and from a headset, take a call, unplug headphones, swipe the app
   away. Android 17 silences background audio that was not clearly started by the user, and I could
   only test on Android 15.

### In Play Console

6. **Developer account.** A personal account created after 13 November 2023 must run a closed test with
   at least 12 testers opted in for 14 consecutive days before applying for production access.
7. **Play App Signing.** Enrol when you create the app; your keystore from step 2 is the upload key.
8. **Foreground service declaration** (App content). For `mediaPlayback`: describe the feature, describe
   the impact if the system interrupts it, choose the "Media playback" use case, and link a short video
   showing a user starting playback and it continuing in the background.
9. **Data safety form.** Required for every app. The app itself collects and shares no user data: no
   accounts, analytics or ads. Favorite stations are saved on the device only (DataStore) and are
   never sent anywhere, so they do not count as collected data. Note that each station's server sees the listener's IP address when
   streaming; say so in the privacy policy.
10. **Other App content forms:** privacy policy URL, ads (none), app access (no login), content rating
    questionnaire, target audience and content, news app declaration (answer for an app that only
    relays radio stations), government apps (no).
11. **Store listing:** app name, short and full description in Hebrew, a 512x512 icon, a 1024x500
    feature graphic, and phone screenshots. Category: Music & Audio.
12. **Countries and pricing**, then upload the signed AAB to the closed test track first.

### Worth doing before or soon after release

13. **Turn on TalkBack** and walk through the app once: every control should be announced sensibly.
14. **Try a tablet or foldable**, where the app can no longer be locked to portrait.
15. **Logos and a bundled Hebrew font** are still missing from the design; neither is a Play requirement.
16. **Developer verification.** Google's Android developer verification programme is rolling out by
    region from September 2026. Complete identity verification in Play Console when prompted.

## Sources checked

- Target API level: https://developer.android.com/google/play/requirements/target-sdk
- 16 KB page sizes: https://developer.android.com/guide/practices/page-sizes
- Foreground service declaration: https://support.google.com/googleplay/android-developer/answer/13392821
- Closed testing for new personal accounts: https://support.google.com/googleplay/android-developer/answer/14151465
- User Data policy (privacy policy, Data safety): https://support.google.com/googleplay/android-developer/answer/10144311
- Android 16 behaviour changes: https://developer.android.com/about/versions/16/behavior-changes-16
- Android 17 behaviour changes: https://developer.android.com/about/versions/17/behavior-changes-17
  and https://developer.android.com/about/versions/17/behavior-changes-all
- Android 17 background audio hardening: https://developer.android.com/about/versions/17/changes/bg-audio
