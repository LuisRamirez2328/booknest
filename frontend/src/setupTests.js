import '@testing-library/jest-dom/vitest'
import { vi } from 'vitest'

afterEach(() => {
  vi.clearAllMocks()
  localStorage.clear()
})
