<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span>个人信息</span>
          <el-button type="primary" class="rounded-button" @click="handleEdit">{{ isEditing ? '保存' : '编辑' }}</el-button>
        </div>
      </template>
      <div class="profile-content">
        <div class="avatar-container">
          <ImagePreview 
            :src="userInfo.avatar && userInfo.avatar.trim() !== '' ? (userInfo.avatar.startsWith('/api') ? userInfo.avatar : '/api' + userInfo.avatar) : ''"
            title="头像预览"
          >
            <el-avatar :size="100" :src="userInfo.avatar && userInfo.avatar.trim() !== '' ? (userInfo.avatar.startsWith('/api') ? userInfo.avatar : '/api' + userInfo.avatar) : undefined">
              <span v-if="!userInfo.avatar || userInfo.avatar.trim() === ''">{{ userInfo.realName?.charAt(0) || userInfo.username?.charAt(0) || '用' }}</span>
            </el-avatar>
          </ImagePreview>
          <el-upload
            v-if="isEditing"
            class="avatar-uploader"
            action="/api/user/avatar"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
            :headers="uploadHeaders"
          >
            <el-button size="small">更换头像</el-button>
          </el-upload>
        </div>
        <el-form
          ref="formRef"
          :model="userInfo"
          :disabled="!isEditing"
          label-width="100px"
        >
          <el-form-item label="用户名">
            <el-input v-model="userInfo.username" disabled />
          </el-form-item>
          <el-form-item label="姓名">
            <el-input v-model="userInfo.name" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="userInfo.phone" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="userInfo.email" />
          </el-form-item>
          <el-form-item label="角色">
            <el-input v-model="userInfo.role" disabled />
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <el-card class="password-card">
      <template #header>
        <div class="card-header">
          <span>修改密码</span>
          <el-button type="primary" class="rounded-button" @click="handleChangePassword">
            修改密码
          </el-button>
        </div>
      </template>
      <div class="password-content">
        <el-form
          ref="passwordFormRef"
          :model="passwordForm"
          :rules="passwordRules"
          label-width="100px"
        >
          <el-form-item label="原密码" prop="oldPassword">
            <el-input
              v-model="passwordForm.oldPassword"
              type="password"
              show-password
            />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input
              v-model="passwordForm.newPassword"
              type="password"
              show-password
            />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="passwordForm.confirmPassword"
              type="password"
              show-password
            />
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getInfo, updateUser, updatePassword } from '@/api/user.js'
import { useUserStore } from '@/stores/user'
import ImagePreview from '@/components/ImagePreview.vue'

// 计算上传请求头，避免直接在模板中访问localStorage
const uploadHeaders = computed(() => {
  const token = typeof localStorage !== 'undefined' ? localStorage.getItem('token') : ''
  return { 'Authorization': 'Bearer ' + token }
})

const userInfo = ref({
  id: '',
  username: '',
  name: '',
  phone: '',
  email: '',
  role: '',
  avatar: ''
})

// 页面加载时获取用户信息
onMounted(() => {
  fetchUserInfo()
})

// 获取用户信息函数
const fetchUserInfo = async () => {
  try {
    // 从localStorage获取用户名，这是登录时存储的
    const username = localStorage.getItem('username')
    if (!username) {
      ElMessage.warning('未找到用户信息，请重新登录')
      return
    }
    
    const res = await getInfo(username)
    if (res.code === 200 || res.success) {
      // 处理头像路径，添加/api前缀以通过代理访问后端静态资源
      let avatarPath = res.data.avatar || ''
      if (avatarPath) {
        // 检查头像路径是否已包含/api前缀
        if (!avatarPath.startsWith('/api')) {
          avatarPath = '/api' + avatarPath
        }
        // 添加时间戳参数以避免浏览器缓存问题
        avatarPath = avatarPath + '?' + new Date().getTime()
      }
      
      // 注意字段映射，后端返回的是realName，前端使用name
      // 保存用户ID，用于后续更新操作
      userInfo.value = {
        id: res.data.id,
        username: res.data.username,
        name: res.data.realName || res.data.name,
        phone: res.data.phone || '',
        email: res.data.email || '',
        role: res.data.role || '',
        avatar: avatarPath
      }
      
      // 同时更新全局用户状态和localStorage
      const userStore = useUserStore()
      const updatedUserInfo = {
        ...userInfo.value,
        // 确保字段名称与store中保持一致
        realName: userInfo.value.name
      }
      userStore.setUserInfo(updatedUserInfo)
      localStorage.setItem('userInfo', JSON.stringify(updatedUserInfo))
    } else {
      ElMessage.error('获取用户信息失败: ' + (res.msg || '未知错误'))
    }
  } catch (error) {
    console.error('获取用户信息异常:', error)
    ElMessage.error('获取用户信息异常，请稍后重试')
  }
}

const isEditing = ref(false)
const formRef = ref(null)

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const handleEdit = async () => {
  if (isEditing.value) {
    // 保存修改
    // 进行表单验证
    if (formRef.value) {
      try {
        await formRef.value.validate()
        // 调用API保存用户信息
        const updateData = {
          // 添加用户ID，确保后端能够正确更新对应的用户记录
          id: userInfo.value.id,
          // 注意字段映射，后端使用realName
          realName: userInfo.value.name,
          phone: userInfo.value.phone,
          email: userInfo.value.email,
          avatar: userInfo.value.avatar
        }
        
        const res = await updateUser(updateData)
        if (res.code === 200 || res.success) {
          ElMessage.success('保存成功')
          isEditing.value = false
        } else {
          ElMessage.error('保存失败: ' + (res.msg || '未知错误'))
        }
      } catch (error) {
        console.error('保存用户信息异常:', error)
        ElMessage.error('保存异常，请稍后重试')
      }
    }
  } else {
    // 进入编辑模式
    isEditing.value = true
  }
}

