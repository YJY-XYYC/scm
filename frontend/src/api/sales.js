import request from '@/utils/request'

// 获取销售统计数据
export function getSalesStatistics(params) {
  return request({
    url: '/sales/statistics',
    method: 'get',
    params
  })
}

// 获取销售明细列表
export function getSalesList(params) {
  return request({
    url: '/sales/list',
    method: 'get',
    params
  })
} 