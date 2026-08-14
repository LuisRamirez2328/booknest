import { useEffect, useState } from 'react'
import { api, getSession, setSession, clearSession } from './api.js'
import BookList from './components/BookList.jsx'
import MyLoans from './components/MyLoans.jsx'
import AdminBookForm from './components/AdminBookForm.jsx'

export default function App() {
  const [session, setSessionState] = useState(() => getSession())
  const [view, setView] = useState('catalog')

  useEffect(() => {
    if (!session.token && view !== 'auth') setView('auth')
  }, [session, view])

  function handleAuth(data) {
    setSession(data)
    setSessionState(getSession())
    setView('catalog')
  }

  function logout() {
    clearSession()
    setSessionState({ name: '', email: '', role: '' })
    setView('auth')
  }

  if (!session.token) {
    return <Auth onAuth={handleAuth} />
  }

  return (
    <div className="app">
      <header className="topbar">
        <div className="brand">📚 BookNest</div>
        <nav>
          <button className={view === 'catalog' ? 'active' : ''} onClick={() => setView('catalog')}>
            Catálogo
          </button>
          <button className={view === 'loans' ? 'active' : ''} onClick={() => setView('loans')}>
            Mis préstamos
          </button>
          {session.role === 'ADMIN' && (
            <button className={view === 'admin' ? 'active' : ''} onClick={() => setView('admin')}>
              Alta de libro
            </button>
          )}
        </nav>
        <div className="user">
          <span>
            {session.name} · {session.role}
          </span>
          <button onClick={logout}>Salir</button>
        </div>
      </header>

      <main>
        {view === 'catalog' && <BookList />}
        {view === 'loans' && <MyLoans />}
        {view === 'admin' && session.role === 'ADMIN' && <AdminBookForm />}
      </main>
    </div>
  )
}

function Auth({ onAuth }) {
  const [mode, setMode] = useState('login')
  const [form, setForm] = useState({ name: '', email: '', password: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function submit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const data =
        mode === 'login' ? await api.login(form) : await api.register(form)
      onAuth(data)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-wrap">
      <form className="auth-card" onSubmit={submit}>
        <h1>📚 BookNest</h1>
        <p className="subtitle">
          {mode === 'login' ? 'Inicia sesión en tu biblioteca' : 'Crea una cuenta para empezar'}
        </p>

        {mode === 'register' && (
          <label>
            Nombre
            <input
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
              required
            />
          </label>
        )}

        <label>
          Email
          <input
            type="email"
            value={form.email}
            onChange={(e) => setForm({ ...form, email: e.target.value })}
            required
          />
        </label>

        <label>
          Contraseña
          <input
            type="password"
            minLength={6}
            value={form.password}
            onChange={(e) => setForm({ ...form, password: e.target.value })}
            required
          />
        </label>

        {error && <p className="error">{error}</p>}

        <button type="submit" disabled={loading}>
          {loading ? 'Espera…' : mode === 'login' ? 'Entrar' : 'Registrarme'}
        </button>

        <p className="switch">
          {mode === 'login' ? '¿No tienes cuenta?' : '¿Ya tienes cuenta?'}{' '}
          <a
            href="#"
            onClick={(e) => {
              e.preventDefault()
              setMode(mode === 'login' ? 'register' : 'login')
              setError('')
            }}
          >
            {mode === 'login' ? 'Regístrate' : 'Inicia sesión'}
          </a>
        </p>
        <p className="hint">Demo: admin@booknest.dev / admin123 · user@booknest.dev / user123</p>
      </form>
    </div>
  )
}
