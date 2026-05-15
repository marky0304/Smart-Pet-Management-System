import request from '@/utils/request'

export const chatAPI = {
  sendMessage: (message, sessionId, currentPage) =>
    request.post('/chat/send', { message, sessionId, currentPage }),

  getHistory: (sessionId) =>
    request.get('/chat/history', { params: { sessionId } })
}

export default chatAPI
