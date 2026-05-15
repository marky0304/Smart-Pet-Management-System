import request from '@/utils/request'

// 获取商品列表
export const getProductList = (params) => {
  return request({
    url: '/product/list',
    method: 'get',
    params
  })
}

// 获取商品详情
export const getProductDetail = (id) => {
  return request({
    url: `/product/${id}`,
    method: 'get'
  })
}

// 获取热销商品
export const getHotProducts = (limit = 8) => {
  return request({
    url: '/product/hot',
    method: 'get',
    params: { limit }
  })
}

// 获取新品推荐
export const getNewProducts = (limit = 8) => {
  return request({
    url: '/product/new',
    method: 'get',
    params: { limit }
  })
}

// ==================== 管理员功能 ====================

// 管理员获取商品列表（含下架）
export const getAdminProductList = (params) => {
  return request({
    url: '/product/admin/list',
    method: 'get',
    params
  })
}

// 创建商品（管理员）
export const createProduct = (data) => {
  return request({
    url: '/product/admin',
    method: 'post',
    data
  })
}

// 更新商品（管理员）
export const updateProduct = (id, data) => {
  return request({
    url: `/product/admin/${id}`,
    method: 'put',
    data
  })
}

// 删除商品（管理员）
export const deleteProduct = (id) => {
  return request({
    url: `/product/admin/${id}`,
    method: 'delete'
  })
}

// 更新商品状态（管理员）
export const updateProductStatus = (id, status) => {
  return request({
    url: `/product/admin/status/${id}`,
    method: 'put',
    params: { status }
  })
}
