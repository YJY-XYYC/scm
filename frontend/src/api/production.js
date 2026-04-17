import request from '@/utils/request'

/**
 * 获取生产计划列表
 * @param {Object} params 查询参数
 * @param {number} params.page 页码
 * @param {number} params.pageSize 每页数量
 * @param {string} params.planName 计划名称
 * @param {string} params.productName 产物名称
 * @param {string} params.status 计划状态
 * @returns {Promise} 请求结果
 */
export function getProductionPlans(params) {
  return request({
    url: '/scm/api/production-plan/list',
    method: 'get',
    params
  })
}

/**
 * 根据ID获取生产计划详情
 * @param {number} id 生产计划ID
 * @returns {Promise} 请求结果
 */
export function getProductionPlanById(id) {
  return request({
    url: `/scm/api/production-plan/${id}`,
    method: 'get'
  })
}

/**
 * 创建新生产计划
 * @param {Object} data 生产计划数据
 * @param {string} data.planName 计划名称
 * @param {string} data.productName 产物名称
 * @param {string} data.requiredMaterials 所需材料
 * @param {number} data.productionTime 制作时间
 * @param {string} data.status 计划状态
 * @returns {Promise} 请求结果
 */
export function createProductionPlan(data) {
  return request({
    url: '/scm/api/production-plan',
    method: 'post',
    data
  })
}

/**
 * 更新生产计划信息
 * @param {Object} data 生产计划数据
 * @param {number} data.id 生产计划ID
 * @param {string} data.planName 计划名称
 * @param {string} data.productName 产物名称
 * @param {string} data.requiredMaterials 所需材料
 * @param {number} data.productionTime 制作时间
 * @param {string} data.status 计划状态
 * @returns {Promise} 请求结果
 */
export function updateProductionPlan(data) {
  return request({
    url: '/scm/api/production-plan',
    method: 'put',
    data
  })
}

/**
 * 更新生产计划状态
 * @param {number} id 生产计划ID
 * @param {string} status 状态值
 * @returns {Promise} 请求结果
 */
export function updateProductionPlanStatus(id, status) {
  return request({
    url: `/scm/api/production-plan/status/${id}`,
    method: 'put',
    params: { status }
  })
}

/**
 * 删除生产计划
 * @param {number} id 生产计划ID
 * @returns {Promise} 请求结果
 */
export function deleteProductionPlan(id) {
  return request({
    url: `/scm/api/production-plan/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除生产计划
 * @param {Array} ids 生产计划ID数组
 * @returns {Promise} 请求结果
 */
export function batchDeleteProductionPlans(ids) {
  return request({
    url: '/scm/api/production/batch',
    method: 'delete',
    data: { ids }
  })
}