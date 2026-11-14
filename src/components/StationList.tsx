import { CATEGORIES, type Station } from '../data/stations'
import { StationLogo } from './StationLogo'

interface Props {
  stations: Station[]
  currentId: string | null
  onSelect: (station: Station) => void
}

/** Home screen: stations grouped by category, empty categories omitted. */
export function StationList({ stations, currentId, onSelect }: Props) {
  return (
    <>
      {CATEGORIES.map(({ id, label }) => {
        const group = stations.filter((station) => station.category === id)
        if (group.length === 0) return null
        return (
          <section key={id} aria-labelledby={`category-${id}`}>
            <h2 id={`category-${id}`}>{label}</h2>
            <ul>
              {group.map((station) => (
                <li key={station.id}>
                  <button
                    type="button"
                    onClick={() => onSelect(station)}
                    aria-pressed={station.id === currentId}
                  >
                    <StationLogo station={station} />
                    <span>{station.name}</span>
                  </button>
                </li>
              ))}
            </ul>
          </section>
        )
      })}
    </>
  )
}
