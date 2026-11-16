# Israeli Radio App — Spec

## Goal

A premium-feeling Android radio app that plays live Hebrew-language Israeli radio stations only, covering as many of them as possible. It should feel like a polished native product: dark, elegant and fast. Tap a station, hear it within a second or two, and keep hearing it when the screen is off.

## Platform and stack

- Native Android app, distributed through Google Play
- Kotlin, Jetpack Compose, Material 3
- Media3 (ExoPlayer + MediaSession) for all playback
- Single activity, MVVM with ViewModels and Kotlin coroutines/Flow
- minSdk 26 (Android 8.0), target the latest stable SDK
- Gradle with Kotlin DSL and a version catalog
- No backend, no user accounts, no analytics, no ads

Allowed libraries: Compose, Material 3, Media3, DataStore (preferences), kotlinx.serialization (JSON). Ask before adding anything else.

## Language and layout

- UI text in Hebrew, defined in `strings.xml` (no hardcoded strings in composables)
- Full RTL layout; use start/end, never left/right
- Code, comments, file names and commit messages in English

## Stations

### Scope

- Include every Israeli radio station that broadcasts in Hebrew and has a working stream: national, regional, music, talk, religious and online-only stations
- Exclude stations that broadcast mainly in Arabic, Russian, English or any other language
- Exclude non-Israeli stations, even if they play Israeli music
- No duplicates: one entry per station, using its best quality stream

The list below is the minimum starting set, not the full list. Extend it with every other station that fits the scope.

