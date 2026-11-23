import { useCallback, useEffect, useState } from 'react'
import { api, getToken, json, setToken } from './api.js'

const fmtDate = (value) => new Intl.DateTimeFormat('pl-PL', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value))
const money = (value) => new Intl.NumberFormat('pl-PL', { style: 'currency', currency: 'PLN' }).format(value)
const blankClass = { title: '', maxCapacity: 12, startTime: '', endTime: '', trainerId: '' }

function App() {
  const [user, setUser] = useState(null)
  const [view, setView] = useState('schedule')
  const [authMode, setAuthMode] = useState('login')
  const [loading, setLoading] = useState(Boolean(getToken()))
  const [notice, setNotice] = useState('')
  const [error, setError] = useState('')

  const refreshProfile = useCallback(async () => {
    if (!getToken()) { setLoading(false); return }
    try {
      const profile = await api('/api/auth/me')
      setUser(profile)
      setView(profile.role === 'ADMIN' ? 'admin' : profile.role === 'TRAINER' ? 'trainer' : 'schedule')
    } catch {
      setToken(null)
      setUser(null)
    } finally { setLoading(false) }
  }, [])

  useEffect(() => { refreshProfile() }, [refreshProfile])
  const run = async (action, success) => {
    setError(''); setNotice('')
    try { const result = await action(); if (success) setNotice(success); return result }
    catch (cause) { setError(cause.message || 'Wystąpił błąd.'); return null }
  }
  const signOut = () => { setToken(null); setUser(null); setView('schedule'); setNotice('Wylogowano.') }

  if (loading) return <div className="grid min-h-screen place-items-center bg-paper text-ink">Ładowanie GymFlex…</div>
  return <div className="min-h-screen bg-paper text-ink">
    <header className="border-b border-black/5 bg-white">
      <div className="mx-auto flex max-w-7xl items-center justify-between px-5 py-4 lg:px-8">
        <button onClick={() => setView('schedule')} className="flex items-center gap-3 text-left">
          <span className="grid h-10 w-10 place-items-center rounded-xl bg-lime text-xl font-black">G</span>
          <span><span className="block text-lg font-black tracking-tight">GymFlex</span><span className="text-xs text-slate-500">KLUB W TWOIM RYTMIE</span></span>
        </button>
        <nav className="hidden items-center gap-6 md:flex">
          <button className="nav-link" onClick={() => setView('schedule')}>Grafik</button>
          {user?.role === 'CLIENT' && <button className="nav-link" onClick={() => setView('account')}>Moje konto</button>}
          {user?.role === 'TRAINER' && <button className="nav-link" onClick={() => setView('trainer')}>Panel trenera</button>}
          {user?.role === 'ADMIN' && <button className="nav-link" onClick={() => setView('admin')}>Panel admina</button>}
        </nav>
        <div className="flex items-center gap-3">
          {user ? <><span className="hidden text-sm text-slate-600 sm:block">{user.firstName} {user.lastName}</span><button className="button-dark !px-4 !py-2" onClick={signOut}>Wyloguj</button></> : <button className="button-dark !px-4 !py-2" onClick={() => setView('auth')}>Zaloguj</button>}
        </div>
      </div>
    </header>
    <main className="mx-auto max-w-7xl px-5 py-8 lg:px-8 lg:py-12">
      {notice && <div className="alert-success">{notice}</div>}
      {error && <div className="alert-error">{error}</div>}
      {view === 'auth' && <AuthPanel mode={authMode} onMode={setAuthMode} onSuccess={async () => { await refreshProfile(); setNotice('Witaj w GymFlex!') }} run={run} />}
      {view === 'schedule' && <Schedule user={user} run={run} onAuth={() => setView('auth')} />}
      {view === 'account' && user?.role === 'CLIENT' && <ClientAccount run={run} />}
      {view === 'trainer' && user?.role === 'TRAINER' && <TrainerPanel run={run} />}
      {view === 'admin' && user?.role === 'ADMIN' && <AdminPanel run={run} />}
      {view !== 'auth' && view !== 'schedule' && !user && <AuthPanel mode={authMode} onMode={setAuthMode} onSuccess={refreshProfile} run={run} />}
    </main>
    <footer className="border-t border-black/5 bg-white"><div className="mx-auto flex max-w-7xl flex-col gap-2 px-5 py-6 text-xs text-slate-500 sm:flex-row sm:justify-between lg:px-8"><span>© GymFlex · Dbaj o formę, po swojemu.</span><a href="http://localhost:8081/swagger-ui.html" target="_blank" rel="noreferrer" className="font-semibold text-slate-700">Dokumentacja API</a></div></footer>
  </div>
}

