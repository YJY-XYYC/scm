<template>
  <div class="login-container">
    <el-form
      ref="loginFormRef"
      :model="loginForm"
      :rules="loginRules"
      class="login-form"
      auto-complete="on"
      label-position="left"
    >
      <div class="title-container">
        <h3 class="title">供应链管理(SCM)数字化系统</h3>
      </div>

      <el-form-item prop="username">
        <el-input
          v-model="loginForm.username"
          placeholder="用户名"
          type="text"
          auto-complete="on"
        />
      </el-form-item>

      <el-form-item prop="password">
        <el-input
          v-model="loginForm.password"
          placeholder="密码"
          :type="passwordVisible ? 'text' : 'password'"
          auto-complete="on"
        >
          <template #suffix>
            <el-icon class="cursor-pointer" @click="passwordVisible = !passwordVisible">
              <View v-if="passwordVisible" />
              <Hide v-else />
            </el-icon>
          </template>
        </el-input>
      </el-form-item>

      <el-form-item prop="captcha">
        <el-input
          v-model="loginForm.captcha"
          placeholder="验证码"
          style="width: 60%; display: inline-block;"
        />
        <div class="captcha-image-container" style="display: inline-block; margin-left: 10px; vertical-align: bottom;">
          <img
            :src="captchaUrl"
            @click="refreshCaptcha"
            style="cursor: pointer; height: 40px; border-radius: 4px;"
            alt="验证码"
          />
        </div>
      </el-form-item>

      <el-button
        :loading="loading"
        type="primary"
        style="width: 100%; margin-bottom: 20px"
        @click="handleLogin"
      >
        登录
      </el-button>
      
      <div class="register-link">
        还没有账号？<a href="/scm/register">立即注册</a>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, getInfo, getUserPermissions } from '@/api/user'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const loginFormRef = ref(null)
const loginForm = ref({
  username: '',
  password: '',
  captcha: ''
})

// 添加表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  try {
    await loginFormRef.value.validate()
    
    loading.value = true
    console.log('登录表单数据:', loginForm.value)
    
    const res = await login(loginForm.value)
    
    // 存储token和用户名
    userStore.setToken(res.data.result)
    localStorage.setItem('token', res.data.result)
    localStorage.setItem('username', loginForm.value.username)
    
    // 登录成功后获取用户信息，直接传递登录表单中的用户名
    const infoRes = await getInfo(loginForm.value.username)
    
    // 设置用户信息，与sys_user表字段对应
    // 确保头像为空字符串而不是默认值
    userStore.setUserInfo({
      id: infoRes.data.id,
      username: infoRes.data.username,
      realName: infoRes.data.realName,
      phone: infoRes.data.phone,
      email: infoRes.data.email,
      avatar: infoRes.data.avatar || '',
      role: infoRes.data.role,
      roles: [infoRes.data.role],
      status: infoRes.data.status,
      introduction: `我是${infoRes.data.role === 'ADMIN' ? '管理员' : '普通用户'}`
    })
    
    // 获取用户权限信息
    try {
      const permissionsRes = await getUserPermissions(loginForm.value.username)
      if (permissionsRes.data && permissionsRes.data.permissions) {
        userStore.setPermissions(permissionsRes.data.permissions)
      }
      if (permissionsRes.data && permissionsRes.data.menus) {
        userStore.setMenus(permissionsRes.data.menus)
      }
    } catch (permError) {
      console.error('获取用户权限失败:', permError)
      // 权限获取失败不影响登录，但记录错误
    }
    
    ElMessage.success('登录成功')
    router.push('/')
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error(error.response?.data?.message || '用户名或密码错误')
  } finally {
    loading.value = false
  }
}

// 添加密码可见性控制
const passwordVisible = ref(false)

// 验证码相关
const captchaUrl = ref('')

// 刷新验证码
const refreshCaptcha = () => {
  captchaUrl.value = `http://localhost:8080/scm/api/captcha/generate?timestamp=${Date.now()}`
}

// 页面加载时刷新验证码
onMounted(() => {
  refreshCaptcha()
})
</script>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  /* 可以使用纯色背景 */
  // background-color: #274151;
  
  /* 也可以使用渐变背景（取消下面一行的注释来使用） */
  /* background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); */
  
  /* 使用public文件夹中的背景图片 */
  background-image: url('/images/login.jpg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  background-attachment: fixed;
  background-color: #f0f0f0; /* 备用背景色，防止图片加载失败时显示黑色 */
}

.login-form {
  width: 400px;
  padding: 35px;
  border-radius: 8px;
  background: #4a5568a2;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.title-container {
  text-align: center;
  margin-bottom: 30px;

  .title {
    font-size: 24px;
    color: white;
    margin: 0;
  }
}

// 修改Element Plus输入框wrapper的背景颜色
.login-form :deep(.el-input__wrapper) {
  background-color: #4a556800;
}

// 修改输入框内部文字颜色为白色
.login-form :deep(.el-input__inner) {
  color: white;
}

.register-link {
  text-align: center;
  color: white;
}

.register-link a {
  color: #409eff;
  text-decoration: none;
  margin-left: 5px;
}

.register-link a:hover {
  text-decoration: underline;
}
</style>