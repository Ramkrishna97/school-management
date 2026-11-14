import { lazy, Suspense, useState, type FormEvent, type ReactNode } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { client, errorMessage, useSession, type Allocation, type InventoryItem,
  type DailyPeriod, type PurchaseRequisition, type Session, type SubstitutionNotice, type Teacher, type TeacherLeave,
  type TimetableEntry, type VendorOrder } from './api'

const InventoryChart = lazy(() => import('./InventoryChart'))

const weekdays = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY']
const today = new Date().toLocaleDateString('en-CA')

function Login() {
  const setSession = useSession(state => state.setSession)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const data = new FormData(event.currentTarget)
    setBusy(true)
    setError('')
    try {
      const response = await client.post<Session>('/auth/login', {
        username: data.get('username'), password: data.get('password'),
      })
      setSession(response.data)
    } catch (cause) {
      setError(errorMessage(cause))
    } finally {
      setBusy(false)
    }
  }

  return <main className="mx-auto flex min-h-screen max-w-md items-center px-5">
    <section className="card w-full">
      <p className="text-sm font-semibold uppercase tracking-widest text-blue-700">School Management</p>
      <h1 className="mt-2 text-3xl font-bold">Sign in</h1>
      <p className="mt-2 mb-6 text-sm text-slate-600">Use a seeded account to explore the current services.</p>
      {error && <p role="alert" className="mb-4 rounded bg-red-100 p-3 text-red-900">{error}</p>}
      <form onSubmit={submit} className="grid gap-3">
        <label>Username<input name="username" defaultValue="principal" autoComplete="username" required /></label>
        <label>Password<input name="password" type="password" autoComplete="current-password" required /></label>
        <button disabled={busy}>{busy ? 'Signing in…' : 'Sign in'}</button>
      </form>
      <p className="mt-5 text-xs text-slate-500">Local demo users: principal, admin, teacher, storemanager. Set SCHOOL_BOOTSTRAP_PASSWORD to change the seeded password.</p>
    </section>
  </main>
}

function Section({ title, children }: { title: string, children: ReactNode }) {
  return <section className="card"><h2 className="heading">{title}</h2>{children}</section>
}

