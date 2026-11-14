import { StationList } from './components/StationList'
import { stations } from './data/stations'
import { useRadioPlayer, type PlayerStatus } from './player/useRadioPlayer'

const STATUS_TEXT: Record<PlayerStatus, string> = {
  idle: 'בחרו תחנה',
  loading: 'מתחבר…',
  playing: 'מתנגן',
  paused: 'מושהה',
  error: 'לא הצלחנו להתחבר לתחנה',
}

export default function App() {
  const { current, status, play, toggle } = useRadioPlayer()
  const isActive = status === 'playing' || status === 'loading'

  return (
    <main>
      <h1>רדיו ישראלי</h1>

      <StationList stations={stations} currentId={current?.id ?? null} onSelect={play} />

      <section aria-label="נגן">
        <p>{current ? current.name : 'לא נבחרה תחנה'}</p>
        <p role="status">{STATUS_TEXT[status]}</p>
        <button type="button" onClick={toggle} disabled={!current}>
          {isActive ? 'השהה' : 'נגן'}
        </button>
      </section>
    </main>
  )
}
