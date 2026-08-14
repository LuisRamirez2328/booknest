import { useCallback, useEffect, useState } from 'react'
import { api } from '../api.js'

export default function MyLoans() {
  const [loans, setLoans] = useState([])
  const [message, setMessage] = useState('')

  const load = useCallback(async () => {
    try {
      setLoans(await api.myLoans())
    } catch (err) {
      setMessage(err.message)
    }
  }, [])

  useEffect(() => {
    load()
  }, [load])

  async function returnLoan(loan) {
    setMessage('')
    try {
      await api.returnLoan(loan.id)
      setMessage(`"${loan.bookTitle}" devuelto correctamente.`)
      load()
    } catch (err) {
      setMessage(err.message)
    }
  }

  const formatDate = (iso) => (iso ? new Date(iso + 'T00:00:00').toLocaleDateString('es-MX') : '—')

  return (
    <section>
      <h2>Mis préstamos</h2>
      {message && <p className="message">{message}</p>}

      {loans.length === 0 && <p className="empty">Aún no tienes préstamos activos.</p>}

      {loans.length > 0 && (
        <table className="table">
          <thead>
            <tr>
              <th>Libro</th>
              <th>Prestado</th>
              <th>Vence</th>
              <th>Estado</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {loans.map((l) => (
              <tr key={l.id}>
                <td>{l.bookTitle}</td>
                <td>{formatDate(l.loanDate)}</td>
                <td>{formatDate(l.dueDate)}</td>
                <td>{l.active ? <span className="badge">Prestado</span> : <span className="badge done">Devuelto</span>}</td>
                <td>
                  {l.active && (
                    <button onClick={() => returnLoan(l)}>Devolver</button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  )
}
