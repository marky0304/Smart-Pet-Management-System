import request from '@/utils/request'

// 用户注册
export const registerUser = (data) => {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

// 用户登录
export const login = (data) => {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

// 获取用户信息
export const getUserInfo = () => {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

// 更新用户信息
export const updateUser = (data) => {
  return request({
    url: '/user/update',
    method: 'put',
    data
  })
}

// 修改密码
export const updatePassword = (data) => {
  return request({
    url: '/auth/updatePassword',
    method: 'post',
    data
  })
}

// 发送重置密码验证码
export const sendVerifyCode = (account) => {
  return request({
    url: '/auth/sendCode',
    method: 'post',
    params: { account }
  })
}

// 重置密码
export const resetPassword = (data) => {
  return request({
    url: '/auth/resetPassword',
    method: 'post',
    data
  })
}

// 获取用户列表（管理员）
export const getUserList = (params) => {
  return request({
    url: '/user/list',
    method: 'get',
    params
  })
}

// 更新用户状态（管理员）
export const updateUserStatus = (userId, status) => {
  return request({
    url: `/user/status/${userId}`,
    method: 'put',
    params: { status }
  })
}

// 删除用户（管理员）
export const deleteUser = (userId) => {
  return request({
    url: `/user/${userId}`,
    method: 'delete'
  })
}
