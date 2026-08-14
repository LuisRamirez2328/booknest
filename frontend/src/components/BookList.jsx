import { useCallback, useEffect, useState } from 'react'
import { api } from '../api.js'

export default function BookList() {
  const [books, setBooks] = useState([])
  const [categories, setCategories] = useState([])
  const [query, setQuery] = useState('')
  const [categoryId, setCategoryId] = useState('')
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [loading, setLoading] = useState(false)
  const [message, setMessage] = useState('')

  const load = useCallback(async (q, cat, pg) => {
    setLoading(true)
    try {
      const data = await api.books({ q, categoryId: cat, page: pg, size: 12 })
      setBooks(data.content)
      setTotalPages(data.totalPages)
      setPage(data.number)
    } catch (err) {
      setMessage(err.message)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    load(query, categoryId || undefined, 0)
  }, [load, query, categoryId])

  useEffect(() => {
    api.categories().then(setCategories).catch(() => {})
  }, [])

  async function borrow(book) {
    setMessage('')
    try {
      await api.borrow(book.id)
      setMessage(`"${book.title}" prestado. ¡Que lo disfrutes!`)
      load(query, categoryId || undefined, page)
    } catch (err) {
      setMessage(err.message)
    }
  }

  return (
    <section>
      <div className="toolbar">
        <input
          placeholder="Buscar por título o autor…"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
        <select value={categoryId} onChange={(e) => setCategoryId(e.target.value)}>
          <option value="">Todas las categorías</option>
          {categories.map((c) => (
            <option key={c.id} value={c.id}>
              {c.name}
            </option>
          ))}
        </select>
      </div>

      {message && <p className="message">{message}</p>}
      {loading && <p className="message">Cargando…</p>}

      <div className="grid">
        {books.map((b) => (
          <article className="card" key={b.id}>
            <div className="card-title">{b.title}</div>
            <div className="card-author">{b.authorName}</div>
            <div className="card-meta">
              {b.publishedYear && <span>{b.publishedYear}</span>}
              {b.categories?.length > 0 && <span>{b.categories.join(', ')}</span>}
            </div>
            <div className="card-copies">
              {b.availableCopies} de {b.totalCopies} ejemplares disponibles
            </div>
            <button onClick={() => borrow(b)} disabled={b.availableCopies < 1}>
              {b.availableCopies < 1 ? 'Sin ejemplares' : 'Tomar prestado'}
            </button>
          </article>
        ))}
        {!loading && books.length === 0 && <p className="empty">No hay libros que coincidan.</p>}
      </div>

      {totalPages > 1 && (
        <div className="pager">
          <button disabled={page === 0} onClick={() => load(query, categoryId || undefined, page - 1)}>
            ← Anterior
          </button>
          <span>
            Página {page + 1} de {totalPages}
          </span>
          <button
            disabled={page + 1 >= totalPages}
            onClick={() => load(query, categoryId || undefined, page + 1)}
          >
            Siguiente →
          </button>
        </div>
      )}
    </section>
  )
}
