import request from '@/utils/request'

// 创建订单
export const createOrder = (data) => {
  return request({
    url: '/order',
    method: 'post',
    data
  })
}

// 取消订单
export const cancelOrder = (id) => {
  return request({
    url: `/order/cancel/${id}`,
    method: 'put'
  })
}

// 支付订单
export const payOrder = (id) => {
  return request({
    url: `/order/pay/${id}`,
    method: 'put'
  })
}

// 确认收货
export const confirmOrder = (id) => {
  return request({
    url: `/order/confirm/${id}`,
    method: 'put'
  })
}

// 获取订单详情
export const getOrderDetail = (id) => {
  return request({
    url: `/order/${id}`,
    method: 'get'
  })
}

// 获取我的订单列表
export const getMyOrders = (params) => {
  return request({
    url: '/order/my-list',
    method: 'get',
    params
  })
}

// ==================== 管理员功能 ====================

// 获取所有订单
export const getAllOrders = (params) => {
  return request({
    url: '/order/admin/list',
    method: 'get',
    params
  })
}

// 更新订单状态
export const updateOrderStatus = (id, status) => {
  return request({
    url: `/order/admin/status/${id}`,
    method: 'put',
    params: { status }
  })
}

// 发货
export const shipOrder = (id, trackingNumber) => {
  return request({
    url: `/order/admin/ship/${id}`,
    method: 'put',
    params: { trackingNumber }
  })
}

// 用户申请退货
export const returnOrder = (id, reason) => {
  return request({
    url: `/order/return/${id}`,
    method: 'put',
    params: { reason }
  })
}

// 管理员审批退货
export const approveReturn = (id, notes) => {
  return request({
    url: `/order/admin/approve-return/${id}`,
    method: 'put',
    params: { notes: notes || '' }
  })
}

// 管理员拒绝退货
export const rejectReturn = (id, notes) => {
  return request({
    url: `/order/admin/reject-return/${id}`,
    method: 'put',
    params: { notes: notes || '' }
  })
}
