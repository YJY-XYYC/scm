<template>
  <div class="register-container">
    <div class="register-form">
      <div class="title-container">
        <h3 class="title">用户注册</h3>
      </div>
      <el-form ref="registerFormRef" :model="registerForm" :rules="rules" label-width="80px" size="large">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="registerForm.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="请输入密码"></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请确认密码"></el-input>
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="registerForm.realName" placeholder="请输入真实姓名"></el-input>
        </el-form-item>
        <el-form-item label="手机号码" prop="phone">
          <el-input v-model="registerForm.phone" placeholder="请输入手机号码"></el-input>
        </el-form-item>
        <el-form-item label="电子邮箱" prop="email">
          <el-input v-model="registerForm.email" placeholder="请输入电子邮箱"></el-input>
        </el-form-item>
        <el-form-item label="注册身份" prop="role">
          <el-select v-model="registerForm.role" placeholder="请选择注册身份">
            <el-option
              v-for="role in roles"
              :key="role.code"
              :label="role.name"
              :value="role.code"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="用户头像">
          <div class="avatar-uploader">
            <el-upload
              v-model:file-list="avatarFileList"
              class="avatar-uploader"
              action=""
              :auto-upload="false"
              :on-change="handleAvatarChange"
              :show-file-list="false"
              accept=".jpg,.jpeg,.png,.gif"
            >
              <img v-if="registerForm.avatar" :src="registerForm.avatar" class="avatar" />
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
            <div class="avatar-hint">点击上传头像（可选）</div>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRegister" :loading="loading" style="width: 100%">注册</el-button>
        </el-form-item>
        <div class="login-link">
          已有账号？<a href="/scm/login">立即登录</a>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()
const registerFormRef = ref(null)
const loading = ref(false)

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  phone: '',
  email: '',
  avatar: '',
  role: ''
})

// 角色列表
const roles = ref([])

// 获取所有可用角色
const loadRoles = async () => {
  try {
    const response = await request({
      url: '/role/all',
      method: 'get'
    })
    if (response.code === 200) {
      roles.value = response.data
    } else {
      ElMessage.error(response.msg || '获取角色列表失败')
      // 默认角色
      roles.value = [
        { code: 'user', name: '普通用户' }
      ]
    }
  } catch (error) {
    console.error('获取角色列表失败:', error)
    ElMessage.error('获取角色列表失败，请稍后重试')
    // 默认角色
    roles.value = [
      { code: 'user', name: '普通用户' }
    ]
  }
}

const avatarFileList = ref([])

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入电子邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的电子邮箱格式', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

const handleAvatarChange = async (file) => {
  try {
    // 创建FormData对象用于文件上传
    const formData = new FormData()
    formData.append('file', file.raw)
    
    // 上传头像到服务器
    // 移除手动设置的Content-Type，让axios自动处理multipart/form-data
    const response = await request({
      url: '/api/user/avatar',
      method: 'post',
      data: formData
    })
    
    if (response.code === 200) {
      // 保存头像路径到表单，添加/api前缀以通过代理访问后端静态资源
      registerForm.avatar = '/api' + response.data
      ElMessage.success('头像上传成功')
    } else {
      ElMessage.error(response.msg || '头像上传失败')
    }
  } catch (error) {
    console.error('头像上传失败:', error)
    ElMessage.error('头像上传失败，请稍后重试')
  }
}

const handleRegister = async () => {
  try {
    await registerFormRef.value.validate()
    loading.value = true
    
    // 调用注册接口
    const response = await request({
      url: '/api/auth/register',
      method: 'post',
      data: {
        username: registerForm.username,
        password: registerForm.password,
        realName: registerForm.realName,
        phone: registerForm.phone,
        email: registerForm.email,
        avatar: registerForm.avatar,
        role: registerForm.role
      }
    })
    
    if (response.code === 200) {
      ElMessage.success('注册成功，请登录')
      // 注册成功后跳转到登录页面
      router.push('/login')
    } else {
      ElMessage.error(response.msg || '注册失败')
    }
  } catch (error) {
    if (error.response) {
      ElMessage.error(error.response.data.msg || '注册失败')
    } else {
      ElMessage.error('注册失败，请稍后重试')
    }
    console.error('注册失败:', error)
  } finally {
    loading.value = false
  }
}

// 页面加载时获取角色列表
onMounted(() => {
  loadRoles()
})
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  /* background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); */
  
  /* 使用public文件夹中的背景图片 */
  background-image: url('/images/login.jpg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  background-attachment: fixed;
  background-color: #f0f0f0; /* 备用背景色，防止图片加载失败时显示黑色 */
  
  display: flex;
  align-items: center;
  justify-content: center;
  /* padding: 20px; */
}

.register-form {
  background-color: #4a5568a2;
  padding: 40px;
  border-radius: 10px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(10px);
  width: 100%;
  max-width: 450px;
}

.title-container {
  text-align: center;
  margin-bottom: 30px;
}

.title {
  color: white;
  font-size: 28px;
  font-weight: 600;
  margin: 0;
}

.login-link {
  text-align: center;
  margin-top: 20px;
  color: white;
}

.login-link a {
  color: #409eff;
  text-decoration: none;
  margin-left: 5px;
}

.login-link a:hover {
  text-decoration: underline;
}

/* 头像上传样式 */
.avatar-uploader {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #409eff;
  cursor: pointer;
}

.avatar-uploader-icon {
  width: 120px;
  height: 120px;
  border: 2px dashed #c0c4cc;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #c0c4cc;
  cursor: pointer;
  background-color: rgba(255, 255, 255, 0.1);
}

.avatar-uploader-icon:hover {
  color: #409eff;
  border-color: #409eff;
}

.avatar-hint {
  margin-top: 10px;
  color: #b2bec3;
  font-size: 14px;
}

/* 修改输入框和选择器样式 */
.register-form :deep(.el-input__wrapper),
.register-form :deep(.el-select__wrapper) {
  background-color: #4a556800;
  border-color: #636e72;
}

.register-form :deep(.el-input__inner),
.register-form :deep(.el-select__input) {
  color: white;
}

.register-form :deep(.el-input__inner::placeholder),
.register-form :deep(.el-select__placeholder) {
  color: #b2bec3;
}

.register-form :deep(.el-input__wrapper.is-focus),
.register-form :deep(.el-select__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409eff inset;
  border-color: #409eff;
}

/* 修改表单标签样式 */
.register-form :deep(.el-form-item__label) {
  color: white;
}
</style>