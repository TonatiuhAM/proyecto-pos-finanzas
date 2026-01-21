import { describe, it, expect } from 'vitest'

describe('Setup de Testing', () => {
  it('debería ejecutar tests correctamente', () => {
    expect(true).toBe(true)
  })

  it('debería realizar operaciones matemáticas básicas', () => {
    expect(2 + 2).toBe(4)
    expect(10 - 5).toBe(5)
  })
})
