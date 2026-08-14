import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import App from './App.jsx'

vi.mock('./api.js', async (importOriginal) => {
  const actual = await importOriginal()
  return {
    ...actual,
    api: {
      books: vi.fn(),
      categories: vi.fn(),
      login: vi.fn(),
      register: vi.fn()
    }
  }
})

import { api } from './api.js'

beforeEach(() => {
  api.books.mockResolvedValue({ content: [], totalPages: 0, number: 0 })
  api.categories.mockResolvedValue([])
})

describe('App', () => {
  it('shows the login form when there is no session', () => {
    render(<App />)
    expect(screen.getByRole('heading', { name: /BookNest/ })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Entrar' })).toBeInTheDocument()
  })

  it('logs in and shows the catalog', async () => {
    api.login.mockResolvedValue({ token: 'tok', name: 'Ana', email: 'ana@b.dev', role: 'USER' })
    const user = userEvent.setup()
    render(<App />)

    await user.type(screen.getByLabelText('Email'), 'ana@b.dev')
    await user.type(screen.getByLabelText('Contraseña'), 'secret1')
    await user.click(screen.getByRole('button', { name: 'Entrar' }))

    await waitFor(() =>
      expect(api.login).toHaveBeenCalledWith(
        expect.objectContaining({ email: 'ana@b.dev', password: 'secret1' })
      )
    )
    expect(await screen.findByText(/Ana · USER/)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Catálogo' })).toHaveClass('active')
  })

  it('shows the admin nav only for ADMIN users', async () => {
    localStorage.setItem('bn_token', 'tok')
    localStorage.setItem('bn_name', 'Admin')
    localStorage.setItem('bn_email', 'admin@b.dev')
    localStorage.setItem('bn_role', 'ADMIN')
    render(<App />)

    expect(await screen.findByText(/Admin · ADMIN/)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Alta de libro' })).toBeInTheDocument()
  })

  it('hides the admin nav for regular users', async () => {
    localStorage.setItem('bn_token', 'tok')
    localStorage.setItem('bn_name', 'Ana')
    localStorage.setItem('bn_email', 'ana@b.dev')
    localStorage.setItem('bn_role', 'USER')
    render(<App />)

    await screen.findByText(/Ana · USER/)
    expect(screen.queryByRole('button', { name: 'Alta de libro' })).not.toBeInTheDocument()
  })

  it('logs out and returns to the login form', async () => {
    localStorage.setItem('bn_token', 'tok')
    localStorage.setItem('bn_name', 'Ana')
    localStorage.setItem('bn_email', 'ana@b.dev')
    localStorage.setItem('bn_role', 'USER')
    const user = userEvent.setup()
    render(<App />)
    await screen.findByText(/Ana · USER/)

    await user.click(screen.getByRole('button', { name: 'Salir' }))

    expect(await screen.findByRole('button', { name: 'Entrar' })).toBeInTheDocument()
    expect(localStorage.getItem('bn_token')).toBeNull()
  })
})
