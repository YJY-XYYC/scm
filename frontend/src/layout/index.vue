<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '64px' : '200px'" class="aside">
      <div class="logo" :class="{ 'is-collapse': isCollapse }">
        <h2 v-if="!isCollapse">供应链管理系统</h2>
        <h2 v-else>SCM</h2>
      </div>
      <el-menu
        :default-active="route.path"
        router
        :collapse="isCollapse"
        background-color="#343c48"
        text-color="#fff"
        active-text-color="#409EFF"
      >
        <!-- 动态渲染菜单 -->
        <template v-for="menu in dynamicMenus" :key="menu.index">
          <!-- 子菜单 -->
          <el-sub-menu v-if="menu.isSubMenu && menu.children && menu.children.length > 0" :index="menu.index">
            <template #title>
              <el-icon v-if="menu.icon && iconMap[menu.icon]">
                <component :is="iconMap[menu.icon]" />
              </el-icon>
              <span>{{ menu.label }}</span>
            </template>
            <el-menu-item
              v-for="child in menu.children"
              :key="child.index"
              :index="child.index"
            >
              {{ child.label }}
            </el-menu-item>
          </el-sub-menu>
          <!-- 普通菜单项 -->
          <el-menu-item v-else :index="menu.index">
            <el-icon v-if="menu.icon && iconMap[menu.icon]">
              <component :is="iconMap[menu.icon]" />
            </el-icon>
            <template #title>{{ menu.label }}</template>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header>
        <div class="header-left">
          <el-icon class="fold-btn" @click="toggleSidebar">
            <component :is="isCollapse ? 'Expand' : 'Fold'" />
          </el-icon>
          <Breadcrumb />
        </div>
        <div class="header-right">
          <el-dropdown>
            <span class="user-info">
              <el-avatar :size="32" :src="userInfo.avatar && userInfo.avatar.trim() !== '' ? (userInfo.avatar.startsWith('/api') ? userInfo.avatar : '/api' + userInfo.avatar) : undefined" class="user-avatar">
                <span v-if="!userInfo.avatar || userInfo.avatar.trim() === ''">{{ userInfo.realName?.charAt(0) || userInfo.username?.charAt(0) || '用' }}</span>
              </el-avatar>
              <el-icon><CaretBottom /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleProfile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><Close /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main>
        <router-view></router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { useRoute, useRouter } from 'vue-router'
import { House, User, Goods, List, Shop, Expand, Fold, CaretBottom, Money, Setting, Box, DataAnalysis, Management, ShoppingCart } from '@element-plus/icons-vue'
import { ElAvatar } from 'element-plus'
import { ElMessageBox } from 'element-plus'
import Breadcrumb from '@/components/Breadcrumb.vue'

const route = useRoute()
const router = useRouter()
const isCollapse = ref(false)
const isClicked = ref(false)
const userStore = useUserStore()
const userInfo = ref({
  username: 'aaa',
  realName: '用户',
  avatar: ''
})

// 图标映射
const iconMap = {
  'House': House,
  'Setting': Setting,
  'Goods': Goods,
  'List': List,
  'Shop': Shop,
  'Money': Money,
  'Box': Box,
  'DataAnalysis': DataAnalysis,
  'Management': Management,
  'ShoppingCart': ShoppingCart
}

// 计算属性：根据用户权限动态生成菜单
const dynamicMenus = computed(() => {
  // 如果有从后端获取的菜单数据，使用后端数据
  if (userStore.menus && userStore.menus.length > 0) {
    return userStore.menus
  }
  
  // 如果用户是管理员，显示所有菜单
  if (userInfo.value.role === 'ADMIN' || userInfo.value.role === 'admin') {
    return getDefaultMenus()
  }
  
  // 普通用户的默认菜单（可以根据需要调整）
  return getDefaultMenus().filter(menu => {
    // 普通用户只显示首页、商品管理和订单管理
    return menu.index === '/dashboard' || 
           menu.index === '/product/index' || 
           menu.index === '/order/index'
  })
})

// 获取默认菜单配置
function getDefaultMenus() {
  return [
    {
      index: '/dashboard',
      label: '首页',
      icon: 'House',
      isSubMenu: false
    },
    {
      index: '/system',
      label: '系统管理',
      icon: 'Setting',
      isSubMenu: true,
      children: [
        { index: '/system/user', label: '用户管理' },
        { index: '/system/menu', label: '菜单管理' },
        { index: '/system/role', label: '角色管理' },
        { index: '/system/dict', label: '字典管理' }
      ]
    },
    {
      index: '/product/selection',
      label: '商品选购',
      icon: 'ShoppingCart',
      isSubMenu: false
    },
    {
      index: '/product/index',
      label: '商品管理',
      icon: 'Goods',
      isSubMenu: false
    },
    {
      index: '/order/index',
      label: '订单管理',
      icon: 'List',
      isSubMenu: false
    },
    {
      index: '/supplier/index',
      label: '供应商管理',
      icon: 'Shop',
      isSubMenu: false
    },
    {
      index: '/sales/index',
      label: '销售额管理',
      icon: 'Money',
      isSubMenu: false
    },
    {   
      index: '/inventory/index',
      label: '库存管理',
      icon: 'Box',
      isSubMenu: false    
    },
    {   
      index: '/material/index',
      label: '物料管理',
      icon: 'Management',
      isSubMenu: false    
    },
    {      
      index: '/production/index',      
      label: '生产计划管理',      
      icon: 'DataAnalysis',      
      isSubMenu: false    
    }
  ]
}

