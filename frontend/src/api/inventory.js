import request from '@/utils/request'

// 获取库存列表
export function getInventoryList(params) {
  return request({
    url: '/inventory/list',
    method: 'get',
    params
  })
}

// 调整单个商品库存
export function adjustStock(adjustment) {
  return request({
    url: `/inventory/adjust/${adjustment.productId}`,
    method: 'put',
    params: {
      adjustAmount: adjustment.adjustAmount,
      remark: adjustment.remark
    }
  })
}

// 批量调整库存
export function batchAdjustStock(adjustments) {
  return request({
    url: '/inventory/batch-adjust',
    method: 'put',
    data: adjustments
  })
}

// 获取库存统计信息
export function getInventoryStatistics() {
  return request({
    url: '/inventory/statistics',
    method: 'get'
  })
}

// 获取低库存物品
export function getLowStockProducts() {
  return request({
    url: '/inventory/low-stock',
    method: 'get'
  })
}

// 获取商品分类
export function getProductCategories() {
  return request({
    url: '/inventory/categories',
    method: 'get'
  })
}

// 导出库存数据
export function exportInventoryData(params) {
  return request({
    url: '/inventory/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

// 更新商品禁用状态
export function updateProductForbiddenStatus(productId, forbidden) {
  return request({
    url: '/inventory/product/forbidden',
    method: 'put',
    params: {
      productId,
      forbidden
    }
  })
}

// 获取库存调整日志
export function getInventoryLogs() {
  return request({
    url: '/inventory/logs',
    method: 'get'
  })
}