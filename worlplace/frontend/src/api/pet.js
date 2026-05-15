import request from '@/utils/request'

// 宠物管理API
export const petAPI = {
  // 获取宠物列表
  getPetList: (params) => request.get('/pet/list', { params }),

  // 获取我的宠物列表
  getMyPetList: (params) => request.get('/pet/my', { params }),

  // 获取我的宠物（别名）
  getMyPets: (params) => request.get('/pet/my', { params }),

  // 获取所有宠物列表（管理员）
  getAllPetList: (params) => request.get('/pet/admin/list', { params }),

  // 获取宠物详情
  getPetDetail: (petId) => request.get(`/pet/${petId}`),

  // 创建宠物
  createPet: (data) => request.post('/pet', data),

  // 添加宠物（别名）
  addPet: (data) => request.post('/pet', data),

  // 更新宠物信息
  updatePet: (petId, data) => request.put(`/pet/${petId}`, data),

  // 删除宠物
  deletePet: (petId) => request.delete(`/pet/${petId}`),

  // 审核宠物（管理员）
  auditPet: (petId, status) => request.put(`/pet/admin/${petId}/audit`, { status }),

  // 获取宠物统计信息（管理员）
  getPetStatistics: () => request.get('/pet/admin/statistics'),

  // 获取宠物品种列表
  getPetBreeds: (type) => request.get('/pet/breeds', { params: { type } })
}

// 导出兼容的函数名
export const getMyPetList = petAPI.getMyPetList
export const getMyPets = petAPI.getMyPets
export const getAllPetList = petAPI.getAllPetList
export const getPetDetail = petAPI.getPetDetail
export const addPet = petAPI.addPet
export const updatePet = petAPI.updatePet
export const deletePet = petAPI.deletePet
export const auditPet = petAPI.auditPet
export const getPetStatistics = petAPI.getPetStatistics

export default petAPI
