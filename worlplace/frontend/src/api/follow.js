import request from '@/utils/request'

export const follow = (userId) => {
  return request({
    url: `/follow/${userId}`,
    method: 'post'
  })
}

export const unfollow = (userId) => {
  return request({
    url: `/follow/${userId}`,
    method: 'delete'
  })
}

export const checkFollow = (userId) => {
  return request({
    url: `/follow/check/${userId}`,
    method: 'get'
  })
}

export const getFollowers = (params) => {
  return request({
    url: '/follow/followers',
    method: 'get',
    params
  })
}

export const getFollowing = (params) => {
  return request({
    url: '/follow/following',
    method: 'get',
    params
  })
}

export const getFollowStats = () => {
  return request({
    url: '/follow/stats',
    method: 'get'
  })
}
