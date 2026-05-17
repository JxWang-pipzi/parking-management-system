import { defineStore } from 'pinia'
import apiClient from '../utils/api'

export const useOrderStore = defineStore('order', {
  state: () => ({
    orders: [],
    currentOrder: null
  }),
  getters: {
    pendingOrders: (state) => state.orders.filter(order => order.status === 0),
    completedOrders: (state) => state.orders.filter(order => order.status === 1),
    cancelledOrders: (state) => state.orders.filter(order => order.status === 2),
    findOrderById: (state) => (id) => state.orders.find(order => order.id === id)
  },
  actions: {
    async getOrders(status = null) {
      try {
        const params = {}
        if (status !== null) {
          params.status = status
        }
        const response = await apiClient.get('/orders', { params })
        if (response.data && response.data.code === 200) {
          this.orders = Array.isArray(response.data.data) ? response.data.data : []
        }
      } catch (error) {
        console.error('获取订单失败:', error)
        this.orders = []
      }
    },
    async fetchOrders() {
      return await this.getOrders()
    },
    async fetchOrderById(id) {
      try {
        const response = await apiClient.get(`/orders/${id}`)
        if (response.data.code === 200) {
          this.currentOrder = response.data.data
        }
      } catch (error) {
        console.error('Get order error:', error)
      }
    },
    async createOrder(orderData) {
      try {
        const response = await apiClient.post('/orders', orderData)
        if (response.data.code === 200) {
          this.orders.unshift(response.data.data)
          return response.data.data
        }
        return null
      } catch (error) {
        console.error('Create order error:', error)
        return null
      }
    },
    async payOrder(orderId, paymentMethod) {
      try {
        const response = await apiClient.post(`/orders/${orderId}/pay`, { paymentMethod })
        return response.data.code === 200
      } catch (error) {
        console.error('Pay order error:', error)
        return false
      }
    },
    async cancelOrder(orderId) {
      try {
        const response = await apiClient.put(`/orders/${orderId}/cancel`)
        if (response.data.code === 200) {
          const index = this.orders.findIndex(o => o.id === orderId)
          if (index !== -1) {
            this.orders[index].status = 2
          }
          return true
        }
        return false
      } catch (error) {
        console.error('Cancel order error:', error)
        return false
      }
    },
    async completeOrder(orderId) {
      try {
        const response = await apiClient.put(`/orders/${orderId}/complete`)
        if (response.data.code === 200) {
          const index = this.orders.findIndex(o => o.id === orderId)
          if (index !== -1) {
            this.orders[index].status = 1
          }
          return true
        }
        return false
      } catch (error) {
        console.error('Complete order error:', error)
        return false
      }
    }
  }
})