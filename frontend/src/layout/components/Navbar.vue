<template>
  <div class="navbar">
    <!-- ... 其他内容 -->
    <div class="right-menu">
      <el-dropdown trigger="click">
        <div class="avatar-container">
          <el-avatar :size="30" :src="userInfo.avatar || undefined">
          <span v-if="!userInfo.avatar">{{ userInfo.name?.charAt(0) || '用' }}</span>
        </el-avatar>
          <span class="username">{{ userInfo.name || '用户' }}</span>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="$router.push('/profile')">
              <el-icon><User /></el-icon>个人中心
            </el-dropdown-item>
            <el-dropdown-item divided @click="handleLogout">
              <el-icon><SwitchButton /></el-icon>退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { User, SwitchButton } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
// 移除默认头像图片导入，使用用户名首字母作为默认显示

const router = useRouter()
const userStore = useUserStore()

// 使用computed属性响应式地获取用户信息，确保当userStore更新时，头像能立即更新
const userInfo = computed(() => ({
  name: userStore.userInfo?.realName || userStore.userInfo?.username || '',
  avatar: userStore.userInfo?.avatar || ''
}))

const handleLogout = () => {
  localStorage.removeItem('token')
  router.push('/login')
}
</script>

<style lang="scss" scoped>
.navbar {
  // ... 其他样式
  .right-menu {
    .avatar-container {
      display: flex;
      align-items: center;
      cursor: pointer;
      
      .username {
        margin-left: 8px;
        color: #333;
      }
    }
  }
}
</style>