- Kan Bet (כאן ב')
- Kan Gimel (כאן גימל)
- Kan 88 (כאן 88)
- Kan Tarbut (כאן תרבות)
- Kan Kol HaMusica (כאן קול המוסיקה)
- Galei Tzahal (גלי צה"ל)
- Galgalatz (גלגלצ)
- 103FM
- eco99fm
- Radio Tel Aviv 102FM
- Radio Haifa 107.5
- Radios 100FM
- Kol Chai (קול חי)
- Kol Barama (קול ברמה)

All stations live in one file, `app/src/main/assets/stations.json`. Each entry:

```json
{
  "id": "kan-88",
  "name": "כאן 88",
  "category": "music",
  "streamUrl": "https://...",
  "streamType": "hls"
}
```

- `category` is one of: `news`, `music`, `talk`, `religious`, `regional`, `online`
- `streamType` is `hls` or `mp3`
- No station logos anywhere in the app: stations are shown by name only
- Adding or removing a station must require editing only this file

### Stream rules

- Do not invent stream URLs. Find the current official stream for each station and verify it actually plays before adding it
- HTTPS streams only. Do not enable cleartext traffic
- Play everything through Media3 ExoPlayer, which handles both HLS and MP3/AAC
- Public directories such as radio-browser.info (country code IL) may be used to discover stations, but only as leads; the stream must still be verified
- If a station has no working HTTPS stream, leave it out and list it in `STATIONS_TODO.md` with the reason
- Provide a script, `scripts/check_streams`, that tests every stream URL in `stations.json` and reports the dead ones, so the list can be re-verified with one command

## Features

### Must have (v1)

1. **Station list** — home screen with category tabs and a plain list of stations, shown by name (no logos). Must stay easy to browse with 100+ stations
2. **Playback** — tap a station to play it; tapping another station switches immediately
3. **Background playback** — a Media3 `MediaLibraryService` running as a foreground service, so audio continues with the screen off or the app in the background
4. **Media notification and lock screen controls** — station name, a generated text tile as artwork, play/pause; works with Bluetooth and wired headset buttons
5. **Audio focus and interruptions** — pause on phone calls and when headphones are unplugged; duck for navigation prompts
6. **Persistent player bar** — fixed at the bottom of every screen while a station is loaded: station tile and name, play/pause; expands into a full-screen player with previous/next station and a volume slider
7. **Loading and error states** — a clear "connecting" state while buffering, automatic retry on a dropped stream, and a friendly Hebrew message with a retry button if it still fails
8. **Favorites** — star a station; favorites are the first tab on the home screen; stored with DataStore
9. **Search** — filter stations by name as the user types
10. **Resume** — on reopening, show the last played station in a "continue listening" card at the top of the home screen, ready to play (do not autoplay)

### Later (not in v1)

- Android Auto (the `MediaLibraryService` in v1 should be structured so this is a small addition)
- Sleep timer
- Now-playing song title where the stream provides metadata
- Home screen widget

## Design

The reference for the expanded player is `design/2.png`. The home screen follows the description below and uses the same visual language; if a home screen reference image is added to `design/`, the image wins over this text. The look is dark, calm and elegant: a near-black canvas, one gold accent and serif display type, like a hi-fi tuner at night.

Implement this as a custom Material 3 dark theme (colors, typography, shapes). Do not use dynamic color. There is no light variant.

- **Background:** near black (`#0A0A0C`) everywhere, with a slightly lighter surface (`#16161A`) for cards, discs and the player bar, and a hairline outline (`#2A2A2E`) around surfaces and round buttons
- **Favorites backdrop:** the favorites tab, and only that tab, shows the 1920s radio illustration (`res/drawable-nodpi/radio_1920s_bg.webp`) behind its content. The image is scaled to fill the whole screen, anchored to the bottom, and covered by a 72% black scrim. Text drawn directly on it is off-white or gold only; text that is grey elsewhere (the category under a station, the empty message) is off-white on this tab, because grey does not reach 4.5:1 over the brass horn. It is shown both with favorites and when the tab is empty, where a short Hebrew message explains how to add a favorite. It is decorative (no content description), always on, and has no setting. Every other tab keeps the plain near-black background, and the player bar and the "continue listening" card keep their solid surface color
- **Accent:** one muted gold (`#C8A96A`), used for the play button, the disc label of the active station, the live indicator, the selected tab, the favorite star, links and the volume slider. Marks drawn on gold are near black
- **Text:** warm off-white (`#F2EEE6`) for primary, grey (`#8E8E93`) for secondary
- **Typography:** Frank Ruhl Libre (serif, bundled in `res/font`) for the screen title, station names and disc labels; the system sans-serif for secondary text, tabs and labels. Small labels such as the live indicator are letter-spaced
- **Home screen:** simple and quick to scan, top to bottom:
  - a serif screen title with a round outlined search button at the end edge
  - a "continue listening" card on the lighter surface with a thin gold outline: a small letter-spaced gold label, the last station name in large serif, its category in grey, and a solid gold play button. While a station is playing, the card shows it as now playing with a pause button
  - a row of text tabs, favorites first and then one per category; the selected tab has a thin gold underline. The row scrolls horizontally if it does not fit
  - a plain vertical list of the stations in the selected tab, separated by hairlines. No cards, no tiles, no carousels
- **Station rows:** at least 72dp tall. The station name in serif, the category beneath it in small grey text, and a favorite star at the end edge (gold when starred, outlined grey when not). The playing station shows a small animated equalizer next to its name
- **Station disc:** text only. It shows the number in the station name large in serif (for example "88") with the rest of the name small and letter-spaced beneath it; a name without a number is shown whole in serif. Used large in the expanded player and small in the player bar and the media notification
- **Player bar:** a compact floating bar at the bottom with a small round disc, the station name, the status and a gold play/pause button; tapping it opens the expanded player
- **Expanded player:** full screen, top to bottom:
  - a round outlined collapse button at the start edge, the letter-spaced gold live indicator ("● שידור חי") centred, and a round outlined favorite button at the end edge; the indicator shows the connecting, paused and error states too
  - a large disc on the lighter surface, inside a thin gold ring with a gap between them
  - the station name in large serif and the category in grey beneath it
  - previous station, a large solid gold play/pause button, next station; previous and next are thin outlined glyphs and the transport row is not mirrored for RTL
  - a volume slider labelled "עוצמה", gold track and round gold thumb, controlling the system media volume
- **Buttons:** circles and fully rounded pills; secondary round buttons are outlined, not filled
- **Motion:** quick and restrained: 150–250ms, rows dim slightly on press, the expanded player slides up from the bar, a small animated equalizer on the playing station
- **Loading:** the status text shows "connecting"; no spinners
- **Touch targets:** at least 48dp; the play button is the largest control on each screen
- **System bars:** edge-to-edge with light icons and correct insets handling

Avoid: light backgrounds, a second accent color, station logos, cards or carousels in the station list, heavy shadows, filled secondary buttons.

## Quality bar

- Playback starts within about 2 seconds on a normal mobile connection
- Switching stations never leaves two streams playing at once
- Playback survives screen off, app backgrounding and rotation
- Smooth scrolling with no dropped frames on a mid-range device
- Accessible: content descriptions on all controls, works with TalkBack, sufficient contrast, respects system font scaling
- Only the permissions actually needed (internet, foreground service, notifications)

## Google Play compliance

- Every change must keep the app compliant with current Google Play policies; check the official documentation rather than relying on memory
- Target API level meets the current Play requirement for new apps
- Foreground service declared with type `mediaPlayback`; notification permission requested at runtime on Android 13+, and the app still works if it is denied
- Release build: signed AAB, R8 enabled, not debuggable, no hardcoded secrets; the keystore and its passwords never enter the repository
- A privacy policy link is shown inside the app once the URL is provided
- `PLAY_RELEASE_CHECKLIST.md` lists what was verified in code and what must be done by hand in Play Console; keep it up to date

## Build order

Build in small steps and stop for review after each one:

1. Project skeleton and a bare screen that plays 3 stations with ExoPlayer — no styling
2. Playback service: `MediaLibraryService`, media notification, lock screen controls, audio focus
3. Full verified `stations.json` with every station in scope, the stream check script, and the station list
4. Theme (colors, type, shapes) applied to the home screen and player bar
5. Expanded player, loading and error states
6. Favorites, search, resume
7. Release setup: app icon, splash screen, signing config, signed AAB for Google Play

Commit after each step that works.