function AuthPanel({ mode, onMode, onSuccess, run }) {
  const [form, setForm] = useState({ firstName: '', lastName: '', email: '', password: '' })
  const register = mode === 'register'
  const submit = async (event) => {
    event.preventDefault()
    const result = await run(() => api(`/api/auth/${register ? 'register' : 'login'}`, json('POST', form)), register ? 'Konto utworzone.' : 'Zalogowano.')
    if (result?.token) { setToken(result.token); await onSuccess() }
  }
  return <section className="mx-auto grid max-w-5xl overflow-hidden rounded-3xl bg-white shadow-xl shadow-slate-900/5 md:grid-cols-2">
    <div className="relative flex min-h-72 flex-col justify-between overflow-hidden bg-ink p-8 text-white md:p-10"><div className="absolute -right-16 -top-12 h-64 w-64 rounded-full border-[35px] border-lime/20"/><div className="relative"><span className="eyebrow text-lime">FORMA, KTÓRA PASUJE</span><h1 className="mt-5 max-w-sm text-4xl font-black leading-tight">Twój trening.<br/>Twoje tempo.</h1></div><p className="relative mt-10 max-w-sm text-sm leading-6 text-white/65">Zarezerwuj miejsce, wybierz karnet i miej swój plan treningowy zawsze pod ręką.</p></div>
    <form onSubmit={submit} className="space-y-4 p-8 md:p-10"><div><p className="eyebrow text-slate-400">{register ? 'DOŁĄCZ DO NAS' : 'MIŁO CIĘ WIDZIEĆ'}</p><h2 className="mt-2 text-2xl font-extrabold">{register ? 'Utwórz konto' : 'Zaloguj się'}</h2></div>
      {register && <><Field label="Imię" value={form.firstName} onChange={(value) => setForm({ ...form, firstName: value })} required/><Field label="Nazwisko" value={form.lastName} onChange={(value) => setForm({ ...form, lastName: value })} required/></>}
      <Field label="Adres e-mail" type="email" value={form.email} onChange={(value) => setForm({ ...form, email: value })} required/>
      <Field label="Hasło" type="password" value={form.password} onChange={(value) => setForm({ ...form, password: value })} required/>
      <button className="button-lime w-full" type="submit">{register ? 'Utwórz konto' : 'Zaloguj się'} <span>→</span></button>
      <p className="pt-2 text-center text-sm text-slate-500">{register ? 'Masz już konto?' : 'Nie masz konta?'} <button type="button" className="font-bold text-ink underline underline-offset-4" onClick={() => onMode(register ? 'login' : 'register')}>{register ? 'Zaloguj się' : 'Zarejestruj się'}</button></p>
    </form>
  </section>
}