const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
}

onMounted(() => {
  // 获取用户信息，合并默认值和store中的信息
  if (userStore.userInfo && Object.keys(userStore.userInfo).length > 0) {
    userInfo.value = { ...userInfo.value, ...userStore.userInfo }
  }
  
  // 如果本地没有权限信息，尝试重新获取
  if (!userStore.permissions || userStore.permissions.length === 0) {
    // 可以在这里添加重新获取权限的逻辑
  }
})

const handleProfile = () => {
  router.push('/profile')
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确认退出登录吗？')
    userStore.logout()
    router.push('/login')
  } catch (error) {
    console.error('退出登录失败：', error)
  }
}
</script>

<style lang="scss">
// 移除标签页下面的滑块和滑块所在的导轨
.el-tabs__active-bar {
  display: none !important;
}

// 移除标签导航容器的底部边框（导轨）
.el-tabs__nav-wrap::after {
  display: none !important;
}

// 头像样式
.user-avatar {
  vertical-align: middle;
  margin-right: 4px;
}

// 移除标签头部的底部边框
.el-tabs__header {
  border-bottom: none !important;
}

// 设置标签页未选中状态的文字颜色为白色
.el-tabs__item {
  color: #fff !important;
}

// 设置标签页鼠标悬停效果
.el-tabs__item:hover {
  background-color: #4a5568 !important;
  // color: #409EFF !important;
}

// 设置标签页选中状态的背景颜色和字体颜色与侧边栏li元素相同
.el-tabs__item.is-active {
  background-color: #4a5568 !important;
  color: #409EFF !important;
}

// 系统管理的悬停效果
.el-sub-menu__title:hover {
  background-color: #4a5568 !important;
}
</style>

<style lang="scss" scoped>
.layout-container {
  height: 100vh;
  
  .aside {
    background-color: #343c48;
    transition: width 0.3s;
    overflow: hidden;
    
    .logo {
      height: 60px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #2b2f3a;
      padding: 0 20px;
      transition: all 0.3s;
      
      h2 {
        margin: 0;
        font-size: 18px;
        color: #fff;
        white-space: nowrap;
      }
      
      &.is-collapse {
        padding: 0;
        
        h2 {
          font-size: 14px;
        }
      }
    }
    
    .el-menu {
      border-right: none;
      background-color: #343c48;

      &:not(.el-menu--collapse) {
        width: 200px;
      }
      
      .el-menu-item {
        &:hover, &.is-active {
          background-color: #4a5568 !important;
        }
      }
    }
  }
  
  .el-header {
    padding: 0;
    background-color: #343c48;
    border-bottom: none;
    display: flex;
    justify-content: space-between;
    color: #ffffff;
    
    .header-left {
      display: flex;
      align-items: center;
      height: 100%;
      
      .fold-btn {
        padding: 0 15px;
        font-size: 20px;
        cursor: pointer;
        transition: background .3s;
        color: #fff;
        
        &:hover {
          background: rgba(255,255,255,.1);
          color: #ffffff !important;
        }
      }
    }
    
    .header-right {
      display: flex;
      align-items: center;
      padding-right: 20px;
      
      .user-info {
        display: flex;
        align-items: center;
        cursor: pointer;
        color: #fff;
        
        .el-icon {
          margin-left: 5px;
          color: #fff;
        }
      }
    }
  }
  
  .el-main {
    background-color: #20262e;
    padding: 20px;
    overflow-y: auto;
  }
  
  /* 修改右上角头像下拉菜单背景色 - 最高优先级 */
  .layout-container .el-header .header-right :deep(.el-dropdown .el-popper) {
    background-color: #48494c !important;
    border: none !important;
    --el-popper-bg-color: #48494c !important;
  }
  
  .layout-container .el-header .header-right :deep(.el-dropdown-menu) {
    background-color: #48494c !important;
  }
  
  .layout-container .el-header .header-right :deep(.el-dropdown-menu__item) {
    background-color: #48494c !important;
    color: white !important;
  }
  
  .layout-container .el-header .header-right :deep(.el-dropdown-menu__item:hover) {
    background-color: #5a5b5f !important;
    color: white !important;
  }
  
  /* 确保下拉菜单箭头也使用相同背景色 */
  .layout-container .el-header .header-right :deep(.el-popper__arrow),
  .layout-container .el-header .header-right :deep(.el-popper__arrow::before) {
    background-color: #48494c !important;
    border-color: #48494c !important;
  }
}
</style>