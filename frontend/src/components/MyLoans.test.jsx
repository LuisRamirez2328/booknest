import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import MyLoans from './MyLoans.jsx'

vi.mock('../api.js', () => ({
  api: {
    myLoans: vi.fn(),
    returnLoan: vi.fn()
  }
}))

import { api } from '../api.js'

const loans = [
  {
    id: 10,
    bookTitle: 'Cien años de soledad',
    loanDate: '2026-08-01',
    dueDate: '2026-08-15',
    active: true
  },
  {
    id: 11,
    bookTitle: 'Rayuela',
    loanDate: '2026-07-01',
    dueDate: '2026-07-15',
    active: false
  }
]

beforeEach(() => {
  api.myLoans.mockResolvedValue(loans)
})

describe('MyLoans', () => {
  it('renders active and returned loans', async () => {
    render(<MyLoans />)
    expect(await screen.findByText('Cien años de soledad')).toBeInTheDocument()
    expect(screen.getByText('Rayuela')).toBeInTheDocument()
    expect(screen.getByText('Prestado', { selector: '.badge' })).toBeInTheDocument()
    expect(screen.getByText('Devuelto', { selector: '.badge' })).toBeInTheDocument()
  })

  it('shows an empty message when there are no loans', async () => {
    api.myLoans.mockResolvedValue([])
    render(<MyLoans />)
    expect(
      await screen.findByText('Aún no tienes préstamos activos.')
    ).toBeInTheDocument()
  })

  it('only shows the return button for active loans', async () => {
    render(<MyLoans />)
    await screen.findByText('Cien años de soledad')
    const buttons = screen.getAllByRole('button', { name: 'Devolver' })
    expect(buttons).toHaveLength(1)
  })

  it('returns a loan and reloads the list', async () => {
    api.returnLoan.mockResolvedValue(null)
    const user = userEvent.setup()
    render(<MyLoans />)
    await screen.findByText('Cien años de soledad')

    await user.click(screen.getByRole('button', { name: 'Devolver' }))

    await waitFor(() => expect(api.returnLoan).toHaveBeenCalledWith(10))
    expect(api.myLoans).toHaveBeenCalledTimes(2)
    expect(
      await screen.findByText('"Cien años de soledad" devuelto correctamente.')
    ).toBeInTheDocument()
  })

  it('shows the API error message when the return fails', async () => {
    api.returnLoan.mockRejectedValue(new Error('El préstamo ya no está activo'))
    const user = userEvent.setup()
    render(<MyLoans />)
    await screen.findByText('Cien años de soledad')

    await user.click(screen.getByRole('button', { name: 'Devolver' }))

    expect(
      await screen.findByText('El préstamo ya no está activo')
    ).toBeInTheDocument()
  })
})
