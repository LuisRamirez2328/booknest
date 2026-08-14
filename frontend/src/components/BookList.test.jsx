import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import BookList from './BookList.jsx'

vi.mock('../api.js', () => ({
  api: {
    books: vi.fn(),
    categories: vi.fn(),
    borrow: vi.fn()
  }
}))

import { api } from '../api.js'

const booksPage = {
  content: [
    {
      id: 1,
      title: 'Cien años de soledad',
      authorName: 'Gabriel García Márquez',
      publishedYear: 1967,
      categories: ['Novela'],
      totalCopies: 3,
      availableCopies: 2
    },
    {
      id: 2,
      title: 'El Principito',
      authorName: 'Antoine de Saint-Exupéry',
      publishedYear: 1943,
      categories: ['Infantil'],
      totalCopies: 1,
      availableCopies: 0
    }
  ],
  totalPages: 2,
  number: 0
}

beforeEach(() => {
  api.books.mockResolvedValue(booksPage)
  api.categories.mockResolvedValue([
    { id: 1, name: 'Novela' },
    { id: 2, name: 'Infantil' }
  ])
})

describe('BookList', () => {
  it('renders books from the API', async () => {
    render(<BookList />)
    expect(await screen.findByText('Cien años de soledad')).toBeInTheDocument()
    expect(screen.getByText('El Principito')).toBeInTheDocument()
    expect(screen.getByText('Gabriel García Márquez')).toBeInTheDocument()
    expect(screen.getByText(/2 de 3 ejemplares disponibles/)).toBeInTheDocument()
  })

  it('shows an empty message when there are no books', async () => {
    api.books.mockResolvedValue({ content: [], totalPages: 0, number: 0 })
    render(<BookList />)
    expect(await screen.findByText('No hay libros que coincidan.')).toBeInTheDocument()
  })

  it('disables the borrow button when no copies are available', async () => {
    render(<BookList />)
    await screen.findByText('Cien años de soledad')
    expect(screen.getByRole('button', { name: 'Sin ejemplares' })).toBeDisabled()
    expect(screen.getByRole('button', { name: 'Tomar prestado' })).toBeEnabled()
  })

  it('borrows a book and reloads the list', async () => {
    api.borrow.mockResolvedValue(null)
    const user = userEvent.setup()
    render(<BookList />)
    await screen.findByText('Cien años de soledad')

    await user.click(screen.getByRole('button', { name: 'Tomar prestado' }))

    await waitFor(() => expect(api.borrow).toHaveBeenCalledWith(1))
    expect(api.books).toHaveBeenCalledTimes(2)
    expect(
      await screen.findByText('"Cien años de soledad" prestado. ¡Que lo disfrutes!')
    ).toBeInTheDocument()
  })

  it('shows the API error message when borrowing fails', async () => {
    api.borrow.mockRejectedValue(new Error('Sin ejemplares disponibles'))
    const user = userEvent.setup()
    render(<BookList />)
    await screen.findByText('Cien años de soledad')

    await user.click(screen.getByRole('button', { name: 'Tomar prestado' }))

    expect(await screen.findByText('Sin ejemplares disponibles')).toBeInTheDocument()
  })

  it('filters by search query', async () => {
    const user = userEvent.setup()
    render(<BookList />)
    await screen.findByText('Cien años de soledad')

    await user.type(screen.getByPlaceholderText('Buscar por título o autor…'), 'principito')

    await waitFor(() =>
      expect(api.books).toHaveBeenLastCalledWith({ q: 'principito', page: 0, size: 12 })
    )
  })
})
