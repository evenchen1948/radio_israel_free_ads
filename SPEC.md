# Israeli Radio App — Spec

## Goal

A premium-feeling radio app that plays live Israeli radio stations only. It should feel like a high-end audio product: calm, dark, minimal, and fast. Tap a station, hear it within a second or two.

## Platform and stack

- Progressive Web App (PWA), installable to the phone home screen
- React + Vite + TypeScript
- Mobile first; must also look right on desktop
- No backend. Everything runs in the browser
- No user accounts, no analytics, no ads

## Language and layout

- UI text in Hebrew
- Full RTL layout (`dir="rtl"`)
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

All stations live in one file, `src/data/stations.json`. Each entry:

```json
{
  "id": "kan-88",
  "name": "כאן 88",
  "category": "music",
  "streamUrl": "https://...",
  "streamType": "hls",
  "logo": "/logos/kan-88.svg"
}
```

- `category` is one of: `news`, `music`, `talk`, `religious`, `regional`
- `streamType` is `hls` or `mp3`
- Adding or removing a station must require editing only this file

### Stream rules

- Do not invent stream URLs. Find the current official stream for each station and verify it actually plays before adding it
- HTTPS streams only (the app is served over HTTPS, so HTTP streams will be blocked)
- Use the native `<audio>` element for MP3/AAC streams and `hls.js` for HLS streams where the browser has no native HLS support
- If a station has no working HTTPS stream, leave it out and list it in `STATIONS_TODO.md` with the reason

## Features

### Must have (v1)

1. **Station list** — home screen, grouped by category, each station shown with logo and name
2. **Playback** — tap a station to play it; tapping another station switches immediately
3. **Persistent player bar** — fixed at the bottom of every screen: station logo and name, play/pause, volume
4. **Loading and error states** — a clear "connecting" state while buffering, and a friendly Hebrew message with a retry button if the stream fails
5. **Favorites** — star a station; favorites appear at the top of the home screen; stored in `localStorage`
6. **Search** — filter stations by name as the user types
7. **Resume** — on reopening, show the last played station ready to play (do not autoplay)
8. **PWA** — manifest, icons, installable, app shell cached for instant load
9. **Lock screen controls** — Media Session API so play/pause and station name show on the lock screen and on headphones

### Later (not in v1)

- Sleep timer
- Now-playing song title where the station provides metadata
- Car mode with oversized controls
- Light theme

## Design

The look and feel should follow the Wolt app: bright, friendly, playful and very polished. Use Wolt as the reference for layout, spacing and motion, not for branding. Do not use Wolt's logo, name, illustrations or proprietary font.

- **Background:** clean white (`#FFFFFF`), with very light grey (`#F5F6F7`) for secondary surfaces
- **Accent:** one bright blue (`#009DE0`), used for the play button, the active station, links and the favorite state
- **Text:** near black (`#202125`) for primary, mid grey (`#717378`) for secondary
- **Typography:** one rounded, friendly Hebrew typeface with heavy weights (Rubik or Heebo). Big bold headings, compact secondary text
- **Cards:** stations appear as large rounded cards (16px radius) with the logo as the hero, name below, soft subtle shadow. Horizontal scrolling rows per category, like Wolt's venue carousels
- **Category chips:** pill-shaped filter chips at the top of the home screen
- **Buttons:** fully rounded pills; the primary button is solid blue with white text
- **Player:** a compact bar fixed at the bottom that expands into a bottom sheet with a large logo and controls
- **Motion:** quick and playful — 150–250ms spring-like transitions, cards scale slightly on press, the bottom sheet slides up smoothly, a small animated equalizer on the playing station
- **Loading:** skeleton placeholders instead of spinners
- **Touch targets:** at least 48px; the play button is the largest element in the player

Avoid: dark heavy backgrounds, more than one accent color, sharp corners, dense text lists, harsh shadows.s
## Quality bar

- Playback starts within about 2 seconds on a normal mobile connection
- Switching stations never leaves two streams playing at once
- No layout shift when the player bar appears
- Works in Chrome on Android and Safari on iOS
- Accessible: proper labels on all controls, visible focus states, sufficient contrast

## Build order

Build in small steps and stop for review after each one:

1. Project skeleton and a bare player that plays 3 stations — no styling
2. Full verified `stations.json` and the station list
3. Design system (colors, type, spacing) applied to the home screen and player bar
4. Loading and error states
5. Favorites, search, resume
6. PWA setup and lock screen controls
7. Deployment config for static hosting

Commit after each step that works.
