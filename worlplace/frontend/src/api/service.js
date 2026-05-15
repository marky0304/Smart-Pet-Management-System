import request from '@/utils/request'

// 获取服务列表
export const getServiceList = (params) => {
  return request({
    url: '/service/list',
    method: 'get',
    params
  })
}

// 获取服务详情
export const getServiceDetail = (id) => {
  return request({
    url: `/service/${id}`,
    method: 'get'
  })
}

// 添加服务（管理员）
export const addService = (data) => {
  return request({
    url: '/service/add',
    method: 'post',
    data
  })
}

// 更新服务（管理员）
export const updateService = (id, data) => {
  return request({
    url: `/service/update/${id}`,
    method: 'put',
    data
  })
}

// 删除服务（管理员）
export const deleteService = (id) => {
  return request({
    url: `/service/delete/${id}`,
    method: 'delete'
  })
}