function Schedule({ user, run, onAuth }) {
  const [classes, setClasses] = useState([])
  const [types, setTypes] = useState([])
  const [paymentMethod, setPaymentMethod] = useState('CARD')
  const load = useCallback(async () => {
    const [classRows, typeRows] = await Promise.all([api('/api/classes'), api('/api/membership-types')])
    setClasses(classRows); setTypes(typeRows)
  }, [])
  useEffect(() => { run(load) }, [load])
  const reserve = (classId) => run(async () => { if (!user) { onAuth(); return null }; return api('/api/reservations', json('POST', { fitnessClassId: classId })) }, 'Zapisano na zajęcia.')
  return <>
    <section className="hero-panel"><div className="relative z-10 max-w-2xl"><span className="eyebrow text-lime">RUSZ SIĘ PO SWOJEMU</span><h1 className="mt-5 text-4xl font-black leading-[1.05] tracking-tight text-white sm:text-6xl">Miejsce na<br/><span className="text-lime">Twoją formę.</span></h1><p className="mt-5 max-w-lg text-base leading-7 text-white/65">Dobry trening zaczyna się od planu. Zobacz najbliższe zajęcia i zarezerwuj miejsce.</p><a href="#classes" className="button-lime mt-7 inline-flex">Zobacz grafik <span>↓</span></a></div><div className="hero-orbit"><div className="hero-orbit-inner"><span>MOVE</span><strong>YOUR<br/>WAY</strong><span>GYMFLEX · EST. 2026</span></div></div><div className="hero-number">01<span> / 04</span></div></section>
    <section id="classes" className="mt-14"><SectionTitle kicker="ZNAJDŹ SWÓJ RYTM" title="Nadchodzące zajęcia" aside={`${classes.length} terminów`} />
      <div className="mt-6 grid gap-4 md:grid-cols-2 xl:grid-cols-3">{classes.map((item) => <article key={item.id} className="card group"><div className="flex items-start justify-between"><span className="date-chip">{new Intl.DateTimeFormat('pl-PL', { weekday: 'short', day: 'numeric', month: 'short' }).format(new Date(item.startTime))}</span><span className="text-sm font-semibold text-slate-400">{new Intl.DateTimeFormat('pl-PL', { timeStyle: 'short' }).format(new Date(item.startTime))}</span></div><h3 className="mt-5 text-xl font-extrabold">{item.title}</h3><p className="mt-1 text-sm text-slate-500">z {item.trainer}</p><div className="mt-5 flex items-center justify-between border-t border-slate-100 pt-4"><span className="text-sm text-slate-500">Limit <b className="text-ink">{item.maxCapacity}</b> osób</span>{user?.role === 'CLIENT' ? <button className="button-dark !px-4 !py-2" onClick={() => reserve(item.id)}>Zapisz się <span>→</span></button> : !user ? <button className="button-outline !px-4 !py-2" onClick={onAuth}>Zaloguj się</button> : null}</div></article>)}</div>
      {!classes.length && <Empty text="Brak zaplanowanych zajęć. Wróć tu wkrótce."/>}
    </section>
    <section className="mt-16"><SectionTitle kicker="WYBIERZ SWÓJ PLAN" title="Karnety" aside={user?.role === 'CLIENT' ? <select aria-label="Metoda płatności" className="field !w-auto !py-2" value={paymentMethod} onChange={(e) => setPaymentMethod(e.target.value)}><option value="CARD">Karta</option><option value="BLIK">BLIK</option><option value="CASH">Gotówka</option><option value="TRANSFER">Przelew</option></select> : 'Zakup bezpośrednio w klubie'}/><div className="mt-6 grid gap-4 sm:grid-cols-2 xl:grid-cols-3">{types.map((type, index) => <article key={type.id} className={`card ${index === 1 ? 'card-featured' : ''}`}><span className={`eyebrow ${index === 1 ? 'text-lime' : 'text-slate-400'}`}>{type.durationDays} DNI DOSTĘPU</span><h3 className={`mt-3 text-xl font-extrabold ${index === 1 ? 'text-white' : 'text-ink'}`}>{type.name}</h3><p className={`mt-4 text-3xl font-black ${index === 1 ? 'text-lime' : ''}`}>{money(type.price)}</p><p className={`mt-1 text-sm ${index === 1 ? 'text-white/50' : 'text-slate-500'}`}>jednorazowo</p>{user?.role === 'CLIENT' && <button onClick={() => run(() => api('/api/memberships/purchase', json('POST', { membershipTypeId: type.id, paymentMethod })), 'Karnet kupiony.')} className={`mt-6 w-full ${index === 1 ? 'button-lime' : 'button-outline'}`}>Kup karnet <span>→</span></button>}</article>)}</div></section>
  </>
}

