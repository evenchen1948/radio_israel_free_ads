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