function Dashboard() {
  const session = useSession(state => state.session)!
  const clear = useSession(state => state.clear)
  const queries = useQueryClient()
  const [tab, setTab] = useState('Overview')
  const [day, setDay] = useState('MONDAY')
  const [date, setDate] = useState(today)
  const [className, setClassName] = useState('7')
  const [section, setSection] = useState('A')
  const [teacherId, setTeacherId] = useState<number>(session.teacherId ?? 1)
  const [notice, setNotice] = useState('')
  const [error, setError] = useState('')
  const [scanResults, setScanResults] = useState<Allocation[] | null>(null)
  const [vendorName, setVendorName] = useState('School Supplies Co.')

  const canManageTeachers = ['SUPER_ADMIN', 'PRINCIPAL'].includes(session.role)
  const canViewTeachers = canManageTeachers || session.role === 'TEACHER'
  const canViewTimetable = canViewTeachers
  const canViewInventory = ['SUPER_ADMIN', 'PRINCIPAL', 'STORE_MANAGER'].includes(session.role)
  const canManageInventory = ['SUPER_ADMIN', 'STORE_MANAGER'].includes(session.role)

  const teachersQuery = useQuery({ queryKey: ['teachers'], enabled: canManageTeachers,
    queryFn: () => client.get<Teacher[]>('/teachers').then(response => response.data) })
  const ownTeacherQuery = useQuery({ queryKey: ['teacher', session.teacherId],
    enabled: session.role === 'TEACHER' && session.teacherId !== null,
    queryFn: () => client.get<Teacher>(`/teachers/${session.teacherId}`).then(response => response.data) })
  const teachers = teachersQuery.data ?? (ownTeacherQuery.data ? [ownTeacherQuery.data] : [])
  const timetableQuery = useQuery({ queryKey: ['timetable', day], enabled: canViewTimetable,
    queryFn: () => client.get<TimetableEntry[]>(`/timetables?dayOfWeek=${day}`).then(response => response.data) })
  const allocationQuery = useQuery({ queryKey: ['allocations', date], enabled: canViewTimetable,
    queryFn: () => client.get<Allocation[]>(`/substitutions?date=${date}`).then(response => response.data) })
  const dailyQuery = useQuery({ queryKey: ['daily', date, className, section], enabled: canViewTimetable,
    queryFn: () => client.get<DailyPeriod[]>('/timetables/daily',
      { params: { date, className, section } }).then(response => response.data) })
  const noticesQuery = useQuery({ queryKey: ['notices', session.teacherId],
    enabled: session.role === 'TEACHER' && session.teacherId !== null,
    queryFn: () => client.get<SubstitutionNotice[]>('/substitutions/notices/mine').then(response => response.data) })
  const inventoryQuery = useQuery({ queryKey: ['inventory'], enabled: canViewInventory,
    queryFn: () => client.get<InventoryItem[]>('/inventory/items').then(response => response.data) })
  const requisitionsQuery = useQuery({ queryKey: ['requisitions'], enabled: canViewInventory,
    queryFn: () => client.get<PurchaseRequisition[]>('/inventory/requisitions').then(response => response.data) })
  const ordersQuery = useQuery({ queryKey: ['orders'], enabled: canViewInventory,
    queryFn: () => client.get<VendorOrder[]>('/inventory/orders').then(response => response.data) })
  const leavesQuery = useQuery({ queryKey: ['leaves', teacherId], enabled: canViewTeachers && teacherId > 0,
    queryFn: () => client.get<TeacherLeave[]>(`/teachers/${teacherId}/leaves`).then(response => response.data) })

  async function act(work: Promise<unknown>, message: string) {
    setError('')
    try {
      await work
      setNotice(message)
      await queries.invalidateQueries()
    } catch (cause) {
      setNotice('')
      setError(errorMessage(cause))
    }
  }

  function createTeacher(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const data = new FormData(event.currentTarget)
    void act(client.post('/teachers', {
      name: data.get('name'), department: data.get('department'),
      qualification: data.get('qualification'), classTeacherOf: data.get('classTeacherOf') || null,
      subjectCodes: String(data.get('subjects')).split(',').map(x => x.trim()).filter(Boolean),
    }), 'Teacher added')
    event.currentTarget.reset()
  }

  function applyLeave(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const data = new FormData(event.currentTarget)
    void act(client.post(`/teachers/${teacherId}/leaves`, {
      startDate: data.get('startDate'), endDate: data.get('endDate'),
      type: data.get('type'), reason: data.get('reason'),
    }), 'Leave request submitted for approval')
    event.currentTarget.reset()
  }

  function createPeriod(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const data = new FormData(event.currentTarget)
    void act(client.post('/timetables', {
      dayOfWeek: day, periodNumber: Number(data.get('period')),
      className: data.get('className'), section: data.get('section'),
      subjectCode: data.get('subject'), teacherId: Number(data.get('teacher')),
    }), 'Period added')
    event.currentTarget.reset()
  }

  function createItem(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const data = new FormData(event.currentTarget)
    void act(client.post('/inventory/items', {
      sku: data.get('sku'), name: data.get('name'), category: data.get('category'),
      quantity: Number(data.get('quantity')), minimumQuantity: Number(data.get('minimum')),
      reorderQuantity: Number(data.get('reorder')), status: 'OPERATIONAL',
    }), 'Inventory item added')
    event.currentTarget.reset()
  }

  async function scan() {
    setError('')
    try {
      const response = await client.post<Allocation[]>(`/substitutions/allocate-absences/${date}`)
      setScanResults(response.data)
      setNotice('Substitution scan complete')
      await queries.invalidateQueries({ queryKey: ['allocations', date] })
    } catch (cause) {
      setError(errorMessage(cause))
    }
  }

  const nameOf = (id: number | null) => teachers.find(teacher => teacher.id === id)?.name ??
    (id === null ? 'Unfilled' : `Teacher #${id}`)
  const nav = ['Overview', ...(canViewTeachers ? ['Teachers', 'Timetable'] : []),
    ...(canViewInventory ? ['Inventory'] : [])]
  const items = inventoryQuery.data ?? []

  return <main className="mx-auto max-w-7xl space-y-6 px-5 py-8">
    <header className="flex flex-wrap items-center justify-between gap-4">
      <div><p className="text-sm font-semibold uppercase tracking-widest text-blue-700">School Management</p>
        <h1 className="mt-1 text-3xl font-bold">Operations dashboard</h1></div>
      <div className="text-right text-sm"><p>{session.username} · {session.role.replace('_', ' ')}</p>
        <button className="mt-2" onClick={clear}>Sign out</button></div>
    </header>
    <nav className="flex flex-wrap gap-2" aria-label="Sections">{nav.map(item =>
      <button key={item} className={tab === item ? '' : 'secondary'} onClick={() => setTab(item)}>{item}</button>)}</nav>
    {notice && <p role="status" className="rounded bg-green-100 p-3 text-green-900">{notice}</p>}
    {error && <p role="alert" className="rounded bg-red-100 p-3 text-red-900">{error}</p>}

    {tab === 'Overview' && <div className="grid gap-5 lg:grid-cols-3">
      <Section title="Teachers"><p className="metric">{canViewTeachers ? teachers.length : '—'}</p><p>Profiles available</p></Section>
      <Section title="Timetable"><p className="metric">{canViewTimetable ? (timetableQuery.data?.length ?? 0) : '—'}</p><p>Periods on {day.toLowerCase()}</p></Section>
      <Section title="Inventory alerts"><p className="metric">{canViewInventory ? items.filter(item => item.needsReorder).length : '—'}</p><p>Items below minimum</p></Section>
      {canViewInventory && <div className="lg:col-span-3"><Section title="Stock versus minimum">
        <Suspense fallback={<p>Loading chart...</p>}><InventoryChart items={items} /></Suspense>
      </Section></div>}
    </div>}

    {tab === 'Teachers' && canViewTeachers && <div className="grid gap-5 lg:grid-cols-2">
      <Section title="Teacher profiles">
        <ul className="divide-y divide-slate-200">{teachers.map(teacher => <li key={teacher.id} className="py-3 text-sm">
          <strong>{teacher.name}</strong> · {teacher.department} · {teacher.subjectCodes.join(', ')}
          <p className="text-slate-500">{teacher.qualification} · Casual {teacher.casualLeaveBalance} / Sick {teacher.sickLeaveBalance} / Earned {teacher.earnedLeaveBalance}</p>
        </li>)}</ul>
        {canManageTeachers && <form onSubmit={createTeacher} className="mt-5 grid gap-2 border-t pt-5">
          <h3 className="font-semibold">Add teacher</h3><input name="name" placeholder="Full name" required />
          <input name="department" placeholder="Department" required />
          <input name="qualification" placeholder="Qualification" required />
          <input name="classTeacherOf" placeholder="Class teacher of (optional)" />
          <input name="subjects" placeholder="Subject codes, comma separated" required /><button>Add teacher</button>
        </form>}
      </Section>
      <Section title="Leave requests">
        {canManageTeachers && <label>Teacher<select value={teacherId} onChange={event => setTeacherId(Number(event.target.value))}>
          {teachers.map(teacher => <option key={teacher.id} value={teacher.id}>{teacher.name}</option>)}</select></label>}
        <form onSubmit={applyLeave} className="mt-3 grid gap-2">
          <div className="grid grid-cols-2 gap-2"><label>From<input name="startDate" type="date" min={today} required /></label>
            <label>Through<input name="endDate" type="date" min={today} required /></label></div>
          <select name="type"><option>CASUAL</option><option>SICK</option><option>EARNED</option></select>
          <input name="reason" placeholder="Reason" required /><button>Request leave</button>
        </form>
        <ul className="mt-5 space-y-2">{(leavesQuery.data ?? []).map(leave =>
          <li key={leave.id} className="rounded bg-slate-100 p-3 text-sm">{leave.startDate}–{leave.endDate} · {leave.type} · {leave.status}<br />{leave.reason}
            {canManageTeachers && leave.status === 'PENDING' && <div className="mt-2 flex gap-2">
              <button onClick={() => void act(client.patch(`/teachers/leaves/${leave.id}/decision`, { status: 'APPROVED' }), 'Leave approved and substitution scan requested')}>Approve</button>
              <button className="secondary" onClick={() => void act(client.patch(`/teachers/leaves/${leave.id}/decision`, { status: 'REJECTED' }), 'Leave rejected')}>Reject</button>
            </div>}
          </li>)}</ul>
      </Section>
    </div>}

    {tab === 'Timetable' && canViewTimetable && <div className="grid gap-5 lg:grid-cols-2">
      <Section title="Weekly timetable">
        <label>Day<select value={day} onChange={event => setDay(event.target.value)}>{weekdays.map(value => <option key={value}>{value}</option>)}</select></label>
        <div className="mt-4 overflow-x-auto"><table><thead><tr><th>Period</th><th>Class</th><th>Subject</th><th>Teacher</th></tr></thead><tbody>
          {(timetableQuery.data ?? []).map(entry => <tr key={entry.id}><td>{entry.periodNumber}</td><td>{entry.className}-{entry.section}</td><td>{entry.subjectCode}</td><td>{nameOf(entry.teacherId)}</td></tr>)}</tbody></table></div>
        {canManageTeachers && <form onSubmit={createPeriod} className="mt-5 grid gap-2 border-t pt-5">
          <h3 className="font-semibold">Add period</h3><input name="period" type="number" min="1" max="8" placeholder="Period" required />
          <div className="grid grid-cols-2 gap-2"><input name="className" placeholder="Class" required /><input name="section" placeholder="Section" required /></div>
          <input name="subject" placeholder="Subject code" required />
          <select name="teacher" required>{teachers.map(teacher => <option key={teacher.id} value={teacher.id}>{teacher.name}</option>)}</select>
          <button>Add period</button>
        </form>}
      </Section>
      <Section title="Substitutions">
        <label>Date<input type="date" value={date} onChange={event => { setDate(event.target.value); setScanResults(null) }} /></label>
        <div className="mt-2 grid grid-cols-2 gap-2"><label>Class<input value={className} onChange={event => setClassName(event.target.value)} /></label>
          <label>Section<input value={section} onChange={event => setSection(event.target.value)} /></label></div>
        <button className="secondary mt-2" onClick={() => { setDate('2030-01-07'); setDay('MONDAY'); setScanResults(null) }}>Load seeded leave date</button>
        {canManageTeachers && <button className="mt-3 ml-2" onClick={() => void scan()}>Run allocation scan</button>}
        <ul className="mt-5 space-y-2">{(scanResults ?? allocationQuery.data ?? []).map(allocation =>
          <li key={allocation.timetableEntryId} className="rounded bg-slate-100 p-3 text-sm">
            Period {allocation.periodNumber} · {allocation.className}-{allocation.section} · {allocation.subjectCode}<br />
            {nameOf(allocation.absentTeacherId)} → {nameOf(allocation.substituteTeacherId)} · {allocation.status}
          </li>)}</ul>
        <h3 className="mt-5 font-semibold">Daily class view</h3>
        <div className="mt-2 overflow-x-auto"><table><thead><tr><th>Period</th><th>Subject</th><th>Teacher</th><th>Status</th></tr></thead><tbody>
          {(dailyQuery.data ?? []).map(period => <tr key={period.periodNumber}><td>{period.periodNumber}</td>
            <td>{period.subjectCode ?? '-'}</td><td>{period.effectiveTeacherId === null ? '-' : nameOf(period.effectiveTeacherId)}</td>
            <td>{period.status}</td></tr>)}</tbody></table></div>
        {session.role === 'TEACHER' && <><h3 className="mt-5 font-semibold">My substitution notices</h3>
          <ul className="mt-2 space-y-2">{(noticesQuery.data ?? []).map(item =>
            <li key={item.id} className="rounded bg-blue-50 p-3 text-sm">{item.message} ({item.status})</li>)}</ul></>}
      </Section>
    </div>}

    {tab === 'Inventory' && canViewInventory && <div className="grid gap-5 lg:grid-cols-2">
      <Section title="Assets and stock">
        <div className="overflow-x-auto"><table><thead><tr><th>Item</th><th>Stock</th><th>Minimum</th><th>Status</th><th></th></tr></thead><tbody>
          {items.map(item => <tr key={item.id}><td>{item.name}<span className="block text-xs text-slate-500">{item.sku}</span></td>
            <td>{item.quantity}</td><td>{item.minimumQuantity}</td><td>{item.status}</td>
            <td>{canManageInventory && <button onClick={() => void act(client.patch(`/inventory/items/${item.id}/stock`, { delta: 1 }), 'Stock updated')}>+1</button>}</td></tr>)}</tbody></table></div>
        {canManageInventory && <form onSubmit={createItem} className="mt-5 grid gap-2 border-t pt-5">
          <h3 className="font-semibold">Add item</h3><input name="sku" placeholder="SKU" required /><input name="name" placeholder="Item name" required />
          <select name="category"><option>CONSUMABLE</option><option>LAB_EQUIPMENT</option><option>SPORTS_GEAR</option><option>IT_HARDWARE</option></select>
          <div className="grid grid-cols-3 gap-2"><input name="quantity" type="number" min="0" placeholder="Stock" required />
            <input name="minimum" type="number" min="1" placeholder="Minimum" required />
            <input name="reorder" type="number" min="1" placeholder="Reorder" required /></div><button>Add item</button>
        </form>}
      </Section>
      <Section title="Procurement">
        {canManageInventory && <label>Vendor<input value={vendorName} onChange={event => setVendorName(event.target.value)} /></label>}
        <h3 className="mt-4 font-semibold">Requisitions</h3>
        <ul className="mt-2 space-y-2">{(requisitionsQuery.data ?? []).map(req => <li key={req.id} className="rounded bg-slate-100 p-3 text-sm">
          #{req.id} · {items.find(item => item.id === req.itemId)?.name ?? req.itemId} · {req.quantity} units · {req.status}
          {canManageInventory && req.status === 'OPEN' && <button className="ml-3" onClick={() => void act(client.post(`/inventory/requisitions/${req.id}/orders`, { vendorName }), 'Vendor order placed')}>Order</button>}
        </li>)}</ul>
        <h3 className="mt-5 font-semibold">Vendor orders</h3>
        <ul className="mt-2 space-y-2">{(ordersQuery.data ?? []).map(order => <li key={order.id} className="rounded bg-slate-100 p-3 text-sm">
          #{order.id} · {order.vendorName} · {order.quantity} units · {order.status}
          {canManageInventory && order.status === 'PLACED' && <button className="ml-3" onClick={() => void act(client.patch(`/inventory/orders/${order.id}/receive`), 'Order received')}>Receive</button>}
        </li>)}</ul>
      </Section>
    </div>}
    {tab === 'Overview' && !canViewTeachers && !canViewInventory &&
      <p className="text-sm text-slate-500">Your role is ready for the next domain modules.</p>}
  </main>
}

export default function App() {
  const session = useSession(state => state.session)
  return session ? <Dashboard /> : <Login />
}