function ClientAccount({ run }) {
  const [data, setData] = useState({ memberships: [], reservations: [], payments: [] })
  const load = useCallback(async () => {
    const [memberships, reservations, payments] = await Promise.all([api('/api/memberships/mine'), api('/api/reservations/mine'), api('/api/payments/mine')])
    setData({ memberships, reservations, payments })
  }, [])
  useEffect(() => { run(load) }, [load])
  return <><SectionTitle kicker="TWOJE GYMFLEX" title="Moje konto"/><div className="mt-8 grid gap-6 xl:grid-cols-2"><Panel title="Moje karnety">{data.memberships.map((m) => <div className="list-row" key={m.membershipId}><div><b>{m.membershipTypeName}</b><p className="muted">{m.startDate} — {m.endDate}</p></div><Status value={m.paymentStatus}/></div>)}{!data.memberships.length && <Empty text="Nie masz jeszcze karnetu."/>}</Panel><Panel title="Moje rezerwacje">{data.reservations.map((r) => <div className="list-row" key={r.reservationId}><div><b>{r.className}</b><p className="muted">{fmtDate(r.startTime)} · {r.trainerFullName}</p></div>{r.status === 'CONFIRMED' && new Date(r.startTime) > new Date() ? <button className="button-quiet" onClick={() => run(() => api(`/api/reservations/${r.reservationId}`, { method: 'DELETE' }), 'Wypisano z zajęć.').then(load)}>Wypisz</button> : <Status value={r.status}/>}</div>)}{!data.reservations.length && <Empty text="Nie masz nadchodzących rezerwacji."/>}</Panel></div><div className="mt-6"><Panel title="Historia płatności">{data.payments.map((p) => <div className="list-row" key={p.id}><div><b>{money(p.amount)}</b><p className="muted">{p.method} · {p.paidAt ? fmtDate(p.paidAt) : 'oczekuje'}</p></div><Status value={p.status}/></div>)}{!data.payments.length && <Empty text="Brak płatności."/>}</Panel></div></>
}

function TrainerPanel({ run }) {
  const [classes, setClasses] = useState([])
  const [participants, setParticipants] = useState({})
  const [form, setForm] = useState(blankClass)
  const load = useCallback(async () => setClasses(await api('/api/classes/mine')), [])
  useEffect(() => { run(load) }, [load])
  const create = async (event) => { event.preventDefault(); const ok = await run(() => api('/api/classes', json('POST', { ...form, maxCapacity: Number(form.maxCapacity) })), 'Dodano zajęcia.'); if (ok) { setForm(blankClass); load() } }
  const showParticipants = async (id) => { const rows = await run(() => api(`/api/classes/${id}/participants`)); if (rows) setParticipants({ ...participants, [id]: rows }) }
  return <><SectionTitle kicker="STREFA TRENERA" title="Twoje zajęcia"/><div className="mt-8 grid gap-6 xl:grid-cols-[0.8fr_1.2fr]"><Panel title="Dodaj do grafiku"><form className="space-y-4" onSubmit={create}><Field label="Nazwa zajęć" value={form.title} onChange={(v) => setForm({ ...form, title: v })} required/><Field label="Limit miejsc" type="number" value={form.maxCapacity} onChange={(v) => setForm({ ...form, maxCapacity: v })} required/><Field label="Początek" type="datetime-local" value={form.startTime} onChange={(v) => setForm({ ...form, startTime: v })} required/><Field label="Koniec" type="datetime-local" value={form.endTime} onChange={(v) => setForm({ ...form, endTime: v })} required/><button className="button-lime w-full">Dodaj zajęcia <span>→</span></button></form></Panel><Panel title="Twój grafik">{classes.map((item) => <div className="border-b border-slate-100 py-4 last:border-0" key={item.id}><div className="flex flex-wrap items-center justify-between gap-3"><div><b>{item.title}</b><p className="muted">{fmtDate(item.startTime)} · {item.maxCapacity} miejsc</p></div><button className="button-outline !px-3 !py-2" onClick={() => showParticipants(item.id)}>Uczestnicy</button></div>{participants[item.id] && <div className="mt-4 space-y-2">{participants[item.id].map((person) => <div className="list-row !px-0" key={person.reservationId}><span className="text-sm">{person.userFullName}</span>{new Date(item.endTime) < new Date() ? <AttendanceActions item={item} person={person} run={run} onDone={() => showParticipants(item.id)}/> : <span className="muted">Zapisany</span>}</div>)}{!participants[item.id].length && <p className="muted">Brak zapisanych osób.</p>}</div>}</div>)}{!classes.length && <Empty text="Nie masz jeszcze zajęć w grafiku."/>}</Panel></div></>
}

