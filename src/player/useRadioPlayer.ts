import Hls from 'hls.js'
import { useCallback, useEffect, useRef, useState } from 'react'
import type { Station } from '../data/stations'

export type PlayerStatus = 'idle' | 'loading' | 'playing' | 'paused' | 'error'

const HLS_MIME = 'application/vnd.apple.mpegurl'

/**
 * Owns the app's single <audio> element. Every station change tears the
 * previous stream down first, so two streams can never play at once.
 */
export function useRadioPlayer() {
  const audioRef = useRef<HTMLAudioElement | null>(null)
  const hlsRef = useRef<Hls | null>(null)
  const [current, setCurrent] = useState<Station | null>(null)
  const [status, setStatus] = useState<PlayerStatus>('idle')

  const teardown = useCallback(() => {
    hlsRef.current?.destroy()
    hlsRef.current = null
    const audio = audioRef.current
    if (audio) {
      audio.pause()
      audio.removeAttribute('src')
      audio.load()
    }
  }, [])

  // Attach the station's stream to the audio element and start playback.
  const start = useCallback(
    (station: Station) => {
      const audio = audioRef.current
      if (!audio) return
      teardown()
      setStatus('loading')

      const needsHlsJs =
        station.streamType === 'hls' && !audio.canPlayType(HLS_MIME) && Hls.isSupported()

      if (needsHlsJs) {
        const hls = new Hls()
        hlsRef.current = hls
        hls.on(Hls.Events.ERROR, (_event, data) => {
          if (data.fatal) setStatus('error')
        })
        hls.loadSource(station.streamUrl)
        hls.attachMedia(audio)
      } else {
        // MP3/AAC streams, and HLS where the browser plays it natively (Safari).
        audio.src = station.streamUrl
      }

      audio.play().catch((error: unknown) => {
        // AbortError means a newer station replaced this one mid-load.
        if (error instanceof DOMException && error.name === 'AbortError') return
        setStatus('error')
      })
    },
    [teardown],
  )

  const play = useCallback(
    (station: Station) => {
      setCurrent(station)
      start(station)
    },
    [start],
  )

  const toggle = useCallback(() => {
    if (!current) return
    if (status === 'playing' || status === 'loading') {
      // Drop the connection rather than buffer a live stream while paused.
      teardown()
      setStatus('paused')
    } else {
      start(current)
    }
  }, [current, status, start, teardown])

  useEffect(() => {
    const audio = new Audio()
    audio.preload = 'none'
    audioRef.current = audio

    const onPlaying = () => setStatus('playing')
    const onWaiting = () => setStatus('loading')
    const onError = () => {
      // Clearing src during teardown also fires "error"; ignore that case.
      if (audio.getAttribute('src')) setStatus('error')
    }
    audio.addEventListener('playing', onPlaying)
    audio.addEventListener('waiting', onWaiting)
    audio.addEventListener('error', onError)

    return () => {
      audio.removeEventListener('playing', onPlaying)
      audio.removeEventListener('waiting', onWaiting)
      audio.removeEventListener('error', onError)
      hlsRef.current?.destroy()
      hlsRef.current = null
      audio.pause()
      audio.removeAttribute('src')
      audioRef.current = null
    }
  }, [])

  return { current, status, play, toggle }
}
