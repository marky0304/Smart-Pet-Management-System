import request from '@/utils/request'

export const getAddresses = () => {
  return request({
    url: '/address',
    method: 'get'
  })
}

export const getAddressById = (id) => {
  return request({
    url: `/address/${id}`,
    method: 'get'
  })
}

export const createAddress = (data) => {
  return request({
    url: '/address',
    method: 'post',
    data
  })
}

export const updateAddress = (id, data) => {
  return request({
    url: `/address/${id}`,
    method: 'put',
    data
  })
}

export const deleteAddress = (id) => {
  return request({
    url: `/address/${id}`,
    method: 'delete'
  })
}

export const setDefaultAddress = (id) => {
  return request({
    url: `/address/default/${id}`,
    method: 'put'
  })
}
