import request from '@/utils/request'

export const chatConfigAPI = {
  getAll: () => request.get('/chat-config'),

  batchSave: (configs) => request.put('/chat-config', configs)
}

export default chatConfigAPI
