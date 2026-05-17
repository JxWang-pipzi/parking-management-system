import { defineStore } from 'pinia'
import apiClient from '../utils/api'
import { ElMessage } from 'element-plus'

export const useParkingStore = defineStore('parking', {
  state: () => ({
    parkingLots: [],
    currentParkingLot: null,
    parkingSpaces: []
  }),
  getters: {
    availableParkingLots: (state) => state.parkingLots.filter(lot => lot.status === 1),
    getParkingLotById: (state) => (id) => state.parkingLots.find(lot => lot.id === id),
    getParkingLotName: (state) => (id) => {
      const lot = state.parkingLots.find(l => l.id === id)
      return lot ? lot.name : '-'
    }
  },
  actions: {
    async getParkingLots(params = {}) {
      try {
        const response = await apiClient.get('/parking-lots', { params })
        if (response.data && response.data.code === 200) {
          this.parkingLots = Array.isArray(response.data.data) ? response.data.data : []
        }
      } catch (error) {
        console.error('获取停车场数据失败:', error)
        ElMessage.error('获取停车场数据失败，请检查后端服务是否正常运行')
        this.parkingLots = []
      }
    },
    async fetchParkingLots(params = {}) {
      return await this.getParkingLots(params)
    },
    async addParkingLot(lotData) {
      try {
        const response = await apiClient.post('/parking-lots', lotData)
        if (response.data.code === 200) {
          this.parkingLots.push(response.data.data)
          return true
        }
      } catch (error) {
        console.error('添加停车场失败:', error)
        ElMessage.error('添加停车场失败，请检查后端服务是否正常运行')
      }
      return false
    },
    async updateParkingLot(id, lotData) {
      try {
        const response = await apiClient.put(`/parking-lots/${id}`, lotData)
        if (response.data.code === 200) {
          const index = this.parkingLots.findIndex(lot => lot.id === id)
          if (index !== -1) {
            this.parkingLots[index] = response.data.data
          }
          return true
        }
      } catch (error) {
        console.error('更新停车场失败:', error)
        ElMessage.error('更新停车场失败，请检查后端服务是否正常运行')
      }
      return false
    },
    async deleteParkingLot(id) {
      try {
        const response = await apiClient.delete(`/parking-lots/${id}`)
        if (response.data.code === 200) {
          const index = this.parkingLots.findIndex(lot => lot.id === id)
          if (index !== -1) {
            this.parkingLots.splice(index, 1)
          }
          return true
        }
      } catch (error) {
        console.error('删除停车场失败:', error)
        ElMessage.error('删除停车场失败，请检查后端服务是否正常运行')
      }
      return false
    },
    async fetchParkingLotById(id) {
      try {
        const response = await apiClient.get(`/parking-lots/${id}`)
        if (response.data.code === 200) {
          this.currentParkingLot = response.data.data
        }
      } catch (error) {
        console.error('获取停车场详情失败:', error)
        ElMessage.error('获取停车场详情失败，请检查后端服务是否正常运行')
      }
    },
    async getParkingSpaces() {
      try {
        const response = await apiClient.get('/parking-spaces')
        if (response.data.code === 200) {
          this.parkingSpaces = Array.isArray(response.data.data) ? response.data.data : []
        }
      } catch (error) {
        console.error('获取车位数据失败:', error)
        ElMessage.error('获取车位数据失败，请检查后端服务是否正常运行')
        this.parkingSpaces = []
      }
    },
    async getParkingSpacesByLot(parkingLotId) {
      try {
        const response = await apiClient.get(`/parking-lots/${parkingLotId}/spaces`)
        if (response.data.code === 200) {
          this.parkingSpaces = Array.isArray(response.data.data) ? response.data.data : []
        }
      } catch (error) {
        console.error('获取车位数据失败:', error)
        ElMessage.error('获取车位数据失败，请检查后端服务是否正常运行')
        this.parkingSpaces = []
      }
    },
    async addParkingSpace(spaceData) {
      try {
        const response = await apiClient.post('/parking-spaces', spaceData)
        if (response.data.code === 200) {
          this.parkingSpaces.push(response.data.data)
          return true
        }
      } catch (error) {
        console.error('添加车位失败:', error)
        ElMessage.error('添加车位失败')
      }
      return false
    },
    async updateParkingSpace(id, spaceData) {
      try {
        const response = await apiClient.put(`/parking-spaces/${id}`, spaceData)
        if (response.data.code === 200) {
          const index = this.parkingSpaces.findIndex(s => s.id === id)
          if (index !== -1) {
            this.parkingSpaces[index] = response.data.data
          }
          return true
        }
      } catch (error) {
        console.error('更新车位失败:', error)
        ElMessage.error('更新车位失败')
      }
      return false
    },
    async deleteParkingSpace(id) {
      try {
        const response = await apiClient.delete(`/parking-spaces/${id}`)
        if (response.data.code === 200) {
          const index = this.parkingSpaces.findIndex(s => s.id === id)
          if (index !== -1) {
            this.parkingSpaces.splice(index, 1)
          }
          return true
        }
      } catch (error) {
        console.error('删除车位失败:', error)
        ElMessage.error('删除车位失败')
      }
      return false
    },
    async reserveSpace(spaceId) {
      try {
        const response = await apiClient.post(`/parking-spaces/${spaceId}/reserve`)
        return response.data.code === 200
      } catch (error) {
        console.error('预约车位失败:', error)
        ElMessage.error('预约车位失败，请检查后端服务是否正常运行')
        return false
      }
    },
    async releaseSpace(spaceId) {
      try {
        const response = await apiClient.post(`/parking-spaces/${spaceId}/release`)
        return response.data.code === 200
      } catch (error) {
        console.error('释放车位失败:', error)
        ElMessage.error('释放车位失败，请检查后端服务是否正常运行')
        return false
      }
    }
  }
})
