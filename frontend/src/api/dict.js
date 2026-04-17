import request from '@/utils/request'

// 获取字典列表
export function getDictList(pageNum = 1, pageSize = 10) {
  return request({
    url: '/dict/list',
    method: 'get',
    params: {
      pageNum,
      pageSize
    }
  })
}

// 获取字典项列表
export function getDictItemList(dictId) {
  return request({
    url: `/dict/${dictId}/items`,
    method: 'get'
  })
}

// 根据字典编码获取字典项列表
export function getDictItemByCode(dictCode) {
  return request({
    url: `/dict/code/${dictCode}/items`,
    method: 'get'
  })
}

// 添加字典
export function addDict(data) {
  return request({
    url: '/dict',
    method: 'post',
    data
  })
}

// 更新字典
export function updateDict(data) {
  return request({
    url: '/dict',
    method: 'put',
    data
  })
}

// 删除字典
export function deleteDict(id) {
  return request({
    url: `/dict/${id}`,
    method: 'delete'
  })
}

// 添加字典项
export function addDictItem(data) {
  return request({
    url: '/dict/item',
    method: 'post',
    data
  })
}

// 更新字典项
export function updateDictItem(data) {
  return request({
    url: '/dict/item',
    method: 'put',
    data
  })
}

// 删除字典项
export function deleteDictItem(id) {
  return request({
    url: `/dict/item/${id}`,
    method: 'delete'
  })
} 