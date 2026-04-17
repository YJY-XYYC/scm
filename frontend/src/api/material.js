import request from '@/utils/request'

/**
 * 获取物料列表
 * @param {Object} params 查询参数
 * @param {number} params.page 页码
 * @param {number} params.pageSize 每页数量
 * @param {string} params.materialCode 物料编码
 * @param {string} params.materialName 物料名称
 * @param {string} params.materialType 物料类型
 * @param {string} params.status 状态
 * @returns {Promise} 请求结果
 */
export function getMaterials(params) {
  return request({
    url: '/scm/api/material/list',
    method: 'get',
    params
  })
}

/**
 * 根据ID获取物料详情
 * @param {number} id 物料ID
 * @returns {Promise} 请求结果
 */
export function getMaterialById(id) {
  return request({
    url: `/scm/api/material/${id}`,
    method: 'get'
  })
}

/**
 * 创建新物料
 * @param {Object} data 物料数据
 * @param {string} data.materialCode 物料编码
 * @param {string} data.materialName 物料名称
 * @param {string} data.materialType 物料类型
 * @param {string} data.requiredMaterials 所需材料
 * @param {number} data.requiredTime 所需时间
 * @param {string} data.description 描述
 * @param {string} data.status 状态
 * @returns {Promise} 请求结果
 */
export function createMaterial(data) {
  return request({
    url: '/scm/api/material',
    method: 'post',
    data
  })
}

/**
 * 更新物料信息
 * @param {Object} data 物料数据
 * @param {number} data.id 物料ID
 * @param {string} data.materialName 物料名称
 * @param {string} data.materialType 物料类型
 * @param {string} data.requiredMaterials 所需材料
 * @param {number} data.requiredTime 所需时间
 * @param {string} data.description 描述
 * @param {string} data.status 状态
 * @returns {Promise} 请求结果
 */
export function updateMaterial(data) {
  return request({
    url: '/scm/api/material',
    method: 'put',
    data
  })
}

/**
 * 更新物料状态
 * @param {number} id 物料ID
 * @param {string} status 状态值
 * @returns {Promise} 请求结果
 */
export function updateMaterialStatus(id, status) {
  return request({
    url: `/scm/api/material/${id}/status`,
    method: 'put',
    data: { status }
  })
}

/**
 * 删除物料
 * @param {number} id 物料ID
 * @returns {Promise} 请求结果
 */
export function deleteMaterial(id) {
  return request({
    url: `/scm/api/material/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除物料
 * @param {Array} ids 物料ID数组
 * @returns {Promise} 请求结果
 */
export function batchDeleteMaterials(ids) {
  return request({
    url: '/scm/api/material/batch',
    method: 'delete',
    data: { ids }
  })
}