import { useState } from 'react'
import type { Station } from '../data/stations'

/** Station logo, falling back to a plain lettered tile when the file is missing. */
export function StationLogo({ station, size = 48 }: { station: Station; size?: number }) {
  const [failed, setFailed] = useState(false)

  if (failed) {
    return (
      <span
        aria-hidden="true"
        style={{
          display: 'inline-block',
          width: size,
          height: size,
          lineHeight: `${size}px`,
          textAlign: 'center',
          border: '1px solid',
        }}
      >
        {station.name.slice(0, 2)}
      </span>
    )
  }

  // Decorative: the station name is always rendered next to the logo.
  return (
    <img src={station.logo} alt="" width={size} height={size} onError={() => setFailed(true)} />
  )
}