function AttendanceActions({ item, person, run, onDone }) {
  const mark = (status) => run(() => api(`/api/classes/${item.id}/reservations/${person.reservationId}/attendance`, json('PUT', { status })), 'Zapisano obecność.').then(onDone)
  return <div className="flex gap-2"><button className="button-mini" onClick={() => mark('ATTENDED')}>Obecny</button><button className="button-mini button-mini-muted" onClick={() => mark('NO_SHOW')}>Nieobecny</button></div>
}

function AdminPanel({ run }) {
  const [tab, setTab] = useState('overview')
  const tabs = [['overview', 'Przegląd'], ['classes', 'Grafik'], ['passes', 'Karnety'], ['users', 'Konta'], ['payments', 'Płatności'], ['logs', 'Dziennik']]
  return <><SectionTitle kicker="CENTRUM ZARZĄDZANIA" title="Panel administratora"/><div className="mt-7 flex gap-2 overflow-x-auto pb-2">{tabs.map(([key, label]) => <button key={key} onClick={() => setTab(key)} className={`tab-button ${tab === key ? 'tab-active' : ''}`}>{label}</button>)}</div>{tab === 'overview' && <AdminOverview run={run}/ >}{tab === 'classes' && <AdminClasses run={run}/ >}{tab === 'passes' && <AdminPasses run={run}/ >}{tab === 'users' && <><AdminUsers run={run}/><AdminUserList run={run}/></>}{tab === 'payments' && <AdminPayments run={run}/ >}{tab === 'logs' && <AdminLogs run={run}/ >}</>
}

function AdminOverview({ run }) {
  const [rows, setRows] = useState([])
  const [activeClients, setActiveClients] = useState([])
  const [sort, setSort] = useState('startTime')
  const load = useCallback(async () => { const [occupancy, clients] = await Promise.all([api(`/api/classes/occupancy?sort=${sort}`), api('/api/users/report/most-active')]); setRows(occupancy); setActiveClients(clients) }, [sort])
  useEffect(() => { run(load) }, [load])
  return <div className="space-y-6"><Panel title="Obłożenie nadchodzących zajęć" aside={<select className="field !py-2" value={sort} onChange={(e) => setSort(e.target.value)}><option value="startTime">Według terminu</option><option value="occupancy">Według obłożenia</option></select>}><div className="overflow-x-auto"><table className="w-full min-w-[620px] text-left text-sm"><thead className="table-head"><tr><th>Zajęcia</th><th>Termin</th><th>Trener</th><th>Miejsca</th><th>Obłożenie</th></tr></thead><tbody>{rows.map((row) => <tr className="table-row" key={row.classId}><td className="font-bold">{row.title}</td><td>{fmtDate(row.startTime)}</td><td>{row.trainer}</td><td>{row.booked}/{row.maxCapacity}</td><td><div className="flex items-center gap-2"><div className="h-2 w-24 rounded-full bg-slate-100"><div className="h-full rounded-full bg-lime-500" style={{ width: `${Math.min(row.occupancyPct, 100)}%` }}/></div>{row.occupancyPct}%</div></td></tr>)}</tbody></table>{!rows.length && <Empty text="Brak nadchodzących zajęć."/>}</div></Panel><Panel title="Najaktywniejsi klienci · ostatnie 30 dni"><div className="overflow-x-auto"><table className="w-full min-w-[480px] text-left text-sm"><thead className="table-head"><tr><th>Klient</th><th>Odbyte treningi</th></tr></thead><tbody>{activeClients.map((row) => <tr className="table-row" key={row[0]}><td className="font-bold">{row[1]} {row[2]}</td><td>{row[3]}</td></tr>)}</tbody></table>{!activeClients.length && <Empty text="Brak klientów spełniających kryteria raportu."/>}</div></Panel></div>
}

