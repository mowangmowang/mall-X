import request from '../utils/request'

export function fetchHomeContent() {
  return request({
    url: '/home/content',
    method: 'get'
  })
}

export function fetchRecommendProductList(params) {
  return request({
    url: '/home/recommendProductList',
    method: 'get',
    params
  })
}
