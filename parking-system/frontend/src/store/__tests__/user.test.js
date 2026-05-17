import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useUserStore } from '../../store/user'
import apiClient from '../../utils/api'

vi.mock('../../utils/api', () => ({
  default: {
    post: vi.fn(),
    get: vi.fn(),
    put: vi.fn(),
    delete: vi.fn()
  }
}))

vi.mock('../../utils/websocket', () => ({
  default: {
    disconnect: vi.fn(),
    connect: vi.fn(),
    subscribe: vi.fn()
  }
}))

vi.mock('element-plus', () => ({
  ElMessage: {
    error: vi.fn(),
    success: vi.fn()
  }
}))

describe('useUserStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    vi.clearAllMocks()
  })

  describe('login', () => {
    it('正常登录', async () => {
      apiClient.post.mockResolvedValue({
        data: {
          code: 200,
          data: {
            user: { id: 1, username: 'testuser', role: 0 },
            token: 'fake-token'
          }
        }
      })

      const store = useUserStore()
      const result = await store.login('testuser', 'password123')

      expect(result).toBe(true)
      expect(store.user).toEqual({ id: 1, username: 'testuser', role: 0 })
      expect(store.token).toBe('fake-token')
      expect(store.isLoggedIn).toBe(true)
      expect(localStorage.getItem('token')).toBe('fake-token')
    })

    it('登录失败', async () => {
      apiClient.post.mockResolvedValue({
        data: {
          code: 400,
          message: '用户名或密码错误'
        }
      })

      const store = useUserStore()
      const result = await store.login('testuser', 'wrongpassword')

      expect(result).toBe(false)
      expect(store.user).toBeNull()
      expect(store.token).toBeNull()
      expect(store.isLoggedIn).toBe(false)
    })

    it('网络异常', async () => {
      apiClient.post.mockRejectedValue(new Error('Network Error'))

      const store = useUserStore()
      const result = await store.login('testuser', 'password123')

      expect(result).toBe(false)
    })
  })

  describe('register', () => {
    it('正常注册', async () => {
      apiClient.post.mockResolvedValue({
        data: { code: 200 }
      })

      const store = useUserStore()
      const result = await store.register({
        username: 'newuser',
        password: 'password123',
        name: 'New User'
      })

      expect(result).toBe(true)
    })

    it('注册失败', async () => {
      apiClient.post.mockResolvedValue({
        data: { code: 400, message: '用户名已存在' }
      })

      const store = useUserStore()
      const result = await store.register({
        username: 'existinguser',
        password: 'password123'
      })

      expect(result).toBe(false)
    })

    it('网络异常', async () => {
      apiClient.post.mockRejectedValue(new Error('Network Error'))

      const store = useUserStore()
      const result = await store.register({
        username: 'newuser',
        password: 'password123'
      })

      expect(result).toBe(false)
    })
  })

  describe('logout', () => {
    it('退出登录清除状态', async () => {
      apiClient.post.mockResolvedValue({
        data: {
          code: 200,
          data: {
            user: { id: 1, username: 'testuser', role: 0 },
            token: 'fake-token'
          }
        }
      })

      const store = useUserStore()
      await store.login('testuser', 'password123')

      expect(store.isLoggedIn).toBe(true)

      store.logout()

      expect(store.user).toBeNull()
      expect(store.token).toBeNull()
      expect(store.isLoggedIn).toBe(false)
      expect(localStorage.getItem('token')).toBeNull()
      expect(localStorage.getItem('user')).toBeNull()
    })
  })

  describe('isAdmin', () => {
    it('管理员用户', async () => {
      apiClient.post.mockResolvedValue({
        data: {
          code: 200,
          data: {
            user: { id: 1, username: 'admin', role: 1 },
            token: 'admin-token'
          }
        }
      })

      const store = useUserStore()
      await store.login('admin', 'password123')

      expect(store.isAdmin).toBe(true)
    })

    it('普通用户', async () => {
      apiClient.post.mockResolvedValue({
        data: {
          code: 200,
          data: {
            user: { id: 2, username: 'testuser', role: 0 },
            token: 'user-token'
          }
        }
      })

      const store = useUserStore()
      await store.login('testuser', 'password123')

      expect(store.isAdmin).toBe(false)
    })

    it('未登录用户', () => {
      const store = useUserStore()
      expect(store.isAdmin).toBeFalsy()
    })
  })

  describe('changePassword', () => {
    it('修改密码成功', async () => {
      apiClient.post.mockResolvedValue({
        data: {
          code: 200,
          data: {
            user: { id: 1, username: 'testuser', role: 0 },
            token: 'fake-token'
          }
        }
      })

      const store = useUserStore()
      await store.login('testuser', 'password123')

      apiClient.put.mockResolvedValue({
        data: { code: 200 }
      })

      const result = await store.changePassword('oldpass', 'newpass')

      expect(result).toBe(true)
    })

    it('修改密码失败', async () => {
      apiClient.post.mockResolvedValue({
        data: {
          code: 200,
          data: {
            user: { id: 1, username: 'testuser', role: 0 },
            token: 'fake-token'
          }
        }
      })

      const store = useUserStore()
      await store.login('testuser', 'password123')

      apiClient.put.mockResolvedValue({
        data: { code: 400, message: '旧密码错误' }
      })

      const result = await store.changePassword('wrongoldpass', 'newpass')

      expect(result).toBe(false)
    })

    it('未登录时修改密码', async () => {
      const store = useUserStore()
      const result = await store.changePassword('oldpass', 'newpass')

      expect(result).toBe(false)
    })

    it('网络异常', async () => {
      apiClient.post.mockResolvedValue({
        data: {
          code: 200,
          data: {
            user: { id: 1, username: 'testuser', role: 0 },
            token: 'fake-token'
          }
        }
      })

      const store = useUserStore()
      await store.login('testuser', 'password123')

      apiClient.put.mockRejectedValue(new Error('Network Error'))

      const result = await store.changePassword('oldpass', 'newpass')

      expect(result).toBe(false)
    })
  })
})
