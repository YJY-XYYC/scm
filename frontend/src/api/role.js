import request from '@/utils/request'

// 获取角色列表
export function getRoleList(pageNum = 1, pageSize = 10, keyword = '') {
  return request({
    url: '/role/list',
    method: 'get',
    params: {
      pageNum,
      pageSize,
      keyword
    }
  })
}

export function addRole(data) {
  return request({
    url: '/role',
    method: 'post',
    data
  })
}

export function updateRole(data) {
  return request({
    url: '/role',
    method: 'put',
    data
  })
}

export function deleteRole(id) {
  return request({
    url: `/role/${id}`,
    method: 'delete'
  })
}

// 分配菜单权限
export function assignMenus(data) {
  return request({
    url: '/role/assignMenus',
    method: 'post',
    data
  })
}

// 获取角色的菜单权限
export function getMenuIdsByRoleId(roleId) {
  return request({
    url: `/role/${roleId}/menuIds`,
    method: 'get'
  })
}

// 获取所有可用角色
export function getAllActiveRoles() {
  return request({
    url: '/role/all',
    method: 'get'
  })
}