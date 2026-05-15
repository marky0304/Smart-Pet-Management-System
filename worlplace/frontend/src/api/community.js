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

// 社区API
export const communityAPI = {
  // 动态相关
  publishPost: (data) => api.post('/community/posts', data),
  getPostList: (params) => api.get('/community/posts', { params }),
  getPostDetail: (postId) => api.get(`/community/posts/${postId}`),
  deletePost: (postId) => api.delete(`/community/posts/${postId}`),
  likePost: (postId) => api.post(`/community/posts/${postId}/like`),
  
  // 评论相关
  addComment: (postId, data) => api.post(`/community/posts/${postId}/comments`, data),
  getCommentList: (postId, params) => api.get(`/community/posts/${postId}/comments`, { params }),
  deleteComment: (commentId) => api.delete(`/community/comments/${commentId}`),
  likeComment: (commentId) => api.post(`/community/comments/${commentId}/like`),
  
  // 话题相关
  getHotTopics: (params) => api.get('/community/topics/hot', { params }),
  searchTopics: (params) => api.get('/community/topics/search', { params }),
  
  // 用户相关
  getRecommendUsers: (params) => api.get('/community/users/recommend', { params }),
  getHotPosts: (params) => api.get('/community/posts/hot', { params })
}

export default communityAPI