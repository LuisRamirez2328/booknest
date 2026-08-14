import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import AdminBookForm from './AdminBookForm.jsx'

vi.mock('../api.js', () => ({
  api: {
    authors: vi.fn(),
    categories: vi.fn(),
    createBook: vi.fn()
  }
}))

import { api } from '../api.js'

beforeEach(() => {
  api.authors.mockResolvedValue([
    { id: 1, name: 'Gabriel García Márquez' },
    { id: 2, name: 'Julio Cortázar' }
  ])
  api.categories.mockResolvedValue([
    { id: 1, name: 'Novela' },
    { id: 2, name: 'Fantasía' }
  ])
})

describe('AdminBookForm', () => {
  it('renders the authors and categories from the API', async () => {
    render(<AdminBookForm />)
    expect(await screen.findByText('Gabriel García Márquez')).toBeInTheDocument()
    expect(screen.getByText('Julio Cortázar')).toBeInTheDocument()
    expect(screen.getByText('Novela')).toBeInTheDocument()
    expect(screen.getByText('Fantasía')).toBeInTheDocument()
  })

  it('creates a book with the submitted payload', async () => {
    api.createBook.mockResolvedValue(null)
    const user = userEvent.setup()
    render(<AdminBookForm />)
    await screen.findByText('Gabriel García Márquez')

    await user.type(screen.getByLabelText('Título'), 'El Aleph')
    await user.type(screen.getByLabelText('ISBN'), '9789500426017')
    await user.type(screen.getByLabelText('Año de publicación'), '1949')
    await user.selectOptions(screen.getByLabelText('Autor'), '2')
    await user.click(screen.getByLabelText('Fantasía'))
    await user.clear(screen.getByLabelText('Ejemplares'))
    await user.type(screen.getByLabelText('Ejemplares'), '4')
    await user.click(screen.getByRole('button', { name: 'Crear libro' }))

    await waitFor(() =>
      expect(api.createBook).toHaveBeenCalledWith({
        title: 'El Aleph',
        isbn: '9789500426017',
        publishedYear: 1949,
        authorId: 2,
        categoryIds: [2],
        totalCopies: 4
      })
    )
    expect(
      await screen.findByText('Libro "El Aleph" creado.')
    ).toBeInTheDocument()
  })

  it('resets the form after a successful create', async () => {
    api.createBook.mockResolvedValue(null)
    const user = userEvent.setup()
    render(<AdminBookForm />)
    await screen.findByText('Gabriel García Márquez')

    await user.type(screen.getByLabelText('Título'), 'El Aleph')
    await user.type(screen.getByLabelText('ISBN'), '9789500426017')
    await user.selectOptions(screen.getByLabelText('Autor'), '1')
    await user.click(screen.getByLabelText('Fantasía'))
    await user.click(screen.getByRole('button', { name: 'Crear libro' }))
    await screen.findByText('Libro "El Aleph" creado.')

    expect(screen.getByLabelText('Título')).toHaveValue('')
    expect(screen.getByLabelText('ISBN')).toHaveValue('')
    expect(screen.getByLabelText('Autor')).toHaveValue('')
    expect(screen.getByRole('checkbox', { name: 'Fantasía' })).not.toBeChecked()
  })

  it('shows the API error message when create fails', async () => {
    api.createBook.mockRejectedValue(new Error('ISBN ya registrado'))
    const user = userEvent.setup()
    render(<AdminBookForm />)
    await screen.findByText('Gabriel García Márquez')

    await user.type(screen.getByLabelText('Título'), 'El Aleph')
    await user.type(screen.getByLabelText('ISBN'), '9789500426017')
    await user.selectOptions(screen.getByLabelText('Autor'), '1')
    await user.click(screen.getByRole('button', { name: 'Crear libro' }))

    expect(await screen.findByText('ISBN ya registrado')).toBeInTheDocument()
  })
})