function AdminClasses({ run }) {
  const [classes, setClasses] = useState([])
  const [trainers, setTrainers] = useState([])
  const [form, setForm] = useState(blankClass)
  const [editingId, setEditingId] = useState(null)
  const load = useCallback(async () => { const [items, users] = await Promise.all([api('/api/classes'), api('/api/users/trainers')]); setClasses(items); setTrainers(users) }, [])
  useEffect(() => { run(load) }, [load])
  const submit = async (event) => { event.preventDefault(); const method = editingId ? 'PUT' : 'POST'; const ok = await run(() => api(editingId ? `/api/classes/${editingId}` : '/api/classes', json(method, { ...form, maxCapacity: Number(form.maxCapacity), trainerId: form.trainerId ? Number(form.trainerId) : null })), editingId ? 'Zmieniono zajęcia.' : 'Dodano zajęcia.'); if (ok) { setForm(blankClass); setEditingId(null); load() } }
  const edit = (item) => { const toInput = (v) => { const d = new Date(v); return new Date(d.getTime() - d.getTimezoneOffset() * 60000).toISOString().slice(0, 16) }; setEditingId(item.id); setForm({ title: item.title, maxCapacity: item.maxCapacity, startTime: toInput(item.startTime), endTime: toInput(item.endTime), trainerId: item.trainerId }) }
  return <div className="grid gap-6 xl:grid-cols-[0.8fr_1.2fr]"><Panel title={editingId ? 'Edytuj zajęcia' : 'Dodaj zajęcia'}><form className="space-y-3" onSubmit={submit}><Field label="Nazwa" value={form.title} onChange={(v) => setForm({ ...form, title: v })} required/><Field label="Limit miejsc" type="number" value={form.maxCapacity} onChange={(v) => setForm({ ...form, maxCapacity: v })} required/><label className="field-label">Trener<select className="field" value={form.trainerId} onChange={(e) => setForm({ ...form, trainerId: e.target.value })}><option value="">Administrator</option>{trainers.map((t) => <option value={t.id} key={t.id}>{t.firstName} {t.lastName}</option>)}</select></label><Field label="Początek" type="datetime-local" value={form.startTime} onChange={(v) => setForm({ ...form, startTime: v })} required/><Field label="Koniec" type="datetime-local" value={form.endTime} onChange={(v) => setForm({ ...form, endTime: v })} required/><button className="button-lime w-full">{editingId ? 'Zapisz zmiany' : 'Dodaj do grafiku'} <span>→</span></button>{editingId && <button type="button" className="button-outline w-full" onClick={() => { setEditingId(null); setForm(blankClass) }}>Anuluj edycję</button>}</form></Panel><Panel title="Grafik i zarządzanie">{classes.map((item) => <div key={item.id} className="list-row"><div><b>{item.title}</b><p className="muted">{fmtDate(item.startTime)} · {item.trainer}</p></div><div className="flex gap-2"><button className="button-quiet" onClick={() => edit(item)}>Edytuj</button><button className="button-quiet text-rose-600" onClick={() => run(() => api(`/api/classes/${item.id}`, { method: 'DELETE' }), 'Zajęcia odwołane.').then(load)}>Odwołaj</button></div></div>)}{!classes.length && <Empty text="Brak zaplanowanych zajęć."/>}</Panel></div>
}

