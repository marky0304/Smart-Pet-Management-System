import request from '@/utils/request'

export const getNotificationList = (params) => {
  return request({
    url: '/notification/list',
    method: 'get',
    params
  })
}

export const getUnreadCount = () => {
  return request({
    url: '/notification/unread-count',
    method: 'get'
  })
}

export const markAsRead = (id) => {
  return request({
    url: `/notification/read/${id}`,
    method: 'put'
  })
}

export const markAllAsRead = () => {
  return request({
    url: '/notification/read-all',
    method: 'put'
  })
}
