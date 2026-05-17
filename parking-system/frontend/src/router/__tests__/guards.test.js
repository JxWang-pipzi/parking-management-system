import { describe, it, expect, beforeEach, vi } from 'vitest'

const { guardFn } = vi.hoisted(() => ({
  guardFn: { value: null }
}))

vi.mock('vue-router', async () => {
  const actual = await vi.importActual('vue-router')
  const originalCreateRouter = actual.createRouter
  const createRouter = (options) => {
    const router = originalCreateRouter({
      ...options,
      history: actual.createMemoryHistory()
    })
    const originalBeforeEach = router.beforeEach.bind(router)
    router.beforeEach = (guard) => {
      guardFn.value = guard
      return originalBeforeEach(guard)
    }
    return router
  }
  return {
    ...actual,
    createWebHistory: () => actual.createMemoryHistory(),
    createRouter
  }
})

import router from '../../router'

function createJWT(exp) {
  const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }))
  const payload = btoa(JSON.stringify({ exp }))
  return `${header}.${payload}.fakesignature`
}

describe('Router Guards', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('requiresAuth: 无token跳转登录', () => {
    localStorage.removeItem('token')
    const to = {
      matched: [{ meta: { requiresAuth: true } }],
      fullPath: '/orders'
    }
    const from = {}
    const next = vi.fn()
    guardFn.value(to, from, next)
    expect(next).toHaveBeenCalledWith({
      path: '/login',
      query: { redirect: '/orders' }
    })
  })

  it('requiresAuth: 有效token允许访问', () => {
    const token = createJWT(Math.floor(Date.now() / 1000) + 3600)
    localStorage.setItem('token', token)
    const to = {
      matched: [{ meta: { requiresAuth: true } }],
      fullPath: '/orders'
    }
    const from = {}
    const next = vi.fn()
    guardFn.value(to, from, next)
    expect(next).toHaveBeenCalledWith()
  })

  it('requiresAuth: 过期token跳转登录', () => {
    const token = createJWT(Math.floor(Date.now() / 1000) - 3600)
    localStorage.setItem('token', token)
    const to = {
      matched: [{ meta: { requiresAuth: true } }],
      fullPath: '/orders'
    }
    const from = {}
    const next = vi.fn()
    guardFn.value(to, from, next)
    expect(next).toHaveBeenCalledWith({
      path: '/login',
      query: { redirect: '/orders' }
    })
  })

  it('requiresAdmin: 非管理员跳转首页', () => {
    const token = createJWT(Math.floor(Date.now() / 1000) + 3600)
    localStorage.setItem('token', token)
    localStorage.setItem('user', JSON.stringify({ role: 0 }))
    const to = {
      matched: [{ meta: { requiresAuth: true, requiresAdmin: true } }],
      fullPath: '/admin'
    }
    const from = {}
    const next = vi.fn()
    guardFn.value(to, from, next)
    expect(next).toHaveBeenCalledWith('/')
  })

  it('requiresAdmin: 管理员允许访问', () => {
    const token = createJWT(Math.floor(Date.now() / 1000) + 3600)
    localStorage.setItem('token', token)
    localStorage.setItem('user', JSON.stringify({ role: 1 }))
    const to = {
      matched: [{ meta: { requiresAuth: true, requiresAdmin: true } }],
      fullPath: '/admin'
    }
    const from = {}
    const next = vi.fn()
    guardFn.value(to, from, next)
    expect(next).toHaveBeenCalledWith()
  })

  it('requiresGuest: 已登录用户不能访问登录页', () => {
    const token = createJWT(Math.floor(Date.now() / 1000) + 3600)
    localStorage.setItem('token', token)
    const to = {
      matched: [{ meta: { requiresGuest: true } }],
      fullPath: '/login'
    }
    const from = {}
    const next = vi.fn()
    guardFn.value(to, from, next)
    expect(next).toHaveBeenCalledWith('/')
  })

  it('requiresGuest: 未登录用户可以访问登录页', () => {
    localStorage.removeItem('token')
    const to = {
      matched: [{ meta: { requiresGuest: true } }],
      fullPath: '/login'
    }
    const from = {}
    const next = vi.fn()
    guardFn.value(to, from, next)
    expect(next).toHaveBeenCalledWith()
  })

  it('无特殊要求的路由直接放行', () => {
    const to = {
      matched: [{ meta: {} }],
      fullPath: '/'
    }
    const from = {}
    const next = vi.fn()
    guardFn.value(to, from, next)
    expect(next).toHaveBeenCalledWith()
  })
})