function AdminPasses({ run }) {
  const [types, setTypes] = useState([])
  const [form, setForm] = useState({ name: '', price: '', durationDays: 30 })
  const [editingId, setEditingId] = useState(null)
  const load = useCallback(async () => setTypes(await api('/api/membership-types')), [])
  useEffect(() => { run(load) }, [load])
  const submit = async (e) => { e.preventDefault(); const ok = await run(() => api(editingId ? `/api/membership-types/${editingId}` : '/api/membership-types', json(editingId ? 'PUT' : 'POST', { ...form, price: Number(form.price), durationDays: Number(form.durationDays) })), editingId ? 'Zmieniono karnet.' : 'Dodano karnet.'); if (ok) { setForm({ name: '', price: '', durationDays: 30 }); setEditingId(null); load() } }
  return <div className="grid gap-6 lg:grid-cols-[0.7fr_1.3fr]"><Panel title={editingId ? 'Edytuj typ karnetu' : 'Dodaj typ karnetu'}><form className="space-y-4" onSubmit={submit}><Field label="Nazwa" value={form.name} onChange={(v) => setForm({ ...form, name: v })} required/><Field label="Cena (PLN)" type="number" min="0" step="0.01" value={form.price} onChange={(v) => setForm({ ...form, price: v })} required/><Field label="Ważność (dni)" type="number" min="1" value={form.durationDays} onChange={(v) => setForm({ ...form, durationDays: v })} required/><button className="button-lime w-full">{editingId ? 'Zapisz zmiany' : 'Dodaj karnet'} <span>→</span></button>{editingId && <button type="button" className="button-outline w-full" onClick={() => { setEditingId(null); setForm({ name: '', price: '', durationDays: 30 }) }}>Anuluj edycję</button>}</form></Panel><Panel title="Aktualna oferta">{types.map((item) => <div className="list-row" key={item.id}><div><b className="text-ink">{item.name}</b><p className="muted">{money(item.price)} · {item.durationDays} dni · {item.active ? 'aktywny' : 'wycofany'}</p></div>{item.active && <div className="flex gap-2"><button className="button-quiet" onClick={() => { setEditingId(item.id); setForm({ name: item.name, price: item.price, durationDays: item.durationDays }) }}>Edytuj</button><button className="button-quiet text-rose-600" onClick={() => run(() => api(`/api/membership-types/${item.id}`, { method: 'DELETE' }), 'Karnet wycofany.').then(load)}>Wycofaj</button></div>}</div>)}</Panel></div>
}

function AdminUsers({ run }) {
  const [form, setForm] = useState({ firstName: '', lastName: '', email: '', password: '', role: 'TRAINER' })
  const create = async (e) => { e.preventDefault(); await run(async () => { await api('/api/users', json('POST', form)); setForm({ firstName: '', lastName: '', email: '', password: '', role: 'TRAINER' }) }, 'Utworzono konto.') }
  return <Panel title="Załóż konto trenera lub administratora"><form onSubmit={create} className="grid gap-4 sm:grid-cols-2"><Field label="Imię" value={form.firstName} onChange={(v) => setForm({ ...form, firstName: v })} required/><Field label="Nazwisko" value={form.lastName} onChange={(v) => setForm({ ...form, lastName: v })} required/><Field label="E-mail" type="email" value={form.email} onChange={(v) => setForm({ ...form, email: v })} required/><Field label="Hasło" type="password" value={form.password} onChange={(v) => setForm({ ...form, password: v })} required/><label className="field-label">Rola<select className="field" value={form.role} onChange={(e) => setForm({ ...form, role: e.target.value })}><option value="TRAINER">Trener</option><option value="ADMIN">Administrator</option></select></label><div className="flex items-end"><button className="button-lime w-full">Utwórz konto <span>→</span></button></div></form></Panel>
}

