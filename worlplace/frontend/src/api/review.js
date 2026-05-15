import request from '@/utils/request'

export const createReview = (data) => {
  return request({
    url: '/product-review/',
    method: 'post',
    data
  })
}

export const getProductReviews = (productId, params) => {
  return request({
    url: `/product-review/product/${productId}`,
    method: 'get',
    params
  })
}

export const getMyReviews = (params) => {
  return request({
    url: '/product-review/my',
    method: 'get',
    params
  })
}

export const deleteReview = (reviewId) => {
  return request({
    url: `/product-review/${reviewId}`,
    method: 'delete'
  })
}
