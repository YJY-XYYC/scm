import request from '@/utils/request'

// 获取订单列表
export function getOrderList(params) {
  return request({
    url: '/order/list',
    method: 'get',
    params
  })
}

// 获取订单详情
export function getOrderDetail(id) {
  return request({
    url: `/order/${id}`,
    method: 'get'
  })
}

// 支付订单
export function payOrder(id) {
  return request({
    url: `/order/${id}/pay`,
    method: 'put'
  })
}

// 发货
export function shipOrder(id) {
  return request({
    url: `/order/${id}/ship`,
    method: 'put'
  })
}

// 取消订单
export function cancelOrder(id) {
  return request({
    url: `/order/${id}/cancel`,
    method: 'put'
  })
}

// 完成订单
export function completeOrder(id) {
  return request({
    url: `/order/${id}/complete`,
    method: 'put'
  })
}

// 删除订单
export function deleteOrder(id) {
  return request({
    url: `/order/${id}`,
    method: 'delete'
  })
}

// 批量删除订单
export function batchDeleteOrders(ids) {
  return request({
    url: '/order/batch',
    method: 'delete',
    data: ids
  })
}

// 添加订单统计的接口
export function getOrderStatistics() {
  return request({
    url: '/order/statistics',
    method: 'get'
  })
}

// 新增订单
export function addOrder(data) {
  return request({
    url: '/order',
    method: 'post',
    data
  })
}

// 检查订单编号是否存在
export function checkOrderNoExists(orderNo) {
  return request({
    url: '/order/check-no',
    method: 'get',
    params: { orderNo }
  })
}
