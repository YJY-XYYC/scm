import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}'),
    permissions: JSON.parse(localStorage.getItem('permissions') || '[]'),
    menus: JSON.parse(localStorage.getItem('menus') || '[]')
  }),
  
  actions: {
    setToken(token) {
      this.token = token
      localStorage.setItem('token', token)
    },
    
    setUserInfo(info) {
      this.userInfo = info
      localStorage.setItem('userInfo', JSON.stringify(info))
    },
    
    setPermissions(permissionList) {
      this.permissions = permissionList
      localStorage.setItem('permissions', JSON.stringify(permissionList))
    },
    
    setMenus(menuList) {
      this.menus = menuList
      localStorage.setItem('menus', JSON.stringify(menuList))
    },
    
    logout() {
      this.token = ''
      this.userInfo = {}
      this.permissions = []
      this.menus = []
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      localStorage.removeItem('permissions')
      localStorage.removeItem('menus')
    }
  }
})