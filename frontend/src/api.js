const API = '/api'

export function getToken() {
  return localStorage.getItem('bn_token')
}

export function getSession() {
  return {
    token: localStorage.getItem('bn_token') || '',
    name: localStorage.getItem('bn_name') || '',
    email: localStorage.getItem('bn_email') || '',
    role: localStorage.getItem('bn_role') || ''
  }
}

export function setSession(data) {
  localStorage.setItem('bn_token', data.token)
  localStorage.setItem('bn_name', data.name)
  localStorage.setItem('bn_email', data.email)
  localStorage.setItem('bn_role', data.role)
}

export function clearSession() {
  ;['bn_token', 'bn_name', 'bn_email', 'bn_role'].forEach((k) => localStorage.removeItem(k))
}

async function request(path, { method = 'GET', body } = {}) {
  const headers = { 'Content-Type': 'application/json' }
  const token = getToken()
  if (token) headers.Authorization = `Bearer ${token}`

  const res = await fetch(API + path, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined
  })

  if (res.status === 401) {
    clearSession()
    throw new Error('Sesión inválida o expirada')
  }

  if (res.status === 204) return null

  const data = await res.json().catch(() => null)

  if (!res.ok) {
    let msg = data?.message || 'Error en la petición'
    if (data?.fieldErrors) {
      msg = Object.values(data.fieldErrors).join(', ')
    }
    throw new Error(msg)
  }

  return data
}

export const api = {
  register: (payload) => request('/auth/register', { method: 'POST', body: payload }),
  login: (payload) => request('/auth/login', { method: 'POST', body: payload }),
  books: (params = {}) => {
    const qs = new URLSearchParams(
      Object.entries(params).filter(([, v]) => v !== undefined && v !== null && v !== '')
    ).toString()
    return request('/books' + (qs ? `?${qs}` : ''))
  },
  createBook: (payload) => request('/books', { method: 'POST', body: payload }),
  authors: () => request('/authors'),
  categories: () => request('/categories'),
  borrow: (bookId) => request('/loans', { method: 'POST', body: { bookId } }),
  returnLoan: (id) => request(`/loans/${id}/return`, { method: 'PUT' }),
  myLoans: () => request('/loans/me')
}
