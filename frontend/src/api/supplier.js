import request from '@/utils/request'

// 获取供应商列表
export function getSupplierList(params) {
  return request({
    url: '/supplier/list',
    method: 'get',
    params
  })
}

// 根据产品名称获取供应商列表
export function getSuppliersByProductName(productName) {
  return request({
    url: '/supplier/by-product',
    method: 'get',
    params: { productName }
  })
}

// 获取所有产品名称
export function getProductNames() {
  return request({
    url: '/supplier/product-names',
    method: 'get'
  })
}

// 根据产品名称获取属性
export function getPropertyByProductName(productName) {
  return request({
    url: '/supplier/property-by-product',
    method: 'get',
    params: { productName }
  })
}

// 添加供应商
export function addSupplier(data) {
  return request({
    url: '/supplier',
    method: 'post',
    data
  })
}

// 更新供应商
export function updateSupplier(data) {
  return request({
    url: '/supplier',
    method: 'put',
    data: data
  })
}

// 更新供应商状态
export function updateSupplierStatus(id, status) {
  return request({
    url: `/supplier/${id}/status`,
    method: 'put',
    params: { status: status }
  })
}

// 删除供应商
export function deleteSupplier(id) {
  return request({
    url: `/supplier/${id}`,
    method: 'delete'
  })
}

// 获取供应商详情
export function getSupplierDetail(id) {
  return request({
    url: `/supplier/${id}`,
    method: 'get'
  })
}

// 批量删除供应商
export async function batchDeleteSuppliers(ids) {
  // 由于后端没有批量删除接口，这里循环调用单个删除接口
  for (const id of ids) {
    await deleteSupplier(id)
  }
  // 返回成功结果
  return { data: { success: true } }
}

// 更新供应商产品项
export function updateSupplierItem(item) {
  return request({
    url: '/supplier/item',
    method: 'put',
    data: item
  })
}

// 新增供应商产品项
export function addSupplierItem(item) {
  return request({
    url: '/supplier/item',
    method: 'post',
    data: item
  })
}

// 批量导入供应商
// export function importSupplier(file) {
//   const formData = new FormData()
//   formData.append('file', file)
//   return request({
//     url: '/supplier/import',
//     method: 'post',
//     headers: {
//       'Content-Type': 'multipart/form-data'
//     },
//     data: formData
//   })
// }
