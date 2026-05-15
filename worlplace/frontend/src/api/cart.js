import request from '@/utils/request'

export const getCartItems = () => {
  return request({ url: '/cart', method: 'get' })
}

export const addToCart = (data) => {
  return request({ url: '/cart', method: 'post', data })
}

export const updateCartQuantity = (id, quantity) => {
  return request({ url: `/cart/${id}`, method: 'put', params: { quantity } })
}

export const removeCartItem = (id) => {
  return request({ url: `/cart/${id}`, method: 'delete' })
}

export const clearCart = () => {
  return request({ url: '/cart/clear', method: 'delete' })
}
