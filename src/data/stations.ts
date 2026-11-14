import stationsJson from './stations.json'

export type StationCategory = 'news' | 'music' | 'talk' | 'religious' | 'regional'
export type StreamType = 'hls' | 'mp3'

export interface Station {
  id: string
  name: string
  category: StationCategory
  streamUrl: string
  streamType: StreamType
  logo: string
}

// stations.json is the single source of station data; this module only types it.
export const stations = stationsJson as Station[]

// Display order and Hebrew labels for the home screen groups.
export const CATEGORIES: { id: StationCategory; label: string }[] = [
  { id: 'news', label: 'חדשות ואקטואליה' },
  { id: 'music', label: 'מוזיקה' },
  { id: 'talk', label: 'תרבות ושיחה' },
  { id: 'regional', label: 'אזורי' },
  { id: 'religious', label: 'דת ומסורת' },
]
