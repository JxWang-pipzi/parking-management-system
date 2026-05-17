import { defineStore } from 'pinia'
import apiClient from '../utils/api'
import wsManager from '../utils/websocket'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', {
  state: () => ({
    user: JSON.parse(localStorage.getItem('user')) || null,
    token: localStorage.getItem('token') || null,
    isLoggedIn: !!localStorage.getItem('token'),
    users: []
  }),
  getters: {
    currentUser: (state) => state.user,
    isAdmin: (state) => state.user && state.user.role === 1
  },
  actions: {
    async login(username, password) {
      try {
        const response = await apiClient.post('/users/login', { username, password })
        if (response.data.code === 200) {
          const result = response.data.data
          this.user = result.user
          this.token = result.token
          this.isLoggedIn = true
          localStorage.setItem('token', this.token)
          localStorage.setItem('user', JSON.stringify(this.user))
          return true
        } else {
          ElMessage.error(response.data.message || '登录失败')
          return false
        }
      } catch (error) {
        console.error('Login error:', error)
        ElMessage.error('登录失败，请检查后端服务')
        return false
      }
    },
    async wechatLogin() {
      try {
        const username = 'wx_test_user'
        const password = 'wx_test_123456'
        const loginResponse = await apiClient.post('/users/login', { username, password })
        if (loginResponse.data.code === 200) {
          const result = loginResponse.data.data
          this.user = result.user
          this.user.wechatNickname = '微信测试用户'
          this.token = result.token
          this.isLoggedIn = true
          localStorage.setItem('token', this.token)
          localStorage.setItem('user', JSON.stringify(this.user))
          return true
        }
        const registerResponse = await apiClient.post('/users/register', {
          username,
          password,
          name: '微信测试用户',
          phone: '',
          email: ''
        })
        if (registerResponse.data.code === 200) {
          const retryLogin = await apiClient.post('/users/login', { username, password })
          if (retryLogin.data.code === 200) {
            const result = retryLogin.data.data
            this.user = result.user
            this.user.wechatNickname = '微信测试用户'
            this.token = result.token
            this.isLoggedIn = true
            localStorage.setItem('token', this.token)
            localStorage.setItem('user', JSON.stringify(this.user))
            return true
          }
        }
        ElMessage.error('微信登录失败')
        return false
      } catch (error) {
        console.error('WeChat login error:', error)
        ElMessage.error('微信登录失败')
        return false
      }
    },
    async register(userData) {
      try {
        const response = await apiClient.post('/users/register', userData)
        return response.data.code === 200
      } catch (error) {
        console.error('Register error:', error)
        return false
      }
    },
    async getUsers() {
      try {
        const response = await apiClient.get('/users')
        if (response.data.code === 200) {
          this.users = Array.isArray(response.data.data) ? response.data.data : []
        }
      } catch (error) {
        console.error('获取用户列表失败:', error)
        ElMessage.error('获取用户列表失败')
        this.users = []
      }
    },
    async addUser(userData) {
      try {
        const response = await apiClient.post('/users', userData)
        if (response.data.code === 200) {
          this.users.push(response.data.data)
          return true
        }
      } catch (error) {
        console.error('添加用户失败:', error)
        ElMessage.error('添加用户失败')
      }
      return false
    },
    async updateUser(id, userData) {
      try {
        const response = await apiClient.put(`/users/${id}`, userData)
        if (response.data.code === 200) {
          const index = this.users.findIndex(u => u.id === id)
          if (index !== -1) {
            this.users[index] = response.data.data
          }
          return true
        }
      } catch (error) {
        console.error('更新用户失败:', error)
        ElMessage.error('更新用户失败')
      }
      return false
    },
    async deleteUser(id) {
      try {
        const response = await apiClient.delete(`/users/${id}`)
        if (response.data.code === 200) {
          const index = this.users.findIndex(u => u.id === id)
          if (index !== -1) {
            this.users.splice(index, 1)
          }
          return true
        }
      } catch (error) {
        console.error('删除用户失败:', error)
        ElMessage.error('删除用户失败')
      }
      return false
    },
    async getProfile() {
      try {
        if (!this.user) return
        const response = await apiClient.get('/users/profile')
        if (response.data.code === 200) {
          const wechatNickname = this.user.wechatNickname
          const wechatAvatar = this.user.wechatAvatar
          this.user = response.data.data
          if (wechatNickname) this.user.wechatNickname = wechatNickname
          if (wechatAvatar) this.user.wechatAvatar = wechatAvatar
          localStorage.setItem('user', JSON.stringify(this.user))
        }
      } catch (error) {
        console.error('Get profile error:', error)
      }
    },
    async updateProfile(userData) {
      try {
        const response = await apiClient.put('/users/profile', userData)
        if (response.data.code === 200) {
          this.user = response.data.data
          localStorage.setItem('user', JSON.stringify(this.user))
          return true
        }
        return false
      } catch (error) {
        console.error('Update profile error:', error)
        return false
      }
    },
    async changePassword(oldPassword, newPassword) {
      try {
        if (!this.user) return false
        const response = await apiClient.put('/users/password', {
          oldPassword,
          newPassword
        })
        if (response.data.code === 200) {
          return true
        }
        ElMessage.error(response.data.message || '修改密码失败')
        return false
      } catch (error) {
        console.error('Change password error:', error)
        ElMessage.error('修改密码失败，请稍后重试')
        return false
      }
    },
    logout() {
      wsManager.disconnect()
      this.user = null
      this.token = null
      this.isLoggedIn = false
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }
})