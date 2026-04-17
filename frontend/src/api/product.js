import request from '@/utils/request'

// 获取商品列表
export function getProductList(params) {
  return request({
    url: '/product/list',
    method: 'get',
    params
  })
}

// 获取商品详情
export function getProductDetail(id) {
  return request({
    url: `/product/${id}`,
    method: 'get'
  })
}

// 新增商品
export function addProduct(data) {
  return request({
    url: '/product',
    method: 'post',
    data
  })
}

// 更新商品
export function updateProduct(data) {
  return request({
    url: '/product',
    method: 'put',
    data
  })
}

// 删除商品
export function deleteProduct(id) {
  return request({
    url: `/product/${id}`,
    method: 'delete'
  })
}

// 更新商品状态
export function updateProductStatus(id, status) {
  return request({
    url: `/product/${id}/status?status=${status}`,
    method: 'put'
  })
}

// 更新商品库存
export function updateProductStock(id, adjustAmount, remark) {
  // 后端期望接收stock参数，计算最终库存值
  return request({
    url: `/product/${id}/stock`,
    method: 'put',
    params: {
      stock: adjustAmount // 直接传递调整量，后端应该负责计算最终库存
    }
  })
}

// 上传商品图片
export function uploadProductImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/upload',
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data: formData
  })
}

// 获取商品分类
export function getProductCategories() {
  return request({
    url: '/product/category/list',
    method: 'get'
  })
}

// 批量导入商品
export function importProduct(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/product/import',
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data: formData
  })
}

// 导出商品数据
export function exportProduct(params) {
  return request({
    url: '/product/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}