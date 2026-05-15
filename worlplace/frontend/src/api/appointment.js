import request from '@/utils/request'

// 创建预约
export const createAppointment = (data) => {
  // 确保使用正确的字段名
  const requestData = {
    petId: data.petId,
    serviceId: data.serviceId,
    appointmentDatetime: data.appointmentDate || data.appointmentDatetime, // 兼容两种字段名
    notes: data.notes || ''
  }
  return request({
    url: '/appointment',
    method: 'post',
    data: requestData
  })
}

// 获取我的预约列表
export const getMyAppointments = (params) => {
  return request({
    url: '/appointment/my-list',
    method: 'get',
    params
  })
}

// 获取预约详情
export const getAppointmentDetail = (id) => {
  return request({
    url: `/appointment/${id}`,
    method: 'get'
  })
}

// 取消预约
export const cancelAppointment = (id) => {
  return request({
    url: `/appointment/cancel/${id}`,
    method: 'put'
  })
}

// 获取所有预约列表（管理员）
export const getAllAppointments = (params) => {
  return request({
    url: '/appointment/admin/list',
    method: 'get',
    params
  })
}

// 更新预约状态（管理员）
export const updateAppointmentStatus = (id, status) => {
  return request({
    url: `/appointment/admin/status/${id}`,
    method: 'put',
    params: { status }
  })
}

// 添加评价
export const addReview = (data) => {
  return request({
    url: '/appointment/review',
    method: 'post',
    data
  })
}

// 确认预约
export const confirmAppointment = (id) => {
  return request({
    url: `/appointment/confirm/${id}`,
    method: 'put'
  })
}

// 完成预约
export const completeAppointment = (id) => {
  return request({
    url: `/appointment/complete/${id}`,
    method: 'put'
  })
}

// 获取即将到来的预约
export const getUpcomingAppointments = (params) => {
  return request({
    url: '/appointment/upcoming',
    method: 'get',
    params
  })
}

// 获取历史预约
export const getHistoryAppointments = (params) => {
  return request({
    url: '/appointment/history',
    method: 'get',
    params
  })
}

// 获取预约统计（管理员）
export const getAppointmentStatistics = () => {
  return request({
    url: '/appointment/admin/statistics',
    method: 'get'
  })
}