function AdminUserList({ run }) {
  const [users, setUsers] = useState([])
  const load = useCallback(async () => setUsers(await api('/api/users/admin')), [])
  useEffect(() => { run(load) }, [load])
  return <Panel title="Konta użytkowników">{users.map((user) => <div className="list-row" key={user.id}><div><b>{user.firstName} {user.lastName}</b><p className="muted">{user.email} · {user.role} · {user.active ? 'Aktywne' : 'Nieaktywne'}</p></div>{user.active && <button className="button-quiet text-rose-600" onClick={() => run(() => api(`/api/users/${user.id}`, { method: 'DELETE' }), 'Konto dezaktywowano.').then(load)}>Dezaktywuj</button>}</div>)}{!users.length && <Empty text="Brak kont użytkowników."/>}</Panel>
}

function AdminPayments({ run }) {
  const [rows, setRows] = useState([])
  const load = useCallback(async () => setRows(await api('/api/payments')), [])
  useEffect(() => { run(load) }, [load])
  return <Panel title="Płatności">{rows.map((p) => <div className="list-row" key={p.id}><div><b>{money(p.amount)}</b><p className="muted">Płatność #{p.id} · {p.method} · karnet #{p.membershipId}</p></div><div className="flex items-center gap-3"><Status value={p.status}/><select aria-label="Zmień status płatności" className="field !w-auto !py-2" value={p.status} onChange={(e) => run(() => api(`/api/payments/${p.id}/status`, json('PATCH', { status: e.target.value })), 'Zmieniono status płatności.').then(load)}><option value="PAID">Opłacona</option><option value="FAILED">Nieudana</option><option value="REFUNDED">Zwrócona</option></select></div></div>)}</Panel>
}

function AdminLogs({ run }) {
  const [logs, setLogs] = useState([])
  const load = useCallback(async () => setLogs(await api('/api/audit-logs')), [])
  useEffect(() => { run(load) }, [load])
  return <Panel title="Dziennik zdarzeń">{logs.map((log) => <div className="list-row" key={log.id}><div><b>{log.action}</b><p className="muted">{log.details} · użytkownik #{log.userId ?? 'system'}</p></div><span className="muted">{fmtDate(log.createdAt)}</span></div>)}{!logs.length && <Empty text="Dziennik jest pusty."/>}</Panel>
}

function Field({ label, value, onChange, type = 'text', ...props }) {
  return <label className="field-label">{label}<input className="field" type={type} value={value ?? ''} onChange={(e) => onChange(e.target.value)} {...props}/></label>
}
function Panel({ title, aside, children }) { return <section className="rounded-2xl border border-black/5 bg-white p-5 shadow-sm sm:p-6"><div className="mb-4 flex items-center justify-between gap-4"><h2 className="text-lg font-extrabold">{title}</h2>{aside}</div>{children}</section> }
function SectionTitle({ kicker, title, aside }) { return <div className="flex flex-wrap items-end justify-between gap-3"><div><p className="eyebrow text-slate-400">{kicker}</p><h1 className="mt-2 text-3xl font-black tracking-tight sm:text-4xl">{title}</h1></div>{aside && <span className="text-sm text-slate-500">{aside}</span>}</div> }
function Empty({ text }) { return <div className="rounded-xl bg-paper px-4 py-8 text-center text-sm text-slate-500">{text}</div> }
function Status({ value }) { const green = ['PAID', 'CONFIRMED', 'ATTENDED'].includes(value); return <span className={`status ${green ? 'status-green' : 'status-neutral'}`}>{value}</span> }

export default App
