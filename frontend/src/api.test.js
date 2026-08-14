import { describe, it, expect, vi, beforeEach } from 'vitest'
import { api, getSession, getToken, setSession, clearSession } from './api.js'

const jsonOk = (body, status = 200) =>
  Promise.resolve({ ok: status < 400, status, json: () => Promise.resolve(body) })

beforeEach(() => {
  vi.stubGlobal('fetch', vi.fn())
})

describe('session', () => {
  it('stores and retrieves session data', () => {
    setSession({ token: 'tok', name: 'Ana', email: 'a@b.dev', role: 'ADMIN' })
    expect(getToken()).toBe('tok')
    expect(getSession()).toEqual({ token: 'tok', name: 'Ana', email: 'a@b.dev', role: 'ADMIN' })
  })

  it('clears all session keys', () => {
    setSession({ token: 'tok', name: 'Ana', email: 'a@b.dev', role: 'ADMIN' })
    clearSession()
    expect(getToken()).toBeNull()
    expect(getSession()).toEqual({ token: '', name: '', email: '', role: '' })
  })
})

describe('api.login', () => {
  it('POSTs credentials and returns data', async () => {
    fetch.mockReturnValue(
      jsonOk({ token: 'tok', name: 'Ana', email: 'a@b.dev', role: 'USER' })
    )
    const data = await api.login({ email: 'a@b.dev', password: 'secret1' })
    expect(fetch).toHaveBeenCalledWith(
      '/api/auth/login',
      expect.objectContaining({
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: 'a@b.dev', password: 'secret1' })
      })
    )
    expect(data.token).toBe('tok')
  })
})

describe('api.books', () => {
  it('builds a query string with non-empty params', async () => {
    fetch.mockReturnValue(jsonOk({ content: [], totalPages: 0, number: 0 }))
    await api.books({ q: 'harry', categoryId: 2, page: 0, size: 12 })
    expect(fetch).toHaveBeenCalledWith(
      '/api/books?q=harry&categoryId=2&page=0&size=12',
      expect.anything()
    )
  })

  it('omits empty params from the query string', async () => {
    fetch.mockReturnValue(jsonOk({ content: [], totalPages: 0, number: 0 }))
    await api.books({ q: '', categoryId: undefined, page: 0, size: 12 })
    expect(fetch).toHaveBeenCalledWith('/api/books?page=0&size=12', expect.anything())
  })
})

describe('api error handling', () => {
  it('sends the bearer token when a session exists', async () => {
    localStorage.setItem('bn_token', 'tok')
    fetch.mockReturnValue(jsonOk([]))
    await api.books()
    const [, init] = fetch.mock.calls[0]
    expect(init.headers.Authorization).toBe('Bearer tok')
  })

  it('clears session and throws on 401', async () => {
    localStorage.setItem('bn_token', 'tok')
    fetch.mockReturnValue(jsonOk({ message: 'no' }, 401))
    await expect(api.myLoans()).rejects.toThrow('Sesión inválida o expirada')
    expect(getToken()).toBeNull()
  })

  it('returns null on 204', async () => {
    fetch.mockReturnValue(
      Promise.resolve({ ok: true, status: 204, json: () => Promise.resolve(null) })
    )
    await expect(api.returnLoan(5)).resolves.toBeNull()
  })

  it('joins fieldErrors into the message', async () => {
    fetch.mockReturnValue(
      jsonOk(
        { message: 'Validation failed', fieldErrors: { title: 'Requerido', isbn: 'Inválido' } },
        400
      )
    )
    await expect(api.createBook({})).rejects.toThrow('Requerido, Inválido')
  })

  it('falls back to a default message', async () => {
    fetch.mockReturnValue(jsonOk({}, 500))
    await expect(api.books()).rejects.toThrow('Error en la petición')
  })
})