const handleAvatarSuccess = (response) => {
  // 成功上传后，设置头像路径
  if (response.code === 200) {
    // 添加/api前缀以通过代理访问后端静态资源
    const avatarPath = '/api' + response.data
    // 添加时间戳参数以避免浏览器缓存问题
    const avatarPathWithCacheBuster = avatarPath + '?' + new Date().getTime()
    
    // 更新本地用户信息
    userInfo.value.avatar = avatarPathWithCacheBuster
    
    // 同时更新全局用户状态，确保其他组件也能实时显示新头像
    const userStore = useUserStore()
    const updatedUserInfo = {
      ...userStore.userInfo,
      avatar: avatarPathWithCacheBuster
    }
    userStore.setUserInfo(updatedUserInfo)
    
    // 确保localStorage中的数据也被更新，这样页面刷新后也能显示最新头像
    localStorage.setItem('userInfo', JSON.stringify(updatedUserInfo))
    
    ElMessage.success('头像上传成功')
  } else {
    console.error('头像上传失败:', response)
    ElMessage.error(response.msg || '头像上传失败')
  }
}

const beforeAvatarUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error('上传头像图片只能是 JPG/PNG 格式!')
  }
  if (!isLt2M) {
    ElMessage.error('上传头像图片大小不能超过 2MB!')
  }
  return isJPG && isLt2M
}

const handleChangePassword = async () => {
  if (passwordForm.oldPassword && passwordForm.newPassword && passwordForm.confirmPassword) {
    try {
      // 创建包含用户ID的密码修改数据
      const passwordData = {
        ...passwordForm,
        id: userInfo.value.id  // 添加用户ID，确保后端能正确识别要修改密码的用户
      }
      const res = await updatePassword(passwordData)
      if (res.code === 200 || res.success) {
        ElMessage.success('密码修改成功')
        passwordForm.oldPassword = ''
        passwordForm.newPassword = ''
        passwordForm.confirmPassword = ''
      } else {
        ElMessage.error('密码修改失败: ' + (res.msg || '未知错误'))
      }
    } catch (error) {
      console.error('密码修改异常:', error)
      ElMessage.error('密码修改异常，请稍后重试')
    }
  } else {
    ElMessage.warning('请填写完整的密码信息')
  }
}
</script>

<style lang="scss" scoped>
.profile-container {
  padding: 20px;
  
  /* 个人信息卡片样式 */
  .profile-card {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%);
    color: white;
    border: none;
  }
  
  /* 将密码修改卡片样式设置为与个人信息卡片相同 */
  .password-card {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%);
    color: white;
    border: none;
  }
  
  /* 卡片通用样式 */
  .profile-card,
  .password-card {
    margin-bottom: 20px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }
  
  /* 全局标签样式 */
  label {
    color: white !important;
  }
  
  /* Element UI表单标签样式 */
  :deep(.el-form-item__label) {
    color: white !important;
  }
  
  /* 按钮样式 */
  .rounded-button {
    background-color: #ffffff !important;
    color: #274151 !important;
    border-color: #ffffff !important;
    transition: all 0.3s ease !important;
  }
  
  /* 按钮悬停效果 */
  .rounded-button:hover:not(.is-disabled) {
    background-color: #5a5b5f !important;
    color: #274151 !important;
    border-color: #ffffff !important;
  }
  
  /* 个人信息内容样式 */
  .profile-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    
    .avatar-container {
      margin-bottom: 20px;
      text-align: center;
      
      .avatar-uploader {
        margin-top: 10px;
      }
    }
    
    /* 头像容器样式 */
    .avatar-wrapper {
      position: relative;
      display: inline-block;
    }
    
    /* 预览提示文字 */
    .preview-hint {
      position: absolute;
      bottom: -20px;
      left: 50%;
      transform: translateX(-50%);
      font-size: 12px;
      color: rgba(255, 255, 255, 0.8);
      white-space: nowrap;
    }
    
    .el-form {
      width: 100%;
      max-width: 500px;
    }
  }
  
  /* 输入框通用样式 */
  .profile-card :deep(.el-input__wrapper),
  .password-card :deep(.el-input__wrapper) {
    background-color: #48494c !important;
    border-color: white !important;
  }
  
  .profile-card :deep(.el-input__inner),
  .password-card :deep(.el-input__inner) {
    color: white !important;
  }
  
  /* 输入框聚焦样式 */
  .profile-card :deep(.el-input__wrapper:focus),
  .profile-card :deep(.el-input__wrapper.is-focus),
  .password-card :deep(.el-input__wrapper:focus),
  .password-card :deep(.el-input__wrapper.is-focus) {
    border-color: white !important;
    box-shadow: 0 0 0 1px white inset !important;
  }
  
  /* 密码修改内容样式 */
  .password-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    
    .el-form {
      width: 100%;
      max-width: 500px;
    }
  }
  
  /* 密码修改按钮样式 */
  .password-card .el-button {
    background-color: #ffffff !important;
    color: #274151 !important;
    border-color: #ffffff !important;
    transition: all 0.3s ease !important;
  }
  
  .password-card .el-button:hover:not(.is-disabled) {
    background-color: #5a5b5f !important;
    color: #274151 !important;
    border-color: #ffffff !important;
  }
}
</style>