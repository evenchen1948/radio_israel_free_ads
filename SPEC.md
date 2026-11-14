# Israeli Radio App — Spec

## Goal

A premium-feeling Android radio app that plays live Israeli radio stations only. It should feel like a polished native product: bright, friendly and fast. Tap a station, hear it within a second or two, and keep hearing it when the screen is off.

## Platform and stack

- Native Android app, distributed through Google Play
- Kotlin, Jetpack Compose, Material 3
- Media3 (ExoPlayer + MediaSession) for all playback
- Single activity, MVVM with ViewModels and Kotlin coroutines/Flow
- minSdk 26 (Android 8.0), target the latest stable SDK
- Gradle with Kotlin DSL and a version catalog
- No backend, no user accounts, no analytics, no ads

Allowed libraries: Compose, Material 3, Media3, Coil (logos), DataStore (preferences), kotlinx.serialization (JSON). Ask before adding anything else.

## Language and layout

- UI text in Hebrew, defined in `strings.xml` (no hardcoded strings in composables)
- Full RTL layout; use start/end, never left/right
- Code, comments, file names and commit messages in English

## Stations

Israeli stations only. Initial list:

- Kan Bet (כאן ב')
- Kan Gimel (כאן גימל)
- Kan 88 (כאן 88)
- Kan Tarbut (כאן תרבות)
- Kan Kol HaMusica (כאן קול המוסיקה)
- Kan Reka (כאן רקע)
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
  "streamType": "hls",
  "logo": "logos/kan-88.webp"
}
```

- `category` is one of: `news`, `music`, `talk`, `religious`, `regional`
- `streamType` is `hls` or `mp3`
- Logos are bundled in `assets/logos/`
- Adding or removing a station must require editing only this file (plus its logo)

### Stream rules

- Do not invent stream URLs. Find the current official stream for each station and verify it actually plays before adding it
- HTTPS streams only. Do not enable cleartext traffic
- Play everything through Media3 ExoPlayer, which handles both HLS and MP3/AAC
- If a station has no working HTTPS stream, leave it out and list it in `STATIONS_TODO.md` with the reason

## Features

### Must have (v1)

1. **Station list** — home screen, grouped by category, each station shown with logo and name
2. **Playback** — tap a station to play it; tapping another station switches immediately
3. **Background playback** — a Media3 `MediaLibraryService` running as a foreground service, so audio continues with the screen off or the app in the background
4. **Media notification and lock screen controls** — station name, logo, play/pause; works with Bluetooth and wired headset buttons
5. **Audio focus and interruptions** — pause on phone calls and when headphones are unplugged; duck for navigation prompts
6. **Persistent player bar** — fixed at the bottom of every screen: station logo and name, play/pause; expands into a full player sheet
7. **Loading and error states** — a clear "connecting" state while buffering, automatic retry on a dropped stream, and a friendly Hebrew message with a retry button if it still fails
8. **Favorites** — star a station; favorites appear at the top of the home screen; stored with DataStore
9. **Search** — filter stations by name as the user types
10. **Resume** — on reopening, show the last played station ready to play (do not autoplay)

### Later (not in v1)

- Android Auto (the `MediaLibraryService` in v1 should be structured so this is a small addition)
- Sleep timer
- Now-playing song title where the stream provides metadata
- Home screen widget
- Dark theme

## Design

The look and feel should follow the Wolt app: bright, friendly, playful and very polished. Use Wolt as the reference for layout, spacing and motion, not for branding. Do not use Wolt's logo, name, illustrations or proprietary font.

Implement this as a custom Material 3 theme (colors, typography, shapes). Do not use dynamic color.

- **Background:** clean white (`#FFFFFF`), with very light grey (`#F5F6F7`) for secondary surfaces
- **Accent:** one bright blue (`#009DE0`), used for the play button, the active station, links and the favorite state
- **Text:** near black (`#202125`) for primary, mid grey (`#717378`) for secondary
- **Typography:** one rounded, friendly Hebrew typeface with heavy weights (Rubik or Heebo), bundled in the app. Big bold headings, compact secondary text
- **Cards:** stations appear as large rounded cards (16dp radius) with the logo as the hero, name below, soft subtle elevation. Horizontal scrolling rows per category, like Wolt's venue carousels
- **Category chips:** pill-shaped filter chips at the top of the home screen
- **Buttons:** fully rounded pills; the primary button is solid blue with white text
- **Player:** a compact bar fixed at the bottom that expands into a bottom sheet with a large logo and controls
- **Motion:** quick and playful — 150–250ms spring animations, cards scale slightly on press, the bottom sheet slides up smoothly, a small animated equalizer on the playing station
- **Loading:** skeleton placeholders instead of spinners
- **Touch targets:** at least 48dp; the play button is the largest element in the player
- **System bars:** edge-to-edge, with correct insets handling

Avoid: dark heavy backgrounds, more than one accent color, sharp corners, dense text lists, harsh shadows.

## Quality bar

- Playback starts within about 2 seconds on a normal mobile connection
- Switching stations never leaves two streams playing at once
- Playback survives screen off, app backgrounding and rotation
- Smooth scrolling with no dropped frames on a mid-range device
- Accessible: content descriptions on all controls, works with TalkBack, sufficient contrast, respects system font scaling
- Only the permissions actually needed (internet, foreground service, notifications)

## Build order

Build in small steps and stop for review after each one:

1. Project skeleton and a bare screen that plays 3 stations with ExoPlayer — no styling
2. Playback service: `MediaLibraryService`, media notification, lock screen controls, audio focus
3. Full verified `stations.json` and the station list
4. Theme (colors, type, shapes) applied to the home screen and player bar
5. Expanded player sheet, loading and error states
6. Favorites, search, resume
7. Release setup: app icon, splash screen, signing config, signed AAB for Google Play

Commit after each step that works.
