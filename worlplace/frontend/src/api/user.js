import axios from 'axios'

const API_BASE_URL = '/api'

// 创建axios实例
const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    return response
  },
  error => {
    if (error.response?.status === 401) {
      // Token过期，跳转到登录页
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

// 用户管理API
export const userAPI = {
  // 获取用户列表
  getUserList: (params) => api.get('/user/list', { params }),
  
  // 获取用户详情
  getUserDetail: (userId) => api.get(`/user/${userId}`),
  
  // 更新用户信息
  updateUser: (userId, data) => api.put(`/user/${userId}`, data),
  
  // 切换用户状态
  toggleUserStatus: (userId) => api.put(`/user/${userId}/status`),
  
  // 重置用户密码
  resetPassword: (userId) => api.put(`/user/${userId}/password`),
  
  // 删除用户
  deleteUser: (userId) => api.delete(`/user/${userId}`),
  
  // 获取当前用户信息
  getCurrentUser: () => api.get('/user/current'),
  
  // 更新当前用户信息
  updateCurrentUser: (data) => api.put('/user/current', data),
  
  // 获取用户统计信息
  getUserStats: (userId) => api.get(`/user/${userId}/stats`),
  
  // 获取当前用户统计信息
  getCurrentUserStats: () => api.get('/user/current/stats')
}

export default userAPI