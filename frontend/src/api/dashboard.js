import request from '@/utils/request'

// 获取统计数据
export function getStatistics(type = 'week', start = null, end = null) {
  const params = { type }
  // 提供了时间范围就添加参数
  if (start && end) {
    params.start = start
    params.end = end
  }
  return request({
    url: '/api/dashboard/statistics',
    method: 'get',
    params
  })
}

// 获取订单趋势
export function getOrderTrend(type = 'week') {
  return request({
    url: '/api/dashboard/order-trend',
    method: 'get',
    params: { type }
  })
}

// 获取用户分布
export function getUserDistribution() {
  return request({
    url: '/api/dashboard/user-distribution',
    method: 'get'
  })
}

// 获取商品分类分布
export function getCategoryDistribution() {
  return request({
    url: '/api/dashboard/category-distribution',
    method: 'get'
  })
}

// 获取最近活动
export function getRecentActivities() {
  return request({
    url: '/api/dashboard/recent-activities',
    method: 'get'
  })
}

// 获取销售趋势
export function getSalesTrend(type = 'week', start = null, end = null) {
  const params = { type }
  // 无论周模式还是月模式，只要提供了时间范围就添加参数
  if (start && end) {
    params.start = start
    params.end = end
  }
  return request({
    url: '/api/dashboard/sales-trend',
    method: 'get',
    params
  })
}