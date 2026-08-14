import { useEffect, useState } from 'react'
import { api } from '../api.js'

export default function AdminBookForm() {
  const [authors, setAuthors] = useState([])
  const [categories, setCategories] = useState([])
  const [form, setForm] = useState({
    title: '',
    isbn: '',
    publishedYear: '',
    authorId: '',
    categoryIds: [],
    totalCopies: 1
  })
  const [message, setMessage] = useState('')

  useEffect(() => {
    api.authors().then(setAuthors).catch(() => {})
    api.categories().then(setCategories).catch(() => {})
  }, [])

  function toggleCategory(id) {
    setForm((f) => ({
      ...f,
      categoryIds: f.categoryIds.includes(id)
        ? f.categoryIds.filter((c) => c !== id)
        : [...f.categoryIds, id]
    }))
  }

  async function submit(e) {
    e.preventDefault()
    setMessage('')
    try {
      await api.createBook({
        title: form.title,
        isbn: form.isbn,
        publishedYear: form.publishedYear ? Number(form.publishedYear) : null,
        authorId: Number(form.authorId),
        categoryIds: form.categoryIds.map(Number),
        totalCopies: Number(form.totalCopies)
      })
      setMessage(`Libro "${form.title}" creado.`)
      setForm({ title: '', isbn: '', publishedYear: '', authorId: '', categoryIds: [], totalCopies: 1 })
    } catch (err) {
      setMessage(err.message)
    }
  }

  return (
    <section>
      <h2>Alta de libro</h2>
      {message && <p className="message">{message}</p>}

      <form className="form" onSubmit={submit}>
        <label>
          Título
          <input value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} required />
        </label>
        <label>
          ISBN
          <input value={form.isbn} onChange={(e) => setForm({ ...form, isbn: e.target.value })} required />
        </label>
        <label>
          Año de publicación
          <input
            type="number"
            value={form.publishedYear}
            onChange={(e) => setForm({ ...form, publishedYear: e.target.value })}
          />
        </label>
        <label>
          Autor
          <select value={form.authorId} onChange={(e) => setForm({ ...form, authorId: e.target.value })} required>
            <option value="">Selecciona…</option>
            {authors.map((a) => (
              <option key={a.id} value={a.id}>
                {a.name}
              </option>
            ))}
          </select>
        </label>
        <fieldset>
          <legend>Categorías</legend>
          {categories.map((c) => (
            <label key={c.id} className="chip">
              <input
                type="checkbox"
                checked={form.categoryIds.includes(c.id)}
                onChange={() => toggleCategory(c.id)}
              />
              {c.name}
            </label>
          ))}
        </fieldset>
        <label>
          Ejemplares
          <input
            type="number"
            min="1"
            value={form.totalCopies}
            onChange={(e) => setForm({ ...form, totalCopies: e.target.value })}
            required
          />
        </label>
        <button type="submit">Crear libro</button>
      </form>
    </section>
  )
}
