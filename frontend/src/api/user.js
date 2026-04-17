import request from '@/utils/request'

export function login(data) {
  // 调用后端真实登录API
  return request({
    url: '/api/auth/login',
    method: 'post',
    data: data
  })
}

export function getInfo(username) {
  // 调用后端获取用户信息API
  // 直接接受username参数，更直观且灵活
  return request({
    url: '/api/user/info',
    method: 'get',
    params: {
      username: username
    }
  })
}

// 退出登录
export function logout() {
  return request({
    url: '/api/auth/logout',
    method: 'post'
  })
}

// 获取用户列表
export function getUserList(params) {
  return request({
    url: '/api/user/list',
    method: 'get',
    params: {
      pageNum: params.pageNum || 1,
      pageSize: params.pageSize || 10,
      username: params.username || ''
    }
  })
}

// 获取用户详情
export function getUserDetail(id) {
  return request({
    url: `/api/user/${id}`,
    method: 'get'
  })
}

// 新增用户
export function addUser(data) {
  return request({
    url: '/api/user',
    method: 'post',
    data
  })
}

// 更新用户
export function updateUser(data) {
  return request({
    url: '/api/user',
    method: 'put',
    data
  })
}

// 删除用户
export function deleteUser(id) {
  return request({
    url: `/api/user/${id}`,
    method: 'delete'
  })
}

// 重置用户密码
export function resetUserPassword(id) {
  return request({
    url: `/api/user/${id}/password`,
    method: 'put'
  })
}

// 更新用户状态
export function updateUserStatus(id, status) {
  return request({
    url: `/api/user/${id}/status`,
    method: 'put',
    params: { status }
  })
}

// 获取用户菜单权限
export function getUserPermissions(username) {
  return request({
    url: '/api/user/permissions',
    method: 'get',
    params: { username }
  })
}

// 获取用户详情
export function getUserInfo(id) {
  return request({
    url: `/api/user/${id}`,
    method: 'get'
  })
}

// 修改密码
export function updatePassword(data) {
  return request({
    url: '/api/user/password',
    method: 'put',
    data
  })
}
